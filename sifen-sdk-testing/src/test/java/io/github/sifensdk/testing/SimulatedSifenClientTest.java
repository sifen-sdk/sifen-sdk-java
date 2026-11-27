/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.testing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.sifensdk.client.BatchResult;
import io.github.sifensdk.client.BatchSubmission;
import io.github.sifensdk.client.DocumentQueryResult;
import io.github.sifensdk.client.EventReceptionResult;
import io.github.sifensdk.client.ProtocolException;
import io.github.sifensdk.client.ReceptionResult;
import io.github.sifensdk.client.RucQueryResult;
import io.github.sifensdk.client.SifenClient;
import io.github.sifensdk.client.Status;
import io.github.sifensdk.client.TransportException;
import io.github.sifensdk.core.Environment;
import io.github.sifensdk.core.signing.SignedDocument;
import io.github.sifensdk.core.signing.SignedEvent;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** The real SDK client against the simulated SIFEN, over mutual TLS on the loopback interface. */
class SimulatedSifenClientTest {

  private SimulatedSifen sifen;
  private SifenClient client;

  @BeforeEach
  void start() {
    sifen = new SimulatedSifen().start();
    client = client(Duration.ofSeconds(30));
  }

  @AfterEach
  void stop() {
    sifen.close();
  }

  @Test
  void sendsADocumentAndFindsItAfterwards() {
    SignedDocument document = signed();

    ReceptionResult result = client.send(document);

    assertThat(result.document().cdc()).isEqualTo(Requests.CDC);
    assertThat(result.document().status()).isEqualTo(Status.APPROVED);
    assertThat(result.document().authorizationProtocol()).matches("[0-9]{10}");
    assertThat(result.document().processedAt()).isNotNull();
    RecordedRequest request = sifen.recordedRequests(SimulatedSifen.Service.RECEPTION).get(0);
    assertThat(request.bodyUtf8()).contains(new String(document.xml(), StandardCharsets.UTF_8));
    assertThat(request.requestId()).isEqualTo(result.exchange().requestId());
    assertThat(request.clientCertificate()).isEqualTo(SyntheticClient.certificate());

    DocumentQueryResult query = client.queryDocument(Requests.CDC);
    assertThat(query.found()).isTrue();
    assertThat(query.documentXml()).contains(Requests.CDC);
  }

  @Test
  void rejectionIsAResult() {
    sifen.enqueue(
        SimulatedSifen.Service.RECEPTION,
        SifenResponses.receptionRejected("1001", "CDC duplicado"));

    ReceptionResult result = client.send(signed());

    assertThat(result.document().status()).isEqualTo(Status.REJECTED);
    assertThat(result.document().messages()).extracting(m -> m.code()).containsExactly("1001");
    assertThat(client.queryDocument(Requests.CDC).found()).isFalse();
  }

  @Test
  void batchIsAcceptedAndProcessed() {
    sifen.enqueue(SimulatedSifen.Service.BATCH_RESULT, SifenResponses.batchInProcess());

    BatchSubmission submission = client.sendBatch(List.of(signed()));
    assertThat(submission.accepted()).isTrue();
    assertThat(submission.protocolNumber()).matches("[0-9]{17}");

    BatchResult inProcess = client.queryBatch(submission.protocolNumber());
    assertThat(inProcess.state()).isEqualTo(BatchResult.State.IN_PROCESS);

    BatchResult processed = client.queryBatch(submission.protocolNumber());
    assertThat(processed.state()).isEqualTo(BatchResult.State.PROCESSED);
    assertThat(processed.documents()).hasSize(1);
    assertThat(processed.documents().get(0).cdc()).isEqualTo(Requests.CDC);
    assertThat(processed.documents().get(0).status()).isEqualTo(Status.APPROVED);

    assertThat(client.queryBatch("999").state()).isEqualTo(BatchResult.State.NOT_FOUND);
  }

  @Test
  void queriesATaxpayer() {
    RucQueryResult found = client.queryRuc("44444401");
    assertThat(found.found()).isTrue();
    assertThat(found.name()).isEqualTo(SifenResponses.SYNTHETIC_NAME);
    assertThat(found.electronicIssuer()).isTrue();

    sifen.enqueue(SimulatedSifen.Service.RUC_QUERY, SifenResponses.rucNotFound());
    assertThat(client.queryRuc("44444401").found()).isFalse();
  }

  @Test
  void sendsEvents() {
    SignedEvent event =
        SignedEvent.of(
            ("<rGesEve xmlns=\"http://ekuatia.set.gov.py/sifen/xsd\"><rEve Id=\"7\">"
                    + "<dFecFirma>2026-11-26T10:00:00</dFecFirma><dVerFor>150</dVerFor></rEve>"
                    + "</rGesEve>")
                .getBytes(StandardCharsets.UTF_8),
            "7",
            LocalDateTime.parse("2026-11-26T10:00:00"));

    EventReceptionResult registered = client.sendEvents(List.of(event));
    assertThat(registered.events()).hasSize(1);
    assertThat(registered.events().get(0).id()).isEqualTo("7");
    assertThat(registered.events().get(0).status()).isEqualTo(Status.APPROVED);

    sifen.enqueue(
        SimulatedSifen.Service.EVENTS,
        SifenResponses.eventRejected("4002", "CDC no existente en el SIFEN"));
    EventReceptionResult rejected = client.sendEvents(List.of(event));
    assertThat(rejected.events().get(0).status()).isEqualTo(Status.REJECTED);
  }

  @Test
  void failuresAreExceptions() {
    sifen.enqueue(SimulatedSifen.Service.RECEPTION, SifenResponses.balancerHtmlPage());
    assertThatThrownBy(() -> client.send(signed()))
        .isInstanceOf(ProtocolException.class)
        .hasMessageContaining("HTML");

    sifen.enqueue(
        SimulatedSifen.Service.DOCUMENT_QUERY, SifenResponses.soapFault("Servicio no disponible"));
    assertThatThrownBy(() -> client.queryDocument(Requests.CDC))
        .isInstanceOf(ProtocolException.class)
        .hasMessageContaining("Servicio no disponible");

    sifen.enqueue(SimulatedSifen.Service.RUC_QUERY, SifenResponses.httpError(500));
    assertThatThrownBy(() -> client.queryRuc("44444401")).isInstanceOf(ProtocolException.class);
  }

  @Test
  void slowAnswerIsATransportExceptionWithUnknownOutcome() {
    SifenClient impatient = client(Duration.ofMillis(300));
    sifen.enqueue(
        SimulatedSifen.Service.RECEPTION,
        SifenResponses.receptionApproved().delayed(Duration.ofSeconds(30)));

    assertThatThrownBy(() -> impatient.send(signed())).isInstanceOf(TransportException.class);
  }

  private SifenClient client(Duration requestTimeout) {
    return SifenClient.builder()
        .environment(Environment.TEST)
        .keyMaterial(SyntheticClient.keyMaterial())
        .baseUri(sifen.baseUri())
        .trustStore(sifen.serverTrustStore())
        .requestTimeout(requestTimeout)
        .build();
  }

  private static SignedDocument signed() {
    return SignedDocument.of(
        Requests.rde(Requests.CDC).getBytes(StandardCharsets.UTF_8),
        Requests.CDC,
        "https://ekuatia.set.gov.py/consultas-test/qr?nVersion=150",
        LocalDateTime.of(2026, 11, 26, 10, 0));
  }
}
