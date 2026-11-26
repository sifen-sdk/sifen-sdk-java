/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.validation;

import java.util.List;

/**
 * The outcome of validating a document before sending it.
 *
 * <p>Besides the issues found, it lists the rules the SDK could not check locally (for example,
 * those that depend on SIFEN's own data, such as "must match the taxpayer registry"), so nothing is
 * hidden.
 */
public final class ValidationResult {

  private final List<ValidationIssue> issues;
  private final List<String> uncheckedRules;

  /**
   * Creates a result.
   *
   * @param issues issues found
   * @param uncheckedRules ids of applicable rules that cannot be checked offline
   */
  public ValidationResult(List<ValidationIssue> issues, List<String> uncheckedRules) {
    this.issues = List.copyOf(issues);
    this.uncheckedRules = List.copyOf(uncheckedRules);
  }

  /**
   * @return whether there is no issue of severity {@link Severity#ERROR}
   */
  public boolean isValid() {
    return errors().isEmpty();
  }

  /**
   * @return all issues
   */
  public List<ValidationIssue> issues() {
    return issues;
  }

  /**
   * @return issues of severity {@link Severity#ERROR}
   */
  public List<ValidationIssue> errors() {
    return issues.stream().filter(i -> i.severity() == Severity.ERROR).toList();
  }

  /**
   * @return ids of applicable rules that cannot be checked offline
   */
  public List<String> uncheckedRules() {
    return uncheckedRules;
  }

  @Override
  public String toString() {
    return "ValidationResult{errors="
        + errors().size()
        + ", issues="
        + issues.size()
        + ", unchecked="
        + uncheckedRules.size()
        + '}';
  }
}
