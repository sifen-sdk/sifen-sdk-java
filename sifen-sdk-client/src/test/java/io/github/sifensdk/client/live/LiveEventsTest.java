/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.live;

import io.github.sifensdk.client.EventResult;
import io.github.sifensdk.client.ReceptionResult;
import io.github.sifensdk.client.ResultMessage;
import io.github.sifensdk.client.SifenClient;
import io.github.sifensdk.core.Environment;
import io.github.sifensdk.core.SifenDocuments;
import io.github.sifensdk.core.SifenEvents;
import io.github.sifensdk.core.model.Cancellation;
import io.github.sifensdk.core.model.Conformity;
import io.github.sifensdk.core.model.Disconformity;
import io.github.sifensdk.core.model.ElectronicDocument;
import io.github.sifensdk.core.model.Event;
import io.github.sifensdk.core.model.EventDetail;
import io.github.sifensdk.core.model.GeneralData;
import io.github.sifensdk.core.model.Nomination;
import io.github.sifensdk.core.model.NonRecognition;
import io.github.sifensdk.core.model.NumberInvalidation;
import io.github.sifensdk.core.model.ReceiptNotification;
import io.github.sifensdk.core.model.Receiver;
import io.github.sifensdk.core.signing.Csc;
import io.github.sifensdk.core.signing.KeyMaterial;
import io.github.sifensdk.core.signing.KeyMaterials;
import io.github.sifensdk.core.signing.SignedDocument;
import io.github.sifensdk.core.validation.ValidationIssue;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Sends each event a taxpayer can send to SIFEN's test environment, on documents this test issues
 * first, and prints SIFEN's answer. Runs only with {@code SIFEN_TEST_EVENTS=true} and the
 * credentials of {@code LiveSifenTest}. The receiver of the documents is the issuer itself, so the
 * receiver's events are signed with the same certificate. Prints only states and codes.
 */
@EnabledIfEnvironmentVariable(named = "SIFEN_TEST_EVENTS", matches = "true")
class LiveEventsTest {

  private static final String TEST_NAME =
      "DE generado en ambiente de prueba - sin valor comercial ni fiscal";

  @Test
  void sendsEveryEvent() throws Exception {
    Properties p = new Properties();
    try (Reader r =
        Files.newBufferedReader(Path.of(env("SIFEN_TEST_ISSUER_FILE")), StandardCharsets.UTF_8)) {
      p.load(r);
    }
    KeyMaterial key =
        KeyMaterials.pkcs12(
            Path.of(env("SIFEN_TEST_P12_PATH")), env("SIFEN_TEST_P12_PASSWORD").toCharArray());
    Csc csc = Csc.of(env("SIFEN_TEST_CSC_ID"), env("SIFEN_TEST_CSC").toCharArray());
    SifenClient client =
        SifenClient.builder().environment(Environment.TEST).keyMaterial(key).build();
    SifenDocuments documents = SifenDocuments.builder().environment(Environment.TEST).build();
    SifenEvents events = SifenEvents.builder().environment(Environment.TEST).build();
    SecureRandom random = new SecureRandom();
    long number = (System.currentTimeMillis() / 1000L) % 8_000_000L + 1_000_000L;
    long eventId = System.currentTimeMillis() / 1000L % 9_000_000_000L + 1;

    // Documents approved now, one per event that needs its own document.
    String forReceipt = issue(client, documents, key, csc, invoice(p, number++, random), "recibo");
    String forDisconformity =
        issue(client, documents, key, csc, invoice(p, number++, random), "disconformidad");
    String forNonRecognition =
        issue(client, documents, key, csc, invoice(p, number++, random), "desconocimiento");
    String forCancellation =
        issue(client, documents, key, csc, invoice(p, number++, random), "cancelacion");
    String unnamed =
        issue(client, documents, key, csc, unnamedInvoice(p, number++, random), "innominada");
    LocalDateTime now = LocalDateTime.now(SifenDocuments.PARAGUAY).truncatedTo(ChronoUnit.SECONDS);

    send(
        client,
        events,
        key,
        eventId++,
        "notificacion de recepcion",
        ReceiptNotification.builder()
            .cdc(forReceipt)
            .issueDateTime(now.minusMinutes(5))
            .receptionDateTime(now)
            .receiverTypeCode("1")
            .receiverName(TEST_NAME)
            .receiverRuc(p.getProperty("ruc"))
            .receiverCheckDigit(p.getProperty("dv"))
            .operationTotalPyg(new BigDecimal("11000"))
            .build());
    send(
        client,
        events,
        key,
        eventId++,
        "conformidad total",
        Conformity.builder().cdc(forReceipt).typeCode("1").build());
    send(
        client,
        events,
        key,
        eventId++,
        "disconformidad",
        Disconformity.builder().cdc(forDisconformity).reason("Prueba del SIFEN SDK").build());
    send(
        client,
        events,
        key,
        eventId++,
        "desconocimiento",
        NonRecognition.builder()
            .cdc(forNonRecognition)
            .issueDateTime(now.minusMinutes(5))
            .receptionDateTime(now)
            .receiverTypeCode("1")
            .receiverName(TEST_NAME)
            .receiverRuc(p.getProperty("ruc"))
            .receiverCheckDigit(p.getProperty("dv"))
            .reason("Prueba del SIFEN SDK")
            .build());
    send(
        client,
        events,
        key,
        eventId++,
        "cancelacion",
        Cancellation.builder().cdc(forCancellation).reason("Prueba del SIFEN SDK").build());
    send(
        client,
        events,
        key,
        eventId++,
        "nominacion",
        Nomination.builder()
            .cdc(unnamed)
            .reason("Prueba del SIFEN SDK")
            .receiverNatureCode("1")
            .operationTypeCode("1")
            .receiverCountryCode("PRY")
            .receiverTaxpayerTypeCode(p.getProperty("tipoContribuyente"))
            .receiverRuc(p.getProperty("ruc"))
            .receiverCheckDigit(p.getProperty("dv"))
            .receiverName(TEST_NAME)
            .build());
    // Two numbers far from the ones the other tests use.
    long start = (number + 3_000_000L) % 9_000_000L + 500_000L;
    send(
        client,
        events,
        key,
        eventId++,
        "inutilizacion",
        NumberInvalidation.builder()
            .stampNumber(p.getProperty("timbrado"))
            .establishment(p.getProperty("establecimiento"))
            .dispatchPoint(p.getProperty("puntoExpedicion"))
            .rangeStartNumber(String.format("%07d", start))
            .rangeEndNumber(String.format("%07d", start + 1))
            .documentTypeCode("1")
            .reason("Prueba del SIFEN SDK")
            .build());
  }

