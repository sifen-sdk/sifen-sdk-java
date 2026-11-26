/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.testing;

import io.github.sifensdk.client.ResultMessage;
import io.github.sifensdk.testing.Interaction.ReceivedDocument;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

/**
 * Ready-made responses of every SIFEN web service, to {@linkplain SimulatedSifen#enqueue(String,
 * SimulatedResponse) enqueue} in a {@link SimulatedSifen}.
 *
 * <p>Every SOAP response is valid according to the official XSD of its service ({@code
 * WS_SiRecepDE_v150.xsd}, {@code WS_SiRecepLoteDE_v141.xsd}, {@code WS_SiConsLote_v141.xsd}, {@code
 * WS_SiConsDE_v141.xsd}, {@code WS_SiConsRUC_v141.xsd} and {@code WS_SiRecepEvento_v150.xsd}).
 * Where the Technical Manual and the XSDs disagree, the responses follow the XSDs, which is what
 * SIFEN is expected to send:
 *
 * <ul>
 *   <li>{@code dFecProc} carries the date, the time and the offset of Paraguay, for example {@code
 *       2026-11-26T10:15:30-03:00} (contradiction C-703 of the specification).
 *   <li>In the synchronous reception, {@code dEstRes} and {@code dProtAut} are children of {@code
 *       rProtDe}, not of {@code gResProc}; the CDC goes in {@code Id}; and {@code rProtDe} hangs
 *       directly from {@code rRetEnviDe}, without {@code xProtDe} (C-701, C-702, C-704).
 *   <li>The finished batch result carries {@code gResProcLote} per document, not a document
 *       container (C-715), with at most 5 messages per document (C-713).
 *   <li>The document query answers with {@code rEnviConsDeResponse} (C-716), and its container
 *       {@code xContenDE} is text holding escaped XML (C-717).
 *   <li>The taxpayer data hang directly from {@code xContRUC}, without {@code rContRUC} (C-719).
 * </ul>
 *
 * <p>Responses are rendered when the request arrives: they take the CDC, the batch number and the
 * event ids from it (SIFEN's responses do not echo the request id {@code dId}; see {@link
 * RecordedRequest#requestId()}). Approvals are remembered, so a later document query of an approved
 * CDC finds it, and a batch result query knows the CDCs of the batch it refers to.
 *
 * <p>Response codes and messages come from the Technical Manual v150 (chapter 12) and the best
 * practices guide of the DNIT.
 */
public final class SifenResponses {

  private static final String NS = " xmlns:ns2=\"" + Xml.SIFEN_NS + "\"";

  private static final ResultMessage DOCUMENT_APPROVED =
      new ResultMessage("0260", "Autorización del DE satisfactoria");
  private static final ResultMessage EVENT_APPROVED =
      new ResultMessage("0600", "Evento registrado correctamente");

  /** Synthetic taxpayer name used by the default response of the taxpayer query. */
  static final String SYNTHETIC_NAME = "CONTRIBUYENTE SINTETICO DE PRUEBA";

  private SifenResponses() {}

  // --- siRecepDE ---------------------------------------------------------------------------------

  /**
   * Synchronous reception: document approved (code 0260, with authorization number and the digest
   * value of the signed document). The default response of {@link SimulatedSifen#RECEPTION}.
   *
   * @return the response
   */
  public static SimulatedResponse receptionApproved() {
    return reception(Outcome.approved());
  }

  /**
   * Synchronous reception: document approved with an observation.
   *
   * @param code the observation code, for example {@code 1005}
   * @param message the observation message, for example "Transmisión extemporánea del DE"
   * @return the response
   * @throws IllegalArgumentException if the code or the message break the XSD limits
   */
  public static SimulatedResponse receptionApprovedWithObservation(String code, String message) {
    return reception(Outcome.approvedWithObservation(code, message));
  }

