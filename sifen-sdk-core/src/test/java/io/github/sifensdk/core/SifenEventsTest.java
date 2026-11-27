/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.sifensdk.core.internal.xml.SchemaValidator;
import io.github.sifensdk.core.model.Cancellation;
import io.github.sifensdk.core.model.Conformity;
import io.github.sifensdk.core.model.Disconformity;
import io.github.sifensdk.core.model.Event;
import io.github.sifensdk.core.model.EventDetail;
import io.github.sifensdk.core.model.Nomination;
import io.github.sifensdk.core.model.NonRecognition;
import io.github.sifensdk.core.model.NumberInvalidation;
import io.github.sifensdk.core.model.ReceiptNotification;
import io.github.sifensdk.core.model.TransportUpdate;
import io.github.sifensdk.core.signing.KeyMaterial;
import io.github.sifensdk.core.signing.KeyMaterials;
import io.github.sifensdk.core.signing.SignedEvent;
import io.github.sifensdk.core.validation.ValidationException;
import io.github.sifensdk.core.validation.ValidationResult;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class SifenEventsTest {

  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-11-26T13:05:00Z"), ZoneOffset.UTC);
  private static final LocalDateTime ISSUED = LocalDateTime.parse("2026-11-26T09:00:00");

  private final SifenEvents events =
      SifenEvents.builder().environment(Environment.TEST).clock(CLOCK).build();

  static String cdc() {
    return "01444444017001001001452822017012515873260988"; // MT150 example 10.1
  }

  /** One valid sample of each event a taxpayer can send (synthetic data). */
  static Stream<EventDetail> details() {
    return Stream.of(
        Cancellation.builder().cdc(cdc()).reason("Error en los datos del receptor").build(),
        NumberInvalidation.builder()
            .stampNumber("12345678")
            .establishment("001")
            .dispatchPoint("001")
            .rangeStartNumber("0000101")
            .rangeEndNumber("0000102")
            .documentTypeCode("1")
            .reason("Números salteados por error del sistema")
            .build(),
        ReceiptNotification.builder()
            .cdc(cdc())
            .issueDateTime(ISSUED)
            .receptionDateTime(ISSUED.plusHours(1))
            .receiverTypeCode("1")
            .receiverName("Receptor de prueba")
            .receiverRuc("44444401")
            .receiverCheckDigit("7")
            .guaraniTotal(new BigDecimal("110000"))
            .build(),
        Conformity.builder().cdc(cdc()).typeCode("1").build(),
        Disconformity.builder()
            .cdc(cdc())
            .reason("Los importes no coinciden con el pedido")
            .build(),
        NonRecognition.builder()
            .cdc(cdc())
            .issueDateTime(ISSUED)
            .receptionDateTime(ISSUED.plusHours(1))
            .receiverTypeCode("1")
            .receiverName("Receptor de prueba")
            .receiverRuc("44444401")
            .receiverCheckDigit("7")
            .reason("No reconocemos esta operación")
            .build(),
        Nomination.builder()
            .cdc(cdc())
            .reason("Identificación del receptor")
            .receiverNatureCode("1")
            .operationTypeCode("1")
            .receiverCountryCode("PRY")
            .receiverTaxpayerTypeCode("2")
            .receiverRuc("44444401")
            .receiverCheckDigit("7")
            .receiverName("Receptor de prueba")
            .build(),
        TransportUpdate.builder()
            .cdc(cdc())
            .reasonCode("1")
            .deliveryDepartmentCode("1")
            .deliveryCityCode("1")
            .deliveryAddress("Calle de prueba")
            .deliveryHouseNumber("0")
            .driverName("Chofer de prueba")
            .driverIdDocumentNumber("1234567")
            .transportModeCode("1")
            .build());
  }

  private static Event event(EventDetail detail) {
    return Event.builder().id("1").detail(detail).build();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("details")
  void eachEventValidatesSignsAndMatchesTheXsd(EventDetail detail) throws Exception {
    ValidationResult result = events.validate(event(detail));
    assertThat(result.errors()).as(detail + " is valid").isEmpty();

    SignedEvent signed = events.sign(event(detail), keyMaterial());
    String xml = new String(signed.xml(), StandardCharsets.UTF_8);
    assertThat(signed.id()).isEqualTo("1");
    assertThat(signed.signatureDateTime()).isEqualTo(LocalDateTime.parse("2026-11-26T10:05:00"));
    assertThat(xml)
        .startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?><rGesEve xmlns=")
        .contains("<rEve Id=\"1\"><dFecFirma>2026-11-26T10:05:00</dFecFirma><dVerFor>150")
        .contains("<Reference URI=\"#1\">")
        .doesNotContain("dTiGDE");
    assertThat(SchemaValidator.validateSignedEvent(signed.xml())).isEmpty();
  }

  @Test
  void theSDKWritesTheCatalogDescriptionsOfTheEvent() throws Exception {
    Nomination nomination =
        (Nomination) details().filter(d -> d instanceof Nomination).findFirst().orElseThrow();
    SignedEvent signed = events.sign(event(nomination), keyMaterial());
    assertThat(new String(signed.xml(), StandardCharsets.UTF_8))
        .contains("<cPaisRec>PRY</cPaisRec><dDesPaisRe>Paraguay</dDesPaisRe>");
  }

  @Test
  void anInvalidEventIsNotSigned() {
    Event tooShort =
        event(Cancellation.builder().cdc(cdc()).reason("No").build()); // mOtEve: 5 to 500
    assertThat(events.validate(tooShort).isValid()).isFalse();
    assertThatThrownBy(() -> events.sign(tooShort, keyMaterial()))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  void anEventNeedsAnIdAndADetail() {
    assertThatThrownBy(() -> Event.builder().id("1").build())
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() ->
            Event.builder().detail(Conformity.builder().cdc(cdc()).typeCode("1").build()).build())
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void signedEventsDoNotShowTheirContent() throws Exception {
    SignedEvent signed =
        events.sign(event(Conformity.builder().cdc(cdc()).typeCode("1").build()), keyMaterial());
    assertThat(signed.toString()).startsWith("SignedEvent[").doesNotContain(cdc());
  }

  static KeyMaterial keyMaterial() {
    try (InputStream in = SifenEventsTest.class.getResourceAsStream("/signing/prueba.p12")) {
      return KeyMaterials.pkcs12(in.readAllBytes(), "prueba-sintetica".toCharArray());
    } catch (java.io.IOException e) {
      throw new java.io.UncheckedIOException(e);
    }
  }
}