  private static String issue(
      SifenClient client,
      SifenDocuments documents,
      KeyMaterial key,
      Csc csc,
      ElectronicDocument document,
      String label) {
    Set<String> errors =
        documents.validate(document).errors().stream()
            .map(ValidationIssue::ruleId)
            .collect(Collectors.toSet());
    if (!errors.isEmpty()) {
      System.out.println("EVENTO factura " + label + ": NO VALIDA " + errors);
    }
    SignedDocument signed = documents.sign(document, key, csc);
    ReceptionResult r = client.send(signed);
    System.out.println(
        "EVENTO factura "
            + label
            + ": "
            + r.document().status()
            + " "
            + codes(r.document().messages()));
    return signed.cdc();
  }

  private static void send(
      SifenClient client,
      SifenEvents events,
      KeyMaterial key,
      long id,
      String label,
      EventDetail detail)
      throws java.io.IOException {
    try {
      Event event = Event.builder().id(Long.toString(id)).detail(detail).build();
      EventResult result = client.sendEvents(List.of(events.sign(event, key)));
      String dump = System.getenv("SIFEN_TEST_DUMP_DIR");
      if (dump != null && !dump.isBlank()) {
        Files.write(Path.of(dump, label.replace(' ', '-') + ".xml"), result.exchange().request());
      }
      for (EventResult.Event e : result.events()) {
        System.out.println("EVENTO " + label + ": " + e.status() + " " + codes(e.messages()));
      }
    } catch (RuntimeException e) {
      System.out.println(
          "EVENTO "
              + label
              + ": NO ENVIADO "
              + e.getClass().getSimpleName()
              + " "
              + e.getMessage());
    }
  }

  private static String codes(List<ResultMessage> messages) {
    return messages.stream()
        .map(m -> m.code() + " " + m.message())
        .collect(Collectors.joining(" | "));
  }

  private static ElectronicDocument invoice(Properties p, long number, SecureRandom random) {
    return LiveSifenTest.invoice(p, number, random);
  }

  /** An invoice to an unnamed receiver (innominado), to be nominated afterwards. */
  private static ElectronicDocument unnamedInvoice(Properties p, long number, SecureRandom random) {
    ElectronicDocument base = invoice(p, number, random);
    GeneralData general = base.generalData();
    return base.toBuilder()
        .generalData(
            general.toBuilder()
                .receiver(
                    Receiver.builder()
                        .natureCode("2")
                        .operationTypeCode("2")
                        .countryCode("PRY")
                        .idTypeCode("5")
                        .idNumber("0")
                        .name("Sin Nombre")
                        .build())
                .build())
        .build();
  }

  private static String env(String name) {
    String value = System.getenv(name);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException("Missing environment variable " + name);
    }
    return value;
  }
}
