/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.internal;

import io.github.sifensdk.client.DocumentResult;
import io.github.sifensdk.client.Exchange;
import io.github.sifensdk.client.ProtocolException;
import io.github.sifensdk.client.ReceptionResult;
import io.github.sifensdk.client.ResultMessage;
import io.github.sifensdk.client.Status;
import io.github.sifensdk.core.SifenDocuments;
import io.github.sifensdk.core.signing.SignedDocument;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;

/**
 * Synchronous reception of one electronic document ({@code siRecepDE}, MT150 9.1).
 *
 * <p>The request is {@code rEnviDe} with {@code dId} and {@code xDE}, which carries the signed
 * {@code rDE} byte for byte (ADR-0005). The answer is {@code rRetEnviDe} with the processing
 * protocol {@code rProtDe}. The reading tolerates the known differences between the Manual and the
 * XSD (C-701 to C-705). Nothing is ever resent (ADR-0007).
 */
public final class Reception {

  private static final String REQUEST_ROOT = "rEnviDe";
  private static final String RESPONSE_ROOT = "rRetEnviDe";

  /**
   * {@code dFecProc}: the XSD ({@code fecUTC}) requires an offset; the Manual writes it without one
   * and with a dash or nothing between date and time (C-703).
   */
  private static final Pattern DATE_TIME =
      Pattern.compile(
          "(\\d{4}-\\d{2}-\\d{2})[T\\- ]?(\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{1,9})?)(Z|[+-]\\d{2}:\\d{2})?");

  private static final Pattern REQUEST_ID = Pattern.compile("[0-9]{1,15}");

  private Reception() {}

  /**
   * Sends a signed document and returns SIFEN's verdict. Never retries.
   *
   * @param transport the issuer's transport
   * @param document the signed document, sent exactly as signed
   * @return the result; a rejection is a result, not an exception
   * @throws io.github.sifensdk.client.TransportException if no response was received
   * @throws ProtocolException if the response is not a valid answer of the service
   */
  public static ReceptionResult send(Transport transport, SignedDocument document) {
    Objects.requireNonNull(transport, "transport");
    Objects.requireNonNull(document, "document");
    String requestId = transport.newRequestId();
    return parse(
        transport.post(Endpoints.RECEPTION, requestId, request(requestId, document)),
        document.cdc());
  }

  /**
   * The complete SOAP envelope of {@code rEnviDe} (Schema XML 2: {@code dId} ASch02, {@code xDE}
   * ASch03, named {@code xDE} as in the XSD and NT 10).
   *
   * @param requestId the request id, 1 to 15 digits
   * @param document the signed document; its bytes are embedded unchanged after the XML declaration
   * @return the envelope
   */
  static byte[] request(String requestId, SignedDocument document) {
    String id = requireRequestId(requestId);
    byte[] rde = Soap.withoutDeclaration(document.xml());
    byte[] before =
        ("<" + REQUEST_ROOT + " xmlns=\"" + Soap.SIFEN_NS + "\"><dId>" + id + "</dId><xDE>")
            .getBytes(StandardCharsets.UTF_8);
    byte[] after = ("</xDE></" + REQUEST_ROOT + ">").getBytes(StandardCharsets.UTF_8);
    byte[] body = new byte[before.length + rde.length + after.length];
    System.arraycopy(before, 0, body, 0, before.length);
    System.arraycopy(rde, 0, body, before.length, rde.length);
    System.arraycopy(after, 0, body, before.length + rde.length, after.length);
    return Soap.envelope(body);
  }

