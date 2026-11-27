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
 * Campos que componen la Nota de Remisión Electrónica.
 *
 * <p>SIFEN group {@code E500} ({@code gCamNRE}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "E500", xml = "gCamNRE")
public final class DeliveryNote {

  private final String reasonCode;
  private final @Nullable String reasonDescription;
  private final String responsibleCode;
  private final @Nullable String responsibleDescription;
  private final Integer estimatedKilometers;
  private final @Nullable LocalDate futureInvoiceDate;
  private final @Nullable BigDecimal freightAmount;

  private DeliveryNote(Builder b) {
    this.reasonCode = Checks.required(b.reasonCode, "reasonCode", "E501", "iMotEmiNR");
    this.reasonDescription = b.reasonDescription;
    this.responsibleCode =
        Checks.required(b.responsibleCode, "responsibleCode", "E503", "iRespEmiNR");
    this.responsibleDescription = b.responsibleDescription;
    this.estimatedKilometers =
        Checks.required(b.estimatedKilometers, "estimatedKilometers", "E505", "dKmR");
    this.futureInvoiceDate = b.futureInvoiceDate;
    this.freightAmount = b.freightAmount;
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
   * Motivo de emisión.
   *
   * <p>SIFEN field {@code E501} ({@code iMotEmiNR}).
   *
   * @return the value
   */
  @SifenField(id = "E501", xml = "iMotEmiNR")
  public String reasonCode() {
    return reasonCode;
  }

  /**
   * Descripción del motivo de emisión.
   *
   * <p>SIFEN field {@code E502} ({@code dDesMotEmiNR}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E501}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E502", xml = "dDesMotEmiNR")
  public @Nullable String reasonDescription() {
    return reasonDescription;
  }

  /**
   * Responsable de la emisión de la Nota Remisión Electrónica.
   *
   * <p>SIFEN field {@code E503} ({@code iRespEmiNR}).
   *
   * @return the value
   */
  @SifenField(id = "E503", xml = "iRespEmiNR")
  public String responsibleCode() {
    return responsibleCode;
  }

  /**
   * Descripción del responsable de la emisión de la Nota de Remisión Electrónica.
   *
   * <p>SIFEN field {@code E504} ({@code dDesRespEmiNR}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E503}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E504", xml = "dDesRespEmiNR")
  public @Nullable String responsibleDescription() {
    return responsibleDescription;
  }

  /**
   * Kilómetros estimados de recorrido.
   *
   * <p>SIFEN field {@code E505} ({@code dKmR}).
   *
   * @return the value
   */
  @SifenField(id = "E505", xml = "dKmR")
  public Integer estimatedKilometers() {
    return estimatedKilometers;
  }

  /**
   * Fecha futura de emisión de la factura.
   *
   * <p>SIFEN field {@code E506} ({@code dFecEm}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E506", xml = "dFecEm")
  public @Nullable LocalDate futureInvoiceDate() {
    return futureInvoiceDate;
  }

  /**
   * Costo del Flete.
   *
   * <p>SIFEN field {@code E507} ({@code cPreFle}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E507", xml = "cPreFle")
  public @Nullable BigDecimal freightAmount() {
    return freightAmount;
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
    if (!(other instanceof DeliveryNote)) {
      return false;
    }
    DeliveryNote that = (DeliveryNote) other;
    return Objects.equals(reasonCode, that.reasonCode)
        && Objects.equals(reasonDescription, that.reasonDescription)
        && Objects.equals(responsibleCode, that.responsibleCode)
        && Objects.equals(responsibleDescription, that.responsibleDescription)
        && Objects.equals(estimatedKilometers, that.estimatedKilometers)
        && Objects.equals(futureInvoiceDate, that.futureInvoiceDate)
        && Objects.equals(freightAmount, that.freightAmount);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        reasonCode,
        reasonDescription,
        responsibleCode,
        responsibleDescription,
        estimatedKilometers,
        futureInvoiceDate,
        freightAmount);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "DeliveryNote";
  }

  /**
   * Builder for {@link DeliveryNote}. Not thread-safe; {@link #build()} returns an immutable copy.
   */
  public static final class Builder {

    private @Nullable String reasonCode;
    private @Nullable String reasonDescription;
    private @Nullable String responsibleCode;
    private @Nullable String responsibleDescription;
    private @Nullable Integer estimatedKilometers;
    private @Nullable LocalDate futureInvoiceDate;
    private @Nullable BigDecimal freightAmount;

    private Builder() {}

    private Builder(DeliveryNote source) {
      this.reasonCode = source.reasonCode;
      this.reasonDescription = source.reasonDescription;
      this.responsibleCode = source.responsibleCode;
      this.responsibleDescription = source.responsibleDescription;
      this.estimatedKilometers = source.estimatedKilometers;
      this.futureInvoiceDate = source.futureInvoiceDate;
      this.freightAmount = source.freightAmount;
    }

    /**
     * Sets {@code reasonCode} (E501, {@code iMotEmiNR}).
     *
     * @param reasonCode the value
     * @return this builder
     */
    public Builder reasonCode(String reasonCode) {
      this.reasonCode = reasonCode;
      return this;
    }

    /**
     * Sets {@code reasonDescription} (E502, {@code dDesMotEmiNR}).
     *
     * @param reasonDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder reasonDescription(@Nullable String reasonDescription) {
      this.reasonDescription = reasonDescription;
      return this;
    }

    /**
     * Sets {@code responsibleCode} (E503, {@code iRespEmiNR}).
     *
     * @param responsibleCode the value
     * @return this builder
     */
    public Builder responsibleCode(String responsibleCode) {
      this.responsibleCode = responsibleCode;
      return this;
    }

    /**
     * Sets {@code responsibleDescription} (E504, {@code dDesRespEmiNR}).
     *
     * @param responsibleDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder responsibleDescription(@Nullable String responsibleDescription) {
      this.responsibleDescription = responsibleDescription;
      return this;
    }

    /**
     * Sets {@code estimatedKilometers} (E505, {@code dKmR}).
     *
     * @param estimatedKilometers the value
     * @return this builder
     */
    public Builder estimatedKilometers(Integer estimatedKilometers) {
      this.estimatedKilometers = estimatedKilometers;
      return this;
    }

    /**
     * Sets {@code futureInvoiceDate} (E506, {@code dFecEm}).
     *
     * @param futureInvoiceDate the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder futureInvoiceDate(@Nullable LocalDate futureInvoiceDate) {
      this.futureInvoiceDate = futureInvoiceDate;
      return this;
    }

    /**
     * Sets {@code freightAmount} (E507, {@code cPreFle}).
     *
     * @param freightAmount the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder freightAmount(@Nullable BigDecimal freightAmount) {
      this.freightAmount = freightAmount;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public DeliveryNote build() {
      return new DeliveryNote(this);
    }
  }
}
