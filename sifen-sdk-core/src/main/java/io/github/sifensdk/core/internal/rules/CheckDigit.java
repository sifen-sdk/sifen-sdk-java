/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.rules;

import org.jspecify.annotations.Nullable;

/**
 * The modulo 11 check digit used by SIFEN for the CDC and for the RUC.
 *
 * <p>Source: Manual Técnico v150, section 10.2 (check digit of the CDC, weights 2 to 11 from the
 * right; remainder 0 or 1 gives 0, otherwise 11 minus the remainder), verified against the Manual's
 * own example (RUC 44444401-7 and the CDC of section 10.1, case {@code conformidad/cdc}). The RUC
 * check digit fields (D102, D207, E625, E984, E996) cite the same "módulo 11".
 *
 * <p>Only digits are handled. A RUC with letters needs the ASCII conversion of rule {@code
 * R-A002-F1}, which contradiction C-102 leaves open, so such values are not checked.
 */
final class CheckDigit {

  private CheckDigit() {}

  /**
   * Computes the check digit of a string of digits.
   *
   * @param digits only ASCII digits, not empty
   * @return the check digit, or null if the input has anything other than digits
   */
  static @Nullable Integer modulo11(String digits) {
    if (digits.isEmpty()) {
      return null;
    }
    int total = 0;
    int weight = 2;
    for (int i = digits.length() - 1; i >= 0; i--) {
      char c = digits.charAt(i);
      if (c < '0' || c > '9') {
        return null;
      }
      total += (c - '0') * weight;
      weight = weight == 11 ? 2 : weight + 1;
    }
    int remainder = total % 11;
    return remainder <= 1 ? 0 : 11 - remainder;
  }
}
