/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.spec;

import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * A rule of the specification ({@code reglas/}).
 *
 * @param id stable rule id, for example {@code R-E727-A1}
 * @param field field id the rule applies to
 * @param type obligatorio, opcional, condicional, prohibido, formato, dependencia or aritmetica
 * @param text what SIFEN requires, in one sentence (Spanish, as in the source)
 * @param condition declarative condition, or null
 * @param declarative false when the condition cannot be expressed with the grammar
 * @param formula arithmetic formula as written in the source, or null
 * @param operands field ids used by the formula
 * @param tolerance tolerance as written in the source, or null
 * @param sifenCode SIFEN rejection code, or null
 * @param sources official sources
 * @param validity validity
 * @param contradictions related contradiction ids
 * @param discarded true when the specification records the rule as {@code descartada}: written by
 *     the source, but SIFEN does not apply it (shown by an evidence file), so it is never verified
 */
public record Rule(
    String id,
    String field,
    String type,
    String text,
    @Nullable Condition condition,
    boolean declarative,
    @Nullable String formula,
    List<String> operands,
    @Nullable Object tolerance,
    @Nullable String sifenCode,
    List<Source> sources,
    Validity validity,
    List<String> contradictions,
    boolean discarded) {}