  /**
   * Synchronous reception: document rejected.
   *
   * @param code the rejection code, for example {@code 1001}
   * @param message the rejection message, for example "CDC duplicado"
   * @return the response
   * @throws IllegalArgumentException if the code or the message break the XSD limits
   */
  public static SimulatedResponse receptionRejected(String code, String message) {
    return reception(Outcome.rejected(code, message));
  }

  /**
   * Synchronous reception ({@code rRetEnviDe}) with the given outcome for the document sent.
   *
   * @param outcome the outcome
   * @return the response
   */
  public static SimulatedResponse reception(Outcome outcome) {
    Objects.requireNonNull(outcome, "outcome");
    return SimulatedResponse.soap(
        200,
        "siRecepDE " + outcome,
        i -> {
          List<ReceivedDocument> documents = i.receptionDocuments();
          ReceivedDocument document = documents.isEmpty() ? null : documents.get(0);
          String cdc = outcome.id() != null ? outcome.id() : document == null ? null : document.cdc;
          String authorization = i.state().newAuthorization();
          if (document != null) {
            i.state().rememberReceived(document.cdc, document.xml);
          }
          if (cdc != null && outcome.approvedOrObserved()) {
            i.state().approve(cdc, authorization);
          }
          StringBuilder sb = new StringBuilder();
          sb.append("<ns2:rRetEnviDe").append(NS).append("><ns2:rProtDe>");
          if (cdc != null) {
            element(sb, "Id", cdc);
          }
          element(sb, "dFecProc", i.processedAt());
          String digest = document == null ? null : document.digestValue;
          if (digest != null && isBase64(digest)) {
            element(sb, "dDigVal", digest);
          }
          element(sb, "dEstRes", outcome.kind().estRes);
          if (outcome.approvedOrObserved()) {
            element(sb, "dProtAut", authorization);
          }
          for (ResultMessage m : outcome.messages(DOCUMENT_APPROVED)) {
            sb.append("<ns2:gResProc>");
            element(sb, "dCodRes", m.code());
            element(sb, "dMsgRes", m.message());
            sb.append("</ns2:gResProc>");
          }
          sb.append("</ns2:rProtDe></ns2:rRetEnviDe>");
          return sb.toString();
        });
  }

  // --- siRecepLoteDE -----------------------------------------------------------------------------

  /**
   * Batch reception: batch accepted for processing (code 0300) with a new 17-digit protocol number.
   * The default response of {@link SimulatedSifen#BATCH}.
   *
   * <p>{@code dTpoProces} is 0, as in the examples of the best practices guide (the Manual says it
   * is never below one second; contradiction C-710).
   *
   * @return the response
   */
  public static SimulatedResponse batchAccepted() {
    return batchAccepted(null);
  }

  /**
   * Batch reception: batch accepted with the given protocol number.
   *
   * @param protocolNumber the protocol number ({@code dProtConsLote}), up to 28 digits (C-708)
   * @return the response
   * @throws IllegalArgumentException if the protocol number is not 1 to 28 digits
   */
  public static SimulatedResponse batchAccepted(@Nullable String protocolNumber) {
    if (protocolNumber != null && !protocolNumber.matches("[0-9]{1,28}")) {
      throw new IllegalArgumentException("A batch protocol number has 1 to 28 digits");
    }
    return SimulatedResponse.soap(
        200,
        "siRecepLoteDE accepted",
        i -> {
          String protocol = protocolNumber != null ? protocolNumber : i.state().newProtocolNumber();
          List<String> cdcs = new ArrayList<>();
          for (ReceivedDocument d : i.batchDocuments()) {
            cdcs.add(d.cdc);
            i.state().rememberReceived(d.cdc, d.xml);
          }
          i.state().rememberBatch(protocol, cdcs);
          StringBuilder sb = new StringBuilder();
          sb.append("<ns2:rResEnviLoteDe").append(NS).append('>');
          element(sb, "dFecProc", i.processedAt());
          element(sb, "dCodRes", "0300");
          element(sb, "dMsgRes", "Lote recibido con éxito");
          element(sb, "dProtConsLote", protocol);
          element(sb, "dTpoProces", "0");
          sb.append("</ns2:rResEnviLoteDe>");
          return sb.toString();
        });
  }

