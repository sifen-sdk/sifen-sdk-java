/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.conformance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * A small JSON reader for the conformance cases of the specification.
 *
 * <p>The client depends on the JDK and the core only, and the core's JSON reader is internal to its
 * module, so the client tests carry their own. Objects become {@code Map<String, Object>} (order
 * kept), arrays {@code List<Object>}, numbers {@link BigDecimal}, {@code null} Java {@code null}.
 * Only for the trusted, small files of the specification.
 */
final class CaseJson {

  private final String text;
  private int pos;

  private CaseJson(String text) {
    this.text = text;
  }

  /**
   * Parses a complete JSON document.
   *
   * @param text the document
   * @return the value
   * @throws IllegalArgumentException if it is not valid JSON
   */
  static @Nullable Object parse(String text) {
    CaseJson json = new CaseJson(text);
    Object value = json.value();
    json.skipWhitespace();
    if (json.pos != text.length()) {
      throw json.error("unexpected trailing content");
    }
    return value;
  }

  @SuppressWarnings("unchecked")
  static Map<String, Object> object(@Nullable Object o) {
    if (!(o instanceof Map)) {
      throw new IllegalArgumentException("Expected a JSON object");
    }
    return (Map<String, Object>) o;
  }

  @SuppressWarnings("unchecked")
  static List<Object> list(@Nullable Object o) {
    if (!(o instanceof List)) {
      throw new IllegalArgumentException("Expected a JSON array");
    }
    return (List<Object>) o;
  }

  private @Nullable Object value() {
    skipWhitespace();
    if (pos >= text.length()) {
      throw error("unexpected end of input");
    }
    char c = text.charAt(pos);
    if (c == '{') {
      return objectValue();
    }
    if (c == '[') {
      return arrayValue();
    }
    if (c == '"') {
      return string();
    }
    if (text.startsWith("true", pos)) {
      pos += 4;
      return Boolean.TRUE;
    }
    if (text.startsWith("false", pos)) {
      pos += 5;
      return Boolean.FALSE;
    }
    if (text.startsWith("null", pos)) {
      pos += 4;
      return null;
    }
    return number();
  }

  private Map<String, Object> objectValue() {
    Map<String, Object> out = new LinkedHashMap<>();
    pos++;
    skipWhitespace();
    if (peek() == '}') {
      pos++;
      return Collections.unmodifiableMap(out);
    }
    while (true) {
      skipWhitespace();
      if (peek() != '"') {
        throw error("expected a key");
      }
      String key = string();
      skipWhitespace();
      expect(':');
      out.put(key, value());
      skipWhitespace();
      char c = next();
      if (c == '}') {
        return Collections.unmodifiableMap(out);
      }
      if (c != ',') {
        throw error("expected ',' or '}'");
      }
    }
  }

  private List<Object> arrayValue() {
    List<Object> out = new ArrayList<>();
    pos++;
    skipWhitespace();
    if (peek() == ']') {
      pos++;
      return Collections.unmodifiableList(out);
    }
    while (true) {
      out.add(value());
      skipWhitespace();
      char c = next();
      if (c == ']') {
        return Collections.unmodifiableList(out);
      }
      if (c != ',') {
        throw error("expected ',' or ']'");
      }
    }
  }

  private String string() {
    expect('"');
    StringBuilder sb = new StringBuilder();
    while (true) {
      char c = next();
      if (c == '"') {
        return sb.toString();
      }
      if (c != '\\') {
        sb.append(c);
        continue;
      }
      char e = next();
      switch (e) {
        case '"':
        case '\\':
        case '/':
          sb.append(e);
          break;
        case 'b':
          sb.append('\b');
          break;
        case 'f':
          sb.append('\f');
          break;
        case 'n':
          sb.append('\n');
          break;
        case 'r':
          sb.append('\r');
          break;
        case 't':
          sb.append('\t');
          break;
        case 'u':
          if (pos + 4 > text.length()) {
            throw error("truncated escape");
          }
          sb.append((char) Integer.parseInt(text.substring(pos, pos + 4), 16));
          pos += 4;
          break;
        default:
          throw error("invalid escape");
      }
    }
  }

  private BigDecimal number() {
    int start = pos;
    while (pos < text.length() && "+-0123456789.eE".indexOf(text.charAt(pos)) >= 0) {
      pos++;
    }
    try {
      return new BigDecimal(text.substring(start, pos));
    } catch (NumberFormatException e) {
      throw error("invalid value");
    }
  }

  private void skipWhitespace() {
    while (pos < text.length() && " \t\r\n".indexOf(text.charAt(pos)) >= 0) {
      pos++;
    }
  }

  private char peek() {
    if (pos >= text.length()) {
      throw error("unexpected end of input");
    }
    return text.charAt(pos);
  }

  private char next() {
    char c = peek();
    pos++;
    return c;
  }

  private void expect(char c) {
    if (next() != c) {
      throw error("expected '" + c + "'");
    }
  }

  private IllegalArgumentException error(String message) {
    return new IllegalArgumentException("Invalid JSON at " + pos + ": " + message);
  }
}
