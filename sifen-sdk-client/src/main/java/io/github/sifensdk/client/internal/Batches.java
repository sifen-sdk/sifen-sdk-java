/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.internal;

import io.github.sifensdk.client.BatchResult;
import io.github.sifensdk.client.BatchSubmission;
import io.github.sifensdk.client.DocumentResult;
import io.github.sifensdk.client.Exchange;
import io.github.sifensdk.client.ProtocolException;
import io.github.sifensdk.client.ResultMessage;
import io.github.sifensdk.core.signing.SignedDocument;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;

/**
 * Batch reception ({@code siRecepLoteDE}, MT150 9.2) and batch result query ({@code
 * siResultLoteDE}, MT150 9.3).
 *
 * <p>Each operation is a pure request builder, a pure response parser and the call that joins them
 * through the {@link Transport}. Nothing is retried (ADR-0007): a batch sent twice can block the
 * issuer's RUC for 10 to 60 minutes (Guía de mejores prácticas, "Motivos de bloqueo", i).
 */
public final class Batches {

  /**
   * Protocol number as the XSD defines it: {@code xs:decimal}, {@code totalDigits} 28, {@code
   * fractionDigits} 0, not negative ({@code WS_SiRecepLoteDE_v141.xsd} and {@code
   * WS_SiConsLote_v141.xsd}, {@code dProtConsLote}; C-708). Kept as text: real numbers already have
   * 17 digits and must never pass through a 64-bit integer or a floating point number.
   */
  private static final Pattern PROTOCOL_NUMBER = Pattern.compile("[0-9]{1,28}");

  private static final Pattern NON_NEGATIVE_INTEGER = Pattern.compile("[0-9]{1,18}");

  private Batches() {}

  /**
   * Sends a batch of signed documents ({@code siRecepLoteDE}).
   *
   * @param transport the transport
   * @param documents 1 to 50 signed documents of the same type and issuer
   * @return whether SIFEN accepted the batch, and its protocol number
   * @throws IllegalArgumentException if the batch breaks a documented limit (see {@link
   *     BatchArchive#validate(List)})
   */
  public static BatchSubmission send(Transport transport, List<SignedDocument> documents) {
    String requestId = transport.newRequestId();
    return parseSubmission(
        transport.post(Endpoints.BATCH, requestId, submissionRequest(requestId, documents)));
  }

  /**
   * Queries the result of a batch ({@code siResultLoteDE}).
   *
   * @param transport the transport
   * @param protocolNumber the protocol number received when the batch was accepted
   * @return the state of the batch and of each document
   * @throws IllegalArgumentException if the protocol number is not 1 to 28 digits
   */
  public static BatchResult result(Transport transport, String protocolNumber) {
    String requestId = transport.newRequestId();
    return parseResult(
        transport.post(
            Endpoints.BATCH_RESULT, requestId, resultRequest(requestId, protocolNumber)));
  }

  /**
   * The SOAP envelope of a batch reception: {@code rEnvioLote} with {@code dId} and {@code xDE}
   * (BSch01-BSch03), where {@code xDE} is the Base64 of the ZIP built by {@link
   * BatchArchive#zip(List)}.
   *
   * @param requestId the request id ({@code dId}, BSch02)
   * @param documents the signed documents
   * @return the complete envelope
   * @throws IllegalArgumentException if the batch breaks a documented limit
   */
  public static byte[] submissionRequest(String requestId, List<SignedDocument> documents) {
    BatchArchive.validate(documents);
    String xde = Base64.getEncoder().encodeToString(BatchArchive.zip(documents));
    String body =
        "<rEnvioLote xmlns=\""
            + Soap.SIFEN_NS
            + "\"><dId>"
            + Soap.escape(requestId)
            + "</dId><xDE>"
            + xde
            + "</xDE></rEnvioLote>";
    byte[] envelope = Soap.envelope(body.getBytes(StandardCharsets.UTF_8));
    BatchArchive.checkMessageSize(envelope.length);
    return envelope;
  }