  /**
   * Reads the answer of {@code siRecepDE}.
   *
   * <ul>
   *   <li>{@code rRetEnviDe/rProtDe} as in the XSD; also inside the Manual's {@code xProtDe}
   *       (C-701).
   *   <li>The CDC from {@code Id} (XSD), {@code id} (Manual PP02) or {@code dId} (Manual example
   *       7.4), C-702. If absent, blank or all zeros, it is the CDC sent; if it is another CDC, the
   *       answer does not belong to this document.
   *   <li>{@code dEstRes} and {@code dProtAut} directly under {@code rProtDe} (XSD) or inside
   *       {@code gResProc} (Manual), C-704.
   *   <li>{@code dFecProc} with offset (XSD) or without it, read as Paraguay's time (Manual),
   *       C-703.
   *   <li>Without {@code rProtDe}: a result {@link Status#REJECTED} if it carries a rejection code
   *       of the service (0200 or the generic codes of MT150 12.2), otherwise a protocol error.
   * </ul>
   *
   * @param exchange the exchange
   * @param cdc the CDC of the document sent
   * @return the result
   * @throws ProtocolException if the answer cannot be read or classified
   */
  static ReceptionResult parse(Exchange exchange, String cdc) {
    Element root = Soap.body(exchange, RESPONSE_ROOT);
    Element protocol = Soap.child(root, "rProtDe");
    if (protocol == null) {
      Element wrapper = Soap.child(root, "xProtDe");
      if (wrapper != null) {
        protocol = Soap.child(wrapper, "rProtDe");
      }
    }
    DocumentResult result =
        protocol != null ? protocol(protocol, cdc, exchange) : withoutProtocol(root, cdc, exchange);
    return new ReceptionResult(result, exchange);
  }

  private static DocumentResult protocol(Element protocol, String cdc, Exchange exchange) {
    String answered = firstChildText(protocol, "Id", "id", "dId");
    if (answered != null && !isZeros(answered) && !answered.equals(cdc)) {
      throw new ProtocolException(
          "SIFEN's reception answer (rProtDe/Id, PP02) refers to another document", exchange);
    }
    List<Element> groups = Soap.children(protocol, "gResProc");
    List<ResultMessage> messages = Soap.messages(groups);
    String state = childOrGroupText(protocol, groups, "dEstRes");
    Status status =
        state != null
            ? status(state, exchange)
            : statusFromCodes(messages, exchange, "rProtDe has no dEstRes (PP050)");
    return new DocumentResult(
        cdc,
        status,
        childOrGroupText(protocol, groups, "dProtAut"),
        processedAt(childText(protocol, "dFecProc"), exchange),
        messages);
  }

  private static DocumentResult withoutProtocol(Element root, String cdc, Exchange exchange) {
    List<Element> groups = new ArrayList<>();
    if (Soap.child(root, "dCodRes") != null) {
      groups.add(root);
    }
    groups.addAll(Soap.children(root, "gResProc"));
    List<ResultMessage> messages = new ArrayList<>();
    for (Element g : groups) {
      String code = childText(g, "dCodRes");
      if (code != null) {
        String message = childText(g, "dMsgRes");
        messages.add(new ResultMessage(code, message == null ? "" : message));
      }
    }
    if (!ReceptionCodes.anyServiceRejection(messages, ReceptionCodes.RECEPTION_TOO_LARGE)) {
      throw new ProtocolException(
          messages.isEmpty()
              ? "SIFEN's reception answer has no rProtDe (PP01) and no result code"
              : "SIFEN's reception answer has no rProtDe (PP01) and its result code "
                  + messages.get(0).code()
                  + " is not a known rejection of the service",
          exchange);
    }
    return new DocumentResult(
        cdc, Status.REJECTED, null, processedAt(childText(root, "dFecProc"), exchange), messages);
  }

