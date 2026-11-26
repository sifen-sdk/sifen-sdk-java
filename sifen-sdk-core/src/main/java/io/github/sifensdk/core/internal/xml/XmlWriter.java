/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.xml;

import io.github.sifensdk.core.internal.tree.Node;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Writes a {@link Node} tree as the XML bytes SIFEN expects.
 *
 * <p>The output is deterministic (same tree, same bytes) and follows the Manual Técnico v150,
 * section 7.2, and the DNIT "Guía de mejores prácticas":
 *
 * <ul>
 *   <li>UTF-8 with a single declaration {@code <?xml version="1.0" encoding="UTF-8"?>} (MT150
 *       7.2.1; the Manual prints {@code version="150"}, an obvious typo: XML only defines 1.0 and
 *       1.1).
 *   <li>The SIFEN namespace {@code http://ekuatia.set.gov.py/sifen/xsd} declared once, as the
 *       default namespace of the root element; no element prefixes (MT150 7.2.2 "no se podrá
 *       utilizar prefijos de namespace"; MT150 7.2.4 and Guía item 4; SIFEN answers 0106 "Utilizado
 *       prefijo en el namespace", AB07).
 *   <li>No whitespace, line breaks or comments between elements (MT150 7.2.4 and Guía items 2 and
 *       3). Text is written exactly as it is in the tree: this class neither trims nor formats.
 *   <li>When the root is {@code rDE}, it also carries {@code
 *       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"} and {@code
 *       xsi:schemaLocation="http://ekuatia.set.gov.py/sifen/xsd siRecepDE_v150.xsd"}, exactly as
 *       the example of MT150 7.2.2 and the Guía ("Generación de los Documentos Electrónicos") show.
 *       The other examples of MT150 7.2.2.1 and 7.2.2.2 write a single URL ({@code
 *       .../xsd/siRecepDE_v150.xsd}), which is not a valid {@code schemaLocation} (it must be
 *       namespace/location pairs), so the pair form is used. The {@code xsi} prefix only appears on
 *       that attribute, never on an element, as in the Manual. The attribute is outside the signed
 *       {@code DE} and is not visibly used inside it, so exclusive canonicalization of {@code DE}
 *       does not include it.
 * </ul>
 *
 * <p>Escaping: in text, {@code &}, {@code <}, {@code >} and carriage return are written as
 * references; in attribute values also {@code "}, tab and line feed, so that they survive
 * attribute-value normalization. Characters that XML 1.0 does not allow (control characters other
 * than tab, line feed and carriage return, unpaired surrogates, U+FFFE and U+FFFF) are rejected
 * with an exception that names the element path, never the value.
 */
public final class XmlWriter {

  /** The SIFEN namespace, declared as the default namespace of the root element. */
  public static final String SIFEN_NAMESPACE = "http://ekuatia.set.gov.py/sifen/xsd";

  /** The XML Schema instance namespace. */
  static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";

  /** The schema location of {@code rDE}, as in MT150 section 7.2.2. */
  static final String RDE_SCHEMA_LOCATION = SIFEN_NAMESPACE + " siRecepDE_v150.xsd";

  private static final String DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

  private XmlWriter() {}

  /**
   * Writes a document.
   *
   * @param root the root element, normally {@code rDE} without {@code Signature} nor {@code
   *     gCamFuFD}
   * @return the UTF-8 bytes, starting with the XML declaration
   * @throws IllegalArgumentException if a name is not a valid unprefixed XML name, an attribute
   *     tries to declare a namespace, or a text contains a character XML does not allow
   */
  public static byte[] write(Node root) {
    StringBuilder out = new StringBuilder(4096);
    out.append(DECLARATION);
    writeElement(root, out, true);
    return out.toString().getBytes(StandardCharsets.UTF_8);
  }

  private static void writeElement(Node node, StringBuilder out, boolean root) {
    String name = node.name();
    requireName(name, node);
    out.append('<').append(name);
    if (root) {
      out.append(" xmlns=\"").append(SIFEN_NAMESPACE).append('"');
      if ("rDE".equals(name)) {
        out.append(" xmlns:xsi=\"").append(XSI_NAMESPACE).append('"');
        out.append(" xsi:schemaLocation=\"").append(RDE_SCHEMA_LOCATION).append('"');
      }
    }
    for (Map.Entry<String, String> a : node.attributes().entrySet()) {
      String attribute = a.getKey();
      requireName(attribute, node);
      if (attribute.equals("xmlns")) {
        throw new IllegalArgumentException(
            "Namespaces are declared by the serializer, not by attributes: " + node.path());
      }
      out.append(' ').append(attribute).append("=\"");
      escape(a.getValue(), out, true, node);
      out.append('"');
    }
    out.append('>');
    String text = node.text();
    if (text != null) {
      escape(text, out, false, node);
    } else {
      for (Node child : node.children()) {
        writeElement(child, out, false);
      }
    }
    out.append("</").append(name).append('>');
  }

  /** Accepts unprefixed ASCII XML names, which is what every SIFEN element and attribute uses. */
  private static void requireName(String name, Node node) {
    boolean ok = !name.isEmpty() && isNameStart(name.charAt(0));
    for (int i = 1; ok && i < name.length(); i++) {
      char c = name.charAt(i);
      ok = isNameStart(c) || (c >= '0' && c <= '9') || c == '-' || c == '.';
    }
    if (!ok) {
      throw new IllegalArgumentException("Invalid XML name at " + node.path());
    }
  }

  private static boolean isNameStart(char c) {
    return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_';
  }

  private static void escape(String s, StringBuilder out, boolean attribute, Node node) {
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '&':
          out.append("&amp;");
          break;
        case '<':
          out.append("&lt;");
          break;
        case '>':
          out.append("&gt;");
          break;
        case '\r':
          out.append("&#xD;");
          break;
        case '"':
          out.append(attribute ? "&quot;" : "\"");
          break;
        case '\t':
          out.append(attribute ? "&#x9;" : "\t");
          break;
        case '\n':
          out.append(attribute ? "&#xA;" : "\n");
          break;
        default:
          if (Character.isHighSurrogate(c)
              && i + 1 < s.length()
              && Character.isLowSurrogate(s.charAt(i + 1))) {
            out.append(c).append(s.charAt(++i));
          } else if (c < 0x20 || Character.isSurrogate(c) || c == 0xFFFE || c == 0xFFFF) {
            throw new IllegalArgumentException(
                "Character not allowed in XML 1.0 at " + node.path());
          } else {
            out.append(c);
          }
      }
    }
  }
}
