/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.rules;

import io.github.sifensdk.core.internal.spec.Spec;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The element and attribute names declared in the bundled XSD of a tree (the electronic document or
 * the events).
 *
 * <p>Used to know which specification fields have no element in the current XSD (for example {@code
 * dFeFinT}, C-105, or {@code dTotCom}, C-505): structure follows the XSD, so their rules cannot be
 * enforced and are reported as unchecked instead.
 *
 * @param elements declared element names
 * @param attributes declared attribute names
 */
record XsdNames(Set<String> elements, Set<String> attributes) {

  private static final String XSD_NS = XMLConstants.W3C_XML_SCHEMA_NS_URI;

  static XsdNames bundled(Engine.Profile profile) {
    Set<String> elements = new HashSet<>();
    Set<String> attributes = new HashSet<>();
    for (String schema : profile.schemas()) {
      Document doc = parse(Spec.BASE + "xsd/oficial/" + schema);
      collect(doc.getElementsByTagNameNS(XSD_NS, "element"), elements);
      collect(doc.getElementsByTagNameNS(XSD_NS, "attribute"), attributes);
    }
    // The root may be declared as a complex type (rDE) rather than an element; it is always
    // present.
    elements.add(profile.rootElement());
    return new XsdNames(
        Collections.unmodifiableSet(elements), Collections.unmodifiableSet(attributes));
  }

  private static void collect(NodeList list, Set<String> out) {
    for (int i = 0; i < list.getLength(); i++) {
      String name = ((Element) list.item(i)).getAttribute("name");
      if (!name.isEmpty()) {
        out.add(name);
      }
    }
  }

  private static Document parse(String resource) {
    try (InputStream in = XsdNames.class.getResourceAsStream(resource)) {
      if (in == null) {
        throw new IllegalStateException("Missing bundled schema: " + resource);
      }
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setXIncludeAware(false);
      factory.setExpandEntityReferences(false);
      DocumentBuilder builder = factory.newDocumentBuilder();
      return builder.parse(in);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (ParserConfigurationException | SAXException e) {
      throw new IllegalStateException("Cannot read bundled schema: " + resource, e);
    }
  }
}
