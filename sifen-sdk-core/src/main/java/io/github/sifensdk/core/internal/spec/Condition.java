/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.spec;

import java.util.List;

/** A declarative condition, as defined in the specification's FORMATO.md. */
public sealed interface Condition {

  /** All conditions hold. */
  record All(List<Condition> conditions) implements Condition {}

  /** At least one condition holds. */
  record Any(List<Condition> conditions) implements Condition {}

  /** The condition does not hold. */
  record Not(Condition condition) implements Condition {}

  /**
   * A comparison on one field.
   *
   * @param field field id (for example {@code C002})
   * @param op one of igual, distinto, mayor, mayor_igual, menor, menor_igual, en, no_en, presente,
   *     ausente
   * @param values the compared value(s); empty for presente/ausente
   * @param quantifier {@code alguno} (default) or {@code todos}, for repeated fields
   */
  record Compare(String field, String op, List<String> values, String quantifier)
      implements Condition {}
}