  /**
   * Batch reception: batch not queued for processing (code 0301). SIFEN will not process it.
   *
   * @return the response
   */
  public static SimulatedResponse batchRejected() {
    return batchRejected("0301", "Lote no encolado para procesamiento");
  }

  /**
   * Batch reception: batch rejected with the given code, without protocol number.
   *
   * @param code the code, for example {@code 0301} or {@code 0270}
   * @param message the message
   * @return the response
   * @throws IllegalArgumentException if the code or the message break the XSD limits
   */
  public static SimulatedResponse batchRejected(String code, String message) {
    ResultMessage m = Outcome.message(code, message);
    return SimulatedResponse.soap(
        200,
        "siRecepLoteDE rejected " + code,
        i -> {
          StringBuilder sb = new StringBuilder();
          sb.append("<ns2:rResEnviLoteDe").append(NS).append('>');
          element(sb, "dFecProc", i.processedAt());
          element(sb, "dCodRes", m.code());
          element(sb, "dMsgRes", m.message());
          element(sb, "dTpoProces", "0");
          sb.append("</ns2:rResEnviLoteDe>");
          return sb.toString();
        });
  }

  // --- siResultLoteDE ----------------------------------------------------------------------------

  /**
   * Batch result: still processing (code 0361), with the batch number in the message as SIFEN
   * writes it ("Lote {n} en procesamiento").
   *
   * @return the response
   */
  public static SimulatedResponse batchInProcess() {
    return batchResult("0361", "in process", n -> "Lote {" + n + "} en procesamiento");
  }

  /**
   * Batch result: the batch number does not exist (code 0360). The default response of {@link
   * SimulatedSifen#BATCH_RESULT} for batches this simulated SIFEN did not accept.
   *
   * @return the response
   */
  public static SimulatedResponse batchNotFound() {
    return batchResult("0360", "not found", n -> "Número del Lote inexistente");
  }

  /**
   * Batch result: query out of time (code 0364); SIFEN no longer answers for that batch and its
   * documents must be queried one by one.
   *
   * @return the response
   */
  public static SimulatedResponse batchExpired() {
    return batchResult("0364", "expired", n -> "Consulta extemporánea de Lote");
  }

  /**
   * Batch result: processing finished (code 0362) and every document of the batch approved. The
   * CDCs are those of the batch the query refers to, if this simulated SIFEN accepted it; the
   * default response of {@link SimulatedSifen#BATCH_RESULT} for those batches.
   *
   * @return the response
   */
  public static SimulatedResponse batchProcessed() {
    return batchProcessed(new Outcome[0]);
  }

