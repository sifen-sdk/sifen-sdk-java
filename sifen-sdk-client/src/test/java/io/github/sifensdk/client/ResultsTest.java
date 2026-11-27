/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** The results' {@code toString} shows states, codes and counts only, and equality by value. */
class ResultsTest {

  // The synthetic RUC and CDC of the Manual Técnico's example; they identify nobody.
  private static final String CDC = "01444444017001001001452822017012515873260988";
  private static final String RUC = "44444401";
  private static final String NAME = "CONTRIBUYENTE SINTETICO DE PRUEBA";
  private static final String TEXT = "RUC 44444401 del receptor CONTRIBUYENTE SINTETICO no existe";
  private static final String XML =
      "<rDE><dRucRec>44444401</dRucRec><dNomRec>" + NAME + "</dNomRec>";
  private static final ResultMessage MESSAGE = new ResultMessage("0160", TEXT);
  private static final Exchange EXCHANGE =
      new Exchange(
          "1",
          URI.create("https://sifen-test.set.gov.py/de/ws/sync/recibe.wsdl"),
          XML.getBytes(StandardCharsets.UTF_8),
          200,
          XML.getBytes(StandardCharsets.UTF_8),
          Instant.EPOCH,
          Duration.ZERO);

  private static void assertSafe(Object result) {
    assertThat(result.toString())
        .doesNotContain(CDC)
        .doesNotContain(RUC)
        .doesNotContain("CONTRIBUYENTE")
        .doesNotContain("no existe")
        .doesNotContain("<");
  }

  private static DocumentResult document() {
    return DocumentResult.of(
        CDC,
        Status.REJECTED,
        null,
        OffsetDateTime.parse("2026-11-27T10:00:00-03:00"),
        List.of(MESSAGE));
  }

  @Test
  void documentResults() {
    DocumentResult document = document();
    assertThat(document).hasToString("DocumentResult[status=REJECTED, codes=[0160]]");
    assertSafe(document);
    assertThat(document).isEqualTo(document()).hasSameHashCodeAs(document());
    assertThat(document)
        .isNotEqualTo(DocumentResult.of(CDC, Status.APPROVED, "1", null, List.of()));

    ReceptionResult reception = new ReceptionResult(document, EXCHANGE);
    assertSafe(reception);

    BatchResult batch =
        BatchResult.of(
            BatchResult.State.PROCESSED,
            List.of(document, document),
            List.of(new ResultMessage("0362", TEXT)),
            EXCHANGE);
    assertThat(batch).hasToString("BatchResult[state=PROCESSED, codes=[0362], documents=2]");
    assertSafe(batch);

    BatchSubmission submission =
        BatchSubmission.of(true, "123", "2", List.of(new ResultMessage("0300", TEXT)), EXCHANGE);
    assertThat(submission).hasToString("BatchSubmission[accepted=true, codes=[0300]]");
    assertSafe(submission);
    assertThat(submission.processingTime()).isEqualTo("2");
  }

  @Test
  void queryResults() {
    QueriedEvent event =
        QueriedEvent.of(
            "1",
            EventType.CANCELLATION,
            "rGeVeCan",
            LocalDateTime.parse("2026-11-27T10:00:00"),
            Map.of("Id", CDC, "mOtEve", NAME),
            XML,
            Status.APPROVED,
            null,
            List.of(new ResultMessage("0600", TEXT)));
    assertThat(event).hasToString("QueriedEvent[type=CANCELLATION, status=APPROVED, codes=[0600]]");
    assertSafe(event);
    assertThat(event.authorizationProtocol()).isNull();

    DocumentQueryResult document =
        DocumentQueryResult.of(
            true, XML, "1234567890", List.of(event, event), List.of(MESSAGE), EXCHANGE);
    assertThat(document).hasToString("DocumentQueryResult[found=true, codes=[0160], events=2]");
    assertSafe(document);

    RucQueryResult ruc =
        RucQueryResult.of(true, RUC, NAME, "ACT", "ACTIVO", true, List.of(MESSAGE), EXCHANGE);
    assertThat(ruc).hasToString("RucQueryResult[found=true, codes=[0160]]");
    assertSafe(ruc);
    assertThat(ruc)
        .isEqualTo(
            RucQueryResult.of(true, RUC, NAME, "ACT", "ACTIVO", true, List.of(MESSAGE), EXCHANGE));
  }

  @Test
  void eventResults() {
    EventResult event = EventResult.of("1", Status.REJECTED, null, List.of(MESSAGE));
    assertThat(event).hasToString("EventResult[status=REJECTED, codes=[0160]]");
    assertSafe(event);
    assertThat(event.authorizationProtocol()).isNull();

    EventReceptionResult reception = EventReceptionResult.of(List.of(event, event), EXCHANGE);
    assertThat(reception).hasToString("EventReceptionResult[events=2]");
    assertSafe(reception);
    assertThat(reception).isEqualTo(EventReceptionResult.of(List.of(event, event), EXCHANGE));
  }

  @Test
  void messagesShowTheCodeOnly() {
    assertThat(MESSAGE).hasToString("ResultMessage[code=0160]");
    assertSafe(MESSAGE);
  }
}
