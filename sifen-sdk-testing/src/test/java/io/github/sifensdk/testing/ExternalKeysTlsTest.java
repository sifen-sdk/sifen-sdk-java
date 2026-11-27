/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.testing;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.sifensdk.client.ReceptionResult;
import io.github.sifensdk.client.SifenClient;
import io.github.sifensdk.client.Status;
import io.github.sifensdk.core.Environment;
import io.github.sifensdk.core.signing.KeyMaterial;
import io.github.sifensdk.core.signing.KeyMaterials;
import io.github.sifensdk.core.signing.Pkcs11Config;
import io.github.sifensdk.core.signing.RemoteSigner;
import io.github.sifensdk.core.signing.SignedDocument;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Mutual TLS with keys that never leave where they live (ADR-0012): a PKCS#11 token (SoftHSM2) and
 * a remote signer authenticate the connection to the simulated SIFEN, and a key in a file keeps
 * working in the same process.
 */
class ExternalKeysTlsTest {

  private static final byte[] SHA256_DIGEST_INFO =
      HexFormat.of().parseHex("3031300d060960864801650304020105000420");

  private SimulatedSifen sifen;

  @BeforeEach
  void start() {
    sifen = new SimulatedSifen().start();
  }

  @AfterEach
  void stop() {
    sifen.close();
  }

  private SifenClient client(KeyMaterial keyMaterial) {
    return SifenClient.builder()
        .environment(Environment.TEST)
        .keyMaterial(keyMaterial)
        .baseUri(sifen.baseUri())
        .trustStore(sifen.serverTrustStore())
        .build();
  }

  private static SignedDocument signed() {
    return SignedDocument.of(
        Requests.rde(Requests.CDC).getBytes(StandardCharsets.UTF_8),
        Requests.CDC,
        "https://ekuatia.set.gov.py/consultas-test/qr?nVersion=150",
        LocalDateTime.of(2026, 11, 26, 10, 0));
  }

  private void sendsAndPresentsTheCertificate(KeyMaterial keyMaterial) {
    ReceptionResult result = client(keyMaterial).send(signed());

    assertThat(result.document().status()).isEqualTo(Status.APPROVED);
    assertThat(sifen.recordedRequests(SimulatedSifen.RECEPTION))
        .last()
        .satisfies(r -> assertThat(r.clientCertificate()).isEqualTo(SyntheticClient.certificate()));
  }

  @Test
  void aTokenKeyAuthenticatesTheConnection() {
    KeyMaterial token =
        KeyMaterials.pkcs11(
            Pkcs11Config.slot(SoftHsm.library(), SoftHsm.singleKeyToken()), SoftHsm.PIN);

    sendsAndPresentsTheCertificate(token);
    sendsAndPresentsTheCertificate(SyntheticClient.keyMaterial());
  }

  @Test
  void aRemoteSignerAuthenticatesTheConnectionWithDigestsOnly() {
    List<Integer> received = new ArrayList<>();
    KeyMaterial file = SyntheticClient.keyMaterial();
    KeyMaterial remote =
        KeyMaterials.remote(
            new RemoteSigner() {
              @Override
              public List<X509Certificate> certificateChain() {
                return file.certificateChain();
              }

              @Override
              public synchronized byte[] signSha256Digest(byte[] sha256Digest) {
                received.add(sha256Digest.length);
                try {
                  Signature raw = Signature.getInstance("NONEwithRSA");
                  raw.initSign(file.privateKey());
                  raw.update(SHA256_DIGEST_INFO);
                  raw.update(sha256Digest);
                  return raw.sign();
                } catch (GeneralSecurityException e) {
                  throw new IllegalStateException(e);
                }
              }
            });

    sendsAndPresentsTheCertificate(remote);
    sendsAndPresentsTheCertificate(file);
    assertThat(received).isNotEmpty().allSatisfy(n -> assertThat(n).isEqualTo(32));
  }
}