  /**
   * Batch result: processing finished (code 0362) with the given outcome for each document. The
   * outcome at position <i>k</i> applies to the <i>k</i>-th document of the batch, unless it is
   * {@linkplain Outcome#forId(String) pinned to a CDC}; with no outcomes, every document is
   * approved. At most 5 messages per document are reported (C-713) and at most 50 documents.
   *
   * @param outcomes the outcome of each document
   * @return the response
   */
  public static SimulatedResponse batchProcessed(Outcome... outcomes) {
    List<Outcome> list = List.of(outcomes);
    return SimulatedResponse.soap(
        200,
        "siResultLoteDE processed " + list,
        i -> {
          String protocol = i.queriedBatch();
          List<String> cdcs = protocol == null ? null : i.state().batch(protocol);
          if (cdcs == null) {
            cdcs = List.of();
          }
          int count = list.isEmpty() ? cdcs.size() : list.size();
          if (count > 50) {
            throw new IllegalStateException("A batch has at most 50 documents, got " + count);
          }
          StringBuilder sb = new StringBuilder();
          sb.append("<ns2:rResEnviConsLoteDe").append(NS).append('>');
          element(sb, "dFecProc", i.processedAt());
          element(sb, "dCodResLot", "0362");
          element(sb, "dMsgResLot", "Procesamiento de lote {" + display(protocol) + "} concluido");
          for (int k = 0; k < count; k++) {
            Outcome outcome = list.isEmpty() ? Outcome.approved() : list.get(k);
            String cdc = outcome.id() != null ? outcome.id() : k < cdcs.size() ? cdcs.get(k) : null;
            if (cdc == null) {
              throw new IllegalStateException(
                  "Outcome "
                      + (k + 1)
                      + " has no CDC: the queried batch has "
                      + cdcs.size()
                      + " known documents; pin it with Outcome.forId(cdc)");
            }
            sb.append("<ns2:gResProcLote>");
            element(sb, "id", cdc);
            element(sb, "dEstRes", outcome.kind().estRes);
            if (outcome.approvedOrObserved()) {
              String authorization = i.state().newAuthorization();
              i.state().approve(cdc, authorization);
              element(sb, "dProtAut", authorization);
            }
            List<ResultMessage> messages = outcome.messages(DOCUMENT_APPROVED);
            for (ResultMessage m : messages.subList(0, Math.min(5, messages.size()))) {
              sb.append("<ns2:gResProc>");
              element(sb, "dCodRes", m.code());
              element(sb, "dMsgRes", m.message());
              sb.append("</ns2:gResProc>");
            }
            sb.append("</ns2:gResProcLote>");
          }
          sb.append("</ns2:rResEnviConsLoteDe>");
          return sb.toString();
        });
  }

  /** Default of the batch result service: processed if the batch is known, otherwise 0360. */
  static SimulatedResponse batchResultFromState() {
    SimulatedResponse processed = batchProcessed();
    SimulatedResponse notFound = batchNotFound();
    return SimulatedResponse.dynamic(
        "siResultLoteDE from state",
        i -> {
          String protocol = i.queriedBatch();
          return protocol != null && i.state().batch(protocol) != null ? processed : notFound;
        });
  }

  private static SimulatedResponse batchResult(
      String code, String description, Function<String, String> message) {
    return SimulatedResponse.soap(
        200,
        "siResultLoteDE " + description,
        i -> {
          StringBuilder sb = new StringBuilder();
          sb.append("<ns2:rResEnviConsLoteDe").append(NS).append('>');
          element(sb, "dFecProc", i.processedAt());
          element(sb, "dCodResLot", code);
          element(sb, "dMsgResLot", message.apply(display(i.queriedBatch())));
          sb.append("</ns2:rResEnviConsLoteDe>");
          return sb.toString();
        });
  }

  // --- siConsDE ----------------------------------------------------------------------------------

  /**
   * Document query: found (code 0422). The container holds the document this simulated SIFEN
   * approved with that CDC; if it never received it, a minimal synthetic {@code rDE} with only the
   * CDC.
   *
   * @return the response
   */
  public static SimulatedResponse documentFound() {
    return documentFound(null);
  }

  /**
   * Document query: found (code 0422), with the given document in the container.
   *
   * @param documentXml the {@code rDE} to return, without XML declaration; null for the document
   *     this simulated SIFEN approved
   * @return the response
   */
  public static SimulatedResponse documentFound(@Nullable String documentXml) {
    return SimulatedResponse.soap(
        200,
        "siConsDE found",
        i -> {
          String cdc = i.text("dCDC");
          State.Approved approved = cdc == null ? null : i.state().approvedDocument(cdc);
          String rde =
              documentXml != null
                  ? documentXml
                  : approved != null
                      ? approved.xml
                      : "<rDE xmlns=\""
                          + Xml.SIFEN_NS
                          + "\"><DE Id=\""
                          + Xml.escape(display(cdc))
                          + "\"/></rDE>";
          String authorization =
              approved != null ? approved.authorization : i.state().newAuthorization();
          String container =
              "<rContDe>" + rde + "<dProtAut>" + authorization + "</dProtAut></rContDe>";
          StringBuilder sb = new StringBuilder();
          sb.append("<ns2:rEnviConsDeResponse").append(NS).append('>');
          element(sb, "dFecProc", i.processedAt());
          element(sb, "dCodRes", "0422");
          element(sb, "dMsgRes", "CDC encontrado");
          element(sb, "xContenDE", container);
          sb.append("</ns2:rEnviConsDeResponse>");
          return sb.toString();
        });
  }

