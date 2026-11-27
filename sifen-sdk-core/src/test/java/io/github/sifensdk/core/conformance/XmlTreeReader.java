/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.conformance;

import io.github.sifensdk.core.internal.spec.Field;
import io.github.sifensdk.core.internal.spec.Spec;
import io.github.sifensdk.core.internal.tree.Node;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

/**
 * Reads an unsigned SIFEN XML (an {@code rDE} or an {@code rGesEve}) into the neutral tree that the
 * rule engine evaluates, for the conformance battery of the specification.
 *
 * <p>The SDK has no XML reader on purpose: documents are built from the model. The conformance
 * cases, however, are XML files shared by every SDK, so the tests need one. Each element gets the
 * field id of the specification field with that XML name under the parent's field ({@code campos/},
 * preferring a field in force over a future or removed one with the same name); attributes are kept
 * as attributes, as the mappers write them. Elements without a field (none in a valid document)
 * keep a null id.
 */
final class XmlTreeReader {

  /** Field id of each root element: the electronic document (AA001) and the events (GDE001). */
  private static final Map<String, String> ROOTS = Map.of("rDE", "AA001", "rGesEve", "GDE001");

  private static final Map<String, Field> BY_PARENT_AND_NAME = index();

  private XmlTreeReader() {}

  /**
   * Reads an XML document.
   *
   * @param xml UTF-8 bytes of an unsigned {@code rDE} or {@code rGesEve}
   * @return the root of the tree
   * @throws IllegalArgumentException if the XML cannot be read or its root is neither
   */
  static Node read(byte[] xml) {
    Element root;
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setExpandEntityReferences(false);
      root = factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml)).getDocumentElement();
    } catch (ParserConfigurationException | SAXException | IOException e) {
      throw new IllegalArgumentException("Cannot read the XML of a conformance case", e);
    }
    String rootId = ROOTS.get(root.getLocalName());
    if (rootId == null) {
      throw new IllegalArgumentException("Unexpected root element: " + root.getLocalName());
    }
    return convert(root, rootId);
  }

  private static Node convert(Element element, @Nullable String fieldId) {
    String name = element.getLocalName();
    boolean hasChildElements = false;
    for (org.w3c.dom.Node c = element.getFirstChild(); c != null; c = c.getNextSibling()) {
      if (c.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
        hasChildElements = true;
        break;
      }
    }
    Node node =
        hasChildElements
            ? Node.group(name, fieldId)
            : Node.leaf(name, fieldId, element.getTextContent());
    NamedNodeMap attributes = element.getAttributes();
    for (int i = 0; i < attributes.getLength(); i++) {
      Attr a = (Attr) attributes.item(i);
      if (!XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(a.getNamespaceURI())
          && !XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI.equals(a.getNamespaceURI())) {
        node.attribute(a.getLocalName() == null ? a.getName() : a.getLocalName(), a.getValue());
      }
    }
    if (hasChildElements) {
      for (org.w3c.dom.Node c = element.getFirstChild(); c != null; c = c.getNextSibling()) {
        if (c.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
          Element child = (Element) c;
          Field field =
              fieldId == null ? null : BY_PARENT_AND_NAME.get(fieldId + "/" + child.getLocalName());
          node.add(convert(child, field == null ? null : field.id()));
        }
      }
    }
    return node;
  }

  private static Map<String, Field> index() {
    Map<String, Field> out = new HashMap<>();
    for (Field f : Spec.bundled().fields().values()) {
      if (f.parent() == null) {
        continue;
      }
      String xml = f.xml().substring(f.xml().indexOf(':') + 1);
      String key = f.parent() + "/" + xml;
      Field current = out.get(key);
      if (current == null || ("vigente".equals(f.state()) && !"vigente".equals(current.state()))) {
        out.put(key, f);
      }
    }
    return Map.copyOf(out);
  }
}
