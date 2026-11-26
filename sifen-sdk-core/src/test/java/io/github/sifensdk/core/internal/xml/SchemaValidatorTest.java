/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.sifensdk.core.internal.tree.Node;
import io.github.sifensdk.core.validation.Severity;
import io.github.sifensdk.core.validation.ValidationIssue;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.Test;

class SchemaValidatorTest {

  private static String minimal() {
    return new String(XmlWriter.write(MinimalDocument.rde()), StandardCharsets.UTF_8);
  }

  private static List<ValidationIssue> validate(String xml) {
    return SchemaValidator.validateDocument(xml.getBytes(StandardCharsets.UTF_8));
  }

  private static String replace(String xml, String from, String to) {
    assertThat(xml).contains(from);
    return xml.replace(from, to);
  }

  private static byte[] resource(String name) throws IOException {
    try (InputStream in = SchemaValidatorTest.class.getResourceAsStream("/xml/" + name)) {
      assertThat(in).as(name).isNotNull();
      return in.readAllBytes();
    }
  }

  @Test
  void minimalHandBuiltDocumentIsValid() {
    assertThat(SchemaValidator.validateDocument(XmlWriter.write(MinimalDocument.rde()))).isEmpty();
  }

  @Test
  void dEntContWithoutTheTrailingSpaceOfTheXsdValidates() {
    // C-201: the XSD declares name="dEntCont " (NCName, whitespace collapse): the JDK trims it.
    String compPub =
        "<gCompPub><dModCont>AB</dModCont><dEntCont>12345</dEntCont><dAnoCont>25</dAnoCont>"
            + "<dSecCont>1234567</dSecCont><dFeCodCont>2025-01-01</dFeCodCont></gCompPub>";
    String xml = replace(minimal(), "</dDesIndPres>", "</dDesIndPres>" + compPub);

    assertThat(validate(xml)).isEmpty();
  }

  @Test
  void aBareDeElementIsAlsoAccepted() {
    Node de = MinimalDocument.de();
    assertThat(SchemaValidator.validateDocument(XmlWriter.write(de))).isEmpty();
  }

  @Test
  void missingRequiredElementIsReportedOnItsParent() {
    List<ValidationIssue> issues =
        validate(replace(minimal(), "<dFeIniT>2024-01-01</dFeIniT>", ""));

    assertThat(issues).hasSize(1);
    ValidationIssue issue = issues.get(0);
    assertThat(issue.ruleId()).isEqualTo("XSD");
    assertThat(issue.severity()).isEqualTo(Severity.ERROR);
    assertThat(issue.path()).isEqualTo("rDE/DE/gTimb");
    assertThat(issue.xmlName()).isEqualTo("gTimb");
    assertThat(issue.fieldId()).isEqualTo("C001");
    assertThat(issue.message()).contains("missing").endsWith("expected: dSerieNum, dFeIniT");
  }

  @Test
  void wrongOrderIsReportedOnTheUnexpectedElement() {
    String xml =
        replace(
            minimal(),
            "<dEst>001</dEst><dPunExp>001</dPunExp>",
            "<dPunExp>001</dPunExp><dEst>001</dEst>");

    List<ValidationIssue> issues = validate(xml);

    assertThat(issues).isNotEmpty();
    ValidationIssue issue = issues.get(0);
    assertThat(issue.path()).isEqualTo("rDE/DE/gTimb/dPunExp");
    assertThat(issue.xmlName()).isEqualTo("dPunExp");
    assertThat(issue.fieldId()).isEqualTo("C006");
    assertThat(issue.message()).contains("Unexpected element").contains("expected: dEst");
  }

