/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import io.github.sifensdk.core.catalog.Department;
import io.github.sifensdk.core.catalog.NominationIdDocumentType;
import io.github.sifensdk.core.catalog.OperationType;
import io.github.sifensdk.core.catalog.ReceiverNature;
import io.github.sifensdk.core.catalog.TaxpayerType;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Raíz Gestión de Eventos Nominación.
 *
 * <p>Detail of the event of nominación del receptor de un DE innominado: SIFEN group {@code
 * GENFE001} ({@code rGEveNom}). Immutable and thread-safe; create it with {@link #builder()}. The
 * builder only checks what Java needs (the elements the XSD always requires); the SIFEN rules are
 * verified by the validation step.
 */
@SifenField(id = "GENFE001", xml = "rGEveNom")
public final class Nomination implements EventDetail {

  private final String cdc;
  private final String reason;
  private final String receiverNatureCode;
  private final String operationTypeCode;
  private final String receiverCountryCode;
  private final String receiverCountryDescription;
  private final @Nullable String receiverTaxpayerTypeCode;
  private final @Nullable String receiverRuc;
  private final @Nullable String receiverCheckDigit;
  private final @Nullable String receiverIdDocumentTypeCode;
  private final @Nullable String receiverIdDocumentTypeDescription;
  private final @Nullable String receiverIdDocumentNumber;
  private final String receiverName;
  private final @Nullable String receiverTradeName;
  private final @Nullable String receiverAddress;
  private final @Nullable String receiverHouseNumber;
  private final @Nullable String receiverDepartmentCode;
  private final @Nullable String receiverDepartmentDescription;
  private final @Nullable String receiverDistrictCode;
  private final @Nullable String receiverDistrictDescription;
  private final @Nullable String receiverCityCode;
  private final @Nullable String receiverCityDescription;
  private final @Nullable String receiverPhone;
  private final @Nullable String receiverMobilePhone;
  private final @Nullable String receiverEmail;
  private final @Nullable String customerCode;

  private Nomination(Builder b) {
    this.cdc = Checks.required(b.cdc, "cdc", "GENFE002", "Id");
    this.reason = Checks.required(b.reason, "reason", "GENFE003", "mOtEve");
    this.receiverNatureCode =
        Checks.required(b.receiverNatureCode, "receiverNatureCode", "GENFE004", "iNatRec");
    this.operationTypeCode =
        Checks.required(b.operationTypeCode, "operationTypeCode", "GENFE027", "iTiOpe");
    this.receiverCountryCode =
        Checks.required(b.receiverCountryCode, "receiverCountryCode", "GENFE005", "cPaisRec");
    this.receiverCountryDescription = b.receiverCountryDescription;
    this.receiverTaxpayerTypeCode = b.receiverTaxpayerTypeCode;
    this.receiverRuc = b.receiverRuc;
    this.receiverCheckDigit = b.receiverCheckDigit;
    this.receiverIdDocumentTypeCode = b.receiverIdDocumentTypeCode;
    this.receiverIdDocumentTypeDescription = b.receiverIdDocumentTypeDescription;
    this.receiverIdDocumentNumber = b.receiverIdDocumentNumber;
    this.receiverName = Checks.required(b.receiverName, "receiverName", "GENFE013", "dNomRec");
    this.receiverTradeName = b.receiverTradeName;
    this.receiverAddress = b.receiverAddress;
    this.receiverHouseNumber = b.receiverHouseNumber;
    this.receiverDepartmentCode = b.receiverDepartmentCode;
    this.receiverDepartmentDescription = b.receiverDepartmentDescription;
    this.receiverDistrictCode = b.receiverDistrictCode;
    this.receiverDistrictDescription = b.receiverDistrictDescription;
    this.receiverCityCode = b.receiverCityCode;
    this.receiverCityDescription = b.receiverCityDescription;
    this.receiverPhone = b.receiverPhone;
    this.receiverMobilePhone = b.receiverMobilePhone;
    this.receiverEmail = b.receiverEmail;
    this.customerCode = b.customerCode;
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
   * Identificador del DTE.
   *
   * <p>SIFEN field {@code GENFE002} ({@code Id}).
   *
   * @return the value
   */
  @SifenField(id = "GENFE002", xml = "Id")
  public String cdc() {
    return cdc;
  }

  /**
   * Motivo del Evento.
   *
   * <p>SIFEN field {@code GENFE003} ({@code mOtEve}).
   *
   * @return the value
   */
  @SifenField(id = "GENFE003", xml = "mOtEve")
  public String reason() {
    return reason;
  }

  /**
   * Naturaleza del receptor.
   *
   * <p>SIFEN field {@code GENFE004} ({@code iNatRec}).
   *
   * @return the value
   */
  @SifenField(id = "GENFE004", xml = "iNatRec")
  public String receiverNatureCode() {
    return receiverNatureCode;
  }

  /**
   * Returns {@link #receiverNatureCode()} (GENFE004) as {@link ReceiverNature}.
   *
   * @return the constant, or empty if the code is not set or this SDK version does not know it
   */
  public Optional<ReceiverNature> receiverNature() {
    return ReceiverNature.fromCode(receiverNatureCode);
  }

  /**
   * Tipo de operación.
   *
   * <p>SIFEN field {@code GENFE027} ({@code iTiOpe}).
   *
   * @return the value
   */
  @SifenField(id = "GENFE027", xml = "iTiOpe")
  public String operationTypeCode() {
    return operationTypeCode;
  }

  /**
   * Returns {@link #operationTypeCode()} (GENFE027) as {@link OperationType}.
   *
   * @return the constant, or empty if the code is not set or this SDK version does not know it
   */
  public Optional<OperationType> operationType() {
    return OperationType.fromCode(operationTypeCode);
  }

  /**
   * Código de país del receptor.
   *
   * <p>SIFEN field {@code GENFE005} ({@code cPaisRec}).
   *
   * @return the value
   */
  @SifenField(id = "GENFE005", xml = "cPaisRec")
  public String receiverCountryCode() {
    return receiverCountryCode;
  }

  /**
   * Descripción del país receptor.
   *
   * <p>SIFEN field {@code GENFE006} ({@code dDesPaisRe}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code GENFE005}. Set it only
   * when that code admits a free description or the bundled catalog does not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GENFE006", xml = "dDesPaisRe")
  public @Nullable String receiverCountryDescription() {
    return receiverCountryDescription;
  }

  /**
   * Tipo de contribuyente receptor.
   *
   * <p>SIFEN field {@code GENFE007} ({@code iTiContRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GENFE007", xml = "iTiContRec")
  public @Nullable String receiverTaxpayerTypeCode() {
    return receiverTaxpayerTypeCode;
  }

  /**
   * Returns {@link #receiverTaxpayerTypeCode()} (GENFE007) as {@link TaxpayerType}.
   *
   * @return the constant, or empty if the code is not set or this SDK version does not know it
   */
  public Optional<TaxpayerType> receiverTaxpayerType() {
    return receiverTaxpayerTypeCode == null
        ? Optional.empty()
        : TaxpayerType.fromCode(receiverTaxpayerTypeCode);
  }

  /**
   * RUC del receptor.
   *
   * <p>SIFEN field {@code GENFE008} ({@code dRucRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GENFE008", xml = "dRucRec")
  public @Nullable String receiverRuc() {
    return receiverRuc;
  }

  /**
   * Dígito verificador del RUC del receptor.
   *
   * <p>SIFEN field {@code GENFE009} ({@code dDVRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GENFE009", xml = "dDVRec")
  public @Nullable String receiverCheckDigit() {
    return receiverCheckDigit;
  }

  /**
   * Tipo de documento de identidad del receptor.
   *
   * <p>SIFEN field {@code GENFE010} ({@code iTipIDRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GENFE010", xml = "iTipIDRec")
  public @Nullable String receiverIdDocumentTypeCode() {
    return receiverIdDocumentTypeCode;
  }

  /**
   * Returns {@link #receiverIdDocumentTypeCode()} (GENFE010) as {@link NominationIdDocumentType}.
   *
   * @return the constant, or empty if the code is not set or this SDK version does not know it
   */
  public Optional<NominationIdDocumentType> receiverIdDocumentType() {
    return receiverIdDocumentTypeCode == null
        ? Optional.empty()
        : NominationIdDocumentType.fromCode(receiverIdDocumentTypeCode);
  }

  /**
   * Descripción del tipo de documento de identidad.
   *
   * <p>SIFEN field {@code GENFE011} ({@code dDTipIDRec}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code GENFE010}. Set it only
   * when that code admits a free description or the bundled catalog does not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GENFE011", xml = "dDTipIDRec")
  public @Nullable String receiverIdDocumentTypeDescription() {
    return receiverIdDocumentTypeDescription;
  }

  /**
   * Número de documento de identidad.
   *
   * <p>SIFEN field {@code GENFE012} ({@code dNumIDRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GENFE012", xml = "dNumIDRec")
  public @Nullable String receiverIdDocumentNumber() {
    return receiverIdDocumentNumber;
  }

  /**
   * Nombre o razón social del receptor del DTE.
   *
   * <p>SIFEN field {@code GENFE013} ({@code dNomRec}).
   *
   * @return the value
   */
  @SifenField(id = "GENFE013", xml = "dNomRec")
  public String receiverName() {
    return receiverName;
  }

  /**
   * Nombre de fantasía.
   *
   * <p>SIFEN field {@code GENFE014} ({@code dNomFanRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GENFE014", xml = "dNomFanRec")
  public @Nullable String receiverTradeName() {
    return receiverTradeName;
  }

  /**
   * Dirección del receptor.
   *
   * <p>SIFEN field {@code GENFE015} ({@code dDirRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GENFE015", xml = "dDirRec")
  public @Nullable String receiverAddress() {
    return receiverAddress;
  }

  /**
   * Número de casa del receptor.
   *
   * <p>SIFEN field {@code GENFE016} ({@code dNumCasRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GENFE016", xml = "dNumCasRec")
  public @Nullable String receiverHouseNumber() {
    return receiverHouseNumber;
  }

  /**
   * Código del departamento del receptor.
   *
   * <p>SIFEN field {@code GENFE017} ({@code cDepRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GENFE017", xml = "cDepRec")
  public @Nullable String receiverDepartmentCode() {
    return receiverDepartmentCode;
  }

  /**
   * Returns {@link #receiverDepartmentCode()} (GENFE017) as {@link Department}.
   *
   * @return the constant, or empty if the code is not set or this SDK version does not know it
   */
  public Optional<Department> receiverDepartment() {
    return receiverDepartmentCode == null
        ? Optional.empty()
        : Department.fromCode(receiverDepartmentCode);
  }

  /**
   * Descripción del departamento del receptor.
   *
   * <p>SIFEN field {@code GENFE018} ({@code dDesDepRec}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code GENFE017}. Set it only
   * when that code admits a free description or the bundled catalog does not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GENFE018", xml = "dDesDepRec")
  public @Nullable String receiverDepartmentDescription() {
    return receiverDepartmentDescription;
  }

  /**
   * Código del distrito del receptor.
   *
   * <p>SIFEN field {@code GENFE019} ({@code cDisRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GENFE019", xml = "cDisRec")
  public @Nullable String receiverDistrictCode() {
    return receiverDistrictCode;
  }

  /**
   * Descripción del distrito del receptor.
   *
   * <p>SIFEN field {@code GENFE020} ({@code dDesDisRec}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code GENFE019}. Set it only
   * when that code admits a free description or the bundled catalog does not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GENFE020", xml = "dDesDisRec")
  public @Nullable String receiverDistrictDescription() {
    return receiverDistrictDescription;
  }

  /**
   * Código de la ciudad del receptor.
   *
   * <p>SIFEN field {@code GENFE021} ({@code cCiuRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GENFE021", xml = "cCiuRec")
  public @Nullable String receiverCityCode() {
    return receiverCityCode;
  }

  /**
   * Descripción de la ciudad del receptor.
   *
   * <p>SIFEN field {@code GENFE022} ({@code dDesCiuRec}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code GENFE021}. Set it only
   * when that code admits a free description or the bundled catalog does not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GENFE022", xml = "dDesCiuRec")
  public @Nullable String receiverCityDescription() {
    return receiverCityDescription;
  }

  /**
   * Número de teléfono del receptor.
   *
   * <p>SIFEN field {@code GENFE023} ({@code dTelRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GENFE023", xml = "dTelRec")
  public @Nullable String receiverPhone() {
    return receiverPhone;
  }

  /**
   * Número de celular del receptor.
   *
   * <p>SIFEN field {@code GENFE024} ({@code dCelRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GENFE024", xml = "dCelRec")
  public @Nullable String receiverMobilePhone() {
    return receiverMobilePhone;
  }

  /**
   * Correo electrónico del receptor.
   *
   * <p>SIFEN field {@code GENFE025} ({@code dEmailRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GENFE025", xml = "dEmailRec")
  public @Nullable String receiverEmail() {
    return receiverEmail;
  }

  /**
   * Código del cliente.
   *
   * <p>SIFEN field {@code GENFE026} ({@code dCodCliente}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GENFE026", xml = "dCodCliente")
  public @Nullable String customerCode() {
    return customerCode;
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
    if (!(other instanceof Nomination)) {
      return false;
    }
    Nomination that = (Nomination) other;
    return Objects.equals(cdc, that.cdc)
        && Objects.equals(reason, that.reason)
        && Objects.equals(receiverNatureCode, that.receiverNatureCode)
        && Objects.equals(operationTypeCode, that.operationTypeCode)
        && Objects.equals(receiverCountryCode, that.receiverCountryCode)
        && Objects.equals(receiverCountryDescription, that.receiverCountryDescription)
        && Objects.equals(receiverTaxpayerTypeCode, that.receiverTaxpayerTypeCode)
        && Objects.equals(receiverRuc, that.receiverRuc)
        && Objects.equals(receiverCheckDigit, that.receiverCheckDigit)
        && Objects.equals(receiverIdDocumentTypeCode, that.receiverIdDocumentTypeCode)
        && Objects.equals(receiverIdDocumentTypeDescription, that.receiverIdDocumentTypeDescription)
        && Objects.equals(receiverIdDocumentNumber, that.receiverIdDocumentNumber)
        && Objects.equals(receiverName, that.receiverName)
        && Objects.equals(receiverTradeName, that.receiverTradeName)
        && Objects.equals(receiverAddress, that.receiverAddress)
        && Objects.equals(receiverHouseNumber, that.receiverHouseNumber)
        && Objects.equals(receiverDepartmentCode, that.receiverDepartmentCode)
        && Objects.equals(receiverDepartmentDescription, that.receiverDepartmentDescription)
        && Objects.equals(receiverDistrictCode, that.receiverDistrictCode)
        && Objects.equals(receiverDistrictDescription, that.receiverDistrictDescription)
        && Objects.equals(receiverCityCode, that.receiverCityCode)
        && Objects.equals(receiverCityDescription, that.receiverCityDescription)
        && Objects.equals(receiverPhone, that.receiverPhone)
        && Objects.equals(receiverMobilePhone, that.receiverMobilePhone)
        && Objects.equals(receiverEmail, that.receiverEmail)
        && Objects.equals(customerCode, that.customerCode);
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
        reason,
        receiverNatureCode,
        operationTypeCode,
        receiverCountryCode,
        receiverCountryDescription,
        receiverTaxpayerTypeCode,
        receiverRuc,
        receiverCheckDigit,
        receiverIdDocumentTypeCode,
        receiverIdDocumentTypeDescription,
        receiverIdDocumentNumber,
        receiverName,
        receiverTradeName,
        receiverAddress,
        receiverHouseNumber,
        receiverDepartmentCode,
        receiverDepartmentDescription,
        receiverDistrictCode,
        receiverDistrictDescription,
        receiverCityCode,
        receiverCityDescription,
        receiverPhone,
        receiverMobilePhone,
        receiverEmail,
        customerCode);
  }

  /**
   * The type name. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "Nomination";
  }

  /**
   * Builder for {@link Nomination}. Not thread-safe; {@link #build()} returns an immutable copy.
   */
  public static final class Builder {

    private @Nullable String cdc;
    private @Nullable String reason;
    private @Nullable String receiverNatureCode;
    private @Nullable String operationTypeCode;
    private @Nullable String receiverCountryCode;
    private @Nullable String receiverCountryDescription;
    private @Nullable String receiverTaxpayerTypeCode;
    private @Nullable String receiverRuc;
    private @Nullable String receiverCheckDigit;
    private @Nullable String receiverIdDocumentTypeCode;
    private @Nullable String receiverIdDocumentTypeDescription;
    private @Nullable String receiverIdDocumentNumber;
    private @Nullable String receiverName;
    private @Nullable String receiverTradeName;
    private @Nullable String receiverAddress;
    private @Nullable String receiverHouseNumber;
    private @Nullable String receiverDepartmentCode;
    private @Nullable String receiverDepartmentDescription;
    private @Nullable String receiverDistrictCode;
    private @Nullable String receiverDistrictDescription;
    private @Nullable String receiverCityCode;
    private @Nullable String receiverCityDescription;
    private @Nullable String receiverPhone;
    private @Nullable String receiverMobilePhone;
    private @Nullable String receiverEmail;
    private @Nullable String customerCode;

    private Builder() {}

    private Builder(Nomination source) {
      this.cdc = source.cdc;
      this.reason = source.reason;
      this.receiverNatureCode = source.receiverNatureCode;
      this.operationTypeCode = source.operationTypeCode;
      this.receiverCountryCode = source.receiverCountryCode;
      this.receiverCountryDescription = source.receiverCountryDescription;
      this.receiverTaxpayerTypeCode = source.receiverTaxpayerTypeCode;
      this.receiverRuc = source.receiverRuc;
      this.receiverCheckDigit = source.receiverCheckDigit;
      this.receiverIdDocumentTypeCode = source.receiverIdDocumentTypeCode;
      this.receiverIdDocumentTypeDescription = source.receiverIdDocumentTypeDescription;
      this.receiverIdDocumentNumber = source.receiverIdDocumentNumber;
      this.receiverName = source.receiverName;
      this.receiverTradeName = source.receiverTradeName;
      this.receiverAddress = source.receiverAddress;
      this.receiverHouseNumber = source.receiverHouseNumber;
      this.receiverDepartmentCode = source.receiverDepartmentCode;
      this.receiverDepartmentDescription = source.receiverDepartmentDescription;
      this.receiverDistrictCode = source.receiverDistrictCode;
      this.receiverDistrictDescription = source.receiverDistrictDescription;
      this.receiverCityCode = source.receiverCityCode;
      this.receiverCityDescription = source.receiverCityDescription;
      this.receiverPhone = source.receiverPhone;
      this.receiverMobilePhone = source.receiverMobilePhone;
      this.receiverEmail = source.receiverEmail;
      this.customerCode = source.customerCode;
    }

    /**
     * Sets {@code cdc} (GENFE002, {@code Id}).
     *
     * @param cdc the value
     * @return this builder
     */
    public Builder cdc(String cdc) {
      this.cdc = cdc;
      return this;
    }

    /**
     * Sets {@code reason} (GENFE003, {@code mOtEve}).
     *
     * @param reason the value
     * @return this builder
     */
    public Builder reason(String reason) {
      this.reason = reason;
      return this;
    }

    /**
     * Sets {@code receiverNatureCode} (GENFE004, {@code iNatRec}).
     *
     * @param receiverNatureCode the value
     * @return this builder
     */
    public Builder receiverNatureCode(String receiverNatureCode) {
      this.receiverNatureCode = receiverNatureCode;
      return this;
    }

    /**
     * Sets {@code receiverNatureCode} (GENFE004, {@code iNatRec}) from {@link ReceiverNature}.
     *
     * @param receiverNature the catalog constant
     * @return this builder
     */
    public Builder receiverNature(ReceiverNature receiverNature) {
      this.receiverNatureCode = receiverNature.code();
      return this;
    }

    /**
     * Sets {@code operationTypeCode} (GENFE027, {@code iTiOpe}).
     *
     * @param operationTypeCode the value
     * @return this builder
     */
    public Builder operationTypeCode(String operationTypeCode) {
      this.operationTypeCode = operationTypeCode;
      return this;
    }

    /**
     * Sets {@code operationTypeCode} (GENFE027, {@code iTiOpe}) from {@link OperationType}.
     *
     * @param operationType the catalog constant
     * @return this builder
     */
    public Builder operationType(OperationType operationType) {
      this.operationTypeCode = operationType.code();
      return this;
    }

    /**
     * Sets {@code receiverCountryCode} (GENFE005, {@code cPaisRec}).
     *
     * @param receiverCountryCode the value
     * @return this builder
     */
    public Builder receiverCountryCode(String receiverCountryCode) {
      this.receiverCountryCode = receiverCountryCode;
      return this;
    }

    /**
     * Sets {@code receiverCountryDescription} (GENFE006, {@code dDesPaisRe}).
     *
     * @param receiverCountryDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder receiverCountryDescription(@Nullable String receiverCountryDescription) {
      this.receiverCountryDescription = receiverCountryDescription;
      return this;
    }

    /**
     * Sets {@code receiverTaxpayerTypeCode} (GENFE007, {@code iTiContRec}).
     *
     * @param receiverTaxpayerTypeCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder receiverTaxpayerTypeCode(@Nullable String receiverTaxpayerTypeCode) {
      this.receiverTaxpayerTypeCode = receiverTaxpayerTypeCode;
      return this;
    }

    /**
     * Sets {@code receiverTaxpayerTypeCode} (GENFE007, {@code iTiContRec}) from {@link
     * TaxpayerType}.
     *
     * @param receiverTaxpayerType the catalog constant
     * @return this builder
     */
    public Builder receiverTaxpayerType(@Nullable TaxpayerType receiverTaxpayerType) {
      this.receiverTaxpayerTypeCode =
          receiverTaxpayerType == null ? null : receiverTaxpayerType.code();
      return this;
    }

    /**
     * Sets {@code receiverRuc} (GENFE008, {@code dRucRec}).
     *
     * @param receiverRuc the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder receiverRuc(@Nullable String receiverRuc) {
      this.receiverRuc = receiverRuc;
      return this;
    }

    /**
     * Sets {@code receiverCheckDigit} (GENFE009, {@code dDVRec}).
     *
     * @param receiverCheckDigit the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder receiverCheckDigit(@Nullable String receiverCheckDigit) {
      this.receiverCheckDigit = receiverCheckDigit;
      return this;
    }

    /**
     * Sets {@code receiverIdDocumentTypeCode} (GENFE010, {@code iTipIDRec}).
     *
     * @param receiverIdDocumentTypeCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder receiverIdDocumentTypeCode(@Nullable String receiverIdDocumentTypeCode) {
      this.receiverIdDocumentTypeCode = receiverIdDocumentTypeCode;
      return this;
    }

    /**
     * Sets {@code receiverIdDocumentTypeCode} (GENFE010, {@code iTipIDRec}) from {@link
     * NominationIdDocumentType}.
     *
     * @param receiverIdDocumentType the catalog constant
     * @return this builder
     */
    public Builder receiverIdDocumentType(
        @Nullable NominationIdDocumentType receiverIdDocumentType) {
      this.receiverIdDocumentTypeCode =
          receiverIdDocumentType == null ? null : receiverIdDocumentType.code();
      return this;
    }

    /**
     * Sets {@code receiverIdDocumentTypeDescription} (GENFE011, {@code dDTipIDRec}).
     *
     * @param receiverIdDocumentTypeDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder receiverIdDocumentTypeDescription(
        @Nullable String receiverIdDocumentTypeDescription) {
      this.receiverIdDocumentTypeDescription = receiverIdDocumentTypeDescription;
      return this;
    }

    /**
     * Sets {@code receiverIdDocumentNumber} (GENFE012, {@code dNumIDRec}).
     *
     * @param receiverIdDocumentNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder receiverIdDocumentNumber(@Nullable String receiverIdDocumentNumber) {
      this.receiverIdDocumentNumber = receiverIdDocumentNumber;
      return this;
    }

    /**
     * Sets {@code receiverName} (GENFE013, {@code dNomRec}).
     *
     * @param receiverName the value
     * @return this builder
     */
    public Builder receiverName(String receiverName) {
      this.receiverName = receiverName;
      return this;
    }

    /**
     * Sets {@code receiverTradeName} (GENFE014, {@code dNomFanRec}).
     *
     * @param receiverTradeName the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder receiverTradeName(@Nullable String receiverTradeName) {
      this.receiverTradeName = receiverTradeName;
      return this;
    }

    /**
     * Sets {@code receiverAddress} (GENFE015, {@code dDirRec}).
     *
     * @param receiverAddress the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder receiverAddress(@Nullable String receiverAddress) {
      this.receiverAddress = receiverAddress;
      return this;
    }

    /**
     * Sets {@code receiverHouseNumber} (GENFE016, {@code dNumCasRec}).
     *
     * @param receiverHouseNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder receiverHouseNumber(@Nullable String receiverHouseNumber) {
      this.receiverHouseNumber = receiverHouseNumber;
      return this;
    }

    /**
     * Sets {@code receiverDepartmentCode} (GENFE017, {@code cDepRec}).
     *
     * @param receiverDepartmentCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder receiverDepartmentCode(@Nullable String receiverDepartmentCode) {
      this.receiverDepartmentCode = receiverDepartmentCode;
      return this;
    }

    /**
     * Sets {@code receiverDepartmentCode} (GENFE017, {@code cDepRec}) from {@link Department}.
     *
     * @param receiverDepartment the catalog constant
     * @return this builder
     */
    public Builder receiverDepartment(@Nullable Department receiverDepartment) {
      this.receiverDepartmentCode = receiverDepartment == null ? null : receiverDepartment.code();
      return this;
    }

    /**
     * Sets {@code receiverDepartmentDescription} (GENFE018, {@code dDesDepRec}).
     *
     * @param receiverDepartmentDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder receiverDepartmentDescription(@Nullable String receiverDepartmentDescription) {
      this.receiverDepartmentDescription = receiverDepartmentDescription;
      return this;
    }

    /**
     * Sets {@code receiverDistrictCode} (GENFE019, {@code cDisRec}).
     *
     * @param receiverDistrictCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder receiverDistrictCode(@Nullable String receiverDistrictCode) {
      this.receiverDistrictCode = receiverDistrictCode;
      return this;
    }

    /**
     * Sets {@code receiverDistrictDescription} (GENFE020, {@code dDesDisRec}).
     *
     * @param receiverDistrictDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder receiverDistrictDescription(@Nullable String receiverDistrictDescription) {
      this.receiverDistrictDescription = receiverDistrictDescription;
      return this;
    }

    /**
     * Sets {@code receiverCityCode} (GENFE021, {@code cCiuRec}).
     *
     * @param receiverCityCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder receiverCityCode(@Nullable String receiverCityCode) {
      this.receiverCityCode = receiverCityCode;
      return this;
    }

    /**
     * Sets {@code receiverCityDescription} (GENFE022, {@code dDesCiuRec}).
     *
     * @param receiverCityDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder receiverCityDescription(@Nullable String receiverCityDescription) {
      this.receiverCityDescription = receiverCityDescription;
      return this;
    }

    /**
     * Sets {@code receiverPhone} (GENFE023, {@code dTelRec}).
     *
     * @param receiverPhone the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder receiverPhone(@Nullable String receiverPhone) {
      this.receiverPhone = receiverPhone;
      return this;
    }

    /**
     * Sets {@code receiverMobilePhone} (GENFE024, {@code dCelRec}).
     *
     * @param receiverMobilePhone the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder receiverMobilePhone(@Nullable String receiverMobilePhone) {
      this.receiverMobilePhone = receiverMobilePhone;
      return this;
    }

    /**
     * Sets {@code receiverEmail} (GENFE025, {@code dEmailRec}).
     *
     * @param receiverEmail the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder receiverEmail(@Nullable String receiverEmail) {
      this.receiverEmail = receiverEmail;
      return this;
    }

    /**
     * Sets {@code customerCode} (GENFE026, {@code dCodCliente}).
     *
     * @param customerCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder customerCode(@Nullable String customerCode) {
      this.customerCode = customerCode;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public Nomination build() {
      return new Nomination(this);
    }
  }
}
