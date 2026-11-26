/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.dsig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.sifensdk.core.Environment;
import io.github.sifensdk.core.SigningException;
import io.github.sifensdk.core.signing.Csc;
import io.github.sifensdk.core.signing.KeyMaterial;
import io.github.sifensdk.core.signing.KeyMaterials;
import io.github.sifensdk.core.signing.SignedDocument;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.xml.crypto.dsig.XMLSignature;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

class DocumentAssemblerTest {

  private static final String CDC = "01444444017001001001452822017012515873260988";
  private static final char[] PASSWORD = "prueba-sintetica".toCharArray();
  private static final Csc TEST_CSC =
      Csc.of("0001", "ABCD0000000000000000000000000000".toCharArray());

  private static byte[] resource(String name) throws IOException {
    try (InputStream in =
        Objects.requireNonNull(
            DocumentAssemblerTest.class.getResourceAsStream("/signing/" + name))) {
      return in.readAllBytes();
    }
  }

  private static KeyMaterial key() throws IOException {
    return KeyMaterials.pkcs12(resource("prueba.p12"), PASSWORD);
  }

  private static SignedDocument signSample() throws IOException {
    return DocumentAssembler.signAndFinish(
        resource("rde-sin-firmar.xml"), key(), TEST_CSC, Environment.TEST);
  }

  private static List<String> childNames(Element element) {
    List<String> names = new ArrayList<>();
    for (Node n = element.getFirstChild(); n != null; n = n.getNextSibling()) {
      names.add(n instanceof Element ? n.getLocalName() : "#text");
    }
    return names;
  }