  @Test
  void valueOutsideThePatternIsReportedOnceWithoutTheValue() {
    List<ValidationIssue> issues =
        validate(replace(minimal(), "<dNumTim>12345678</dNumTim>", "<dNumTim>X9Z7W</dNumTim>"));

    assertThat(issues).hasSize(1);
    ValidationIssue issue = issues.get(0);
    assertThat(issue.path()).isEqualTo("rDE/DE/gTimb/dNumTim");
    assertThat(issue.fieldId()).isEqualTo("C004");
    assertThat(issue.message()).doesNotContain("X9Z7W");
    assertThat(issue.toString()).doesNotContain("X9Z7W");
  }

  @Test
  void invalidAttributeIsReportedOnItsElement() {
    List<ValidationIssue> issues = validate(replace(minimal(), MinimalDocument.CDC, "not-a-cdc"));

    assertThat(issues).isNotEmpty();
    assertThat(issues.get(0).path()).isEqualTo("rDE/DE");
    assertThat(issues.get(0).fieldId()).isEqualTo("A001");
    assertThat(issues).allSatisfy(i -> assertThat(i.message()).doesNotContain("not-a-cdc"));
  }

  @Test
  void repeatedSiblingsGetTheirPositionInThePath() {
    String xml = minimal();
    int start = xml.indexOf("<gCamItem>");
    int end = xml.indexOf("</gCamItem>") + "</gCamItem>".length();
    String item = xml.substring(start, end);
    String badItem = item.replace("<cUniMed>77</cUniMed>", "<cUniMed>abc</cUniMed>");
    xml = xml.substring(0, end) + badItem + xml.substring(end);

    List<ValidationIssue> issues = validate(xml);

    assertThat(issues).isNotEmpty();
    assertThat(issues.get(0).path()).isEqualTo("rDE/DE/gDtipDE/gCamItem[2]/cUniMed");
    assertThat(issues.get(0).xmlName()).isEqualTo("cUniMed");
  }

  @Test
  void ambiguousXmlNamesAreResolvedWithTheParent() {
    // dNomRec is D211 under gDatRec, but also a field of several events.
    List<ValidationIssue> issues =
        validate(
            replace(minimal(), "<dNomRec>Receptor de prueba</dNomRec>", "<dNomRec></dNomRec>"));

    assertThat(issues).isNotEmpty();
    assertThat(issues.get(0).path()).isEqualTo("rDE/DE/gDatGralOpe/gDatRec/dNomRec");
    assertThat(issues.get(0).fieldId()).isEqualTo("D211");
  }

  @Test
  void elementsOutsideTheSpecHaveNoFieldId() {
    List<ValidationIssue> issues =
        validate(
            replace(
                minimal(),
                "<dSisFact>1</dSisFact>",
                "<dSisFact>1</dSisFact><dNoExiste>1</dNoExiste>"));

    assertThat(issues).isNotEmpty();
    assertThat(issues.get(0).xmlName()).isEqualTo("dNoExiste");
    assertThat(issues.get(0).fieldId()).isNull();
  }

