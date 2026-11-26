/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.signing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.sifensdk.core.SigningException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class KeyMaterialsTest {

  static final char[] PASSWORD = "prueba-sintetica".toCharArray();
  static final String SUBJECT_RUC = "SERIALNUMBER=RUC44444401-7";

  static Path resource(String name) {
    try {
      return Path.of(
          Objects.requireNonNull(KeyMaterialsTest.class.getResource("/signing/" + name), name)
              .toURI());
    } catch (URISyntaxException e) {
      throw new IllegalStateException(e);
    }
  }

  static byte[] bytes(String name) throws IOException {
    try (InputStream in =
        Objects.requireNonNull(KeyMaterialsTest.class.getResourceAsStream("/signing/" + name))) {
      return in.readAllBytes();
    }
  }

  static KeyMaterial testKey() {
    return KeyMaterials.pkcs12(resource("prueba.p12"), PASSWORD);
  }

  @Test
  void loadsPkcs12FromPathBytesAndStream() throws IOException {
    KeyMaterial fromPath = testKey();
    KeyMaterial fromBytes = KeyMaterials.pkcs12(bytes("prueba.p12"), PASSWORD);
    KeyMaterial fromStream;
    try (InputStream in = KeyMaterialsTest.class.getResourceAsStream("/signing/prueba.p12")) {
      fromStream = KeyMaterials.pkcs12(Objects.requireNonNull(in), PASSWORD, "firma");
    }
    for (KeyMaterial key : new KeyMaterial[] {fromPath, fromBytes, fromStream}) {
      assertThat(key.privateKey().getAlgorithm()).isEqualTo("RSA");
      assertThat(key.certificate().getSubjectX500Principal().toString()).contains(SUBJECT_RUC);
      assertThat(key.certificateChain()).first().isEqualTo(key.certificate());
    }
    assertThat(fromBytes.certificate()).isEqualTo(fromPath.certificate());
  }

  @Test
  void toStringShowsOnlyTheSubject() {
    KeyMaterial key = testKey();
    assertThat(key.toString())
        .contains(SUBJECT_RUC)
        .doesNotContain("prueba-sintetica")
        .doesNotContain("MII");
  }

  @Test
  void wrongPasswordIsAClearError() {
    assertThatThrownBy(() -> KeyMaterials.pkcs12(resource("prueba.p12"), "otra".toCharArray()))
        .isInstanceOf(SigningException.class)
        .hasMessageContaining("password is wrong")
        .hasMessageNotContaining("otra");
  }

  @Test
  void severalKeysNeedAnAlias() {
    assertThatThrownBy(() -> KeyMaterials.pkcs12(resource("dos-alias.p12"), PASSWORD))
        .isInstanceOf(SigningException.class)
        .hasMessageContaining("2 private keys")
        .hasMessageContaining("alias");
    KeyMaterial other = KeyMaterials.pkcs12(resource("dos-alias.p12"), PASSWORD, "otra");
    assertThat(other.certificate().getSubjectX500Principal().toString())
        .contains("SEGUNDA CLAVE SINTETICA");
    assertThatThrownBy(() -> KeyMaterials.pkcs12(resource("dos-alias.p12"), PASSWORD, "nada"))
        .isInstanceOf(SigningException.class);
  }

  @Test
  void loadsFromAKeyStoreWithAlias() throws Exception {
    KeyStore store = KeyStore.getInstance("PKCS12");
    try (InputStream in = KeyMaterialsTest.class.getResourceAsStream("/signing/dos-alias.p12")) {
      store.load(in, PASSWORD);
    }
    KeyMaterial key = KeyMaterials.keyStore(store, "firma", PASSWORD);
    assertThat(key.certificate()).isEqualTo(testKey().certificate());
    assertThatThrownBy(() -> KeyMaterials.keyStore(store, "firma", "mala".toCharArray()))
        .isInstanceOf(SigningException.class)
        .hasMessageContaining("password is wrong");
  }

  @Test
  void loadsPem() throws IOException {
    KeyMaterial fromPaths =
        KeyMaterials.pem(resource("prueba-certificado.pem"), resource("prueba-clave.pem"));
    assertThat(fromPaths.certificate()).isEqualTo(testKey().certificate());
    assertThat(fromPaths.privateKey().getAlgorithm()).isEqualTo("RSA");
    String certificate = new String(bytes("prueba-certificado.pem"), StandardCharsets.US_ASCII);
    String key = new String(bytes("prueba-clave.pem"), StandardCharsets.US_ASCII);
    assertThat(KeyMaterials.pem(certificate, key).certificateChain()).hasSize(1);
  }

  @Test
  void encryptedOrMismatchedPemKeysAreClearErrors() throws IOException {
    String certificate = new String(bytes("prueba-certificado.pem"), StandardCharsets.US_ASCII);
    String encrypted =
        "-----BEGIN ENCRYPTED PRIVATE KEY-----\nMIIBAA==\n-----END ENCRYPTED PRIVATE KEY-----\n";
    assertThatThrownBy(() -> KeyMaterials.pem(certificate, encrypted))
        .isInstanceOf(SigningException.class)
        .hasMessageContaining("Encrypted");
    String pkcs1 = "-----BEGIN RSA PRIVATE KEY-----\nMIIBAA==\n-----END RSA PRIVATE KEY-----\n";
    assertThatThrownBy(() -> KeyMaterials.pem(certificate, pkcs1))
        .isInstanceOf(SigningException.class)
        .hasMessageContaining("PKCS#8");
    assertThatThrownBy(() -> KeyMaterials.pem("no certificate", "no key"))
        .isInstanceOf(SigningException.class)
        .hasMessageContaining("BEGIN CERTIFICATE");
  }

  @Test
  void pemKeyMustBelongToTheCertificate() throws Exception {
    KeyStore store = KeyStore.getInstance("PKCS12");
    try (InputStream in = KeyMaterialsTest.class.getResourceAsStream("/signing/dos-alias.p12")) {
      store.load(in, PASSWORD);
    }
    java.security.PrivateKey otherKey = (java.security.PrivateKey) store.getKey("otra", PASSWORD);
    String otherKeyPem =
        "-----BEGIN PRIVATE KEY-----\n"
            + java.util.Base64.getMimeEncoder().encodeToString(otherKey.getEncoded())
            + "\n-----END PRIVATE KEY-----\n";
    String certificate = new String(bytes("prueba-certificado.pem"), StandardCharsets.US_ASCII);
    assertThatThrownBy(() -> KeyMaterials.pem(certificate, otherKeyPem))
        .isInstanceOf(SigningException.class)
        .hasMessageContaining("does not belong");
  }

  @Test
  void cscHidesItsSecretAndKnowsThePublishedTestCodes() {
    char[] secret = "ABCD0000000000000000000000000000".toCharArray();
    Csc csc = Csc.of("0001", secret);
    secret[0] = 'X';
    assertThat(csc.isPublishedTestCode()).isTrue();
    assertThat(new String(csc.secret())).isEqualTo("ABCD0000000000000000000000000000");
    assertThat(csc.toString()).isEqualTo("Csc[id=0001]");
    assertThat(
            Csc.of("0002", "EFGH0000000000000000000000000000".toCharArray()).isPublishedTestCode())
        .isTrue();
    assertThat(
            Csc.of("0001", "A1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6".toCharArray()).isPublishedTestCode())
        .isFalse();
    assertThatThrownBy(() -> Csc.of("1", secret)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> Csc.of("0001", "ABC".toCharArray()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageNotContaining("ABC");
  }
}
