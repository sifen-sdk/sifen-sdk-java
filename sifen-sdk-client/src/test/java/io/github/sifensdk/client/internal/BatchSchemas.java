/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * Test helper: validates the element inside a SOAP body against an official XSD of {@code
 * ../spec/xsd/oficial/}, without network. Includes by absolute URL ({@code
 * https://ekuatia.set.gov.py/sifen/xsd/…}) and by relative name are both served from that
 * directory; anything else fails.
 */
final class BatchSchemas {

  static final Path OFFICIAL = Path.of("..", "spec", "xsd", "oficial");
  private static final String OFFICIAL_URL = "https://ekuatia.set.gov.py/sifen/xsd/";
  private static final Map<String, Schema> CACHE = new ConcurrentHashMap<>();

  private BatchSchemas() {}

  /**
   * Validates the first element of the SOAP body against a schema.
   *
   * @param envelope the SOAP envelope
   * @param xsd the file name of the schema, for example {@code WS_SiRecepLoteDE_v141.xsd}
   * @throws AssertionError if it is not valid
   */
  static void validateBody(byte[] envelope, String xsd) {
    validateElement(firstBodyElement(envelope), xsd);
  }

  /**
   * Validates an element against a schema.
   *
   * @param element the element
   * @param xsd the schema
   * @throws AssertionError if it is not valid
   */
  static void validateElement(Element element, String xsd) {
    Validator validator = CACHE.computeIfAbsent(xsd, BatchSchemas::compile).newValidator();
    try {
      validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
      validator.validate(new DOMSource(element));
    } catch (org.xml.sax.SAXException e) {
      throw new AssertionError(
          element.getLocalName() + " is not valid against " + xsd + ": " + e.getMessage(), e);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /** Parses XML namespace-aware, without DTDs. */
  static Document parse(byte[] xml) {
    try {
      DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
      f.setNamespaceAware(true);
      f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
    } catch (Exception e) {
      throw new AssertionError("Not well-formed XML: " + e.getMessage(), e);
    }
  }

  static Element firstBodyElement(byte[] envelope) {
    Element root = parse(envelope).getDocumentElement();
    Element body = Soap.child(root, "Body");
    if (body == null) {
      throw new AssertionError("No SOAP body");
    }
    for (Node n = body.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (n instanceof Element) {
        return (Element) n;
      }
    }
    throw new AssertionError("Empty SOAP body");
  }

  private static Schema compile(String xsd) {
    try {
      SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
      factory.setResourceResolver(new OfficialResolver());
      Path file = OFFICIAL.resolve(xsd);
      StreamSource source =
          new StreamSource(Files.newInputStream(file), OFFICIAL_URL + file.getFileName());
      return factory.newSchema(source);
    } catch (Exception e) {
      throw new AssertionError("Could not compile " + xsd + ": " + e.getMessage(), e);
    }
  }

  /** Serves the official schemas from disk; never downloads. */
  private static final class OfficialResolver implements LSResourceResolver {
    @Override
    public LSInput resolveResource(
        String type, String namespaceUri, String publicId, String systemId, String baseUri) {
      if (systemId == null) {
        throw new AssertionError("Schema reference without location: " + namespaceUri);
      }
      String name = systemId.substring(systemId.lastIndexOf('/') + 1);
      boolean known =
          systemId.startsWith(OFFICIAL_URL) || systemId.equals(name) && !name.contains(":");
      Path file = OFFICIAL.resolve(name);
      if (!known || !Files.isRegularFile(file)) {
        throw new AssertionError("Schema reference outside the official set: " + systemId);
      }
      try {
        return new FileInput(OFFICIAL_URL + name, publicId, Files.readAllBytes(file));
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
  }

  private static final class FileInput implements LSInput {
    private final String systemId;
    private final String publicId;
    private final byte[] bytes;

    FileInput(String systemId, String publicId, byte[] bytes) {
      this.systemId = systemId;
      this.publicId = publicId;
      this.bytes = bytes;
    }

    @Override
    public Reader getCharacterStream() {
      return null;
    }

    @Override
    public void setCharacterStream(Reader characterStream) {}

    @Override
    public InputStream getByteStream() {
      return new ByteArrayInputStream(bytes);
    }

    @Override
    public void setByteStream(InputStream byteStream) {}

    @Override
    public String getStringData() {
      return null;
    }

    @Override
    public void setStringData(String stringData) {}

    @Override
    public String getSystemId() {
      return systemId;
    }

    @Override
    public void setSystemId(String systemId) {}

    @Override
    public String getPublicId() {
      return publicId;
    }

    @Override
    public void setPublicId(String publicId) {}

    @Override
    public String getBaseURI() {
      return systemId;
    }

    @Override
    public void setBaseURI(String baseUri) {}

    @Override
    public String getEncoding() {
      return null;
    }

    @Override
    public void setEncoding(String encoding) {}

    @Override
    public boolean getCertifiedText() {
      return false;
    }

    @Override
    public void setCertifiedText(boolean certifiedText) {}
  }
}
