/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.mapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.jspecify.annotations.Nullable;

/** Text formats of the tree: values are written as given, never rounded or rescaled. */
final class Format {

  /** {@code AAAA-MM-DDThh:mm:ss}, local time of Paraguay without zone (MT150, D002 and A004). */
  private static final DateTimeFormatter DATE_TIME =
      DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");

  private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("uuuu-MM-dd");

  private Format() {}

  /** An amount or quantity exactly as received: same digits and same scale, no exponent. */
  static String amount(BigDecimal value) {
    return value.toPlainString();
  }

  static @Nullable String amountOrNull(@Nullable BigDecimal value) {
    return value == null ? null : amount(value);
  }

  static String integer(Integer value) {
    return value.toString();
  }

  static @Nullable String integerOrNull(@Nullable Integer value) {
    return value == null ? null : integer(value);
  }

  static String date(LocalDate value) {
    return DATE.format(value);
  }

  static @Nullable String dateOrNull(@Nullable LocalDate value) {
    return value == null ? null : date(value);
  }

  static String dateTime(LocalDateTime value) {
    return DATE_TIME.format(value);
  }

  static @Nullable String dateTimeOrNull(@Nullable LocalDateTime value) {
    return value == null ? null : dateTime(value);
  }
}
