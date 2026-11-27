/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.sifensdk.client.BatchResult;
import io.github.sifensdk.client.BatchSubmission;
import io.github.sifensdk.client.DocumentResult;
import io.github.sifensdk.client.Exchange;
import io.github.sifensdk.client.ProtocolException;
import io.github.sifensdk.client.ResultMessage;
import io.github.sifensdk.client.Status;
import io.github.sifensdk.core.cdc.Cdc;
import io.github.sifensdk.core.signing.SignedDocument;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

class BatchesTest {

  private static final String REQUEST_ID = "123456789012345";
  private static final String RUC = "44444401";
  private static final String RUC_DV = "7";

  /** A synthetic signed rDE whose bytes a re-serialization would change (spaces, order, UTF-8). */
  static SignedDocument document(String type, String ruc, String dv, String number) {
    return document(type, ruc, dv, number, "");
  }

  static SignedDocument document(
      String type, String ruc, String dv, String number, String padding) {
    String cdc =
        Cdc.compose(
                type,
                ruc,
                dv,
                "001",
                "001",
                number,
                "2",
                LocalDate.of(2026, 11, 26),
                "1",
                "123456789")
            .value();
    String xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<rDE xmlns=\"http://ekuatia.set.gov.py/sifen/xsd\""
            + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
            + " xsi:schemaLocation=\"http://ekuatia.set.gov.py/sifen/xsd siRecepDE_v150.xsd\">"
            + "<dVerFor>150</dVerFor><DE Id=\""
            + cdc
            + "\"><dSinDatos b='2' a=\"1\">Peña &amp; Señal "
            + padding
            + "</dSinDatos></DE>"
            + "<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\"><SignatureValue>QUJD"
            + "</SignatureValue></Signature></rDE>";
    return SignedDocument.of(
        xml.getBytes(StandardCharsets.UTF_8),
        cdc,
        "https://ekuatia.set.gov.py/consultas-test/qr?nVersion=150",
        LocalDateTime.of(2026, 11, 26, 10, 0));
  }

  static Exchange exchange(byte[] response) {
    return new Exchange(
        REQUEST_ID,
        URI.create("https://sifen-test.set.gov.py/de/ws/async/recibe-lote.wsdl"),
        new byte[0],
        200,
        response,
        Instant.parse("2026-11-26T13:00:00Z"),
        Duration.ofMillis(100));
  }

