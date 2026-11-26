/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.testing;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;

/**
 * A response the {@link SimulatedSifen} sends: HTTP status, content type and body, optionally after
 * a delay.
 *
 * <p>The ready-made responses of every SIFEN service, valid according to the official XSDs, are in
 * {@link SifenResponses}; they are rendered when the request arrives, so they can take the CDC, the
 * batch number or the event ids from it. The factories here build arbitrary responses, for the
 * cases {@link SifenResponses} does not cover.
 *
 * <p>Immutable; the same instance can be enqueued several times.
 */
public final class SimulatedResponse {

  /** Content type of SOAP 1.2, as SIFEN sends it. */
  static final String SOAP_CONTENT_TYPE = "application/soap+xml;charset=utf-8";

  private static final String ENVELOPE_START =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?><env:Envelope xmlns:env=\""
          + Xml.ENVELOPE_NS
          + "\"><env:Header/><env:Body>";
  private static final String ENVELOPE_END = "</env:Body></env:Envelope>";

  private final Function<Interaction, Rendered> renderer;
  private final Duration delay;
  private final String description;

  private SimulatedResponse(
      Function<Interaction, Rendered> renderer, Duration delay, String description) {
    this.renderer = renderer;
    this.delay = delay;
    this.description = description;
  }

  /**
   * An arbitrary response.
   *
   * @param httpStatus the HTTP status, for example 200
   * @param contentType the {@code Content-Type} header
   * @param body the body; copied
   * @return the response
   * @throws IllegalArgumentException if the status is not between 100 and 599
   */
  public static SimulatedResponse of(int httpStatus, String contentType, byte[] body) {
    checkStatus(httpStatus);
    Objects.requireNonNull(contentType, "contentType");
    byte[] copy = body.clone();
    return new SimulatedResponse(
        i -> new Rendered(httpStatus, contentType, copy.clone()),
        Duration.ZERO,
        "HTTP " + httpStatus + " " + contentType);
  }

  /**
   * A SOAP 1.2 response with HTTP 200 whose body contains the given XML, for example a response
   * element written by hand.
   *
   * @param bodyContentXml the content of {@code env:Body}, without XML declaration, for example
   *     {@code <ns2:rRetEnviDe
   *     xmlns:ns2="http://ekuatia.set.gov.py/sifen/xsd">...</ns2:rRetEnviDe>}
   * @return the response
   */
  public static SimulatedResponse soap(String bodyContentXml) {
    Objects.requireNonNull(bodyContentXml, "bodyContentXml");
    return soap(200, "custom SOAP", i -> bodyContentXml);
  }

  /** A SOAP envelope rendered when the request arrives. */
  static SimulatedResponse soap(
      int httpStatus, String description, Function<Interaction, String> bodyContent) {
    checkStatus(httpStatus);
    return new SimulatedResponse(
        i ->
            new Rendered(
                httpStatus,
                SOAP_CONTENT_TYPE,
                envelope(bodyContent.apply(i)).getBytes(StandardCharsets.UTF_8)),
        Duration.ZERO,
        description);
  }

  /** A response chosen when the request arrives. */
  static SimulatedResponse dynamic(
      String description, Function<Interaction, SimulatedResponse> choice) {
    return new SimulatedResponse(i -> choice.apply(i).render(i), Duration.ZERO, description);
  }

  /**
   * The same response, sent only after waiting the given time; use it to test the client's
   * timeouts. The connection stays open while waiting. Closing the simulated SIFEN interrupts the
   * wait.
   *
   * @param delay how long to wait before answering
   * @return a new response
   * @throws IllegalArgumentException if the delay is negative
   */
  public SimulatedResponse delayed(Duration delay) {
    if (Objects.requireNonNull(delay, "delay").isNegative()) {
      throw new IllegalArgumentException("Negative delay: " + delay);
    }
    return new SimulatedResponse(renderer, delay, description);
  }

  /**
   * The delay before answering.
   *
   * @return the delay, zero by default
   */
  public Duration delay() {
    return delay;
  }

  static String envelope(String bodyContent) {
    return ENVELOPE_START + bodyContent + ENVELOPE_END;
  }

  /** The response actually sent for a request. */
  Rendered render(Interaction interaction) {
    return renderer.apply(interaction);
  }

  private static void checkStatus(int httpStatus) {
    if (httpStatus < 100 || httpStatus > 599) {
      throw new IllegalArgumentException("Not an HTTP status: " + httpStatus);
    }
  }

  @Override
  public String toString() {
    return "SimulatedResponse{"
        + description
        + (delay.isZero() ? "" : ", delay=" + delay.toMillis() + "ms")
        + "}";
  }

  /** A response ready to be written. */
  static final class Rendered {
    final int httpStatus;
    final String contentType;
    final byte[] body;

    Rendered(int httpStatus, String contentType, byte[] body) {
      this.httpStatus = httpStatus;
      this.contentType = contentType;
      this.body = body;
    }

    String bodyUtf8() {
      return new String(body, StandardCharsets.UTF_8);
    }
  }
}
