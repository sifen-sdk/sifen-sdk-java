/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.internal;

import static io.github.sifensdk.client.internal.ReceptionTest.CDC;
import static io.github.sifensdk.client.internal.ReceptionTest.exchange;
import static io.github.sifensdk.client.internal.ReceptionTest.resource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.sifensdk.client.DocumentQueryResult;
import io.github.sifensdk.client.EventType;
import io.github.sifensdk.client.Exchange;
import io.github.sifensdk.client.ProtocolException;
import io.github.sifensdk.client.QueriedEvent;
import io.github.sifensdk.client.ResultMessage;
import io.github.sifensdk.client.RucQueryResult;
import io.github.sifensdk.client.Status;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

class QueriesTest {

  private static final String RUC = "44444401";

  private static Schema documentSchema;
  private static Schema rucSchema;

  @BeforeAll
  static void compileSchemas() throws Exception {
    documentSchema = ReceptionSchemas.schema("WS_SiConsDE_v141.xsd");
    rucSchema = ReceptionSchemas.schema("WS_SiConsRUC_v141.xsd");
  }

  private static DocumentQueryResult document(String file) throws IOException {
    return Queries.parseDocument(exchange(resource("consulta-de/" + file)));
  }

  private static RucQueryResult ruc(String file) throws IOException {
    return Queries.parseRuc(exchange(resource("consulta-ruc/" + file)), RUC);
  }

  // --- siConsDE: request ---

