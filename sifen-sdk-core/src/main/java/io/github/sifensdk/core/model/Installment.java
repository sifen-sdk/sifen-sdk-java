/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Campos que describen las cuotas.
 *
 * <p>SIFEN group {@code E650} ({@code gCuotas}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "E650", xml = "gCuotas")
public final class Installment {

  private final String currencyCode;
  private final @Nullable String currencyDescription;
  private final BigDecimal amount;
  private final @Nullable LocalDate dueDate;

  private Installment(Builder b) {
    this.currencyCode = Checks.required(b.currencyCode, "currencyCode", "E653", "cMoneCuo");
    this.currencyDescription = b.currencyDescription;
    this.amount = Checks.required(b.amount, "amount", "E651", "dMonCuota");
    this.dueDate = b.dueDate;
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
   * Moneda de las cuotas.
   *
   * <p>SIFEN field {@code E653} ({@code cMoneCuo}).
   *
   * @return the value
   */
  @SifenField(id = "E653", xml = "cMoneCuo")
  public String currencyCode() {
    return currencyCode;
  }

  /**
   * Descripción de la moneda de las cuotas.
   *
   * <p>SIFEN field {@code E654} ({@code dDMoneCuo}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E653}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E654", xml = "dDMoneCuo")
  public @Nullable String currencyDescription() {
    return currencyDescription;
  }

  /**
   * Monto de cada cuota.
   *
   * <p>SIFEN field {@code E651} ({@code dMonCuota}).
   *
   * @return the value
   */
  @SifenField(id = "E651", xml = "dMonCuota")
  public BigDecimal amount() {
    return amount;
  }

  /**
   * Fecha de vencimiento de cada cuota.
   *
   * <p>SIFEN field {@code E652} ({@code dVencCuo}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E652", xml = "dVencCuo")
  public @Nullable LocalDate dueDate() {
    return dueDate;
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
    if (!(other instanceof Installment)) {
      return false;
    }
    Installment that = (Installment) other;
    return Objects.equals(currencyCode, that.currencyCode)
        && Objects.equals(currencyDescription, that.currencyDescription)
        && Objects.equals(amount, that.amount)
        && Objects.equals(dueDate, that.dueDate);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(currencyCode, currencyDescription, amount, dueDate);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "Installment";
  }

  /**
   * Builder for {@link Installment}. Not thread-safe; {@link #build()} returns an immutable copy.
   */
  public static final class Builder {

    private @Nullable String currencyCode;
    private @Nullable String currencyDescription;
    private @Nullable BigDecimal amount;
    private @Nullable LocalDate dueDate;

    private Builder() {}

    private Builder(Installment source) {
      this.currencyCode = source.currencyCode;
      this.currencyDescription = source.currencyDescription;
      this.amount = source.amount;
      this.dueDate = source.dueDate;
    }

    /**
     * Sets {@code currencyCode} (E653, {@code cMoneCuo}).
     *
     * @param currencyCode the value
     * @return this builder
     */
    public Builder currencyCode(String currencyCode) {
      this.currencyCode = currencyCode;
      return this;
    }

    /**
     * Sets {@code currencyDescription} (E654, {@code dDMoneCuo}).
     *
     * @param currencyDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder currencyDescription(@Nullable String currencyDescription) {
      this.currencyDescription = currencyDescription;
      return this;
    }

    /**
     * Sets {@code amount} (E651, {@code dMonCuota}).
     *
     * @param amount the value
     * @return this builder
     */
    public Builder amount(BigDecimal amount) {
      this.amount = amount;
      return this;
    }

    /**
     * Sets {@code dueDate} (E652, {@code dVencCuo}).
     *
     * @param dueDate the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder dueDate(@Nullable LocalDate dueDate) {
      this.dueDate = dueDate;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public Installment build() {
      return new Installment(this);
    }
  }
}
