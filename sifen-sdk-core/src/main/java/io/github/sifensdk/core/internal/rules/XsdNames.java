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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
 * @param elementAttributes attributes of each element, as {@code element@attribute} (for example
 *     {@code rEve@Id}): an attribute whose name is also an element name (the {@code Id} of {@code
 *     rEve} and the {@code Id} element of each event detail) is told apart by its element
 */
record XsdNames(Set<String> elements, Set<String> attributes, Set<String> elementAttributes) {

  XsdNames(Set<String> elements, Set<String> attributes) {
    this(elements, attributes, Set.of());
  }

  private static final String XSD_NS = XMLConstants.W3C_XML_SCHEMA_NS_URI;

  static XsdNames bundled(Engine.Profile profile) {
    Set<String> elements = new HashSet<>();
    Set<String> attributes = new HashSet<>();
    Map<String, Set<String>> typeAttributes = new HashMap<>();
    Map<String, String> elementTypes = new HashMap<>();
    for (String schema : profile.schemas()) {
      Document doc = parse(Spec.BASE + "xsd/oficial/" + schema);
      collect(doc.getElementsByTagNameNS(XSD_NS, "element"), elements);
      collect(doc.getElementsByTagNameNS(XSD_NS, "attribute"), attributes);
      NodeList types = doc.getElementsByTagNameNS(XSD_NS, "complexType");
      for (int i = 0; i < types.getLength(); i++) {
        Element type = (Element) types.item(i);
        if (!type.getAttribute("name").isEmpty()) {
          Set<String> names = new HashSet<>();
          collect(type.getElementsByTagNameNS(XSD_NS, "attribute"), names);
          typeAttributes.put(type.getAttribute("name"), names);
        }
      }
      NodeList declared = doc.getElementsByTagNameNS(XSD_NS, "element");
      for (int i = 0; i < declared.getLength(); i++) {
        Element e = (Element) declared.item(i);
        if (!e.getAttribute("name").isEmpty() && !e.getAttribute("type").isEmpty()) {
          String type = e.getAttribute("type");
          elementTypes.put(e.getAttribute("name"), type.substring(type.indexOf(':') + 1));
        }
      }
    }
    Set<String> elementAttributes = new HashSet<>();
    for (Map.Entry<String, String> e : elementTypes.entrySet()) {
      for (String attribute : typeAttributes.getOrDefault(e.getValue(), Set.of())) {
        elementAttributes.add(e.getKey() + "@" + attribute);
      }
    }
    // The root may be declared as a complex type (rDE) rather than an element; it is always
    // present.
    elements.add(profile.rootElement());
    return new XsdNames(
        Collections.unmodifiableSet(elements),
        Collections.unmodifiableSet(attributes),
        Collections.unmodifiableSet(elementAttributes));
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
