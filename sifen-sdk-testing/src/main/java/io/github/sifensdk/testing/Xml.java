/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.testing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Safe reading of the requests the simulated SIFEN receives, and escaping of the values it writes.
 *
 * <p>The parser rejects DTDs and external entities; elements are matched by local name so that
 * prefixes do not matter.
 */
final class Xml {

  /** SIFEN namespace. */
  static final String SIFEN_NS = "http://ekuatia.set.gov.py/sifen/xsd";

  /** SOAP 1.2 envelope namespace. */
  static final String ENVELOPE_NS = "http://www.w3.org/2003/05/soap-envelope";

  /** Reports nothing on the console: malformed requests are a normal case for the simulator. */
  private static final ErrorHandler SILENT =
      new ErrorHandler() {
        @Override
        public void warning(SAXParseException exception) {}

        @Override
        public void error(SAXParseException exception) throws SAXException {
          throw exception;
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
          throw exception;
        }
      };

  private Xml() {}

  /**
   * Parses XML safely.
   *
   * @param xml the bytes
   * @return the document, or null if the bytes are not well-formed XML (or declare a DTD)
   */
  static @Nullable Document parse(byte[] xml) {
    try {
      return parser().parse(new ByteArrayInputStream(xml));
    } catch (SAXException | IOException e) {
      return null;
    }
  }

  /**
   * The first element inside the SOAP body of a request.
   *
   * @param document the parsed request
   * @return the payload, or null if the document is not a SOAP envelope with content
   */
  static @Nullable Element soapPayload(Document document) {
    Element envelope = document.getDocumentElement();
    if (envelope == null || !"Envelope".equals(envelope.getLocalName())) {
      return null;
    }
    Element body = child(envelope, "Body");
    return body == null ? null : firstElement(body);
  }

  static @Nullable Element child(Element parent, String localName) {
    for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (n instanceof Element && localName.equals(n.getLocalName())) {
        return (Element) n;
      }
    }
    return null;
  }

  static @Nullable Element firstElement(Element parent) {
    for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (n instanceof Element) {
        return (Element) n;
      }
    }
    return null;
  }

  static @Nullable Element descendant(Element parent, String localName) {
    for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (n instanceof Element) {
        Element e = (Element) n;
        if (localName.equals(e.getLocalName())) {
          return e;
        }
        Element found = descendant(e, localName);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }

  static List<Element> descendants(Element parent, String localName) {
    List<Element> out = new ArrayList<>();
    collect(parent, localName, out);
    return out;
  }

  private static void collect(Element parent, String localName, List<Element> out) {
    for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (n instanceof Element) {
        Element e = (Element) n;
        if (localName.equals(e.getLocalName())) {
          out.add(e);
        }
        collect(e, localName, out);
      }
    }
  }

  /**
   * The trimmed text of the first descendant with a local name.
   *
   * @return the text, or null if absent or empty
   */
  static @Nullable String text(Element parent, String localName) {
    Element e = descendant(parent, localName);
    if (e == null) {
      return null;
    }
    String t = e.getTextContent().trim();
    return t.isEmpty() ? null : t;
  }

  /**
   * The value of an attribute, or null if absent or empty.
   *
   * @return the value or null
   */
  static @Nullable String attribute(Element element, String name) {
    String value = element.getAttribute(name).trim();
    return value.isEmpty() ? null : value;
  }

  /**
   * Serializes an element without XML declaration. Only used to keep a copy of the documents the
   * simulated SIFEN received, to answer document queries.
   *
   * @return the XML text
   */
  static String serialize(Element element) {
    try {
      TransformerFactory factory = TransformerFactory.newInstance();
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
      Transformer transformer = factory.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      StringWriter out = new StringWriter();
      transformer.transform(new DOMSource(element), new StreamResult(out));
      return out.toString();
    } catch (TransformerException e) {
      throw new IllegalStateException("Could not serialize a received document", e);
    }
  }

  /**
   * Escapes a value for XML text content or attribute values.
   *
   * @param value the value
   * @return the escaped value
   */
  static String escape(String value) {
    StringBuilder sb = new StringBuilder(value.length() + 16);
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      switch (c) {
        case '&':
          sb.append("&amp;");
          break;
        case '<':
          sb.append("&lt;");
          break;
        case '>':
          sb.append("&gt;");
          break;
        case '"':
          sb.append("&quot;");
          break;
        default:
          sb.append(c);
      }
    }
    return sb.toString();
  }

  private static DocumentBuilder parser() {
    try {
      DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
      f.setNamespaceAware(true);
      f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      f.setFeature("http://xml.org/sax/features/external-general-entities", false);
      f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
      f.setXIncludeAware(false);
      f.setExpandEntityReferences(false);
      DocumentBuilder b = f.newDocumentBuilder();
      b.setErrorHandler(SILENT);
      return b;
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException("No secure XML parser available", e);
    }
  }
}
