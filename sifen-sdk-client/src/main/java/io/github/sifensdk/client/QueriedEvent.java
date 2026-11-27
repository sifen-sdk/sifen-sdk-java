/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * An event registered on a document, as a document query returns it ({@code rContEv}): the event as
 * it was sent and SIFEN's answer to it. Read only: SIFEN also lists events registered by the SET or
 * by other parties, which an issuer never builds.
 *
 * <p>A class rather than a record so that fields can be added in a minor version. {@link
 * #toString()} shows only the type, the state and the result codes, never the event's contents.
 */
public final class QueriedEvent {

  private final String id;
  private final EventType type;
  private final String detailElement;
  private final @Nullable LocalDateTime signatureDateTime;
  private final Map<String, String> values;
  private final String xml;
  private final @Nullable Status status;
  private final @Nullable String authorizationProtocol;
  private final List<ResultMessage> messages;

  private QueriedEvent(
      String id,
      EventType type,
      String detailElement,
      @Nullable LocalDateTime signatureDateTime,
      Map<String, String> values,
      String xml,
      @Nullable Status status,
      @Nullable String authorizationProtocol,
      List<ResultMessage> messages) {
    this.id = Objects.requireNonNull(id, "id");
    this.type = Objects.requireNonNull(type, "type");
    this.detailElement = Objects.requireNonNull(detailElement, "detailElement");
    this.signatureDateTime = signatureDateTime;
    this.values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
    this.xml = Objects.requireNonNull(xml, "xml");
    this.status = status;
    this.authorizationProtocol = authorizationProtocol;
    this.messages = List.copyOf(messages);
  }

  /**
   * Creates the event. For the SDK's internal use and tests; not part of the API: it may change in
   * any version.
   *
   * @param id the id
   * @param type the type
   * @param detailElement the detail element
   * @param signatureDateTime the signature date and time, or null
   * @param values the leaf values of the detail
   * @param xml the event as received
   * @param status the state, or null
   * @param authorizationProtocol the transaction number, or null if none
   * @param messages codes and messages
   * @return the event
   */
  public static QueriedEvent of(
      String id,
      EventType type,
      String detailElement,
      @Nullable LocalDateTime signatureDateTime,
      Map<String, String> values,
      String xml,
      @Nullable Status status,
      @Nullable String authorizationProtocol,
      List<ResultMessage> messages) {
    return new QueriedEvent(
        id,
        type,
        detailElement,
        signatureDateTime,
        values,
        xml,
        status,
        authorizationProtocol,
        messages);
  }

  /**
   * Returns the event identifier ({@code Id} of {@code rEve}, GDE003).
   *
   * @return the id
   */
  public String id() {
    return id;
  }

  /**
   * Returns the type, by the element of its detail.
   *
   * @return the type
   */
  public EventType type() {
    return type;
  }

  /**
   * Returns the local name of the detail element (for example {@code rGeVeCan}), also for types
   * this version does not know.
   *
   * @return the local name
   */
  public String detailElement() {
    return detailElement;
  }

  /**
   * Returns the signature date and time ({@code dFecFirma}, GDE004).
   *
   * @return the date and time, or null if absent or not readable
   */
  public @Nullable LocalDateTime signatureDateTime() {
    return signatureDateTime;
  }

  /**
   * Returns the leaf elements of the detail, by local name, in document order (for example {@code
   * mOtEve} of a cancellation); repeated or nested elements are not included.
   *
   * @return the values, unmodifiable
   */
  public Map<String, String> values() {
    return values;
  }

  /**
   * Returns the event ({@code rGesEve}) as received.
   *
   * @return the XML text
   */
  public String xml() {
    return xml;
  }

  /**
   * Returns SIFEN's state for the event ({@code dEstRes}).
   *
   * @return the state, or null if absent or not known
   */
  public @Nullable Status status() {
    return status;
  }

  /**
   * Returns the event's transaction number ({@code dProtAut}).
   *
   * @return the transaction number, or null if none
   */
  public @Nullable String authorizationProtocol() {
    return authorizationProtocol;
  }

  /**
   * Returns SIFEN's codes and messages for the event.
   *
   * @return the codes and messages, unmodifiable
   */
  public List<ResultMessage> messages() {
    return messages;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof QueriedEvent)) {
      return false;
    }
    QueriedEvent that = (QueriedEvent) o;
    return id.equals(that.id)
        && type == that.type
        && detailElement.equals(that.detailElement)
        && Objects.equals(signatureDateTime, that.signatureDateTime)
        && values.equals(that.values)
        && xml.equals(that.xml)
        && status == that.status
        && Objects.equals(authorizationProtocol, that.authorizationProtocol)
        && messages.equals(that.messages);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id,
        type,
        detailElement,
        signatureDateTime,
        values,
        xml,
        status,
        authorizationProtocol,
        messages);
  }

  /** Returns the type, the state and the result codes, without the event's contents. */
  @Override
  public String toString() {
    return "QueriedEvent[type="
        + type
        + ", status="
        + status
        + ", codes="
        + ResultMessage.codes(messages)
        + "]";
  }
}
