/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Campos que componen la Autofactura Electrónica.
 *
 * <p>SIFEN group {@code E300} ({@code gCamAE}): the seller of a self-invoice (C002 = 4), who is not
 * a taxpayer (or is a foreigner), and the place of the transaction. Immutable and thread-safe;
 * create it with {@link #builder()}. The builder only checks what Java needs (required elements and
 * non-null lists); the SIFEN rules (mandatory, conditional, arithmetic) are verified by the
 * validation step, never here. {@link #toString()} never shows the seller's data.
 */
@SifenField(id = "E300", xml = "gCamAE")
public final class SelfInvoice {

  private final String sellerNatureCode;
  private final @Nullable String sellerNatureDescription;
  private final String sellerIdDocumentTypeCode;
  private final @Nullable String sellerIdDocumentTypeDescription;
  private final String sellerIdDocumentNumber;
  private final String sellerName;
  private final String sellerAddress;
  private final String sellerHouseNumber;
  private final String sellerDepartmentCode;
  private final @Nullable String sellerDepartmentDescription;
  private final @Nullable String sellerDistrictCode;
  private final @Nullable String sellerDistrictDescription;
  private final String sellerCityCode;
  private final @Nullable String sellerCityDescription;
  private final String transactionAddress;
  private final String transactionDepartmentCode;
  private final @Nullable String transactionDepartmentDescription;
  private final @Nullable String transactionDistrictCode;
  private final @Nullable String transactionDistrictDescription;
  private final String transactionCityCode;
  private final @Nullable String transactionCityDescription;

  private SelfInvoice(Builder b) {
    this.sellerNatureCode =
        Checks.required(b.sellerNatureCode, "sellerNatureCode", "E301", "iNatVen");
    this.sellerNatureDescription = b.sellerNatureDescription;
    this.sellerIdDocumentTypeCode =
        Checks.required(
            b.sellerIdDocumentTypeCode, "sellerIdDocumentTypeCode", "E304", "iTipIDVen");
    this.sellerIdDocumentTypeDescription = b.sellerIdDocumentTypeDescription;
    this.sellerIdDocumentNumber =
        Checks.required(b.sellerIdDocumentNumber, "sellerIdDocumentNumber", "E306", "dNumIDVen");
    this.sellerName = Checks.required(b.sellerName, "sellerName", "E307", "dNomVen");
    this.sellerAddress = Checks.required(b.sellerAddress, "sellerAddress", "E308", "dDirVen");
    this.sellerHouseNumber =
        Checks.required(b.sellerHouseNumber, "sellerHouseNumber", "E309", "dNumCasVen");
    this.sellerDepartmentCode =
        Checks.required(b.sellerDepartmentCode, "sellerDepartmentCode", "E310", "cDepVen");
    this.sellerDepartmentDescription = b.sellerDepartmentDescription;
    this.sellerDistrictCode = b.sellerDistrictCode;
    this.sellerDistrictDescription = b.sellerDistrictDescription;
    this.sellerCityCode = Checks.required(b.sellerCityCode, "sellerCityCode", "E314", "cCiuVen");
    this.sellerCityDescription = b.sellerCityDescription;
    this.transactionAddress =
        Checks.required(b.transactionAddress, "transactionAddress", "E316", "dDirProv");
    this.transactionDepartmentCode =
        Checks.required(
            b.transactionDepartmentCode, "transactionDepartmentCode", "E317", "cDepProv");
    this.transactionDepartmentDescription = b.transactionDepartmentDescription;
    this.transactionDistrictCode = b.transactionDistrictCode;
    this.transactionDistrictDescription = b.transactionDistrictDescription;
    this.transactionCityCode =
        Checks.required(b.transactionCityCode, "transactionCityCode", "E321", "cCiuProv");
    this.transactionCityDescription = b.transactionCityDescription;
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
   * Naturaleza del vendedor.
   *
   * <p>SIFEN field {@code E301} ({@code iNatVen}).
   *
   * @return the value
   */
  @SifenField(id = "E301", xml = "iNatVen")
  public String sellerNatureCode() {
    return sellerNatureCode;
  }

  /**
   * Descripción de la naturaleza del vendedor.
   *
   * <p>SIFEN field {@code E302} ({@code dDesNatVen}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E301}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E302", xml = "dDesNatVen")
  public @Nullable String sellerNatureDescription() {
    return sellerNatureDescription;
  }

  /**
   * Tipo de documento de identidad del vendedor.
   *
   * <p>SIFEN field {@code E304} ({@code iTipIDVen}).
   *
   * @return the value
   */
  @SifenField(id = "E304", xml = "iTipIDVen")
  public String sellerIdDocumentTypeCode() {
    return sellerIdDocumentTypeCode;
  }

  /**
   * Descripción del tipo de documento de identidad del vendedor.
   *
   * <p>SIFEN field {@code E305} ({@code dDTipIDVen}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E304}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E305", xml = "dDTipIDVen")
  public @Nullable String sellerIdDocumentTypeDescription() {
    return sellerIdDocumentTypeDescription;
  }

  /**
   * Número de documento de identidad del vendedor.
   *
   * <p>SIFEN field {@code E306} ({@code dNumIDVen}).
   *
   * @return the value
   */
  @SifenField(id = "E306", xml = "dNumIDVen")
  public String sellerIdDocumentNumber() {
    return sellerIdDocumentNumber;
  }

  /**
   * Nombre y apellido del vendedor.
   *
   * <p>SIFEN field {@code E307} ({@code dNomVen}).
   *
   * @return the value
   */
  @SifenField(id = "E307", xml = "dNomVen")
  public String sellerName() {
    return sellerName;
  }

  /**
   * Dirección del vendedor.
   *
   * <p>SIFEN field {@code E308} ({@code dDirVen}).
   *
   * <p>En caso de extranjeros, colocar la dirección en donde se realizó la transacción. Nombre de
   * la calle principal.
   *
   * @return the value
   */
  @SifenField(id = "E308", xml = "dDirVen")
  public String sellerAddress() {
    return sellerAddress;
  }

  /**
   * Número de casa del vendedor.
   *
   * <p>SIFEN field {@code E309} ({@code dNumCasVen}).
   *
   * <p>Si no tiene numeración colocar 0 (cero).
   *
   * @return the value
   */
  @SifenField(id = "E309", xml = "dNumCasVen")
  public String sellerHouseNumber() {
    return sellerHouseNumber;
  }

  /**
   * Código del departamento del vendedor.
   *
   * <p>SIFEN field {@code E310} ({@code cDepVen}).
   *
   * <p>En caso de extranjeros, colocar el departamento en donde se realizó la transacción. Según
   * XSD de Departamentos.
   *
   * @return the value
   */
  @SifenField(id = "E310", xml = "cDepVen")
  public String sellerDepartmentCode() {
    return sellerDepartmentCode;
  }

  /**
   * Descripción del departamento del vendedor.
   *
   * <p>SIFEN field {@code E311} ({@code dDesDepVen}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E310}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E311", xml = "dDesDepVen")
  public @Nullable String sellerDepartmentDescription() {
    return sellerDepartmentDescription;
  }

  /**
   * Código del distrito del vendedor.
   *
   * <p>SIFEN field {@code E312} ({@code cDisVen}).
   *
   * <p>En caso de extranjeros, colocar el distrito en donde se realizó la transacción. Según Tabla
   * 2.1 - Distritos.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E312", xml = "cDisVen")
  public @Nullable String sellerDistrictCode() {
    return sellerDistrictCode;
  }

  /**
   * Descripción del distrito del vendedor.
   *
   * <p>SIFEN field {@code E313} ({@code dDesDisVen}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E312}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E313", xml = "dDesDisVen")
  public @Nullable String sellerDistrictDescription() {
    return sellerDistrictDescription;
  }

  /**
   * Código de la ciudad del vendedor.
   *
   * <p>SIFEN field {@code E314} ({@code cCiuVen}).
   *
   * <p>En caso de extranjeros, colocar la ciudad en donde se realizó la transacción. Según Tabla
   * 2.2 - Ciudades.
   *
   * @return the value
   */
  @SifenField(id = "E314", xml = "cCiuVen")
  public String sellerCityCode() {
    return sellerCityCode;
  }

  /**
   * Descripción de la ciudad del vendedor.
   *
   * <p>SIFEN field {@code E315} ({@code dDesCiuVen}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E314}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E315", xml = "dDesCiuVen")
  public @Nullable String sellerCityDescription() {
    return sellerCityDescription;
  }

  /**
   * Lugar de la transacción.
   *
   * <p>SIFEN field {@code E316} ({@code dDirProv}).
   *
   * <p>Nombre de la calle principal (Dirección donde se provee el servicio o producto).
   *
   * @return the value
   */
  @SifenField(id = "E316", xml = "dDirProv")
  public String transactionAddress() {
    return transactionAddress;
  }

  /**
   * Código del departamento donde se realiza la transacción.
   *
   * <p>SIFEN field {@code E317} ({@code cDepProv}).
   *
   * <p>Según XSD de Departamentos.
   *
   * @return the value
   */
  @SifenField(id = "E317", xml = "cDepProv")
  public String transactionDepartmentCode() {
    return transactionDepartmentCode;
  }

  /**
   * Descripción del departamento donde se realiza la transacción.
   *
   * <p>SIFEN field {@code E318} ({@code dDesDepProv}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E317}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E318", xml = "dDesDepProv")
  public @Nullable String transactionDepartmentDescription() {
    return transactionDepartmentDescription;
  }

  /**
   * Código del distrito donde se realiza la transacción.
   *
   * <p>SIFEN field {@code E319} ({@code cDisProv}).
   *
   * <p>Según Tabla 2.1 - Distritos.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E319", xml = "cDisProv")
  public @Nullable String transactionDistrictCode() {
    return transactionDistrictCode;
  }

  /**
   * Descripción del distrito donde se realiza la transacción.
   *
   * <p>SIFEN field {@code E320} ({@code dDesDisProv}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E319}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E320", xml = "dDesDisProv")
  public @Nullable String transactionDistrictDescription() {
    return transactionDistrictDescription;
  }

  /**
   * Código de la ciudad donde se realiza la transacción.
   *
   * <p>SIFEN field {@code E321} ({@code cCiuProv}).
   *
   * <p>Según Tabla 2.2 - Ciudades.
   *
   * @return the value
   */
  @SifenField(id = "E321", xml = "cCiuProv")
  public String transactionCityCode() {
    return transactionCityCode;
  }

  /**
   * Descripción de la ciudad donde se realiza la transacción.
   *
   * <p>SIFEN field {@code E322} ({@code dDesCiuProv}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E321}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E322", xml = "dDesCiuProv")
  public @Nullable String transactionCityDescription() {
    return transactionCityDescription;
  }

  /**
   * Compares all values.
   *
   * @param other the object to compare with
   * @return whether both hold the same values
   */
  @Override
  public boolean equals(@Nullable Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof SelfInvoice)) {
      return false;
    }
    SelfInvoice that = (SelfInvoice) other;
    return Objects.equals(sellerNatureCode, that.sellerNatureCode)
        && Objects.equals(sellerNatureDescription, that.sellerNatureDescription)
        && Objects.equals(sellerIdDocumentTypeCode, that.sellerIdDocumentTypeCode)
        && Objects.equals(sellerIdDocumentTypeDescription, that.sellerIdDocumentTypeDescription)
        && Objects.equals(sellerIdDocumentNumber, that.sellerIdDocumentNumber)
        && Objects.equals(sellerName, that.sellerName)
        && Objects.equals(sellerAddress, that.sellerAddress)
        && Objects.equals(sellerHouseNumber, that.sellerHouseNumber)
        && Objects.equals(sellerDepartmentCode, that.sellerDepartmentCode)
        && Objects.equals(sellerDepartmentDescription, that.sellerDepartmentDescription)
        && Objects.equals(sellerDistrictCode, that.sellerDistrictCode)
        && Objects.equals(sellerDistrictDescription, that.sellerDistrictDescription)
        && Objects.equals(sellerCityCode, that.sellerCityCode)
        && Objects.equals(sellerCityDescription, that.sellerCityDescription)
        && Objects.equals(transactionAddress, that.transactionAddress)
        && Objects.equals(transactionDepartmentCode, that.transactionDepartmentCode)
        && Objects.equals(transactionDepartmentDescription, that.transactionDepartmentDescription)
        && Objects.equals(transactionDistrictCode, that.transactionDistrictCode)
        && Objects.equals(transactionDistrictDescription, that.transactionDistrictDescription)
        && Objects.equals(transactionCityCode, that.transactionCityCode)
        && Objects.equals(transactionCityDescription, that.transactionCityDescription);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        sellerNatureCode,
        sellerNatureDescription,
        sellerIdDocumentTypeCode,
        sellerIdDocumentTypeDescription,
        sellerIdDocumentNumber,
        sellerName,
        sellerAddress,
        sellerHouseNumber,
        sellerDepartmentCode,
        sellerDepartmentDescription,
        sellerDistrictCode,
        sellerDistrictDescription,
        sellerCityCode,
        sellerCityDescription,
        transactionAddress,
        transactionDepartmentCode,
        transactionDepartmentDescription,
        transactionDistrictCode,
        transactionDistrictDescription,
        transactionCityCode,
        transactionCityDescription);
  }

  /**
   * The type name only. Never shows document values (the seller is usually a natural person).
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "SelfInvoice";
  }

  /**
   * Builder for {@link SelfInvoice}. Not thread-safe; {@link #build()} returns an immutable copy.
   */
  public static final class Builder {

    private @Nullable String sellerNatureCode;
    private @Nullable String sellerNatureDescription;
    private @Nullable String sellerIdDocumentTypeCode;
    private @Nullable String sellerIdDocumentTypeDescription;
    private @Nullable String sellerIdDocumentNumber;
    private @Nullable String sellerName;
    private @Nullable String sellerAddress;
    private @Nullable String sellerHouseNumber;
    private @Nullable String sellerDepartmentCode;
    private @Nullable String sellerDepartmentDescription;
    private @Nullable String sellerDistrictCode;
    private @Nullable String sellerDistrictDescription;
    private @Nullable String sellerCityCode;
    private @Nullable String sellerCityDescription;
    private @Nullable String transactionAddress;
    private @Nullable String transactionDepartmentCode;
    private @Nullable String transactionDepartmentDescription;
    private @Nullable String transactionDistrictCode;
    private @Nullable String transactionDistrictDescription;
    private @Nullable String transactionCityCode;
    private @Nullable String transactionCityDescription;

    private Builder() {}

    private Builder(SelfInvoice source) {
      this.sellerNatureCode = source.sellerNatureCode;
      this.sellerNatureDescription = source.sellerNatureDescription;
      this.sellerIdDocumentTypeCode = source.sellerIdDocumentTypeCode;
      this.sellerIdDocumentTypeDescription = source.sellerIdDocumentTypeDescription;
      this.sellerIdDocumentNumber = source.sellerIdDocumentNumber;
      this.sellerName = source.sellerName;
      this.sellerAddress = source.sellerAddress;
      this.sellerHouseNumber = source.sellerHouseNumber;
      this.sellerDepartmentCode = source.sellerDepartmentCode;
      this.sellerDepartmentDescription = source.sellerDepartmentDescription;
      this.sellerDistrictCode = source.sellerDistrictCode;
      this.sellerDistrictDescription = source.sellerDistrictDescription;
      this.sellerCityCode = source.sellerCityCode;
      this.sellerCityDescription = source.sellerCityDescription;
      this.transactionAddress = source.transactionAddress;
      this.transactionDepartmentCode = source.transactionDepartmentCode;
      this.transactionDepartmentDescription = source.transactionDepartmentDescription;
      this.transactionDistrictCode = source.transactionDistrictCode;
      this.transactionDistrictDescription = source.transactionDistrictDescription;
      this.transactionCityCode = source.transactionCityCode;
      this.transactionCityDescription = source.transactionCityDescription;
    }

    /**
     * Sets {@code sellerNatureCode} (E301, {@code iNatVen}).
     *
     * @param sellerNatureCode the value
     * @return this builder
     */
    public Builder sellerNatureCode(String sellerNatureCode) {
      this.sellerNatureCode = sellerNatureCode;
      return this;
    }

    /**
     * Sets {@code sellerNatureDescription} (E302, {@code dDesNatVen}).
     *
     * @param sellerNatureDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder sellerNatureDescription(@Nullable String sellerNatureDescription) {
      this.sellerNatureDescription = sellerNatureDescription;
      return this;
    }

    /**
     * Sets {@code sellerIdDocumentTypeCode} (E304, {@code iTipIDVen}).
     *
     * @param sellerIdDocumentTypeCode the value
     * @return this builder
     */
    public Builder sellerIdDocumentTypeCode(String sellerIdDocumentTypeCode) {
      this.sellerIdDocumentTypeCode = sellerIdDocumentTypeCode;
      return this;
    }

    /**
     * Sets {@code sellerIdDocumentTypeDescription} (E305, {@code dDTipIDVen}).
     *
     * @param sellerIdDocumentTypeDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder sellerIdDocumentTypeDescription(
        @Nullable String sellerIdDocumentTypeDescription) {
      this.sellerIdDocumentTypeDescription = sellerIdDocumentTypeDescription;
      return this;
    }

    /**
     * Sets {@code sellerIdDocumentNumber} (E306, {@code dNumIDVen}).
     *
     * @param sellerIdDocumentNumber the value
     * @return this builder
     */
    public Builder sellerIdDocumentNumber(String sellerIdDocumentNumber) {
      this.sellerIdDocumentNumber = sellerIdDocumentNumber;
      return this;
    }

    /**
     * Sets {@code sellerName} (E307, {@code dNomVen}).
     *
     * @param sellerName the value
     * @return this builder
     */
    public Builder sellerName(String sellerName) {
      this.sellerName = sellerName;
      return this;
    }

    /**
     * Sets {@code sellerAddress} (E308, {@code dDirVen}).
     *
     * @param sellerAddress the value
     * @return this builder
     */
    public Builder sellerAddress(String sellerAddress) {
      this.sellerAddress = sellerAddress;
      return this;
    }

    /**
     * Sets {@code sellerHouseNumber} (E309, {@code dNumCasVen}).
     *
     * @param sellerHouseNumber the value
     * @return this builder
     */
    public Builder sellerHouseNumber(String sellerHouseNumber) {
      this.sellerHouseNumber = sellerHouseNumber;
      return this;
    }

    /**
     * Sets {@code sellerDepartmentCode} (E310, {@code cDepVen}).
     *
     * @param sellerDepartmentCode the value
     * @return this builder
     */
    public Builder sellerDepartmentCode(String sellerDepartmentCode) {
      this.sellerDepartmentCode = sellerDepartmentCode;
      return this;
    }

    /**
     * Sets {@code sellerDepartmentDescription} (E311, {@code dDesDepVen}).
     *
     * @param sellerDepartmentDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder sellerDepartmentDescription(@Nullable String sellerDepartmentDescription) {
      this.sellerDepartmentDescription = sellerDepartmentDescription;
      return this;
    }

    /**
     * Sets {@code sellerDistrictCode} (E312, {@code cDisVen}).
     *
     * @param sellerDistrictCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder sellerDistrictCode(@Nullable String sellerDistrictCode) {
      this.sellerDistrictCode = sellerDistrictCode;
      return this;
    }

    /**
     * Sets {@code sellerDistrictDescription} (E313, {@code dDesDisVen}).
     *
     * @param sellerDistrictDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder sellerDistrictDescription(@Nullable String sellerDistrictDescription) {
      this.sellerDistrictDescription = sellerDistrictDescription;
      return this;
    }

    /**
     * Sets {@code sellerCityCode} (E314, {@code cCiuVen}).
     *
     * @param sellerCityCode the value
     * @return this builder
     */
    public Builder sellerCityCode(String sellerCityCode) {
      this.sellerCityCode = sellerCityCode;
      return this;
    }

    /**
     * Sets {@code sellerCityDescription} (E315, {@code dDesCiuVen}).
     *
     * @param sellerCityDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder sellerCityDescription(@Nullable String sellerCityDescription) {
      this.sellerCityDescription = sellerCityDescription;
      return this;
    }

    /**
     * Sets {@code transactionAddress} (E316, {@code dDirProv}).
     *
     * @param transactionAddress the value
     * @return this builder
     */
    public Builder transactionAddress(String transactionAddress) {
      this.transactionAddress = transactionAddress;
      return this;
    }

    /**
     * Sets {@code transactionDepartmentCode} (E317, {@code cDepProv}).
     *
     * @param transactionDepartmentCode the value
     * @return this builder
     */
    public Builder transactionDepartmentCode(String transactionDepartmentCode) {
      this.transactionDepartmentCode = transactionDepartmentCode;
      return this;
    }

    /**
     * Sets {@code transactionDepartmentDescription} (E318, {@code dDesDepProv}).
     *
     * @param transactionDepartmentDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder transactionDepartmentDescription(
        @Nullable String transactionDepartmentDescription) {
      this.transactionDepartmentDescription = transactionDepartmentDescription;
      return this;
    }

    /**
     * Sets {@code transactionDistrictCode} (E319, {@code cDisProv}).
     *
     * @param transactionDistrictCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder transactionDistrictCode(@Nullable String transactionDistrictCode) {
      this.transactionDistrictCode = transactionDistrictCode;
      return this;
    }

    /**
     * Sets {@code transactionDistrictDescription} (E320, {@code dDesDisProv}).
     *
     * @param transactionDistrictDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder transactionDistrictDescription(@Nullable String transactionDistrictDescription) {
      this.transactionDistrictDescription = transactionDistrictDescription;
      return this;
    }

    /**
     * Sets {@code transactionCityCode} (E321, {@code cCiuProv}).
     *
     * @param transactionCityCode the value
     * @return this builder
     */
    public Builder transactionCityCode(String transactionCityCode) {
      this.transactionCityCode = transactionCityCode;
      return this;
    }

    /**
     * Sets {@code transactionCityDescription} (E322, {@code dDesCiuProv}).
     *
     * @param transactionCityDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder transactionCityDescription(@Nullable String transactionCityDescription) {
      this.transactionCityDescription = transactionCityDescription;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public SelfInvoice build() {
      return new SelfInvoice(this);
    }
  }
}