  /**
   * Document query: not found (code 0420), with the message SIFEN actually sends ("Documento No
   * Existe en SIFEN o ha sido Rechazado"); it also covers rejected documents. The default response
   * of {@link SimulatedSifen#DOCUMENT_QUERY} for CDCs this simulated SIFEN did not approve.
   *
   * @return the response
   */
  public static SimulatedResponse documentNotFound() {
    return documentResponse("0420", "Documento No Existe en SIFEN o ha sido Rechazado");
  }

  /**
   * Document query: the RUC of the connection certificate may not query that document (code 0421).
   *
   * @return the response
   */
  public static SimulatedResponse documentNotPermitted() {
    return documentResponse("0421", "RUC Certificado sin permiso");
  }

  /** Default of the document query: found if this simulated SIFEN approved it, otherwise 0420. */
  static SimulatedResponse documentFromState() {
    SimulatedResponse found = documentFound();
    SimulatedResponse notFound = documentNotFound();
    return SimulatedResponse.dynamic(
        "siConsDE from state",
        i -> {
          String cdc = i.text("dCDC");
          return cdc != null && i.state().approvedDocument(cdc) != null ? found : notFound;
        });
  }

  private static SimulatedResponse documentResponse(String code, String message) {
    return SimulatedResponse.soap(
        200,
        "siConsDE " + code,
        i -> {
          StringBuilder sb = new StringBuilder();
          sb.append("<ns2:rEnviConsDeResponse").append(NS).append('>');
          element(sb, "dFecProc", i.processedAt());
          element(sb, "dCodRes", code);
          element(sb, "dMsgRes", message);
          sb.append("</ns2:rEnviConsDeResponse>");
          return sb.toString();
        });
  }

  // --- siConsRUC ---------------------------------------------------------------------------------

  /**
   * Taxpayer query: found (code 0502), active ({@code ACT}, "ACTIVO") and electronic issuer. The
   * RUC is the one queried. The default response of {@link SimulatedSifen#RUC_QUERY}, with a
   * synthetic name.
   *
   * @param name the taxpayer's name ({@code dRazCons}), 1 to 250 characters
   * @return the response
   * @throws IllegalArgumentException if the name breaks the XSD limits
   */
  public static SimulatedResponse rucFound(String name) {
    return rucFound(name, "ACT", "ACTIVO", true);
  }

  /**
   * Taxpayer query: found (code 0502) with the given data.
   *
   * @param name the taxpayer's name ({@code dRazCons}), 1 to 250 characters
   * @param statusCode the status code ({@code dCodEstCons}), 3 characters, for example {@code ACT}
   * @param statusDescription the status ({@code dDesEstCons}), up to 25 characters
   * @param electronicIssuer whether the taxpayer is an electronic issuer ({@code dRUCFactElec} S/N)
   * @return the response
   * @throws IllegalArgumentException if a value breaks the XSD limits
   */
  public static SimulatedResponse rucFound(
      String name, String statusCode, String statusDescription, boolean electronicIssuer) {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(statusCode, "statusCode");
    Objects.requireNonNull(statusDescription, "statusDescription");
    if (name.isEmpty() || name.length() > 250) {
      throw new IllegalArgumentException("dRazCons has 1 to 250 characters");
    }
    if (statusCode.length() != 3) {
      throw new IllegalArgumentException("dCodEstCons has 3 characters");
    }
    if (statusDescription.length() > 25) {
      throw new IllegalArgumentException("dDesEstCons has at most 25 characters");
    }
    return SimulatedResponse.soap(
        200,
        "siConsRUC found",
        i -> {
          String ruc = i.text("dRUCCons");
          StringBuilder sb = new StringBuilder();
          sb.append("<ns2:rResEnviConsRUC").append(NS).append('>');
          element(sb, "dCodRes", "0502");
          element(sb, "dMsgRes", "RUC encontrado");
          sb.append("<ns2:xContRUC>");
          element(sb, "dRUCCons", ruc == null ? "44444401" : ruc);
          element(sb, "dRazCons", name);
          element(sb, "dCodEstCons", statusCode);
          element(sb, "dDesEstCons", statusDescription);
          element(sb, "dRUCFactElec", electronicIssuer ? "S" : "N");
          sb.append("</ns2:xContRUC></ns2:rResEnviConsRUC>");
          return sb.toString();
        });
  }

