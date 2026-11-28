/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import io.github.sifensdk.core.catalog.VatTreatment;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Campos que describen el IVA de la operación.
 *
 * <p>SIFEN group {@code E730} ({@code gCamIVA}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "E730", xml = "gCamIVA")
public final class ItemVat {

  private final String treatmentCode;
  private final @Nullable String treatmentDescription;
  private final BigDecimal taxableProportionRate;
  private final BigDecimal rate;
  private final BigDecimal taxableBase;
  private final BigDecimal amount;
  private final BigDecimal exemptBase;

  private ItemVat(Builder b) {
    this.treatmentCode = Checks.required(b.treatmentCode, "treatmentCode", "E731", "iAfecIVA");
    this.treatmentDescription = b.treatmentDescription;
    this.taxableProportionRate =
        Checks.required(b.taxableProportionRate, "taxableProportionRate", "E733", "dPropIVA");
    this.rate = Checks.required(b.rate, "rate", "E734", "dTasaIVA");
    this.taxableBase = Checks.required(b.taxableBase, "taxableBase", "E735", "dBasGravIVA");
    this.amount = Checks.required(b.amount, "amount", "E736", "dLiqIVAItem");
    this.exemptBase = Checks.required(b.exemptBase, "exemptBase", "E737", "dBasExe");
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
   * Forma de afectación tributaria del IVA.
   *
   * <p>SIFEN field {@code E731} ({@code iAfecIVA}).
   *
   * @return the value
   */
  @SifenField(id = "E731", xml = "iAfecIVA")
  public String treatmentCode() {
    return treatmentCode;
  }

  /**
   * Returns {@link #treatmentCode()} (E731) as {@link VatTreatment}.
   *
   * @return the constant, or empty if the code is not set or this SDK version does not know it
   */
  public Optional<VatTreatment> treatment() {
    return VatTreatment.fromCode(treatmentCode);
  }

  /**
   * Descripción de la forma de afectación tributaria del IVA.
   *
   * <p>SIFEN field {@code E732} ({@code dDesAfecIVA}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E731}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E732", xml = "dDesAfecIVA")
  public @Nullable String treatmentDescription() {
    return treatmentDescription;
  }

  /**
   * Proporción gravada de IVA.
   *
   * <p>SIFEN field {@code E733} ({@code dPropIVA}).
   *
   * @return the value
   */
  @SifenField(id = "E733", xml = "dPropIVA")
  public BigDecimal taxableProportionRate() {
    return taxableProportionRate;
  }

  /**
   * Tasa del IVA.
   *
   * <p>SIFEN field {@code E734} ({@code dTasaIVA}).
   *
   * @return the value
   */
  @SifenField(id = "E734", xml = "dTasaIVA")
  public BigDecimal rate() {
    return rate;
  }

  /**
   * Base gravada del IVA por ítem.
   *
   * <p>SIFEN field {@code E735} ({@code dBasGravIVA}).
   *
   * @return the value
   */
  @SifenField(id = "E735", xml = "dBasGravIVA")
  public BigDecimal taxableBase() {
    return taxableBase;
  }

  /**
   * Liquidación del IVA por ítem.
   *
   * <p>SIFEN field {@code E736} ({@code dLiqIVAItem}).
   *
   * @return the value
   */
  @SifenField(id = "E736", xml = "dLiqIVAItem")
  public BigDecimal amount() {
    return amount;
  }

  /**
   * Base Exenta por ítem.
   *
   * <p>SIFEN field {@code E737} ({@code dBasExe}).
   *
   * @return the value
   */
  @SifenField(id = "E737", xml = "dBasExe")
  public BigDecimal exemptBase() {
    return exemptBase;
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
    if (!(other instanceof ItemVat)) {
      return false;
    }
    ItemVat that = (ItemVat) other;
    return Objects.equals(treatmentCode, that.treatmentCode)
        && Objects.equals(treatmentDescription, that.treatmentDescription)
        && Objects.equals(taxableProportionRate, that.taxableProportionRate)
        && Objects.equals(rate, that.rate)
        && Objects.equals(taxableBase, that.taxableBase)
        && Objects.equals(amount, that.amount)
        && Objects.equals(exemptBase, that.exemptBase);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        treatmentCode,
        treatmentDescription,
        taxableProportionRate,
        rate,
        taxableBase,
        amount,
        exemptBase);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "ItemVat";
  }

  /** Builder for {@link ItemVat}. Not thread-safe; {@link #build()} returns an immutable copy. */
  public static final class Builder {

    private @Nullable String treatmentCode;
    private @Nullable String treatmentDescription;
    private @Nullable BigDecimal taxableProportionRate;
    private @Nullable BigDecimal rate;
    private @Nullable BigDecimal taxableBase;
    private @Nullable BigDecimal amount;
    private @Nullable BigDecimal exemptBase;

    private Builder() {}

    private Builder(ItemVat source) {
      this.treatmentCode = source.treatmentCode;
      this.treatmentDescription = source.treatmentDescription;
      this.taxableProportionRate = source.taxableProportionRate;
      this.rate = source.rate;
      this.taxableBase = source.taxableBase;
      this.amount = source.amount;
      this.exemptBase = source.exemptBase;
    }

    /**
     * Sets {@code treatmentCode} (E731, {@code iAfecIVA}).
     *
     * @param treatmentCode the value
     * @return this builder
     */
    public Builder treatmentCode(String treatmentCode) {
      this.treatmentCode = treatmentCode;
      return this;
    }

    /**
     * Sets {@code treatmentCode} (E731, {@code iAfecIVA}) from {@link VatTreatment}.
     *
     * @param treatment the catalog constant
     * @return this builder
     */
    public Builder treatment(VatTreatment treatment) {
      this.treatmentCode = treatment.code();
      return this;
    }

    /**
     * Sets {@code treatmentDescription} (E732, {@code dDesAfecIVA}).
     *
     * @param treatmentDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder treatmentDescription(@Nullable String treatmentDescription) {
      this.treatmentDescription = treatmentDescription;
      return this;
    }

    /**
     * Sets {@code taxableProportionRate} (E733, {@code dPropIVA}).
     *
     * @param taxableProportionRate the value
     * @return this builder
     */
    public Builder taxableProportionRate(BigDecimal taxableProportionRate) {
      this.taxableProportionRate = taxableProportionRate;
      return this;
    }

    /**
     * Sets {@code rate} (E734, {@code dTasaIVA}).
     *
     * @param rate the value
     * @return this builder
     */
    public Builder rate(BigDecimal rate) {
      this.rate = rate;
      return this;
    }

    /**
     * Sets {@code taxableBase} (E735, {@code dBasGravIVA}).
     *
     * @param taxableBase the value
     * @return this builder
     */
    public Builder taxableBase(BigDecimal taxableBase) {
      this.taxableBase = taxableBase;
      return this;
    }

    /**
     * Sets {@code amount} (E736, {@code dLiqIVAItem}).
     *
     * @param amount the value
     * @return this builder
     */
    public Builder amount(BigDecimal amount) {
      this.amount = amount;
      return this;
    }

    /**
     * Sets {@code exemptBase} (E737, {@code dBasExe}).
     *
     * @param exemptBase the value
     * @return this builder
     */
    public Builder exemptBase(BigDecimal exemptBase) {
      this.exemptBase = exemptBase;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public ItemVat build() {
      return new ItemVat(this);
    }
  }
}
