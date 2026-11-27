/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.internal;

import io.github.sifensdk.client.EventReceptionResult;
import io.github.sifensdk.client.EventResult;
import io.github.sifensdk.client.Exchange;
import io.github.sifensdk.client.ProtocolException;
import io.github.sifensdk.client.Status;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;

/**
 * Event reception ({@code siRecepEvento}, MT150 9.5): transport of events already signed.
 *
 * <p>The request is {@code rEnviEventoDe} with {@code dId} and {@code dEvReg}, which holds {@code
 * gGroupGesEve} with one {@code rGesEve} per event (GSch01-GSch03; {@code
 * WS_SiRecepEvento_v150.xsd} and {@code Evento_v150.xsd}, {@code tgGroupGesEve}; example of MT150
 * 7.2.2). Each {@code rGesEve} contains the event {@code rEve} and, as its sibling, the {@code
 * Signature} over it ({@code trGesEve}), so the signed unit given by the caller is the whole {@code
 * rGesEve}. It is copied byte for byte as signed (ADR-0005). Nothing is retried (ADR-0007).
 */
public final class Events {

  /**
   * Maximum number of events per request: {@code rGesEve} maxOccurs 15 ({@code Evento_v150.xsd},
   * {@code tgGroupGesEve}) and {@code gResProcEVe} 1-15 (GRSch03).
   */
  public static final int MAX_EVENTS = 15;

  /**
   * Start of {@code gGroupGesEve}. SIFEN's test environment answers 0160 ("XML mal formado") to an
   * event request whose {@code gGroupGesEve} has no {@code xsi:schemaLocation}, although the XSD
   * does not require it, and registers the same signed event when it has one (specification
   * evidence E-002, 2026-11-27).
   */
  static final String GROUP_START =
      "<gGroupGesEve xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
          + " xsi:schemaLocation=\"http://ekuatia.set.gov.py/sifen/xsd siRecepEvento_v150.xsd\">";

  /**
   * Maximum size of the request message, in bytes: 1000 KB (MT150 12.3.6, code 0560), with KB read
   * as 1000 bytes, the stricter reading.
   */
  public static final int MAX_MESSAGE_BYTES = 1000 * 1000;

  private Events() {}

  /**
   * Sends signed events ({@code siRecepEvento}).
   *
   * @param transport the transport
   * @param signedEvents 1 to 15 signed {@code rGesEve}, each exactly as signed
   * @return the outcome of each event
   * @throws IllegalArgumentException if the events break a documented limit
   */
  public static EventReceptionResult send(Transport transport, List<byte[]> signedEvents) {
    String requestId = transport.newRequestId();
    return parse(transport.post(Endpoints.EVENTS, requestId, request(requestId, signedEvents)));
  }

  /**
   * The SOAP envelope of an event reception.
   *
   * @param requestId the request id ({@code dId}, GSch02)
   * @param signedEvents 1 to 15 signed {@code rGesEve}
   * @return the complete envelope
   * @throws IllegalArgumentException if there are no events or more than 15, an event is not a
   *     {@code rGesEve}, or the message exceeds 1000 KB
   */
  public static byte[] request(String requestId, List<byte[]> signedEvents) {
    Objects.requireNonNull(signedEvents, "signedEvents");
    if (signedEvents.isEmpty() || signedEvents.size() > MAX_EVENTS) {
      throw new IllegalArgumentException(
          "An event request carries 1 to "
              + MAX_EVENTS
              + " events (Evento_v150.xsd tgGroupGesEve; MT150 GRSch03), got "
              + signedEvents.size());
    }
    ByteArrayOutputStream body = new ByteArrayOutputStream();
    body.writeBytes(
        ("<rEnviEventoDe xmlns=\""
                + Soap.SIFEN_NS
                + "\"><dId>"
                + Soap.escape(requestId)
                + "</dId><dEvReg>"
                + GROUP_START)
            .getBytes(StandardCharsets.UTF_8));
    for (int i = 0; i < signedEvents.size(); i++) {
      byte[] event =
          Soap.withoutDeclaration(
              Objects.requireNonNull(signedEvents.get(i), "signedEvents[" + i + "]"));
      if (!EventXml.startsWithElement(event, "rGesEve")) {
        throw new IllegalArgumentException(
            "Event "
                + (i + 1)
                + " is not a signed rGesEve (the element that holds rEve and its Signature,"
                + " Evento_v150.xsd trGesEve)");
      }
      body.writeBytes(event);
    }
    body.writeBytes("</gGroupGesEve></dEvReg></rEnviEventoDe>".getBytes(StandardCharsets.UTF_8));
    byte[] envelope = Soap.envelope(body.toByteArray());
    if (envelope.length > MAX_MESSAGE_BYTES) {
      throw new IllegalArgumentException(
          "The event message has "
              + envelope.length
              + " bytes; SIFEN admits up to 1000 KB (MT150 12.3.6, code 0560)");
    }
    return envelope;
  }

