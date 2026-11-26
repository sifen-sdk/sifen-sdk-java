/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Grupo de campos que identifican al receptor.
 *
 * <p>SIFEN group {@code D200} ({@code gDatRec}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "D200", xml = "gDatRec")
public final class Receiver {

  private final String natureCode;
  private final String operationTypeCode;
  private final String countryCode;
  private final @Nullable String countryDescription;
  private final @Nullable String taxpayerTypeCode;
  private final @Nullable String ruc;
  private final @Nullable String checkDigit;
  private final @Nullable String idTypeCode;
  private final @Nullable String idTypeDescription;
  private final @Nullable String idNumber;
  private final String name;
  private final @Nullable String tradeName;
  private final @Nullable String address;
  private final @Nullable String houseNumber;
  private final @Nullable String departmentCode;
  private final @Nullable String departmentDescription;
  private final @Nullable String districtCode;
  private final @Nullable String districtDescription;
  private final @Nullable String cityCode;
  private final @Nullable String cityDescription;
  private final @Nullable String phone;
  private final @Nullable String mobilePhone;
  private final @Nullable String email;
  private final @Nullable String customerCode;

  private Receiver(Builder b) {
    this.natureCode = Checks.required(b.natureCode, "natureCode", "D201", "iNatRec");
    this.operationTypeCode =
        Checks.required(b.operationTypeCode, "operationTypeCode", "D202", "iTiOpe");
    this.countryCode = Checks.required(b.countryCode, "countryCode", "D203", "cPaisRec");
    this.countryDescription = b.countryDescription;
    this.taxpayerTypeCode = b.taxpayerTypeCode;
    this.ruc = b.ruc;
    this.checkDigit = b.checkDigit;
    this.idTypeCode = b.idTypeCode;
    this.idTypeDescription = b.idTypeDescription;
    this.idNumber = b.idNumber;
    this.name = Checks.required(b.name, "name", "D211", "dNomRec");
    this.tradeName = b.tradeName;
    this.address = b.address;
    this.houseNumber = b.houseNumber;
    this.departmentCode = b.departmentCode;
    this.departmentDescription = b.departmentDescription;
    this.districtCode = b.districtCode;
    this.districtDescription = b.districtDescription;
    this.cityCode = b.cityCode;
    this.cityDescription = b.cityDescription;
    this.phone = b.phone;
    this.mobilePhone = b.mobilePhone;
    this.email = b.email;
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
   * Naturaleza del receptor.
   *
   * <p>SIFEN field {@code D201} ({@code iNatRec}).
   *
   * @return the value
   */
  @SifenField(id = "D201", xml = "iNatRec")
  public String natureCode() {
    return natureCode;
  }

  /**
   * Tipo de operación.
   *
   * <p>SIFEN field {@code D202} ({@code iTiOpe}).
   *
   * @return the value
   */
  @SifenField(id = "D202", xml = "iTiOpe")
  public String operationTypeCode() {
    return operationTypeCode;
  }

  /**
   * Código de país del receptor.
   *
   * <p>SIFEN field {@code D203} ({@code cPaisRec}).
   *
   * @return the value
   */
  @SifenField(id = "D203", xml = "cPaisRec")
  public String countryCode() {
    return countryCode;
  }

  /**
   * Descripción del país receptor.
   *
   * <p>SIFEN field {@code D204} ({@code dDesPaisRe}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code D203}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D204", xml = "dDesPaisRe")
  public @Nullable String countryDescription() {
    return countryDescription;
  }

  /**
   * Tipo de contribuyente receptor.
   *
   * <p>SIFEN field {@code D205} ({@code iTiContRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D205", xml = "iTiContRec")
  public @Nullable String taxpayerTypeCode() {
    return taxpayerTypeCode;
  }

  /**
   * RUC del receptor.
   *
   * <p>SIFEN field {@code D206} ({@code dRucRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D206", xml = "dRucRec")
  public @Nullable String ruc() {
    return ruc;
  }

  /**
   * Dígito verificador del RUC del receptor.
   *
   * <p>SIFEN field {@code D207} ({@code dDVRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D207", xml = "dDVRec")
  public @Nullable String checkDigit() {
    return checkDigit;
  }

  /**
   * Tipo de documento de identidad del receptor.
   *
   * <p>SIFEN field {@code D208} ({@code iTipIDRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D208", xml = "iTipIDRec")
  public @Nullable String idTypeCode() {
    return idTypeCode;
  }

  /**
   * Descripción del tipo de documento de identidad.
   *
   * <p>SIFEN field {@code D209} ({@code dDTipIDRec}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code D208}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D209", xml = "dDTipIDRec")
  public @Nullable String idTypeDescription() {
    return idTypeDescription;
  }

  /**
   * Número de documento de identidad.
   *
   * <p>SIFEN field {@code D210} ({@code dNumIDRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D210", xml = "dNumIDRec")
  public @Nullable String idNumber() {
    return idNumber;
  }

  /**
   * Nombre o razón social del receptor del DE.
   *
   * <p>SIFEN field {@code D211} ({@code dNomRec}).
   *
   * @return the value
   */
  @SifenField(id = "D211", xml = "dNomRec")
  public String name() {
    return name;
  }

  /**
   * Nombre de fantasía.
   *
   * <p>SIFEN field {@code D212} ({@code dNomFanRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D212", xml = "dNomFanRec")
  public @Nullable String tradeName() {
    return tradeName;
  }

  /**
   * Dirección del receptor.
   *
   * <p>SIFEN field {@code D213} ({@code dDirRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D213", xml = "dDirRec")
  public @Nullable String address() {
    return address;
  }

  /**
   * Número de casa del receptor.
   *
   * <p>SIFEN field {@code D218} ({@code dNumCasRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D218", xml = "dNumCasRec")
  public @Nullable String houseNumber() {
    return houseNumber;
  }

  /**
   * Código del departamento del receptor.
   *
   * <p>SIFEN field {@code D219} ({@code cDepRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D219", xml = "cDepRec")
  public @Nullable String departmentCode() {
    return departmentCode;
  }

  /**
   * Descripción del departamento del receptor.
   *
   * <p>SIFEN field {@code D220} ({@code dDesDepRec}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code D219}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D220", xml = "dDesDepRec")
  public @Nullable String departmentDescription() {
    return departmentDescription;
  }

  /**
   * Código del distrito del receptor.
   *
   * <p>SIFEN field {@code D221} ({@code cDisRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D221", xml = "cDisRec")
  public @Nullable String districtCode() {
    return districtCode;
  }

  /**
   * Descripción del distrito del receptor.
   *
   * <p>SIFEN field {@code D222} ({@code dDesDisRec}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code D221}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D222", xml = "dDesDisRec")
  public @Nullable String districtDescription() {
    return districtDescription;
  }

  /**
   * Código de la ciudad del receptor.
   *
   * <p>SIFEN field {@code D223} ({@code cCiuRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D223", xml = "cCiuRec")
  public @Nullable String cityCode() {
    return cityCode;
  }

  /**
   * Descripción de la ciudad del receptor.
   *
   * <p>SIFEN field {@code D224} ({@code dDesCiuRec}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code D223}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D224", xml = "dDesCiuRec")
  public @Nullable String cityDescription() {
    return cityDescription;
  }

  /**
   * Número de teléfono del receptor.
   *
   * <p>SIFEN field {@code D214} ({@code dTelRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D214", xml = "dTelRec")
  public @Nullable String phone() {
    return phone;
  }

  /**
   * Número de celular del receptor.
   *
   * <p>SIFEN field {@code D215} ({@code dCelRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D215", xml = "dCelRec")
  public @Nullable String mobilePhone() {
    return mobilePhone;
  }

  /**
   * Correo electrónico del receptor.
   *
   * <p>SIFEN field {@code D216} ({@code dEmailRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D216", xml = "dEmailRec")
  public @Nullable String email() {
    return email;
  }

  /**
   * Código del cliente.
   *
   * <p>SIFEN field {@code D217} ({@code dCodCliente}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D217", xml = "dCodCliente")
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
    if (!(other instanceof Receiver)) {
      return false;
    }
    Receiver that = (Receiver) other;
    return Objects.equals(natureCode, that.natureCode)
        && Objects.equals(operationTypeCode, that.operationTypeCode)
        && Objects.equals(countryCode, that.countryCode)
        && Objects.equals(countryDescription, that.countryDescription)
        && Objects.equals(taxpayerTypeCode, that.taxpayerTypeCode)
        && Objects.equals(ruc, that.ruc)
        && Objects.equals(checkDigit, that.checkDigit)
        && Objects.equals(idTypeCode, that.idTypeCode)
        && Objects.equals(idTypeDescription, that.idTypeDescription)
        && Objects.equals(idNumber, that.idNumber)
        && Objects.equals(name, that.name)
        && Objects.equals(tradeName, that.tradeName)
        && Objects.equals(address, that.address)
        && Objects.equals(houseNumber, that.houseNumber)
        && Objects.equals(departmentCode, that.departmentCode)
        && Objects.equals(departmentDescription, that.departmentDescription)
        && Objects.equals(districtCode, that.districtCode)
        && Objects.equals(districtDescription, that.districtDescription)
        && Objects.equals(cityCode, that.cityCode)
        && Objects.equals(cityDescription, that.cityDescription)
        && Objects.equals(phone, that.phone)
        && Objects.equals(mobilePhone, that.mobilePhone)
        && Objects.equals(email, that.email)
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
        natureCode,
        operationTypeCode,
        countryCode,
        countryDescription,
        taxpayerTypeCode,
        ruc,
        checkDigit,
        idTypeCode,
        idTypeDescription,
        idNumber,
        name,
        tradeName,
        address,
        houseNumber,
        departmentCode,
        departmentDescription,
        districtCode,
        districtDescription,
        cityCode,
        cityDescription,
        phone,
        mobilePhone,
        email,
        customerCode);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "Receiver";
  }

  /** Builder for {@link Receiver}. Not thread-safe; {@link #build()} returns an immutable copy. */
  public static final class Builder {

    private @Nullable String natureCode;
    private @Nullable String operationTypeCode;
    private @Nullable String countryCode;
    private @Nullable String countryDescription;
    private @Nullable String taxpayerTypeCode;
    private @Nullable String ruc;
    private @Nullable String checkDigit;
    private @Nullable String idTypeCode;
    private @Nullable String idTypeDescription;
    private @Nullable String idNumber;
    private @Nullable String name;
    private @Nullable String tradeName;
    private @Nullable String address;
    private @Nullable String houseNumber;
    private @Nullable String departmentCode;
    private @Nullable String departmentDescription;
    private @Nullable String districtCode;
    private @Nullable String districtDescription;
    private @Nullable String cityCode;
    private @Nullable String cityDescription;
    private @Nullable String phone;
    private @Nullable String mobilePhone;
    private @Nullable String email;
    private @Nullable String customerCode;

    private Builder() {}

    private Builder(Receiver source) {
      this.natureCode = source.natureCode;
      this.operationTypeCode = source.operationTypeCode;
      this.countryCode = source.countryCode;
      this.countryDescription = source.countryDescription;
      this.taxpayerTypeCode = source.taxpayerTypeCode;
      this.ruc = source.ruc;
      this.checkDigit = source.checkDigit;
      this.idTypeCode = source.idTypeCode;
      this.idTypeDescription = source.idTypeDescription;
      this.idNumber = source.idNumber;
      this.name = source.name;
      this.tradeName = source.tradeName;
      this.address = source.address;
      this.houseNumber = source.houseNumber;
      this.departmentCode = source.departmentCode;
      this.departmentDescription = source.departmentDescription;
      this.districtCode = source.districtCode;
      this.districtDescription = source.districtDescription;
      this.cityCode = source.cityCode;
      this.cityDescription = source.cityDescription;
      this.phone = source.phone;
      this.mobilePhone = source.mobilePhone;
      this.email = source.email;
      this.customerCode = source.customerCode;
    }

    /**
     * Sets {@code natureCode} (D201, {@code iNatRec}).
     *
     * @param natureCode the value
     * @return this builder
     */
    public Builder natureCode(String natureCode) {
      this.natureCode = natureCode;
      return this;
    }

    /**
     * Sets {@code operationTypeCode} (D202, {@code iTiOpe}).
     *
     * @param operationTypeCode the value
     * @return this builder
     */
    public Builder operationTypeCode(String operationTypeCode) {
      this.operationTypeCode = operationTypeCode;
      return this;
    }

    /**
     * Sets {@code countryCode} (D203, {@code cPaisRec}).
     *
     * @param countryCode the value
     * @return this builder
     */
    public Builder countryCode(String countryCode) {
      this.countryCode = countryCode;
      return this;
    }

    /**
     * Sets {@code countryDescription} (D204, {@code dDesPaisRe}).
     *
     * @param countryDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder countryDescription(@Nullable String countryDescription) {
      this.countryDescription = countryDescription;
      return this;
    }

    /**
     * Sets {@code taxpayerTypeCode} (D205, {@code iTiContRec}).
     *
     * @param taxpayerTypeCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder taxpayerTypeCode(@Nullable String taxpayerTypeCode) {
      this.taxpayerTypeCode = taxpayerTypeCode;
      return this;
    }

    /**
     * Sets {@code ruc} (D206, {@code dRucRec}).
     *
     * @param ruc the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder ruc(@Nullable String ruc) {
      this.ruc = ruc;
      return this;
    }

    /**
     * Sets {@code checkDigit} (D207, {@code dDVRec}).
     *
     * @param checkDigit the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder checkDigit(@Nullable String checkDigit) {
      this.checkDigit = checkDigit;
      return this;
    }

    /**
     * Sets {@code idTypeCode} (D208, {@code iTipIDRec}).
     *
     * @param idTypeCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder idTypeCode(@Nullable String idTypeCode) {
      this.idTypeCode = idTypeCode;
      return this;
    }

    /**
     * Sets {@code idTypeDescription} (D209, {@code dDTipIDRec}).
     *
     * @param idTypeDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder idTypeDescription(@Nullable String idTypeDescription) {
      this.idTypeDescription = idTypeDescription;
      return this;
    }

    /**
     * Sets {@code idNumber} (D210, {@code dNumIDRec}).
     *
     * @param idNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder idNumber(@Nullable String idNumber) {
      this.idNumber = idNumber;
      return this;
    }

    /**
     * Sets {@code name} (D211, {@code dNomRec}).
     *
     * @param name the value
     * @return this builder
     */
    public Builder name(String name) {
      this.name = name;
      return this;
    }

    /**
     * Sets {@code tradeName} (D212, {@code dNomFanRec}).
     *
     * @param tradeName the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder tradeName(@Nullable String tradeName) {
      this.tradeName = tradeName;
      return this;
    }

    /**
     * Sets {@code address} (D213, {@code dDirRec}).
     *
     * @param address the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder address(@Nullable String address) {
      this.address = address;
      return this;
    }

    /**
     * Sets {@code houseNumber} (D218, {@code dNumCasRec}).
     *
     * @param houseNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder houseNumber(@Nullable String houseNumber) {
      this.houseNumber = houseNumber;
      return this;
    }

    /**
     * Sets {@code departmentCode} (D219, {@code cDepRec}).
     *
     * @param departmentCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder departmentCode(@Nullable String departmentCode) {
      this.departmentCode = departmentCode;
      return this;
    }

    /**
     * Sets {@code departmentDescription} (D220, {@code dDesDepRec}).
     *
     * @param departmentDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder departmentDescription(@Nullable String departmentDescription) {
      this.departmentDescription = departmentDescription;
      return this;
    }

    /**
     * Sets {@code districtCode} (D221, {@code cDisRec}).
     *
     * @param districtCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder districtCode(@Nullable String districtCode) {
      this.districtCode = districtCode;
      return this;
    }

    /**
     * Sets {@code districtDescription} (D222, {@code dDesDisRec}).
     *
     * @param districtDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder districtDescription(@Nullable String districtDescription) {
      this.districtDescription = districtDescription;
      return this;
    }

    /**
     * Sets {@code cityCode} (D223, {@code cCiuRec}).
     *
     * @param cityCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder cityCode(@Nullable String cityCode) {
      this.cityCode = cityCode;
      return this;
    }

    /**
     * Sets {@code cityDescription} (D224, {@code dDesCiuRec}).
     *
     * @param cityDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder cityDescription(@Nullable String cityDescription) {
      this.cityDescription = cityDescription;
      return this;
    }

    /**
     * Sets {@code phone} (D214, {@code dTelRec}).
     *
     * @param phone the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder phone(@Nullable String phone) {
      this.phone = phone;
      return this;
    }

    /**
     * Sets {@code mobilePhone} (D215, {@code dCelRec}).
     *
     * @param mobilePhone the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder mobilePhone(@Nullable String mobilePhone) {
      this.mobilePhone = mobilePhone;
      return this;
    }

    /**
     * Sets {@code email} (D216, {@code dEmailRec}).
     *
     * @param email the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder email(@Nullable String email) {
      this.email = email;
      return this;
    }

    /**
     * Sets {@code customerCode} (D217, {@code dCodCliente}).
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
    public Receiver build() {
      return new Receiver(this);
    }
  }
}
