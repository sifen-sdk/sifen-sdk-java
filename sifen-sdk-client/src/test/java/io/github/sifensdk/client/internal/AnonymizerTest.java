/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.sifensdk.client.Exchange;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class AnonymizerTest {

  private static final String CDC = "01444444017001001001452822017012515873260988";

  private static final String REQUEST =
      "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">"
          + "<env:Body><rEnviDe xmlns=\"http://ekuatia.set.gov.py/sifen/xsd\"><dId>123456789012345</dId>"
          + "<xDE><rDE><dVerFor>150</dVerFor><DE Id=\""
          + CDC
          + "\"><iTiDE>1</iTiDE><dRucEm>44444401</dRucEm><dNomEmi>Juana Pérez</dNomEmi>"
          + "<dEmailE>juana@example.com</dEmailE><cMoneOpe>PYG</cMoneOpe>"
          + "<dTotGralOpe>131000</dTotGralOpe></DE>"
          + "<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\"><SignedInfo>"
          + "<Reference URI=\"#"
          + CDC
          + "\"><DigestValue>q83vEjRWeJA=</DigestValue></Reference></SignedInfo>"
          + "<SignatureValue>AbCdEf123==</SignatureValue><KeyInfo><X509Data>"
          + "<X509Certificate>MIIBszCCAVmgAwIBAgIUQ</X509Certificate></X509Data></KeyInfo></Signature>"
          + "<gCamFuFD><dCarQR>https://ekuatia.set.gov.py/consultas-test/qr?Id="
          + CDC
          + "</dCarQR></gCamFuFD></rDE></xDE></rEnviDe></env:Body></env:Envelope>";

  private static final String RESPONSE =
      "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\"><env:Body>"
          + "<ns2:rRetEnviDe xmlns:ns2=\"http://ekuatia.set.gov.py/sifen/xsd\"><ns2:rProtDe>"
          + "<ns2:id>"
          + CDC
          + "</ns2:id><ns2:dEstRes>Rechazado</ns2:dEstRes><ns2:gResProc><ns2:dCodRes>1001</ns2:dCodRes>"
          + "<ns2:dMsgRes>TEST - RUC 44444401 no habilitado</ns2:dMsgRes></ns2:gResProc>"
          + "<ns2:xContenDE>&lt;rDE&gt;&lt;dNomRec&gt;Juana Pérez&lt;/dNomRec&gt;&lt;/rDE&gt;</ns2:xContenDE>"
          + "</ns2:rProtDe></ns2:rRetEnviDe></env:Body></env:Envelope>";

  @Test
  void keepsTheStructureAndTheAnswerAndMasksTheData() {
    Exchange exchange =
        new Exchange(
            "123456789012345",
            URI.create("https://sifen-test.set.gov.py/de/ws/sync/recibe.wsdl"),
            REQUEST.getBytes(StandardCharsets.UTF_8),
            200,
            RESPONSE.getBytes(StandardCharsets.UTF_8),
            Instant.parse("2026-11-27T13:00:00Z"),
            Duration.ofMillis(300));

    Exchange anonymized = exchange.anonymized();
    String request = new String(anonymized.request(), StandardCharsets.UTF_8);
    String response = new String(anonymized.response(), StandardCharsets.UTF_8);

    for (String secret :
        new String[] {
          CDC,
          "44444401",
          "Juana",
          "Pérez",
          "juana@example.com",
          "131000",
          "q83vEjRWeJA=",
          "AbCdEf123==",
          "MIIBszCCAVmgAwIBAgIUQ"
        }) {
      assertThat(request).doesNotContain(secret);
      assertThat(response).doesNotContain(secret);
    }
    assertThat(request)
        .contains("<dVerFor>150</dVerFor>", "<iTiDE>1</iTiDE>", "<cMoneOpe>PYG</cMoneOpe>")
        .contains("<dRucEm>99999999</dRucEm>", "<dNomEmi>xxxxx xxxxx</dNomEmi>")
        .contains("[removed]");
    assertThat(response)
        .contains(
            "<ns2:dCodRes>1001</ns2:dCodRes>", "Rechazado", "TEST - RUC 99999999 no habilitado");
    assertThat(anonymized.requestId()).isEqualTo(exchange.requestId());
    assertThat(exchange.request())
        .as("the original is not changed")
        .isEqualTo(REQUEST.getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void aBodyThatIsNotXmlIsMaskedAsAWhole() {
    byte[] html = "<html><body>Error RUC 44444401</body>".getBytes(StandardCharsets.UTF_8);

    assertThat(new String(Anonymizer.anonymize(html), StandardCharsets.UTF_8))
        .doesNotContain("44444401")
        .doesNotContain("Error");
  }
}
