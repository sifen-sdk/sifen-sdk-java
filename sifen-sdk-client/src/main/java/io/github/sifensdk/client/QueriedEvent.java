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
import org.jspecify.annotations.Nullable;

/**
 * An event registered on a document, as a document query returns it ({@code rContEv}): the event as
 * it was sent and SIFEN's answer to it. Read only: SIFEN also lists events registered by the SET or
 * by other parties, which an issuer never builds.
 *
 * @param id the event identifier ({@code Id} of {@code rEve}, GDE003)
 * @param type the type, by the element of its detail
 * @param detailElement the local name of the detail element (for example {@code rGeVeCan}), also
 *     for types this version does not know
 * @param signatureDateTime the signature date and time ({@code dFecFirma}, GDE004), or null if
 *     absent or not readable
 * @param values the leaf elements of the detail, by local name, in document order (for example
 *     {@code mOtEve} of a cancellation); repeated or nested elements are not included
 * @param xml the event ({@code rGesEve}) as received
 * @param status SIFEN's state for the event ({@code dEstRes}), or null if absent or not known
 * @param authorizationProtocol the event's transaction number ({@code dProtAut}), empty if none
 * @param messages SIFEN's codes and messages for the event
 */
public record QueriedEvent(
    String id,
    EventType type,
    String detailElement,
    @Nullable LocalDateTime signatureDateTime,
    Map<String, String> values,
    String xml,
    @Nullable Status status,
    String authorizationProtocol,
    List<ResultMessage> messages) {

  /**
   * Creates the event.
   *
   * @param id the id
   * @param type the type
   * @param detailElement the detail element
   * @param signatureDateTime the signature date and time, or null
   * @param values the leaf values of the detail
   * @param xml the event as received
   * @param status the state, or null
   * @param authorizationProtocol the transaction number, empty if none
   * @param messages codes and messages
   */
  public QueriedEvent {
    values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
    messages = List.copyOf(messages);
  }

  /** Returns a description without content of the event. */
  @Override
  public String toString() {
    return "QueriedEvent[" + type + ", " + status + "]";
  }
}