  @Test
  void missingDeIsReported() {
    List<ValidationIssue> issues =
        validate(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<rDE xmlns=\"http://ekuatia.set.gov.py/sifen/xsd\"><dVerFor>150</dVerFor></rDE>");

    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).path()).isEqualTo("rDE");
  }

  @Test
  void elementsInAnotherNamespaceAreNotTheDe() {
    List<ValidationIssue> issues =
        validate(
            replace(minimal(), "xmlns=\"http://ekuatia.set.gov.py/sifen/xsd\"", "xmlns=\"urn:x\""));

    assertThat(issues).hasSize(1);
  }

  @Test
  void unsignedDocumentFailsTheSignedSchemaBecauseSignatureIsRequired() {
    List<ValidationIssue> issues =
        SchemaValidator.validateSigned(XmlWriter.write(MinimalDocument.rde()));

    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).path()).isEqualTo("rDE");
    assertThat(issues.get(0).fieldId()).isEqualTo("AA001");
    assertThat(issues.get(0).message()).contains("Signature");
  }

  @Test
  void schemaLocationHintsInTheDocumentAreIgnoredAndNeverFetched() {
    // Port 9 on localhost (discard) or an unresolvable host: a download attempt would fail or hang.
    String xml =
        replace(
            minimal(),
            "siRecepDE_v150.xsd\"",
            "siRecepDE_v150.xsd urn:otro http://127.0.0.1:9/otro.xsd\"");

    assertThat(validate(xml)).isEmpty();
  }

  @Test
  void schemaReferencesThatAreNotBundledFailWithoutNetwork() throws IOException {
    byte[] external = resource("externo.xsd");
    assertThatThrownBy(() ->
            SchemaValidator.compile(
                new StreamSource(new java.io.ByteArrayInputStream(external), "urn:test:externo")))
        .hasStackTraceContaining("not bundled");

    byte[] relative = resource("relativo-ausente.xsd");
    assertThatThrownBy(() ->
            SchemaValidator.compile(
                new StreamSource(
                    new java.io.ByteArrayInputStream(relative),
                    SchemaValidator.OFFICIAL_URL + "relativo-ausente.xsd")))
        .hasStackTraceContaining("not bundled");
  }

  @Test
  void resolverServesOfficialSchemasByAbsoluteUrlAndRelativeName() {
    String base = SchemaValidator.OFFICIAL_URL + "DE_v150.xsd";
    assertThat(SchemaValidator.BundledResolver.bundledName(base, null)).isEqualTo("DE_v150.xsd");
    assertThat(SchemaValidator.BundledResolver.bundledName("xmldsig-core-schema.xsd", base))
        .isEqualTo("xmldsig-core-schema.xsd");
    assertThat(
            SchemaValidator.BundledResolver.bundledName(
                "http://ekuatia.set.gov.py/sifen/xsd/DE_v150.xsd", null))
        .isNull();
    assertThat(SchemaValidator.BundledResolver.bundledName("../xsd/DE_v150.xsd", base))
        .isEqualTo("DE_v150.xsd");
    assertThat(SchemaValidator.BundledResolver.bundledName("../../etc/passwd", base)).isNull();
    assertThat(SchemaValidator.BundledResolver.bundledName("DE_v150.xsd", "urn:x")).isNull();
    assertThat(SchemaValidator.BundledResolver.bundledName("file:///tmp/DE_v150.xsd", base))
        .isNull();
    assertThat(SchemaValidator.BundledResolver.bundledName(null, base)).isNull();
  }

  @Test
  void doctypeWithExternalEntityIsRejected() throws IOException {
    for (String name : List.of("dtd-entidad-externa.xml", "dtd-entidad-interna.xml")) {
      byte[] xml = resource(name);

      List<ValidationIssue> unsigned = SchemaValidator.validateDocument(xml);
      List<ValidationIssue> signed = SchemaValidator.validateSigned(xml);

      assertThat(unsigned).as(name).hasSize(1);
      assertThat(unsigned.get(0).message()).contains("DOCTYPE");
      assertThat(signed).as(name).hasSize(1);
      assertThat(signed.get(0).message()).contains("DOCTYPE");
    }
  }

  @Test
  void notWellFormedXmlIsOneIssue() {
    List<ValidationIssue> issues = validate("<rDE><DE>");

    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).ruleId()).isEqualTo("XSD");
  }

  @Test
  void schemasAreCompiledOnceAndValidationIsThreadSafe() throws Exception {
    byte[] xml = XmlWriter.write(MinimalDocument.rde());
    List<Thread> threads = new java.util.ArrayList<>();
    java.util.concurrent.ConcurrentLinkedQueue<List<ValidationIssue>> results =
        new java.util.concurrent.ConcurrentLinkedQueue<>();
    for (int i = 0; i < 8; i++) {
      Thread t = new Thread(() -> results.add(SchemaValidator.validateDocument(xml)));
      threads.add(t);
      t.start();
    }
    for (Thread t : threads) {
      t.join();
    }
    assertThat(results).hasSize(8).allSatisfy(r -> assertThat(r).isEmpty());
  }
}
