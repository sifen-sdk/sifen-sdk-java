/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.sifensdk.core.internal.keys.SifenKeysProvider;
import io.github.sifensdk.core.internal.mapping.Samples;
import io.github.sifensdk.core.model.Conformity;
import io.github.sifensdk.core.model.Event;
import io.github.sifensdk.core.signing.CertificateChecks;
import io.github.sifensdk.core.signing.Csc;
import io.github.sifensdk.core.signing.KeyMaterial;
import io.github.sifensdk.core.signing.KeyMaterials;
import io.github.sifensdk.core.signing.Pkcs11Config;
import io.github.sifensdk.core.signing.RemoteSigner;
import io.github.sifensdk.core.signing.SoftHsm;
import io.github.sifensdk.core.validation.Severity;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/**
 * Keys that never leave where they live (ADR-0012): a PKCS#11 token (SoftHSM2) and a remote signer
 * sign exactly like the same key in a file, and the signer's certificate is checked before signing.
 */
class ExternalKeysTest {

  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-11-26T13:05:00Z"), ZoneOffset.UTC);
  private static final char[] PASSWORD = "prueba-sintetica".toCharArray();
  private static final byte[] SHA256_DIGEST_INFO =
      HexFormat.of().parseHex("3031300d060960864801650304020105000420");

  private final SifenDocuments documents = documents(CLOCK);
  private final SifenEvents events =
      SifenEvents.builder().environment(Environment.TEST).clock(CLOCK).build();

  private static SifenDocuments documents(Clock clock) {
    return SifenDocuments.builder().environment(Environment.TEST).clock(clock).build();
  }

  private static KeyMaterial fileKey() {
    return KeyMaterials.pkcs12(signing("prueba.p12"), PASSWORD);
  }

  private static Path signing(String name) {
    try {
      return Path.of(
          Objects.requireNonNull(ExternalKeysTest.class.getResource("/signing/" + name), name)
              .toURI());
    } catch (java.net.URISyntaxException e) {
      throw new IllegalStateException(e);
    }
  }

  private static Csc csc() {
    return Csc.of("0001", "ABCD0000000000000000000000000000".toCharArray());
  }

  private static Event event() {
    return Event.builder()
        .id("1")
        .detail(Conformity.builder().cdc(SifenEventsTest.cdc()).typeCode("1").build())
        .build();
  }

  // --- PKCS#11 ----------------------------------------------------------------------------------

  @Test
  void aTokenKeySignsDocumentsAndEventsExactlyLikeTheSameKeyInAFile() {
    long slot = SoftHsm.singleKeyToken();
    KeyMaterial token =
        KeyMaterials.pkcs11(Pkcs11Config.slot(SoftHsm.library(), slot), SoftHsm.PIN);

    assertThat(token.privateKey().getEncoded()).as("the key never leaves the token").isNull();
    assertThat(token.certificate()).isEqualTo(fileKey().certificate());
    assertThat(token.toString()).doesNotContain(new String(SoftHsm.PIN));
    // RSASSA-PKCS1-v1_5 is deterministic: same key, same clock, same bytes.
    assertThat(documents.sign(Samples.invoice(), token, csc()).xml())
        .isEqualTo(documents.sign(Samples.invoice(), fileKey(), csc()).xml());
    assertThat(events.sign(event(), token).xml()).isEqualTo(events.sign(event(), fileKey()).xml());
    assertThat(Security.getProvider(SifenKeysProvider.NAME))
        .as("signing XML installs no provider")
        .isNull();
  }

  @Test
  void aTokenWithSeveralKeysNeedsTheAlias() {
    KeyMaterial other = KeyMaterials.pkcs12(signing("dos-alias.p12"), PASSWORD, "otra");
    long slot = SoftHsm.twoKeyToken();
    Pkcs11Config config = Pkcs11Config.slot(SoftHsm.library(), slot);

    assertThatThrownBy(() -> KeyMaterials.pkcs11(config, SoftHsm.PIN))
        .isInstanceOf(SigningException.class)
        .hasMessageContaining("choose one by its alias");
    assertThat(KeyMaterials.pkcs11(config, SoftHsm.PIN, "otra").certificate())
        .isEqualTo(other.certificate());
  }

