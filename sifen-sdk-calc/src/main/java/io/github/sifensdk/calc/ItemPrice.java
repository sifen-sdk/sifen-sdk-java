/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.calc;

import java.math.BigDecimal;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * What the issuer decides for one item, from which {@link Calculator#item(ItemPrice)} derives the
 * rest: quantity, unit price, discounts and advances per unit, VAT treatment and rate, and the
 * item's exchange rate.
 *
 * <p>Amounts are per unit and in the currency of the document. Without a VAT treatment the item is
 * one of a self-invoice (C002 = 4), which has no VAT group nor discounts (R-E730-P1, R-EA008-A2).
 * Immutable and thread-safe; create it with {@link #builder()}.
 */
public final class ItemPrice {

  private final String currencyCode;
  private final BigDecimal quantity;
  private final BigDecimal unitPrice;
  private final BigDecimal discountPerUnit;
  private final BigDecimal globalDiscountRate;
  private final BigDecimal advancePerUnit;
  private final BigDecimal globalAdvancePerUnit;
  private final @Nullable BigDecimal exchangeRate;
  private final @Nullable String vatTreatmentCode;
  private final BigDecimal vatRate;
  private final BigDecimal taxableProportionRate;

  private ItemPrice(Builder b) {
    this.currencyCode = Objects.requireNonNull(b.currencyCode, "currencyCode (D015)");
    this.quantity = positive(b.quantity, "quantity (E711)");
    this.unitPrice = notNegative(b.unitPrice, "unitPrice (E721)");
    this.discountPerUnit = notNegative(b.discountPerUnit, "discountPerUnit (EA002)");
    this.globalDiscountRate = notNegative(b.globalDiscountRate, "globalDiscountRate (F010)");
    this.advancePerUnit = notNegative(b.advancePerUnit, "advancePerUnit (EA006)");
    this.globalAdvancePerUnit = notNegative(b.globalAdvancePerUnit, "globalAdvancePerUnit (EA007)");
    this.exchangeRate = b.exchangeRate == null ? null : positive(b.exchangeRate, "exchangeRate");
    this.vatTreatmentCode = b.vatTreatmentCode;
    if (vatTreatmentCode == null) {
      if (discountPerUnit.signum() != 0
          || globalDiscountRate.signum() != 0
          || advancePerUnit.signum() != 0
          || globalAdvancePerUnit.signum() != 0) {
        throw new IllegalArgumentException(
            "An item without VAT (self-invoice, C002 = 4) has no discounts nor advances"
                + " (R-EA008-A2: EA008 = E721 × E711)");
      }
      this.vatRate = BigDecimal.ZERO;
      this.taxableProportionRate = BigDecimal.ZERO;
      return;
    }
    switch (vatTreatmentCode) {
      case "1":
        this.vatRate = rate(b.vatRate);
        this.taxableProportionRate = BigDecimal.valueOf(100);
        break;
      case "2":
      case "3":
        this.vatRate = BigDecimal.ZERO;
        this.taxableProportionRate = BigDecimal.ZERO;
        break;
      case "4":
        this.vatRate = rate(b.vatRate);
        BigDecimal p = Objects.requireNonNull(b.taxableProportionRate, "taxableProportionRate");
        if (p.signum() <= 0 || p.compareTo(BigDecimal.valueOf(100)) >= 0) {
          throw new IllegalArgumentException(
              "A partially taxed item (E731 = 4) has a taxable proportion (E733) between 0 and 100");
        }
        this.taxableProportionRate = p;
        break;
      default:
        throw new IllegalArgumentException(
            "The VAT treatment (E731) is 1 (taxed), 2 (exonerated), 3 (exempt) or 4 (partially"
                + " taxed)");
    }
  }

  private static BigDecimal rate(@Nullable BigDecimal rate) {
    Objects.requireNonNull(rate, "vatRate (E734)");
    if (rate.compareTo(BigDecimal.valueOf(5)) != 0 && rate.compareTo(BigDecimal.TEN) != 0) {
      throw new IllegalArgumentException("The VAT rate (E734) of a taxed item is 5 or 10");
    }
    return rate;
  }

  private static BigDecimal positive(@Nullable BigDecimal value, String name) {
    Objects.requireNonNull(value, name);
    if (value.signum() <= 0) {
      throw new IllegalArgumentException(name + " must be greater than 0");
    }
    return value;
  }

  private static BigDecimal notNegative(@Nullable BigDecimal value, String name) {
    Objects.requireNonNull(value, name);
    if (value.signum() < 0) {
      throw new IllegalArgumentException(name + " must not be negative");
    }
    return value;
  }

  /**
   * Starts a new builder. Discounts and advances default to 0.
   *
   * @return a builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * The currency of the document (D015), which sets the decimals of the derived amounts.
   *
   * @return the ISO 4217 code
   */
  public String currencyCode() {
    return currencyCode;
  }

  /**
   * The quantity (E711).
   *
   * @return the quantity
   */
  public BigDecimal quantity() {
    return quantity;
  }

  /**
   * The unit price (E721).
   *
   * @return the unit price
   */
  public BigDecimal unitPrice() {
    return unitPrice;
  }

  /**
   * The discount per unit of this item (EA002).
   *
   * @return the discount, 0 if none
   */
  public BigDecimal discountPerUnit() {
    return discountPerUnit;
  }

  /**
   * The global discount rate of the document (F010), from which the global discount per unit
   * (EA004) is derived.
   *
   * @return the rate in percent, 0 if none
   */
  public BigDecimal globalDiscountRate() {
    return globalDiscountRate;
  }

  /**
   * The advance per unit of this item (EA006).
   *
   * @return the advance, 0 if none
   */
  public BigDecimal advancePerUnit() {
    return advancePerUnit;
  }

  /**
   * The global advance per unit (EA007).
   *
   * @return the advance, 0 if none
   */
  public BigDecimal globalAdvancePerUnit() {
    return globalAdvancePerUnit;
  }

  /**
   * The exchange rate of the item (E725), when the document has one per item (D017 = 2).
   *
   * @return the rate, or {@code null}
   */
  public @Nullable BigDecimal exchangeRate() {
    return exchangeRate;
  }

  /**
   * The VAT treatment (E731), or {@code null} for an item of a self-invoice.
   *
   * @return the code
   */
  public @Nullable String vatTreatmentCode() {
    return vatTreatmentCode;
  }

  /**
   * The VAT rate (E734): 5 or 10, 0 when exonerated or exempt.
   *
   * @return the rate
   */
  public BigDecimal vatRate() {
    return vatRate;
  }

  /**
   * The taxable proportion (E733): 100 when taxed, 0 when exonerated or exempt.
   *
   * @return the proportion in percent
   */
  public BigDecimal taxableProportionRate() {
    return taxableProportionRate;
  }

  /** Describes it without amounts. */
  @Override
  public String toString() {
    return "ItemPrice[" + currencyCode + ", VAT " + vatTreatmentCode + "]";
  }

  /** Builder for {@link ItemPrice}. Not thread-safe. */
  public static final class Builder {

    private @Nullable String currencyCode;
    private @Nullable BigDecimal quantity;
    private @Nullable BigDecimal unitPrice;
    private BigDecimal discountPerUnit = BigDecimal.ZERO;
    private BigDecimal globalDiscountRate = BigDecimal.ZERO;
    private BigDecimal advancePerUnit = BigDecimal.ZERO;
    private BigDecimal globalAdvancePerUnit = BigDecimal.ZERO;
    private @Nullable BigDecimal exchangeRate;
    private @Nullable String vatTreatmentCode;
    private @Nullable BigDecimal vatRate;
    private @Nullable BigDecimal taxableProportionRate;

    private Builder() {}

    /**
     * Sets the currency of the document (D015).
     *
     * @param currencyCode the ISO 4217 code, for example {@code PYG}
     * @return this builder
     */
    public Builder currencyCode(String currencyCode) {
      this.currencyCode = currencyCode;
      return this;
    }

    /**
     * Sets the quantity (E711).
     *
     * @param quantity greater than 0
     * @return this builder
     */
    public Builder quantity(BigDecimal quantity) {
      this.quantity = quantity;
      return this;
    }

    /**
     * Sets the unit price (E721).
     *
     * @param unitPrice the price
     * @return this builder
     */
    public Builder unitPrice(BigDecimal unitPrice) {
      this.unitPrice = unitPrice;
      return this;
    }

    /**
     * Sets the discount per unit of this item (EA002).
     *
     * @param discountPerUnit the amount per unit
     * @return this builder
     */
    public Builder discountPerUnit(BigDecimal discountPerUnit) {
      this.discountPerUnit = discountPerUnit;
      return this;
    }

    /**
     * Sets the global discount rate of the document (F010); the same for every item.
     *
     * @param globalDiscountRate in percent
     * @return this builder
     */
    public Builder globalDiscountRate(BigDecimal globalDiscountRate) {
      this.globalDiscountRate = globalDiscountRate;
      return this;
    }

    /**
     * Sets the advance per unit of this item (EA006).
     *
     * @param advancePerUnit the amount per unit
     * @return this builder
     */
    public Builder advancePerUnit(BigDecimal advancePerUnit) {
      this.advancePerUnit = advancePerUnit;
      return this;
    }

    /**
     * Sets the global advance per unit (EA007).
     *
     * @param globalAdvancePerUnit the amount per unit
     * @return this builder
     */
    public Builder globalAdvancePerUnit(BigDecimal globalAdvancePerUnit) {
      this.globalAdvancePerUnit = globalAdvancePerUnit;
      return this;
    }

    /**
     * Sets the exchange rate of the item (E725), for documents with one per item (D017 = 2).
     *
     * @param exchangeRate the rate
     * @return this builder
     */
    public Builder exchangeRate(@Nullable BigDecimal exchangeRate) {
      this.exchangeRate = exchangeRate;
      return this;
    }

    /**
     * A taxed item (E731 = 1).
     *
     * @param rate 5 or 10 (E734)
     * @return this builder
     */
    public Builder taxed(BigDecimal rate) {
      this.vatTreatmentCode = "1";
      this.vatRate = rate;
      return this;
    }

    /**
     * An exonerated item (E731 = 2).
     *
     * @return this builder
     */
    public Builder exonerated() {
      this.vatTreatmentCode = "2";
      return this;
    }

    /**
     * An exempt item (E731 = 3).
     *
     * @return this builder
     */
    public Builder exempt() {
      this.vatTreatmentCode = "3";
      return this;
    }

    /**
     * A partially taxed item (E731 = 4).
     *
     * @param rate 5 or 10 (E734)
     * @param taxableProportionRate the taxed part in percent (E733), between 0 and 100
     * @return this builder
     */
    public Builder partiallyTaxed(BigDecimal rate, BigDecimal taxableProportionRate) {
      this.vatTreatmentCode = "4";
      this.vatRate = rate;
      this.taxableProportionRate = taxableProportionRate;
      return this;
    }

    /**
     * Builds the immutable input.
     *
     * @return the input
     * @throws NullPointerException if the currency, quantity or unit price is missing
     * @throws IllegalArgumentException if a value is out of range
     */
    public ItemPrice build() {
      return new ItemPrice(this);
    }
  }
}
