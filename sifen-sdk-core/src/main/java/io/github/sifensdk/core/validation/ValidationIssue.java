/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.validation;

import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * One problem found before sending a document to SIFEN.
 *
 * <p>Always says which field, which rule and where the rule is documented, so the issuer can fix
 * the data in its own system. It never contains the value of a field, only its location.
 */
public final class ValidationIssue {

  private final Severity severity;
  private final String ruleId;
  private final @Nullable String fieldId;
  private final @Nullable String xmlName;
  private final String path;
  private final String message;
  private final @Nullable String sifenCode;
  private final List<String> sources;
  private final List<String> contradictions;

  /**
   * Creates an issue.
   *
   * @param severity severity
   * @param ruleId specification rule id (for example {@code R-E727-A1}), or {@code XSD} for schema
   *     errors
   * @param fieldId specification field id (for example {@code E727}), or null
   * @param xmlName XML element name (for example {@code dTotBruOpeItem}), or null
   * @param path location in the document (for example {@code rDE/DE/gDtipDE/gCamItem[2]})
   * @param message what is wrong
   * @param sifenCode the code SIFEN would answer with, or null
   * @param sources official sources of the rule (for example {@code MT150 1859})
   * @param contradictions ids of open contradictions that affect the rule
   */
  public ValidationIssue(
      Severity severity,
      String ruleId,
      @Nullable String fieldId,
      @Nullable String xmlName,
      String path,
      String message,
      @Nullable String sifenCode,
      List<String> sources,
      List<String> contradictions) {
    this.severity = Objects.requireNonNull(severity, "severity");
    this.ruleId = Objects.requireNonNull(ruleId, "ruleId");
    this.fieldId = fieldId;
    this.xmlName = xmlName;
    this.path = Objects.requireNonNull(path, "path");
    this.message = Objects.requireNonNull(message, "message");
    this.sifenCode = sifenCode;
    this.sources = List.copyOf(sources);
    this.contradictions = List.copyOf(contradictions);
  }

  /**
   * @return severity
   */
  public Severity severity() {
    return severity;
  }

  /**
   * @return rule id, or {@code XSD} for schema errors
   */
  public String ruleId() {
    return ruleId;
  }

  /**
   * @return field id, or null
   */
  public @Nullable String fieldId() {
    return fieldId;
  }

  /**
   * @return XML element name, or null
   */
  public @Nullable String xmlName() {
    return xmlName;
  }

  /**
   * @return location in the document
   */
  public String path() {
    return path;
  }

  /**
   * @return what is wrong
   */
  public String message() {
    return message;
  }

  /**
   * @return the code SIFEN would answer with, or null
   */
  public @Nullable String sifenCode() {
    return sifenCode;
  }

  /**
   * @return official sources of the rule
   */
  public List<String> sources() {
    return sources;
  }

  /**
   * @return open contradictions that affect the rule
   */
  public List<String> contradictions() {
    return contradictions;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(severity).append(' ').append(path);
    if (fieldId != null) {
      sb.append(" (").append(xmlName).append(", ").append(fieldId).append(')');
    }
    sb.append(": ").append(message).append(" [").append(ruleId);
    if (sifenCode != null) {
      sb.append(", SIFEN ").append(sifenCode);
    }
    if (!sources.isEmpty()) {
      sb.append(", ").append(String.join("; ", sources));
    }
    return sb.append(']').toString();
  }
}
