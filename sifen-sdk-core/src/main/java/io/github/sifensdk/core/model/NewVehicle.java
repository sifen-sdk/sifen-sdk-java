/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import io.github.sifensdk.core.catalog.FuelType;
import io.github.sifensdk.core.catalog.VehicleSaleType;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Grupo de detalle de vehículos nuevos.
 *
 * <p>SIFEN group {@code E770} ({@code gVehNuevo}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "E770", xml = "gVehNuevo")
public final class NewVehicle {

  private final @Nullable String saleOperationTypeCode;
  private final @Nullable String saleOperationTypeDescription;
  private final @Nullable String chassis;
  private final @Nullable String color;
  private final @Nullable Integer enginePower;
  private final @Nullable Integer engineCapacity;
  private final @Nullable BigDecimal netWeight;
  private final @Nullable BigDecimal grossWeight;
  private final @Nullable String fuelTypeCode;
  private final @Nullable String fuelTypeDescription;
  private final @Nullable String engineNumber;
  private final @Nullable BigDecimal maxTractionCapacity;
  private final @Nullable Integer manufactureYear;
  private final @Nullable String vehicleType;
  private final @Nullable Integer passengerCapacity;
  private final @Nullable String cylinders;

  private NewVehicle(Builder b) {
    this.saleOperationTypeCode = b.saleOperationTypeCode;
    this.saleOperationTypeDescription = b.saleOperationTypeDescription;
    this.chassis = b.chassis;
    this.color = b.color;
    this.enginePower = b.enginePower;
    this.engineCapacity = b.engineCapacity;
    this.netWeight = b.netWeight;
    this.grossWeight = b.grossWeight;
    this.fuelTypeCode = b.fuelTypeCode;
    this.fuelTypeDescription = b.fuelTypeDescription;
    this.engineNumber = b.engineNumber;
    this.maxTractionCapacity = b.maxTractionCapacity;
    this.manufactureYear = b.manufactureYear;
    this.vehicleType = b.vehicleType;
    this.passengerCapacity = b.passengerCapacity;
    this.cylinders = b.cylinders;
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
   * Tipo de operación de venta de vehículos.
   *
   * <p>SIFEN field {@code E771} ({@code iTipOpVN}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E771", xml = "iTipOpVN")
  public @Nullable String saleOperationTypeCode() {
    return saleOperationTypeCode;
  }

  /**
   * Returns {@link #saleOperationTypeCode()} (E771) as {@link VehicleSaleType}.
   *
   * @return the constant, or empty if the code is not set or this SDK version does not know it
   */
  public Optional<VehicleSaleType> saleOperationType() {
    return saleOperationTypeCode == null
        ? Optional.empty()
        : VehicleSaleType.fromCode(saleOperationTypeCode);
  }

  /**
   * Descripción del tipo de operación de venta de vehículos.
   *
   * <p>SIFEN field {@code E772} ({@code dDesTipOpVN}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E771}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E772", xml = "dDesTipOpVN")
  public @Nullable String saleOperationTypeDescription() {
    return saleOperationTypeDescription;
  }

  /**
   * Chasis del vehículo.
   *
   * <p>SIFEN field {@code E773} ({@code dChasis}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E773", xml = "dChasis")
  public @Nullable String chassis() {
    return chassis;
  }

  /**
   * Color del vehículo.
   *
   * <p>SIFEN field {@code E774} ({@code dColor}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E774", xml = "dColor")
  public @Nullable String color() {
    return color;
  }

  /**
   * Potencia del motor (CV).
   *
   * <p>SIFEN field {@code E775} ({@code dPotencia}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E775", xml = "dPotencia")
  public @Nullable Integer enginePower() {
    return enginePower;
  }

  /**
   * Capacidad del motor.
   *
   * <p>SIFEN field {@code E776} ({@code dCapMot}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E776", xml = "dCapMot")
  public @Nullable Integer engineCapacity() {
    return engineCapacity;
  }

  /**
   * Peso Neto.
   *
   * <p>SIFEN field {@code E777} ({@code dPNet}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E777", xml = "dPNet")
  public @Nullable BigDecimal netWeight() {
    return netWeight;
  }

  /**
   * Peso Bruto.
   *
   * <p>SIFEN field {@code E778} ({@code dPBruto}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E778", xml = "dPBruto")
  public @Nullable BigDecimal grossWeight() {
    return grossWeight;
  }

  /**
   * Tipo de combustible.
   *
   * <p>SIFEN field {@code E779} ({@code iTipCom}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E779", xml = "iTipCom")
  public @Nullable String fuelTypeCode() {
    return fuelTypeCode;
  }

  /**
   * Returns {@link #fuelTypeCode()} (E779) as {@link FuelType}.
   *
   * @return the constant, or empty if the code is not set or this SDK version does not know it
   */
  public Optional<FuelType> fuelType() {
    return fuelTypeCode == null ? Optional.empty() : FuelType.fromCode(fuelTypeCode);
  }

  /**
   * Descripción del tipo de combustible.
   *
   * <p>SIFEN field {@code E780} ({@code dDesTipCom}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E779}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E780", xml = "dDesTipCom")
  public @Nullable String fuelTypeDescription() {
    return fuelTypeDescription;
  }

  /**
   * Número del motor.
   *
   * <p>SIFEN field {@code E781} ({@code dNroMotor}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E781", xml = "dNroMotor")
  public @Nullable String engineNumber() {
    return engineNumber;
  }

  /**
   * Capacidad máxima de tracción.
   *
   * <p>SIFEN field {@code E782} ({@code dCapTracc}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E782", xml = "dCapTracc")
  public @Nullable BigDecimal maxTractionCapacity() {
    return maxTractionCapacity;
  }

  /**
   * Año de fabricación.
   *
   * <p>SIFEN field {@code E783} ({@code dAnoFab}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E783", xml = "dAnoFab")
  public @Nullable Integer manufactureYear() {
    return manufactureYear;
  }

  /**
   * Tipo de vehículo.
   *
   * <p>SIFEN field {@code E784} ({@code cTipVeh}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E784", xml = "cTipVeh")
  public @Nullable String vehicleType() {
    return vehicleType;
  }

  /**
   * Capacidad máxima de pasajeros.
   *
   * <p>SIFEN field {@code E785} ({@code dCapac}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E785", xml = "dCapac")
  public @Nullable Integer passengerCapacity() {
    return passengerCapacity;
  }

  /**
   * Cilindradas del motor.
   *
   * <p>SIFEN field {@code E786} ({@code dCilin}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E786", xml = "dCilin")
  public @Nullable String cylinders() {
    return cylinders;
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
    if (!(other instanceof NewVehicle)) {
      return false;
    }
    NewVehicle that = (NewVehicle) other;
    return Objects.equals(saleOperationTypeCode, that.saleOperationTypeCode)
        && Objects.equals(saleOperationTypeDescription, that.saleOperationTypeDescription)
        && Objects.equals(chassis, that.chassis)
        && Objects.equals(color, that.color)
        && Objects.equals(enginePower, that.enginePower)
        && Objects.equals(engineCapacity, that.engineCapacity)
        && Objects.equals(netWeight, that.netWeight)
        && Objects.equals(grossWeight, that.grossWeight)
        && Objects.equals(fuelTypeCode, that.fuelTypeCode)
        && Objects.equals(fuelTypeDescription, that.fuelTypeDescription)
        && Objects.equals(engineNumber, that.engineNumber)
        && Objects.equals(maxTractionCapacity, that.maxTractionCapacity)
        && Objects.equals(manufactureYear, that.manufactureYear)
        && Objects.equals(vehicleType, that.vehicleType)
        && Objects.equals(passengerCapacity, that.passengerCapacity)
        && Objects.equals(cylinders, that.cylinders);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        saleOperationTypeCode,
        saleOperationTypeDescription,
        chassis,
        color,
        enginePower,
        engineCapacity,
        netWeight,
        grossWeight,
        fuelTypeCode,
        fuelTypeDescription,
        engineNumber,
        maxTractionCapacity,
        manufactureYear,
        vehicleType,
        passengerCapacity,
        cylinders);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "NewVehicle";
  }

  /**
   * Builder for {@link NewVehicle}. Not thread-safe; {@link #build()} returns an immutable copy.
   */
  public static final class Builder {

    private @Nullable String saleOperationTypeCode;
    private @Nullable String saleOperationTypeDescription;
    private @Nullable String chassis;
    private @Nullable String color;
    private @Nullable Integer enginePower;
    private @Nullable Integer engineCapacity;
    private @Nullable BigDecimal netWeight;
    private @Nullable BigDecimal grossWeight;
    private @Nullable String fuelTypeCode;
    private @Nullable String fuelTypeDescription;
    private @Nullable String engineNumber;
    private @Nullable BigDecimal maxTractionCapacity;
    private @Nullable Integer manufactureYear;
    private @Nullable String vehicleType;
    private @Nullable Integer passengerCapacity;
    private @Nullable String cylinders;

    private Builder() {}

    private Builder(NewVehicle source) {
      this.saleOperationTypeCode = source.saleOperationTypeCode;
      this.saleOperationTypeDescription = source.saleOperationTypeDescription;
      this.chassis = source.chassis;
      this.color = source.color;
      this.enginePower = source.enginePower;
      this.engineCapacity = source.engineCapacity;
      this.netWeight = source.netWeight;
      this.grossWeight = source.grossWeight;
      this.fuelTypeCode = source.fuelTypeCode;
      this.fuelTypeDescription = source.fuelTypeDescription;
      this.engineNumber = source.engineNumber;
      this.maxTractionCapacity = source.maxTractionCapacity;
      this.manufactureYear = source.manufactureYear;
      this.vehicleType = source.vehicleType;
      this.passengerCapacity = source.passengerCapacity;
      this.cylinders = source.cylinders;
    }

    /**
     * Sets {@code saleOperationTypeCode} (E771, {@code iTipOpVN}).
     *
     * @param saleOperationTypeCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder saleOperationTypeCode(@Nullable String saleOperationTypeCode) {
      this.saleOperationTypeCode = saleOperationTypeCode;
      return this;
    }

    /**
     * Sets {@code saleOperationTypeCode} (E771, {@code iTipOpVN}) from {@link VehicleSaleType}.
     *
     * @param saleOperationType the catalog constant
     * @return this builder
     */
    public Builder saleOperationType(@Nullable VehicleSaleType saleOperationType) {
      this.saleOperationTypeCode = saleOperationType == null ? null : saleOperationType.code();
      return this;
    }

    /**
     * Sets {@code saleOperationTypeDescription} (E772, {@code dDesTipOpVN}).
     *
     * @param saleOperationTypeDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder saleOperationTypeDescription(@Nullable String saleOperationTypeDescription) {
      this.saleOperationTypeDescription = saleOperationTypeDescription;
      return this;
    }

    /**
     * Sets {@code chassis} (E773, {@code dChasis}).
     *
     * @param chassis the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder chassis(@Nullable String chassis) {
      this.chassis = chassis;
      return this;
    }

    /**
     * Sets {@code color} (E774, {@code dColor}).
     *
     * @param color the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder color(@Nullable String color) {
      this.color = color;
      return this;
    }

    /**
     * Sets {@code enginePower} (E775, {@code dPotencia}).
     *
     * @param enginePower the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder enginePower(@Nullable Integer enginePower) {
      this.enginePower = enginePower;
      return this;
    }

    /**
     * Sets {@code engineCapacity} (E776, {@code dCapMot}).
     *
     * @param engineCapacity the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder engineCapacity(@Nullable Integer engineCapacity) {
      this.engineCapacity = engineCapacity;
      return this;
    }

    /**
     * Sets {@code netWeight} (E777, {@code dPNet}).
     *
     * @param netWeight the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder netWeight(@Nullable BigDecimal netWeight) {
      this.netWeight = netWeight;
      return this;
    }

    /**
     * Sets {@code grossWeight} (E778, {@code dPBruto}).
     *
     * @param grossWeight the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder grossWeight(@Nullable BigDecimal grossWeight) {
      this.grossWeight = grossWeight;
      return this;
    }

    /**
     * Sets {@code fuelTypeCode} (E779, {@code iTipCom}).
     *
     * @param fuelTypeCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder fuelTypeCode(@Nullable String fuelTypeCode) {
      this.fuelTypeCode = fuelTypeCode;
      return this;
    }

    /**
     * Sets {@code fuelTypeCode} (E779, {@code iTipCom}) from {@link FuelType}.
     *
     * @param fuelType the catalog constant
     * @return this builder
     */
    public Builder fuelType(@Nullable FuelType fuelType) {
      this.fuelTypeCode = fuelType == null ? null : fuelType.code();
      return this;
    }

    /**
     * Sets {@code fuelTypeDescription} (E780, {@code dDesTipCom}).
     *
     * @param fuelTypeDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder fuelTypeDescription(@Nullable String fuelTypeDescription) {
      this.fuelTypeDescription = fuelTypeDescription;
      return this;
    }

    /**
     * Sets {@code engineNumber} (E781, {@code dNroMotor}).
     *
     * @param engineNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder engineNumber(@Nullable String engineNumber) {
      this.engineNumber = engineNumber;
      return this;
    }

    /**
     * Sets {@code maxTractionCapacity} (E782, {@code dCapTracc}).
     *
     * @param maxTractionCapacity the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder maxTractionCapacity(@Nullable BigDecimal maxTractionCapacity) {
      this.maxTractionCapacity = maxTractionCapacity;
      return this;
    }

    /**
     * Sets {@code manufactureYear} (E783, {@code dAnoFab}).
     *
     * @param manufactureYear the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder manufactureYear(@Nullable Integer manufactureYear) {
      this.manufactureYear = manufactureYear;
      return this;
    }

    /**
     * Sets {@code vehicleType} (E784, {@code cTipVeh}).
     *
     * @param vehicleType the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder vehicleType(@Nullable String vehicleType) {
      this.vehicleType = vehicleType;
      return this;
    }

    /**
     * Sets {@code passengerCapacity} (E785, {@code dCapac}).
     *
     * @param passengerCapacity the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder passengerCapacity(@Nullable Integer passengerCapacity) {
      this.passengerCapacity = passengerCapacity;
      return this;
    }

    /**
     * Sets {@code cylinders} (E786, {@code dCilin}).
     *
     * @param cylinders the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder cylinders(@Nullable String cylinders) {
      this.cylinders = cylinders;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     */
    public NewVehicle build() {
      return new NewVehicle(this);
    }
  }
}
