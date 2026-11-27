/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Removes the data of a SOAP message so that it can be attached to a bug report: keeps the
 * structure, SIFEN's answer and the codes, and masks every other value keeping its shape.
 *
 * <ul>
 *   <li>Kept: element and attribute names, namespaces, the codes of SIFEN's answer ({@code
 *       dCodRes}, {@code dEstRes}, {@code dMsgRes} with long digit runs masked), the format version
 *       ({@code dVerFor}) and coded fields ({@code iTiDE}, {@code iAfecIVA} and the like, and
 *       {@code cMoneOpe}, {@code cUniMed}), which identify no one.
 *   <li>Masked: every other text and attribute value, digit by digit ({@code 9}) and letter by
 *       letter ({@code x}), so lengths and formats stay visible: RUCs, names, addresses, CDCs,
 *       amounts, dates, the QR and escaped contents ({@code xContenDE}).
 *   <li>Removed: signature values, digests and certificates.
 * </ul>
 *
 * A body that is not XML is masked as a whole.
 */
public final class Anonymizer {

  private static final Set<String> KEEP =
      Set.of("dCodRes", "dEstRes", "dVerFor", "cMoneOpe", "cUniMed", "dTotDE", "dCantDE");
  private static final Set<String> REMOVE =
      Set.of("SignatureValue", "DigestValue", "X509Certificate");
  private static final Set<String> KEEP_ATTRIBUTES = Set.of("schemaLocation", "Algorithm");
  private static final Pattern CODE = Pattern.compile("i[A-Z][A-Za-z0-9]*");
  private static final Pattern LONG_DIGITS = Pattern.compile("[0-9]{5,}");

  private Anonymizer() {}

  /**
   * Returns an anonymized copy of a message.
   *
   * @param message the bytes of a SOAP request or response
   * @return the anonymized bytes, UTF-8
   */
  public static byte[] anonymize(byte[] message) {
    if (message.length == 0) {
      return message;
    }
    Document document;
    try {
      document = Soap.parser().parse(new ByteArrayInputStream(message));
    } catch (SAXException | IOException | RuntimeException e) {
      return mask(new String(message, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
    }
    visit(document.getDocumentElement());
    return serialize(document);
  }

  private static void visit(Element element) {
    String name = element.getLocalName() != null ? element.getLocalName() : element.getTagName();
    NamedNodeMap attributes = element.getAttributes();
    for (int i = 0; i < attributes.getLength(); i++) {
      Attr a = (Attr) attributes.item(i);
      String attrName = a.getLocalName() != null ? a.getLocalName() : a.getName();
      if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(a.getNamespaceURI())
          || attrName.startsWith("xmlns")
          || KEEP_ATTRIBUTES.contains(attrName)) {
        continue;
      }
      a.setValue(mask(a.getValue()));
    }
    for (Node n = element.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (n instanceof Element) {
        visit((Element) n);
      } else if (n.getNodeType() == Node.TEXT_NODE || n.getNodeType() == Node.CDATA_SECTION_NODE) {
        n.setNodeValue(text(name, n.getNodeValue()));
      }
    }
  }

  private static String text(String element, String value) {
    if (value.isBlank()) {
      return value;
    }
    if (REMOVE.contains(element)) {
      return "[removed]";
    }
    if (KEEP.contains(element) || CODE.matcher(element).matches()) {
      return value;
    }
    if (element.equals("dMsgRes")) {
      return LONG_DIGITS.matcher(value).replaceAll(m -> "9".repeat(m.group().length()));
    }
    return mask(value);
  }

  /** Digits become 9 and letters x; everything else stays, so formats remain visible. */
  static String mask(String value) {
    StringBuilder sb = new StringBuilder(value.length());
    value
        .codePoints()
        .forEach(c -> {
          if (Character.isDigit(c)) {
            sb.append('9');
          } else if (Character.isLetter(c)) {
            sb.append('x');
          } else {
            sb.appendCodePoint(c);
          }
        });
    return sb.toString();
  }

  private static byte[] serialize(Document document) {
    try {
      TransformerFactory factory = TransformerFactory.newDefaultInstance();
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      Transformer transformer = factory.newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty(OutputKeys.INDENT, "no");
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      transformer.transform(new DOMSource(document), new StreamResult(out));
      return out.toByteArray();
    } catch (TransformerException e) {
      throw new IllegalStateException("Could not serialize the anonymized message", e);
    }
  }
}
