/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.math.BigDecimal;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Campos de subtotales y totales.
 *
 * <p>SIFEN group {@code F001} ({@code gTotSub}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "F001", xml = "gTotSub")
public final class Totals {

  private final @Nullable BigDecimal exemptSubtotal;
  private final @Nullable BigDecimal exoneratedSubtotal;
  private final @Nullable BigDecimal vat5Subtotal;
  private final @Nullable BigDecimal vat10Subtotal;
  private final BigDecimal grossTotal;
  private final BigDecimal itemDiscountTotal;
  private final BigDecimal itemGlobalDiscountTotal;
  private final BigDecimal itemAdvanceTotal;
  private final BigDecimal itemGlobalAdvanceTotal;
  private final BigDecimal globalDiscountRate;
  private final BigDecimal discountTotal;
  private final BigDecimal advanceTotal;
  private final BigDecimal roundingAmount;
  private final @Nullable BigDecimal commissionAmount;
  private final BigDecimal netTotal;
  private final @Nullable BigDecimal vat5Amount;
  private final @Nullable BigDecimal vat10Amount;
  private final @Nullable BigDecimal vat5RoundingAmount;
  private final @Nullable BigDecimal vat10RoundingAmount;
  private final @Nullable BigDecimal commissionVatAmount;
  private final @Nullable BigDecimal vatTotal;
  private final @Nullable BigDecimal vat5TaxableBaseTotal;
  private final @Nullable BigDecimal vat10TaxableBaseTotal;
  private final @Nullable BigDecimal vatTaxableBaseTotal;
  private final @Nullable BigDecimal guaraniTotal;

  private Totals(Builder b) {
    this.exemptSubtotal = b.exemptSubtotal;
    this.exoneratedSubtotal = b.exoneratedSubtotal;
    this.vat5Subtotal = b.vat5Subtotal;
    this.vat10Subtotal = b.vat10Subtotal;
    this.grossTotal = Checks.required(b.grossTotal, "grossTotal", "F008", "dTotOpe");
    this.itemDiscountTotal =
        Checks.required(b.itemDiscountTotal, "itemDiscountTotal", "F009", "dTotDesc");
    this.itemGlobalDiscountTotal =
        Checks.required(
            b.itemGlobalDiscountTotal, "itemGlobalDiscountTotal", "F033", "dTotDescGlotem");
    this.itemAdvanceTotal =
        Checks.required(b.itemAdvanceTotal, "itemAdvanceTotal", "F034", "dTotAntItem");
    this.itemGlobalAdvanceTotal =
        Checks.required(b.itemGlobalAdvanceTotal, "itemGlobalAdvanceTotal", "F035", "dTotAnt");
    this.globalDiscountRate =
        Checks.required(b.globalDiscountRate, "globalDiscountRate", "F010", "dPorcDescTotal");
    this.discountTotal = Checks.required(b.discountTotal, "discountTotal", "F011", "dDescTotal");
    this.advanceTotal = Checks.required(b.advanceTotal, "advanceTotal", "F012", "dAnticipo");
    this.roundingAmount = Checks.required(b.roundingAmount, "roundingAmount", "F013", "dRedon");
    this.commissionAmount = b.commissionAmount;
    this.netTotal = Checks.required(b.netTotal, "netTotal", "F014", "dTotGralOpe");
    this.vat5Amount = b.vat5Amount;
    this.vat10Amount = b.vat10Amount;
    this.vat5RoundingAmount = b.vat5RoundingAmount;
    this.vat10RoundingAmount = b.vat10RoundingAmount;
    this.commissionVatAmount = b.commissionVatAmount;
    this.vatTotal = b.vatTotal;
    this.vat5TaxableBaseTotal = b.vat5TaxableBaseTotal;
    this.vat10TaxableBaseTotal = b.vat10TaxableBaseTotal;
    this.vatTaxableBaseTotal = b.vatTaxableBaseTotal;
    this.guaraniTotal = b.guaraniTotal;
  }

