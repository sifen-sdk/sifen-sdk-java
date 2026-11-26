/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.rules;

import io.github.sifensdk.core.Environment;
import io.github.sifensdk.core.internal.tree.Node;
import io.github.sifensdk.core.validation.ValidationResult;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

/**
 * Applies the rules of the bundled specification to the tree of an electronic document, before it
 * is signed.
 *
 * <p>Stateless and thread-safe: the specification is prepared once (immutable) and every call
 * builds its own evaluation.
 *
 * <h2>What is evaluated</h2>
 *
 * <ul>
 *   <li><b>Selection.</b> Rules of fields under {@code rDE} (AA001), except the groups added by the
 *       signer ({@code ds:Signature}, I001, and {@code gCamFuFD}, J001). A rule is evaluated when
 *       its field is in force (state {@code vigente} and validity applies) and the rule applies on
 *       the reference date in the environment ({@code Validity.appliesOn}, end exclusive).
 *   <li><b>Superseded rules without an end date.</b> A rule that a later source ended without
 *       publishing the date for this environment is skipped when another rule of the same field and
 *       type, not superseded, applies (the later one wins, for example {@code R-F009-A2} over
 *       {@code R-F009-A1} after the NT 1); when there is none, it still applies.
 *   <li><b>Scope.</b> A field exists inside its parent group. {@code obligatorio} requires the
 *       field in each existing instance of the parent group (no parent, no requirement: the
 *       parent's own rule decides); {@code condicional} requires it when the condition holds in
 *       that instance; {@code prohibido} rejects each occurrence for which the condition holds;
 *       {@code opcional} does nothing.
 *   <li><b>Conditions</b> (spec FORMATO.md): {@code todas}, {@code alguna}, {@code no}; {@code
 *       igual}, {@code distinto}, {@code en}, {@code no_en} compare text; {@code mayor}, {@code
 *       menor} and the like compare decimals; {@code presente}, {@code ausente}. A field inside the
 *       same repeated group as the evaluated instance is read from the same repetition (the same
 *       item); otherwise any repetition satisfies it, or all with {@code "cuantificador": "todos"}.
 *       An absent field satisfies only {@code ausente} and {@code no_en}.
 *   <li><b>Format, dependency and arithmetic rules</b> are checked by Java implementations
 *       registered by rule id ({@code ValueChecks}, {@code ArithmeticChecks}); see the latter for
 *       the arithmetic comparison semantics (ADR 0010). Values are never modified.
 *   <li><b>Catalogs.</b> Every code must exist in its catalog and be in force on the reference date
 *       (issue rule id {@code CATALOG}); descriptions must match their code and department,
 *       district and city codes must be related, under the rules that require it.
 *   <li><b>Unchecked.</b> Rules that apply but cannot be verified offline are listed in {@link
 *       ValidationResult#uncheckedRules()}: non-declarative rules without a Java implementation,
 *       format, dependency and arithmetic rules without one (for example those that depend on
 *       SIFEN's data: registered RUC, stamp in force, duplicates, time limits against SIFEN's
 *       clock), and presence rules of fields that have no element in the current XSD (for example
 *       {@code dFeFinT}, C-105).
 * </ul>
 *
 * <p>Issues have severity {@code ERROR}, except the rules passed as warnings and those the
 * specification decides to report as warnings ({@code R-F017-A1}, C-502). Messages are in English
 * and never contain the value of a field.
 */
public final class RuleEngine {

  private RuleEngine() {}

  /**
   * Evaluates the rules on a document.
   *
   * @param rde the root {@code rDE} of the unsigned document tree
   * @param environment the environment the document is for
   * @param referenceDate the document's issue date ({@code dFeEmiDE}, D002), which selects the
   *     rules in force
   * @return issues found and rules that could not be checked
   */
  public static ValidationResult evaluate(
      Node rde, Environment environment, LocalDate referenceDate) {
    return evaluate(rde, environment, referenceDate, Set.of());
  }

  /**
   * Evaluates the rules on a document, reporting some rules as warnings instead of errors.
   *
   * @param rde the root {@code rDE} of the unsigned document tree
   * @param environment the environment the document is for
   * @param referenceDate the document's issue date ({@code dFeEmiDE}, D002)
   * @param warningRuleIds ids of rules (for example {@code R-F017-A2}, or {@code CATALOG}) whose
   *     issues are reported with severity {@code WARNING}
   * @return issues found and rules that could not be checked
   */
  public static ValidationResult evaluate(
      Node rde, Environment environment, LocalDate referenceDate, Set<String> warningRuleIds) {
    Objects.requireNonNull(rde, "rde");
    Objects.requireNonNull(environment, "environment");
    Objects.requireNonNull(referenceDate, "referenceDate");
    Objects.requireNonNull(warningRuleIds, "warningRuleIds");
    return Engine.bundled().evaluate(rde, environment, referenceDate, warningRuleIds);
  }
}
