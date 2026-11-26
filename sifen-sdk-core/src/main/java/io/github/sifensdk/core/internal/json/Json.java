/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.json;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Minimal, strict JSON reader (RFC 8259) for the bundled specification files.
 *
 * <p>Objects become unmodifiable {@code Map<String, Object>} (insertion order kept), arrays become
 * unmodifiable {@code List<Object>}, numbers become {@link BigDecimal}, and {@code null} becomes
 * Java {@code null}. It exists so the core needs no third-party dependency (ADR-0002); it is not
 * meant for untrusted or very large input.
 */
public final class Json {

  private final String text;
  private int pos;

  private Json(String text) {
    this.text = text;
  }

  /**
   * Parses a complete JSON document.
   *
   * @param text the document
   * @return the parsed value
   * @throws IllegalArgumentException if the text is not valid JSON
   */
  public static @Nullable Object parse(String text) {
    Json json = new Json(text);
    json.skipWhitespace();
    Object value = json.readValue();
    json.skipWhitespace();
    if (json.pos != text.length()) {
      throw json.error("unexpected trailing content");
    }
    return value;
  }

  private @Nullable Object readValue() {
    if (pos >= text.length()) {
      throw error("unexpected end of input");
    }
    char c = text.charAt(pos);
    switch (c) {
      case '{':
        return readObject();
      case '[':
        return readArray();
      case '"':
        return readString();
      case 't':
        expect("true");
        return Boolean.TRUE;
      case 'f':
        expect("false");
        return Boolean.FALSE;
      case 'n':
        expect("null");
        return null;
      default:
        if (c == '-' || (c >= '0' && c <= '9')) {
          return readNumber();
        }
        throw error("unexpected character '" + c + "'");
    }
  }

  private Map<String, @Nullable Object> readObject() {
    Map<String, @Nullable Object> map = new LinkedHashMap<>();
    pos++;
    skipWhitespace();
    if (peek() == '}') {
      pos++;
      return Collections.unmodifiableMap(map);
    }
    while (true) {
      skipWhitespace();
      if (peek() != '"') {
        throw error("expected a string key");
      }
      String key = readString();
      skipWhitespace();
      if (peek() != ':') {
        throw error("expected ':'");
      }
      pos++;
      skipWhitespace();
      if (map.containsKey(key)) {
        throw error("duplicate key '" + key + "'");
      }
      map.put(key, readValue());
      skipWhitespace();
      char c = peek();
      pos++;
      if (c == '}') {
        return Collections.unmodifiableMap(map);
      }
      if (c != ',') {
        throw error("expected ',' or '}'");
      }
    }
  }

  private List<@Nullable Object> readArray() {
    List<@Nullable Object> list = new ArrayList<>();
    pos++;
    skipWhitespace();
    if (peek() == ']') {
      pos++;
      return Collections.unmodifiableList(list);
    }
    while (true) {
      skipWhitespace();
      list.add(readValue());
      skipWhitespace();
      char c = peek();
      pos++;
      if (c == ']') {
        return Collections.unmodifiableList(list);
      }
      if (c != ',') {
        throw error("expected ',' or ']'");
      }
    }
  }

  private String readString() {
    StringBuilder sb = new StringBuilder();
    pos++;
    while (true) {
      if (pos >= text.length()) {
        throw error("unterminated string");
      }
      char c = text.charAt(pos++);
      if (c == '"') {
        return sb.toString();
      }
      if (c < 0x20) {
        throw error("control character in string");
      }
      if (c != '\\') {
        sb.append(c);
        continue;
      }
      if (pos >= text.length()) {
        throw error("unterminated escape");
      }
      char e = text.charAt(pos++);
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
            throw error("short unicode escape");
          }
          try {
            sb.append((char) Integer.parseInt(text.substring(pos, pos + 4), 16));
          } catch (NumberFormatException ex) {
            throw error("invalid unicode escape");
          }
          pos += 4;
          break;
        default:
          throw error("invalid escape '\\" + e + "'");
      }
    }
  }

  private BigDecimal readNumber() {
    int start = pos;
    if (peek() == '-') {
      pos++;
    }
    while (pos < text.length() && "0123456789.eE+-".indexOf(text.charAt(pos)) >= 0) {
      pos++;
    }
    try {
      return new BigDecimal(text.substring(start, pos));
    } catch (NumberFormatException ex) {
      throw error("invalid number");
    }
  }

  private void expect(String literal) {
    if (!text.startsWith(literal, pos)) {
      throw error("expected '" + literal + "'");
    }
    pos += literal.length();
  }

  private char peek() {
    if (pos >= text.length()) {
      throw error("unexpected end of input");
    }
    return text.charAt(pos);
  }

  private void skipWhitespace() {
    while (pos < text.length()) {
      char c = text.charAt(pos);
      if (c != ' ' && c != '\n' && c != '\r' && c != '\t') {
        return;
      }
      pos++;
    }
  }

  private IllegalArgumentException error(String message) {
    return new IllegalArgumentException("Invalid JSON at offset " + pos + ": " + message);
  }
}
