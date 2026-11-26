/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.sifensdk.client.EventResult;
import io.github.sifensdk.client.Exchange;
import io.github.sifensdk.client.ProtocolException;
import io.github.sifensdk.client.ResultMessage;
import io.github.sifensdk.client.Status;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

class EventsTest {

  private static final String REQUEST_ID = "123456789012345";
  private static final String CDC = "01444444017001001000000122026112611234567894";

  /**
   * A synthetic signed cancellation event: structurally valid against {@code Evento_v150.xsd}, with
   * a signature that is not cryptographically valid (the transport never checks it).
   */
  static byte[] signedEvent(String id) {
    String xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<rGesEve xmlns=\"http://ekuatia.set.gov.py/sifen/xsd\""
            + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
            + " xsi:schemaLocation=\"http://ekuatia.set.gov.py/sifen/xsd siRecepEvento_v150.xsd\">"
            + "<rEve Id=\""
            + id
            + "\"><dFecFirma>2026-11-26T10:00:00</dFecFirma><dVerFor>150</dVerFor>"
            + "<gGroupTiEvt><rGeVeCan><Id>"
            + CDC
            + "</Id><mOtEve>Cancelación sintética de prueba</mOtEve></rGeVeCan></gGroupTiEvt>"
            + "</rEve>"
            + "<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\"><SignedInfo>"
            + "<CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/>"
            + "<SignatureMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#rsa-sha256\"/>"
            + "<Reference URI=\"#"
            + id
            + "\"><Transforms>"
            + "<Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/>"
            + "<Transform Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/></Transforms>"
            + "<DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\"/>"
            + "<DigestValue>QUJD</DigestValue></Reference></SignedInfo>"
            + "<SignatureValue>QUJD</SignatureValue>"
            + "<KeyInfo><X509Data><X509Certificate>QUJD</X509Certificate></X509Data></KeyInfo>"
            + "</Signature></rGesEve>";
    return xml.getBytes(StandardCharsets.UTF_8);
  }

  private static Exchange exchange(String resource) {
    return BatchesTest.exchange(BatchesTest.resource(resource));
  }

  private static int indexOf(byte[] haystack, byte[] needle) {
    outer:
    for (int i = 0; i <= haystack.length - needle.length; i++) {
      for (int j = 0; j < needle.length; j++) {
        if (haystack[i + j] != needle[j]) {
          continue outer;
        }
      }
      return i;
    }
    return -1;
  }

  @Test
  void requestIsValidAgainstTheOfficialXsdAndCarriesEachEventByteForByte() {
    List<byte[]> events = List.of(signedEvent("1"), signedEvent("2"));

    byte[] envelope = Events.request(REQUEST_ID, events);

    BatchSchemas.validateBody(envelope, "WS_SiRecepEvento_v150.xsd");
    for (byte[] event : events) {
      assertThat(indexOf(envelope, Soap.withoutDeclaration(event))).isPositive();
    }
    String text = new String(envelope, StandardCharsets.UTF_8);
    assertThat(text.indexOf("<?xml", 1)).as("event declarations removed").isEqualTo(-1);
    Element body = BatchSchemas.firstBodyElement(envelope);
    assertThat(body.getLocalName()).isEqualTo("rEnviEventoDe");
    Element group = Soap.descendant(body, "gGroupGesEve");
    assertThat(group).isNotNull();
    assertThat(Soap.children(group, "rGesEve")).hasSize(2);
  }

  @Test
  void acceptsFifteenEvents() {
    List<byte[]> events = new ArrayList<>();
    for (int i = 1; i <= 15; i++) {
      events.add(signedEvent(String.valueOf(i)));
    }
    BatchSchemas.validateBody(Events.request(REQUEST_ID, events), "WS_SiRecepEvento_v150.xsd");
  }

  @Test
  void rejectsNoEventsAndSixteenEvents() {
    assertThatThrownBy(() -> Events.request(REQUEST_ID, List.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("1 to 15");
    List<byte[]> sixteen = Collections.nCopies(16, signedEvent("1"));
    assertThatThrownBy(() -> Events.request(REQUEST_ID, sixteen))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("16");
  }

  @Test
  void rejectsSomethingThatIsNotASignedRGesEve() {
    byte[] bareEvent = "<rEve Id=\"1\"></rEve>".getBytes(StandardCharsets.UTF_8);
    byte[] prefixed =
        "<ns:rGesEve xmlns:ns=\"http://ekuatia.set.gov.py/sifen/xsd\"/>"
            .getBytes(StandardCharsets.UTF_8);
    byte[] similarName = "<rGesEveX/>".getBytes(StandardCharsets.UTF_8);
    for (byte[] bad : List.of(bareEvent, prefixed, similarName, new byte[0])) {
      assertThatThrownBy(() -> Events.request(REQUEST_ID, List.of(signedEvent("1"), bad)))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Event 2");
    }
  }

  @Test
  void approvedAndRejectedEvents() {
    Exchange exchange = exchange("evento/mixto.xml");
    BatchSchemas.validateBody(exchange.response(), "WS_SiRecepEvento_v150.xsd");

    EventResult result = Events.parse(exchange);

    assertThat(result.events())
        .containsExactly(
            new EventResult.Event(
                "1",
                Status.APPROVED,
                "1234567892",
                List.of(new ResultMessage("0600", "Evento registrado correctamente"))),
            new EventResult.Event(
                "2",
                Status.REJECTED,
                "",
                List.of(
                    new ResultMessage(
                        "4003", "CDC ya se encuentra con el mismo evento solicitado"))));
    assertThat(result.exchange()).isSameAs(exchange);
  }

  @Test
  void shortStatesOfEvents() {
    // A, AO and R are shorter than the 8 characters tdEstRes requires, so this answer is not
    // valid against protProcesEventos_v141.xsd; it is read anyway.
    Exchange exchange = exchange("evento/abreviado.xml");
    assertThatThrownBy(
            () -> BatchSchemas.validateBody(exchange.response(), "WS_SiRecepEvento_v150.xsd"))
        .isInstanceOf(AssertionError.class);

    EventResult result = Events.parse(exchange);

    assertThat(result.events())
        .extracting(EventResult.Event::status)
        .containsExactly(Status.APPROVED, Status.APPROVED_WITH_OBSERVATION, Status.REJECTED);
    assertThat(result.events())
        .extracting(EventResult.Event::authorizationProtocol)
        .containsExactly("1234567893", "1234567894", "");
  }

  @Test
  void htmlOfTheLoadBalancerIsAProtocolError() {
    assertThatThrownBy(() -> Events.parse(exchange("lote/balanceador.html")))
        .isInstanceOf(ProtocolException.class)
        .hasMessageContaining("HTML");
  }

  @Test
  void soapFaultIsAProtocolError() {
    assertThatThrownBy(() -> Events.parse(exchange("evento/fault.xml")))
        .isInstanceOf(ProtocolException.class)
        .hasMessageContaining("SOAP fault");
  }
}