  @Test
  void documentRequestIsValidAgainstTheXsd() throws Exception {
    byte[] envelope = Queries.documentRequest("123456789012345", CDC);

    ReceptionSchemas.validateBody(documentSchema, envelope);
    assertThat(new String(envelope, StandardCharsets.UTF_8))
        .contains(
            "<rEnviConsDeRequest xmlns=\""
                + Soap.SIFEN_NS
                + "\"><dId>123456789012345</dId><dCDC>"
                + CDC
                + "</dCDC></rEnviConsDeRequest>");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "",
        "shorter",
        "longer",
        "letter at the end",
        "letter outside the RUC",
        "letter E in the RUC"
      })
  void documentQueryRejectsAnInvalidCdcBeforeSending(String variant) {
    String cdc;
    switch (variant) {
      case "shorter":
        cdc = CDC.substring(1);
        break;
      case "longer":
        cdc = CDC + "0";
        break;
      case "letter at the end":
        cdc = CDC.substring(0, 43) + "X";
        break;
      case "letter outside the RUC":
        cdc = "A" + CDC.substring(1);
        break;
      case "letter E in the RUC":
        cdc = CDC.substring(0, 9) + "E" + CDC.substring(10);
        break;
      default:
        cdc = variant;
    }
    String value = cdc;
    assertThatThrownBy(() -> Queries.documentRequest("1", value))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("dCDC");
  }

  @Test
  void cdcWithALetterAtTheEndOfTheRucIsAccepted() throws Exception {
    // tCDC: [0-9]{2}([0-9]{7}[0-9A-D])[0-9]{34}
    String cdc = CDC.substring(0, 9) + "A" + CDC.substring(10);

    ReceptionSchemas.validateBody(documentSchema, Queries.documentRequest("1", cdc));
  }

  // --- siConsDE: responses ---

  @ParameterizedTest
  @ValueSource(
      strings = {
        "encontrado.xml",
        "no-encontrado-0420.xml",
        "sin-permiso-0421.xml",
        "codigo-no-documentado.xml"
      })
  void documentExampleResponsesAreValid(String file) throws Exception {
    ReceptionSchemas.validateBody(documentSchema, resource("consulta-de/" + file));
  }

  @Test
  void containerAsElementsIsNotValidAgainstTheXsd() throws Exception {
    Element body = ReceptionSchemas.body(resource("consulta-de/encontrado-contenedor-xml.xml"));
    assertThatThrownBy(() -> documentSchema.newValidator().validate(new DOMSource(body)))
        .isInstanceOf(SAXException.class);
  }

  @Test
  void documentFoundReturnsTheContainerTextAsReceived() throws IOException {
    DocumentQueryResult q = document("encontrado.xml");

    assertThat(q.found()).isTrue();
    assertThat(q.messages()).containsExactly(new ResultMessage("0422", "CDC encontrado"));
    assertThat(q.documentXml())
        .isEqualTo(
            "<rContDe xmlns=\"http://ekuatia.set.gov.py/sifen/xsd\"><rDE><dVerFor>150</dVerFor>"
                + "<DE Id=\""
                + CDC
                + "\"><dRazRec>A &amp; B S.A.</dRazRec></DE></rDE><dProtAut>1234567890"
                + "</dProtAut></rContDe>");
  }

  @Test
  void documentFoundAsElementsReturnsTheExactCharacters() throws IOException {
    DocumentQueryResult q = document("encontrado-contenedor-xml.xml");

    assertThat(q.found()).isTrue();
    assertThat(q.documentXml())
        .isEqualTo(
            "<rContDe xmlns=\"http://ekuatia.set.gov.py/sifen/xsd\"><rDE><DE Id=\""
                + CDC
                + "\"><dRazRec>A &amp; B S.A.</dRazRec></DE></rDE><dProtAut>1234567890"
                + "</dProtAut></rContDe>");
  }

  @Test
  void documentFoundWithEventsAsSifenReturnsIt() throws IOException {
    // Shape returned by SIFEN's test environment (evidence E-002): rDE, dProtAut and xContEv
    // without the rContDe root, as escaped text; one xContEv with one rContEv per event.
    DocumentQueryResult q = document("encontrado-con-eventos.xml");

    assertThat(q.found()).isTrue();
    assertThat(q.authorizationProtocol()).isEqualTo("1234567890");
    assertThat(q.events()).hasSize(3);
    QueriedEvent conformity = q.events().get(0);
    assertThat(conformity.id()).isEqualTo("1");
    assertThat(conformity.type()).isEqualTo(EventType.CONFORMITY);
    assertThat(conformity.signatureDateTime())
        .isEqualTo(LocalDateTime.parse("2026-11-27T10:00:01"));
    assertThat(conformity.values()).containsEntry("Id", CDC).containsEntry("iTipConf", "1");
    assertThat(conformity.status()).isEqualTo(Status.APPROVED);
    assertThat(conformity.authorizationProtocol()).isEqualTo("1111111111");
    assertThat(conformity.messages())
        .containsExactly(new ResultMessage("0600", "Evento registrado correctamente"));
    assertThat(conformity.xml())
        .startsWith("<rGesEve")
        .endsWith("</rGesEve>")
        .contains("rGeVeConf");
    assertThat(q.events().get(1).type()).isEqualTo(EventType.CANCELLATION);
    assertThat(q.events().get(1).values()).containsEntry("mOtEve", "Prueba");
    assertThat(q.events().get(1).xml()).contains("rGeVeCan").doesNotContain("rGeVeConf");
    // An event of the SET is read too.
    assertThat(q.events().get(2).type()).isEqualTo(EventType.ANALYSIS_OBJECT);
    assertThat(q.events().get(2).toString()).doesNotContain(CDC);
  }

  @Test
  void documentFoundInTheXsdFormHasItsTransactionNumberAndNoEvents() throws IOException {
    DocumentQueryResult q = document("encontrado.xml");
    assertThat(q.authorizationProtocol()).isEqualTo("1234567890");
    assertThat(q.events()).isEmpty();
  }

  @Test
  void documentNotFound() throws IOException {
    DocumentQueryResult q = document("no-encontrado-0420.xml");

    assertThat(q.found()).isFalse();
    assertThat(q.documentXml()).isNull();
    assertThat(q.messages())
        .containsExactly(
            new ResultMessage("0420", "Documento No Existe en SIFEN o ha sido Rechazado"));
  }

  @Test
  void documentWithoutPermissionIsNotFound() throws IOException {
    DocumentQueryResult q = document("sin-permiso-0421.xml");

    assertThat(q.found()).isFalse();
    assertThat(q.messages()).extracting(ResultMessage::code).containsExactly("0421");
  }

  @Test
  void documentQueryServiceRejectionIsNotFound() {
    String xml =
        "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\"><env:Body>"
            + "<rEnviConsDeResponse xmlns=\"http://ekuatia.set.gov.py/sifen/xsd\">"
            + "<dFecProc>2025-01-16T09:00:00-03:00</dFecProc><dCodRes>0160</dCodRes>"
            + "<dMsgRes>XML malformado</dMsgRes></rEnviConsDeResponse></env:Body></env:Envelope>";

    DocumentQueryResult q = Queries.parseDocument(exchange(xml.getBytes(StandardCharsets.UTF_8)));

    assertThat(q.found()).isFalse();
    assertThat(q.messages()).extracting(ResultMessage::code).containsExactly("0160");
  }

  @Test
  void documentUndocumentedCodeIsAProtocolError() throws IOException {
    Exchange exchange = exchange(resource("consulta-de/codigo-no-documentado.xml"));

    assertThatThrownBy(() -> Queries.parseDocument(exchange))
        .isInstanceOf(ProtocolException.class)
        .hasMessageContaining("0362");
  }

  @Test
  void documentHtmlFaultAndUnexpectedElementAreProtocolErrors() throws IOException {
    assertThatThrownBy(
            () -> Queries.parseDocument(exchange(403, resource("recepcion/balanceador.html"))))
        .isInstanceOf(ProtocolException.class)
        .hasMessageContaining("HTML");
    assertThatThrownBy(
            () -> Queries.parseDocument(exchange(500, resource("recepcion/soap-fault.xml"))))
        .isInstanceOf(ProtocolException.class)
        .hasMessageContaining("SOAP fault");
    assertThatThrownBy(
            () -> Queries.parseDocument(exchange(resource("consulta-ruc/habilitado.xml"))))
        .isInstanceOf(ProtocolException.class)
        .hasMessageContaining("rEnviConsDeResponse");
  }

  // --- siConsRUC: request ---

  @Test
  void rucRequestIsValidAgainstTheXsd() throws Exception {
    byte[] envelope = Queries.rucRequest("42", RUC);

    ReceptionSchemas.validateBody(rucSchema, envelope);
    assertThat(new String(envelope, StandardCharsets.UTF_8))
        .contains(
            "<rEnviConsRUC xmlns=\""
                + Soap.SIFEN_NS
                + "\"><dId>42</dId><dRUCCons>44444401</dRUCCons></rEnviConsRUC>");
  }

  @ParameterizedTest
  @ValueSource(strings = {"12345", "80000001", "1234567A", "8000000D"})
  void validRucsAreAcceptedAndValidAgainstTheXsd(String ruc) throws Exception {
    ReceptionSchemas.validateBody(rucSchema, Queries.rucRequest("1", ruc));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"", "1234", "123456789", "44444401-7", "04444440", "4444440E", "44 4444", "A4444"})
  void invalidRucIsRejectedBeforeSending(String ruc) {
    assertThatThrownBy(() -> Queries.rucRequest("1", ruc))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("dRUCCons")
        .hasMessageNotContaining(ruc.isEmpty() ? "\u0000" : ruc);
  }

  // --- siConsRUC: responses ---

  @ParameterizedTest
  @ValueSource(
      strings = {
        "habilitado.xml",
        "no-facturador.xml",
        "inexistente-0500.xml",
        "sin-permiso-0501.xml",
        "otro-ruc.xml"
      })
  void rucExampleResponsesAreValid(String file) throws Exception {
    ReceptionSchemas.validateBody(rucSchema, resource("consulta-ruc/" + file));
  }

  @Test
  void rucContainerInTheManualFormIsNotValidAgainstTheXsd() throws Exception {
    Element body = ReceptionSchemas.body(resource("consulta-ruc/habilitado-forma-manual.xml"));
    assertThatThrownBy(() -> rucSchema.newValidator().validate(new DOMSource(body)))
        .isInstanceOf(SAXException.class);
  }

  @Test
  void rucFoundAndElectronicIssuer() throws IOException {
    RucQueryResult q = ruc("habilitado.xml");

    assertThat(q.found()).isTrue();
    assertThat(q.ruc()).isEqualTo(RUC);
    assertThat(q.name()).isEqualTo("CONTRIBUYENTE DE PRUEBA S.A.");
    assertThat(q.statusCode()).isEqualTo("ACT");
    assertThat(q.statusDescription()).isEqualTo("ACTIVO");
    assertThat(q.electronicIssuer()).isTrue();
    assertThat(q.messages()).containsExactly(new ResultMessage("0502", "RUC encontrado"));
  }

  @Test
  void rucFoundNotElectronicIssuer() throws IOException {
    RucQueryResult q = ruc("no-facturador.xml");

    assertThat(q.found()).isTrue();
    assertThat(q.statusCode()).isEqualTo("SUS");
    assertThat(q.electronicIssuer()).isFalse();
  }

  @Test
  void rucContainerInTheManualFormIsAlsoRead() throws IOException {
    RucQueryResult q = ruc("habilitado-forma-manual.xml");

    assertThat(q.found()).isTrue();
    assertThat(q.name()).isEqualTo("CONTRIBUYENTE DE PRUEBA S.A.");
    assertThat(q.electronicIssuer()).isTrue();
  }

  @Test
  void rucNotFound() throws IOException {
    RucQueryResult q = ruc("inexistente-0500.xml");

    assertThat(q.found()).isFalse();
    assertThat(q.ruc()).isEqualTo(RUC);
    assertThat(q.name()).isNull();
    assertThat(q.statusCode()).isNull();
    assertThat(q.statusDescription()).isNull();
    assertThat(q.electronicIssuer()).isNull();
    assertThat(q.messages()).containsExactly(new ResultMessage("0500", "RUC no existe"));
  }

  @Test
  void rucWithoutPermissionIsNotFound() throws IOException {
    RucQueryResult q = ruc("sin-permiso-0501.xml");

    assertThat(q.found()).isFalse();
    assertThat(q.messages()).extracting(ResultMessage::code).containsExactly("0501");
  }

  @Test
  void rucAnswerForAnotherRucIsAProtocolError() throws IOException {
    Exchange exchange = exchange(resource("consulta-ruc/otro-ruc.xml"));

    assertThatThrownBy(() -> Queries.parseRuc(exchange, RUC))
        .isInstanceOf(ProtocolException.class)
        .hasMessageContaining("another RUC")
        .hasMessageNotContaining("80000001");
  }

  @Test
  void rucUndocumentedValuesAreProtocolErrors() throws IOException {
    String found = new String(resource("consulta-ruc/habilitado.xml"), StandardCharsets.UTF_8);
    byte[] badFlag =
        found
            .replace("<ns2:dRUCFactElec>S<", "<ns2:dRUCFactElec>X<")
            .getBytes(StandardCharsets.UTF_8);
    byte[] badCode = found.replace(">0502<", ">0422<").getBytes(StandardCharsets.UTF_8);

    assertThatThrownBy(() -> Queries.parseRuc(exchange(badFlag), RUC))
        .isInstanceOf(ProtocolException.class)
        .hasMessageContaining("dRUCFactElec");
    assertThatThrownBy(() -> Queries.parseRuc(exchange(badCode), RUC))
        .isInstanceOf(ProtocolException.class)
        .hasMessageContaining("0422");
  }

  @Test
  void rucHtmlFaultAndUnexpectedElementAreProtocolErrors() throws IOException {
    assertThatThrownBy(
            () -> Queries.parseRuc(exchange(403, resource("recepcion/balanceador.html")), RUC))
        .isInstanceOf(ProtocolException.class)
        .hasMessageContaining("HTML");
    assertThatThrownBy(
            () -> Queries.parseRuc(exchange(500, resource("recepcion/soap-fault.xml")), RUC))
        .isInstanceOf(ProtocolException.class)
        .hasMessageContaining("SOAP fault");
    assertThatThrownBy(
            () -> Queries.parseRuc(exchange(resource("consulta-de/encontrado.xml")), RUC))
        .isInstanceOf(ProtocolException.class)
        .hasMessageContaining("rResEnviConsRUC");
  }
}
