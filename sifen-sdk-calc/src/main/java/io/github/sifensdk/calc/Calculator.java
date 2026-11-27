/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.calc;

import io.github.sifensdk.core.model.CommercialOperation;
import io.github.sifensdk.core.model.ElectronicDocument;
import io.github.sifensdk.core.model.Item;
import io.github.sifensdk.core.model.ItemDiscountsAndAdvances;
import io.github.sifensdk.core.model.ItemValues;
import io.github.sifensdk.core.model.ItemVat;
import io.github.sifensdk.core.model.Totals;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Optional reference calculator: derives the amounts of items and the totals of a document from
 * what the issuer decides (quantities, prices, discounts, advances, VAT), with the formulas of the
 * Manual Técnico v150 as the specification records them. The core never invokes it (ADR-0002,
 * ADR-0010); use it only if your system does not compute these amounts itself.
 *
 * <p>How it computes (horizontal, as SIFEN verifies it; evidence E-001 of the specification):
 *
 * <ul>
 *   <li>Each item amount is computed exactly from the item's values and rounded once, to the
 *       decimals of the currency ({@link Builder#decimals(String, int)}: 0 for {@code PYG} and 2
 *       for other currencies by default) with {@link RoundingMode#HALF_UP} unless configured
 *       otherwise. Amounts in guaraníes of a document in another currency (EA009, F023) have 0
 *       decimals.
 *   <li>A total that the Manual defines as a sum of item amounts is the exact sum of the rounded
 *       item amounts, never a value computed again over the whole document. Totals defined as sums
 *       of products (F009, F033, F034, F035) are computed exactly and rounded once.
 *   <li>Every result is therefore within 0.5 of the unit of its field from the exact formula over
 *       the reported values, which is what SIFEN and the core's checks accept (ADR-0010).
 * </ul>
 *
 * <p>Not computed: the excise tax (ISC, D013 = 2: F006 and F008-A2 are not defined, C-501), the
 * commission (F025, F026), and the VAT of the rounding (F036, F037), which SIFEN does not verify.
 * The rounding to multiples of 50 guaraníes (F013) is off by default ({@link
 * Builder#roundToFifty(boolean)}). Immutable and thread-safe.
 */
public final class Calculator {

  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
  private static final BigDecimal TEN_THOUSAND = BigDecimal.valueOf(10000);
  private static final BigDecimal FIFTY = BigDecimal.valueOf(50);
  private static final int DIVISION_SCALE = 30;
  private static final String GUARANI = "PYG";

  private final Map<String, Integer> decimals;
  private final int defaultDecimals;
  private final RoundingMode roundingMode;
  private final boolean roundToFifty;

  private Calculator(Builder b) {
    this.decimals = Map.copyOf(b.decimals);
    this.defaultDecimals = b.defaultDecimals;
    this.roundingMode = b.roundingMode;
    this.roundToFifty = b.roundToFifty;
  }

  /**
   * Starts a builder with the default settings.
   *
   * @return a builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * A calculator with the default settings: 0 decimals in guaraníes, 2 in other currencies, {@code
   * HALF_UP}, no rounding to multiples of 50.
   *
   * @return the calculator
   */
  public static Calculator defaults() {
    return builder().build();
  }

  /**
   * Derives the amounts of one item.
   *
   * <ul>
   *   <li>E727 = E721 × E711 (R-E727-A1).
   *   <li>EA003 = EA002 × 100 / E721 (R-EA003-A1), with 2 decimals, when there is a discount.
   *   <li>EA004 = F010 × E721 / 100 (R-EA004-A2).
   *   <li>EA008 = (E721 − EA002 − EA004 − EA006 − EA007) × E711 (R-EA008-A1), or E721 × E711 in a
   *       self-invoice (R-EA008-A2).
   *   <li>EA009 = EA008 × E725 (R-EA009-A1), when the item has an exchange rate.
   *   <li>E735 = 100 × EA008 × E733 / (10000 + E734 × E733) (R-E735-A4, A5), E736 = E735 × E734 /
   *       100 (R-E736-A2) and E737 = 100 × EA008 × (100 − E733) / (10000 + E734 × E733)
   *       (R-E737-A1); 0 when exonerated or exempt (R-E735-A1, R-E736-A1, R-E737-A2).
   * </ul>
   *
   * @param price what the issuer decides for the item
   * @return the item's values and VAT
   * @throws IllegalArgumentException if the discounts and advances exceed the unit price
   */
  public ItemAmounts item(ItemPrice price) {
    Objects.requireNonNull(price, "price");
    String currency = price.currencyCode();
    BigDecimal unitPrice = price.unitPrice();
    BigDecimal quantity = price.quantity();
    BigDecimal discount = round(price.discountPerUnit(), currency);
    BigDecimal globalDiscount =
        price.globalDiscountRate().signum() == 0 || unitPrice.signum() == 0
            ? zero(currency)
            : round(price.globalDiscountRate().multiply(unitPrice).divide(HUNDRED), currency);
    BigDecimal advance = round(price.advancePerUnit(), currency);
    BigDecimal globalAdvance = round(price.globalAdvancePerUnit(), currency);
    boolean selfInvoice = price.vatTreatmentCode() == null;

    BigDecimal net = unitPrice.subtract(discount).subtract(globalDiscount).subtract(advance);
    net = net.subtract(globalAdvance);
    if (net.signum() < 0) {
      throw new IllegalArgumentException(
          "The discounts and advances per unit exceed the unit price (EA008 would be negative)");
    }
    BigDecimal total = round((selfInvoice ? unitPrice : net).multiply(quantity), currency);
    BigDecimal exchangeRate = price.exchangeRate();
    BigDecimal guaraniTotal =
        exchangeRate == null ? null : round(total.multiply(exchangeRate), GUARANI);

    ItemDiscountsAndAdvances.Builder discounts =
        ItemDiscountsAndAdvances.builder()
            .discountAmount(discount)
            .globalDiscountAmount(globalDiscount)
            .advanceAmount(advance)
            .globalAdvanceAmount(globalAdvance)
            .total(total)
            .guaraniTotal(guaraniTotal);
    if (discount.signum() != 0 && unitPrice.signum() != 0) {
      discounts.discountRate(discount.multiply(HUNDRED).divide(unitPrice, 2, RoundingMode.HALF_UP));
    }
    ItemValues values =
        ItemValues.builder()
            .unitPrice(unitPrice)
            .exchangeRate(exchangeRate)
            .grossTotal(round(unitPrice.multiply(quantity), currency))
            .discountsAndAdvances(discounts.build())
            .build();
    return new ItemAmounts(values, selfInvoice ? null : vat(price, total, currency));
  }

  private ItemVat vat(ItemPrice price, BigDecimal total, String currency) {
    String treatment = Objects.requireNonNull(price.vatTreatmentCode());
    BigDecimal rate = price.vatRate();
    BigDecimal proportion = price.taxableProportionRate();
    BigDecimal base = zero(currency);
    BigDecimal amount = zero(currency);
    BigDecimal exempt = zero(currency);
    if (treatment.equals("1") || treatment.equals("4")) {
      BigDecimal divisor = TEN_THOUSAND.add(rate.multiply(proportion));
      base = round(divide(HUNDRED.multiply(total).multiply(proportion), divisor), currency);
      amount = round(base.multiply(rate).divide(HUNDRED), currency);
      if (treatment.equals("4")) {
        BigDecimal untaxed = HUNDRED.subtract(proportion);
        exempt = round(divide(HUNDRED.multiply(total).multiply(untaxed), divisor), currency);
      }
    }
    return ItemVat.builder()
        .treatmentCode(treatment)
        .taxableProportionRate(proportion)
        .rate(rate)
        .taxableBase(base)
        .amount(amount)
        .exemptBase(exempt)
        .build();
  }

  /**
   * Computes the totals of a document (group F) from its items as they are, without a global
   * discount (F010 = 0).
   *
   * @param document the document with all its items
   * @return the totals
   * @throws IllegalArgumentException if the document has no amounts (delivery note), uses the
   *     excise tax (D013 = 2) or an item lacks its values or VAT
   */
  public Totals totals(ElectronicDocument document) {
    return totals(document, BigDecimal.ZERO);
  }

  /**
   * Computes the totals of a document (group F) from its items as they are.
   *
   * <ul>
   *   <li>F002, F003, F004, F005: sums of EA008 by VAT treatment and rate, with E737 and E735 +
   *       E736 of partially taxed items (R-F002-A2, R-F003-A1, R-F004-A2, R-F005-A2).
   *   <li>F008 = F002 + F003 + F004 + F005 (R-F008-A1), or the sum of EA008 in a self-invoice
   *       (R-F008-A3).
   *   <li>F009, F033, F034, F035: sums of EA002, EA004, EA006 and EA007 × E711 (R-F009-A2,
   *       R-F033-A2, R-F034-A2, R-F035-A2); F011 = F009 + F033, F012 = F034 + F035.
   *   <li>F013: the rounding to multiples of 50 guaraníes, when enabled; otherwise 0.
   *   <li>F014 = F008 − F013 (R-F014-A3, without commission).
   *   <li>F015, F016: sums of E736 by rate; F018, F019: sums of E735 by rate; F020 = F018 + F019;
   *       F017 = F015 + F016 (R-F017-A1 without rounding VAT nor commission).
   *   <li>F023 = F014 × D018 (R-F023-A1) or the sum of EA009 (R-F023-A2), in a foreign currency.
   * </ul>
   *
   * @param document the document with all its items
   * @param globalDiscountRate the global discount rate (F010) the items were computed with
   * @return the totals
   * @throws IllegalArgumentException if the document has no amounts (delivery note), uses the
   *     excise tax (D013 = 2) or an item lacks its values or VAT
   */
  public Totals totals(ElectronicDocument document, BigDecimal globalDiscountRate) {
    Objects.requireNonNull(document, "document");
    Objects.requireNonNull(globalDiscountRate, "globalDiscountRate");
    String type = document.stamp().documentTypeCode();
    if (type.equals("7")) {
      throw new IllegalArgumentException("A delivery note (C002 = 7) has no totals");
    }
    boolean selfInvoice = type.equals("4");
    CommercialOperation operation = document.generalData().commercialOperation();
    String currency = operation == null ? GUARANI : operation.currencyCode();
    if (operation != null && operation.taxTypeCode().equals("2")) {
      throw new IllegalArgumentException(
          "The excise tax (ISC, D013 = 2) is not computed: its totals are not defined (C-501)");
    }

    BigDecimal exempt = zero(currency);
    BigDecimal exonerated = zero(currency);
    BigDecimal sub5 = zero(currency);
    BigDecimal sub10 = zero(currency);
    BigDecimal itemTotals = zero(currency);
    BigDecimal discounts = BigDecimal.ZERO;
    BigDecimal globalDiscounts = BigDecimal.ZERO;
    BigDecimal advances = BigDecimal.ZERO;
    BigDecimal globalAdvances = BigDecimal.ZERO;
    BigDecimal vat5 = zero(currency);
    BigDecimal vat10 = zero(currency);
    BigDecimal base5 = zero(currency);
    BigDecimal base10 = zero(currency);
    BigDecimal guaranies = BigDecimal.ZERO;
    for (Item item : document.documentTypeData().items()) {
      ItemValues values = item.valuation();
      if (values == null) {
        throw new IllegalArgumentException("An item has no values (E720)");
      }
      ItemDiscountsAndAdvances d = values.discountsAndAdvances();
      BigDecimal total = d.total();
      BigDecimal quantity = item.quantity();
      itemTotals = itemTotals.add(total);
      discounts = discounts.add(orZero(d.discountAmount()).multiply(quantity));
      globalDiscounts = globalDiscounts.add(orZero(d.globalDiscountAmount()).multiply(quantity));
      advances = advances.add(orZero(d.advanceAmount()).multiply(quantity));
      globalAdvances = globalAdvances.add(orZero(d.globalAdvanceAmount()).multiply(quantity));
      guaranies = guaranies.add(orZero(d.guaraniTotal()));
      if (selfInvoice) {
        continue;
      }
      ItemVat vat = item.vat();
      if (vat == null) {
        throw new IllegalArgumentException("An item has no VAT group (E730)");
      }
      boolean five = vat.rate().compareTo(BigDecimal.valueOf(5)) == 0;
      switch (vat.treatmentCode()) {
        case "1":
          if (five) {
            sub5 = sub5.add(total);
          } else {
            sub10 = sub10.add(total);
          }
          break;
        case "2":
          exonerated = exonerated.add(total);
          break;
        case "3":
          exempt = exempt.add(total);
          break;
        case "4":
          exempt = exempt.add(vat.exemptBase());
          if (five) {
            sub5 = sub5.add(vat.taxableBase()).add(vat.amount());
          } else {
            sub10 = sub10.add(vat.taxableBase()).add(vat.amount());
          }
          break;
        default:
          throw new IllegalArgumentException("Unknown VAT treatment (E731)");
      }
      if (five) {
        vat5 = vat5.add(vat.amount());
        base5 = base5.add(vat.taxableBase());
      } else {
        vat10 = vat10.add(vat.amount());
        base10 = base10.add(vat.taxableBase());
      }
    }

    BigDecimal gross = selfInvoice ? itemTotals : exempt.add(exonerated).add(sub5).add(sub10);
    BigDecimal itemDiscounts = round(discounts, currency);
    BigDecimal itemGlobalDiscounts = round(globalDiscounts, currency);
    BigDecimal itemAdvances = round(advances, currency);
    BigDecimal itemGlobalAdvances = round(globalAdvances, currency);
    BigDecimal rounding =
        roundToFifty && currency.equals(GUARANI) ? gross.remainder(FIFTY) : zero(currency);
    BigDecimal net = gross.subtract(rounding);

    Totals.Builder totals =
        Totals.builder()
            .grossTotal(gross)
            .itemDiscountTotal(itemDiscounts)
            .itemGlobalDiscountTotal(itemGlobalDiscounts)
            .itemAdvanceTotal(itemAdvances)
            .itemGlobalAdvanceTotal(itemGlobalAdvances)
            .globalDiscountRate(globalDiscountRate)
            .discountTotal(itemDiscounts.add(itemGlobalDiscounts))
            .advanceTotal(itemAdvances.add(itemGlobalAdvances))
            .roundingAmount(rounding)
            .netTotal(net);
    if (selfInvoice) {
      return totals.build();
    }
    totals
        .exemptSubtotal(exempt)
        .exoneratedSubtotal(exonerated)
        .vat5Subtotal(sub5)
        .vat10Subtotal(sub10)
        .vat5Amount(vat5)
        .vat10Amount(vat10)
        .vatTotal(vat5.add(vat10))
        .vat5TaxableBaseTotal(base5)
        .vat10TaxableBaseTotal(base10)
        .vatTaxableBaseTotal(base5.add(base10));
    if (!currency.equals(GUARANI) && operation != null) {
      if ("2".equals(operation.exchangeRateConditionCode())) {
        totals.guaraniTotal(guaranies);
      } else if (operation.exchangeRate() != null) {
        totals.guaraniTotal(round(net.multiply(operation.exchangeRate()), GUARANI));
      }
    }
    return totals.build();
  }

  /**
   * Returns a copy of the document with the totals computed by {@link #totals(ElectronicDocument,
   * BigDecimal)}, replacing any it had. The document passed is not changed.
   *
   * @param document the document with all its items
   * @param globalDiscountRate the global discount rate (F010) the items were computed with
   * @return the document with its totals
   */
  public ElectronicDocument withTotals(ElectronicDocument document, BigDecimal globalDiscountRate) {
    return document.toBuilder().totals(totals(document, globalDiscountRate)).build();
  }

  /**
   * Returns a copy of the document with its totals, without a global discount. See {@link
   * #withTotals(ElectronicDocument, BigDecimal)}.
   *
   * @param document the document with all its items
   * @return the document with its totals
   */
  public ElectronicDocument withTotals(ElectronicDocument document) {
    return withTotals(document, BigDecimal.ZERO);
  }

  private int decimalsOf(String currency) {
    Integer d = decimals.get(currency.toUpperCase(Locale.ROOT));
    if (d != null) {
      return d;
    }
    return currency.equalsIgnoreCase(GUARANI) ? 0 : defaultDecimals;
  }

  private BigDecimal round(BigDecimal value, String currency) {
    return value.setScale(decimalsOf(currency), roundingMode);
  }

  private BigDecimal zero(String currency) {
    return BigDecimal.ZERO.setScale(decimalsOf(currency));
  }

  private static BigDecimal divide(BigDecimal dividend, BigDecimal divisor) {
    return dividend.divide(divisor, DIVISION_SCALE, RoundingMode.HALF_EVEN);
  }

  private static BigDecimal orZero(@Nullable BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  /** Builder for {@link Calculator}. Not thread-safe. */
  public static final class Builder {

    private final Map<String, Integer> decimals = new HashMap<>();
    private int defaultDecimals = 2;
    private RoundingMode roundingMode = RoundingMode.HALF_UP;
    private boolean roundToFifty;

    private Builder() {}

    /**
     * Sets the decimals of the amounts in a currency.
     *
     * @param currencyCode the ISO 4217 code
     * @param decimals from 0 to 8 (the XSD allows up to 8)
     * @return this builder
     * @throws IllegalArgumentException if out of range
     */
    public Builder decimals(String currencyCode, int decimals) {
      this.decimals.put(
          Objects.requireNonNull(currencyCode).toUpperCase(Locale.ROOT), check(decimals));
      return this;
    }

    /**
     * Sets the decimals of currencies other than the guaraní without their own setting (default 2).
     *
     * @param decimals from 0 to 8
     * @return this builder
     */
    public Builder defaultDecimals(int decimals) {
      this.defaultDecimals = check(decimals);
      return this;
    }

    /**
     * Sets how each amount is rounded to its decimals (default {@link RoundingMode#HALF_UP}).
     *
     * <p>Only {@code HALF_UP}, {@code HALF_DOWN} and {@code HALF_EVEN}: they keep every amount
     * within half a unit of its exact value, which SIFEN requires (ADR-0010). Truncating or
     * rounding always up or down can miss by almost a unit, and SIFEN rejects it.
     *
     * @param roundingMode a half-way mode
     * @return this builder
     * @throws IllegalArgumentException for any other mode
     */
    public Builder roundingMode(RoundingMode roundingMode) {
      switch (Objects.requireNonNull(roundingMode)) {
        case HALF_UP:
        case HALF_DOWN:
        case HALF_EVEN:
          this.roundingMode = roundingMode;
          return this;
        default:
          throw new IllegalArgumentException(
              "Only HALF_UP, HALF_DOWN or HALF_EVEN keep amounts within half a unit");
      }
    }

    /**
     * Whether to round the total of documents in guaraníes down to a multiple of 50 (F013, Manual
     * Técnico v150, example 107437 → 107400). Off by default; SIFEN does not verify it.
     *
     * @param roundToFifty whether to round
     * @return this builder
     */
    public Builder roundToFifty(boolean roundToFifty) {
      this.roundToFifty = roundToFifty;
      return this;
    }

    private static int check(int decimals) {
      if (decimals < 0 || decimals > 8) {
        throw new IllegalArgumentException("Decimals go from 0 to 8");
      }
      return decimals;
    }

    /**
     * Builds the immutable calculator.
     *
     * @return the calculator
     */
    public Calculator build() {
      return new Calculator(this);
    }
  }
}
