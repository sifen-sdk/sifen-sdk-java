/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.rules;

import io.github.sifensdk.core.internal.spec.Rule;
import io.github.sifensdk.core.internal.tree.Node;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Java implementations of the {@code aritmetica} rules of the electronic document that apply to
 * invoices (C002 = 1), self-invoices (C002 = 4) and credit and debit notes (C002 = 5 and 6),
 * registered by rule id ({@code spec/reglas/de-e8-*.json} and {@code
 * spec/reglas/de-f-totales.json}, every version with its validity; the engine selects the version
 * in force).
 *
 * <h2>Comparison semantics (ADR 0010)</h2>
 *
 * <ul>
 *   <li>A value is <b>never</b> modified, rounded or recomputed in the document; the check only
 *       compares.
 *   <li>The formula is evaluated <b>exactly</b> (fractions, no intermediate rounding) on the values
 *       as reported.
 *   <li>A reported value is correct when its difference with the exact result is at most the rule's
 *       documented tolerance ({@code holgura}: 0.8 in the discount percentages, validations 1861
 *       and 1862) or, without one, at most 0.5 in the unit of the field: {@code |reported - exact|
 *       <= 0.5}. This is what SIFEN does in its test environment (specification evidence E-001): in
 *       guaraníes and in dollars it accepts 0.50 and rejects 0.51; with a tolerance of 0.8 it
 *       accepts 0.80 and rejects 0.81, without adding the 0.5.
 *   <li>Sums over items use each item's reported values, never recalculated ones.
 *   <li>An optional operand that is absent (discounts and advances per item, commission, rounding
 *       VAT) counts as 0. When a required operand is absent or not a number, the rule is not
 *       checked: the presence rules or the XSD report it.
 *   <li>Self-invoices (C002 = 4): the item total is {@code R-EA008-A2} and the gross total {@code
 *       R-F008-A3}; the specification's conditions of {@code R-EA008-A1} (C-305, evidence E-003)
 *       and {@code R-F008-A1} (C-517, evidence E-002) exclude them.
 *   <li>Rules the specification records as {@code descartada} are never evaluated: SIFEN does not
 *       apply them ({@code R-F017-A2}: SIFEN verifies the field table's formula, {@code R-F017-A1},
 *       with code 2371, C-502).
 *   <li>Events: the size of an invalidated range ({@code R-GEI006-A3}: up to 1000 numbers, C-612,
 *       E-003) and the reception date of the receiver's events, never before the issue date ({@code
 *       R-GEN004-A1}, {@code R-GED004-A1}).
 * </ul>
 *
 * <p>Not implemented, so reported as unchecked when they apply: {@code R-F013-A1} (rounding to
 * multiples of 50 is optional, F013 may be 0, there is no validation code, and SIFEN does not
 * verify it, E-001), {@code R-F026-A1} (formula not written; SIFEN does not verify it, E-001),
 * {@code R-F036-A1} and {@code R-F037-A1} (C-504; SIFEN does not verify F037, E-001), {@code
 * R-F008-A2} (C-501), {@code R-F023-A3} (F023 = F014 for C002 = 4: removed by the NT 8 without a
 * published date, so never selected while R-F023-A1 and R-F023-A2 are in force; F023 is prohibited
 * with C002 = 4 anyway, R-F023-P4), {@code R-H004-A1} (needs other documents in SIFEN) and {@code
 * R-E797-A0} (operands are a vehicle type code and a capacity, corrected by the NT 23).
 */
final class ArithmeticChecks {

  private static final Rational HUNDRED = Rational.of(100);
  private static final Rational TEN_THOUSAND = Rational.of(10000);
  private static final BigDecimal FIVE = BigDecimal.valueOf(5);
  private static final BigDecimal HALF = new BigDecimal("0.5");
  private static final BigDecimal THOUSAND = BigDecimal.valueOf(1000);

  private ArithmeticChecks() {}

