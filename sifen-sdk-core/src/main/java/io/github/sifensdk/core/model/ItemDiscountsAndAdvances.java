/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.math.BigDecimal;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Campos que describen los descuentos, anticipos valor total por ítem.
 *
 * <p>SIFEN group {@code EA001} ({@code gValorRestaItem}). Immutable and thread-safe; create it with
 * {@link #builder()}. The builder only checks what Java needs (required elements and non-null
 * lists); the SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step,
 * never here. Amounts are kept exactly as given.
 */
@SifenField(id = "EA001", xml = "gValorRestaItem")
public final class ItemDiscountsAndAdvances {

  private final @Nullable BigDecimal discountAmount;
  private final @Nullable BigDecimal discountRate;
  private final @Nullable BigDecimal globalDiscountAmount;
  private final @Nullable BigDecimal advanceAmount;
  private final @Nullable BigDecimal globalAdvanceAmount;
  private final BigDecimal total;
  private final @Nullable BigDecimal guaraniTotal;

  private ItemDiscountsAndAdvances(Builder b) {
    this.discountAmount = b.discountAmount;
    this.discountRate = b.discountRate;
    this.globalDiscountAmount = b.globalDiscountAmount;
    this.advanceAmount = b.advanceAmount;
    this.globalAdvanceAmount = b.globalAdvanceAmount;
    this.total = Checks.required(b.total, "total", "EA008", "dTotOpeItem");
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
   * Descuento particular sobre el precio unitario por ítem (incluidos impuestos).
   *
   * <p>SIFEN field {@code EA002} ({@code dDescItem}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "EA002", xml = "dDescItem")
  public @Nullable BigDecimal discountAmount() {
    return discountAmount;
  }

  /**
   * Porcentaje de descuento particular por ítem.
   *
   * <p>SIFEN field {@code EA003} ({@code dPorcDesIt}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "EA003", xml = "dPorcDesIt")
  public @Nullable BigDecimal discountRate() {
    return discountRate;
  }

  /**
   * Descuento global sobre el precio unitario por ítem (incluidos impuestos).
   *
   * <p>SIFEN field {@code EA004} ({@code dDescGloItem}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "EA004", xml = "dDescGloItem")
  public @Nullable BigDecimal globalDiscountAmount() {
    return globalDiscountAmount;
  }

  /**
   * Anticipo particular sobre el precio unitario por ítem (incluidos impuestos).
   *
   * <p>SIFEN field {@code EA006} ({@code dAntPreUniIt}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "EA006", xml = "dAntPreUniIt")
  public @Nullable BigDecimal advanceAmount() {
    return advanceAmount;
  }

  /**
   * Anticipo global sobre el precio unitario por ítem (incluidos impuestos).
   *
   * <p>SIFEN field {@code EA007} ({@code dAntGloPreUniIt}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "EA007", xml = "dAntGloPreUniIt")
  public @Nullable BigDecimal globalAdvanceAmount() {
    return globalAdvanceAmount;
  }

  /**
   * Valor total de la operación por ítem.
   *
   * <p>SIFEN field {@code EA008} ({@code dTotOpeItem}).
   *
   * @return the value
   */
  @SifenField(id = "EA008", xml = "dTotOpeItem")
  public BigDecimal total() {
    return total;
  }

  /**
   * Valor total de la operación por ítem en guaraníes.
   *
   * <p>SIFEN field {@code EA009} ({@code dTotOpeGs}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "EA009", xml = "dTotOpeGs")
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
    if (!(other instanceof ItemDiscountsAndAdvances)) {
      return false;
    }
    ItemDiscountsAndAdvances that = (ItemDiscountsAndAdvances) other;
    return Objects.equals(discountAmount, that.discountAmount)
        && Objects.equals(discountRate, that.discountRate)
        && Objects.equals(globalDiscountAmount, that.globalDiscountAmount)
        && Objects.equals(advanceAmount, that.advanceAmount)
        && Objects.equals(globalAdvanceAmount, that.globalAdvanceAmount)
        && Objects.equals(total, that.total)
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
        discountAmount,
        discountRate,
        globalDiscountAmount,
        advanceAmount,
        globalAdvanceAmount,
        total,
        guaraniTotal);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "ItemDiscountsAndAdvances";
  }

  /**
   * Builder for {@link ItemDiscountsAndAdvances}. Not thread-safe; {@link #build()} returns an
   * immutable copy.
   */
  public static final class Builder {

    private @Nullable BigDecimal discountAmount;
    private @Nullable BigDecimal discountRate;
    private @Nullable BigDecimal globalDiscountAmount;
    private @Nullable BigDecimal advanceAmount;
    private @Nullable BigDecimal globalAdvanceAmount;
    private @Nullable BigDecimal total;
    private @Nullable BigDecimal guaraniTotal;

    private Builder() {}

    private Builder(ItemDiscountsAndAdvances source) {
      this.discountAmount = source.discountAmount;
      this.discountRate = source.discountRate;
      this.globalDiscountAmount = source.globalDiscountAmount;
      this.advanceAmount = source.advanceAmount;
      this.globalAdvanceAmount = source.globalAdvanceAmount;
      this.total = source.total;
      this.guaraniTotal = source.guaraniTotal;
    }

    /**
     * Sets {@code discountAmount} (EA002, {@code dDescItem}).
     *
     * @param discountAmount the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder discountAmount(@Nullable BigDecimal discountAmount) {
      this.discountAmount = discountAmount;
      return this;
    }

    /**
     * Sets {@code discountRate} (EA003, {@code dPorcDesIt}).
     *
     * @param discountRate the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder discountRate(@Nullable BigDecimal discountRate) {
      this.discountRate = discountRate;
      return this;
    }

    /**
     * Sets {@code globalDiscountAmount} (EA004, {@code dDescGloItem}).
     *
     * @param globalDiscountAmount the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder globalDiscountAmount(@Nullable BigDecimal globalDiscountAmount) {
      this.globalDiscountAmount = globalDiscountAmount;
      return this;
    }

    /**
     * Sets {@code advanceAmount} (EA006, {@code dAntPreUniIt}).
     *
     * @param advanceAmount the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder advanceAmount(@Nullable BigDecimal advanceAmount) {
      this.advanceAmount = advanceAmount;
      return this;
    }

    /**
     * Sets {@code globalAdvanceAmount} (EA007, {@code dAntGloPreUniIt}).
     *
     * @param globalAdvanceAmount the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder globalAdvanceAmount(@Nullable BigDecimal globalAdvanceAmount) {
      this.globalAdvanceAmount = globalAdvanceAmount;
      return this;
    }

    /**
     * Sets {@code total} (EA008, {@code dTotOpeItem}).
     *
     * @param total the value
     * @return this builder
     */
    public Builder total(BigDecimal total) {
      this.total = total;
      return this;
    }

    /**
     * Sets {@code guaraniTotal} (EA009, {@code dTotOpeGs}).
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
    public ItemDiscountsAndAdvances build() {
      return new ItemDiscountsAndAdvances(this);
    }
  }
}