  /**
   * Reads the answer of the event reception ({@code rRetEnviEventoDe}, GRSch01-GRSch035).
   *
   * <p>Each {@code gResProcEVe} gives the event id ({@code id}, GRSch032), its state ({@code
   * dEstRes}, GRSch030), the protocol ({@code dProtAut}, GRSch031, empty if absent) and the {@code
   * gResProc} messages. The state is read from the documented texts ("Aprobado", "Aprobado con
   * observación", "Rechazado") and from the short forms A, AO and R, the vocabulary of the effect
   * column of the Manual's result tables (chapters 11 and 12), which SIFEN also uses for events.
   * Anything else is a {@link ProtocolException}.
   *
   * @param exchange the exchange
   * @return the result
   * @throws ProtocolException if the response is not {@code rRetEnviEventoDe}, has no event result,
   *     or an event result cannot be read
   */
  public static EventReceptionResult parse(Exchange exchange) {
    Element root = Soap.body(exchange, "rRetEnviEventoDe");
    List<Element> groups = Soap.children(root, "gResProcEVe");
    if (groups.isEmpty()) {
      throw new ProtocolException(
          "SIFEN's event answer has no event result (gResProcEVe, GRSch03 1-15)", exchange);
    }
    List<EventResult> events = new ArrayList<>();
    for (Element group : groups) {
      String id = Batches.childText(group, "id");
      String state = Batches.childText(group, "dEstRes");
      if (id == null || state == null) {
        throw new ProtocolException(
            "An event result (gResProcEVe) has no event id (id) or state (dEstRes)", exchange);
      }
      String protocol = Batches.childText(group, "dProtAut");
      events.add(
          EventResult.of(
              id,
              eventStatus(state, exchange),
              protocol,
              Soap.messages(Soap.children(group, "gResProc"))));
    }
    return EventReceptionResult.of(events, exchange);
  }

  /**
   * The state of an event from its text ({@code dEstRes}), or null when it is not one SIFEN
   * documents.
   *
   * @param value the text
   * @return the state, or null
   */
  static @Nullable Status statusOrNull(String value) {
    Status status = BatchStatus.ofText(value);
    if (status != null) {
      return status;
    }
    switch (BatchStatus.normalize(value)) {
      case "a":
        return Status.APPROVED;
      case "ao":
        return Status.APPROVED_WITH_OBSERVATION;
      case "r":
        return Status.REJECTED;
      default:
        return null;
    }
  }

  private static Status eventStatus(String value, Exchange exchange) {
    Status status = BatchStatus.ofText(value);
    if (status != null) {
      return status;
    }
    switch (BatchStatus.normalize(value)) {
      case "a":
        return Status.APPROVED;
      case "ao":
        return Status.APPROVED_WITH_OBSERVATION;
      case "r":
        return Status.REJECTED;
      default:
        throw BatchStatus.unknown(value, exchange);
    }
  }
}
