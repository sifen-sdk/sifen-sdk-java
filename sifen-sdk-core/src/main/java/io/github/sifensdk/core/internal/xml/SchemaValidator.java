/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.xml;

import io.github.sifensdk.core.internal.spec.Field;
import io.github.sifensdk.core.internal.spec.Spec;
import io.github.sifensdk.core.validation.Severity;
import io.github.sifensdk.core.validation.ValidationIssue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Validates SIFEN documents against the official XSD bundled in the jar, without network access.
 *
 * <p>The official schemas include each other by absolute URL ({@code
 * https://ekuatia.set.gov.py/sifen/xsd/DE_v150.xsd}) and by relative name ({@code
 * xmldsig-core-schema.xsd}). A resolver serves both forms from {@code
 * /io/github/sifensdk/core/spec/xsd/oficial/}; any other reference is an error, never a download
 * ({@link XMLConstants#ACCESS_EXTERNAL_SCHEMA} and {@link XMLConstants#ACCESS_EXTERNAL_DTD} are
 * empty and {@link XMLConstants#FEATURE_SECURE_PROCESSING} is on). Documents are parsed without
 * DTDs or external entities: a {@code DOCTYPE} is rejected. Any {@code xsi:schemaLocation} in the
 * document is ignored; only the bundled schemas are used.
 *
 * <p>Issues have rule id {@code XSD}, severity {@link Severity#ERROR}, the element path (as {@link
 * io.github.sifensdk.core.internal.tree.Node#path()} would print it), the XML name and, when the
 * element is in the specification, its field id. Messages are written by this class in English and
 * never contain the value of a field (the parser's own messages do, so they are not used).
 *
 * <p>Known defects of the official XSD that affect validation (they are reported, not fixed: the
 * official files are never edited):
 *
 * <ul>
 *   <li><b>C-201</b>: {@code tgCompPub} declares {@code <xs:element name="dEntCont ">} with a
 *       trailing space. The {@code name} attribute is an {@code xs:NCName}, whose whitespace facet
 *       is {@code collapse}, so the JDK schema processor trims it and the element is {@code
 *       dEntCont}, as in the Manual (E022). The schema compiles and {@code <dEntCont>} validates;
 *       other processors may behave differently.
 *   <li><b>C-310</b>: {@code tgVehNuevo/dCilin} (E786) has {@code <xs:pattern value="/d"/>}, which
 *       in XSD only matches the literal text {@code /d}. Every real value fails with an {@code XSD}
 *       issue on {@code dCilin}; the field is optional (0-1) and is better omitted until SIFEN
 *       fixes the schema.
 * </ul>
 *
 * <p>The compiled schemas are immutable and thread-safe; each is compiled once, lazily, in a holder
 * class. Validators are created per call.
 */
public final class SchemaValidator {

  /** Where the official schemas are bundled. */
  static final String OFFICIAL_XSD = Spec.BASE + "xsd/oficial/";

  /** The URL prefix the official schemas use to include each other. */
  static final String OFFICIAL_URL = "https://ekuatia.set.gov.py/sifen/xsd/";

  /** The wrapper schema that declares a global {@code DE} element of the official type. */
  static final String DE_WRAPPER = "/io/github/sifensdk/core/xml/DE_elemento_v150.xsd";

  private static final String RULE_ID = "XSD";
  private static final String CURRENT_ELEMENT_NODE =
      "http://apache.org/xml/properties/dom/current-element-node";
  private static final Pattern CVC_KEY = Pattern.compile("^(cvc-[A-Za-z0-9.\\-]+):");
  private static final Pattern EXPECTED = Pattern.compile("\\{([^{}]*)\\}");
  private static final Pattern QUALIFIED = Pattern.compile("\"[^\"]*\":");

  private SchemaValidator() {}

  /**
   * Validates the {@code DE} element of an unsigned {@code rDE} against {@code DE_v150.xsd}.
   *
   * <p>The whole {@code rDE} cannot be validated before signing because {@code siRecepDE_v150.xsd}
   * requires {@code Signature} and {@code gCamFuFD}. Only {@code DE} is validated; {@code dVerFor}
   * and the root are not.
   *
   * @param unsignedRde the document as written by {@link XmlWriter}
   * @return the issues, empty when the document is valid
   */
  public static List<ValidationIssue> validateDocument(byte[] unsignedRde) {
    List<ValidationIssue> issues = new ArrayList<>();
    Document document = parse(unsignedRde, issues);
    if (document == null) {
      return issues;
    }
    Element root = document.getDocumentElement();
    Element de = null;
    if (XmlWriter.SIFEN_NAMESPACE.equals(root.getNamespaceURI())) {
      if ("DE".equals(root.getLocalName())) {
        de = root;
      } else if ("rDE".equals(root.getLocalName())) {
        de = firstChild(root, "DE");
      }
    }
    if (de == null) {
      issues.add(
          issue(
              pathOf(root),
              root.getLocalName(),
              "Element DE in namespace " + XmlWriter.SIFEN_NAMESPACE + " not found"));
      return issues;
    }
    validate(DocumentSchema.SCHEMA, de, issues);
    return issues;
  }

  /**
   * Validates a complete signed {@code rDE} against {@code siRecepDE_v150.xsd}.
   *
   * @param signedRde the signed document
   * @return the issues, empty when the document is valid
   */
  public static List<ValidationIssue> validateSigned(byte[] signedRde) {
    List<ValidationIssue> issues = new ArrayList<>();
    Document document = parse(signedRde, issues);
    if (document == null) {
      return issues;
    }
    validate(SignedSchema.SCHEMA, document.getDocumentElement(), issues);
    return issues;
  }

  // ---------------------------------------------------------------------------------------------
  // Validation

  private static void validate(Schema schema, Element element, List<ValidationIssue> issues) {
    Validator validator = schema.newValidator();
    try {
      validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    } catch (SAXException e) {
      throw new IllegalStateException("The XML validator does not support secure processing", e);
    }
    validator.setResourceResolver(new BundledResolver());
    validator.setErrorHandler(new Collector(validator, element, issues));
    try {
      validator.validate(new DOMSource(element));
    } catch (SAXException e) {
      // Already collected by the error handler (a fatal error stops validation).
      if (issues.isEmpty()) {
        issues.add(issue(pathOf(element), element.getLocalName(), "Schema validation failed"));
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /** Turns parser errors into issues, with the location of the element being validated. */
  private static final class Collector implements ErrorHandler {
    private final Validator validator;
    private final Element fallback;
    private final List<ValidationIssue> issues;

    Collector(Validator validator, Element fallback, List<ValidationIssue> issues) {
      this.validator = validator;
      this.fallback = fallback;
      this.issues = issues;
    }

    @Override
    public void warning(SAXParseException e) {
      // Warnings (for example an ignored schemaLocation hint) do not make the document invalid.
    }

    @Override
    public void error(SAXParseException e) {
      add(e);
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
      add(e);
      throw e;
    }

    private void add(SAXParseException e) {
      String raw = e.getMessage() == null ? "" : e.getMessage();
      Matcher m = CVC_KEY.matcher(raw);
      String key = m.find() ? m.group(1) : "";
      if (isFollowUp(key)) {
        return;
      }
      Element current = currentElement();
      issues.add(issue(pathOf(current), current.getLocalName(), message(key, raw)));
    }

    private Element currentElement() {
      try {
        Object node = validator.getProperty(CURRENT_ELEMENT_NODE);
        if (node instanceof Element) {
          return (Element) node;
        }
      } catch (SAXException e) {
        // Not supported by this processor: fall back to the validated element.
      }
      return fallback;
    }
  }

  /**
   * Errors that Xerces reports right after a more precise one for the same element (for example
   * {@code cvc-type.3.1.3} after {@code cvc-pattern-valid}); they would duplicate the issue.
   */
  private static boolean isFollowUp(String key) {
    return key.equals("cvc-type.3.1.3")
        || key.equals("cvc-attribute.3")
        || key.equals("cvc-elt.5.2.2.2.2")
        || key.equals("cvc-complex-type.2.2");
  }

  /** An English message without values, from the Xerces message key. */
  static String message(String key, String raw) {
    switch (key) {
      case "cvc-complex-type.2.4.a":
        return "Unexpected element here (wrong order or not allowed in this group)" + expected(raw);
      case "cvc-complex-type.2.4.b":
        return "Incomplete content: a required child element is missing" + expected(raw);
      case "cvc-complex-type.2.4.c":
      case "cvc-complex-type.2.4.d":
      case "cvc-complex-type.2.4.f":
        return "Unexpected element: no more child elements are allowed here";
      case "cvc-complex-type.2.1":
        return "Element must be empty";
      case "cvc-complex-type.2.3":
        return "Text is not allowed in a group, only child elements";
      case "cvc-complex-type.3.2.2":
        return "Attribute not allowed on this element";
      case "cvc-complex-type.4":
        return "A required attribute is missing";
      case "cvc-pattern-valid":
        return "Value does not match the pattern of its XSD type";
      case "cvc-enumeration-valid":
        return "Value is not one of the values allowed by its XSD type";
      case "cvc-length-valid":
        return "Value does not have the exact length required by its XSD type";
      case "cvc-minLength-valid":
        return "Value is shorter than the minimum length of its XSD type";
      case "cvc-maxLength-valid":
        return "Value is longer than the maximum length of its XSD type";
      case "cvc-totalDigits-valid":
        return "Value has more digits than its XSD type allows";
      case "cvc-fractionDigits-valid":
        return "Value has more decimal places than its XSD type allows";
      case "cvc-minInclusive-valid":
      case "cvc-minExclusive-valid":
        return "Value is below the minimum allowed by its XSD type";
      case "cvc-maxInclusive-valid":
      case "cvc-maxExclusive-valid":
        return "Value is above the maximum allowed by its XSD type";
      case "cvc-datatype-valid.1.2.1":
      case "cvc-datatype-valid.1.2.2":
      case "cvc-datatype-valid.1.2.3":
        return "Value is not valid for its XSD data type";
      case "cvc-elt.1.a":
      case "cvc-elt.1":
        return "Element is not declared as a document root in the schema";
      case "cvc-id.1":
      case "cvc-id.2":
        return "Duplicate or dangling ID value";
      default:
        return "Does not conform to the XSD" + (key.isEmpty() ? "" : " (" + key + ")");
    }
  }

  /**
   * The expected element names that Xerces lists in the last pair of braces of the content-model
   * messages ({@code cvc-complex-type.2.4.a} and {@code .b}): schema names, never document values.
   * Only used for those messages, which contain no values.
   */
  private static String expected(String raw) {
    Matcher m = EXPECTED.matcher(raw);
    String last = null;
    while (m.find()) {
      last = m.group(1);
    }
    if (last == null) {
      return "";
    }
    String names = QUALIFIED.matcher(last).replaceAll("").replace("'", "").trim();
    if (names.isEmpty() || !names.matches("[A-Za-z0-9_.:, \\-]+")) {
      return "";
    }
    return "; expected: " + names;
  }

  private static ValidationIssue issue(String path, @Nullable String xmlName, String message) {
    String fieldId = null;
    if (xmlName != null) {
      fieldId = FieldIndex.fieldId(xmlName, path);
    }
    return new ValidationIssue(
        Severity.ERROR, RULE_ID, fieldId, xmlName, path, message, null, List.of(), List.of());
  }

  // ---------------------------------------------------------------------------------------------
  // Paths and field ids

  /** The path as {@code Node.path()} prints it: 1-based positions only for repeated names. */
  static String pathOf(Element element) {
    StringBuilder sb = new StringBuilder();
    for (org.w3c.dom.Node n = element;
        n != null && n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE;
        n = n.getParentNode()) {
      String name = localName(n);
      org.w3c.dom.Node p = n.getParentNode();
      if (p != null && p.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
        int same = 0;
        int index = 0;
        for (org.w3c.dom.Node s = p.getFirstChild(); s != null; s = s.getNextSibling()) {
          if (s.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE && localName(s).equals(name)) {
            same++;
            if (s == n) {
              index = same;
            }
          }
        }
        if (same > 1) {
          name = name + "[" + index + "]";
        }
      }
      sb.insert(0, sb.length() == 0 ? name : name + "/");
    }
    return sb.toString();
  }

  private static String localName(org.w3c.dom.Node n) {
    String local = n.getLocalName();
    return local != null ? local : n.getNodeName();
  }

  private static @Nullable Element firstChild(Element parent, String localName) {
    for (org.w3c.dom.Node c = parent.getFirstChild(); c != null; c = c.getNextSibling()) {
      if (c.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
          && localName.equals(c.getLocalName())
          && XmlWriter.SIFEN_NAMESPACE.equals(c.getNamespaceURI())) {
        return (Element) c;
      }
    }
    return null;
  }

  /** Field ids by XML name; ambiguous names are resolved with the parent element's name. */
  private static final class FieldIndex {
    static final Map<String, List<Field>> BY_XML = build();

    private static Map<String, List<Field>> build() {
      Map<String, List<Field>> map = new HashMap<>();
      for (Field f : Spec.bundled().fields().values()) {
        String xml = f.xml();
        String local = xml.substring(xml.indexOf(':') + 1);
        map.computeIfAbsent(local, k -> new ArrayList<>()).add(f);
      }
      map.replaceAll((k, v) -> List.copyOf(v));
      return Map.copyOf(map);
    }

    static @Nullable String fieldId(String xmlName, String path) {
      List<Field> candidates = BY_XML.get(xmlName);
      if (candidates == null || candidates.isEmpty()) {
        return null;
      }
      if (candidates.size() == 1) {
        return candidates.get(0).id();
      }
      String parentXml = parentName(path);
      Field chosen = null;
      for (Field f : candidates) {
        String parentId = f.parent();
        Field parent = parentId == null ? null : Spec.bundled().fields().get(parentId);
        String fieldParentXml =
            parent == null ? null : parent.xml().substring(parent.xml().indexOf(':') + 1);
        boolean sameParent =
            parentXml == null ? fieldParentXml == null : parentXml.equals(fieldParentXml);
        if (sameParent && (chosen == null || preferred(f, chosen))) {
          chosen = f;
        }
      }
      return chosen == null ? null : chosen.id();
    }

    /** Prefers a field in force over a future or removed one with the same name and parent. */
    private static boolean preferred(Field candidate, Field current) {
      return "vigente".equals(candidate.state()) && !"vigente".equals(current.state());
    }

    private static @Nullable String parentName(String path) {
      String[] segments = path.split("/");
      if (segments.length < 2) {
        return null;
      }
      String parent = segments[segments.length - 2];
      int bracket = parent.indexOf('[');
      return bracket < 0 ? parent : parent.substring(0, bracket);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // Parsing

  private static @Nullable Document parse(byte[] xml, List<ValidationIssue> issues) {
    DocumentBuilder builder = newDocumentBuilder();
    builder.setErrorHandler(
        new ErrorHandler() {
          @Override
          public void warning(SAXParseException e) {}

          @Override
          public void error(SAXParseException e) throws SAXException {
            throw e;
          }

          @Override
          public void fatalError(SAXParseException e) throws SAXException {
            throw e;
          }
        });
    builder.setEntityResolver((publicId, systemId) -> {
      throw new SAXException("External entities are not allowed");
    });
    try {
      return builder.parse(new ByteArrayInputStream(xml));
    } catch (SAXException e) {
      issues.add(
          new ValidationIssue(
              Severity.ERROR,
              RULE_ID,
              null,
              null,
              "/",
              "Not well-formed XML, or it declares a DOCTYPE or entities (not allowed)",
              null,
              List.of(),
              List.of()));
      return null;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /** A namespace-aware DOM parser with DTDs, external entities and XInclude disabled. */
  static DocumentBuilder newDocumentBuilder() {
    DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
    f.setNamespaceAware(true);
    f.setValidating(false);
    f.setXIncludeAware(false);
    f.setExpandEntityReferences(false);
    try {
      f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      f.setFeature("http://xml.org/sax/features/external-general-entities", false);
      f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
      return f.newDocumentBuilder();
    } catch (ParserConfigurationException | IllegalArgumentException e) {
      throw new IllegalStateException("The XML parser does not support secure processing", e);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // Schemas

  private static final class DocumentSchema {
    static final Schema SCHEMA = compile(DE_WRAPPER, "urn:io.github.sifensdk:xsd:DE_elemento_v150");
  }

  private static final class SignedSchema {
    static final Schema SCHEMA =
        compile(OFFICIAL_XSD + "siRecepDE_v150.xsd", OFFICIAL_URL + "siRecepDE_v150.xsd");
  }

  private static Schema compile(String resource, String systemId) {
    InputStream in = SchemaValidator.class.getResourceAsStream(resource);
    if (in == null) {
      throw new IllegalStateException("Bundled schema not found: " + resource);
    }
    try (InputStream stream = in) {
      return compile(new StreamSource(stream, systemId));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (SAXException e) {
      throw new IllegalStateException("Bundled schema does not compile: " + resource, e);
    }
  }

  /**
   * Compiles a schema with the bundled resolver and external access disabled. Any error or warning
   * while loading the schema (including a reference that is not bundled) fails.
   *
   * @param sources the schema sources
   * @return the compiled schema
   * @throws SAXException if the schema is invalid or references a schema that is not bundled
   */
  static Schema compile(Source... sources) throws SAXException {
    SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    factory.setResourceResolver(new BundledResolver());
    factory.setErrorHandler(
        new ErrorHandler() {
          @Override
          public void warning(SAXParseException e) throws SAXException {
            throw e;
          }

          @Override
          public void error(SAXParseException e) throws SAXException {
            throw e;
          }

          @Override
          public void fatalError(SAXParseException e) throws SAXException {
            throw e;
          }
        });
    return factory.newSchema(sources);
  }

  /**
   * Serves the official schemas from the jar, by relative name or by their {@code
   * https://ekuatia.set.gov.py/sifen/xsd/} URL. Anything else is refused with an exception, so a
   * missing schema is an error and never a download.
   */
  static final class BundledResolver implements LSResourceResolver {

    @Override
    public LSInput resolveResource(
        String type,
        @Nullable String namespaceURI,
        @Nullable String publicId,
        @Nullable String systemId,
        @Nullable String baseURI) {
      String file = bundledName(systemId, baseURI);
      InputStream in =
          file == null ? null : SchemaValidator.class.getResourceAsStream(OFFICIAL_XSD + file);
      if (in == null) {
        throw new IllegalArgumentException(
            "Schema reference not bundled and external access is disabled: " + systemId);
      }
      return new Input(in, publicId, OFFICIAL_URL + file, baseURI);
    }

    /** The file name of a bundled official schema, or null if the reference is not one. */
    static @Nullable String bundledName(@Nullable String systemId, @Nullable String baseURI) {
      if (systemId == null) {
        return null;
      }
      String resolved;
      try {
        URI uri = new URI(systemId.trim());
        if (!uri.isAbsolute() && baseURI != null) {
          URI base = new URI(baseURI);
          if (base.isOpaque()) {
            return null;
          }
          uri = base.resolve(uri);
        }
        resolved = uri.normalize().toString();
      } catch (URISyntaxException e) {
        return null;
      }
      String file;
      if (resolved.startsWith(OFFICIAL_URL)) {
        file = resolved.substring(OFFICIAL_URL.length());
      } else {
        return null;
      }
      if (!file.toLowerCase(Locale.ROOT).endsWith(".xsd") || !file.matches("[A-Za-z0-9_.\\-]+")) {
        return null;
      }
      return file;
    }
  }

  /** A minimal {@link LSInput} over a byte stream. */
  private static final class Input implements LSInput {
    private @Nullable InputStream byteStream;
    private @Nullable String publicId;
    private @Nullable String systemId;
    private @Nullable String baseURI;

    Input(
        InputStream byteStream,
        @Nullable String publicId,
        String systemId,
        @Nullable String baseURI) {
      this.byteStream = byteStream;
      this.publicId = publicId;
      this.systemId = systemId;
      this.baseURI = baseURI;
    }

    @Override
    public @Nullable Reader getCharacterStream() {
      return null;
    }

    @Override
    public void setCharacterStream(@Nullable Reader characterStream) {
      // Not used: the schemas are read as bytes so their own encoding declaration applies.
    }

    @Override
    public @Nullable InputStream getByteStream() {
      return byteStream;
    }

    @Override
    public void setByteStream(@Nullable InputStream byteStream) {
      this.byteStream = byteStream;
    }

    @Override
    public @Nullable String getStringData() {
      return null;
    }

    @Override
    public void setStringData(@Nullable String stringData) {
      // Not used.
    }

    @Override
    public @Nullable String getSystemId() {
      return systemId;
    }

    @Override
    public void setSystemId(@Nullable String systemId) {
      this.systemId = systemId;
    }

    @Override
    public @Nullable String getPublicId() {
      return publicId;
    }

    @Override
    public void setPublicId(@Nullable String publicId) {
      this.publicId = publicId;
    }

    @Override
    public @Nullable String getBaseURI() {
      return baseURI;
    }

    @Override
    public void setBaseURI(@Nullable String baseURI) {
      this.baseURI = baseURI;
    }

    @Override
    public @Nullable String getEncoding() {
      return null;
    }

    @Override
    public void setEncoding(@Nullable String encoding) {
      // Not used: the encoding comes from the schema's XML declaration.
    }

    @Override
    public boolean getCertifiedText() {
      return false;
    }

    @Override
    public void setCertifiedText(boolean certifiedText) {
      // Not used.
    }
  }
}
