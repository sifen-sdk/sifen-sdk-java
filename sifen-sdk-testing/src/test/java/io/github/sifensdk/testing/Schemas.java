/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.testing;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.SAXException;

/**
 * The official XSDs of {@code ../spec/xsd/oficial}, compiled without network: every {@code
 * https://ekuatia.set.gov.py/sifen/xsd/…} (and relative) location is resolved to the local file.
 */
final class Schemas {

  static final Path DIRECTORY = Path.of("..", "spec", "xsd", "oficial");

  private static final Map<String, Schema> CACHE = new ConcurrentHashMap<>();

  private Schemas() {}

  /** One schema per service XSD: several XSDs redefine the same types, so they are not merged. */
  static Schema of(String fileName) {
    return CACHE.computeIfAbsent(fileName, Schemas::compile);
  }

  /** Validates an XML fragment (a response element) against a service XSD. */
  static void validate(String fileName, String xml) throws SAXException, IOException {
    of(fileName).newValidator().validate(new StreamSource(new StringReader(xml)));
  }

  private static Schema compile(String fileName) {
    try {
      DOMImplementationLS ls =
          (DOMImplementationLS)
              DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
      SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      factory.setResourceResolver((type, namespace, publicId, systemId, baseUri) -> {
        if (systemId == null) {
          return null;
        }
        Path local = DIRECTORY.resolve(systemId.substring(systemId.lastIndexOf('/') + 1));
        if (!Files.isRegularFile(local)) {
          throw new IllegalStateException("XSD not available locally: " + systemId);
        }
        try {
          InputStream in = Files.newInputStream(local);
          LSInput input = ls.createLSInput();
          input.setByteStream(in);
          input.setSystemId(local.toUri().toString());
          input.setPublicId(publicId);
          return input;
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });
      Path file = DIRECTORY.resolve(fileName);
      return factory.newSchema(new StreamSource(file.toFile()));
    } catch (SAXException | ParserConfigurationException e) {
      throw new IllegalStateException("Could not compile " + fileName, e);
    }
  }
}
