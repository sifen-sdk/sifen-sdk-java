/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * The raw exchange with SIFEN, kept for audit and diagnosis (ADR-0005, ADR-0006).
 *
 * <p>The request contains the document as sent; store it with the same care as the document. {@link
 * #toString()} never shows contents.
 */
public final class Exchange {

  private final String requestId;
  private final URI endpoint;
  private final byte[] request;
  private final int httpStatus;
  private final byte[] response;
  private final Instant sentAt;
  private final Duration duration;

  /**
   * Creates an exchange.
   *
   * @param requestId the request id sent to SIFEN ({@code dId})
   * @param endpoint the address called
   * @param request the exact bytes sent
   * @param httpStatus the HTTP status received
   * @param response the exact bytes received
   * @param sentAt when the request was sent
   * @param duration how long the call took
   */
  public Exchange(
      String requestId,
      URI endpoint,
      byte[] request,
      int httpStatus,
      byte[] response,
      Instant sentAt,
      Duration duration) {
    this.requestId = Objects.requireNonNull(requestId, "requestId");
    this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
    this.request = request.clone();
    this.httpStatus = httpStatus;
    this.response = response.clone();
    this.sentAt = Objects.requireNonNull(sentAt, "sentAt");
    this.duration = Objects.requireNonNull(duration, "duration");
  }

  /**
   * The request id sent to SIFEN ({@code dId}).
   *
   * @return the request id
   */
  public String requestId() {
    return requestId;
  }

  /**
   * The address called.
   *
   * @return the endpoint
   */
  public URI endpoint() {
    return endpoint;
  }

  /**
   * The exact bytes sent (a copy).
   *
   * @return the request
   */
  public byte[] request() {
    return request.clone();
  }

  /**
   * The HTTP status received.
   *
   * @return the status
   */
  public int httpStatus() {
    return httpStatus;
  }

  /**
   * The exact bytes received (a copy).
   *
   * @return the response
   */
  public byte[] response() {
    return response.clone();
  }

  /**
   * When the request was sent.
   *
   * @return the instant
   */
  public Instant sentAt() {
    return sentAt;
  }

  /**
   * How long the call took.
   *
   * @return the duration
   */
  public Duration duration() {
    return duration;
  }

  @Override
  public String toString() {
    return "Exchange{requestId="
        + requestId
        + ", endpoint="
        + endpoint
        + ", httpStatus="
        + httpStatus
        + ", duration="
        + duration.toMillis()
        + "ms}";
  }
}
