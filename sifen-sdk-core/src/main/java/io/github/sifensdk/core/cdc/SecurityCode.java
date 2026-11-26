/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.cdc;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Generation of the security code of a document ({@code dCodSeg}, B004), part of the {@link Cdc}.
 *
 * <p>Manual Técnico v150, 10.3: a positive number of 9 digits, random, not sequential, between
 * {@code 000000001} and {@code 999999999}, zero-padded on the left, unrelated to the document or
 * the issuer and different from the document number ({@code dNumDoc}, C007). It must be different
 * for each document: with a {@link SecureRandom} the chance of a repetition is negligible, but this
 * class keeps no record of the codes it generated (it has no state).
 */
public final class SecurityCode {

  private static final int MAX = 999_999_999;
  private static final Pattern DIGITS = Pattern.compile("[0-9]{1,9}");

  private SecurityCode() {}

  /**
   * Generates a security code.
   *
   * @param random the source of randomness; it must be a {@link SecureRandom} (the Manual asks for
   *     an algorithm that prevents reproducing the value)
   * @param documentNumber the document number ({@code dNumDoc}, C007), with or without leading
   *     zeros; the code is never numerically equal to it
   * @return nine digits, between {@code 000000001} and {@code 999999999}
   * @throws IllegalArgumentException if the document number is not 1 to 9 digits
   */
  public static String generate(SecureRandom random, String documentNumber) {
    Objects.requireNonNull(random, "random");
    Objects.requireNonNull(documentNumber, "documentNumber");
    if (!DIGITS.matcher(documentNumber).matches()) {
      throw new IllegalArgumentException("documentNumber (C007) must have 1 to 9 digits");
    }
    int number = new BigInteger(documentNumber).intValueExact();
    int code;
    do {
      code = random.nextInt(MAX) + 1;
    } while (code == number);
    return String.format(Locale.ROOT, "%09d", code);
  }
}
