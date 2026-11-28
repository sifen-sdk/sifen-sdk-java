/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import io.github.sifensdk.core.catalog.PaymentType;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Campos que describen la forma de pago al contado o del monto de la entrega inicial.
 *
 * <p>SIFEN group {@code E605} ({@code gPaConEIni}). Immutable and thread-safe; create it with
 * {@link #builder()}. The builder only checks what Java needs (required elements and non-null
 * lists); the SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step,
 * never here. Amounts are kept exactly as given.
 */
@SifenField(id = "E605", xml = "gPaConEIni")
public final class Payment {

  private final String typeCode;
  private final @Nullable String typeDescription;
  private final BigDecimal amount;
  private final String currencyCode;
  private final @Nullable String currencyDescription;
  private final @Nullable BigDecimal exchangeRate;
  private final @Nullable CardPayment card;
  private final @Nullable CheckPayment check;

  private Payment(Builder b) {
    this.typeCode = Checks.required(b.typeCode, "typeCode", "E606", "iTiPago");
    this.typeDescription = b.typeDescription;
    this.amount = Checks.required(b.amount, "amount", "E608", "dMonTiPag");
    this.currencyCode = Checks.required(b.currencyCode, "currencyCode", "E609", "cMoneTiPag");
    this.currencyDescription = b.currencyDescription;
    this.exchangeRate = b.exchangeRate;
    this.card = b.card;
    this.check = b.check;
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
   * Tipo de pago.
   *
   * <p>SIFEN field {@code E606} ({@code iTiPago}).
   *
   * @return the value
   */
  @SifenField(id = "E606", xml = "iTiPago")
  public String typeCode() {
    return typeCode;
  }

  /**
   * Returns {@link #typeCode()} (E606) as {@link PaymentType}.
   *
   * @return the constant, or empty if the code is not set or this SDK version does not know it
   */
  public Optional<PaymentType> type() {
    return PaymentType.fromCode(typeCode);
  }

  /**
   * Descripción del tipo de pago.
   *
   * <p>SIFEN field {@code E607} ({@code dDesTiPag}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E606}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E607", xml = "dDesTiPag")
  public @Nullable String typeDescription() {
    return typeDescription;
  }

  /**
   * Monto por tipo de pago.
   *
   * <p>SIFEN field {@code E608} ({@code dMonTiPag}).
   *
   * @return the value
   */
  @SifenField(id = "E608", xml = "dMonTiPag")
  public BigDecimal amount() {
    return amount;
  }

  /**
   * Moneda por tipo de pago.
   *
   * <p>SIFEN field {@code E609} ({@code cMoneTiPag}).
   *
   * @return the value
   */
  @SifenField(id = "E609", xml = "cMoneTiPag")
  public String currencyCode() {
    return currencyCode;
  }

  /**
   * Returns {@link #currencyCode()} (E609) as {@link Currency}.
   *
   * @return the currency, or empty if the code is not set or is not an ISO 4217 code
   */
  public Optional<Currency> currency() {
    try {
      return Optional.of(Currency.getInstance(currencyCode));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  /**
   * Descripción de la moneda por tipo de pago.
   *
   * <p>SIFEN field {@code E610} ({@code dDMoneTiPag}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E609}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E610", xml = "dDMoneTiPag")
  public @Nullable String currencyDescription() {
    return currencyDescription;
  }

  /**
   * Tipo de cambio por tipo de pago.
   *
   * <p>SIFEN field {@code E611} ({@code dTiCamTiPag}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E611", xml = "dTiCamTiPag")
  public @Nullable BigDecimal exchangeRate() {
    return exchangeRate;
  }

  /**
   * Campos que describen el pago o entrega inicial de la operación con tarjeta de crédito/débito.
   *
   * <p>SIFEN group {@code E620} ({@code gPagTarCD}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E620", xml = "gPagTarCD")
  public @Nullable CardPayment card() {
    return card;
  }

  /**
   * Campos que describen el pago o entrega inicial de la operación con cheque.
   *
   * <p>SIFEN group {@code E630} ({@code gPagCheq}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E630", xml = "gPagCheq")
  public @Nullable CheckPayment check() {
    return check;
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
    if (!(other instanceof Payment)) {
      return false;
    }
    Payment that = (Payment) other;
    return Objects.equals(typeCode, that.typeCode)
        && Objects.equals(typeDescription, that.typeDescription)
        && Objects.equals(amount, that.amount)
        && Objects.equals(currencyCode, that.currencyCode)
        && Objects.equals(currencyDescription, that.currencyDescription)
        && Objects.equals(exchangeRate, that.exchangeRate)
        && Objects.equals(card, that.card)
        && Objects.equals(check, that.check);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        typeCode,
        typeDescription,
        amount,
        currencyCode,
        currencyDescription,
        exchangeRate,
        card,
        check);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "Payment";
  }

  /** Builder for {@link Payment}. Not thread-safe; {@link #build()} returns an immutable copy. */
  public static final class Builder {

    private @Nullable String typeCode;
    private @Nullable String typeDescription;
    private @Nullable BigDecimal amount;
    private @Nullable String currencyCode;
    private @Nullable String currencyDescription;
    private @Nullable BigDecimal exchangeRate;
    private @Nullable CardPayment card;
    private @Nullable CheckPayment check;

    private Builder() {}

    private Builder(Payment source) {
      this.typeCode = source.typeCode;
      this.typeDescription = source.typeDescription;
      this.amount = source.amount;
      this.currencyCode = source.currencyCode;
      this.currencyDescription = source.currencyDescription;
      this.exchangeRate = source.exchangeRate;
      this.card = source.card;
      this.check = source.check;
    }

    /**
     * Sets {@code typeCode} (E606, {@code iTiPago}).
     *
     * @param typeCode the value
     * @return this builder
     */
    public Builder typeCode(String typeCode) {
      this.typeCode = typeCode;
      return this;
    }

    /**
     * Sets {@code typeCode} (E606, {@code iTiPago}) from {@link PaymentType}.
     *
     * @param type the catalog constant
     * @return this builder
     */
    public Builder type(PaymentType type) {
      this.typeCode = type.code();
      return this;
    }

    /**
     * Sets {@code typeDescription} (E607, {@code dDesTiPag}).
     *
     * @param typeDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder typeDescription(@Nullable String typeDescription) {
      this.typeDescription = typeDescription;
      return this;
    }

    /**
     * Sets {@code amount} (E608, {@code dMonTiPag}).
     *
     * @param amount the value
     * @return this builder
     */
    public Builder amount(BigDecimal amount) {
      this.amount = amount;
      return this;
    }

    /**
     * Sets {@code currencyCode} (E609, {@code cMoneTiPag}).
     *
     * @param currencyCode the value
     * @return this builder
     */
    public Builder currencyCode(String currencyCode) {
      this.currencyCode = currencyCode;
      return this;
    }

    /**
     * Sets {@code currencyCode} (E609, {@code cMoneTiPag}) from {@link Currency}.
     *
     * @param currency the currency (ISO 4217)
     * @return this builder
     */
    public Builder currency(Currency currency) {
      this.currencyCode = currency.getCurrencyCode();
      return this;
    }

    /**
     * Sets {@code currencyDescription} (E610, {@code dDMoneTiPag}).
     *
     * @param currencyDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder currencyDescription(@Nullable String currencyDescription) {
      this.currencyDescription = currencyDescription;
      return this;
    }

    /**
     * Sets {@code exchangeRate} (E611, {@code dTiCamTiPag}).
     *
     * @param exchangeRate the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder exchangeRate(@Nullable BigDecimal exchangeRate) {
      this.exchangeRate = exchangeRate;
      return this;
    }

    /**
     * Sets {@code card} (E620, {@code gPagTarCD}).
     *
     * @param card the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder card(@Nullable CardPayment card) {
      this.card = card;
      return this;
    }

    /**
     * Sets {@code check} (E630, {@code gPagCheq}).
     *
     * @param check the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder check(@Nullable CheckPayment check) {
      this.check = check;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public Payment build() {
      return new Payment(this);
    }
  }
}