  /**
   * Starts a new builder.
   *
   * @return an empty builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Starts a builder with a copy of this object's values.
   *
   * @return a builder with these values
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  /**
   * Subtotal de la operación exenta.
   *
   * <p>SIFEN field {@code F002} ({@code dSubExe}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "F002", xml = "dSubExe")
  public @Nullable BigDecimal exemptSubtotal() {
    return exemptSubtotal;
  }

  /**
   * Subtotal de la operación exonerada.
   *
   * <p>SIFEN field {@code F003} ({@code dSubExo}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "F003", xml = "dSubExo")
  public @Nullable BigDecimal exoneratedSubtotal() {
    return exoneratedSubtotal;
  }

  /**
   * Subtotal de la operación con IVA incluido a la tasa 5%.
   *
   * <p>SIFEN field {@code F004} ({@code dSub5}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "F004", xml = "dSub5")
  public @Nullable BigDecimal vat5Subtotal() {
    return vat5Subtotal;
  }

  /**
   * Subtotal de la operación con IVA incluido a la tasa 10%.
   *
   * <p>SIFEN field {@code F005} ({@code dSub10}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "F005", xml = "dSub10")
  public @Nullable BigDecimal vat10Subtotal() {
    return vat10Subtotal;
  }

  /**
   * Total Bruto de la operación.
   *
   * <p>SIFEN field {@code F008} ({@code dTotOpe}).
   *
   * @return the value
   */
  @SifenField(id = "F008", xml = "dTotOpe")
  public BigDecimal grossTotal() {
    return grossTotal;
  }

  /**
   * Total descuento particular por ítem.
   *
   * <p>SIFEN field {@code F009} ({@code dTotDesc}).
   *
   * @return the value
   */
  @SifenField(id = "F009", xml = "dTotDesc")
  public BigDecimal itemDiscountTotal() {
    return itemDiscountTotal;
  }

  /**
   * Total descuento global por ítem.
   *
   * <p>SIFEN field {@code F033} ({@code dTotDescGlotem}).
   *
   * @return the value
   */
  @SifenField(id = "F033", xml = "dTotDescGlotem")
  public BigDecimal itemGlobalDiscountTotal() {
    return itemGlobalDiscountTotal;
  }

  /**
   * Total Anticipo por ítem.
   *
   * <p>SIFEN field {@code F034} ({@code dTotAntItem}).
   *
   * @return the value
   */
  @SifenField(id = "F034", xml = "dTotAntItem")
  public BigDecimal itemAdvanceTotal() {
    return itemAdvanceTotal;
  }

  /**
   * Total Anticipo global por ítem.
   *
   * <p>SIFEN field {@code F035} ({@code dTotAnt}).
   *
   * @return the value
   */
  @SifenField(id = "F035", xml = "dTotAnt")
  public BigDecimal itemGlobalAdvanceTotal() {
    return itemGlobalAdvanceTotal;
  }

  /**
   * Porcentaje de descuento global sobre total de la operación.
   *
   * <p>SIFEN field {@code F010} ({@code dPorcDescTotal}).
   *
   * @return the value
   */
  @SifenField(id = "F010", xml = "dPorcDescTotal")
  public BigDecimal globalDiscountRate() {
    return globalDiscountRate;
  }

  /**
   * Total Descuentos de la operación.
   *
   * <p>SIFEN field {@code F011} ({@code dDescTotal}).
   *
   * @return the value
   */
  @SifenField(id = "F011", xml = "dDescTotal")
  public BigDecimal discountTotal() {
    return discountTotal;
  }

  /**
   * Total Anticipos de la operación.
   *
   * <p>SIFEN field {@code F012} ({@code dAnticipo}).
   *
   * @return the value
   */
  @SifenField(id = "F012", xml = "dAnticipo")
  public BigDecimal advanceTotal() {
    return advanceTotal;
  }

  /**
   * Redondeo de la operación.
   *
   * <p>SIFEN field {@code F013} ({@code dRedon}).
   *
   * @return the value
   */
  @SifenField(id = "F013", xml = "dRedon")
  public BigDecimal roundingAmount() {
    return roundingAmount;
  }

  /**
   * Comisión de la operación.
   *
   * <p>SIFEN field {@code F025} ({@code dComi}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "F025", xml = "dComi")
  public @Nullable BigDecimal commissionAmount() {
    return commissionAmount;
  }

  /**
   * Total Neto de la operación.
   *
   * <p>SIFEN field {@code F014} ({@code dTotGralOpe}).
   *
   * @return the value
   */
  @SifenField(id = "F014", xml = "dTotGralOpe")
  public BigDecimal netTotal() {
    return netTotal;
  }

