/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.testing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class SifenResponsesTest {

  private static final String RECEPTION_XSD = "WS_SiRecepDE_v150.xsd";
  private static final String BATCH_XSD = "WS_SiRecepLoteDE_v141.xsd";
  private static final String BATCH_RESULT_XSD = "WS_SiConsLote_v141.xsd";
  private static final String DOCUMENT_XSD = "WS_SiConsDE_v141.xsd";
  private static final String RUC_XSD = "WS_SiConsRUC_v141.xsd";
  private static final String EVENTS_XSD = "WS_SiRecepEvento_v150.xsd";

  private final State state =
      new State(Clock.fixed(Instant.parse("2026-11-26T13:15:30Z"), ZoneOffset.UTC));

  @Test
  void receptionApprovedTakesCdcAndDigestFromTheRequest() throws Exception {
    Element body =
        valid(
            RECEPTION_XSD,
            SifenResponses.receptionApproved(),
            SimulatedSifen.Service.RECEPTION.path(),
            Requests.reception("123", Requests.CDC));

    assertThat(body.getLocalName()).isEqualTo("rRetEnviDe");
    assertThat(Xml.text(body, "Id")).isEqualTo(Requests.CDC);
    assertThat(Xml.text(body, "dFecProc")).isEqualTo("2026-11-26T10:15:30-03:00");
    assertThat(Xml.text(body, "dDigVal")).isEqualTo(Requests.DIGEST);
    assertThat(Xml.text(body, "dEstRes")).isEqualTo("Aprobado");
    assertThat(Xml.text(body, "dProtAut")).matches("[0-9]{10}");
    assertThat(Xml.text(body, "dCodRes")).isEqualTo("0260");
    // C-704: dEstRes is a child of rProtDe, not of gResProc.
    assertThat(Xml.descendant(body, "dEstRes").getParentNode().getLocalName()).isEqualTo("rProtDe");
  }

  @Test
  void receptionApprovedWithObservation() throws Exception {
    Element body =
        valid(
            RECEPTION_XSD,
            SifenResponses.receptionApprovedWithObservation(
                "1005", "Transmisión extemporánea del DE"),
            SimulatedSifen.Service.RECEPTION.path(),
            Requests.reception("123", Requests.CDC));

    assertThat(Xml.text(body, "dEstRes")).isEqualTo("Aprobado con observación");
    assertThat(Xml.text(body, "dProtAut")).isNotNull();
    assertThat(Xml.text(body, "dCodRes")).isEqualTo("1005");
  }

  @Test
  void receptionRejectedHasNoAuthorization() throws Exception {
    Element body =
        valid(
            RECEPTION_XSD,
            SifenResponses.receptionRejected("1001", "CDC duplicado"),
            SimulatedSifen.Service.RECEPTION.path(),
            Requests.reception("123", Requests.CDC));

    assertThat(Xml.text(body, "dEstRes")).isEqualTo("Rechazado");
    assertThat(Xml.text(body, "dProtAut")).isNull();
    assertThat(Xml.text(body, "dCodRes")).isEqualTo("1001");
    assertThat(Xml.text(body, "dMsgRes")).isEqualTo("CDC duplicado");
  }

  @Test
  void receptionOfAMalformedRequestIsStillValid() throws Exception {
    Element body =
        valid(
            RECEPTION_XSD,
            SifenResponses.receptionRejected("0160", "XML malformado"),
            SimulatedSifen.Service.RECEPTION.path(),
            "<not-xml".getBytes(java.nio.charset.StandardCharsets.UTF_8));

    assertThat(Xml.text(body, "Id")).isNull();
    assertThat(Xml.text(body, "dCodRes")).isEqualTo("0160");
  }

  @Test
  void batchAcceptedThenProcessedWithTheCdcsOfTheBatch() throws Exception {
    Element accepted =
        valid(
            BATCH_XSD,
            SifenResponses.batchAccepted(),
            SimulatedSifen.Service.BATCH.path(),
            Requests.batch("1", Requests.CDC, Requests.OTHER_CDC));
    String protocol = Xml.text(accepted, "dProtConsLote");
    assertThat(protocol).matches("[1-9][0-9]{16}");
    assertThat(Xml.text(accepted, "dCodRes")).isEqualTo("0300");
    assertThat(Xml.text(accepted, "dTpoProces")).isEqualTo("0");

    Element processed =
        valid(
            BATCH_RESULT_XSD,
            SifenResponses.batchProcessed(
                Outcome.approved(), Outcome.rejected("1001", "CDC duplicado")),
            SimulatedSifen.Service.BATCH_RESULT.path(),
            Requests.batchResult("2", protocol));

    assertThat(Xml.text(processed, "dCodResLot")).isEqualTo("0362");
    assertThat(Xml.text(processed, "dMsgResLot")).contains(protocol);
    List<Element> documents = Xml.descendants(processed, "gResProcLote");
    assertThat(documents).hasSize(2);
    assertThat(Xml.text(documents.get(0), "id")).isEqualTo(Requests.CDC);
    assertThat(Xml.text(documents.get(0), "dEstRes")).isEqualTo("Aprobado");
    assertThat(Xml.text(documents.get(0), "dProtAut")).matches("[0-9]{10}");
    assertThat(Xml.text(documents.get(1), "id")).isEqualTo(Requests.OTHER_CDC);
    assertThat(Xml.text(documents.get(1), "dEstRes")).isEqualTo("Rechazado");
    assertThat(Xml.text(documents.get(1), "dProtAut")).isNull();
    assertThat(Xml.text(documents.get(1), "dCodRes")).isEqualTo("1001");
  }

  @Test
  void batchAcceptedWithAGivenProtocolNumberLongerThanALong() throws Exception {
    String protocol = "1234567890123456789012345678";
    Element accepted =
        valid(
            BATCH_XSD,
            SifenResponses.batchAccepted(protocol),
            SimulatedSifen.Service.BATCH.path(),
            Requests.batch("1", Requests.CDC));

    assertThat(Xml.text(accepted, "dProtConsLote")).isEqualTo(protocol);
    assertThatThrownBy(() -> SifenResponses.batchAccepted("12a"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void batchRejected() throws Exception {
    Element body =
        valid(
            BATCH_XSD,
            SifenResponses.batchRejected(),
            SimulatedSifen.Service.BATCH.path(),
            Requests.batch("1", Requests.CDC));

    assertThat(Xml.text(body, "dCodRes")).isEqualTo("0301");
    assertThat(Xml.text(body, "dProtConsLote")).isNull();
  }

  @Test
  void batchResultStates() throws Exception {
    byte[] query = Requests.batchResult("2", "11158097383597290");

    Element inProcess =
        valid(
            BATCH_RESULT_XSD,
            SifenResponses.batchInProcess(),
            SimulatedSifen.Service.BATCH_RESULT.path(),
            query);
    assertThat(Xml.text(inProcess, "dCodResLot")).isEqualTo("0361");
    assertThat(Xml.text(inProcess, "dMsgResLot"))
        .isEqualTo("Lote {11158097383597290} en procesamiento");
    assertThat(Xml.descendants(inProcess, "gResProcLote")).isEmpty();

    Element notFound =
        valid(
            BATCH_RESULT_XSD,
            SifenResponses.batchNotFound(),
            SimulatedSifen.Service.BATCH_RESULT.path(),
            query);
    assertThat(Xml.text(notFound, "dCodResLot")).isEqualTo("0360");

    Element expired =
        valid(
            BATCH_RESULT_XSD,
            SifenResponses.batchExpired(),
            SimulatedSifen.Service.BATCH_RESULT.path(),
            query);
    assertThat(Xml.text(expired, "dCodResLot")).isEqualTo("0364");
  }

  @Test
  void defaultBatchResultKnowsTheBatchesItAccepted() throws Exception {
    Element accepted =
        valid(
            BATCH_XSD,
            SifenResponses.batchAccepted(),
            SimulatedSifen.Service.BATCH.path(),
            Requests.batch("1", Requests.CDC));
    String protocol = Xml.text(accepted, "dProtConsLote");

    Element known =
        valid(
            BATCH_RESULT_XSD,
            SifenResponses.batchResultFromState(),
            SimulatedSifen.Service.BATCH_RESULT.path(),
            Requests.batchResult("2", protocol));
    assertThat(Xml.text(known, "dCodResLot")).isEqualTo("0362");
    assertThat(Xml.text(known, "id")).isEqualTo(Requests.CDC);

    Element unknown =
        valid(
            BATCH_RESULT_XSD,
            SifenResponses.batchResultFromState(),
            SimulatedSifen.Service.BATCH_RESULT.path(),
            Requests.batchResult("3", "999"));
    assertThat(Xml.text(unknown, "dCodResLot")).isEqualTo("0360");
  }

  @Test
  void batchProcessedWithoutKnownCdcsNeedsPinnedOutcomes() throws Exception {
    Element pinned =
        valid(
            BATCH_RESULT_XSD,
            SifenResponses.batchProcessed(Outcome.approved().forId(Requests.CDC)),
            SimulatedSifen.Service.BATCH_RESULT.path(),
            Requests.batchResult("2", "42"));
    assertThat(Xml.text(pinned, "id")).isEqualTo(Requests.CDC);

    SimulatedResponse.Rendered unpinned =
        render(
            SifenResponses.batchProcessed(Outcome.approved()),
            SimulatedSifen.Service.BATCH_RESULT.path(),
            Requests.batchResult("2", "42"),
            true);
    assertThat(unpinned).isNull();
  }

  @Test
  void documentQueryFindsTheDocumentItApproved() throws Exception {
    valid(
        RECEPTION_XSD,
        SifenResponses.receptionApproved(),
        SimulatedSifen.Service.RECEPTION.path(),
        Requests.reception("1", Requests.CDC));

    Element found =
        valid(
            DOCUMENT_XSD,
            SifenResponses.documentFromState(),
            SimulatedSifen.Service.DOCUMENT_QUERY.path(),
            Requests.documentQuery("2", Requests.CDC));

    assertThat(Xml.text(found, "dCodRes")).isEqualTo("0422");
    String container = Xml.text(found, "xContenDE");
    assertThat(container).startsWith("<rContDe>").contains(Requests.CDC).contains("<dProtAut>");
    Document parsed = Xml.parse(container.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    assertThat(parsed).isNotNull();
    assertThat(Xml.descendant(parsed.getDocumentElement(), "DE").getAttribute("Id"))
        .isEqualTo(Requests.CDC);
    // xContenDE is text (xs:string): the container travels escaped (C-717).
    assertThat(rendered(found)).doesNotContain("<rContDe>");
  }

  @Test
  void documentQueryResults() throws Exception {
    byte[] query = Requests.documentQuery("2", Requests.CDC);

    Element notFound =
        valid(
            DOCUMENT_XSD,
            SifenResponses.documentFromState(),
            SimulatedSifen.Service.DOCUMENT_QUERY.path(),
            query);
    assertThat(Xml.text(notFound, "dCodRes")).isEqualTo("0420");
    assertThat(Xml.text(notFound, "xContenDE")).isNull();

    Element explicit =
        valid(
            DOCUMENT_XSD,
            SifenResponses.documentFound(),
            SimulatedSifen.Service.DOCUMENT_QUERY.path(),
            query);
    assertThat(Xml.text(explicit, "dCodRes")).isEqualTo("0422");
    assertThat(Xml.text(explicit, "xContenDE")).contains(Requests.CDC);

    Element given =
        valid(
            DOCUMENT_XSD,
            SifenResponses.documentFound(Requests.rde(Requests.CDC)),
            SimulatedSifen.Service.DOCUMENT_QUERY.path(),
            query);
    assertThat(Xml.text(given, "xContenDE")).contains(Requests.DIGEST);

    Element notPermitted =
        valid(
            DOCUMENT_XSD,
            SifenResponses.documentNotPermitted(),
            SimulatedSifen.Service.DOCUMENT_QUERY.path(),
            query);
    assertThat(Xml.text(notPermitted, "dCodRes")).isEqualTo("0421");
  }

  @Test
  void rucQueryResults() throws Exception {
    byte[] query = Requests.rucQuery("3", "44444401");

    Element found =
        valid(
            RUC_XSD,
            SifenResponses.rucFound("EMPRESA SINTETICA S.A."),
            SimulatedSifen.Service.RUC_QUERY.path(),
            query);
    assertThat(Xml.text(found, "dCodRes")).isEqualTo("0502");
    assertThat(Xml.text(found, "dRUCCons")).isEqualTo("44444401");
    assertThat(Xml.text(found, "dRazCons")).isEqualTo("EMPRESA SINTETICA S.A.");
    assertThat(Xml.text(found, "dCodEstCons")).isEqualTo("ACT");
    assertThat(Xml.text(found, "dRUCFactElec")).isEqualTo("S");
    // C-719: no rContRUC level.
    assertThat(Xml.descendant(found, "rContRUC")).isNull();

    Element inactive =
        valid(
            RUC_XSD,
            SifenResponses.rucFound("OTRA SINTETICA", "SUS", "SUSPENSION TEMPORAL", false),
            SimulatedSifen.Service.RUC_QUERY.path(),
            query);
    assertThat(Xml.text(inactive, "dRUCFactElec")).isEqualTo("N");

    Element notFound =
        valid(
            RUC_XSD, SifenResponses.rucNotFound(), SimulatedSifen.Service.RUC_QUERY.path(), query);
    assertThat(Xml.text(notFound, "dCodRes")).isEqualTo("0500");
    assertThat(Xml.text(notFound, "xContRUC")).isNull();

    Element notPermitted =
        valid(
            RUC_XSD,
            SifenResponses.rucNotPermitted(),
            SimulatedSifen.Service.RUC_QUERY.path(),
            query);
    assertThat(Xml.text(notPermitted, "dCodRes")).isEqualTo("0501");

    assertThatThrownBy(() -> SifenResponses.rucFound("X", "ACTIVO", "ACTIVO", true))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void eventResults() throws Exception {
    byte[] request = Requests.events("4", "15", "16");

    Element registered =
        valid(
            EVENTS_XSD,
            SifenResponses.eventRegistered(),
            SimulatedSifen.Service.EVENTS.path(),
            request);
    List<Element> results = Xml.descendants(registered, "gResProcEVe");
    assertThat(results).hasSize(2);
    assertThat(Xml.text(results.get(0), "id")).isEqualTo("15");
    assertThat(Xml.text(results.get(0), "dEstRes")).isEqualTo("Aprobado");
    assertThat(Xml.text(results.get(0), "dProtAut")).matches("[0-9]{10}");
    assertThat(Xml.text(results.get(0), "dCodRes")).isEqualTo("0600");
    assertThat(Xml.text(results.get(1), "id")).isEqualTo("16");

    Element rejected =
        valid(
            EVENTS_XSD,
            SifenResponses.eventRejected("4002", "CDC no existente en el SIFEN"),
            SimulatedSifen.Service.EVENTS.path(),
            request);
    assertThat(Xml.text(rejected, "dEstRes")).isEqualTo("Rechazado");
    assertThat(Xml.text(rejected, "dProtAut")).isNull();
    assertThat(Xml.text(rejected, "dCodRes")).isEqualTo("4002");

    Element mixed =
        valid(
            EVENTS_XSD,
            SifenResponses.events(
                Outcome.approvedWithObservation(
                    "4103",
                    "Fecha de emisión del DE/DTE ha superado el plazo para registro del evento"),
                Outcome.rejected("4003", "CDC ya se encuentra con el mismo evento solicitado")),
            SimulatedSifen.Service.EVENTS.path(),
            request);
    List<Element> mixedResults = Xml.descendants(mixed, "gResProcEVe");
    assertThat(Xml.text(mixedResults.get(0), "dEstRes")).isEqualTo("Aprobado con observación");
    assertThat(Xml.text(mixedResults.get(1), "dCodRes")).isEqualTo("4003");
  }

  @Test
  void soapFaultIsAValidSoap12Fault() {
    SimulatedResponse.Rendered rendered =
        render(
            SifenResponses.soapFault("Servicio no disponible"),
            SimulatedSifen.Service.RECEPTION.path(),
            Requests.reception("1", Requests.CDC),
            false);
    assertThat(rendered.httpStatus).isEqualTo(500);
    assertThat(rendered.contentType).startsWith("application/soap+xml");
    Document document = Xml.parse(rendered.body);
    assertThat(document).isNotNull();
    Element fault = Xml.soapPayload(document);
    assertThat(fault.getLocalName()).isEqualTo("Fault");
    assertThat(fault.getNamespaceURI()).isEqualTo(Xml.ENVELOPE_NS);
    assertThat(Xml.text(fault, "Text")).isEqualTo("Servicio no disponible");
  }

  @Test
  void htmlPageAndHttpError() {
    SimulatedResponse.Rendered html =
        render(
            SifenResponses.balancerHtmlPage(),
            SimulatedSifen.Service.RECEPTION.path(),
            Requests.reception("1", Requests.CDC),
            false);
    assertThat(html.httpStatus).isEqualTo(200);
    assertThat(html.contentType).startsWith("text/html");
    assertThat(html.bodyUtf8()).startsWith("<!DOCTYPE html>");

    SimulatedResponse.Rendered error =
        render(
            SifenResponses.httpError(503),
            SimulatedSifen.Service.RECEPTION.path(),
            new byte[0],
            false);
    assertThat(error.httpStatus).isEqualTo(503);
  }

  @Test
  void theSchemasDoRejectInvalidResponses() {
    // Guards the harness: without offset dFecProc breaks fecUTC, and "A" is shorter than tdEstRes.
    String invalid =
        "<ns2:rRetEnviEventoDe xmlns:ns2=\"http://ekuatia.set.gov.py/sifen/xsd\">"
            + "<ns2:dFecProc>2026-11-26T10:15:30</ns2:dFecProc><ns2:gResProcEVe>"
            + "<ns2:dEstRes>A</ns2:dEstRes><ns2:id>1</ns2:id><ns2:gResProc>"
            + "<ns2:dCodRes>0600</ns2:dCodRes><ns2:dMsgRes>ok</ns2:dMsgRes></ns2:gResProc>"
            + "</ns2:gResProcEVe></ns2:rRetEnviEventoDe>";
    assertThatThrownBy(() -> Schemas.validate(EVENTS_XSD, invalid))
        .isInstanceOf(org.xml.sax.SAXException.class);
  }

  @Test
  void outcomesCheckTheXsdLimits() {
    assertThatThrownBy(() -> Outcome.rejected("12", "x"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> Outcome.rejected("1001", ""))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> SifenResponses.events()).isInstanceOf(IllegalArgumentException.class);
  }

  // --- helpers ---------------------------------------------------------------------------------

  /** Renders the response, checks it is HTTP 200 SOAP, and validates its payload with the XSD. */
  private Element valid(String xsd, SimulatedResponse response, String path, byte[] request)
      throws Exception {
    SimulatedResponse.Rendered rendered = render(response, path, request, false);
    assertThat(rendered.httpStatus).isEqualTo(200);
    assertThat(rendered.contentType).startsWith("application/soap+xml");
    Document document = Xml.parse(rendered.body);
    assertThat(document).as("well-formed response").isNotNull();
    Element payload = Xml.soapPayload(document);
    assertThat(payload).as("SOAP payload").isNotNull();
    assertThat(payload.getNamespaceURI()).isEqualTo(Xml.SIFEN_NS);
    Schemas.validate(xsd, Xml.serialize(payload));
    payload.setUserData("rendered", rendered.bodyUtf8(), null);
    return payload;
  }

  private static String rendered(Element payload) {
    return (String) payload.getUserData("rendered");
  }

  private SimulatedResponse.Rendered render(
      SimulatedResponse response, String path, byte[] request, boolean expectFailure) {
    RecordedRequest recorded =
        new RecordedRequest(
            "POST",
            path,
            "application/soap+xml; charset=utf-8",
            request,
            List.of(),
            null,
            Instant.parse("2026-11-26T13:15:30Z"));
    try {
      SimulatedResponse.Rendered rendered = response.render(Interaction.of(recorded, state));
      assertThat(expectFailure).as("rendering should fail").isFalse();
      return rendered;
    } catch (IllegalStateException e) {
      assertThat(expectFailure).as("unexpected failure: " + e.getMessage()).isTrue();
      return null;
    }
  }
}