  /**
   * Taxpayer query: the RUC does not exist (code 0500).
   *
   * @return the response
   */
  public static SimulatedResponse rucNotFound() {
    return rucResponse("0500", "RUC no existe");
  }

  /**
   * Taxpayer query: the RUC of the certificate may not use the service (code 0501).
   *
   * @return the response
   */
  public static SimulatedResponse rucNotPermitted() {
    return rucResponse("0501", "RUC sin permiso consulta WS");
  }

  private static SimulatedResponse rucResponse(String code, String message) {
    return SimulatedResponse.soap(
        200,
        "siConsRUC " + code,
        i ->
            "<ns2:rResEnviConsRUC"
                + NS
                + "><ns2:dCodRes>"
                + code
                + "</ns2:dCodRes><ns2:dMsgRes>"
                + Xml.escape(message)
                + "</ns2:dMsgRes></ns2:rResEnviConsRUC>");
  }

  // --- siRecepEvento -----------------------------------------------------------------------------

  /**
   * Event reception: every event sent registered (code 0600, with authorization number). The
   * default response of {@link SimulatedSifen#EVENTS}.
   *
   * @return the response
   */
  public static SimulatedResponse eventRegistered() {
    return eventsAll(Outcome.approved());
  }

  /**
   * Event reception: every event sent rejected with the given code.
   *
   * @param code the rejection code, for example {@code 4002}
   * @param message the rejection message, for example "CDC no existente en el SIFEN"
   * @return the response
   * @throws IllegalArgumentException if the code or the message break the XSD limits
   */
  public static SimulatedResponse eventRejected(String code, String message) {
    return eventsAll(Outcome.rejected(code, message));
  }

  /**
   * Event reception ({@code rRetEnviEventoDe}) with the given outcome for each event. The outcome
   * at position <i>k</i> applies to the <i>k</i>-th event of the request (its {@code rEve/@Id}),
   * unless it is {@linkplain Outcome#forId(String) pinned to an event id}.
   *
   * @param outcomes the outcome of each event, 1 to 15
   * @return the response
   * @throws IllegalArgumentException if there are no outcomes or more than 15
   */
  public static SimulatedResponse events(Outcome... outcomes) {
    List<Outcome> list = List.of(outcomes);
    if (list.isEmpty() || list.size() > 15) {
      throw new IllegalArgumentException("SIFEN answers 1 to 15 events, got " + list.size());
    }
    return SimulatedResponse.soap(
        200,
        "siRecepEvento " + list,
        i -> {
          List<String> ids = i.eventIds();
          List<String> resolved = new ArrayList<>();
          for (int k = 0; k < list.size(); k++) {
            String id =
                list.get(k).id() != null ? list.get(k).id() : k < ids.size() ? ids.get(k) : null;
            if (id == null) {
              throw new IllegalStateException(
                  "Outcome "
                      + (k + 1)
                      + " has no event id: the request has "
                      + ids.size()
                      + " events; pin it with Outcome.forId(id)");
            }
            resolved.add(id);
          }
          return eventResponse(i, list, resolved);
        });
  }

