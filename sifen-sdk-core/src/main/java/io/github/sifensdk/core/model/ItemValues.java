/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.math.BigDecimal;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Campos que describen los precios, descuentos y valor total por ítem.
 *
 * <p>SIFEN group {@code E720} ({@code gValorItem}). Immutable and thread-safe; create it with
 * {@link #builder()}. The builder only checks what Java needs (required elements and non-null
 * lists); the SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step,
 * never here. Amounts are kept exactly as given.
 */
@SifenField(id = "E720", xml = "gValorItem")
public final class ItemValues {

  private final BigDecimal unitPrice;
  private final @Nullable BigDecimal exchangeRate;
  private final BigDecimal grossTotal;
  private final ItemDiscountsAndAdvances discountsAndAdvances;

  private ItemValues(Builder b) {
    this.unitPrice = Checks.required(b.unitPrice, "unitPrice", "E721", "dPUniProSer");
    this.exchangeRate = b.exchangeRate;
    this.grossTotal = Checks.required(b.grossTotal, "grossTotal", "E727", "dTotBruOpeItem");
    this.discountsAndAdvances =
        Checks.required(b.discountsAndAdvances, "discountsAndAdvances", "EA001", "gValorRestaItem");
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
   * Precio unitario del producto y/o servicio (incluidos impuestos).
   *
   * <p>SIFEN field {@code E721} ({@code dPUniProSer}).
   *
   * @return the value
   */
  @SifenField(id = "E721", xml = "dPUniProSer")
  public BigDecimal unitPrice() {
    return unitPrice;
  }

  /**
   * Tipo de cambio por ítem.
   *
   * <p>SIFEN field {@code E725} ({@code dTiCamIt}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E725", xml = "dTiCamIt")
  public @Nullable BigDecimal exchangeRate() {
    return exchangeRate;
  }

  /**
   * Total bruto de la operación por ítem.
   *
   * <p>SIFEN field {@code E727} ({@code dTotBruOpeItem}).
   *
   * @return the value
   */
  @SifenField(id = "E727", xml = "dTotBruOpeItem")
  public BigDecimal grossTotal() {
    return grossTotal;
  }

  /**
   * Campos que describen los descuentos, anticipos valor total por ítem.
   *
   * <p>SIFEN group {@code EA001} ({@code gValorRestaItem}).
   *
   * @return the value
   */
  @SifenField(id = "EA001", xml = "gValorRestaItem")
  public ItemDiscountsAndAdvances discountsAndAdvances() {
    return discountsAndAdvances;
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
    if (!(other instanceof ItemValues)) {
      return false;
    }
    ItemValues that = (ItemValues) other;
    return Objects.equals(unitPrice, that.unitPrice)
        && Objects.equals(exchangeRate, that.exchangeRate)
        && Objects.equals(grossTotal, that.grossTotal)
        && Objects.equals(discountsAndAdvances, that.discountsAndAdvances);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(unitPrice, exchangeRate, grossTotal, discountsAndAdvances);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "ItemValues";
  }

  /**
   * Builder for {@link ItemValues}. Not thread-safe; {@link #build()} returns an immutable copy.
   */
  public static final class Builder {

    private @Nullable BigDecimal unitPrice;
    private @Nullable BigDecimal exchangeRate;
    private @Nullable BigDecimal grossTotal;
    private @Nullable ItemDiscountsAndAdvances discountsAndAdvances;

    private Builder() {}

    private Builder(ItemValues source) {
      this.unitPrice = source.unitPrice;
      this.exchangeRate = source.exchangeRate;
      this.grossTotal = source.grossTotal;
      this.discountsAndAdvances = source.discountsAndAdvances;
    }

    /**
     * Sets {@code unitPrice} (E721, {@code dPUniProSer}).
     *
     * @param unitPrice the value
     * @return this builder
     */
    public Builder unitPrice(BigDecimal unitPrice) {
      this.unitPrice = unitPrice;
      return this;
    }

    /**
     * Sets {@code exchangeRate} (E725, {@code dTiCamIt}).
     *
     * @param exchangeRate the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder exchangeRate(@Nullable BigDecimal exchangeRate) {
      this.exchangeRate = exchangeRate;
      return this;
    }

    /**
     * Sets {@code grossTotal} (E727, {@code dTotBruOpeItem}).
     *
     * @param grossTotal the value
     * @return this builder
     */
    public Builder grossTotal(BigDecimal grossTotal) {
      this.grossTotal = grossTotal;
      return this;
    }

    /**
     * Sets {@code discountsAndAdvances} (EA001, {@code gValorRestaItem}).
     *
     * @param discountsAndAdvances the value
     * @return this builder
     */
    public Builder discountsAndAdvances(ItemDiscountsAndAdvances discountsAndAdvances) {
      this.discountsAndAdvances = discountsAndAdvances;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public ItemValues build() {
      return new ItemValues(this);
    }
  }
}
