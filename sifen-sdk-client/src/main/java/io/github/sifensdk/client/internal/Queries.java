/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.internal;

import io.github.sifensdk.client.DocumentQuery;
import io.github.sifensdk.client.Exchange;
import io.github.sifensdk.client.ProtocolException;
import io.github.sifensdk.client.ResultMessage;
import io.github.sifensdk.client.RucQuery;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Document query by CDC ({@code siConsDE}, MT150 9.4) and taxpayer query by RUC ({@code siConsRUC},
 * MT150 9.6).
 *
 * <p>Queries are not retried automatically in this version. A code that rejects the call itself
 * (size limit, generic codes of MT150 12.2) is a result with {@code found = false}; a code that the
 * specification does not define for the service is a protocol error.
 */
public final class Queries {

  /** {@code tCDC} (FE_Types_v141.xsd, DSch03): 44 characters. */
  private static final Pattern CDC = Pattern.compile("[0-9]{2}([0-9]{7}[0-9A-D])[0-9]{34}");

  /**
   * {@code tRuc} (FE_Types_v141.xsd, RSch03): 5 to 8 characters, without check digit, the last one
   * may be a letter A to D.
   */
  private static final Pattern RUC = Pattern.compile("[1-9][0-9]*[0-9A-D]?");

  /** Table G (MT150 9.4.2), C-718 and C-802: 0422 = CDC found. */
  private static final String CDC_FOUND = "0422";

  /** 0420 = CDC does not exist (or was rejected, C-804); 0421 = no permission (Table G). */
  private static final List<String> CDC_NOT_FOUND = List.of("0420", "0421");

  /** Table H (MT150 9.6.2), C-805: 0502 = RUC found. */
  private static final String RUC_FOUND = "0502";

  /** 0500 = RUC does not exist; 0501 = no permission to use the service (Table H). */
  private static final List<String> RUC_NOT_FOUND = List.of("0500", "0501");

  private static final Pattern CONTENT_START =
      Pattern.compile("<(?:([A-Za-z_][\\w.\\-]*):)?xContenDE(?:\\s[^>]*)?>");

  private Queries() {}

  /**
   * Queries a document by CDC.
   *
   * @param transport the issuer's transport
   * @param cdc the 44 characters of the CDC
   * @return the answer
   * @throws IllegalArgumentException if the CDC does not match {@code tCDC}
   * @throws io.github.sifensdk.client.TransportException if no response was received
   * @throws ProtocolException if the response is not a valid answer of the service
   */
  public static DocumentQuery document(Transport transport, String cdc) {
    Objects.requireNonNull(transport, "transport");
    requireCdc(cdc);
    String requestId = transport.newRequestId();
    return parseDocument(
        transport.post(Endpoints.DOCUMENT_QUERY, requestId, documentRequest(requestId, cdc)));
  }

  /**
   * Queries a taxpayer by RUC.
   *
   * @param transport the issuer's transport
   * @param ruc the RUC without check digit, 5 to 8 characters
   * @return the answer
   * @throws IllegalArgumentException if the RUC does not match {@code tRuc}
   * @throws io.github.sifensdk.client.TransportException if no response was received
   * @throws ProtocolException if the response is not a valid answer of the service
   */
  public static RucQuery ruc(Transport transport, String ruc) {
    Objects.requireNonNull(transport, "transport");
    requireRuc(ruc);
    String requestId = transport.newRequestId();
    return parseRuc(
        transport.post(Endpoints.RUC_QUERY, requestId, rucRequest(requestId, ruc)), ruc);
  }

