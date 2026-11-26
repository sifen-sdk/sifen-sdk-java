/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.qr;

import io.github.sifensdk.core.Environment;
import io.github.sifensdk.core.signing.Csc;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * The QR of a document: the content of {@code dCarQR} (J002), built as in the Manual Técnico v150,
 * 13.8.
 *
 * <ol>
 *   <li>Data (13.8.4.1): {@code nVersion=…&Id=…&dFeEmiDE=…&dRucRec=…} (or {@code dNumIDRec=…})
 *       {@code &dTotGralOpe=…&dTotIVA=…&cItems=…&DigestValue=…&IdCSC=…}, in that order. {@code
 *       dFeEmiDE} and {@code DigestValue} are written as the lowercase hexadecimal of their
 *       characters (13.8.3). Missing values are written as {@code 0} (13.8.2, note (*)).
 *   <li>Hash (13.8.4.2 and 13.8.4.3): {@code cHashQR} = SHA-256, in lowercase hexadecimal, of the
 *       data followed by the CSC secret, without separator. The secret never goes in the URL.
 *   <li>URL (13.8.4.4): the consultation address of the environment ({@link
 *       Environment#qrBaseUri()}, NT 10, without {@code www}) + {@code ?} + data + {@code
 *       &cHashQR=} + hash.
 * </ol>
 *
 * <p>The values are not URL-encoded: the Manual does not do it. The {@code &} are escaped as {@code
 * &amp;amp;} only when written into the XML (13.8.4.5), which any XML serializer does.
 *
 * <p>Immutable and thread-safe. {@link #toString()} shows no content of the document.
 */
public final class QrCode {

  private static final char[] HEX = "0123456789abcdef".toCharArray();

  private final String data;
  private final String hash;
  private final String url;

  private QrCode(String data, String hash, String url) {
    this.data = data;
    this.hash = hash;
    this.url = url;
  }

  /**
   * Builds the QR of a document.
   *
   * @param parameters the values of the document
   * @param csc the taxpayer's security code
   * @param environment the environment, which gives the consultation address
   * @return the QR
   */
  public static QrCode generate(QrParameters parameters, Csc csc, Environment environment) {
    Objects.requireNonNull(parameters, "parameters");
    Objects.requireNonNull(csc, "csc");
    Objects.requireNonNull(environment, "environment");
    StringBuilder data = new StringBuilder(256);
    data.append("nVersion=").append(parameters.version());
    data.append("&Id=").append(parameters.cdc());
    data.append("&dFeEmiDE=").append(hex(parameters.issueDateTime()));
    if (parameters.receiverRuc() != null) {
      data.append("&dRucRec=").append(parameters.receiverRuc());
    } else {
      data.append("&dNumIDRec=").append(orZero(parameters.receiverIdNumber()));
    }
    data.append("&dTotGralOpe=").append(orZero(parameters.generalOperationTotal()));
    data.append("&dTotIVA=").append(orZero(parameters.vatTotal()));
    data.append("&cItems=").append(parameters.itemCount());
    data.append("&DigestValue=").append(hex(parameters.digestValue()));
    data.append("&IdCSC=").append(csc.id());
    String dataText = data.toString();
    String hash = hash(dataText, csc);
    String url = environment.qrBaseUri() + "?" + dataText + "&cHashQR=" + hash;
    return new QrCode(dataText, hash, url);
  }

  /**
   * Returns the data of step 1 (13.8.4.1), without the hash.
   *
   * @return {@code nVersion=…&…&IdCSC=…}
   */
  public String data() {
    return data;
  }

  /**
   * Returns the hash of step 3 ({@code cHashQR}).
   *
   * @return 64 lowercase hexadecimal characters
   */
  public String hash() {
    return hash;
  }

  /**
   * Returns the URL of step 4: the text of {@code dCarQR} and the content of the QR image.
   *
   * @return the URL, with plain {@code &} separators
   */
  public String url() {
    return url;
  }

  /** Returns a description without any content of the document. */
  @Override
  public String toString() {
    return "QrCode[...]";
  }

  private static String orZero(@Nullable String value) {
    return value == null || value.isEmpty() ? "0" : value;
  }

  private static String hex(String text) {
    byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
    StringBuilder out = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      out.append(HEX[(b >> 4) & 0xF]).append(HEX[b & 0xF]);
    }
    return out.toString();
  }

  private static String hash(String data, Csc csc) {
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is not available", e);
    }
    digest.update(data.getBytes(StandardCharsets.UTF_8));
    char[] secret = csc.secret();
    ByteBuffer encoded = StandardCharsets.UTF_8.encode(CharBuffer.wrap(secret));
    byte[] secretBytes = new byte[encoded.remaining()];
    encoded.get(secretBytes);
    try {
      digest.update(secretBytes);
    } finally {
      Arrays.fill(secret, '\0');
      Arrays.fill(secretBytes, (byte) 0);
      if (encoded.hasArray()) {
        Arrays.fill(encoded.array(), (byte) 0);
      }
    }
    byte[] result = digest.digest();
    StringBuilder out = new StringBuilder(result.length * 2);
    for (byte b : result) {
      out.append(HEX[(b >> 4) & 0xF]).append(HEX[b & 0xF]);
    }
    return out.toString();
  }
}
