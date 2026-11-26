/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.testing;

import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * A request received by a {@link SimulatedSifen}, exactly as it arrived.
 *
 * <p>Use it to assert what your integration sent: the service path, the exact body (for example,
 * that the signed document travels byte for byte as it was signed), the request id ({@code dId})
 * and the client certificate presented in the TLS handshake.
 *
 * <p>Immutable. {@link #toString()} never includes the body, which may contain document data.
 */
public final class RecordedRequest {

  private final String method;
  private final String path;
  private final @Nullable String contentType;
  private final byte[] body;
  private final List<X509Certificate> clientCertificateChain;
  private final @Nullable String requestId;
  private final Instant receivedAt;

  RecordedRequest(
      String method,
      String path,
      @Nullable String contentType,
      byte[] body,
      List<X509Certificate> clientCertificateChain,
      @Nullable String requestId,
      Instant receivedAt) {
    this.method = Objects.requireNonNull(method, "method");
    this.path = Objects.requireNonNull(path, "path");
    this.contentType = contentType;
    this.body = body.clone();
    this.clientCertificateChain = List.copyOf(clientCertificateChain);
    this.requestId = requestId;
    this.receivedAt = Objects.requireNonNull(receivedAt, "receivedAt");
  }

  /**
   * The HTTP method, normally {@code POST}.
   *
   * @return the method
   */
  public String method() {
    return method;
  }

  /**
   * The path of the request, without query string, for example {@link SimulatedSifen#RECEPTION}.
   *
   * @return the path
   */
  public String path() {
    return path;
  }

  /**
   * The {@code Content-Type} header sent by the client.
   *
   * @return the header value, or null if absent
   */
  public @Nullable String contentType() {
    return contentType;
  }

  /**
   * The exact bytes of the request body.
   *
   * @return a new array on every call
   */
  public byte[] body() {
    return body.clone();
  }

  /**
   * The request body decoded as UTF-8, the encoding SIFEN requires.
   *
   * @return the body as text
   */
  public String bodyUtf8() {
    return new String(body, StandardCharsets.UTF_8);
  }

  /**
   * The certificate the client presented in the TLS handshake (the first of its chain). The
   * simulated SIFEN, like SIFEN, refuses connections without one, so it is always present.
   *
   * @return the client certificate
   */
  public X509Certificate clientCertificate() {
    return clientCertificateChain.get(0);
  }

  /**
   * The whole certificate chain the client presented, leaf first.
   *
   * @return the chain, never empty
   */
  public List<X509Certificate> clientCertificateChain() {
    return clientCertificateChain;
  }

  /**
   * The request id ({@code dId}) found in the SOAP body. SIFEN's responses do not echo it (the XSDs
   * have no {@code dId} in any response), so this is the place to correlate it.
   *
   * @return the request id, or null if the body has none or is not a SOAP envelope
   */
  public @Nullable String requestId() {
    return requestId;
  }

  /**
   * When the simulated SIFEN received the request, according to its clock.
   *
   * @return the instant
   */
  public Instant receivedAt() {
    return receivedAt;
  }

  @Override
  public String toString() {
    return "RecordedRequest{"
        + method
        + " "
        + path
        + ", "
        + body.length
        + " bytes, requestId="
        + requestId
        + "}";
  }
}
