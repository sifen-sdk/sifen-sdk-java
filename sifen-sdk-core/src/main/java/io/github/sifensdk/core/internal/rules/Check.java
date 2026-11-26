/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.rules;

import io.github.sifensdk.core.internal.spec.Rule;
import org.jspecify.annotations.Nullable;

/**
 * The Java implementation of a {@code formato}, {@code dependencia} or {@code aritmetica} rule,
 * registered by rule id.
 *
 * <p>The engine calls it once per occurrence of the rule's field for which the rule's declarative
 * condition (if any) holds. A check never modifies the document.
 */
@FunctionalInterface
interface Check {

  /**
   * Verifies one occurrence.
   *
   * @param evaluation the running evaluation, to read other fields
   * @param rule the rule being checked
   * @param target the occurrence of the rule's field
   * @return an English message without the field's value when the rule is violated; null when it
   *     holds or when the data needed to check it is missing (other rules report missing data)
   */
  @Nullable String verify(Evaluation evaluation, Rule rule, Occurrence target);
}
