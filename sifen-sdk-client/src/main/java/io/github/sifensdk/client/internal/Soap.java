/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.internal;

import io.github.sifensdk.client.Exchange;
import io.github.sifensdk.client.ProtocolException;
import io.github.sifensdk.client.ResultMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * SOAP 1.2 envelopes for SIFEN, and safe reading of its responses.
 *
 * <p>Requests are assembled as bytes around the signed document, which is embedded exactly as it
 * was signed and is never parsed or serialized again (ADR-0005). Responses are read with a parser
 * that rejects DTDs and external entities, matching elements by local name so that the prefixes
 * SIFEN uses (they vary between services) do not matter.
 */
public final class Soap {

  /** SOAP 1.2 envelope namespace. */
  public static final String ENVELOPE_NS = "http://www.w3.org/2003/05/soap-envelope";

  /** SIFEN namespace. */
  public static final String SIFEN_NS = "http://ekuatia.set.gov.py/sifen/xsd";

  private static final String PREFIX =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
          + "<env:Envelope xmlns:env=\""
          + ENVELOPE_NS
          + "\"><env:Header/><env:Body>";
  private static final String SUFFIX = "</env:Body></env:Envelope>";

  private Soap() {}

  /**
   * Wraps a request body in a SOAP 1.2 envelope.
   *
   * @param body the request element, for example {@code <rEnviDe xmlns="...">...</rEnviDe>}, as
   *     UTF-8 bytes without XML declaration
   * @return the envelope
   */
  public static byte[] envelope(byte[] body) {
    byte[] prefix = PREFIX.getBytes(StandardCharsets.UTF_8);
    byte[] suffix = SUFFIX.getBytes(StandardCharsets.UTF_8);
    byte[] out = new byte[prefix.length + body.length + suffix.length];
    System.arraycopy(prefix, 0, out, 0, prefix.length);
    System.arraycopy(body, 0, out, prefix.length, body.length);
    System.arraycopy(suffix, 0, out, prefix.length + body.length, suffix.length);
    return out;
  }

  /**
   * Returns an XML document without its XML declaration, to embed it in a request.
   *
   * @param xml UTF-8 XML, with or without declaration
   * @return the same bytes after the declaration
   */
  public static byte[] withoutDeclaration(byte[] xml) {
    int start = 0;
    if (xml.length >= 3
        && (xml[0] & 0xFF) == 0xEF
        && (xml[1] & 0xFF) == 0xBB
        && (xml[2] & 0xFF) == 0xBF) {
      start = 3;
    }
    if (xml.length > start + 5 && xml[start] == '<' && xml[start + 1] == '?') {
      for (int i = start + 2; i < xml.length - 1; i++) {
        if (xml[i] == '?' && xml[i + 1] == '>') {
          start = i + 2;
          break;
        }
      }
    }
    byte[] out = new byte[xml.length - start];
    System.arraycopy(xml, start, out, 0, out.length);
    return out;
  }

  /**
   * Escapes a value for XML text content.
   *
   * @param value the value
   * @return the escaped value
   */
  public static String escape(String value) {
    StringBuilder sb = new StringBuilder(value.length());
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
        default:
          sb.append(c);
      }
    }
    return sb.toString();
  }

  /**
   * Reads the SOAP response of an exchange and returns the first element inside the body.
   *
   * @param exchange the exchange
   * @param expectedRoot local name of the expected response element, for example {@code rRetEnviDe}
   * @return the response element
   * @throws ProtocolException if the response is HTML, a SOAP fault, not XML, or another element
   */
  public static Element body(Exchange exchange, String expectedRoot) {
    byte[] response = exchange.response();
    if (looksLikeHtml(response)) {
      throw new ProtocolException(
          "SIFEN answered with an HTML page (HTTP "
              + exchange.httpStatus()
              + ") instead of SOAP; usually the client certificate was not accepted (NT 16) or the"
              + " service is unavailable",
          exchange);
    }
    Document document;
    try {
      document = parser().parse(new ByteArrayInputStream(response));
    } catch (SAXException | IOException e) {
      throw new ProtocolException(
          "SIFEN's response is not well-formed XML (HTTP " + exchange.httpStatus() + ")", exchange);
    }
    Element envelope = document.getDocumentElement();
    Element body = child(envelope, "Body");
    if (!"Envelope".equals(envelope.getLocalName()) || body == null) {
      throw new ProtocolException("SIFEN's response is not a SOAP envelope", exchange);
    }
    Element first = firstElement(body);
    if (first == null) {
      throw new ProtocolException("SIFEN's response has an empty SOAP body", exchange);
    }
    if ("Fault".equals(first.getLocalName())) {
      Element reason = descendant(first, "Text");
      throw new ProtocolException(
          "SIFEN answered with a SOAP fault"
              + (reason == null ? "" : ": " + reason.getTextContent().trim()),
          exchange);
    }
    if (!expectedRoot.equals(first.getLocalName())) {
      throw new ProtocolException(
          "Unexpected SIFEN response element: expected "
              + expectedRoot
              + ", got "
              + first.getLocalName(),
          exchange);
    }
    return first;
  }

  /**
   * The first direct child element with a local name.
   *
   * @param parent the parent
   * @param localName the local name
   * @return the child, or null
   */
  public static @Nullable Element child(Element parent, String localName) {
    for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (n instanceof Element && localName.equals(n.getLocalName())) {
        return (Element) n;
      }
    }
    return null;
  }

  /**
   * All direct child elements with a local name, in order.
   *
   * @param parent the parent
   * @param localName the local name
   * @return the children
   */
  public static List<Element> children(Element parent, String localName) {
    List<Element> out = new ArrayList<>();
    for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (n instanceof Element && localName.equals(n.getLocalName())) {
        out.add((Element) n);
      }
    }
    return out;
  }

  /**
   * The first descendant element with a local name, depth first.
   *
   * @param parent the parent
   * @param localName the local name
   * @return the element, or null
   */
  public static @Nullable Element descendant(Element parent, String localName) {
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

  /**
   * The trimmed text of the first descendant with a local name.
   *
   * @param parent the parent
   * @param localName the local name
   * @return the text, or null if absent or empty
   */
  public static @Nullable String text(Element parent, String localName) {
    Element e = descendant(parent, localName);
    if (e == null) {
      return null;
    }
    String t = e.getTextContent().trim();
    return t.isEmpty() ? null : t;
  }

  /**
   * The {@code dCodRes}/{@code dMsgRes} pairs found in the given result groups (for example every
   * {@code gResProc} of a document).
   *
   * @param groups the result groups
   * @return the messages, in order
   */
  public static List<ResultMessage> messages(List<Element> groups) {
    List<ResultMessage> out = new ArrayList<>();
    for (Element g : groups) {
      String code = text(g, "dCodRes");
      String message = text(g, "dMsgRes");
      if (code != null) {
        out.add(new ResultMessage(code, message == null ? "" : message));
      }
    }
    return out;
  }

  private static @Nullable Element firstElement(Element parent) {
    for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (n instanceof Element) {
        return (Element) n;
      }
    }
    return null;
  }

  private static boolean looksLikeHtml(byte[] response) {
    String head =
        new String(response, 0, Math.min(response.length, 512), StandardCharsets.UTF_8)
            .trim()
            .toLowerCase(java.util.Locale.ROOT);
    return head.startsWith("<!doctype html") || head.startsWith("<html") || head.contains("<html");
  }

  static DocumentBuilder parser() {
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
      b.setErrorHandler(null);
      return b;
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException("No secure XML parser available", e);
    }
  }
}
