/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.cdc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * The control code (CDC, código de control) of an electronic document: field {@code Id} of {@code
 * DE} (A002) plus its check digit (A003).
 *
 * <p>44 characters, in this order (Manual Técnico v150, 10.1 and validation 1000): document type
 * C002 (2), issuer RUC D101 (8, zero-padded on the left), issuer RUC check digit D102 (1),
 * establishment C005 (3), expedition point C006 (3), document number C007 (7), taxpayer type D103
 * (1), issue date D002 ({@code yyyyMMdd}, 8), emission type B002 (1), security code B004 (9) and
 * the check digit (1).
 *
 * <p><b>RUC ending in a letter (contradiction C-102, pending verification).</b> The XSD type {@code
 * tCDC} ({@code [0-9]{2}([0-9]{7}[0-9A-D])[0-9]{34}}) accepts a letter A to D as the last character
 * of the RUC inside the CDC, while the Manual (A002) says the letter is converted to its ASCII code
 * "for the check digit and the generation of the CDC". Writing a two-digit ASCII code into the CDC
 * would break its length of 44, so this class follows the XSD for the structure (the letter stays
 * as is in the CDC) and the Manual for the check digit (the letter is replaced by its ASCII code
 * only to compute it, see {@link #checkDigit(String)}). This combination has not been verified
 * against SIFEN yet.
 *
 * <p>Immutable and thread-safe. {@link #toString()} returns the CDC itself: it is the public
 * identifier of the document, printed on the KuDE.
 */
public final class Cdc {

  /** Length of a CDC (A002). */
  public static final int LENGTH = 44;

  /** XSD type {@code tCDC} (DE_Types_v150.xsd). */
  private static final Pattern CDC_PATTERN = Pattern.compile("[0-9]{2}[0-9]{7}[0-9A-D][0-9]{34}");

  /** XSD type {@code tRuc}, limited to the 8 positions of the CDC. */
  private static final Pattern RUC_PATTERN = Pattern.compile("[0-9]{0,7}[0-9A-D]");

  private static final Pattern DIGITS = Pattern.compile("[0-9]+");

  private static final DateTimeFormatter CDC_DATE =
      DateTimeFormatter.ofPattern("uuuuMMdd", Locale.ROOT);

  private final String value;

  private Cdc(String value) {
    this.value = value;
  }

  /**
   * Parses and verifies a CDC.
   *
   * @param value the 44 characters of the CDC
   * @return the CDC
   * @throws IllegalArgumentException if the value does not have 44 characters, does not match the
   *     XSD type {@code tCDC} or its check digit (A003) is wrong; the message does not contain the
   *     value
   */
  public static Cdc parse(String value) {
    Objects.requireNonNull(value, "value");
    if (value.length() != LENGTH) {
      throw new IllegalArgumentException(
          "A CDC has " + LENGTH + " characters, got " + value.length());
    }
    if (!CDC_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("The CDC does not match the XSD type tCDC");
    }
    int expected = checkDigit(value.substring(0, LENGTH - 1));
    if (value.charAt(LENGTH - 1) - '0' != expected) {
      throw new IllegalArgumentException("The check digit of the CDC (A003) is wrong");
    }
    return new Cdc(value);
  }

  /**
   * Composes a CDC from its parts and computes its check digit (Manual Técnico v150, 10.1 and
   * 10.2).
   *
   * <p>The document type, the RUC and the document number are padded with zeros on the left to 2, 8
   * and 7 positions (validation 1000). Nothing else is completed or transformed.
   *
   * @param documentType document type, C002 ({@code iTiDE}), 1 or 2 digits
   * @param issuerRuc issuer RUC without check digit, D101 ({@code dRucEm}), up to 8 characters; the
   *     last one may be a letter A to D (see the class description)
   * @param issuerRucCheckDigit check digit of the issuer RUC, D102 ({@code dDVEmi}), 1 digit
   * @param establishment establishment, C005 ({@code dEst}), 3 digits
   * @param expeditionPoint expedition point, C006 ({@code dPunExp}), 3 digits
   * @param documentNumber document number, C007 ({@code dNumDoc}), up to 7 digits
   * @param taxpayerType taxpayer type of the issuer, D103 ({@code iTipCont}), 1 digit
   * @param issueDate date of issue, from D002 ({@code dFeEmiDE})
   * @param emissionType emission type, B002 ({@code iTipEmi}), 1 digit
   * @param securityCode security code, B004 ({@code dCodSeg}), 9 digits
   * @return the CDC
   * @throws IllegalArgumentException if a part has the wrong length or characters
   */
  public static Cdc compose(
      String documentType,
      String issuerRuc,
      String issuerRucCheckDigit,
      String establishment,
      String expeditionPoint,
      String documentNumber,
      String taxpayerType,
      LocalDate issueDate,
      String emissionType,
      String securityCode) {
    Objects.requireNonNull(issueDate, "issueDate");
    String ruc = leftPad(Objects.requireNonNull(issuerRuc, "issuerRuc"), 8, "issuerRuc (D101)");
    if (!RUC_PATTERN.matcher(ruc).matches()) {
      throw new IllegalArgumentException(
          "issuerRuc (D101) must be digits, optionally ending in a letter A to D");
    }
    String date = CDC_DATE.format(issueDate);
    if (date.length() != 8) {
      throw new IllegalArgumentException("issueDate (D002) must have a 4-digit year");
    }
    String base =
        digits(leftPad(documentType, 2, "documentType (C002)"), 2, "documentType (C002)")
            + ruc
            + digits(issuerRucCheckDigit, 1, "issuerRucCheckDigit (D102)")
            + digits(establishment, 3, "establishment (C005)")
            + digits(expeditionPoint, 3, "expeditionPoint (C006)")
            + digits(
                leftPad(documentNumber, 7, "documentNumber (C007)"), 7, "documentNumber (C007)")
            + digits(taxpayerType, 1, "taxpayerType (D103)")
            + date
            + digits(emissionType, 1, "emissionType (B002)")
            + digits(securityCode, 9, "securityCode (B004)");
    return new Cdc(base + checkDigit(base));
  }

  /**
   * Computes the check digit of the first 43 characters of a CDC (A003, Manual Técnico v150, 10.2).
   *
   * <p>Modulus 11 as in the SET algorithm the Manual refers to: each letter is first replaced by
   * its ASCII code (Manual A002, contradiction C-102); then, from the rightmost digit, the digits
   * are multiplied by the weights 2, 3, …, 11, starting again at 2 after 11; the products are added
   * and the remainder of dividing by 11 is taken. A remainder greater than 1 gives {@code 11 -
   * remainder}; 0 or 1 give 0.
   *
   * @param base43 the 43 characters before the check digit (digits, and letters A to D)
   * @return the check digit, 0 to 9
   * @throws IllegalArgumentException if the base does not have 43 characters or has other
   *     characters
   */
  public static int checkDigit(String base43) {
    Objects.requireNonNull(base43, "base43");
    if (base43.length() != LENGTH - 1) {
      throw new IllegalArgumentException(
          "The base of the check digit has "
              + (LENGTH - 1)
              + " characters, got "
              + base43.length());
    }
    StringBuilder numeric = new StringBuilder(LENGTH + 2);
    for (int i = 0; i < base43.length(); i++) {
      char c = base43.charAt(i);
      if (c >= '0' && c <= '9') {
        numeric.append(c);
      } else if (c >= 'A' && c <= 'D') {
        numeric.append((int) c);
      } else {
        throw new IllegalArgumentException(
            "The base of the check digit may only have digits and letters A to D");
      }
    }
    int weight = 2;
    int sum = 0;
    for (int i = numeric.length() - 1; i >= 0; i--) {
      sum += (numeric.charAt(i) - '0') * weight;
      weight = weight == 11 ? 2 : weight + 1;
    }
    int remainder = sum % 11;
    return remainder > 1 ? 11 - remainder : 0;
  }

  /**
   * Returns the 44 characters of the CDC.
   *
   * @return the CDC
   */
  public String value() {
    return value;
  }

  /**
   * Returns the CDC in groups of four characters separated by a space, as it must be shown on the
   * KuDE (Manual Técnico v150, 10.1).
   *
   * @return for example {@code 0144 4444 0170 0100 1001 4528 2201 7012 5158 7326 0988}
   */
  public String kudeFormat() {
    StringBuilder out = new StringBuilder(LENGTH + LENGTH / 4);
    for (int i = 0; i < LENGTH; i += 4) {
      if (i > 0) {
        out.append(' ');
      }
      out.append(value, i, i + 4);
    }
    return out.toString();
  }

  /**
   * Returns the document type (C002), 2 digits.
   *
   * @return the document type as written in the CDC
   */
  public String documentType() {
    return value.substring(0, 2);
  }

  /**
   * Returns the issuer RUC without check digit (D101), 8 characters as written in the CDC.
   *
   * @return the RUC, zero-padded on the left
   */
  public String issuerRuc() {
    return value.substring(2, 10);
  }

  /**
   * Returns the check digit of the issuer RUC (D102).
   *
   * @return one digit
   */
  public String issuerRucCheckDigit() {
    return value.substring(10, 11);
  }

  /**
   * Returns the establishment (C005).
   *
   * @return three digits
   */
  public String establishment() {
    return value.substring(11, 14);
  }

  /**
   * Returns the expedition point (C006).
   *
   * @return three digits
   */
  public String expeditionPoint() {
    return value.substring(14, 17);
  }

  /**
   * Returns the document number (C007).
   *
   * @return seven digits
   */
  public String documentNumber() {
    return value.substring(17, 24);
  }

  /**
   * Returns the taxpayer type of the issuer (D103).
   *
   * @return one digit
   */
  public String taxpayerType() {
    return value.substring(24, 25);
  }

  /**
   * Returns the date of issue as written in the CDC (from D002).
   *
   * @return eight digits, {@code yyyyMMdd}
   */
  public String issueDate() {
    return value.substring(25, 33);
  }

  /**
   * Returns the emission type (B002).
   *
   * @return one digit
   */
  public String emissionType() {
    return value.substring(33, 34);
  }

  /**
   * Returns the security code (B004).
   *
   * @return nine digits
   */
  public String securityCode() {
    return value.substring(34, 43);
  }

  /**
   * Returns the check digit of the CDC (A003).
   *
   * @return one digit
   */
  public String checkDigit() {
    return value.substring(43);
  }

  @Override
  public boolean equals(@Nullable Object other) {
    return other instanceof Cdc && ((Cdc) other).value.equals(value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  /** Returns the 44 characters of the CDC. */
  @Override
  public String toString() {
    return value;
  }

  private static String leftPad(@Nullable String text, int length, String name) {
    Objects.requireNonNull(text, name);
    if (text.isEmpty() || text.length() > length) {
      throw new IllegalArgumentException(name + " must have 1 to " + length + " characters");
    }
    return "0".repeat(length - text.length()) + text;
  }

  private static String digits(@Nullable String text, int length, String name) {
    Objects.requireNonNull(text, name);
    if (text.length() != length || !DIGITS.matcher(text).matches()) {
      throw new IllegalArgumentException(name + " must have exactly " + length + " digits");
    }
    return text;
  }
}