  @Test
  void signsTheDeAndAddsTheQrAfterTheSignature() throws IOException {
    SignedDocument signed = signSample();
    byte[] xml = signed.xml();
    String text = new String(xml, StandardCharsets.UTF_8);

    assertThat(text).startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?><rDE ");
    assertThat(text).doesNotContain("\n").doesNotContain("\r").doesNotContain("&#13;");
    assertThat(text).contains("<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">");
    assertThat(text).doesNotContain("ds:").doesNotContain("X509SubjectName");

    Document document = DocumentAssembler.parse(xml);
    Element rde = document.getDocumentElement();
    assertThat(childNames(rde)).containsExactly("dVerFor", "DE", "Signature", "gCamFuFD");
    Element unsigned = (Element) rde.getLastChild();
    assertThat(unsigned.getNamespaceURI()).isEqualTo(DocumentAssembler.SIFEN_NAMESPACE);
    assertThat(childNames(unsigned)).containsExactly("dCarQR");
    assertThat(unsigned.getFirstChild().getTextContent()).isEqualTo(signed.qrUrl());

    Element signature = (Element) rde.getChildNodes().item(2);
    assertThat(signature.getNamespaceURI()).isEqualTo(XMLSignature.XMLNS);
    assertThat(signature.getPrefix()).isNull();
    assertThat(attribute(signature, "CanonicalizationMethod"))
        .isEqualTo("http://www.w3.org/2001/10/xml-exc-c14n#");
    assertThat(attribute(signature, "SignatureMethod"))
        .isEqualTo("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
    assertThat(signature.getElementsByTagNameNS(XMLSignature.XMLNS, "Transform").getLength())
        .isEqualTo(1);
    assertThat(attribute(signature, "Transform"))
        .isEqualTo("http://www.w3.org/2000/09/xmldsig#enveloped-signature");
    assertThat(attribute(signature, "DigestMethod"))
        .isEqualTo("http://www.w3.org/2001/04/xmlenc#sha256");
    Element reference =
        (Element) signature.getElementsByTagNameNS(XMLSignature.XMLNS, "Reference").item(0);
    assertThat(reference.getAttribute("URI")).isEqualTo("#" + CDC);
    assertThat(signature.getElementsByTagNameNS(XMLSignature.XMLNS, "X509Certificate").getLength())
        .isEqualTo(1);
    assertThat(signature.getElementsByTagNameNS(XMLSignature.XMLNS, "KeyValue").getLength())
        .isZero();

    String digest =
        signature
            .getElementsByTagNameNS(XMLSignature.XMLNS, "DigestValue")
            .item(0)
            .getTextContent();
    StringBuilder hex = new StringBuilder();
    for (byte b : digest.getBytes(StandardCharsets.US_ASCII)) {
      hex.append(String.format("%02x", b));
    }
    assertThat(signed.qrUrl())
        .startsWith(
            "https://ekuatia.set.gov.py/consultas-test/qr?nVersion=150&Id="
                + CDC
                + "&dFeEmiDE=323031372d30312d32355430393a33353a3137&dRucRec=88899990"
                + "&dTotGralOpe=300000&dTotIVA=27272&cItems=2&DigestValue="
                + hex
                + "&IdCSC=0001&cHashQR=");
    assertThat(text).contains("<dCarQR>" + signed.qrUrl().replace("&", "&amp;") + "</dCarQR>");

    assertThat(signed.cdc()).isEqualTo(CDC);
    assertThat(signed.signatureDateTime()).isEqualTo(LocalDateTime.of(2017, 1, 25, 9, 40));
    assertThat(signed.toString()).doesNotContain(CDC);
  }

  @Test
  void keepsTheContentOfTheDeUnchanged() throws IOException {
    String input = new String(resource("rde-sin-firmar.xml"), StandardCharsets.UTF_8);
    String de = input.substring(input.indexOf("<DE "), input.indexOf("</DE>") + 5);
    String output = new String(signSample().xml(), StandardCharsets.UTF_8);
    assertThat(output).contains(de);
  }

  @Test
  void theSignatureOfTheResultIsValidAndBreaksIfOneByteOfTheDeChanges() throws IOException {
    SignedDocument signed = signSample();
    KeyMaterial key = key();
    DocumentAssembler.verify(signed.xml(), key.certificate().getPublicKey());

    String text = new String(signed.xml(), StandardCharsets.UTF_8);
    String tampered = text.replace("<dTotGralOpe>300000<", "<dTotGralOpe>300001<");
    assertThat(tampered).isNotEqualTo(text);
    assertThatThrownBy(() ->
            DocumentAssembler.verify(
                tampered.getBytes(StandardCharsets.UTF_8), key.certificate().getPublicKey()))
        .isInstanceOf(SigningException.class)
        .hasMessageContaining("not valid");

    byte[] oneByte = signed.xml();
    int position = text.indexOf("<dNomRec>") + "<dNomRec>".length();
    oneByte[position] = (byte) (oneByte[position] + 1);
    assertThatThrownBy(() -> DocumentAssembler.verify(oneByte, key.certificate().getPublicKey()))
        .isInstanceOf(SigningException.class);
  }

  @Test
  void theUnsignedFieldsAreOutsideTheSignature() throws IOException {
    SignedDocument signed = signSample();
    String text = new String(signed.xml(), StandardCharsets.UTF_8);
    String changedQr = text.replace("<dCarQR>https://", "<dCarQR>http://");
    DocumentAssembler.verify(
        changedQr.getBytes(StandardCharsets.UTF_8), key().certificate().getPublicKey());
  }

  @Test
  void rejectsThePublishedTestCscInProduction() throws IOException {
    byte[] input = resource("rde-sin-firmar.xml");
    KeyMaterial key = key();
    assertThatThrownBy(
            () -> DocumentAssembler.signAndFinish(input, key, TEST_CSC, Environment.PRODUCTION))
        .isInstanceOf(SigningException.class)
        .hasMessageContaining("production")
        .hasMessageNotContaining("ABCD");
    Csc other = Csc.of("0002", "EFGH0000000000000000000000000000".toCharArray());
    assertThatThrownBy(
            () -> DocumentAssembler.signAndFinish(input, key, other, Environment.PRODUCTION))
        .isInstanceOf(SigningException.class);
    Csc synthetic = Csc.of("0001", "A1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6".toCharArray());
    SignedDocument signed =
        DocumentAssembler.signAndFinish(input, key, synthetic, Environment.PRODUCTION);
    assertThat(signed.qrUrl()).startsWith("https://ekuatia.set.gov.py/consultas/qr?");
  }

  @Test
  void rejectsDocumentsThatAreAlreadySignedOrHaveADtd() throws IOException {
    KeyMaterial key = key();
    byte[] signed = signSample().xml();
    assertThatThrownBy(
            () -> DocumentAssembler.signAndFinish(signed, key, TEST_CSC, Environment.TEST))
        .isInstanceOf(SigningException.class)
        .hasMessageContaining("already");

    String withDtd =
        "<?xml version=\"1.0\"?><!DOCTYPE rDE [<!ENTITY x SYSTEM \"file:///etc/passwd\">]>"
            + "<rDE xmlns=\"http://ekuatia.set.gov.py/sifen/xsd\">&x;</rDE>";
    assertThatThrownBy(() ->
            DocumentAssembler.signAndFinish(
                withDtd.getBytes(StandardCharsets.UTF_8), key, TEST_CSC, Environment.TEST))
        .isInstanceOf(SigningException.class)
        .hasMessageContaining("DTD");
  }

  @Test
  void rejectsAnInvalidCdc() throws IOException {
    String input = new String(resource("rde-sin-firmar.xml"), StandardCharsets.UTF_8);
    String wrong = input.replace(CDC, CDC.substring(0, 43) + "7");
    KeyMaterial key = key();
    assertThatThrownBy(() ->
            DocumentAssembler.signAndFinish(
                wrong.getBytes(StandardCharsets.UTF_8), key, TEST_CSC, Environment.TEST))
        .isInstanceOf(SigningException.class)
        .hasMessageContaining("CDC")
        .hasMessageNotContaining(CDC.substring(0, 43));
  }

  @Test
  void signsWithKeyMaterialFromPem() throws IOException {
    KeyMaterial pem =
        KeyMaterials.pem(
            new String(resource("prueba-certificado.pem"), StandardCharsets.US_ASCII),
            new String(resource("prueba-clave.pem"), StandardCharsets.US_ASCII));
    SignedDocument signed =
        DocumentAssembler.signAndFinish(
            resource("rde-sin-firmar.xml"), pem, TEST_CSC, Environment.TEST);
    DocumentAssembler.verify(signed.xml(), key().certificate().getPublicKey());
  }

  private static String attribute(Element signature, String localName) {
    return ((Element) signature.getElementsByTagNameNS(XMLSignature.XMLNS, localName).item(0))
        .getAttribute("Algorithm");
  }
}
