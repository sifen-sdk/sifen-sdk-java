/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * The neutral, ordered representation of a SIFEN XML document before serialization.
 *
 * <p>The model is mapped to this tree; the rule engine evaluates rules on it and the serializer
 * writes it, so both see exactly the same data. Element names are the XML names from the
 * specification (for example {@code dTotOpe}). Children are kept in insertion order, which must be
 * the XSD sequence order. A node has either text or children, never both.
 *
 * <p>Built once and then only read; not thread-safe while being built.
 */
public final class Node {

  private final String name;
  private final @Nullable String fieldId;
  private final @Nullable String text;
  private final Map<String, String> attributes = new LinkedHashMap<>();
  private final List<Node> children = new ArrayList<>();
  private @Nullable Node parent;

  private Node(String name, @Nullable String fieldId, @Nullable String text) {
    this.name = name;
    this.fieldId = fieldId;
    this.text = text;
  }

  /**
   * Creates a group (element with children).
   *
   * @param name XML element name
   * @param fieldId specification field id, or null for elements without one
   * @return the node
   */
  public static Node group(String name, @Nullable String fieldId) {
    return new Node(name, fieldId, null);
  }

  /**
   * Creates a leaf (element with text).
   *
   * @param name XML element name
   * @param fieldId specification field id, or null for elements without one
   * @param text the text exactly as it must appear in the XML (already formatted)
   * @return the node
   */
  public static Node leaf(String name, @Nullable String fieldId, String text) {
    return new Node(name, fieldId, text);
  }

  /**
   * Appends a child and returns this node.
   *
   * @param child the child
   * @return this node
   */
  public Node add(Node child) {
    if (text != null) {
      throw new IllegalStateException("A leaf cannot have children: " + name);
    }
    child.parent = this;
    children.add(child);
    return this;
  }

  /**
   * Appends a leaf only when the value is not null, and returns this node.
   *
   * @param name XML element name
   * @param fieldId field id
   * @param value formatted value, or null to skip
   * @return this node
   */
  public Node addIfPresent(String name, @Nullable String fieldId, @Nullable String value) {
    if (value != null) {
      add(leaf(name, fieldId, value));
    }
    return this;
  }

  /**
   * Sets an attribute and returns this node.
   *
   * @param attribute attribute name
   * @param value attribute value
   * @return this node
   */
  public Node attribute(String attribute, String value) {
    attributes.put(attribute, value);
    return this;
  }

  /**
   * The XML element name.
   *
   * @return the name
   */
  public String name() {
    return name;
  }

  /**
   * The specification field id, if the element has one.
   *
   * @return the field id, or null
   */
  public @Nullable String fieldId() {
    return fieldId;
  }

  /**
   * The text of a leaf.
   *
   * @return the text, or null for a group
   */
  public @Nullable String text() {
    return text;
  }

  /**
   * Whether this node is a leaf.
   *
   * @return whether it has text
   */
  public boolean isLeaf() {
    return text != null;
  }

  /**
   * The attributes, in insertion order.
   *
   * @return attributes
   */
  public Map<String, String> attributes() {
    return Collections.unmodifiableMap(attributes);
  }

  /**
   * The children, in order.
   *
   * @return children
   */
  public List<Node> children() {
    return Collections.unmodifiableList(children);
  }

  /**
   * The parent node.
   *
   * @return the parent, or null for the root
   */
  public @Nullable Node parent() {
    return parent;
  }

  /**
   * The path from the root, with 1-based positions for repeated siblings, for example {@code
   * rDE/DE/gDtipDE/gCamItem[2]/dCodInt}. Used in validation messages.
   *
   * @return the path
   */
  public String path() {
    StringBuilder sb = new StringBuilder();
    for (Node n = this; n != null; n = n.parent) {
      String segment = n.name;
      Node p = n.parent;
      if (p != null) {
        int same = 0;
        int index = 0;
        for (Node sibling : p.children) {
          if (sibling.name.equals(n.name)) {
            same++;
            if (sibling == n) {
              index = same;
            }
          }
        }
        if (same > 1) {
          segment = segment + "[" + index + "]";
        }
      }
      sb.insert(0, sb.length() == 0 ? segment : segment + "/");
    }
    return sb.toString();
  }

  /**
   * All descendants (not this node) with a given field id, in document order.
   *
   * @param id field id
   * @return matching nodes
   */
  public List<Node> findAll(String id) {
    List<Node> out = new ArrayList<>();
    collect(this, id, out);
    return out;
  }

  private static void collect(Node node, String id, List<Node> out) {
    for (Node c : node.children) {
      if (id.equals(c.fieldId)) {
        out.add(c);
      }
      collect(c, id, out);
    }
  }
}