  /**
   * Maps {@code dEstRes} (PP050: "Aprobado", "Aprobado con observación", "Rechazado"), ignoring
   * case and accents.
   */
  private static Status status(String state, Exchange exchange) {
    String normalized =
        Normalizer.normalize(state, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .replaceAll("\\s+", " ")
            .trim()
            .toLowerCase(Locale.ROOT);
    switch (normalized) {
      case "aprobado":
        return Status.APPROVED;
      case "aprobado con observacion":
        return Status.APPROVED_WITH_OBSERVATION;
      case "rechazado":
        return Status.REJECTED;
      default:
        throw new ProtocolException(
            "SIFEN's reception answer has a state (dEstRes, PP050) that is not documented",
            exchange);
    }
  }

  /** Without {@code dEstRes}: only the codes the specification classifies. */
  private static Status statusFromCodes(
      List<ResultMessage> messages, Exchange exchange, String problem) {
    if (ReceptionCodes.anyServiceRejection(messages, ReceptionCodes.RECEPTION_TOO_LARGE)) {
      return Status.REJECTED;
    }
    boolean allApproved = !messages.isEmpty();
    for (ResultMessage m : messages) {
      allApproved &= ReceptionCodes.RECEPTION_APPROVED.equals(m.code());
    }
    if (allApproved) {
      return Status.APPROVED;
    }
    throw new ProtocolException(
        "SIFEN's reception answer cannot be classified: "
            + problem
            + (messages.isEmpty() ? " and no result code" : " and its codes are not conclusive"),
        exchange);
  }

  /**
   * Reads {@code dFecProc}. With an offset it is kept; without one it is Paraguay's local time.
   *
   * @param text the text, or null
   * @param exchange the exchange, for errors
   * @return the instant with its offset, or null if absent
   * @throws ProtocolException if present but not a date and time
   */
  static @Nullable OffsetDateTime processedAt(@Nullable String text, Exchange exchange) {
    if (text == null) {
      return null;
    }
    Matcher m = DATE_TIME.matcher(text);
    if (!m.matches()) {
      throw new ProtocolException(
          "SIFEN's processing date and time (dFecProc) has an unknown format", exchange);
    }
    try {
      LocalDateTime local = LocalDateTime.parse(m.group(1) + "T" + m.group(2));
      String offset = m.group(3);
      return offset != null
          ? local.atOffset(ZoneOffset.of(offset))
          : local.atZone(SifenDocuments.PARAGUAY).toOffsetDateTime();
    } catch (DateTimeException e) {
      throw new ProtocolException(
          "SIFEN's processing date and time (dFecProc) is not a valid date", exchange);
    }
  }

  /**
   * Checks a request id ({@code dId}: {@code dIdType}, an integer of up to 15 digits).
   *
   * @param requestId the request id
   * @return the same value
   * @throws IllegalArgumentException if it is not 1 to 15 digits
   */
  static String requireRequestId(String requestId) {
    Objects.requireNonNull(requestId, "requestId");
    if (!REQUEST_ID.matcher(requestId).matches()) {
      throw new IllegalArgumentException("The request id (dId) must have 1 to 15 digits");
    }
    return requestId;
  }

  /**
   * The trimmed text of a direct child.
   *
   * @param parent the parent
   * @param localName the child's local name
   * @return the text, or null if absent or blank
   */
  static @Nullable String childText(Element parent, String localName) {
    Element e = Soap.child(parent, localName);
    if (e == null) {
      return null;
    }
    String t = e.getTextContent().trim();
    return t.isEmpty() ? null : t;
  }

  private static @Nullable String firstChildText(Element parent, String... localNames) {
    for (String name : localNames) {
      String t = childText(parent, name);
      if (t != null) {
        return t;
      }
    }
    return null;
  }

  private static @Nullable String childOrGroupText(
      Element protocol, List<Element> groups, String localName) {
    String direct = childText(protocol, localName);
    if (direct != null) {
      return direct;
    }
    for (Element g : groups) {
      String t = childText(g, localName);
      if (t != null) {
        return t;
      }
    }
    return null;
  }

  private static boolean isZeros(String value) {
    for (int i = 0; i < value.length(); i++) {
      if (value.charAt(i) != '0') {
        return false;
      }
    }
    return true;
  }
}
