/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Campos que describen el pago o entrega inicial de la operación con tarjeta de crédito/débito.
 *
 * <p>SIFEN group {@code E620} ({@code gPagTarCD}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "E620", xml = "gPagTarCD")
public final class CardPayment {

  private final String brandCode;
  private final @Nullable String brandDescription;
  private final @Nullable String processorName;
  private final @Nullable String processorRuc;
  private final @Nullable String processorCheckDigit;
  private final String processingMethodCode;
  private final @Nullable String authorizationCode;
  private final @Nullable String holderName;
  private final @Nullable String number;

  private CardPayment(Builder b) {
    this.brandCode = Checks.required(b.brandCode, "brandCode", "E621", "iDenTarj");
    this.brandDescription = b.brandDescription;
    this.processorName = b.processorName;
    this.processorRuc = b.processorRuc;
    this.processorCheckDigit = b.processorCheckDigit;
    this.processingMethodCode =
        Checks.required(b.processingMethodCode, "processingMethodCode", "E626", "iForProPa");
    this.authorizationCode = b.authorizationCode;
    this.holderName = b.holderName;
    this.number = b.number;
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
   * Denominación de la tarjeta.
   *
   * <p>SIFEN field {@code E621} ({@code iDenTarj}).
   *
   * @return the value
   */
  @SifenField(id = "E621", xml = "iDenTarj")
  public String brandCode() {
    return brandCode;
  }

  /**
   * Descripción de denominación de la tarjeta.
   *
   * <p>SIFEN field {@code E622} ({@code dDesDenTarj}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E621}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E622", xml = "dDesDenTarj")
  public @Nullable String brandDescription() {
    return brandDescription;
  }

  /**
   * Razón social de la procesadora de tarjeta.
   *
   * <p>SIFEN field {@code E623} ({@code dRSProTar}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E623", xml = "dRSProTar")
  public @Nullable String processorName() {
    return processorName;
  }

  /**
   * RUC de la procesadora de tarjeta.
   *
   * <p>SIFEN field {@code E624} ({@code dRUCProTar}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E624", xml = "dRUCProTar")
  public @Nullable String processorRuc() {
    return processorRuc;
  }

  /**
   * Dígito verificador del RUC de la procesadora de tarjeta.
   *
   * <p>SIFEN field {@code E625} ({@code dDVProTar}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E625", xml = "dDVProTar")
  public @Nullable String processorCheckDigit() {
    return processorCheckDigit;
  }

  /**
   * Forma de procesamiento de pago.
   *
   * <p>SIFEN field {@code E626} ({@code iForProPa}).
   *
   * @return the value
   */
  @SifenField(id = "E626", xml = "iForProPa")
  public String processingMethodCode() {
    return processingMethodCode;
  }

  /**
   * Código de autorización de la operación.
   *
   * <p>SIFEN field {@code E627} ({@code dCodAuOpe}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E627", xml = "dCodAuOpe")
  public @Nullable String authorizationCode() {
    return authorizationCode;
  }

  /**
   * Nombre del titular de la tarjeta.
   *
   * <p>SIFEN field {@code E628} ({@code dNomTit}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E628", xml = "dNomTit")
  public @Nullable String holderName() {
    return holderName;
  }

  /**
   * Número de la tarjeta.
   *
   * <p>SIFEN field {@code E629} ({@code dNumTarj}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E629", xml = "dNumTarj")
  public @Nullable String number() {
    return number;
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
    if (!(other instanceof CardPayment)) {
      return false;
    }
    CardPayment that = (CardPayment) other;
    return Objects.equals(brandCode, that.brandCode)
        && Objects.equals(brandDescription, that.brandDescription)
        && Objects.equals(processorName, that.processorName)
        && Objects.equals(processorRuc, that.processorRuc)
        && Objects.equals(processorCheckDigit, that.processorCheckDigit)
        && Objects.equals(processingMethodCode, that.processingMethodCode)
        && Objects.equals(authorizationCode, that.authorizationCode)
        && Objects.equals(holderName, that.holderName)
        && Objects.equals(number, that.number);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        brandCode,
        brandDescription,
        processorName,
        processorRuc,
        processorCheckDigit,
        processingMethodCode,
        authorizationCode,
        holderName,
        number);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "CardPayment";
  }

  /**
   * Builder for {@link CardPayment}. Not thread-safe; {@link #build()} returns an immutable copy.
   */
  public static final class Builder {

    private @Nullable String brandCode;
    private @Nullable String brandDescription;
    private @Nullable String processorName;
    private @Nullable String processorRuc;
    private @Nullable String processorCheckDigit;
    private @Nullable String processingMethodCode;
    private @Nullable String authorizationCode;
    private @Nullable String holderName;
    private @Nullable String number;

    private Builder() {}

    private Builder(CardPayment source) {
      this.brandCode = source.brandCode;
      this.brandDescription = source.brandDescription;
      this.processorName = source.processorName;
      this.processorRuc = source.processorRuc;
      this.processorCheckDigit = source.processorCheckDigit;
      this.processingMethodCode = source.processingMethodCode;
      this.authorizationCode = source.authorizationCode;
      this.holderName = source.holderName;
      this.number = source.number;
    }

    /**
     * Sets {@code brandCode} (E621, {@code iDenTarj}).
     *
     * @param brandCode the value
     * @return this builder
     */
    public Builder brandCode(String brandCode) {
      this.brandCode = brandCode;
      return this;
    }

    /**
     * Sets {@code brandDescription} (E622, {@code dDesDenTarj}).
     *
     * @param brandDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder brandDescription(@Nullable String brandDescription) {
      this.brandDescription = brandDescription;
      return this;
    }

    /**
     * Sets {@code processorName} (E623, {@code dRSProTar}).
     *
     * @param processorName the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder processorName(@Nullable String processorName) {
      this.processorName = processorName;
      return this;
    }

    /**
     * Sets {@code processorRuc} (E624, {@code dRUCProTar}).
     *
     * @param processorRuc the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder processorRuc(@Nullable String processorRuc) {
      this.processorRuc = processorRuc;
      return this;
    }

    /**
     * Sets {@code processorCheckDigit} (E625, {@code dDVProTar}).
     *
     * @param processorCheckDigit the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder processorCheckDigit(@Nullable String processorCheckDigit) {
      this.processorCheckDigit = processorCheckDigit;
      return this;
    }

    /**
     * Sets {@code processingMethodCode} (E626, {@code iForProPa}).
     *
     * @param processingMethodCode the value
     * @return this builder
     */
    public Builder processingMethodCode(String processingMethodCode) {
      this.processingMethodCode = processingMethodCode;
      return this;
    }

    /**
     * Sets {@code authorizationCode} (E627, {@code dCodAuOpe}).
     *
     * @param authorizationCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder authorizationCode(@Nullable String authorizationCode) {
      this.authorizationCode = authorizationCode;
      return this;
    }

    /**
     * Sets {@code holderName} (E628, {@code dNomTit}).
     *
     * @param holderName the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder holderName(@Nullable String holderName) {
      this.holderName = holderName;
      return this;
    }

    /**
     * Sets {@code number} (E629, {@code dNumTarj}).
     *
     * @param number the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder number(@Nullable String number) {
      this.number = number;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public CardPayment build() {
      return new CardPayment(this);
    }
  }
}
