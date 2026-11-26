/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.testing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * One request being answered: what was received, what can be read from it, and the state of the
 * simulated SIFEN that answers it.
 */
final class Interaction {

  /** {@code fecUTC} of the XSDs: date, time and offset, never {@code Z} (C-703). */
  private static final DateTimeFormatter FEC_UTC =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx");

  /** Decompressed batches larger than this are ignored (the real limit is far lower, C-707). */
  private static final int MAX_BATCH_BYTES = 20 * 1024 * 1024;

  private final RecordedRequest request;
  private final @Nullable Element payload;
  private final State state;

  Interaction(RecordedRequest request, @Nullable Element payload, State state) {
    this.request = Objects.requireNonNull(request, "request");
    this.payload = payload;
    this.state = Objects.requireNonNull(state, "state");
  }

  /** Builds the interaction for a request body, parsing it safely. */
  static Interaction of(RecordedRequest request, State state) {
    return new Interaction(request, payloadOf(request.body()), state);
  }

  static @Nullable Element payloadOf(byte[] body) {
    Document document = Xml.parse(body);
    return document == null ? null : Xml.soapPayload(document);
  }

  RecordedRequest request() {
    return request;
  }

  State state() {
    return state;
  }

  /** Processing date and time ({@code dFecProc}) in Paraguay's time zone, with offset. */
  String processedAt() {
    return OffsetDateTime.ofInstant(state.clock().instant(), State.PARAGUAY).format(FEC_UTC);
  }

  /** The trimmed text of an element of the request payload, or null. */
  @Nullable String text(String localName) {
    return payload == null ? null : Xml.text(payload, localName);
  }

  /** The documents ({@code rDE}) sent to the synchronous reception: normally one. */
  List<ReceivedDocument> receptionDocuments() {
    if (payload == null) {
      return List.of();
    }
    Element xde = Xml.descendant(payload, "xDE");
    return xde == null ? List.of() : documentsIn(xde);
  }

  /** The documents ({@code rDE}) inside the compressed batch ({@code rEnvioLote/xDE}). */
  List<ReceivedDocument> batchDocuments() {
    String base64 = text("xDE");
    if (base64 == null) {
      return List.of();
    }
    byte[] content;
    try {
      content = Base64.getMimeDecoder().decode(base64);
    } catch (IllegalArgumentException e) {
      return List.of();
    }
    byte[] xml = unzipFirstEntry(content);
    if (xml == null) {
      xml = content; // tolerate an uncompressed rLoteDE
    }
    Document document = Xml.parse(xml);
    return document == null ? List.of() : documentsIn(document.getDocumentElement());
  }

  /** The ids ({@code rEve/@Id}) of the events sent, in order. */
  List<String> eventIds() {
    if (payload == null) {
      return List.of();
    }
    List<String> ids = new ArrayList<>();
    for (Element e : Xml.descendants(payload, "rEve")) {
      String id = Xml.attribute(e, "Id");
      if (id != null) {
        ids.add(id);
      }
    }
    return ids;
  }

  /**
   * The batch protocol number of a batch result query: {@code dProtConsLote}, or the batch that
   * contained {@code dCDC} (C-712).
   */
  @Nullable String queriedBatch() {
    String protocol = text("dProtConsLote");
    if (protocol != null) {
      return protocol;
    }
    String cdc = text("dCDC");
    return cdc == null ? null : state.batchContaining(cdc);
  }

  private static List<ReceivedDocument> documentsIn(Element container) {
    List<ReceivedDocument> out = new ArrayList<>();
    List<Element> rdes = new ArrayList<>();
    if ("rDE".equals(container.getLocalName())) {
      rdes.add(container);
    } else {
      rdes.addAll(Xml.descendants(container, "rDE"));
    }
    for (Element rde : rdes) {
      Element de = Xml.descendant(rde, "DE");
      String cdc = de == null ? null : Xml.attribute(de, "Id");
      if (cdc != null) {
        out.add(new ReceivedDocument(cdc, Xml.text(rde, "DigestValue"), Xml.serialize(rde)));
      }
    }
    return out;
  }

  private static byte @Nullable [] unzipFirstEntry(byte[] content) {
    if (content.length < 4 || content[0] != 'P' || content[1] != 'K') {
      return null;
    }
    try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(content))) {
      ZipEntry entry = zip.getNextEntry();
      return entry == null ? null : readLimited(zip);
    } catch (IOException e) {
      return null;
    }
  }

  private static byte @Nullable [] readLimited(InputStream in) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buffer = new byte[8192];
    int total = 0;
    int n;
    while ((n = in.read(buffer)) != -1) {
      total += n;
      if (total > MAX_BATCH_BYTES) {
        return null;
      }
      out.write(buffer, 0, n);
    }
    return out.toByteArray();
  }

  /** A document found in a request: its CDC, its digest value and its XML. */
  static final class ReceivedDocument {
    final String cdc;
    final @Nullable String digestValue;
    final String xml;

    ReceivedDocument(String cdc, @Nullable String digestValue, String xml) {
      this.cdc = cdc;
      this.digestValue = digestValue;
      this.xml = xml;
    }
  }
}
