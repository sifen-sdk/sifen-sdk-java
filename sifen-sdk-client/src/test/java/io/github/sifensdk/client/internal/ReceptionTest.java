/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.sifensdk.client.DocumentResult;
import io.github.sifensdk.client.Exchange;
import io.github.sifensdk.client.ProtocolException;
import io.github.sifensdk.client.ReceptionResult;
import io.github.sifensdk.client.ResultMessage;
import io.github.sifensdk.client.Status;
import io.github.sifensdk.core.signing.SignedDocument;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

class ReceptionTest {

  /** CDC of the Manual's example (RUC 44444401-7). */
  static final String CDC = "01444444017001001001452822017012515873260988";

  /**
   * A synthetic signed rDE with details that a re-serialization would change: CRLF, single quotes,
   * an entity, an empty element, non-ASCII text and a namespaced signature.
   */
  static final String RDE =
      "<rDE xmlns=\"http://ekuatia.set.gov.py/sifen/xsd\">\r\n"
          + "<dVerFor>150</dVerFor><DE Id='"
          + CDC
          + "'><dRazRec>A &amp; B Peña  S.A.</dRazRec><gOpeCom/></DE>"
          + "<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\"><SignedInfo></SignedInfo>"
          + "</Signature><gCamFuFD><dCarQR>https://ekuatia.set.gov.py/consultas/qr?nVersion=150"
          + "&amp;Id="
          + CDC
          + "</dCarQR></gCamFuFD></rDE>";

  private static Schema schema;

  @BeforeAll
  static void compileSchema() throws Exception {
    schema = ReceptionSchemas.schema("WS_SiRecepDE_v150.xsd");
  }

