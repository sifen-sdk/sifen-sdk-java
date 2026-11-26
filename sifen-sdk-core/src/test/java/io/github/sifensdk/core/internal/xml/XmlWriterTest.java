/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.sifensdk.core.internal.tree.Node;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class XmlWriterTest {

  private static String text(byte[] bytes) {
    return new String(bytes, StandardCharsets.UTF_8);
  }

  @Test
  void writesDeclarationDefaultNamespaceAndSchemaLocationOnRde() {
    String xml =
        text(XmlWriter.write(Node.group("rDE", null).add(Node.leaf("dVerFor", null, "150"))));

    assertThat(xml)
        .isEqualTo(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<rDE xmlns=\"http://ekuatia.set.gov.py/sifen/xsd\""
                + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                + " xsi:schemaLocation=\"http://ekuatia.set.gov.py/sifen/xsd siRecepDE_v150.xsd\">"
                + "<dVerFor>150</dVerFor></rDE>");
  }

  @Test
  void otherRootsOnlyDeclareTheDefaultNamespace() {
    String xml = text(XmlWriter.write(Node.group("rEve", null).attribute("Id", "1")));

    assertThat(xml)
        .isEqualTo(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<rEve xmlns=\"http://ekuatia.set.gov.py/sifen/xsd\" Id=\"1\"></rEve>");
  }

  @Test
  void isDeterministicAndHasNoWhitespaceBetweenElementsNorPrefixedElements() {
    byte[] first = XmlWriter.write(MinimalDocument.rde());
    byte[] second = XmlWriter.write(MinimalDocument.rde());

    assertThat(first).isEqualTo(second);
    String xml = text(first);
    assertThat(xml).doesNotContainPattern(">\\s+<").doesNotContain("\n").doesNotContain("\r");
    assertThat(xml).doesNotContainPattern("<[A-Za-z]+:").doesNotContainPattern("</[A-Za-z]+:");
    assertThat(xml).containsOnlyOnce("<?xml");
  }

  @Test
  void writesUtf8AndKeepsTextExactly() {
    byte[] bytes =
        XmlWriter.write(
            Node.group("rDE", null).add(Node.leaf("dDesTiDE", null, " Factura electrónica ")));

    assertThat(text(bytes)).contains("<dDesTiDE> Factura electrónica </dDesTiDE>");
    assertThat(bytes).containsSequence(0xC3, 0xB3); // "ó" in UTF-8
  }

  @Test
  void escapesTextAndAttributesSoTheyRoundTrip() throws Exception {
    String value = "A & B <c> \"d\" 'e' \t\n\r f";
    Node root =
        Node.group("rDE", null)
            .attribute("Id", value)
            .add(Node.leaf("dInfoEmi", null, value))
            .add(Node.leaf("dEmoji", null, "😀"));

    String xml = text(XmlWriter.write(root));

    assertThat(xml)
        .contains("Id=\"A &amp; B &lt;c&gt; &quot;d&quot; 'e' &#x9;&#xA;&#xD; f\"")
        .contains("<dInfoEmi>A &amp; B &lt;c&gt; \"d\" 'e' \t\n&#xD; f</dInfoEmi>")
        .contains("<dEmoji>😀</dEmoji>");
    Document parsed =
        SchemaValidator.newDocumentBuilder()
            .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    Element element = parsed.getDocumentElement();
    assertThat(element.getAttribute("Id")).isEqualTo(value);
    assertThat(element.getFirstChild().getTextContent()).isEqualTo(value);
  }

  @Test
  void rejectsCharactersXmlDoesNotAllowWithoutPrintingTheValue() {
    Node root = Node.group("rDE", null).add(Node.leaf("dNomEmi", null, "secret\u0001"));

    assertThatThrownBy(() -> XmlWriter.write(root))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("rDE/dNomEmi")
        .hasMessageNotContaining("secret");
    assertThatThrownBy(
            () -> XmlWriter.write(Node.group("rDE", null).add(Node.leaf("d", null, "\uD800x"))))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () -> XmlWriter.write(Node.group("rDE", null).add(Node.leaf("d", null, "￾"))))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejectsInvalidOrPrefixedNamesAndNamespaceAttributes() {
    assertThatThrownBy(() -> XmlWriter.write(Node.group("dEntCont ", null)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> XmlWriter.write(Node.group("ds:Signature", null)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> XmlWriter.write(Node.group("rDE", null).attribute("xmlns", "x")))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> XmlWriter.write(Node.group("rDE", null).attribute("xmlns:a", "x")))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> XmlWriter.write(Node.group("rDE", null).attribute("a b", "x")))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
