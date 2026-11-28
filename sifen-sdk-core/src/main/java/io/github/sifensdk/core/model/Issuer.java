/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import io.github.sifensdk.core.catalog.Department;
import io.github.sifensdk.core.catalog.RegimeType;
import io.github.sifensdk.core.catalog.TaxpayerType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Grupo de campos que identifican al emisor.
 *
 * <p>SIFEN group {@code D100} ({@code gEmis}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "D100", xml = "gEmis")
public final class Issuer {

  private final String ruc;
  private final String checkDigit;
  private final String taxpayerTypeCode;
  private final @Nullable String regimeTypeCode;
  private final String name;
  private final @Nullable String tradeName;
  private final String address;
  private final String houseNumber;
  private final @Nullable String addressComplement1;
  private final @Nullable String addressComplement2;
  private final String departmentCode;
  private final @Nullable String departmentDescription;
  private final @Nullable String districtCode;
  private final @Nullable String districtDescription;
  private final String cityCode;
  private final @Nullable String cityDescription;
  private final String phone;
  private final String email;
  private final @Nullable String branchName;
  private final List<EconomicActivity> economicActivities;
  private final @Nullable GenerationResponsible generationResponsible;

  private Issuer(Builder b) {
    this.ruc = Checks.required(b.ruc, "ruc", "D101", "dRucEm");
    this.checkDigit = Checks.required(b.checkDigit, "checkDigit", "D102", "dDVEmi");
    this.taxpayerTypeCode =
        Checks.required(b.taxpayerTypeCode, "taxpayerTypeCode", "D103", "iTipCont");
    this.regimeTypeCode = b.regimeTypeCode;
    this.name = Checks.required(b.name, "name", "D105", "dNomEmi");
    this.tradeName = b.tradeName;
    this.address = Checks.required(b.address, "address", "D107", "dDirEmi");
    this.houseNumber = Checks.required(b.houseNumber, "houseNumber", "D108", "dNumCas");
    this.addressComplement1 = b.addressComplement1;
    this.addressComplement2 = b.addressComplement2;
    this.departmentCode = Checks.required(b.departmentCode, "departmentCode", "D111", "cDepEmi");
    this.departmentDescription = b.departmentDescription;
    this.districtCode = b.districtCode;
    this.districtDescription = b.districtDescription;
    this.cityCode = Checks.required(b.cityCode, "cityCode", "D115", "cCiuEmi");
    this.cityDescription = b.cityDescription;
    this.phone = Checks.required(b.phone, "phone", "D117", "dTelEmi");
    this.email = Checks.required(b.email, "email", "D118", "dEmailE");
    this.branchName = b.branchName;
    this.economicActivities = List.copyOf(b.economicActivities);
    this.generationResponsible = b.generationResponsible;
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
   * RUC del contribuyente emisor.
   *
   * <p>SIFEN field {@code D101} ({@code dRucEm}).
   *
   * @return the value
   */
  @SifenField(id = "D101", xml = "dRucEm")
  public String ruc() {
    return ruc;
  }

  /**
   * Dígito verificador del RUC del contribuyente emisor.
   *
   * <p>SIFEN field {@code D102} ({@code dDVEmi}).
   *
   * @return the value
   */
  @SifenField(id = "D102", xml = "dDVEmi")
  public String checkDigit() {
    return checkDigit;
  }

  /**
   * Tipo de contribuyente.
   *
   * <p>SIFEN field {@code D103} ({@code iTipCont}).
   *
   * @return the value
   */
  @SifenField(id = "D103", xml = "iTipCont")
  public String taxpayerTypeCode() {
    return taxpayerTypeCode;
  }

  /**
   * Returns {@link #taxpayerTypeCode()} (D103) as {@link TaxpayerType}.
   *
   * @return the constant, or empty if the code is not set or this SDK version does not know it
   */
  public Optional<TaxpayerType> taxpayerType() {
    return TaxpayerType.fromCode(taxpayerTypeCode);
  }

  /**
   * Tipo de régimen.
   *
   * <p>SIFEN field {@code D104} ({@code cTipReg}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D104", xml = "cTipReg")
  public @Nullable String regimeTypeCode() {
    return regimeTypeCode;
  }

  /**
   * Returns {@link #regimeTypeCode()} (D104) as {@link RegimeType}.
   *
   * @return the constant, or empty if the code is not set or this SDK version does not know it
   */
  public Optional<RegimeType> regimeType() {
    return regimeTypeCode == null ? Optional.empty() : RegimeType.fromCode(regimeTypeCode);
  }

  /**
   * Nombre o razón social del emisor del DE.
   *
   * <p>SIFEN field {@code D105} ({@code dNomEmi}).
   *
   * @return the value
   */
  @SifenField(id = "D105", xml = "dNomEmi")
  public String name() {
    return name;
  }

  /**
   * Nombre de fantasía.
   *
   * <p>SIFEN field {@code D106} ({@code dNomFanEmi}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D106", xml = "dNomFanEmi")
  public @Nullable String tradeName() {
    return tradeName;
  }

  /**
   * Dirección del local donde se emite el DE.
   *
   * <p>SIFEN field {@code D107} ({@code dDirEmi}).
   *
   * @return the value
   */
  @SifenField(id = "D107", xml = "dDirEmi")
  public String address() {
    return address;
  }

  /**
   * Número de casa.
   *
   * <p>SIFEN field {@code D108} ({@code dNumCas}).
   *
   * @return the value
   */
  @SifenField(id = "D108", xml = "dNumCas")
  public String houseNumber() {
    return houseNumber;
  }

  /**
   * Complemento de dirección 1.
   *
   * <p>SIFEN field {@code D109} ({@code dCompDir1}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D109", xml = "dCompDir1")
  public @Nullable String addressComplement1() {
    return addressComplement1;
  }

  /**
   * Complemento de dirección 2.
   *
   * <p>SIFEN field {@code D110} ({@code dCompDir2}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D110", xml = "dCompDir2")
  public @Nullable String addressComplement2() {
    return addressComplement2;
  }

  /**
   * Código del departamento de emisión.
   *
   * <p>SIFEN field {@code D111} ({@code cDepEmi}).
   *
   * @return the value
   */
  @SifenField(id = "D111", xml = "cDepEmi")
  public String departmentCode() {
    return departmentCode;
  }

  /**
   * Returns {@link #departmentCode()} (D111) as {@link Department}.
   *
   * @return the constant, or empty if the code is not set or this SDK version does not know it
   */
  public Optional<Department> department() {
    return Department.fromCode(departmentCode);
  }

  /**
   * Descripción del departamento de emisión.
   *
   * <p>SIFEN field {@code D112} ({@code dDesDepEmi}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code D111}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D112", xml = "dDesDepEmi")
  public @Nullable String departmentDescription() {
    return departmentDescription;
  }

  /**
   * Código del distrito de emisión.
   *
   * <p>SIFEN field {@code D113} ({@code cDisEmi}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D113", xml = "cDisEmi")
  public @Nullable String districtCode() {
    return districtCode;
  }

  /**
   * Descripción del distrito de emisión.
   *
   * <p>SIFEN field {@code D114} ({@code dDesDisEmi}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code D113}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D114", xml = "dDesDisEmi")
  public @Nullable String districtDescription() {
    return districtDescription;
  }

  /**
   * Código de la ciudad de emisión.
   *
   * <p>SIFEN field {@code D115} ({@code cCiuEmi}).
   *
   * @return the value
   */
  @SifenField(id = "D115", xml = "cCiuEmi")
  public String cityCode() {
    return cityCode;
  }

  /**
   * Descripción de la ciudad de emisión.
   *
   * <p>SIFEN field {@code D116} ({@code dDesCiuEmi}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code D115}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D116", xml = "dDesCiuEmi")
  public @Nullable String cityDescription() {
    return cityDescription;
  }

  /**
   * Teléfono local de emisión de DE.
   *
   * <p>SIFEN field {@code D117} ({@code dTelEmi}).
   *
   * @return the value
   */
  @SifenField(id = "D117", xml = "dTelEmi")
  public String phone() {
    return phone;
  }

  /**
   * Correo electrónico del emisor.
   *
   * <p>SIFEN field {@code D118} ({@code dEmailE}).
   *
   * @return the value
   */
  @SifenField(id = "D118", xml = "dEmailE")
  public String email() {
    return email;
  }

  /**
   * Denominación comercial de la sucursal.
   *
   * <p>SIFEN field {@code D119} ({@code dDenSuc}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D119", xml = "dDenSuc")
  public @Nullable String branchName() {
    return branchName;
  }

  /**
   * Grupo de campos que describen la actividad económica del emisor.
   *
   * <p>SIFEN group {@code D130} ({@code gActEco}).
   *
   * @return an unmodifiable list, possibly empty
   */
  @SifenField(id = "D130", xml = "gActEco")
  public List<EconomicActivity> economicActivities() {
    return economicActivities;
  }

  /**
   * Grupo de campos que identifican al responsable de la generación del DE.
   *
   * <p>SIFEN group {@code D140} ({@code gRespDE}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D140", xml = "gRespDE")
  public @Nullable GenerationResponsible generationResponsible() {
    return generationResponsible;
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
    if (!(other instanceof Issuer)) {
      return false;
    }
    Issuer that = (Issuer) other;
    return Objects.equals(ruc, that.ruc)
        && Objects.equals(checkDigit, that.checkDigit)
        && Objects.equals(taxpayerTypeCode, that.taxpayerTypeCode)
        && Objects.equals(regimeTypeCode, that.regimeTypeCode)
        && Objects.equals(name, that.name)
        && Objects.equals(tradeName, that.tradeName)
        && Objects.equals(address, that.address)
        && Objects.equals(houseNumber, that.houseNumber)
        && Objects.equals(addressComplement1, that.addressComplement1)
        && Objects.equals(addressComplement2, that.addressComplement2)
        && Objects.equals(departmentCode, that.departmentCode)
        && Objects.equals(departmentDescription, that.departmentDescription)
        && Objects.equals(districtCode, that.districtCode)
        && Objects.equals(districtDescription, that.districtDescription)
        && Objects.equals(cityCode, that.cityCode)
        && Objects.equals(cityDescription, that.cityDescription)
        && Objects.equals(phone, that.phone)
        && Objects.equals(email, that.email)
        && Objects.equals(branchName, that.branchName)
        && Objects.equals(economicActivities, that.economicActivities)
        && Objects.equals(generationResponsible, that.generationResponsible);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        ruc,
        checkDigit,
        taxpayerTypeCode,
        regimeTypeCode,
        name,
        tradeName,
        address,
        houseNumber,
        addressComplement1,
        addressComplement2,
        departmentCode,
        departmentDescription,
        districtCode,
        districtDescription,
        cityCode,
        cityDescription,
        phone,
        email,
        branchName,
        economicActivities,
        generationResponsible);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "Issuer[economicActivities=" + economicActivities.size() + "]";
  }

  /** Builder for {@link Issuer}. Not thread-safe; {@link #build()} returns an immutable copy. */
  public static final class Builder {

    private @Nullable String ruc;
    private @Nullable String checkDigit;
    private @Nullable String taxpayerTypeCode;
    private @Nullable String regimeTypeCode;
    private @Nullable String name;
    private @Nullable String tradeName;
    private @Nullable String address;
    private @Nullable String houseNumber;
    private @Nullable String addressComplement1;
    private @Nullable String addressComplement2;
    private @Nullable String departmentCode;
    private @Nullable String departmentDescription;
    private @Nullable String districtCode;
    private @Nullable String districtDescription;
    private @Nullable String cityCode;
    private @Nullable String cityDescription;
    private @Nullable String phone;
    private @Nullable String email;
    private @Nullable String branchName;
    private List<EconomicActivity> economicActivities = new ArrayList<>();
    private @Nullable GenerationResponsible generationResponsible;

    private Builder() {}

    private Builder(Issuer source) {
      this.ruc = source.ruc;
      this.checkDigit = source.checkDigit;
      this.taxpayerTypeCode = source.taxpayerTypeCode;
      this.regimeTypeCode = source.regimeTypeCode;
      this.name = source.name;
      this.tradeName = source.tradeName;
      this.address = source.address;
      this.houseNumber = source.houseNumber;
      this.addressComplement1 = source.addressComplement1;
      this.addressComplement2 = source.addressComplement2;
      this.departmentCode = source.departmentCode;
      this.departmentDescription = source.departmentDescription;
      this.districtCode = source.districtCode;
      this.districtDescription = source.districtDescription;
      this.cityCode = source.cityCode;
      this.cityDescription = source.cityDescription;
      this.phone = source.phone;
      this.email = source.email;
      this.branchName = source.branchName;
      this.economicActivities = new ArrayList<>(source.economicActivities);
      this.generationResponsible = source.generationResponsible;
    }

    /**
     * Sets {@code ruc} (D101, {@code dRucEm}).
     *
     * @param ruc the value
     * @return this builder
     */
    public Builder ruc(String ruc) {
      this.ruc = ruc;
      return this;
    }

    /**
     * Sets {@code checkDigit} (D102, {@code dDVEmi}).
     *
     * @param checkDigit the value
     * @return this builder
     */
    public Builder checkDigit(String checkDigit) {
      this.checkDigit = checkDigit;
      return this;
    }

    /**
     * Sets {@code taxpayerTypeCode} (D103, {@code iTipCont}).
     *
     * @param taxpayerTypeCode the value
     * @return this builder
     */
    public Builder taxpayerTypeCode(String taxpayerTypeCode) {
      this.taxpayerTypeCode = taxpayerTypeCode;
      return this;
    }

    /**
     * Sets {@code taxpayerTypeCode} (D103, {@code iTipCont}) from {@link TaxpayerType}.
     *
     * @param taxpayerType the catalog constant
     * @return this builder
     */
    public Builder taxpayerType(TaxpayerType taxpayerType) {
      this.taxpayerTypeCode = taxpayerType.code();
      return this;
    }

    /**
     * Sets {@code regimeTypeCode} (D104, {@code cTipReg}).
     *
     * @param regimeTypeCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder regimeTypeCode(@Nullable String regimeTypeCode) {
      this.regimeTypeCode = regimeTypeCode;
      return this;
    }

    /**
     * Sets {@code regimeTypeCode} (D104, {@code cTipReg}) from {@link RegimeType}.
     *
     * @param regimeType the catalog constant
     * @return this builder
     */
    public Builder regimeType(@Nullable RegimeType regimeType) {
      this.regimeTypeCode = regimeType == null ? null : regimeType.code();
      return this;
    }

    /**
     * Sets {@code name} (D105, {@code dNomEmi}).
     *
     * @param name the value
     * @return this builder
     */
    public Builder name(String name) {
      this.name = name;
      return this;
    }

    /**
     * Sets {@code tradeName} (D106, {@code dNomFanEmi}).
     *
     * @param tradeName the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder tradeName(@Nullable String tradeName) {
      this.tradeName = tradeName;
      return this;
    }

    /**
     * Sets {@code address} (D107, {@code dDirEmi}).
     *
     * @param address the value
     * @return this builder
     */
    public Builder address(String address) {
      this.address = address;
      return this;
    }

    /**
     * Sets {@code houseNumber} (D108, {@code dNumCas}).
     *
     * @param houseNumber the value
     * @return this builder
     */
    public Builder houseNumber(String houseNumber) {
      this.houseNumber = houseNumber;
      return this;
    }

    /**
     * Sets {@code addressComplement1} (D109, {@code dCompDir1}).
     *
     * @param addressComplement1 the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder addressComplement1(@Nullable String addressComplement1) {
      this.addressComplement1 = addressComplement1;
      return this;
    }

    /**
     * Sets {@code addressComplement2} (D110, {@code dCompDir2}).
     *
     * @param addressComplement2 the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder addressComplement2(@Nullable String addressComplement2) {
      this.addressComplement2 = addressComplement2;
      return this;
    }

    /**
     * Sets {@code departmentCode} (D111, {@code cDepEmi}).
     *
     * @param departmentCode the value
     * @return this builder
     */
    public Builder departmentCode(String departmentCode) {
      this.departmentCode = departmentCode;
      return this;
    }

    /**
     * Sets {@code departmentCode} (D111, {@code cDepEmi}) from {@link Department}.
     *
     * @param department the catalog constant
     * @return this builder
     */
    public Builder department(Department department) {
      this.departmentCode = department.code();
      return this;
    }

    /**
     * Sets {@code departmentDescription} (D112, {@code dDesDepEmi}).
     *
     * @param departmentDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder departmentDescription(@Nullable String departmentDescription) {
      this.departmentDescription = departmentDescription;
      return this;
    }

    /**
     * Sets {@code districtCode} (D113, {@code cDisEmi}).
     *
     * @param districtCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder districtCode(@Nullable String districtCode) {
      this.districtCode = districtCode;
      return this;
    }

    /**
     * Sets {@code districtDescription} (D114, {@code dDesDisEmi}).
     *
     * @param districtDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder districtDescription(@Nullable String districtDescription) {
      this.districtDescription = districtDescription;
      return this;
    }

    /**
     * Sets {@code cityCode} (D115, {@code cCiuEmi}).
     *
     * @param cityCode the value
     * @return this builder
     */
    public Builder cityCode(String cityCode) {
      this.cityCode = cityCode;
      return this;
    }

    /**
     * Sets {@code cityDescription} (D116, {@code dDesCiuEmi}).
     *
     * @param cityDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder cityDescription(@Nullable String cityDescription) {
      this.cityDescription = cityDescription;
      return this;
    }

    /**
     * Sets {@code phone} (D117, {@code dTelEmi}).
     *
     * @param phone the value
     * @return this builder
     */
    public Builder phone(String phone) {
      this.phone = phone;
      return this;
    }

    /**
     * Sets {@code email} (D118, {@code dEmailE}).
     *
     * @param email the value
     * @return this builder
     */
    public Builder email(String email) {
      this.email = email;
      return this;
    }

    /**
     * Sets {@code branchName} (D119, {@code dDenSuc}).
     *
     * @param branchName the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder branchName(@Nullable String branchName) {
      this.branchName = branchName;
      return this;
    }

    /**
     * Replaces the list of {@code economicActivities} (D130, {@code gActEco}).
     *
     * @param economicActivities the elements, in document order
     * @return this builder
     */
    public Builder economicActivities(List<EconomicActivity> economicActivities) {
      this.economicActivities = new ArrayList<>(economicActivities);
      return this;
    }

    /**
     * Appends one element to {@code economicActivities} (D130, {@code gActEco}).
     *
     * @param economicActivity the element
     * @return this builder
     */
    public Builder addEconomicActivity(EconomicActivity economicActivity) {
      this.economicActivities.add(Objects.requireNonNull(economicActivity, "economicActivity"));
      return this;
    }

    /**
     * Sets {@code generationResponsible} (D140, {@code gRespDE}).
     *
     * @param generationResponsible the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder generationResponsible(@Nullable GenerationResponsible generationResponsible) {
      this.generationResponsible = generationResponsible;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public Issuer build() {
      return new Issuer(this);
    }
  }
}
