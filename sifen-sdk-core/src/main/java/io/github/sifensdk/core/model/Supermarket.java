/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.math.BigDecimal;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Grupo del sector supermercados.
 *
 * <p>SIFEN group {@code E810} ({@code gGrupSup}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "E810", xml = "gGrupSup")
public final class Supermarket {

  private final @Nullable String cashierName;
  private final @Nullable BigDecimal cashAmount;
  private final @Nullable BigDecimal changeAmount;
  private final @Nullable BigDecimal donationAmount;
  private final @Nullable String donationDescription;

  private Supermarket(Builder b) {
    this.cashierName = b.cashierName;
    this.cashAmount = b.cashAmount;
    this.changeAmount = b.changeAmount;
    this.donationAmount = b.donationAmount;
    this.donationDescription = b.donationDescription;
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
   * Nombre del cajero.
   *
   * <p>SIFEN field {@code E811} ({@code dNomCaj}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E811", xml = "dNomCaj")
  public @Nullable String cashierName() {
    return cashierName;
  }

  /**
   * Efectivo.
   *
   * <p>SIFEN field {@code E812} ({@code dEfectivo}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E812", xml = "dEfectivo")
  public @Nullable BigDecimal cashAmount() {
    return cashAmount;
  }

  /**
   * Vuelto.
   *
   * <p>SIFEN field {@code E813} ({@code dVuelto}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E813", xml = "dVuelto")
  public @Nullable BigDecimal changeAmount() {
    return changeAmount;
  }

  /**
   * Monto de la donación.
   *
   * <p>SIFEN field {@code E814} ({@code dDonac}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E814", xml = "dDonac")
  public @Nullable BigDecimal donationAmount() {
    return donationAmount;
  }

  /**
   * Descripción de la donación.
   *
   * <p>SIFEN field {@code E815} ({@code dDesDonac}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E815", xml = "dDesDonac")
  public @Nullable String donationDescription() {
    return donationDescription;
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
    if (!(other instanceof Supermarket)) {
      return false;
    }
    Supermarket that = (Supermarket) other;
    return Objects.equals(cashierName, that.cashierName)
        && Objects.equals(cashAmount, that.cashAmount)
        && Objects.equals(changeAmount, that.changeAmount)
        && Objects.equals(donationAmount, that.donationAmount)
        && Objects.equals(donationDescription, that.donationDescription);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(cashierName, cashAmount, changeAmount, donationAmount, donationDescription);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "Supermarket";
  }

  /**
   * Builder for {@link Supermarket}. Not thread-safe; {@link #build()} returns an immutable copy.
   */
  public static final class Builder {

    private @Nullable String cashierName;
    private @Nullable BigDecimal cashAmount;
    private @Nullable BigDecimal changeAmount;
    private @Nullable BigDecimal donationAmount;
    private @Nullable String donationDescription;

    private Builder() {}

    private Builder(Supermarket source) {
      this.cashierName = source.cashierName;
      this.cashAmount = source.cashAmount;
      this.changeAmount = source.changeAmount;
      this.donationAmount = source.donationAmount;
      this.donationDescription = source.donationDescription;
    }

    /**
     * Sets {@code cashierName} (E811, {@code dNomCaj}).
     *
     * @param cashierName the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder cashierName(@Nullable String cashierName) {
      this.cashierName = cashierName;
      return this;
    }

    /**
     * Sets {@code cashAmount} (E812, {@code dEfectivo}).
     *
     * @param cashAmount the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder cashAmount(@Nullable BigDecimal cashAmount) {
      this.cashAmount = cashAmount;
      return this;
    }

    /**
     * Sets {@code changeAmount} (E813, {@code dVuelto}).
     *
     * @param changeAmount the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder changeAmount(@Nullable BigDecimal changeAmount) {
      this.changeAmount = changeAmount;
      return this;
    }

    /**
     * Sets {@code donationAmount} (E814, {@code dDonac}).
     *
     * @param donationAmount the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder donationAmount(@Nullable BigDecimal donationAmount) {
      this.donationAmount = donationAmount;
      return this;
    }

    /**
     * Sets {@code donationDescription} (E815, {@code dDesDonac}).
     *
     * @param donationDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder donationDescription(@Nullable String donationDescription) {
      this.donationDescription = donationDescription;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     */
    public Supermarket build() {
      return new Supermarket(this);
    }
  }
}