  /**
   * Reads the answer of the batch reception ({@code rResEnviLoteDe}, BRSch01-BRSch06).
   *
   * <p>The batch is accepted only with code 0300 ("Lote recibido con éxito"); any other code (0301
   * "Lote no encolado para procesamiento", 0270, or the generic codes of MT150 12.2) means it will
   * not be processed, and is a result, not an exception (ADR-0006). The protocol number is only
   * read when accepted (BRSch05: "Generado solamente si dCodRes=0300"); a rejected batch may carry
   * {@code 0}, which is not a protocol number.
   *
   * <p>{@code dTpoProces} (BRSch06) is read as seconds, as BRSch06 and MT150 8.2.1 say (8.2.2 says
   * milliseconds; contradiction C-710). The Manual never informs less than one second, so {@code 0}
   * (seen in the Guía's example) carries no information and is returned as {@code null}, as is a
   * missing or non-numeric value. It is informative only and must not be used as a waiting time.
   *
   * @param exchange the exchange
   * @return the submission
   * @throws ProtocolException if the response is not {@code rResEnviLoteDe}, has no {@code
   *     dCodRes}, or is accepted without a valid protocol number
   */
  public static BatchSubmission parseSubmission(Exchange exchange) {
    Element root = Soap.body(exchange, "rResEnviLoteDe");
    String code = childText(root, "dCodRes");
    if (code == null) {
      throw new ProtocolException(
          "SIFEN's batch reception answer has no result code (dCodRes, BRSch03)", exchange);
    }
    String message = childText(root, "dMsgRes");
    List<ResultMessage> messages = List.of(new ResultMessage(code, message == null ? "" : message));
    boolean accepted = "0300".equals(code);
    String protocolNumber = null;
    if (accepted) {
      protocolNumber = childText(root, "dProtConsLote");
      if (protocolNumber == null
          || !PROTOCOL_NUMBER.matcher(protocolNumber).matches()
          || isZero(protocolNumber)) {
        throw new ProtocolException(
            "SIFEN accepted the batch (0300) but did not give a valid protocol number"
                + " (dProtConsLote, BRSch05); query the batch by the CDC of one of its documents"
                + " before resending anything",
            exchange);
      }
    }
    return new BatchSubmission(
        accepted,
        protocolNumber,
        processingTime(childText(root, "dTpoProces")),
        messages,
        exchange);
  }