  static Map<String, Check> all() {
    Map<String, Check> m = new HashMap<>();

    // --- E8.1: item values ----------------------------------------------------------------------
    m.put(
        "R-E727-A1",
        (e, r, t) -> verify(r, t, product(num(e, t, "E721"), num(e, t, "E711")), "E721 × E711"));
    m.put(
        "R-EA003-A1",
        (e, r, t) ->
            verify(
                r,
                t,
                quotient(product(num(e, t, "EA002"), HUNDRED), num(e, t, "E721")),
                "EA002 × 100 / E721"));
    m.put(
        "R-EA004-A2",
        (e, r, t) ->
            verify(
                r,
                t,
                quotient(product(num(e, t, "F010"), num(e, t, "E721")), HUNDRED),
                "F010 × E721 / 100"));
    m.put(
        "R-EA008-A1",
        (e, r, t) -> {
          // Not for self-invoices: the specification's condition excludes C002 = 4 (C-305, E-003).
          Rational net =
              difference(
                  num(e, t, "E721"),
                  orZero(e, t, "EA002"),
                  orZero(e, t, "EA004"),
                  orZero(e, t, "EA006"),
                  orZero(e, t, "EA007"));
          return verify(
              r,
              t,
              product(net, num(e, t, "E711")),
              "(E721 − EA002 − EA004 − EA006 − EA007) × E711");
        });
    m.put(
        "R-EA008-A2",
        (e, r, t) -> verify(r, t, product(num(e, t, "E721"), num(e, t, "E711")), "E721 × E711"));
    m.put(
        "R-EA009-A1",
        (e, r, t) -> verify(r, t, product(num(e, t, "EA008"), num(e, t, "E725")), "EA008 × E725"));

    // --- E8.2: item VAT -------------------------------------------------------------------------
    m.put("R-E735-A1", (e, r, t) -> verify(r, t, Rational.ZERO, "0"));
    m.put(
        "R-E735-A2",
        (e, r, t) -> verify(r, t, oldVatBase(e, t, "1.05"), "EA008 × (E733 / 100) / 1.05"));
    m.put(
        "R-E735-A3",
        (e, r, t) -> verify(r, t, oldVatBase(e, t, "1.1"), "EA008 × (E733 / 100) / 1.1"));
    m.put(
        "R-E735-A4",
        (e, r, t) ->
            verify(r, t, nt13Base(e, t, false), "100 × EA008 × E733 / (10000 + E734 × E733)"));
    m.put(
        "R-E735-A5",
        (e, r, t) ->
            verify(r, t, nt13Base(e, t, false), "100 × EA008 × E733 / (10000 + E734 × E733)"));
    m.put("R-E736-A1", (e, r, t) -> verify(r, t, Rational.ZERO, "0"));
    m.put(
        "R-E736-A2",
        (e, r, t) ->
            verify(
                r,
                t,
                quotient(product(num(e, t, "E735"), num(e, t, "E734")), HUNDRED),
                "E735 × (E734 / 100)"));
    m.put(
        "R-E737-A1",
        (e, r, t) ->
            verify(
                r, t, nt13Base(e, t, true), "100 × EA008 × (100 − E733) / (10000 + E734 × E733)"));
    m.put("R-E737-A2", (e, r, t) -> verify(r, t, Rational.ZERO, "0"));

    // --- E9: energy sector ----------------------------------------------------------------------
    m.put(
        "R-E797-A1",
        (e, r, t) -> verify(r, t, difference(num(e, t, "E796"), num(e, t, "E795")), "E796 − E795"));

    // --- F: totals
    // --------------------------------------------------------------------------------
    m.put(
        "R-F002-A1",
        (e, r, t) ->
            verify(r, t, sumItems(e, item -> exempt(e, item)), "sum of EA008 where E731 = 3"));
    m.put(
        "R-F002-A2",
        (e, r, t) ->
            verify(
                r,
                t,
                sumItems(
                    e,
                    item -> {
                      String affectation = e.textIn(item, "E731");
                      if ("3".equals(affectation)) {
                        return itemNum(e, item, "EA008");
                      }
                      if ("4".equals(affectation)) {
                        return itemNum(e, item, "E737");
                      }
                      return Rational.ZERO;
                    }),
                "sum of EA008 where E731 = 3 plus sum of E737 where E731 = 4"));
    m.put(
        "R-F003-A1",
        (e, r, t) ->
            verify(
                r,
                t,
                sumItems(
                    e,
                    item ->
                        "2".equals(e.textIn(item, "E731"))
                            ? itemNum(e, item, "EA008")
                            : Rational.ZERO),
                "sum of EA008 where E731 = 2"));
    m.put(
        "R-F004-A1", (e, r, t) -> verify(r, t, sumAtRate(e, FIVE), "sum of EA008 where E734 = 5"));
    m.put(
        "R-F005-A1",
        (e, r, t) -> verify(r, t, sumAtRate(e, BigDecimal.TEN), "sum of EA008 where E734 = 10"));
    m.put(
        "R-F004-A2",
        (e, r, t) ->
            verify(
                r,
                t,
                taxedAtRate(e, FIVE),
                "sum of EA008 where E734 = 5 and E731 = 1, plus E735 + E736 where E734 = 5 and"
                    + " E731 = 4"));
    m.put(
        "R-F005-A2",
        (e, r, t) ->
            verify(
                r,
                t,
                taxedAtRate(e, BigDecimal.TEN),
                "sum of EA008 where E734 = 10 and E731 = 1, plus E735 + E736 where E734 = 10 and"
                    + " E731 = 4"));
    m.put(
        "R-F008-A1",
        (e, r, t) -> {
          return verify(
              r,
              t,
              sum(
                  orZero(e, t, "F002"),
                  orZero(e, t, "F003"),
                  orZero(e, t, "F004"),
                  orZero(e, t, "F005")),
              "F002 + F003 + F004 + F005");
        });
    m.put(
        "R-F008-A3",
        (e, r, t) -> verify(r, t, sumItems(e, item -> itemNum(e, item, "EA008")), "sum of EA008"));
    m.put(
        "R-F009-A2", (e, r, t) -> verify(r, t, sumItemProducts(e, "EA002"), "sum of EA002 × E711"));
    m.put(
        "R-F033-A2", (e, r, t) -> verify(r, t, sumItemProducts(e, "EA004"), "sum of EA004 × E711"));
    m.put(
        "R-F034-A2", (e, r, t) -> verify(r, t, sumItemProducts(e, "EA006"), "sum of EA006 × E711"));
    m.put(
        "R-F035-A2", (e, r, t) -> verify(r, t, sumItemProducts(e, "EA007"), "sum of EA007 × E711"));
    m.put(
        "R-F011-A2",
        (e, r, t) -> verify(r, t, sum(num(e, t, "F009"), num(e, t, "F033")), "F009 + F033"));
    m.put(
        "R-F012-A2",
        (e, r, t) -> verify(r, t, sum(num(e, t, "F034"), num(e, t, "F035")), "F034 + F035"));
    m.put(
        "R-F014-A1",
        // Only where R-F014-A3 does not apply: the specification's condition excludes C002 = 1, 5,
        // 6.
        (e, r, t) -> verify(r, t, totalNet(e, t), "F008 − F013 + F025"));
    m.put("R-F014-A3", (e, r, t) -> verify(r, t, totalNet(e, t), "F008 − F013 + F025"));
    m.put(
        "R-F015-A1",
        (e, r, t) -> verify(r, t, sumVatAtRate(e, "E736", FIVE), "sum of E736 where E734 = 5"));
    m.put(
        "R-F016-A1",
        (e, r, t) ->
            verify(r, t, sumVatAtRate(e, "E736", BigDecimal.TEN), "sum of E736 where E734 = 10"));
    m.put(
        "R-F017-A1",
        (e, r, t) ->
            verify(
                r,
                t,
                sum(
                    orZero(e, t, "F015"),
                    orZero(e, t, "F016"),
                    negate(orZero(e, t, "F036")),
                    negate(orZero(e, t, "F037")),
                    orZero(e, t, "F026")),
                "F015 + F016 − F036 − F037 + F026"));
    m.put(
        "R-F017-A2",
        (e, r, t) -> verify(r, t, sum(orZero(e, t, "F015"), orZero(e, t, "F016")), "F015 + F016"));
    m.put(
        "R-F018-A1",
        (e, r, t) -> verify(r, t, sumVatAtRate(e, "E735", FIVE), "sum of E735 where E734 = 5"));
    m.put(
        "R-F019-A1",
        (e, r, t) ->
            verify(r, t, sumVatAtRate(e, "E735", BigDecimal.TEN), "sum of E735 where E734 = 10"));
    m.put(
        "R-F020-A1",
        (e, r, t) -> verify(r, t, sum(orZero(e, t, "F018"), orZero(e, t, "F019")), "F018 + F019"));
    m.put(
        "R-F023-A1",
        (e, r, t) -> verify(r, t, product(num(e, t, "F014"), num(e, t, "D018")), "F014 × D018"));
    m.put(
        "R-F023-A2",
        (e, r, t) -> verify(r, t, sumItems(e, item -> itemNum(e, item, "EA009")), "sum of EA009"));

    // --- Events --------------------------------------------------------------------------------
    m.put(
        "R-GEI006-A3",
        (e, r, t) -> {
          BigDecimal end = Evaluation.decimal(t.text());
          BigDecimal start = Evaluation.decimal(e.text(t, "GEI005"));
          if (end == null || start == null) {
            return null;
          }
          return end.subtract(start).add(BigDecimal.ONE).compareTo(THOUSAND) <= 0
              ? null
              : "The range has more than 1000 numbers (GEI006 − GEI005 + 1)";
        });
    m.put("R-GEN004-A1", (e, r, t) -> notBefore(t.text(), e.text(t, "GEN003")));
    m.put("R-GED004-A1", (e, r, t) -> notBefore(t.text(), e.text(t, "GED003")));

    return Map.copyOf(m);
  }

