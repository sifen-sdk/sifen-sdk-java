/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import io.github.sifensdk.core.catalog.VehicleIdentificationType;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Campos que identifican al vehículo del traslado de mercaderías.
 *
 * <p>SIFEN group {@code E960} ({@code gVehTras}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "E960", xml = "gVehTras")
public final class TransportVehicle {

  private final String type;
  private final String brand;
  private final String identificationTypeCode;
  private final @Nullable String identificationNumber;
  private final @Nullable String additionalData;
  private final @Nullable String plateNumber;
  private final @Nullable String flightNumber;

  private TransportVehicle(Builder b) {
    this.type = Checks.required(b.type, "type", "E961", "dTiVehTras");
    this.brand = Checks.required(b.brand, "brand", "E962", "dMarVeh");
    this.identificationTypeCode =
        Checks.required(b.identificationTypeCode, "identificationTypeCode", "E967", "dTipIdenVeh");
    this.identificationNumber = b.identificationNumber;
    this.additionalData = b.additionalData;
    this.plateNumber = b.plateNumber;
    this.flightNumber = b.flightNumber;
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
   * Tipo de vehículo.
   *
   * <p>SIFEN field {@code E961} ({@code dTiVehTras}).
   *
   * @return the value
   */
  @SifenField(id = "E961", xml = "dTiVehTras")
  public String type() {
    return type;
  }

  /**
   * Marca.
   *
   * <p>SIFEN field {@code E962} ({@code dMarVeh}).
   *
   * @return the value
   */
  @SifenField(id = "E962", xml = "dMarVeh")
  public String brand() {
    return brand;
  }

  /**
   * Tipo de identificación del vehículo.
   *
   * <p>SIFEN field {@code E967} ({@code dTipIdenVeh}).
   *
   * @return the value
   */
  @SifenField(id = "E967", xml = "dTipIdenVeh")
  public String identificationTypeCode() {
    return identificationTypeCode;
  }

  /**
   * Returns {@link #identificationTypeCode()} (E967) as {@link VehicleIdentificationType}.
   *
   * @return the constant, or empty if the code is not set or this SDK version does not know it
   */
  public Optional<VehicleIdentificationType> identificationType() {
    return VehicleIdentificationType.fromCode(identificationTypeCode);
  }

  /**
   * Número de identificación del vehículo.
   *
   * <p>SIFEN field {@code E963} ({@code dNroIDVeh}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E963", xml = "dNroIDVeh")
  public @Nullable String identificationNumber() {
    return identificationNumber;
  }

  /**
   * Datos adicionales del vehículo.
   *
   * <p>SIFEN field {@code E964} ({@code dAdicVeh}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E964", xml = "dAdicVeh")
  public @Nullable String additionalData() {
    return additionalData;
  }

  /**
   * Número de matrícula del vehículo.
   *
   * <p>SIFEN field {@code E965} ({@code dNroMatVeh}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E965", xml = "dNroMatVeh")
  public @Nullable String plateNumber() {
    return plateNumber;
  }

  /**
   * Número de vuelo.
   *
   * <p>SIFEN field {@code E966} ({@code dNroVuelo}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E966", xml = "dNroVuelo")
  public @Nullable String flightNumber() {
    return flightNumber;
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
    if (!(other instanceof TransportVehicle)) {
      return false;
    }
    TransportVehicle that = (TransportVehicle) other;
    return Objects.equals(type, that.type)
        && Objects.equals(brand, that.brand)
        && Objects.equals(identificationTypeCode, that.identificationTypeCode)
        && Objects.equals(identificationNumber, that.identificationNumber)
        && Objects.equals(additionalData, that.additionalData)
        && Objects.equals(plateNumber, that.plateNumber)
        && Objects.equals(flightNumber, that.flightNumber);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        type,
        brand,
        identificationTypeCode,
        identificationNumber,
        additionalData,
        plateNumber,
        flightNumber);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "TransportVehicle";
  }

  /**
   * Builder for {@link TransportVehicle}. Not thread-safe; {@link #build()} returns an immutable
   * copy.
   */
  public static final class Builder {

    private @Nullable String type;
    private @Nullable String brand;
    private @Nullable String identificationTypeCode;
    private @Nullable String identificationNumber;
    private @Nullable String additionalData;
    private @Nullable String plateNumber;
    private @Nullable String flightNumber;

    private Builder() {}

    private Builder(TransportVehicle source) {
      this.type = source.type;
      this.brand = source.brand;
      this.identificationTypeCode = source.identificationTypeCode;
      this.identificationNumber = source.identificationNumber;
      this.additionalData = source.additionalData;
      this.plateNumber = source.plateNumber;
      this.flightNumber = source.flightNumber;
    }

    /**
     * Sets {@code type} (E961, {@code dTiVehTras}).
     *
     * @param type the value
     * @return this builder
     */
    public Builder type(String type) {
      this.type = type;
      return this;
    }

    /**
     * Sets {@code brand} (E962, {@code dMarVeh}).
     *
     * @param brand the value
     * @return this builder
     */
    public Builder brand(String brand) {
      this.brand = brand;
      return this;
    }

    /**
     * Sets {@code identificationTypeCode} (E967, {@code dTipIdenVeh}).
     *
     * @param identificationTypeCode the value
     * @return this builder
     */
    public Builder identificationTypeCode(String identificationTypeCode) {
      this.identificationTypeCode = identificationTypeCode;
      return this;
    }

    /**
     * Sets {@code identificationTypeCode} (E967, {@code dTipIdenVeh}) from {@link
     * VehicleIdentificationType}.
     *
     * @param identificationType the catalog constant
     * @return this builder
     */
    public Builder identificationType(VehicleIdentificationType identificationType) {
      this.identificationTypeCode = identificationType.code();
      return this;
    }

    /**
     * Sets {@code identificationNumber} (E963, {@code dNroIDVeh}).
     *
     * @param identificationNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder identificationNumber(@Nullable String identificationNumber) {
      this.identificationNumber = identificationNumber;
      return this;
    }

    /**
     * Sets {@code additionalData} (E964, {@code dAdicVeh}).
     *
     * @param additionalData the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder additionalData(@Nullable String additionalData) {
      this.additionalData = additionalData;
      return this;
    }

    /**
     * Sets {@code plateNumber} (E965, {@code dNroMatVeh}).
     *
     * @param plateNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder plateNumber(@Nullable String plateNumber) {
      this.plateNumber = plateNumber;
      return this;
    }

    /**
     * Sets {@code flightNumber} (E966, {@code dNroVuelo}).
     *
     * @param flightNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder flightNumber(@Nullable String flightNumber) {
      this.flightNumber = flightNumber;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public TransportVehicle build() {
      return new TransportVehicle(this);
    }
  }
}