  private static SimulatedResponse eventsAll(Outcome outcome) {
    return SimulatedResponse.soap(
        200,
        "siRecepEvento all " + outcome,
        i -> {
          List<String> ids = i.eventIds();
          if (ids.isEmpty()) {
            ids = List.of("0");
          } else if (ids.size() > 15) {
            ids = ids.subList(0, 15);
          }
          List<Outcome> outcomes = new ArrayList<>();
          for (int k = 0; k < ids.size(); k++) {
            outcomes.add(outcome);
          }
          return eventResponse(i, outcomes, ids);
        });
  }

  private static String eventResponse(Interaction i, List<Outcome> outcomes, List<String> ids) {
    StringBuilder sb = new StringBuilder();
    sb.append("<ns2:rRetEnviEventoDe").append(NS).append('>');
    element(sb, "dFecProc", i.processedAt());
    for (int k = 0; k < outcomes.size(); k++) {
      Outcome outcome = outcomes.get(k);
      sb.append("<ns2:gResProcEVe>");
      element(sb, "dEstRes", outcome.kind().estRes);
      if (outcome.approvedOrObserved()) {
        element(sb, "dProtAut", i.state().newAuthorization());
      }
      element(sb, "id", ids.get(k));
      for (ResultMessage m : outcome.messages(EVENT_APPROVED)) {
        sb.append("<ns2:gResProc>");
        element(sb, "dCodRes", m.code());
        element(sb, "dMsgRes", m.message());
        sb.append("</ns2:gResProc>");
      }
      sb.append("</ns2:gResProcEVe>");
    }
    sb.append("</ns2:rRetEnviEventoDe>");
    return sb.toString();
  }

  // --- failures ----------------------------------------------------------------------------------

  /**
   * An HTML page from the load balancer in front of SIFEN instead of a SOAP response, with HTTP
   * 200. SIFEN answers like this when it does not accept the client certificate (for example, a
   * certificate not enabled for the environment; Technical Note 16) or when a service is down.
   *
   * @return the response
   */
  public static SimulatedResponse balancerHtmlPage() {
    String html =
        "<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>Request Rejected</title></head>"
            + "<body><h1>Su sesión ha finalizado</h1>"
            + "<p>The requested URL was rejected. (Simulated SIFEN: synthetic load balancer page.)</p>"
            + "</body></html>";
    return SimulatedResponse.of(
        200, "text/html; charset=utf-8", html.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * A SOAP 1.2 fault ({@code env:Receiver}) with HTTP 500.
   *
   * @param reason the text of the fault reason
   * @return the response
   */
  public static SimulatedResponse soapFault(String reason) {
    Objects.requireNonNull(reason, "reason");
    return SimulatedResponse.soap(
        500,
        "SOAP fault",
        i ->
            "<env:Fault><env:Code><env:Value>env:Receiver</env:Value></env:Code>"
                + "<env:Reason><env:Text xml:lang=\"es\">"
                + Xml.escape(reason)
                + "</env:Text></env:Reason></env:Fault>");
  }

  /**
   * An HTTP error with a plain text body, for example 500 or 503.
   *
   * @param httpStatus the HTTP status
   * @return the response
   * @throws IllegalArgumentException if the status is not between 100 and 599
   */
  public static SimulatedResponse httpError(int httpStatus) {
    return SimulatedResponse.of(
        httpStatus,
        "text/plain; charset=utf-8",
        ("HTTP " + httpStatus + " (simulated SIFEN)").getBytes(StandardCharsets.UTF_8));
  }

  // --- helpers -----------------------------------------------------------------------------------

  private static void element(StringBuilder sb, String name, String value) {
    sb.append("<ns2:")
        .append(name)
        .append('>')
        .append(Xml.escape(value))
        .append("</ns2:")
        .append(name)
        .append('>');
  }

  private static String display(@Nullable String value) {
    return value == null ? "" : value;
  }

  private static boolean isBase64(String value) {
    try {
      Base64.getMimeDecoder().decode(value);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