  /** A reception date and time never before the issue date and time of the document. */
  private static @Nullable String notBefore(@Nullable String reception, @Nullable String issue) {
    LocalDateTime received = dateTime(reception);
    LocalDateTime issued = dateTime(issue);
    if (received == null || issued == null || !received.isBefore(issued)) {
      return null;
    }
    return "The reception date is before the issue date of the document";
  }

  private static @Nullable LocalDateTime dateTime(@Nullable String text) {
    if (text == null) {
      return null;
    }
    try {
      return text.length() == 10 ? LocalDate.parse(text).atStartOfDay() : LocalDateTime.parse(text);
    } catch (DateTimeParseException ex) {
      return null;
    }
  }

  // --- comparison -------------------------------------------------------------------------------

  /**
   * Compares the reported value with the exact result. Returns null when it is a valid rounding
   * (plus the rule's tolerance) or when the result cannot be computed.
   */
  static @Nullable String verify(
      Rule rule, Occurrence target, @Nullable Rational exact, String expression) {
    BigDecimal reported = Evaluation.decimal(target.text());
    if (reported == null || exact == null) {
      return null;
    }
    BigDecimal tolerance = tolerance(rule);
    // What SIFEN accepts (spec FORMATO.md, evidence E-001): the documented tolerance when the rule
    // has one, otherwise half a unit of the field (0.5 guaraníes, 0.5 dollars, 0.5 points).
    BigDecimal allowed = tolerance.signum() > 0 ? tolerance : HALF;
    if (exact.isWithin(reported, allowed)) {
      return null;
    }
    return "Does not match "
        + expression
        + (tolerance.signum() > 0
            ? " (difference above the documented tolerance of " + tolerance.toPlainString() + ")"
            : " (difference above 0.5)");
  }

