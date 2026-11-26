/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;

/**
 * Official XSDs of {@code ../spec/xsd/oficial}, compiled without network: every {@code
 * https://ekuatia.set.gov.py/sifen/xsd/...} location (and relative ones) is served from that
 * directory, and anything else fails.
 */
final class ReceptionSchemas {

  static final Path OFFICIAL = Path.of("..", "spec", "xsd", "oficial");

  private ReceptionSchemas() {}

  static Schema schema(String file) throws Exception {
    SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    DOMImplementationLS ls =
        (DOMImplementationLS)
            DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
    factory.setResourceResolver((type, namespace, publicId, systemId, baseUri) -> {
      if (systemId == null) {
        return null;
      }
      String resolved =
          baseUri == null ? systemId : URI.create(baseUri).resolve(systemId).toString();
      String name = resolved.substring(resolved.lastIndexOf('/') + 1);
      Path local = OFFICIAL.resolve(name);
      if (!Files.isRegularFile(local)) {
        throw new IllegalStateException("Schema not available offline: " + resolved);
      }
      LSInput input = ls.createLSInput();
      try {
        input.setByteStream(new ByteArrayInputStream(Files.readAllBytes(local)));
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
      input.setSystemId("https://ekuatia.set.gov.py/sifen/xsd/" + name);
      return input;
    });
    Path main = OFFICIAL.resolve(file);
    try (InputStream in = Files.newInputStream(main)) {
      return factory.newSchema(
          new StreamSource(in, "https://ekuatia.set.gov.py/sifen/xsd/" + file));
    }
  }

  /** Validates the first element inside the SOAP body of an envelope. */
  static void validateBody(Schema schema, byte[] envelope) throws Exception {
    schema.newValidator().validate(new DOMSource(body(envelope)));
  }

  /** The first element inside the SOAP body. */
  static Element body(byte[] envelope) throws Exception {
    DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
    f.setNamespaceAware(true);
    f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    Document d = f.newDocumentBuilder().parse(new ByteArrayInputStream(envelope));
    Element envelopeElement = d.getDocumentElement();
    for (Node n = envelopeElement.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (n instanceof Element && "Body".equals(n.getLocalName())) {
        for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
          if (c instanceof Element) {
            return (Element) c;
          }
        }
      }
    }
    throw new IllegalArgumentException("No SOAP body element");
  }
}
