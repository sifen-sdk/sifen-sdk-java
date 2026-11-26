/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.rules;

import io.github.sifensdk.core.internal.tree.Node;
import org.jspecify.annotations.Nullable;

/**
 * One occurrence of a specification field in the document tree.
 *
 * <p>Most fields are elements; a few (the CDC, {@code A002}) are attributes of their parent
 * element, as the XSD defines them (contradiction C-101). For those, {@code anchor} is the element
 * that carries the attribute.
 *
 * @param fieldId the specification field id
 * @param anchor the element node, or the element that carries the attribute
 * @param attribute the attribute name, or null for an element
 */
record Occurrence(String fieldId, Node anchor, @Nullable String attribute) {

  /** The value: the element text, or the attribute value; null for groups. */
  @Nullable String text() {
    return attribute != null ? anchor.attributes().get(attribute) : anchor.text();
  }

  /** The element that contains this occurrence. */
  @Nullable Node container() {
    return attribute != null ? anchor : anchor.parent();
  }

  /** Location for messages. */
  String path() {
    return attribute != null ? anchor.path() + "/@" + attribute : anchor.path();
  }

  @Override
  public String toString() {
    // Never the value (ADR 0006): only the location.
    return "Occurrence{" + fieldId + " at " + path() + "}";
  }
}