  /**
   * The SOAP envelope of {@code rEnviConsDeRequest} (DSch01, name corrected by NT 10): {@code dId}
   * DSch02 and {@code dCDC} DSch03.
   *
   * @param requestId the request id, 1 to 15 digits
   * @param cdc the CDC
   * @return the envelope
   */
  static byte[] documentRequest(String requestId, String cdc) {
    String id = Reception.requireRequestId(requestId);
    requireCdc(cdc);
    return Soap.envelope(
        ("<rEnviConsDeRequest xmlns=\""
                + Soap.SIFEN_NS
                + "\"><dId>"
                + id
                + "</dId><dCDC>"
                + cdc
                + "</dCDC></rEnviConsDeRequest>")
            .getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Reads {@code rEnviConsDeResponse} (the XSD name, C-716).
   *
   * <p>{@code documentXml} is the content of {@code xContenDE} as received: its text when SIFEN
   * sends the container as text (the XSD type is {@code xs:string}), or the exact characters
   * between its tags when SIFEN sends it as XML elements (C-717, pending verification). In the
   * second case the content may use namespace prefixes declared outside it.
   *
   * @param exchange the exchange
   * @return the answer
   * @throws ProtocolException if the answer cannot be read or its code is unknown
   */
  static DocumentQuery parseDocument(Exchange exchange) {
    Element root = Soap.body(exchange, "rEnviConsDeResponse");
    ResultMessage message = message(root, exchange, "siConsDE");
    boolean found;
    if (CDC_FOUND.equals(message.code())) {
      found = true;
    } else if (CDC_NOT_FOUND.contains(message.code())
        || ReceptionCodes.isServiceRejection(
            message.code(), ReceptionCodes.DOCUMENT_QUERY_TOO_LARGE)) {
      found = false;
    } else {
      throw unknownCode(message, exchange, "siConsDE");
    }
    String documentXml = null;
    Element content = Soap.child(root, "xContenDE");
    if (found && content != null) {
      documentXml = content(content, exchange);
    }
    return new DocumentQuery(found, documentXml, List.of(message), exchange);
  }

  /**
   * The SOAP envelope of {@code rEnviConsRUC} (RSch01): {@code dId} RSch02 and {@code dRUCCons}
   * RSch03.
   *
   * @param requestId the request id, 1 to 15 digits
   * @param ruc the RUC without check digit
   * @return the envelope
   */
  static byte[] rucRequest(String requestId, String ruc) {
    String id = Reception.requireRequestId(requestId);
    requireRuc(ruc);
    return Soap.envelope(
        ("<rEnviConsRUC xmlns=\""
                + Soap.SIFEN_NS
                + "\"><dId>"
                + id
                + "</dId><dRUCCons>"
                + ruc
                + "</dRUCCons></rEnviConsRUC>")
            .getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Reads {@code rResEnviConsRUC}. The fields of {@code xContRUC} are read directly under it (XSD)
   * or under the Manual's {@code rContRUC} level (C-719).
   *
   * @param exchange the exchange
   * @param ruc the RUC queried
   * @return the answer
   * @throws ProtocolException if the answer cannot be read, its code is unknown or it refers to
   *     another RUC
   */
  static RucQuery parseRuc(Exchange exchange, String ruc) {
    Element root = Soap.body(exchange, "rResEnviConsRUC");
    ResultMessage message = message(root, exchange, "siConsRUC");
    if (RUC_NOT_FOUND.contains(message.code())
        || ReceptionCodes.isServiceRejection(message.code(), ReceptionCodes.RUC_QUERY_TOO_LARGE)) {
      return new RucQuery(false, ruc, null, null, null, null, List.of(message), exchange);
    }
    if (!RUC_FOUND.equals(message.code())) {
      throw unknownCode(message, exchange, "siConsRUC");
    }
    Element container = Soap.child(root, "xContRUC");
    if (container == null) {
      return new RucQuery(true, ruc, null, null, null, null, List.of(message), exchange);
    }
    String answered = Soap.text(container, "dRUCCons");
    if (answered != null && !answered.equals(ruc)) {
      throw new ProtocolException(
          "SIFEN's taxpayer answer (xContRUC/dRUCCons, ContRUC02) refers to another RUC", exchange);
    }
    return new RucQuery(
        true,
        ruc,
        Soap.text(container, "dRazCons"),
        Soap.text(container, "dCodEstCons"),
        Soap.text(container, "dDesEstCons"),
        electronicIssuer(Soap.text(container, "dRUCFactElec"), exchange),
        List.of(message),
        exchange);
  }

  /** ContRUC06: S = electronic issuer, N = not (catalog {@code facturador-electronico}). */
  private static @Nullable Boolean electronicIssuer(@Nullable String value, Exchange exchange) {
    if (value == null) {
      return null;
    }
    if ("S".equals(value)) {
      return Boolean.TRUE;
    }
    if ("N".equals(value)) {
      return Boolean.FALSE;
    }
    throw new ProtocolException(
        "SIFEN's taxpayer answer has an undocumented value in dRUCFactElec (ContRUC06)", exchange);
  }

  private static ResultMessage message(Element root, Exchange exchange, String service) {
    String code = Reception.childText(root, "dCodRes");
    if (code == null) {
      throw new ProtocolException(
          "SIFEN's " + service + " answer has no result code (dCodRes)", exchange);
    }
    String text = Reception.childText(root, "dMsgRes");
    return new ResultMessage(code, text == null ? "" : text);
  }

  private static ProtocolException unknownCode(
      ResultMessage message, Exchange exchange, String service) {
    return new ProtocolException(
        "SIFEN's "
            + service
            + " answer has result code "
            + message.code()
            + ", which the specification does not define for this service",
        exchange);
  }

  private static @Nullable String content(Element content, Exchange exchange) {
    boolean elements = false;
    for (Node n = content.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        elements = true;
        break;
      }
    }
    if (!elements) {
      String text = content.getTextContent();
      return text.trim().isEmpty() ? null : text;
    }
    String response = new String(exchange.response(), StandardCharsets.UTF_8);
    Matcher start = CONTENT_START.matcher(response);
    if (!start.find()) {
      throw new ProtocolException("SIFEN's siConsDE answer has an unreadable xContenDE", exchange);
    }
    String prefix = start.group(1);
    String end = "</" + (prefix == null ? "" : prefix + ":") + "xContenDE";
    int to = response.lastIndexOf(end);
    if (to < start.end()) {
      throw new ProtocolException("SIFEN's siConsDE answer has an unreadable xContenDE", exchange);
    }
    return response.substring(start.end(), to);
  }

  private static void requireCdc(String cdc) {
    Objects.requireNonNull(cdc, "cdc");
    if (cdc.length() != 44) {
      throw new IllegalArgumentException(
          "The CDC (dCDC, DSch03) must have 44 characters, it has " + cdc.length());
    }
    if (!CDC.matcher(cdc).matches()) {
      throw new IllegalArgumentException(
          "The CDC (dCDC, DSch03) does not match the XSD type tCDC: digits, with an optional letter"
              + " A to D as the last character of the RUC");
    }
  }

  private static void requireRuc(String ruc) {
    Objects.requireNonNull(ruc, "ruc");
    if (ruc.length() < 5 || ruc.length() > 8 || !RUC.matcher(ruc).matches()) {
      throw new IllegalArgumentException(
          "The RUC (dRUCCons, RSch03) must have 5 to 8 characters without the check digit: digits"
              + " not starting with 0, the last one may be a letter A to D (XSD type tRuc)");
    }
  }
}
