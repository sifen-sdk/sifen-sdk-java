/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.rules;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.jspecify.annotations.Nullable;

/**
 * An exact fraction of two decimals, so that formulas with divisions (for example the VAT base,
 * {@code / 1,1}) are compared without any rounding of the exact result.
 *
 * <p>The denominator is never zero.
 */
final class Rational {

  static final Rational ZERO = new Rational(BigDecimal.ZERO, BigDecimal.ONE);

  private final BigDecimal numerator;
  private final BigDecimal denominator;

  private Rational(BigDecimal numerator, BigDecimal denominator) {
    if (denominator.signum() < 0) {
      this.numerator = numerator.negate();
      this.denominator = denominator.negate();
    } else {
      this.numerator = numerator;
      this.denominator = denominator;
    }
  }

  static Rational of(BigDecimal value) {
    return new Rational(value, BigDecimal.ONE);
  }

  static Rational of(long value) {
    return of(BigDecimal.valueOf(value));
  }

  static Rational of(String value) {
    return of(new BigDecimal(value));
  }

  Rational add(Rational other) {
    if (denominator.compareTo(other.denominator) == 0) {
      return new Rational(numerator.add(other.numerator), denominator);
    }
    return new Rational(
        numerator.multiply(other.denominator).add(other.numerator.multiply(denominator)),
        denominator.multiply(other.denominator));
  }

  Rational subtract(Rational other) {
    return add(other.negate());
  }

  Rational negate() {
    return new Rational(numerator.negate(), denominator);
  }

  Rational multiply(Rational other) {
    return new Rational(
        numerator.multiply(other.numerator), denominator.multiply(other.denominator));
  }

  /** The quotient, or null when dividing by zero. */
  @Nullable Rational divide(Rational other) {
    if (other.numerator.signum() == 0) {
      return null;
    }
    return new Rational(
        numerator.multiply(other.denominator), denominator.multiply(other.numerator));
  }

  /**
   * Whether {@code |reported - this| <= allowed}, computed exactly.
   *
   * @param reported the value as reported
   * @param allowed the allowed difference, not negative
   * @return whether the reported value is within the allowed difference
   */
  boolean isWithin(BigDecimal reported, BigDecimal allowed) {
    BigDecimal difference = reported.multiply(denominator).subtract(numerator).abs();
    return difference.compareTo(allowed.multiply(denominator)) <= 0;
  }

  /**
   * Half a unit of the last decimal of a reported value: the largest difference for which the
   * reported value is a valid rounding of an exact result at the scale the issuer chose.
   */
  static BigDecimal halfUnitOfLastDecimal(BigDecimal reported) {
    int scale = Math.max(0, reported.scale());
    return new BigDecimal(BigInteger.valueOf(5), scale + 1);
  }

  @Override
  public String toString() {
    // Amounts may be document content: never printed (ADR 0006).
    return "Rational";
  }
}