  /**
   * The SOAP envelope of a batch result query: {@code rEnviConsLoteDe} with {@code dId} and {@code
   * dProtConsLote} (CSch01-CSch03).
   *
   * @param requestId the request id ({@code dId}, CSch02)
   * @param protocolNumber the batch protocol number (CSch03)
   * @return the complete envelope
   * @throws IllegalArgumentException if the protocol number is not 1 to 28 digits
   */
  public static byte[] resultRequest(String requestId, String protocolNumber) {
    if (!PROTOCOL_NUMBER.matcher(protocolNumber).matches()) {
      throw new IllegalArgumentException(
          "A batch protocol number (dProtConsLote) has 1 to 28 digits and nothing else"
              + " (WS_SiConsLote_v141.xsd; C-708)");
    }
    String body =
        "<rEnviConsLoteDe xmlns=\""
            + Soap.SIFEN_NS
            + "\"><dId>"
            + Soap.escape(requestId)
            + "</dId><dProtConsLote>"
            + protocolNumber
            + "</dProtConsLote></rEnviConsLoteDe>";
    return Soap.envelope(body.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Reads the answer of the batch result query ({@code rResEnviConsLoteDe}, CRSch01-CRSch055).
   *
   * <p>The batch state comes from {@code dCodResLot} (MT150 9.3.2 Tabla F and 12.3.3.3; Guía de
   * mejores prácticas, "consulta-lote"):
   *
   * <ul>
   *   <li>0360 "Número del Lote inexistente": {@link BatchResult.State#NOT_FOUND};
   *   <li>0361 "Lote en procesamiento": {@link BatchResult.State#IN_PROCESS}. The Manual marks it
   *       with effect R, but the batch is not rejected, only unfinished (C-806): never resend its
   *       documents;
   *   <li>0362 "Procesamiento de lote concluido": {@link BatchResult.State#PROCESSED}, with one
   *       {@code gResProcLote} per document;
   *   <li>0364 "Consulta extemporánea de Lote", only in the Guía (C-807): {@link
   *       BatchResult.State#EXPIRED}; query each CDC with {@code siConsDE}.
   * </ul>
   *
   * <p>The documented rejections of the query or of the batch as a whole (0320, 0340, 0363 of MT150
   * 9.3.2 and 12.3.3, and the generic service rejections of MT150 12.2) are a {@link
   * BatchResult.State#REJECTED} result with their message, never an exception (ADR-0006). Only an
   * undocumented code is a {@link ProtocolException}.
   *
   * <p>Each {@code gResProcLote} becomes a {@link DocumentResult}: CDC ({@code id}), state from
   * {@code dEstRes} ("Aprobado", "Aprobado con observación", "Rechazado", CRSch051), {@code
   * dProtAut} and the {@code gResProc} messages. The processing time of each document is the
   * batch's {@code dFecProc} (CRSch02), which the Manual allows to be empty (C-714): it is then
   * {@code null}.
   *
   * @param exchange the exchange
   * @return the batch result
   * @throws ProtocolException if the response is not {@code rResEnviConsLoteDe}, its code is
   *     missing or unknown, or a document result cannot be read
   */
  public static BatchResult parseResult(Exchange exchange) {
    Element root = Soap.body(exchange, "rResEnviConsLoteDe");
    String code = childText(root, "dCodResLot");
    if (code == null) {
      throw new ProtocolException(
          "SIFEN's batch result answer has no result code (dCodResLot, CRSch03)", exchange);
    }
    String message = childText(root, "dMsgResLot");
    BatchResult.State state;
    switch (code) {
      case "0360":
        state = BatchResult.State.NOT_FOUND;
        break;
      case "0361":
        state = BatchResult.State.IN_PROCESS;
        break;
      case "0362":
        state = BatchResult.State.PROCESSED;
        break;
      case "0364":
        state = BatchResult.State.EXPIRED;
        break;
      case "0320":
      case "0340":
      case "0363":
        state = BatchResult.State.REJECTED;
        break;
      default:
        if (ReceptionCodes.isServiceRejection(code, "")) {
          state = BatchResult.State.REJECTED;
          break;
        }
        throw new ProtocolException(
            "SIFEN answered the batch result query with code "
                + code
                + (message == null ? "" : " (" + message + ")")
                + ", which is not a batch state (0360, 0361, 0362 or 0364)",
            exchange);
    }
    OffsetDateTime processedAt = dateTime(childText(root, "dFecProc"));
    List<DocumentResult> documents = new ArrayList<>();
    for (Element group : Soap.children(root, "gResProcLote")) {
      String cdc = childText(group, "id");
      String status = childText(group, "dEstRes");
      if (cdc == null || status == null) {
        throw new ProtocolException(
            "A document result of the batch (gResProcLote) has no CDC (id) or state (dEstRes)",
            exchange);
      }
      documents.add(
          new DocumentResult(
              cdc,
              BatchStatus.ofDocument(status, exchange),
              childText(group, "dProtAut"),
              processedAt,
              Soap.messages(Soap.children(group, "gResProc"))));
    }
    return new BatchResult(
        state,
        documents,
        List.of(new ResultMessage(code, message == null ? "" : message)),
        exchange);
  }

  /** The trimmed text of a direct child, or null if absent or empty. */
  static @Nullable String childText(Element parent, String localName) {
    Element child = Soap.child(parent, localName);
    if (child == null) {
      return null;
    }
    String text = child.getTextContent().trim();
    return text.isEmpty() ? null : text;
  }

  private static boolean isZero(String digits) {
    for (int i = 0; i < digits.length(); i++) {
      if (digits.charAt(i) != '0') {
        return false;
      }
    }
    return true;
  }

  private static @Nullable Duration processingTime(@Nullable String seconds) {
    if (seconds == null || !NON_NEGATIVE_INTEGER.matcher(seconds).matches()) {
      return null;
    }
    long value = Long.parseLong(seconds);
    return value == 0 ? null : Duration.ofSeconds(value);
  }

  private static @Nullable OffsetDateTime dateTime(@Nullable String value) {
    if (value == null) {
      return null;
    }
    try {
      return OffsetDateTime.parse(value);
    } catch (DateTimeParseException e) {
      return null;
    }
  }
}
