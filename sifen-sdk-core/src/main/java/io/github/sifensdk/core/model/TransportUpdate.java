/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Raíz Gestión de Eventos por actualización de datos del transporte.
 *
 * <p>Detail of the event of actualización de datos del transporte: SIFEN group {@code GET001}
 * ({@code rGeVeTr}). Immutable and thread-safe; create it with {@link #builder()}. The builder only
 * checks what Java needs (the elements the XSD always requires); the SIFEN rules are verified by
 * the validation step.
 */
@SifenField(id = "GET001", xml = "rGeVeTr")
public final class TransportUpdate implements EventDetail {

  private final String cdc;
  private final String reasonCode;
  private final @Nullable String deliveryDepartmentCode;
  private final @Nullable String deliveryDepartmentDescription;
  private final @Nullable String deliveryDistrictCode;
  private final @Nullable String deliveryDistrictDescription;
  private final @Nullable String deliveryCityCode;
  private final @Nullable String deliveryCityDescription;
  private final @Nullable String deliveryAddress;
  private final @Nullable String deliveryHouseNumber;
  private final @Nullable String deliveryAddressComplement;
  private final @Nullable String driverName;
  private final @Nullable String driverIdDocumentNumber;
  private final @Nullable String carrierNatureCode;
  private final @Nullable String carrierRuc;
  private final @Nullable String carrierCheckDigit;
  private final @Nullable String carrierName;
  private final @Nullable String carrierIdDocumentTypeCode;
  private final @Nullable String carrierIdDocumentTypeDescription;
  private final @Nullable String carrierIdDocumentNumber;
  private final @Nullable String transportTypeCode;
  private final @Nullable String transportTypeDescription;
  private final @Nullable String transportModeCode;
  private final @Nullable String transportModeDescription;
  private final @Nullable String vehicleType;
  private final @Nullable String vehicleBrand;
  private final @Nullable String vehicleIdentificationTypeCode;
  private final @Nullable String vehicleIdentificationNumber;
  private final @Nullable String vehicleRegistrationNumber;

  private TransportUpdate(Builder b) {
    this.cdc = Checks.required(b.cdc, "cdc", "GET002", "Id");
    this.reasonCode = Checks.required(b.reasonCode, "reasonCode", "GET003", "dMotEv");
    this.deliveryDepartmentCode = b.deliveryDepartmentCode;
    this.deliveryDepartmentDescription = b.deliveryDepartmentDescription;
    this.deliveryDistrictCode = b.deliveryDistrictCode;
    this.deliveryDistrictDescription = b.deliveryDistrictDescription;
    this.deliveryCityCode = b.deliveryCityCode;
    this.deliveryCityDescription = b.deliveryCityDescription;
    this.deliveryAddress = b.deliveryAddress;
    this.deliveryHouseNumber = b.deliveryHouseNumber;
    this.deliveryAddressComplement = b.deliveryAddressComplement;
    this.driverName = b.driverName;
    this.driverIdDocumentNumber = b.driverIdDocumentNumber;
    this.carrierNatureCode = b.carrierNatureCode;
    this.carrierRuc = b.carrierRuc;
    this.carrierCheckDigit = b.carrierCheckDigit;
    this.carrierName = b.carrierName;
    this.carrierIdDocumentTypeCode = b.carrierIdDocumentTypeCode;
    this.carrierIdDocumentTypeDescription = b.carrierIdDocumentTypeDescription;
    this.carrierIdDocumentNumber = b.carrierIdDocumentNumber;
    this.transportTypeCode = b.transportTypeCode;
    this.transportTypeDescription = b.transportTypeDescription;
    this.transportModeCode = b.transportModeCode;
    this.transportModeDescription = b.transportModeDescription;
    this.vehicleType = b.vehicleType;
    this.vehicleBrand = b.vehicleBrand;
    this.vehicleIdentificationTypeCode = b.vehicleIdentificationTypeCode;
    this.vehicleIdentificationNumber = b.vehicleIdentificationNumber;
    this.vehicleRegistrationNumber = b.vehicleRegistrationNumber;
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
   * CDC del DTE.
   *
   * <p>SIFEN field {@code GET002} ({@code Id}).
   *
   * @return the value
   */
  @SifenField(id = "GET002", xml = "Id")
  public String cdc() {
    return cdc;
  }

  /**
   * Motivo del evento.
   *
   * <p>SIFEN field {@code GET003} ({@code dMotEv}).
   *
   * @return the value
   */
  @SifenField(id = "GET003", xml = "dMotEv")
  public String reasonCode() {
    return reasonCode;
  }

  /**
   * Código del departamento del local de la entrega.
   *
   * <p>SIFEN field {@code GET004} ({@code cDepEnt}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GET004", xml = "cDepEnt")
  public @Nullable String deliveryDepartmentCode() {
    return deliveryDepartmentCode;
  }

  /**
   * Descripción del departamento del local de la entrega.
   *
   * <p>SIFEN field {@code GET005} ({@code dDesDepEnt}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code GET004}. Set it only
   * when that code admits a free description or the bundled catalog does not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GET005", xml = "dDesDepEnt")
  public @Nullable String deliveryDepartmentDescription() {
    return deliveryDepartmentDescription;
  }

  /**
   * Código del distrito del local de la entrega.
   *
   * <p>SIFEN field {@code GET006} ({@code cDisEnt}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GET006", xml = "cDisEnt")
  public @Nullable String deliveryDistrictCode() {
    return deliveryDistrictCode;
  }

  /**
   * Descripción de distrito del local de la entrega.
   *
   * <p>SIFEN field {@code GET007} ({@code dDesDisEnt}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code GET006}. Set it only
   * when that code admits a free description or the bundled catalog does not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GET007", xml = "dDesDisEnt")
  public @Nullable String deliveryDistrictDescription() {
    return deliveryDistrictDescription;
  }

  /**
   * Código de la ciudad del local de la entrega.
   *
   * <p>SIFEN field {@code GET008} ({@code cCiuEnt}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GET008", xml = "cCiuEnt")
  public @Nullable String deliveryCityCode() {
    return deliveryCityCode;
  }

  /**
   * Descripción de ciudad del local de la entrega.
   *
   * <p>SIFEN field {@code GET009} ({@code dDesCiuEnt}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code GET008}. Set it only
   * when that code admits a free description or the bundled catalog does not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GET009", xml = "dDesCiuEnt")
  public @Nullable String deliveryCityDescription() {
    return deliveryCityDescription;
  }

  /**
   * Dirección del local de la entrega.
   *
   * <p>SIFEN field {@code GET010} ({@code dDirEnt}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GET010", xml = "dDirEnt")
  public @Nullable String deliveryAddress() {
    return deliveryAddress;
  }

  /**
   * Número de casa del local de la entrega.
   *
   * <p>SIFEN field {@code GET011} ({@code dNumCas}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GET011", xml = "dNumCas")
  public @Nullable String deliveryHouseNumber() {
    return deliveryHouseNumber;
  }

  /**
   * Complemento de dirección del local de la entrega.
   *
   * <p>SIFEN field {@code GET012} ({@code dCompDir1}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GET012", xml = "dCompDir1")
  public @Nullable String deliveryAddressComplement() {
    return deliveryAddressComplement;
  }

  /**
   * Nombre y apellido del chofer.
   *
   * <p>SIFEN field {@code GET013} ({@code dNomChof}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GET013", xml = "dNomChof")
  public @Nullable String driverName() {
    return driverName;
  }

  /**
   * Número de documento de identidad del chofer.
   *
   * <p>SIFEN field {@code GET014} ({@code dNumIDChof}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GET014", xml = "dNumIDChof")
  public @Nullable String driverIdDocumentNumber() {
    return driverIdDocumentNumber;
  }

  /**
   * Naturaleza del transportista.
   *
   * <p>SIFEN field {@code GET015} ({@code iNatTrans}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GET015", xml = "iNatTrans")
  public @Nullable String carrierNatureCode() {
    return carrierNatureCode;
  }

  /**
   * RUC del transportista.
   *
   * <p>SIFEN field {@code GET016} ({@code dRucTrans}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GET016", xml = "dRucTrans")
  public @Nullable String carrierRuc() {
    return carrierRuc;
  }

  /**
   * Dígito verificador del RUC del transportista.
   *
   * <p>SIFEN field {@code GET017} ({@code dDVTrans}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GET017", xml = "dDVTrans")
  public @Nullable String carrierCheckDigit() {
    return carrierCheckDigit;
  }

  /**
   * Nombre o razón social del transportista.
   *
   * <p>SIFEN field {@code GET018} ({@code dNomTrans}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GET018", xml = "dNomTrans")
  public @Nullable String carrierName() {
    return carrierName;
  }

  /**
   * Tipo de documento de identidad del transportista.
   *
   * <p>SIFEN field {@code GET019} ({@code iTipIDTrans}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GET019", xml = "iTipIDTrans")
  public @Nullable String carrierIdDocumentTypeCode() {
    return carrierIdDocumentTypeCode;
  }

  /**
   * Descripción del tipo de documento de identidad del transportista.
   *
   * <p>SIFEN field {@code GET020} ({@code dDTipIDTrans}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code GET019}. Set it only
   * when that code admits a free description or the bundled catalog does not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GET020", xml = "dDTipIDTrans")
  public @Nullable String carrierIdDocumentTypeDescription() {
    return carrierIdDocumentTypeDescription;
  }

  /**
   * Número de documento de identidad del transportista.
   *
   * <p>SIFEN field {@code GET021} ({@code dNumIDTrans}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GET021", xml = "dNumIDTrans")
  public @Nullable String carrierIdDocumentNumber() {
    return carrierIdDocumentNumber;
  }

  /**
   * Tipo de transporte.
   *
   * <p>SIFEN field {@code GET022} ({@code iTipTrans}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GET022", xml = "iTipTrans")
  public @Nullable String transportTypeCode() {
    return transportTypeCode;
  }

  /**
   * Descripción del tipo de transporte.
   *
   * <p>SIFEN field {@code GET023} ({@code dDesTipTrans}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code GET022}. Set it only
   * when that code admits a free description or the bundled catalog does not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GET023", xml = "dDesTipTrans")
  public @Nullable String transportTypeDescription() {
    return transportTypeDescription;
  }

  /**
   * Modalidad del transporte.
   *
   * <p>SIFEN field {@code GET024} ({@code iModTrans}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GET024", xml = "iModTrans")
  public @Nullable String transportModeCode() {
    return transportModeCode;
  }

  /**
   * Descripción de la modalidad del transporte.
   *
   * <p>SIFEN field {@code GET025} ({@code dDesModTrans}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code GET024}. Set it only
   * when that code admits a free description or the bundled catalog does not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GET025", xml = "dDesModTrans")
  public @Nullable String transportModeDescription() {
    return transportModeDescription;
  }

  /**
   * Tipo de vehículo.
   *
   * <p>SIFEN field {@code GET026} ({@code dTiVehTras}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GET026", xml = "dTiVehTras")
  public @Nullable String vehicleType() {
    return vehicleType;
  }

  /**
   * Marca del vehículo.
   *
   * <p>SIFEN field {@code GET027} ({@code dMarVeh}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GET027", xml = "dMarVeh")
  public @Nullable String vehicleBrand() {
    return vehicleBrand;
  }

  /**
   * Tipo de identificación del vehículo.
   *
   * <p>SIFEN field {@code GET028} ({@code dTipIdenVeh}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GET028", xml = "dTipIdenVeh")
  public @Nullable String vehicleIdentificationTypeCode() {
    return vehicleIdentificationTypeCode;
  }

  /**
   * Número de identificación del vehículo.
   *
   * <p>SIFEN field {@code GET029} ({@code dNroIDVeh}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GET029", xml = "dNroIDVeh")
  public @Nullable String vehicleIdentificationNumber() {
    return vehicleIdentificationNumber;
  }

  /**
   * Número de matrícula del vehículo.
   *
   * <p>SIFEN field {@code GET030} ({@code dNroMatVeh}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GET030", xml = "dNroMatVeh")
  public @Nullable String vehicleRegistrationNumber() {
    return vehicleRegistrationNumber;
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
    if (!(other instanceof TransportUpdate)) {
      return false;
    }
    TransportUpdate that = (TransportUpdate) other;
    return Objects.equals(cdc, that.cdc)
        && Objects.equals(reasonCode, that.reasonCode)
        && Objects.equals(deliveryDepartmentCode, that.deliveryDepartmentCode)
        && Objects.equals(deliveryDepartmentDescription, that.deliveryDepartmentDescription)
        && Objects.equals(deliveryDistrictCode, that.deliveryDistrictCode)
        && Objects.equals(deliveryDistrictDescription, that.deliveryDistrictDescription)
        && Objects.equals(deliveryCityCode, that.deliveryCityCode)
        && Objects.equals(deliveryCityDescription, that.deliveryCityDescription)
        && Objects.equals(deliveryAddress, that.deliveryAddress)
        && Objects.equals(deliveryHouseNumber, that.deliveryHouseNumber)
        && Objects.equals(deliveryAddressComplement, that.deliveryAddressComplement)
        && Objects.equals(driverName, that.driverName)
        && Objects.equals(driverIdDocumentNumber, that.driverIdDocumentNumber)
        && Objects.equals(carrierNatureCode, that.carrierNatureCode)
        && Objects.equals(carrierRuc, that.carrierRuc)
        && Objects.equals(carrierCheckDigit, that.carrierCheckDigit)
        && Objects.equals(carrierName, that.carrierName)
        && Objects.equals(carrierIdDocumentTypeCode, that.carrierIdDocumentTypeCode)
        && Objects.equals(carrierIdDocumentTypeDescription, that.carrierIdDocumentTypeDescription)
        && Objects.equals(carrierIdDocumentNumber, that.carrierIdDocumentNumber)
        && Objects.equals(transportTypeCode, that.transportTypeCode)
        && Objects.equals(transportTypeDescription, that.transportTypeDescription)
        && Objects.equals(transportModeCode, that.transportModeCode)
        && Objects.equals(transportModeDescription, that.transportModeDescription)
        && Objects.equals(vehicleType, that.vehicleType)
        && Objects.equals(vehicleBrand, that.vehicleBrand)
        && Objects.equals(vehicleIdentificationTypeCode, that.vehicleIdentificationTypeCode)
        && Objects.equals(vehicleIdentificationNumber, that.vehicleIdentificationNumber)
        && Objects.equals(vehicleRegistrationNumber, that.vehicleRegistrationNumber);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        cdc,
        reasonCode,
        deliveryDepartmentCode,
        deliveryDepartmentDescription,
        deliveryDistrictCode,
        deliveryDistrictDescription,
        deliveryCityCode,
        deliveryCityDescription,
        deliveryAddress,
        deliveryHouseNumber,
        deliveryAddressComplement,
        driverName,
        driverIdDocumentNumber,
        carrierNatureCode,
        carrierRuc,
        carrierCheckDigit,
        carrierName,
        carrierIdDocumentTypeCode,
        carrierIdDocumentTypeDescription,
        carrierIdDocumentNumber,
        transportTypeCode,
        transportTypeDescription,
        transportModeCode,
        transportModeDescription,
        vehicleType,
        vehicleBrand,
        vehicleIdentificationTypeCode,
        vehicleIdentificationNumber,
        vehicleRegistrationNumber);
  }

  /**
   * The type name. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "TransportUpdate";
  }

  /**
   * Builder for {@link TransportUpdate}. Not thread-safe; {@link #build()} returns an immutable
   * copy.
   */
  public static final class Builder {

    private @Nullable String cdc;
    private @Nullable String reasonCode;
    private @Nullable String deliveryDepartmentCode;
    private @Nullable String deliveryDepartmentDescription;
    private @Nullable String deliveryDistrictCode;
    private @Nullable String deliveryDistrictDescription;
    private @Nullable String deliveryCityCode;
    private @Nullable String deliveryCityDescription;
    private @Nullable String deliveryAddress;
    private @Nullable String deliveryHouseNumber;
    private @Nullable String deliveryAddressComplement;
    private @Nullable String driverName;
    private @Nullable String driverIdDocumentNumber;
    private @Nullable String carrierNatureCode;
    private @Nullable String carrierRuc;
    private @Nullable String carrierCheckDigit;
    private @Nullable String carrierName;
    private @Nullable String carrierIdDocumentTypeCode;
    private @Nullable String carrierIdDocumentTypeDescription;
    private @Nullable String carrierIdDocumentNumber;
    private @Nullable String transportTypeCode;
    private @Nullable String transportTypeDescription;
    private @Nullable String transportModeCode;
    private @Nullable String transportModeDescription;
    private @Nullable String vehicleType;
    private @Nullable String vehicleBrand;
    private @Nullable String vehicleIdentificationTypeCode;
    private @Nullable String vehicleIdentificationNumber;
    private @Nullable String vehicleRegistrationNumber;

    private Builder() {}

    private Builder(TransportUpdate source) {
      this.cdc = source.cdc;
      this.reasonCode = source.reasonCode;
      this.deliveryDepartmentCode = source.deliveryDepartmentCode;
      this.deliveryDepartmentDescription = source.deliveryDepartmentDescription;
      this.deliveryDistrictCode = source.deliveryDistrictCode;
      this.deliveryDistrictDescription = source.deliveryDistrictDescription;
      this.deliveryCityCode = source.deliveryCityCode;
      this.deliveryCityDescription = source.deliveryCityDescription;
      this.deliveryAddress = source.deliveryAddress;
      this.deliveryHouseNumber = source.deliveryHouseNumber;
      this.deliveryAddressComplement = source.deliveryAddressComplement;
      this.driverName = source.driverName;
      this.driverIdDocumentNumber = source.driverIdDocumentNumber;
      this.carrierNatureCode = source.carrierNatureCode;
      this.carrierRuc = source.carrierRuc;
      this.carrierCheckDigit = source.carrierCheckDigit;
      this.carrierName = source.carrierName;
      this.carrierIdDocumentTypeCode = source.carrierIdDocumentTypeCode;
      this.carrierIdDocumentTypeDescription = source.carrierIdDocumentTypeDescription;
      this.carrierIdDocumentNumber = source.carrierIdDocumentNumber;
      this.transportTypeCode = source.transportTypeCode;
      this.transportTypeDescription = source.transportTypeDescription;
      this.transportModeCode = source.transportModeCode;
      this.transportModeDescription = source.transportModeDescription;
      this.vehicleType = source.vehicleType;
      this.vehicleBrand = source.vehicleBrand;
      this.vehicleIdentificationTypeCode = source.vehicleIdentificationTypeCode;
      this.vehicleIdentificationNumber = source.vehicleIdentificationNumber;
      this.vehicleRegistrationNumber = source.vehicleRegistrationNumber;
    }

    /**
     * Sets {@code cdc} (GET002, {@code Id}).
     *
     * @param cdc the value
     * @return this builder
     */
    public Builder cdc(String cdc) {
      this.cdc = cdc;
      return this;
    }

    /**
     * Sets {@code reasonCode} (GET003, {@code dMotEv}).
     *
     * @param reasonCode the value
     * @return this builder
     */
    public Builder reasonCode(String reasonCode) {
      this.reasonCode = reasonCode;
      return this;
    }

    /**
     * Sets {@code deliveryDepartmentCode} (GET004, {@code cDepEnt}).
     *
     * @param deliveryDepartmentCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder deliveryDepartmentCode(@Nullable String deliveryDepartmentCode) {
      this.deliveryDepartmentCode = deliveryDepartmentCode;
      return this;
    }

    /**
     * Sets {@code deliveryDepartmentDescription} (GET005, {@code dDesDepEnt}).
     *
     * @param deliveryDepartmentDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder deliveryDepartmentDescription(@Nullable String deliveryDepartmentDescription) {
      this.deliveryDepartmentDescription = deliveryDepartmentDescription;
      return this;
    }

    /**
     * Sets {@code deliveryDistrictCode} (GET006, {@code cDisEnt}).
     *
     * @param deliveryDistrictCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder deliveryDistrictCode(@Nullable String deliveryDistrictCode) {
      this.deliveryDistrictCode = deliveryDistrictCode;
      return this;
    }

    /**
     * Sets {@code deliveryDistrictDescription} (GET007, {@code dDesDisEnt}).
     *
     * @param deliveryDistrictDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder deliveryDistrictDescription(@Nullable String deliveryDistrictDescription) {
      this.deliveryDistrictDescription = deliveryDistrictDescription;
      return this;
    }

    /**
     * Sets {@code deliveryCityCode} (GET008, {@code cCiuEnt}).
     *
     * @param deliveryCityCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder deliveryCityCode(@Nullable String deliveryCityCode) {
      this.deliveryCityCode = deliveryCityCode;
      return this;
    }

    /**
     * Sets {@code deliveryCityDescription} (GET009, {@code dDesCiuEnt}).
     *
     * @param deliveryCityDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder deliveryCityDescription(@Nullable String deliveryCityDescription) {
      this.deliveryCityDescription = deliveryCityDescription;
      return this;
    }

    /**
     * Sets {@code deliveryAddress} (GET010, {@code dDirEnt}).
     *
     * @param deliveryAddress the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder deliveryAddress(@Nullable String deliveryAddress) {
      this.deliveryAddress = deliveryAddress;
      return this;
    }

    /**
     * Sets {@code deliveryHouseNumber} (GET011, {@code dNumCas}).
     *
     * @param deliveryHouseNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder deliveryHouseNumber(@Nullable String deliveryHouseNumber) {
      this.deliveryHouseNumber = deliveryHouseNumber;
      return this;
    }

    /**
     * Sets {@code deliveryAddressComplement} (GET012, {@code dCompDir1}).
     *
     * @param deliveryAddressComplement the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder deliveryAddressComplement(@Nullable String deliveryAddressComplement) {
      this.deliveryAddressComplement = deliveryAddressComplement;
      return this;
    }

    /**
     * Sets {@code driverName} (GET013, {@code dNomChof}).
     *
     * @param driverName the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder driverName(@Nullable String driverName) {
      this.driverName = driverName;
      return this;
    }

    /**
     * Sets {@code driverIdDocumentNumber} (GET014, {@code dNumIDChof}).
     *
     * @param driverIdDocumentNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder driverIdDocumentNumber(@Nullable String driverIdDocumentNumber) {
      this.driverIdDocumentNumber = driverIdDocumentNumber;
      return this;
    }

    /**
     * Sets {@code carrierNatureCode} (GET015, {@code iNatTrans}).
     *
     * @param carrierNatureCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder carrierNatureCode(@Nullable String carrierNatureCode) {
      this.carrierNatureCode = carrierNatureCode;
      return this;
    }

    /**
     * Sets {@code carrierRuc} (GET016, {@code dRucTrans}).
     *
     * @param carrierRuc the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder carrierRuc(@Nullable String carrierRuc) {
      this.carrierRuc = carrierRuc;
      return this;
    }

    /**
     * Sets {@code carrierCheckDigit} (GET017, {@code dDVTrans}).
     *
     * @param carrierCheckDigit the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder carrierCheckDigit(@Nullable String carrierCheckDigit) {
      this.carrierCheckDigit = carrierCheckDigit;
      return this;
    }

    /**
     * Sets {@code carrierName} (GET018, {@code dNomTrans}).
     *
     * @param carrierName the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder carrierName(@Nullable String carrierName) {
      this.carrierName = carrierName;
      return this;
    }

    /**
     * Sets {@code carrierIdDocumentTypeCode} (GET019, {@code iTipIDTrans}).
     *
     * @param carrierIdDocumentTypeCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder carrierIdDocumentTypeCode(@Nullable String carrierIdDocumentTypeCode) {
      this.carrierIdDocumentTypeCode = carrierIdDocumentTypeCode;
      return this;
    }

    /**
     * Sets {@code carrierIdDocumentTypeDescription} (GET020, {@code dDTipIDTrans}).
     *
     * @param carrierIdDocumentTypeDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder carrierIdDocumentTypeDescription(
        @Nullable String carrierIdDocumentTypeDescription) {
      this.carrierIdDocumentTypeDescription = carrierIdDocumentTypeDescription;
      return this;
    }

    /**
     * Sets {@code carrierIdDocumentNumber} (GET021, {@code dNumIDTrans}).
     *
     * @param carrierIdDocumentNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder carrierIdDocumentNumber(@Nullable String carrierIdDocumentNumber) {
      this.carrierIdDocumentNumber = carrierIdDocumentNumber;
      return this;
    }

    /**
     * Sets {@code transportTypeCode} (GET022, {@code iTipTrans}).
     *
     * @param transportTypeCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder transportTypeCode(@Nullable String transportTypeCode) {
      this.transportTypeCode = transportTypeCode;
      return this;
    }

    /**
     * Sets {@code transportTypeDescription} (GET023, {@code dDesTipTrans}).
     *
     * @param transportTypeDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder transportTypeDescription(@Nullable String transportTypeDescription) {
      this.transportTypeDescription = transportTypeDescription;
      return this;
    }

    /**
     * Sets {@code transportModeCode} (GET024, {@code iModTrans}).
     *
     * @param transportModeCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder transportModeCode(@Nullable String transportModeCode) {
      this.transportModeCode = transportModeCode;
      return this;
    }

    /**
     * Sets {@code transportModeDescription} (GET025, {@code dDesModTrans}).
     *
     * @param transportModeDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder transportModeDescription(@Nullable String transportModeDescription) {
      this.transportModeDescription = transportModeDescription;
      return this;
    }

    /**
     * Sets {@code vehicleType} (GET026, {@code dTiVehTras}).
     *
     * @param vehicleType the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder vehicleType(@Nullable String vehicleType) {
      this.vehicleType = vehicleType;
      return this;
    }

    /**
     * Sets {@code vehicleBrand} (GET027, {@code dMarVeh}).
     *
     * @param vehicleBrand the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder vehicleBrand(@Nullable String vehicleBrand) {
      this.vehicleBrand = vehicleBrand;
      return this;
    }

    /**
     * Sets {@code vehicleIdentificationTypeCode} (GET028, {@code dTipIdenVeh}).
     *
     * @param vehicleIdentificationTypeCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder vehicleIdentificationTypeCode(@Nullable String vehicleIdentificationTypeCode) {
      this.vehicleIdentificationTypeCode = vehicleIdentificationTypeCode;
      return this;
    }

    /**
     * Sets {@code vehicleIdentificationNumber} (GET029, {@code dNroIDVeh}).
     *
     * @param vehicleIdentificationNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder vehicleIdentificationNumber(@Nullable String vehicleIdentificationNumber) {
      this.vehicleIdentificationNumber = vehicleIdentificationNumber;
      return this;
    }

    /**
     * Sets {@code vehicleRegistrationNumber} (GET030, {@code dNroMatVeh}).
     *
     * @param vehicleRegistrationNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder vehicleRegistrationNumber(@Nullable String vehicleRegistrationNumber) {
      this.vehicleRegistrationNumber = vehicleRegistrationNumber;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public TransportUpdate build() {
      return new TransportUpdate(this);
    }
  }
}