  static byte[] resource(String path) {
    try (InputStream in = BatchesTest.class.getResourceAsStream("/respuestas/" + path)) {
      if (in == null) {
        throw new AssertionError("Missing test resource " + path);
      }
      return in.readAllBytes();
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  private static List<SignedDocument> invoices(int count) {
    List<SignedDocument> out = new ArrayList<>();
    for (int i = 1; i <= count; i++) {
      out.add(document("1", RUC, RUC_DV, String.valueOf(i)));
    }
    return out;
  }

  private static byte[] unzipSingleEntry(byte[] zip, String expectedName) throws IOException {
    try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
      ZipEntry entry = in.getNextEntry();
      assertThat(entry).isNotNull();
      assertThat(entry.getName()).isEqualTo(expectedName);
      byte[] content = in.readAllBytes();
      assertThat(in.getNextEntry()).isNull();
      return content;
    }
  }

  private static int indexOf(byte[] haystack, byte[] needle, int from) {
    outer:
    for (int i = from; i <= haystack.length - needle.length; i++) {
      for (int j = 0; j < needle.length; j++) {
        if (haystack[i + j] != needle[j]) {
          continue outer;
        }
      }
      return i;
    }
    return -1;
  }

  @Nested
  class SubmissionRequest {

    @Test
    void envelopeIsValidAgainstTheOfficialXsd() {
      byte[] envelope = Batches.submissionRequest(REQUEST_ID, invoices(3));

      BatchSchemas.validateBody(envelope, "WS_SiRecepLoteDE_v141.xsd");
      Element body = BatchSchemas.firstBodyElement(envelope);
      assertThat(body.getLocalName()).isEqualTo("rEnvioLote");
      assertThat(body.getNamespaceURI()).isEqualTo(Soap.SIFEN_NS);
      assertThat(Batches.childText(body, "dId")).isEqualTo(REQUEST_ID);
    }

    @Test
    void zipHoldsTheLotWithEachSignedDocumentByteForByte() throws IOException {
      List<SignedDocument> documents = invoices(3);
      Element body =
          BatchSchemas.firstBodyElement(Batches.submissionRequest(REQUEST_ID, documents));
      byte[] zip = Base64.getDecoder().decode(Batches.childText(body, "xDE"));

      byte[] lot = unzipSingleEntry(zip, BatchArchive.ENTRY_NAME);

      ByteArrayOutputStream expected = new ByteArrayOutputStream();
      expected.writeBytes(
          "<?xml version=\"1.0\" encoding=\"UTF-8\"?><rLoteDE>".getBytes(StandardCharsets.UTF_8));
      int from = 0;
      for (SignedDocument document : documents) {
        byte[] signed = Soap.withoutDeclaration(document.xml());
        int at = indexOf(lot, signed, from);
        assertThat(at).as("signed rDE present byte for byte, in order").isGreaterThan(from - 1);
        from = at + signed.length;
        expected.writeBytes(signed);
      }
      expected.writeBytes("</rLoteDE>".getBytes(StandardCharsets.UTF_8));
      assertThat(lot).isEqualTo(expected.toByteArray());

      String text = new String(lot, StandardCharsets.UTF_8);
      assertThat(text.indexOf("<?xml")).isZero();
      assertThat(text.indexOf("<?xml", 1)).as("a single declaration (MT150 7.2.1)").isEqualTo(-1);

      Element root = BatchSchemas.parse(lot).getDocumentElement();
      assertThat(root.getLocalName()).isEqualTo("rLoteDE");
      assertThat(root.getNamespaceURI()).isNull();
      List<Element> rdes = Soap.children(root, "rDE");
      assertThat(rdes).hasSize(3);
      assertThat(rdes).allSatisfy(e -> assertThat(e.getNamespaceURI()).isEqualTo(Soap.SIFEN_NS));
    }

    @Test
    void sameDocumentsGiveTheSameBytes() {
      List<SignedDocument> documents = invoices(2);
      assertThat(BatchArchive.zip(documents)).isEqualTo(BatchArchive.zip(documents));
    }

    @Test
    void acceptsFiftyDocuments() {
      byte[] envelope = Batches.submissionRequest(REQUEST_ID, invoices(50));
      BatchSchemas.validateBody(envelope, "WS_SiRecepLoteDE_v141.xsd");
    }

    @Test
    void rejectsAnEmptyBatch() {
      assertThatThrownBy(() -> Batches.submissionRequest(REQUEST_ID, List.of()))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("at least one");
    }

    @Test
    void rejectsFiftyOneDocuments() {
      assertThatThrownBy(() -> Batches.submissionRequest(REQUEST_ID, invoices(51)))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("50")
          .hasMessageContaining("51");
    }

    @Test
    void rejectsMixedDocumentTypes() {
      List<SignedDocument> documents = new ArrayList<>(invoices(2));
      documents.add(document("5", RUC, RUC_DV, "9"));

      assertThatThrownBy(() -> Batches.submissionRequest(REQUEST_ID, documents))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("same type")
          .hasMessageContaining("document 3");
    }

    @Test
    void rejectsMixedIssuersWithoutShowingTheRuc() {
      List<SignedDocument> documents = new ArrayList<>(invoices(1));
      // Same digits with another check digit: a different issuer for SIFEN, and not a real RUC.
      documents.add(document("1", RUC, "8", "2"));

      assertThatThrownBy(() -> Batches.submissionRequest(REQUEST_ID, documents))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("same issuer")
          .hasMessageNotContaining(RUC);
    }

    @Test
    void rejectsARepeatedCdc() {
      SignedDocument one = document("1", RUC, RUC_DV, "1");

      assertThatThrownBy(() -> Batches.submissionRequest(REQUEST_ID, List.of(one, one)))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("repeats the CDC")
          .hasMessageNotContaining(one.cdc());
    }

    @Test
    void rejectsAMessageOverOneThousandKilobytes() {
      Random random = new Random(42);
      List<SignedDocument> documents = new ArrayList<>();
      for (int i = 1; i <= 2; i++) {
        byte[] noise = new byte[450_000];
        random.nextBytes(noise);
        documents.add(
            document(
                "1", RUC, RUC_DV, String.valueOf(i), Base64.getEncoder().encodeToString(noise)));
      }

      assertThatThrownBy(() -> Batches.submissionRequest(REQUEST_ID, documents))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("1000 KB");
    }
  }

  @Nested
  class SubmissionResponse {

    @Test
    void acceptedAsInTheGuide() {
      byte[] response = resource("lote/aceptado.xml");
      BatchSchemas.validateBody(response, "WS_SiRecepLoteDE_v141.xsd");

      BatchSubmission submission = Batches.parseSubmission(exchange(response));

      assertThat(submission.accepted()).isTrue();
      assertThat(submission.protocolNumber()).isEqualTo("11158097383597290");
      assertThat(submission.processingTime()).as("as SIFEN sent it (C-710)").isEqualTo("0");
      assertThat(submission.messages())
          .containsExactly(new ResultMessage("0300", "Lote recibido con éxito"));
    }

    @Test
    void keepsAProtocolNumberLongerThanALong() {
      byte[] response = resource("lote/aceptado-protocolo-largo.xml");
      BatchSchemas.validateBody(response, "WS_SiRecepLoteDE_v141.xsd");

      BatchSubmission submission = Batches.parseSubmission(exchange(response));

      assertThat(submission.protocolNumber()).isEqualTo("9999999999999999999999999998");
      assertThat(submission.processingTime()).isEqualTo("2");
    }

    @Test
    void rejectedIsAResultWithoutProtocolNumber() {
      byte[] response = resource("lote/rechazado.xml");
      BatchSchemas.validateBody(response, "WS_SiRecepLoteDE_v141.xsd");

      BatchSubmission submission = Batches.parseSubmission(exchange(response));

      assertThat(submission.accepted()).isFalse();
      assertThat(submission.protocolNumber()).isNull();
      assertThat(submission.messages())
          .containsExactly(new ResultMessage("0301", "Lote no encolado para procesamiento"));
    }

    @Test
    void acceptedWithoutProtocolNumberIsAProtocolError() {
      byte[] response = resource("lote/aceptado-sin-protocolo.xml");
      BatchSchemas.validateBody(response, "WS_SiRecepLoteDE_v141.xsd");

      assertThatThrownBy(() -> Batches.parseSubmission(exchange(response)))
          .isInstanceOf(ProtocolException.class)
          .hasMessageContaining("dProtConsLote");
    }

    @Test
    void htmlOfTheLoadBalancerIsAProtocolError() {
      Exchange exchange = exchange(resource("lote/balanceador.html"));

      assertThatThrownBy(() -> Batches.parseSubmission(exchange))
          .isInstanceOf(ProtocolException.class)
          .hasMessageContaining("HTML")
          .satisfies(e -> assertThat(((ProtocolException) e).exchange()).isSameAs(exchange));
    }

    @Test
    void anotherServiceAnswerIsAProtocolError() {
      assertThatThrownBy(
              () -> Batches.parseSubmission(exchange(resource("consulta-lote/en-proceso.xml"))))
          .isInstanceOf(ProtocolException.class)
          .hasMessageContaining("rResEnviLoteDe");
    }
  }

  @Nested
  class ResultRequest {

    @Test
    void envelopeIsValidAgainstTheOfficialXsd() {
      byte[] envelope = Batches.resultRequest(REQUEST_ID, "11158097383597290");

      BatchSchemas.validateBody(envelope, "WS_SiConsLote_v141.xsd");
      Element body = BatchSchemas.firstBodyElement(envelope);
      assertThat(body.getLocalName()).isEqualTo("rEnviConsLoteDe");
      assertThat(Batches.childText(body, "dId")).isEqualTo(REQUEST_ID);
      assertThat(Batches.childText(body, "dProtConsLote")).isEqualTo("11158097383597290");
    }

    @Test
    void sendsTwentyEightDigitsUntouched() {
      String protocol = "9999999999999999999999999998";
      byte[] envelope = Batches.resultRequest(REQUEST_ID, protocol);

      BatchSchemas.validateBody(envelope, "WS_SiConsLote_v141.xsd");
      assertThat(new String(envelope, StandardCharsets.UTF_8)).contains(protocol);
    }

    @Test
    void rejectsSomethingThatIsNotAProtocolNumber() {
      for (String bad : new String[] {"", "12a", "1.5", " 12", "12345678901234567890123456789"}) {
        assertThatThrownBy(() -> Batches.resultRequest(REQUEST_ID, bad))
            .as(bad)
            .isInstanceOf(IllegalArgumentException.class);
      }
    }
  }

  @Nested
  class ResultResponse {

    @Test
    void inProcess() {
      byte[] response = resource("consulta-lote/en-proceso.xml");
      BatchSchemas.validateBody(response, "WS_SiConsLote_v141.xsd");

      BatchResult result = Batches.parseResult(exchange(response));

      assertThat(result.state()).isEqualTo(BatchResult.State.IN_PROCESS);
      assertThat(result.documents()).isEmpty();
      assertThat(result.messages())
          .containsExactly(new ResultMessage("0361", "Lote {11158097383597290} en procesamiento"));
    }

    @Test
    void processedWithApprovedObservedAndRejectedDocuments() {
      byte[] response = resource("consulta-lote/procesado.xml");
      BatchSchemas.validateBody(response, "WS_SiConsLote_v141.xsd");

      BatchResult result = Batches.parseResult(exchange(response));

      assertThat(result.state()).isEqualTo(BatchResult.State.PROCESSED);
      OffsetDateTime processedAt = OffsetDateTime.parse("2026-11-26T10:20:00-03:00");
      assertThat(result.documents())
          .containsExactly(
              DocumentResult.of(
                  "01444444017001001000000122026112611234567894",
                  Status.APPROVED,
                  "1234567890",
                  processedAt,
                  List.of(new ResultMessage("0260", "Autorización del DE satisfactoria"))),
              DocumentResult.of(
                  "01444444017001001000000222026112611234567894",
                  Status.APPROVED_WITH_OBSERVATION,
                  "1234567891",
                  processedAt,
                  List.of(
                      new ResultMessage("0260", "Autorización del DE satisfactoria"),
                      new ResultMessage("1001", "Observación sintética de prueba"))),
              DocumentResult.of(
                  "01444444017001001000000322026112611234567894",
                  Status.REJECTED,
                  null,
                  processedAt,
                  List.of(
                      new ResultMessage(
                          "0160",
                          "XML malformado: [El valor del elemento: dDirRec es invalido]"))));
    }

    @Test
    void notFound() {
      byte[] response = resource("consulta-lote/inexistente.xml");
      BatchSchemas.validateBody(response, "WS_SiConsLote_v141.xsd");

      BatchResult result = Batches.parseResult(exchange(response));

      assertThat(result.state()).isEqualTo(BatchResult.State.NOT_FOUND);
      assertThat(result.messages()).extracting(ResultMessage::code).containsExactly("0360");
    }

    @Test
    void expiredWindow() {
      byte[] response = resource("consulta-lote/extemporanea.xml");
      BatchSchemas.validateBody(response, "WS_SiConsLote_v141.xsd");

      BatchResult result = Batches.parseResult(exchange(response));

      assertThat(result.state()).isEqualTo(BatchResult.State.EXPIRED);
      assertThat(result.messages()).extracting(ResultMessage::code).containsExactly("0364");
    }

    @Test
    void aDocumentedRejectionIsAResultNotAnException() {
      byte[] response = resource("consulta-lote/sin-permiso.xml");
      BatchSchemas.validateBody(response, "WS_SiConsLote_v141.xsd");

      BatchResult result = Batches.parseResult(exchange(response));

      assertThat(result.state()).isEqualTo(BatchResult.State.REJECTED);
      assertThat(result.documents()).isEmpty();
      assertThat(result.messages()).extracting(ResultMessage::code).containsExactly("0340");
    }

    @Test
    void anUndocumentedDocumentStateIsAProtocolError() {
      byte[] response = resource("consulta-lote/estado-desconocido.xml");
      BatchSchemas.validateBody(response, "WS_SiConsLote_v141.xsd");

      assertThatThrownBy(() -> Batches.parseResult(exchange(response)))
          .isInstanceOf(ProtocolException.class)
          .hasMessageContaining("dEstRes");
    }

    @Test
    void htmlOfTheLoadBalancerIsAProtocolError() {
      assertThatThrownBy(() -> Batches.parseResult(exchange(resource("lote/balanceador.html"))))
          .isInstanceOf(ProtocolException.class)
          .hasMessageContaining("HTML");
    }
  }

  @Test
  void documentStatesIgnoreCaseAccentsAndSpaces() {
    Exchange exchange = exchange(new byte[0]);
    assertThat(BatchStatus.ofDocument(" APROBADO ", exchange)).isEqualTo(Status.APPROVED);
    assertThat(BatchStatus.ofDocument("Aprobado  con observacion", exchange))
        .isEqualTo(Status.APPROVED_WITH_OBSERVATION);
    assertThat(BatchStatus.ofDocument("rechazado", exchange)).isEqualTo(Status.REJECTED);
    assertThatThrownBy(() -> BatchStatus.ofDocument("A", exchange))
        .isInstanceOf(ProtocolException.class);
  }
}