  @Test
  void aKeyStoreOfAProviderThatIsNotInstalledSignsToo() throws GeneralSecurityException {
    long slot = SoftHsm.singleKeyToken();
    Provider unregistered = configure(SoftHsm.library(), slot);
    KeyStore store = KeyStore.getInstance("PKCS11", unregistered);
    try {
      store.load(null, SoftHsm.PIN);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

    KeyMaterial token = KeyMaterials.keyStore(store, "firma", null);

    assertThat(documents.sign(Samples.invoice(), token, csc()).xml())
        .isEqualTo(documents.sign(Samples.invoice(), fileKey(), csc()).xml());
  }

  private static Provider configure(Path library, long slot) {
    String text = "--name = Prueba\nlibrary = \"" + library + "\"\nslot = " + slot + "\n";
    return Security.getProvider("SunPKCS11").configure(text);
  }

  @Test
  void aWrongPinIsReportedWithoutTheValue() {
    // PKCS#11 login is per process: a token already logged in does not check the PIN again.
    long slot = SoftHsm.emptyToken();
    char[] wrong = "9876".toCharArray();

    assertThatThrownBy(() -> KeyMaterials.pkcs11(Pkcs11Config.slot(SoftHsm.library(), slot), wrong))
        .isInstanceOf(SigningException.class)
        .hasMessageContaining("PIN is wrong")
        .satisfies(e -> assertThat(e.getMessage()).doesNotContain("9876"));
  }

  @Test
  void aMissingLibraryIsReportedClearly() {
    assertThatThrownBy(() ->
            KeyMaterials.pkcs11(
                Pkcs11Config.library(Path.of("/no/existe/libtoken.so")), SoftHsm.PIN))
        .isInstanceOf(SigningException.class)
        .hasMessageContaining("library does not exist");
  }

  // --- Remote signer ----------------------------------------------------------------------------

  /** An in-process "remote" signer over the synthetic key; records what it receives. */
  private static final class RecordingSigner implements RemoteSigner {

    final List<byte[]> received = new ArrayList<>();
    private final KeyMaterial key = fileKey();

    @Override
    public List<X509Certificate> certificateChain() {
      return key.certificateChain();
    }

    @Override
    public synchronized byte[] signSha256Digest(byte[] sha256Digest) {
      received.add(sha256Digest.clone());
      try {
        Signature raw = Signature.getInstance("NONEwithRSA");
        raw.initSign(key.privateKey());
        raw.update(SHA256_DIGEST_INFO);
        raw.update(sha256Digest);
        return raw.sign();
      } catch (GeneralSecurityException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  @Test
  void aRemoteSignerOnlyReceivesDigestsAndSignsLikeTheFileKey() {
    RecordingSigner signer = new RecordingSigner();
    KeyMaterial remote = KeyMaterials.remote(signer);

    assertThat(documents.sign(Samples.invoice(), remote, csc()).xml())
        .isEqualTo(documents.sign(Samples.invoice(), fileKey(), csc()).xml());
    assertThat(events.sign(event(), remote).xml()).isEqualTo(events.sign(event(), fileKey()).xml());
    assertThat(signer.received).hasSize(2).allSatisfy(d -> assertThat(d).hasSize(32));
  }

  @Test
  void aFailingRemoteSignerIsASigningFailureWithItsCause() {
    IllegalStateException unavailable = new IllegalStateException("service unavailable");
    KeyMaterial remote =
        KeyMaterials.remote(
            new RemoteSigner() {
              @Override
              public List<X509Certificate> certificateChain() {
                return fileKey().certificateChain();
              }

              @Override
              public byte[] signSha256Digest(byte[] sha256Digest) {
                throw unavailable;
              }
            });

    assertThatThrownBy(() -> documents.sign(Samples.invoice(), remote, csc()))
        .isInstanceOf(SigningException.class)
        .hasRootCause(unavailable);
  }

  @Test
  void aRemoteSignatureOfTheWrongLengthIsRejected() {
    KeyMaterial remote =
        KeyMaterials.remote(
            new RemoteSigner() {
              @Override
              public List<X509Certificate> certificateChain() {
                return fileKey().certificateChain();
              }

              @Override
              public byte[] signSha256Digest(byte[] sha256Digest) {
                return new byte[10];
              }
            });

    assertThatThrownBy(() -> documents.sign(Samples.invoice(), remote, csc()))
        .isInstanceOf(SigningException.class)
        .hasStackTraceContaining("10 bytes");
  }

  @Test
  void aRemoteSignatureWithAnotherKeyIsRejected() {
    KeyMaterial other = KeyMaterials.pkcs12(signing("dos-alias.p12"), PASSWORD, "otra");
    KeyMaterial remote =
        KeyMaterials.remote(
            new RemoteSigner() {
              @Override
              public List<X509Certificate> certificateChain() {
                return fileKey().certificateChain();
              }

              @Override
              public byte[] signSha256Digest(byte[] sha256Digest) {
                try {
                  Signature raw = Signature.getInstance("NONEwithRSA");
                  raw.initSign(other.privateKey());
                  raw.update(SHA256_DIGEST_INFO);
                  raw.update(sha256Digest);
                  return raw.sign();
                } catch (GeneralSecurityException e) {
                  throw new IllegalStateException(e);
                }
              }
            });

    assertThatThrownBy(() -> documents.sign(Samples.invoice(), remote, csc()))
        .isInstanceOf(SigningException.class)
        .hasStackTraceContaining("does not verify with the certificate");
  }

  // --- Certificate checks -----------------------------------------------------------------------

  @Test
  void anExpiredOrNotYetValidCertificateDoesNotSign() {
    SifenDocuments after =
        documents(Clock.fixed(Instant.parse("2046-01-02T12:00:00Z"), ZoneOffset.UTC));
    SifenEvents before =
        SifenEvents.builder()
            .environment(Environment.TEST)
            .clock(Clock.fixed(Instant.parse("2025-12-31T12:00:00Z"), ZoneOffset.UTC))
            .build();

    assertThatThrownBy(() -> after.sign(Samples.invoice(), fileKey(), csc()))
        .isInstanceOf(SigningException.class)
        .hasMessageContaining("expired");
    assertThatThrownBy(() -> before.sign(event(), fileKey()))
        .isInstanceOf(SigningException.class)
        .hasMessageContaining("cannot sign before");
  }

  @Test
  void aCertificateWithoutSigningUsageDoesNotSign() throws Exception {
    X509Certificate noSigning;
    try (InputStream in =
        ExternalKeysTest.class.getResourceAsStream("/signing/sin-uso-de-firma.pem")) {
      noSigning = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(in);
    }
    KeyMaterial key = KeyMaterials.of(fileKey().privateKey(), List.of(noSigning));

    assertThat(CertificateChecks.check(key, CLOCK.instant(), null))
        .singleElement()
        .satisfies(i -> assertThat(i.code()).isEqualTo(CertificateChecks.NO_SIGNING_USAGE));
    assertThatThrownBy(() -> documents.sign(Samples.invoice(), key, csc()))
        .isInstanceOf(SigningException.class)
        .hasMessageContaining("does not allow digital signature");
  }

  @Test
  void anotherRucInTheCertificateIsOnlyAWarning() {
    assertThat(CertificateChecks.check(fileKey(), CLOCK.instant(), "80000000-1"))
        .singleElement()
        .satisfies(i -> {
          assertThat(i.severity()).isEqualTo(Severity.WARNING);
          assertThat(i.isError()).isFalse();
          assertThat(i.code()).isEqualTo(CertificateChecks.RUC_MISMATCH);
        });
    assertThat(CertificateChecks.check(fileKey(), CLOCK.instant(), "44444401-7")).isEmpty();
    assertThat(CertificateChecks.check(fileKey(), CLOCK.instant(), "44444401")).isEmpty();
    assertThat(CertificateChecks.check(fileKey(), CLOCK.instant(), null)).isEmpty();
  }
}