  static SignedDocument signed() {
    byte[] xml =
        ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + RDE).getBytes(StandardCharsets.UTF_8);
    return SignedDocument.of(
        xml,
        CDC,
        "https://ekuatia.set.gov.py/consultas/qr?nVersion=150&Id=" + CDC,
        LocalDateTime.of(2025, 1, 15, 10, 0));
  }

  static byte[] resource(String path) throws IOException {
    try (InputStream in = ReceptionTest.class.getResourceAsStream("/respuestas/" + path)) {
      assertThat(in).as(path).isNotNull();
      return in.readAllBytes();
    }
  }

  static Exchange exchange(byte[] response) {
    return exchange(200, response);
  }

  static Exchange exchange(int status, byte[] response) {
    return new Exchange(
        "123456789012345",
        URI.create("https://sifen-test.set.gov.py" + Endpoints.RECEPTION),
        new byte[0],
        status,
        response,
        Instant.EPOCH,
        Duration.ZERO);
  }

  static int indexOf(byte[] haystack, byte[] needle) {
    outer:
    for (int i = 0; i + needle.length <= haystack.length; i++) {
      for (int j = 0; j < needle.length; j++) {
        if (haystack[i + j] != needle[j]) {
          continue outer;
        }
      }
      return i;
    }
    return -1;
  }

  private static DocumentResult parse(String file) throws IOException {
    return Reception.parse(exchange(resource("recepcion/" + file)), CDC).document();
  }

  // --- request ---

  @Test
  void requestBodyIsValidAgainstTheXsd() throws Exception {
    byte[] envelope = Reception.request("123456789012345", signed());

    ReceptionSchemas.validateBody(schema, envelope);
    Element body = ReceptionSchemas.body(envelope);
    assertThat(body.getLocalName()).isEqualTo("rEnviDe");
    assertThat(body.getNamespaceURI()).isEqualTo(Soap.SIFEN_NS);
  }

  @Test
  void signedDocumentIsEmbeddedByteForByteWithoutItsDeclaration() {
    byte[] envelope = Reception.request("1", signed());
    byte[] rde = RDE.getBytes(StandardCharsets.UTF_8);
    String text = new String(envelope, StandardCharsets.UTF_8);

    int at = indexOf(envelope, rde);
    assertThat(at).isPositive();
    byte[] before = Arrays.copyOfRange(envelope, 0, at);
    assertThat(new String(before, StandardCharsets.UTF_8))
        .endsWith("<rEnviDe xmlns=\"" + Soap.SIFEN_NS + "\"><dId>1</dId><xDE>");
    byte[] after = Arrays.copyOfRange(envelope, at + rde.length, envelope.length);
    assertThat(new String(after, StandardCharsets.UTF_8))
        .isEqualTo("</xDE></rEnviDe></env:Body></env:Envelope>");
    assertThat(text.indexOf("<?xml")).isZero();
    assertThat(text.lastIndexOf("<?xml")).isZero();
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "1234567890123456", "12a", "-1", " 1"})
  void requestIdMustBeOneToFifteenDigits(String id) {
    SignedDocument document = signed();
    assertThatThrownBy(() -> Reception.request(id, document))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("dId");
  }

  // --- example responses against the XSD ---

  @ParameterizedTest
  @ValueSource(
      strings = {
        "aprobado.xml",
        "aprobado-con-observacion.xml",
        "rechazado-varios-mensajes.xml",
        "otro-documento.xml",
        "estado-no-documentado.xml"
      })
  void exampleResponsesInTheXsdFormAreValid(String file) throws Exception {
    ReceptionSchemas.validateBody(schema, resource("recepcion/" + file));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "aprobado-forma-manual.xml",
        "rechazado-ejemplo-manual.xml",
        "sin-protocolo-0200.xml",
        "sin-protocolo-sin-codigo.xml"
      })
  void responsesInTheManualFormAreNotValidAgainstTheXsd(String file) throws Exception {
    Element body = ReceptionSchemas.body(resource("recepcion/" + file));
    assertThatThrownBy(() -> schema.newValidator().validate(new DOMSource(body)))
        .isInstanceOf(SAXException.class);
  }

  // --- parse ---

  @Test
  void approved() throws IOException {
    DocumentResult r = parse("aprobado.xml");

    assertThat(r.cdc()).isEqualTo(CDC);
    assertThat(r.status()).isEqualTo(Status.APPROVED);
    assertThat(r.authorizationProtocol()).isEqualTo("1234567890");
    assertThat(r.processedAt()).isEqualTo(OffsetDateTime.parse("2025-01-15T10:20:30-03:00"));
    assertThat(r.messages())
        .containsExactly(new ResultMessage("0260", "Autorización del DE satisfactoria"));
  }

  @Test
  void approvedWithObservation() throws IOException {
    DocumentResult r = parse("aprobado-con-observacion.xml");

    assertThat(r.status()).isEqualTo(Status.APPROVED_WITH_OBSERVATION);
    assertThat(r.authorizationProtocol()).isEqualTo("1234567891");
    assertThat(r.messages())
        .containsExactly(new ResultMessage("1005", "Transmisión extemporánea del DE"));
  }

  @Test
  void rejectedWithSeveralMessagesIsAResultInOrder() throws IOException {
    DocumentResult r = parse("rechazado-varios-mensajes.xml");

    assertThat(r.status()).isEqualTo(Status.REJECTED);
    assertThat(r.authorizationProtocol()).isNull();
    assertThat(r.messages())
        .extracting(ResultMessage::code)
        .containsExactly("1101", "1306", "1004");
    assertThat(r.messages().get(1).message())
        .isEqualTo("RUC del receptor inexistente en la base de datos de Marangatu");
  }

  @Test
  void manualFormIsAlsoRead() throws IOException {
    // C-702 (id), C-703 (no offset: Paraguay's time), C-704 (dEstRes/dProtAut inside gResProc).
    DocumentResult r = parse("aprobado-forma-manual.xml");

    assertThat(r.cdc()).isEqualTo(CDC);
    assertThat(r.status()).isEqualTo(Status.APPROVED);
    assertThat(r.authorizationProtocol()).isEqualTo("1234567890");
    assertThat(r.processedAt())
        .isEqualTo(
            LocalDateTime.of(2025, 1, 15, 10, 20, 30)
                .atZone(ZoneId.of("America/Asuncion"))
                .toOffsetDateTime());
    assertThat(r.messages()).extracting(ResultMessage::code).containsExactly("0260");
  }

  @Test
  void manualExampleWithZeroCdcIsTheDocumentSent() throws IOException {
    DocumentResult r = parse("rechazado-ejemplo-manual.xml");

    assertThat(r.cdc()).isEqualTo(CDC);
    assertThat(r.status()).isEqualTo(Status.REJECTED);
    assertThat(r.processedAt())
        .as("Paraguay used -04:00 in winter 2019")
        .isEqualTo(OffsetDateTime.parse("2019-06-03T12:00:00-04:00"));
    assertThat(r.messages()).containsExactly(new ResultMessage("0160", "XML malformado"));
  }

  @Test
  void serviceRejectionWithoutProtocolIsARejectedResult() throws IOException {
    DocumentResult r = parse("sin-protocolo-0200.xml");

    assertThat(r.cdc()).isEqualTo(CDC);
    assertThat(r.status()).isEqualTo(Status.REJECTED);
    assertThat(r.authorizationProtocol()).isNull();
    assertThat(r.processedAt()).isEqualTo(OffsetDateTime.parse("2025-01-15T10:20:30-03:00"));
    assertThat(r.messages()).extracting(ResultMessage::code).containsExactly("0200");
  }

  @Test
  void genericRejectionInsideGroupsWithoutProtocolIsARejectedResult() {
    String xml =
        "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\"><env:Body>"
            + "<rRetEnviDe xmlns=\"http://ekuatia.set.gov.py/sifen/xsd\"><gResProc>"
            + "<dCodRes>0160</dCodRes><dMsgRes>XML malformado</dMsgRes></gResProc>"
            + "</rRetEnviDe></env:Body></env:Envelope>";

    DocumentResult r =
        Reception.parse(exchange(xml.getBytes(StandardCharsets.UTF_8)), CDC).document();

    assertThat(r.status()).isEqualTo(Status.REJECTED);
    assertThat(r.processedAt()).isNull();
    assertThat(r.messages()).containsExactly(new ResultMessage("0160", "XML malformado"));
  }

  @Test
  void stateIsDerivedFromTheCodesOnlyWhenTheSpecificationClassifiesThem() {
    String approved =
        "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\"><env:Body>"
            + "<rRetEnviDe xmlns=\"http://ekuatia.set.gov.py/sifen/xsd\"><rProtDe>"
            + "<dFecProc>2025-01-15T10:20:30-03:00</dFecProc><gResProc><dCodRes>0260</dCodRes>"
            + "</gResProc></rProtDe></rRetEnviDe></env:Body></env:Envelope>";
    String unknown = approved.replace("0260", "1101");

    assertThat(
            Reception.parse(exchange(approved.getBytes(StandardCharsets.UTF_8)), CDC)
                .document()
                .status())
        .isEqualTo(Status.APPROVED);
    assertThatThrownBy(
            () -> Reception.parse(exchange(unknown.getBytes(StandardCharsets.UTF_8)), CDC))
        .isInstanceOf(ProtocolException.class)
        .hasMessageContaining("dEstRes");
  }

  @Test
  void resultKeepsTheExchange() throws IOException {
    Exchange exchange = exchange(resource("recepcion/aprobado.xml"));

    ReceptionResult result = Reception.parse(exchange, CDC);

    assertThat(result.exchange()).isSameAs(exchange);
  }

  @Test
  void withoutProtocolAndWithoutCodeIsAProtocolError() throws IOException {
    Exchange exchange = exchange(resource("recepcion/sin-protocolo-sin-codigo.xml"));

    assertThatThrownBy(() -> Reception.parse(exchange, CDC))
        .isInstanceOf(ProtocolException.class)
        .hasMessageContaining("rProtDe")
        .satisfies(e -> assertThat(((ProtocolException) e).exchange()).isSameAs(exchange));
  }

  @Test
  void answerForAnotherDocumentIsAProtocolError() throws IOException {
    Exchange exchange = exchange(resource("recepcion/otro-documento.xml"));

    assertThatThrownBy(() -> Reception.parse(exchange, CDC))
        .isInstanceOf(ProtocolException.class)
        .hasMessageContaining("another document")
        .hasMessageNotContaining(CDC);
  }

  @Test
  void undocumentedStateIsAProtocolError() throws IOException {
    Exchange exchange = exchange(resource("recepcion/estado-no-documentado.xml"));

    assertThatThrownBy(() -> Reception.parse(exchange, CDC))
        .isInstanceOf(ProtocolException.class)
        .hasMessageContaining("dEstRes");
  }

  @Test
  void loadBalancerHtmlPageIsAProtocolError() throws IOException {
    Exchange exchange = exchange(403, resource("recepcion/balanceador.html"));

    assertThatThrownBy(() -> Reception.parse(exchange, CDC))
        .isInstanceOf(ProtocolException.class)
        .hasMessageContaining("HTML")
        .hasMessageContaining("403");
  }

  @Test
  void soapFaultIsAProtocolError() throws IOException {
    Exchange exchange = exchange(500, resource("recepcion/soap-fault.xml"));

    assertThatThrownBy(() -> Reception.parse(exchange, CDC))
        .isInstanceOf(ProtocolException.class)
        .hasMessageContaining("SOAP fault");
  }

  @Test
  void unexpectedElementIsAProtocolError() throws IOException {
    Exchange exchange = exchange(resource("recepcion/elemento-inesperado.xml"));

    assertThatThrownBy(() -> Reception.parse(exchange, CDC))
        .isInstanceOf(ProtocolException.class)
        .hasMessageContaining("rRetEnviDe")
        .hasMessageContaining("rResEnviLoteDe");
  }

  @Test
  void processingDateFormats() {
    Exchange e = exchange(new byte[0]);
    ZoneId paraguay = ZoneId.of("America/Asuncion");

    assertThat(Reception.processedAt(null, e)).isNull();
    assertThat(Reception.processedAt("2025-01-15T10:20:30-03:00", e))
        .isEqualTo(OffsetDateTime.parse("2025-01-15T10:20:30-03:00"));
    assertThat(Reception.processedAt("2025-01-15-10:20:30", e))
        .isEqualTo(LocalDateTime.of(2025, 1, 15, 10, 20, 30).atZone(paraguay).toOffsetDateTime());
    assertThat(Reception.processedAt("2025-01-1510:20:30", e))
        .isEqualTo(LocalDateTime.of(2025, 1, 15, 10, 20, 30).atZone(paraguay).toOffsetDateTime());
    assertThatThrownBy(() -> Reception.processedAt("15/01/2025", e))
        .isInstanceOf(ProtocolException.class)
        .hasMessageContaining("dFecProc");
    assertThatThrownBy(() -> Reception.processedAt("2025-13-45T10:20:30", e))
        .isInstanceOf(ProtocolException.class)
        .hasMessageContaining("dFecProc");
  }
}
