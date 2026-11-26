/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.internal;

import java.nio.charset.StandardCharsets;

/**
 * Byte-level checks on signed events, without parsing them (ADR-0005: a signed XML is never parsed
 * and serialized again on its way to SIFEN).
 */
final class EventXml {

  private EventXml() {}

  /**
   * Whether the bytes start, after optional white space, with the start tag of an element with the
   * given name and no prefix (MT150 7.2.2 forbids namespace prefixes).
   *
   * @param xml UTF-8 XML without declaration
   * @param name the element name
   * @return true if the first tag is {@code <name} followed by white space, {@code >} or {@code /}
   */
  static boolean startsWithElement(byte[] xml, String name) {
    int i = 0;
    while (i < xml.length && isSpace(xml[i])) {
      i++;
    }
    byte[] tag = ("<" + name).getBytes(StandardCharsets.UTF_8);
    if (xml.length < i + tag.length + 1) {
      return false;
    }
    for (int j = 0; j < tag.length; j++) {
      if (xml[i + j] != tag[j]) {
        return false;
      }
    }
    byte next = xml[i + tag.length];
    return isSpace(next) || next == '>' || next == '/';
  }

  private static boolean isSpace(byte b) {
    return b == ' ' || b == '\t' || b == '\r' || b == '\n';
  }
}
