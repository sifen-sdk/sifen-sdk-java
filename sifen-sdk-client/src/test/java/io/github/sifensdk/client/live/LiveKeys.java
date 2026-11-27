/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.live;

import io.github.sifensdk.core.signing.KeyMaterial;
import io.github.sifensdk.core.signing.KeyMaterials;
import io.github.sifensdk.core.signing.Pkcs11Config;
import io.github.sifensdk.core.signing.RemoteSigner;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

/**
 * The key material of the live tests, by the way it is held ({@code SIFEN_TEST_KEY}):
 *
 * <ul>
 *   <li>{@code file} (default): the PKCS#12 of {@code SIFEN_TEST_P12_PATH} (or {@code
 *       SIFEN_TEST_P12_BASE64}) with {@code SIFEN_TEST_P12_PASSWORD}.
 *   <li>{@code pkcs11}: a token: {@code SIFEN_TEST_PKCS11_LIBRARY}, {@code SIFEN_TEST_PKCS11_SLOT},
 *       {@code SIFEN_TEST_PKCS11_PIN} and optionally {@code SIFEN_TEST_PKCS11_ALIAS}. {@code
 *       scripts/prueba-sifen-token.sh} copies the test certificate to a temporary SoftHSM2 token.
 *   <li>{@code remote}: the PKCS#12 behind an in-process {@link RemoteSigner}, which only receives
 *       digests, as a remote signing service would.
 * </ul>
 */
final class LiveKeys {

  private static final byte[] SHA256_DIGEST_INFO =
      HexFormat.of().parseHex("3031300d060960864801650304020105000420");

  private LiveKeys() {}

  static KeyMaterial key() {
    String mode = System.getenv().getOrDefault("SIFEN_TEST_KEY", "file");
    switch (mode) {
      case "file":
        return file();
      case "pkcs11":
        String alias = System.getenv("SIFEN_TEST_PKCS11_ALIAS");
        return KeyMaterials.pkcs11(
            Pkcs11Config.slot(
                Path.of(env("SIFEN_TEST_PKCS11_LIBRARY")),
                Long.parseLong(env("SIFEN_TEST_PKCS11_SLOT"))),
            env("SIFEN_TEST_PKCS11_PIN").toCharArray(),
            alias == null || alias.isBlank() ? null : alias);
      case "remote":
        return KeyMaterials.remote(new InProcessSigner(file()));
      default:
        throw new IllegalStateException("SIFEN_TEST_KEY is file, pkcs11 or remote");
    }
  }

  /** The way the key is held, for the test's report. */
  static String mode() {
    return System.getenv().getOrDefault("SIFEN_TEST_KEY", "file");
  }

  private static KeyMaterial file() {
    char[] password = env("SIFEN_TEST_P12_PASSWORD").toCharArray();
    String path = System.getenv("SIFEN_TEST_P12_PATH");
    return path != null && !path.isBlank()
        ? KeyMaterials.pkcs12(Path.of(path), password)
        : KeyMaterials.pkcs12(
            Base64.getMimeDecoder().decode(env("SIFEN_TEST_P12_BASE64")), password);
  }

  private static String env(String name) {
    String value = System.getenv(name);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException("Missing environment variable " + name);
    }
    return value;
  }

  /** Signs digests with RSASSA-PKCS1-v1_5, as a remote service would. */
  private static final class InProcessSigner implements RemoteSigner {

    private final KeyMaterial key;

    InProcessSigner(KeyMaterial key) {
      this.key = key;
    }

    @Override
    public List<X509Certificate> certificateChain() {
      return key.certificateChain();
    }

    @Override
    public byte[] signSha256Digest(byte[] sha256Digest) {
      if (sha256Digest.length != 32) {
        throw new IllegalArgumentException("Not a SHA-256 digest");
      }
      try {
        Signature raw = Signature.getInstance("NONEwithRSA");
        raw.initSign(key.privateKey());
        raw.update(SHA256_DIGEST_INFO);
        raw.update(sha256Digest);
        return raw.sign();
      } catch (GeneralSecurityException e) {
        throw new IllegalStateException("Could not sign", e);
      }
    }
  }
}