  private static BigDecimal tolerance(Rule rule) {
    Object t = rule.tolerance();
    return t instanceof BigDecimal && ((BigDecimal) t).signum() > 0
        ? (BigDecimal) t
        : BigDecimal.ZERO;
  }

  // --- operands ---------------------------------------------------------------------------------

  /** A required operand: null when absent or not a number. */
  private static @Nullable Rational num(Evaluation e, Occurrence context, String fieldId) {
    BigDecimal v = Evaluation.decimal(e.text(context, fieldId));
    return v == null ? null : Rational.of(v);
  }

  /** An optional operand: zero when absent, null when present but not a number. */
  private static @Nullable Rational orZero(Evaluation e, Occurrence context, String fieldId) {
    String text = e.text(context, fieldId);
    if (text == null) {
      return Rational.ZERO;
    }
    BigDecimal v = Evaluation.decimal(text);
    return v == null ? null : Rational.of(v);
  }

  private static @Nullable Rational itemNum(Evaluation e, Node item, String fieldId) {
    BigDecimal v = Evaluation.decimal(e.textIn(item, fieldId));
    return v == null ? null : Rational.of(v);
  }

  private static @Nullable Rational itemOrZero(Evaluation e, Node item, String fieldId) {
    String text = e.textIn(item, fieldId);
    if (text == null) {
      return Rational.ZERO;
    }
    BigDecimal v = Evaluation.decimal(text);
    return v == null ? null : Rational.of(v);
  }

  private static boolean rateIs(Evaluation e, Node item, BigDecimal rate) {
    BigDecimal v = Evaluation.decimal(e.textIn(item, "E734"));
    return v != null && v.compareTo(rate) == 0;
  }

  // --- arithmetic with absent operands ----------------------------------------------------------

  private static @Nullable Rational product(@Nullable Rational a, @Nullable Rational b) {
    return a == null || b == null ? null : a.multiply(b);
  }