  /**
   * Liquidación del IVA a la tasa del 5%.
   *
   * <p>SIFEN field {@code F015} ({@code dIVA5}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "F015", xml = "dIVA5")
  public @Nullable BigDecimal vat5Amount() {
    return vat5Amount;
  }

  /**
   * Liquidación del IVA a la tasa del 10%.
   *
   * <p>SIFEN field {@code F016} ({@code dIVA10}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "F016", xml = "dIVA10")
  public @Nullable BigDecimal vat10Amount() {
    return vat10Amount;
  }

  /**
   * Liquidación total del IVA por redondeo a la tasa del 5%.
   *
   * <p>SIFEN field {@code F036} ({@code dLiqTotIVA5}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "F036", xml = "dLiqTotIVA5")
  public @Nullable BigDecimal vat5RoundingAmount() {
    return vat5RoundingAmount;
  }

  /**
   * Liquidación total del IVA por redondeo a la tasa del 10%.
   *
   * <p>SIFEN field {@code F037} ({@code dLiqTotIVA10}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "F037", xml = "dLiqTotIVA10")
  public @Nullable BigDecimal vat10RoundingAmount() {
    return vat10RoundingAmount;
  }

  /**
   * Liquidación total del IVA de la comisión.
   *
   * <p>SIFEN field {@code F026} ({@code dIVAComi}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "F026", xml = "dIVAComi")
  public @Nullable BigDecimal commissionVatAmount() {
    return commissionVatAmount;
  }

  /**
   * Liquidación total del IVA.
   *
   * <p>SIFEN field {@code F017} ({@code dTotIVA}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "F017", xml = "dTotIVA")
  public @Nullable BigDecimal vatTotal() {
    return vatTotal;
  }

  /**
   * Total base gravada al 5%.
   *
   * <p>SIFEN field {@code F018} ({@code dBaseGrav5}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "F018", xml = "dBaseGrav5")
  public @Nullable BigDecimal vat5TaxableBaseTotal() {
    return vat5TaxableBaseTotal;
  }

  /**
   * Total base gravada al 10%.
   *
   * <p>SIFEN field {@code F019} ({@code dBaseGrav10}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "F019", xml = "dBaseGrav10")
  public @Nullable BigDecimal vat10TaxableBaseTotal() {
    return vat10TaxableBaseTotal;
  }

  /**
   * Total de la base gravada de IVA.
   *
   * <p>SIFEN field {@code F020} ({@code dTBasGraIVA}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "F020", xml = "dTBasGraIVA")
  public @Nullable BigDecimal vatTaxableBaseTotal() {
    return vatTaxableBaseTotal;
  }

  /**
   * Total general de la operación en Guaraníes.
   *
   * <p>SIFEN field {@code F023} ({@code dTotalGs}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "F023", xml = "dTotalGs")
  public @Nullable BigDecimal guaraniTotal() {
    return guaraniTotal;
  }

  /**
   * Compares all values; amounts compare with their scale ({@code 1.0} differs from {@code 1}).
   *
   * @param other the object to compare with
   * @return whether both hold the same values
   */
  @Override
  public boolean equals(@Nullable Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Totals)) {
      return false;
    }
    Totals that = (Totals) other;
    return Objects.equals(exemptSubtotal, that.exemptSubtotal)
        && Objects.equals(exoneratedSubtotal, that.exoneratedSubtotal)
        && Objects.equals(vat5Subtotal, that.vat5Subtotal)
        && Objects.equals(vat10Subtotal, that.vat10Subtotal)
        && Objects.equals(grossTotal, that.grossTotal)
        && Objects.equals(itemDiscountTotal, that.itemDiscountTotal)
        && Objects.equals(itemGlobalDiscountTotal, that.itemGlobalDiscountTotal)
        && Objects.equals(itemAdvanceTotal, that.itemAdvanceTotal)
        && Objects.equals(itemGlobalAdvanceTotal, that.itemGlobalAdvanceTotal)
        && Objects.equals(globalDiscountRate, that.globalDiscountRate)
        && Objects.equals(discountTotal, that.discountTotal)
        && Objects.equals(advanceTotal, that.advanceTotal)
        && Objects.equals(roundingAmount, that.roundingAmount)
        && Objects.equals(commissionAmount, that.commissionAmount)
        && Objects.equals(netTotal, that.netTotal)
        && Objects.equals(vat5Amount, that.vat5Amount)
        && Objects.equals(vat10Amount, that.vat10Amount)
        && Objects.equals(vat5RoundingAmount, that.vat5RoundingAmount)
        && Objects.equals(vat10RoundingAmount, that.vat10RoundingAmount)
        && Objects.equals(commissionVatAmount, that.commissionVatAmount)
        && Objects.equals(vatTotal, that.vatTotal)
        && Objects.equals(vat5TaxableBaseTotal, that.vat5TaxableBaseTotal)
        && Objects.equals(vat10TaxableBaseTotal, that.vat10TaxableBaseTotal)
        && Objects.equals(vatTaxableBaseTotal, that.vatTaxableBaseTotal)
        && Objects.equals(guaraniTotal, that.guaraniTotal);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        exemptSubtotal,
        exoneratedSubtotal,
        vat5Subtotal,
        vat10Subtotal,
        grossTotal,
        itemDiscountTotal,
        itemGlobalDiscountTotal,
        itemAdvanceTotal,
        itemGlobalAdvanceTotal,
        globalDiscountRate,
        discountTotal,
        advanceTotal,
        roundingAmount,
        commissionAmount,
        netTotal,
        vat5Amount,
        vat10Amount,
        vat5RoundingAmount,
        vat10RoundingAmount,
        commissionVatAmount,
        vatTotal,
        vat5TaxableBaseTotal,
        vat10TaxableBaseTotal,
        vatTaxableBaseTotal,
        guaraniTotal);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "Totals";
  }

  /** Builder for {@link Totals}. Not thread-safe; {@link #build()} returns an immutable copy. */
  public static final class Builder {

    private @Nullable BigDecimal exemptSubtotal;
    private @Nullable BigDecimal exoneratedSubtotal;
    private @Nullable BigDecimal vat5Subtotal;
    private @Nullable BigDecimal vat10Subtotal;
    private @Nullable BigDecimal grossTotal;
    private @Nullable BigDecimal itemDiscountTotal;
    private @Nullable BigDecimal itemGlobalDiscountTotal;
    private @Nullable BigDecimal itemAdvanceTotal;
    private @Nullable BigDecimal itemGlobalAdvanceTotal;
    private @Nullable BigDecimal globalDiscountRate;
    private @Nullable BigDecimal discountTotal;
    private @Nullable BigDecimal advanceTotal;
    private @Nullable BigDecimal roundingAmount;
    private @Nullable BigDecimal commissionAmount;
    private @Nullable BigDecimal netTotal;
    private @Nullable BigDecimal vat5Amount;
    private @Nullable BigDecimal vat10Amount;
    private @Nullable BigDecimal vat5RoundingAmount;
    private @Nullable BigDecimal vat10RoundingAmount;
    private @Nullable BigDecimal commissionVatAmount;
    private @Nullable BigDecimal vatTotal;
    private @Nullable BigDecimal vat5TaxableBaseTotal;
    private @Nullable BigDecimal vat10TaxableBaseTotal;
    private @Nullable BigDecimal vatTaxableBaseTotal;
    private @Nullable BigDecimal guaraniTotal;

    private Builder() {}

    private Builder(Totals source) {
      this.exemptSubtotal = source.exemptSubtotal;
      this.exoneratedSubtotal = source.exoneratedSubtotal;
      this.vat5Subtotal = source.vat5Subtotal;
      this.vat10Subtotal = source.vat10Subtotal;
      this.grossTotal = source.grossTotal;
      this.itemDiscountTotal = source.itemDiscountTotal;
      this.itemGlobalDiscountTotal = source.itemGlobalDiscountTotal;
      this.itemAdvanceTotal = source.itemAdvanceTotal;
      this.itemGlobalAdvanceTotal = source.itemGlobalAdvanceTotal;
      this.globalDiscountRate = source.globalDiscountRate;
      this.discountTotal = source.discountTotal;
      this.advanceTotal = source.advanceTotal;
      this.roundingAmount = source.roundingAmount;
      this.commissionAmount = source.commissionAmount;
      this.netTotal = source.netTotal;
      this.vat5Amount = source.vat5Amount;
      this.vat10Amount = source.vat10Amount;
      this.vat5RoundingAmount = source.vat5RoundingAmount;
      this.vat10RoundingAmount = source.vat10RoundingAmount;
      this.commissionVatAmount = source.commissionVatAmount;
      this.vatTotal = source.vatTotal;
      this.vat5TaxableBaseTotal = source.vat5TaxableBaseTotal;
      this.vat10TaxableBaseTotal = source.vat10TaxableBaseTotal;
      this.vatTaxableBaseTotal = source.vatTaxableBaseTotal;
      this.guaraniTotal = source.guaraniTotal;
    }

    /**
     * Sets {@code exemptSubtotal} (F002, {@code dSubExe}).
     *
     * @param exemptSubtotal the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder exemptSubtotal(@Nullable BigDecimal exemptSubtotal) {
      this.exemptSubtotal = exemptSubtotal;
      return this;
    }

    /**
     * Sets {@code exoneratedSubtotal} (F003, {@code dSubExo}).
     *
     * @param exoneratedSubtotal the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder exoneratedSubtotal(@Nullable BigDecimal exoneratedSubtotal) {
      this.exoneratedSubtotal = exoneratedSubtotal;
      return this;
    }

    /**
     * Sets {@code vat5Subtotal} (F004, {@code dSub5}).
     *
     * @param vat5Subtotal the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder vat5Subtotal(@Nullable BigDecimal vat5Subtotal) {
      this.vat5Subtotal = vat5Subtotal;
      return this;
    }

    /**
     * Sets {@code vat10Subtotal} (F005, {@code dSub10}).
     *
     * @param vat10Subtotal the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder vat10Subtotal(@Nullable BigDecimal vat10Subtotal) {
      this.vat10Subtotal = vat10Subtotal;
      return this;
    }

    /**
     * Sets {@code grossTotal} (F008, {@code dTotOpe}).
     *
     * @param grossTotal the value
     * @return this builder
     */
    public Builder grossTotal(BigDecimal grossTotal) {
      this.grossTotal = grossTotal;
      return this;
    }

    /**
     * Sets {@code itemDiscountTotal} (F009, {@code dTotDesc}).
     *
     * @param itemDiscountTotal the value
     * @return this builder
     */
    public Builder itemDiscountTotal(BigDecimal itemDiscountTotal) {
      this.itemDiscountTotal = itemDiscountTotal;
      return this;
    }

    /**
     * Sets {@code itemGlobalDiscountTotal} (F033, {@code dTotDescGlotem}).
     *
     * @param itemGlobalDiscountTotal the value
     * @return this builder
     */
    public Builder itemGlobalDiscountTotal(BigDecimal itemGlobalDiscountTotal) {
      this.itemGlobalDiscountTotal = itemGlobalDiscountTotal;
      return this;
    }

    /**
     * Sets {@code itemAdvanceTotal} (F034, {@code dTotAntItem}).
     *
     * @param itemAdvanceTotal the value
     * @return this builder
     */
    public Builder itemAdvanceTotal(BigDecimal itemAdvanceTotal) {
      this.itemAdvanceTotal = itemAdvanceTotal;
      return this;
    }

    /**
     * Sets {@code itemGlobalAdvanceTotal} (F035, {@code dTotAnt}).
     *
     * @param itemGlobalAdvanceTotal the value
     * @return this builder
     */
    public Builder itemGlobalAdvanceTotal(BigDecimal itemGlobalAdvanceTotal) {
      this.itemGlobalAdvanceTotal = itemGlobalAdvanceTotal;
      return this;
    }

    /**
     * Sets {@code globalDiscountRate} (F010, {@code dPorcDescTotal}).
     *
     * @param globalDiscountRate the value
     * @return this builder
     */
    public Builder globalDiscountRate(BigDecimal globalDiscountRate) {
      this.globalDiscountRate = globalDiscountRate;
      return this;
    }

    /**
     * Sets {@code discountTotal} (F011, {@code dDescTotal}).
     *
     * @param discountTotal the value
     * @return this builder
     */
    public Builder discountTotal(BigDecimal discountTotal) {
      this.discountTotal = discountTotal;
      return this;
    }

    /**
     * Sets {@code advanceTotal} (F012, {@code dAnticipo}).
     *
     * @param advanceTotal the value
     * @return this builder
     */
    public Builder advanceTotal(BigDecimal advanceTotal) {
      this.advanceTotal = advanceTotal;
      return this;
    }

    /**
     * Sets {@code roundingAmount} (F013, {@code dRedon}).
     *
     * @param roundingAmount the value
     * @return this builder
     */
    public Builder roundingAmount(BigDecimal roundingAmount) {
      this.roundingAmount = roundingAmount;
      return this;
    }

    /**
     * Sets {@code commissionAmount} (F025, {@code dComi}).
     *
     * @param commissionAmount the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder commissionAmount(@Nullable BigDecimal commissionAmount) {
      this.commissionAmount = commissionAmount;
      return this;
    }

    /**
     * Sets {@code netTotal} (F014, {@code dTotGralOpe}).
     *
     * @param netTotal the value
     * @return this builder
     */
    public Builder netTotal(BigDecimal netTotal) {
      this.netTotal = netTotal;
      return this;
    }

    /**
     * Sets {@code vat5Amount} (F015, {@code dIVA5}).
     *
     * @param vat5Amount the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder vat5Amount(@Nullable BigDecimal vat5Amount) {
      this.vat5Amount = vat5Amount;
      return this;
    }

    /**
     * Sets {@code vat10Amount} (F016, {@code dIVA10}).
     *
     * @param vat10Amount the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder vat10Amount(@Nullable BigDecimal vat10Amount) {
      this.vat10Amount = vat10Amount;
      return this;
    }

    /**
     * Sets {@code vat5RoundingAmount} (F036, {@code dLiqTotIVA5}).
     *
     * @param vat5RoundingAmount the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder vat5RoundingAmount(@Nullable BigDecimal vat5RoundingAmount) {
      this.vat5RoundingAmount = vat5RoundingAmount;
      return this;
    }

    /**
     * Sets {@code vat10RoundingAmount} (F037, {@code dLiqTotIVA10}).
     *
     * @param vat10RoundingAmount the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder vat10RoundingAmount(@Nullable BigDecimal vat10RoundingAmount) {
      this.vat10RoundingAmount = vat10RoundingAmount;
      return this;
    }

    /**
     * Sets {@code commissionVatAmount} (F026, {@code dIVAComi}).
     *
     * @param commissionVatAmount the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder commissionVatAmount(@Nullable BigDecimal commissionVatAmount) {
      this.commissionVatAmount = commissionVatAmount;
      return this;
    }

    /**
     * Sets {@code vatTotal} (F017, {@code dTotIVA}).
     *
     * @param vatTotal the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder vatTotal(@Nullable BigDecimal vatTotal) {
      this.vatTotal = vatTotal;
      return this;
    }

    /**
     * Sets {@code vat5TaxableBaseTotal} (F018, {@code dBaseGrav5}).
     *
     * @param vat5TaxableBaseTotal the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder vat5TaxableBaseTotal(@Nullable BigDecimal vat5TaxableBaseTotal) {
      this.vat5TaxableBaseTotal = vat5TaxableBaseTotal;
      return this;
    }

    /**
     * Sets {@code vat10TaxableBaseTotal} (F019, {@code dBaseGrav10}).
     *
     * @param vat10TaxableBaseTotal the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder vat10TaxableBaseTotal(@Nullable BigDecimal vat10TaxableBaseTotal) {
      this.vat10TaxableBaseTotal = vat10TaxableBaseTotal;
      return this;
    }

    /**
     * Sets {@code vatTaxableBaseTotal} (F020, {@code dTBasGraIVA}).
     *
     * @param vatTaxableBaseTotal the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder vatTaxableBaseTotal(@Nullable BigDecimal vatTaxableBaseTotal) {
      this.vatTaxableBaseTotal = vatTaxableBaseTotal;
      return this;
    }

    /**
     * Sets {@code guaraniTotal} (F023, {@code dTotalGs}).
     *
     * @param guaraniTotal the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder guaraniTotal(@Nullable BigDecimal guaraniTotal) {
      this.guaraniTotal = guaraniTotal;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public Totals build() {
      return new Totals(this);
    }
  }
}
