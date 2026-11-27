/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Campos que describen el transporte de mercaderías.
 *
 * <p>SIFEN group {@code E900} ({@code gTransp}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "E900", xml = "gTransp")
public final class Transport {

  private final @Nullable String typeCode;
  private final @Nullable String typeDescription;
  private final String modeCode;
  private final @Nullable String modeDescription;
  private final String freightPayerCode;
  private final @Nullable String negotiationConditionCode;
  private final @Nullable String manifestNumber;
  private final @Nullable String importDispatchNumber;
  private final @Nullable LocalDate estimatedStartDate;
  private final @Nullable LocalDate estimatedEndDate;
  private final @Nullable String destinationCountryCode;
  private final @Nullable String destinationCountryDescription;
  private final @Nullable TransportDeparture departureLocation;
  private final List<TransportDelivery> deliveryLocations;
  private final List<TransportVehicle> vehicles;
  private final @Nullable Carrier carrier;

  private Transport(Builder b) {
    this.typeCode = b.typeCode;
    this.typeDescription = b.typeDescription;
    this.modeCode = Checks.required(b.modeCode, "modeCode", "E903", "iModTrans");
    this.modeDescription = b.modeDescription;
    this.freightPayerCode =
        Checks.required(b.freightPayerCode, "freightPayerCode", "E905", "iRespFlete");
    this.negotiationConditionCode = b.negotiationConditionCode;
    this.manifestNumber = b.manifestNumber;
    this.importDispatchNumber = b.importDispatchNumber;
    this.estimatedStartDate = b.estimatedStartDate;
    this.estimatedEndDate = b.estimatedEndDate;
    this.destinationCountryCode = b.destinationCountryCode;
    this.destinationCountryDescription = b.destinationCountryDescription;
    this.departureLocation = b.departureLocation;
    this.deliveryLocations = List.copyOf(b.deliveryLocations);
    this.vehicles = List.copyOf(b.vehicles);
    this.carrier = b.carrier;
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
   * Tipo de transporte.
   *
   * <p>SIFEN field {@code E901} ({@code iTipTrans}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E901", xml = "iTipTrans")
  public @Nullable String typeCode() {
    return typeCode;
  }

  /**
   * Descripción del tipo de transporte.
   *
   * <p>SIFEN field {@code E902} ({@code dDesTipTrans}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E901}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E902", xml = "dDesTipTrans")
  public @Nullable String typeDescription() {
    return typeDescription;
  }

  /**
   * Modalidad del transporte.
   *
   * <p>SIFEN field {@code E903} ({@code iModTrans}).
   *
   * @return the value
   */
  @SifenField(id = "E903", xml = "iModTrans")
  public String modeCode() {
    return modeCode;
  }

  /**
   * Descripción de la modalidad del transporte.
   *
   * <p>SIFEN field {@code E904} ({@code dDesModTrans}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E903}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E904", xml = "dDesModTrans")
  public @Nullable String modeDescription() {
    return modeDescription;
  }

  /**
   * Responsable del costo del flete.
   *
   * <p>SIFEN field {@code E905} ({@code iRespFlete}).
   *
   * @return the value
   */
  @SifenField(id = "E905", xml = "iRespFlete")
  public String freightPayerCode() {
    return freightPayerCode;
  }

  /**
   * Condición de la negociación.
   *
   * <p>SIFEN field {@code E906} ({@code cCondNeg}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E906", xml = "cCondNeg")
  public @Nullable String negotiationConditionCode() {
    return negotiationConditionCode;
  }

  /**
   * Número de manifiesto o conocimiento de carga/ declaración de tránsito aduanero/ Carta de porte
   * internacional.
   *
   * <p>SIFEN field {@code E907} ({@code dNuManif}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E907", xml = "dNuManif")
  public @Nullable String manifestNumber() {
    return manifestNumber;
  }

  /**
   * Número de despacho de importación.
   *
   * <p>SIFEN field {@code E908} ({@code dNuDespImp}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E908", xml = "dNuDespImp")
  public @Nullable String importDispatchNumber() {
    return importDispatchNumber;
  }

  /**
   * Fecha estimada de inicio de traslado.
   *
   * <p>SIFEN field {@code E909} ({@code dIniTras}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E909", xml = "dIniTras")
  public @Nullable LocalDate estimatedStartDate() {
    return estimatedStartDate;
  }

  /**
   * Fecha estimada de fin de traslado.
   *
   * <p>SIFEN field {@code E910} ({@code dFinTras}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E910", xml = "dFinTras")
  public @Nullable LocalDate estimatedEndDate() {
    return estimatedEndDate;
  }

  /**
   * Código del país de destino.
   *
   * <p>SIFEN field {@code E911} ({@code cPaisDest}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E911", xml = "cPaisDest")
  public @Nullable String destinationCountryCode() {
    return destinationCountryCode;
  }

  /**
   * Descripción del país de destino.
   *
   * <p>SIFEN field {@code E912} ({@code dDesPaisDest}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E911}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E912", xml = "dDesPaisDest")
  public @Nullable String destinationCountryDescription() {
    return destinationCountryDescription;
  }

  /**
   * Campos que identifican el local de salida de las mercaderías.
   *
   * <p>SIFEN group {@code E920} ({@code gCamSal}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E920", xml = "gCamSal")
  public @Nullable TransportDeparture departureLocation() {
    return departureLocation;
  }

  /**
   * Campos que identifican el local de la entrega de las mercaderías.
   *
   * <p>SIFEN group {@code E940} ({@code gCamEnt}).
   *
   * @return an unmodifiable list, possibly empty
   */
  @SifenField(id = "E940", xml = "gCamEnt")
  public List<TransportDelivery> deliveryLocations() {
    return deliveryLocations;
  }

  /**
   * Campos que identifican al vehículo del traslado de mercaderías.
   *
   * <p>SIFEN group {@code E960} ({@code gVehTras}).
   *
   * @return an unmodifiable list, possibly empty
   */
  @SifenField(id = "E960", xml = "gVehTras")
  public List<TransportVehicle> vehicles() {
    return vehicles;
  }

  /**
   * Campos que identifican al transportista.
   *
   * <p>SIFEN group {@code E980} ({@code gCamTrans}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E980", xml = "gCamTrans")
  public @Nullable Carrier carrier() {
    return carrier;
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
    if (!(other instanceof Transport)) {
      return false;
    }
    Transport that = (Transport) other;
    return Objects.equals(typeCode, that.typeCode)
        && Objects.equals(typeDescription, that.typeDescription)
        && Objects.equals(modeCode, that.modeCode)
        && Objects.equals(modeDescription, that.modeDescription)
        && Objects.equals(freightPayerCode, that.freightPayerCode)
        && Objects.equals(negotiationConditionCode, that.negotiationConditionCode)
        && Objects.equals(manifestNumber, that.manifestNumber)
        && Objects.equals(importDispatchNumber, that.importDispatchNumber)
        && Objects.equals(estimatedStartDate, that.estimatedStartDate)
        && Objects.equals(estimatedEndDate, that.estimatedEndDate)
        && Objects.equals(destinationCountryCode, that.destinationCountryCode)
        && Objects.equals(destinationCountryDescription, that.destinationCountryDescription)
        && Objects.equals(departureLocation, that.departureLocation)
        && Objects.equals(deliveryLocations, that.deliveryLocations)
        && Objects.equals(vehicles, that.vehicles)
        && Objects.equals(carrier, that.carrier);
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
        modeCode,
        modeDescription,
        freightPayerCode,
        negotiationConditionCode,
        manifestNumber,
        importDispatchNumber,
        estimatedStartDate,
        estimatedEndDate,
        destinationCountryCode,
        destinationCountryDescription,
        departureLocation,
        deliveryLocations,
        vehicles,
        carrier);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "Transport[deliveryLocations="
        + deliveryLocations.size()
        + ", vehicles="
        + vehicles.size()
        + "]";
  }

  /** Builder for {@link Transport}. Not thread-safe; {@link #build()} returns an immutable copy. */
  public static final class Builder {

    private @Nullable String typeCode;
    private @Nullable String typeDescription;
    private @Nullable String modeCode;
    private @Nullable String modeDescription;
    private @Nullable String freightPayerCode;
    private @Nullable String negotiationConditionCode;
    private @Nullable String manifestNumber;
    private @Nullable String importDispatchNumber;
    private @Nullable LocalDate estimatedStartDate;
    private @Nullable LocalDate estimatedEndDate;
    private @Nullable String destinationCountryCode;
    private @Nullable String destinationCountryDescription;
    private @Nullable TransportDeparture departureLocation;
    private List<TransportDelivery> deliveryLocations = new ArrayList<>();
    private List<TransportVehicle> vehicles = new ArrayList<>();
    private @Nullable Carrier carrier;

    private Builder() {}

    private Builder(Transport source) {
      this.typeCode = source.typeCode;
      this.typeDescription = source.typeDescription;
      this.modeCode = source.modeCode;
      this.modeDescription = source.modeDescription;
      this.freightPayerCode = source.freightPayerCode;
      this.negotiationConditionCode = source.negotiationConditionCode;
      this.manifestNumber = source.manifestNumber;
      this.importDispatchNumber = source.importDispatchNumber;
      this.estimatedStartDate = source.estimatedStartDate;
      this.estimatedEndDate = source.estimatedEndDate;
      this.destinationCountryCode = source.destinationCountryCode;
      this.destinationCountryDescription = source.destinationCountryDescription;
      this.departureLocation = source.departureLocation;
      this.deliveryLocations = new ArrayList<>(source.deliveryLocations);
      this.vehicles = new ArrayList<>(source.vehicles);
      this.carrier = source.carrier;
    }

    /**
     * Sets {@code typeCode} (E901, {@code iTipTrans}).
     *
     * @param typeCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder typeCode(@Nullable String typeCode) {
      this.typeCode = typeCode;
      return this;
    }

    /**
     * Sets {@code typeDescription} (E902, {@code dDesTipTrans}).
     *
     * @param typeDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder typeDescription(@Nullable String typeDescription) {
      this.typeDescription = typeDescription;
      return this;
    }

    /**
     * Sets {@code modeCode} (E903, {@code iModTrans}).
     *
     * @param modeCode the value
     * @return this builder
     */
    public Builder modeCode(String modeCode) {
      this.modeCode = modeCode;
      return this;
    }

    /**
     * Sets {@code modeDescription} (E904, {@code dDesModTrans}).
     *
     * @param modeDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder modeDescription(@Nullable String modeDescription) {
      this.modeDescription = modeDescription;
      return this;
    }

    /**
     * Sets {@code freightPayerCode} (E905, {@code iRespFlete}).
     *
     * @param freightPayerCode the value
     * @return this builder
     */
    public Builder freightPayerCode(String freightPayerCode) {
      this.freightPayerCode = freightPayerCode;
      return this;
    }

    /**
     * Sets {@code negotiationConditionCode} (E906, {@code cCondNeg}).
     *
     * @param negotiationConditionCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder negotiationConditionCode(@Nullable String negotiationConditionCode) {
      this.negotiationConditionCode = negotiationConditionCode;
      return this;
    }

    /**
     * Sets {@code manifestNumber} (E907, {@code dNuManif}).
     *
     * @param manifestNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder manifestNumber(@Nullable String manifestNumber) {
      this.manifestNumber = manifestNumber;
      return this;
    }

    /**
     * Sets {@code importDispatchNumber} (E908, {@code dNuDespImp}).
     *
     * @param importDispatchNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder importDispatchNumber(@Nullable String importDispatchNumber) {
      this.importDispatchNumber = importDispatchNumber;
      return this;
    }

    /**
     * Sets {@code estimatedStartDate} (E909, {@code dIniTras}).
     *
     * @param estimatedStartDate the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder estimatedStartDate(@Nullable LocalDate estimatedStartDate) {
      this.estimatedStartDate = estimatedStartDate;
      return this;
    }

    /**
     * Sets {@code estimatedEndDate} (E910, {@code dFinTras}).
     *
     * @param estimatedEndDate the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder estimatedEndDate(@Nullable LocalDate estimatedEndDate) {
      this.estimatedEndDate = estimatedEndDate;
      return this;
    }

    /**
     * Sets {@code destinationCountryCode} (E911, {@code cPaisDest}).
     *
     * @param destinationCountryCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder destinationCountryCode(@Nullable String destinationCountryCode) {
      this.destinationCountryCode = destinationCountryCode;
      return this;
    }

    /**
     * Sets {@code destinationCountryDescription} (E912, {@code dDesPaisDest}).
     *
     * @param destinationCountryDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder destinationCountryDescription(@Nullable String destinationCountryDescription) {
      this.destinationCountryDescription = destinationCountryDescription;
      return this;
    }

    /**
     * Sets {@code departureLocation} (E920, {@code gCamSal}).
     *
     * @param departureLocation the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder departureLocation(@Nullable TransportDeparture departureLocation) {
      this.departureLocation = departureLocation;
      return this;
    }

    /**
     * Replaces the list of {@code deliveryLocations} (E940, {@code gCamEnt}).
     *
     * @param deliveryLocations the elements, in document order
     * @return this builder
     */
    public Builder deliveryLocations(List<TransportDelivery> deliveryLocations) {
      this.deliveryLocations = new ArrayList<>(deliveryLocations);
      return this;
    }

    /**
     * Appends one element to {@code deliveryLocations} (E940, {@code gCamEnt}).
     *
     * @param deliveryLocation the element
     * @return this builder
     */
    public Builder addDeliveryLocation(TransportDelivery deliveryLocation) {
      this.deliveryLocations.add(Objects.requireNonNull(deliveryLocation, "deliveryLocation"));
      return this;
    }

    /**
     * Replaces the list of {@code vehicles} (E960, {@code gVehTras}).
     *
     * @param vehicles the elements, in document order
     * @return this builder
     */
    public Builder vehicles(List<TransportVehicle> vehicles) {
      this.vehicles = new ArrayList<>(vehicles);
      return this;
    }

    /**
     * Appends one element to {@code vehicles} (E960, {@code gVehTras}).
     *
     * @param vehicle the element
     * @return this builder
     */
    public Builder addVehicle(TransportVehicle vehicle) {
      this.vehicles.add(Objects.requireNonNull(vehicle, "vehicle"));
      return this;
    }

    /**
     * Sets {@code carrier} (E980, {@code gCamTrans}).
     *
     * @param carrier the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder carrier(@Nullable Carrier carrier) {
      this.carrier = carrier;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public Transport build() {
      return new Transport(this);
    }
  }
}