  private static @Nullable Rational quotient(@Nullable Rational a, @Nullable Rational b) {
    return a == null || b == null ? null : a.divide(b);
  }

  private static @Nullable Rational negate(@Nullable Rational a) {
    return a == null ? null : a.negate();
  }

  private static @Nullable Rational sum(@Nullable Rational... terms) {
    Rational total = Rational.ZERO;
    for (Rational t : terms) {
      if (t == null) {
        return null;
      }
      total = total.add(t);
    }
    return total;
  }

  private static @Nullable Rational difference(
      @Nullable Rational first, @Nullable Rational... rest) {
    if (first == null) {
      return null;
    }
    Rational total = first;
    for (Rational t : rest) {
      if (t == null) {
        return null;
      }
      total = total.subtract(t);
    }
    return total;
  }

  // --- formulas ---------------------------------------------------------------------------------

  /** MT150 1910/1911 before the NT 13: EA008 × (E733 / 100) / divisor. */
  private static @Nullable Rational oldVatBase(Evaluation e, Occurrence t, String divisor) {
    Rational share = quotient(num(e, t, "E733"), HUNDRED);
    return quotient(product(num(e, t, "EA008"), share), Rational.of(divisor));
  }

  /**
   * NT 13: 100 × EA008 × E733 / (10000 + E734 × E733) for the taxed base (E735), or with (100 −
   * E733) in the numerator for the exempt base (E737).
   */
  private static @Nullable Rational nt13Base(Evaluation e, Occurrence t, boolean exemptPart) {
    Rational total = num(e, t, "EA008");
    Rational share = num(e, t, "E733");
    Rational rate = num(e, t, "E734");
    if (total == null || share == null || rate == null) {
      return null;
    }
    Rational numeratorShare = exemptPart ? HUNDRED.subtract(share) : share;
    Rational numerator = HUNDRED.multiply(total).multiply(numeratorShare);
    return numerator.divide(TEN_THOUSAND.add(rate.multiply(share)));
  }

  private static @Nullable Rational totalNet(Evaluation e, Occurrence t) {
    return sum(num(e, t, "F008"), negate(num(e, t, "F013")), orZero(e, t, "F025"));
  }

  /** A term of a sum over items; null aborts the sum (a required item value is missing). */
  @FunctionalInterface
  private interface ItemTerm {
    @Nullable Rational of(Node item);
  }

  private static @Nullable Rational sumItems(Evaluation e, ItemTerm term) {
    Rational total = Rational.ZERO;
    List<Occurrence> items = e.all("E700");
    for (Occurrence item : items) {
      Rational t = term.of(item.anchor());
      if (t == null) {
        return null;
      }
      total = total.add(t);
    }
    return total;
  }

  private static @Nullable Rational exempt(Evaluation e, Node item) {
    return "3".equals(e.textIn(item, "E731")) ? itemNum(e, item, "EA008") : Rational.ZERO;
  }

  /** Before the NT 13: sum of EA008 of the items at a rate. */
  private static @Nullable Rational sumAtRate(Evaluation e, BigDecimal rate) {
    return sumItems(e, item -> rateIs(e, item, rate) ? itemNum(e, item, "EA008") : Rational.ZERO);
  }

  /** NT 13: EA008 of fully taxed items plus E735 + E736 of partially taxed items, at a rate. */
  private static @Nullable Rational taxedAtRate(Evaluation e, BigDecimal rate) {
    return sumItems(
        e,
        item -> {
          if (!rateIs(e, item, rate)) {
            return Rational.ZERO;
          }
          String affectation = e.textIn(item, "E731");
          if ("1".equals(affectation)) {
            return itemNum(e, item, "EA008");
          }
          if ("4".equals(affectation)) {
            return sum(itemNum(e, item, "E735"), itemNum(e, item, "E736"));
          }
          return Rational.ZERO;
        });
  }

  private static @Nullable Rational sumVatAtRate(Evaluation e, String fieldId, BigDecimal rate) {
    return sumItems(e, item -> rateIs(e, item, rate) ? itemNum(e, item, fieldId) : Rational.ZERO);
  }

  /** Sum of an optional per-item amount (absent counts as 0) times the item quantity (E711). */
  private static @Nullable Rational sumItemProducts(Evaluation e, String fieldId) {
    return sumItems(e, item -> product(itemOrZero(e, item, fieldId), itemNum(e, item, "E711")));
  }
}
