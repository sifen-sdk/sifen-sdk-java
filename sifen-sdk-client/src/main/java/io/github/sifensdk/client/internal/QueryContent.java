/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.internal;

import io.github.sifensdk.client.EventType;
import io.github.sifensdk.client.QueriedEvent;
import io.github.sifensdk.client.ResultMessage;
import io.github.sifensdk.client.Status;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Reads the content of {@code xContenDE} of a document query: the transaction number and the events
 * registered on the document.
 *
 * <p>The XSD and the Manual describe a root {@code rContDe} with {@code rDE}, {@code dProtAut} and
 * {@code xContEv}; SIFEN's test environment returns those three without the root, as escaped text
 * (specification evidence E-002, 2026-11-27, C-717). Both forms are read: the elements are looked
 * up wherever they are. Each {@code rContEv} holds the event as sent ({@code xEvento/rGesEve}) and
 * SIFEN's answer ({@code rResEnviEventoDe/rRetEnviEventoDe/gResProcEVe}).
 */
final class QueryContent {

  private static final Pattern DECLARATION = Pattern.compile("<\\?xml[^>]*\\?>");

  final @Nullable String authorizationProtocol;
  final List<QueriedEvent> events;

  private QueryContent(@Nullable String authorizationProtocol, List<QueriedEvent> events) {
    this.authorizationProtocol = authorizationProtocol;
    this.events = events;
  }

  /** Nothing readable: no transaction number and no events. */
  static final QueryContent EMPTY = new QueryContent(null, List.of());

  /**
   * Reads the content.
   *
   * @param content the text of {@code xContenDE}, unescaped
   * @return what could be read; {@link #EMPTY} if it is not well-formed XML
   */
  static QueryContent read(String content) {
    String body = DECLARATION.matcher(content).replaceAll("");
    Document doc;
    try {
      doc = Soap.parser().parse(new InputSource(new StringReader("<wrap>" + body + "</wrap>")));
    } catch (SAXException | IOException e) {
      return EMPTY;
    }
    Element root = doc.getDocumentElement();
    String protocol = null;
    for (Element e : elements(root, "dProtAut")) {
      // The document's number is the one outside the events.
      if (ancestor(e, "rContEv") == null) {
        protocol = e.getTextContent().trim();
        break;
      }
    }
    List<String> rawEvents = rawElements(body, "rGesEve");
    List<QueriedEvent> events = new ArrayList<>();
    List<Element> containers = elements(root, "rContEv");
    for (int i = 0; i < containers.size(); i++) {
      QueriedEvent event = event(containers.get(i), i < rawEvents.size() ? rawEvents.get(i) : "");
      if (event != null) {
        events.add(event);
      }
    }
    return new QueryContent(protocol, List.copyOf(events));
  }

  private static @Nullable QueriedEvent event(Element container, String raw) {
    Element rGesEve = first(container, "rGesEve");
    Element rEve = rGesEve == null ? null : Soap.child(rGesEve, "rEve");
    Element group = rEve == null ? null : Soap.child(rEve, "gGroupTiEvt");
    Element detail = group == null ? null : firstElementChild(group);
    if (rEve == null || detail == null) {
      return null;
    }
    Map<String, String> values = new LinkedHashMap<>();
    for (Node n = detail.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (n instanceof Element && firstElementChild((Element) n) == null) {
        values.putIfAbsent(n.getLocalName(), n.getTextContent());
      }
    }
    LocalDateTime signed = null;
    String date = Soap.text(rEve, "dFecFirma");
    if (date != null) {
      try {
        signed = LocalDateTime.parse(date.trim());
      } catch (DateTimeParseException e) {
        signed = null;
      }
    }
    Element result = first(container, "gResProcEVe");
    Status status = null;
    String protocol = "";
    List<ResultMessage> messages = List.of();
    if (result != null) {
      String state = Soap.text(result, "dEstRes");
      status = state == null ? null : Events.statusOrNull(state);
      String p = Soap.text(result, "dProtAut");
      protocol = p == null ? "" : p.trim();
      messages = Soap.messages(Soap.children(result, "gResProc"));
    }
    return new QueriedEvent(
        rEve.getAttribute("Id"),
        EventType.ofElement(detail.getLocalName()),
        detail.getLocalName(),
        signed,
        values,
        raw,
        status,
        protocol,
        messages);
  }

  /** The text of each element with that name, in order, exactly as it appears in the content. */
  private static List<String> rawElements(String body, String name) {
    List<String> out = new ArrayList<>();
    int from = 0;
    while (true) {
      int start = indexOfStart(body, name, from);
      if (start < 0) {
        return out;
      }
      int end = body.indexOf("</" + name + ">", start);
      if (end < 0) {
        return out;
      }
      end += name.length() + 3;
      out.add(body.substring(start, end));
      from = end;
    }
  }

  private static int indexOfStart(String body, String name, int from) {
    int i = body.indexOf("<" + name, from);
    while (i >= 0) {
      char next = i + name.length() + 1 < body.length() ? body.charAt(i + name.length() + 1) : ' ';
      if (next == '>' || next == ' ' || next == '/' || Character.isWhitespace(next)) {
        return i;
      }
      i = body.indexOf("<" + name, i + 1);
    }
    return -1;
  }

  private static List<Element> elements(Element root, String localName) {
    List<Element> out = new ArrayList<>();
    NodeList all = root.getElementsByTagNameNS("*", localName);
    for (int i = 0; i < all.getLength(); i++) {
      out.add((Element) all.item(i));
    }
    return out;
  }

  private static @Nullable Element first(Element root, String localName) {
    NodeList all = root.getElementsByTagNameNS("*", localName);
    return all.getLength() == 0 ? null : (Element) all.item(0);
  }

  private static @Nullable Element ancestor(Element e, String localName) {
    for (Node n = e.getParentNode(); n instanceof Element; n = n.getParentNode()) {
      if (localName.equals(n.getLocalName())) {
        return (Element) n;
      }
    }
    return null;
  }

  private static @Nullable Element firstElementChild(Element parent) {
    for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (n instanceof Element) {
        return (Element) n;
      }
    }
    return null;
  }
}
