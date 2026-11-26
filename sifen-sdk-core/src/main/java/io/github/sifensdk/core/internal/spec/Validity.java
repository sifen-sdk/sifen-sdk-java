/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.spec;

import io.github.sifensdk.core.Environment;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

/**
 * When a specification entry applies, per environment.
 *
 * <p>A missing bound means "unbounded". A bound present for the entry but published by no source
 * (JSON {@code null}) is also treated as unbounded: the entry applies, and the gap is documented in
 * the specification.
 */
public record Validity(
    @Nullable LocalDate fromTest,
    @Nullable LocalDate fromProduction,
    @Nullable LocalDate untilTest,
    @Nullable LocalDate untilProduction,
    boolean hasUntil) {

  /** Applies always. */
  public static final Validity ALWAYS = new Validity(null, null, null, null, false);

  /**
   * Whether the entry applies on a date in an environment. The {@code until} bound is exclusive.
   *
   * @param environment the environment
   * @param date the reference date (normally the document's issue date)
   * @return whether it applies
   */
  public boolean appliesOn(Environment environment, LocalDate date) {
    LocalDate from = environment == Environment.TEST ? fromTest : fromProduction;
    LocalDate until = environment == Environment.TEST ? untilTest : untilProduction;
    if (from != null && date.isBefore(from)) {
      return false;
    }
    return until == null || date.isBefore(until);
  }

  /**
   * Whether the entry has been superseded by a later source, even if the date is not published.
   * Such entries are kept for history; rule engines skip those whose end date is unknown.
   *
   * @return whether an end of validity is declared
   */
  public boolean isSuperseded() {
    return hasUntil;
  }
}
