/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Campos que identifican al transportista.
 *
 * <p>SIFEN group {@code E980} ({@code gCamTrans}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "E980", xml = "gCamTrans")
public final class Carrier {

  private final String natureCode;
  private final String name;
  private final @Nullable String ruc;
  private final @Nullable String checkDigit;
  private final @Nullable String idDocumentTypeCode;
  private final @Nullable String idDocumentTypeDescription;
  private final @Nullable String idDocumentNumber;
  private final @Nullable String nationalityCode;
  private final @Nullable String nationalityDescription;
  private final String driverIdDocumentNumber;
  private final String driverName;
  private final String taxAddress;
  private final String driverAddress;
  private final @Nullable String agentName;
  private final @Nullable String agentRuc;
  private final @Nullable String agentCheckDigit;
  private final @Nullable String agentAddress;

  private Carrier(Builder b) {
    this.natureCode = Checks.required(b.natureCode, "natureCode", "E981", "iNatTrans");
    this.name = Checks.required(b.name, "name", "E982", "dNomTrans");
    this.ruc = b.ruc;
    this.checkDigit = b.checkDigit;
    this.idDocumentTypeCode = b.idDocumentTypeCode;
    this.idDocumentTypeDescription = b.idDocumentTypeDescription;
    this.idDocumentNumber = b.idDocumentNumber;
    this.nationalityCode = b.nationalityCode;
    this.nationalityDescription = b.nationalityDescription;
    this.driverIdDocumentNumber =
        Checks.required(b.driverIdDocumentNumber, "driverIdDocumentNumber", "E990", "dNumIDChof");
    this.driverName = Checks.required(b.driverName, "driverName", "E991", "dNomChof");
    this.taxAddress = Checks.required(b.taxAddress, "taxAddress", "E992", "dDomFisc");
    this.driverAddress = Checks.required(b.driverAddress, "driverAddress", "E993", "dDirChof");
    this.agentName = b.agentName;
    this.agentRuc = b.agentRuc;
    this.agentCheckDigit = b.agentCheckDigit;
    this.agentAddress = b.agentAddress;
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
   * Naturaleza del transportista.
   *
   * <p>SIFEN field {@code E981} ({@code iNatTrans}).
   *
   * @return the value
   */
  @SifenField(id = "E981", xml = "iNatTrans")
  public String natureCode() {
    return natureCode;
  }

  /**
   * Nombre o razón social del transportista.
   *
   * <p>SIFEN field {@code E982} ({@code dNomTrans}).
   *
   * @return the value
   */
  @SifenField(id = "E982", xml = "dNomTrans")
  public String name() {
    return name;
  }

  /**
   * RUC del transportista.
   *
   * <p>SIFEN field {@code E983} ({@code dRucTrans}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E983", xml = "dRucTrans")
  public @Nullable String ruc() {
    return ruc;
  }

  /**
   * Dígito verificador del RUC del transportista.
   *
   * <p>SIFEN field {@code E984} ({@code dDVTrans}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E984", xml = "dDVTrans")
  public @Nullable String checkDigit() {
    return checkDigit;
  }

  /**
   * Tipo de documento de identidad del transportista.
   *
   * <p>SIFEN field {@code E985} ({@code iTipIDTrans}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E985", xml = "iTipIDTrans")
  public @Nullable String idDocumentTypeCode() {
    return idDocumentTypeCode;
  }

  /**
   * Descripción del tipo de documento de identidad del transportista.
   *
   * <p>SIFEN field {@code E986} ({@code dDTipIDTrans}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E985}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E986", xml = "dDTipIDTrans")
  public @Nullable String idDocumentTypeDescription() {
    return idDocumentTypeDescription;
  }

  /**
   * Número de documento de identidad del transportista.
   *
   * <p>SIFEN field {@code E987} ({@code dNumIDTrans}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E987", xml = "dNumIDTrans")
  public @Nullable String idDocumentNumber() {
    return idDocumentNumber;
  }

  /**
   * Nacionalidad del transportista.
   *
   * <p>SIFEN field {@code E988} ({@code cNacTrans}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E988", xml = "cNacTrans")
  public @Nullable String nationalityCode() {
    return nationalityCode;
  }

  /**
   * Descripción de la nacionalidad del transportista.
   *
   * <p>SIFEN field {@code E989} ({@code dDesNacTrans}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E988}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E989", xml = "dDesNacTrans")
  public @Nullable String nationalityDescription() {
    return nationalityDescription;
  }

  /**
   * Número de documento de identidad del chofer.
   *
   * <p>SIFEN field {@code E990} ({@code dNumIDChof}).
   *
   * @return the value
   */
  @SifenField(id = "E990", xml = "dNumIDChof")
  public String driverIdDocumentNumber() {
    return driverIdDocumentNumber;
  }

  /**
   * Nombre y apellido del chofer.
   *
   * <p>SIFEN field {@code E991} ({@code dNomChof}).
   *
   * @return the value
   */
  @SifenField(id = "E991", xml = "dNomChof")
  public String driverName() {
    return driverName;
  }

  /**
   * Domicilio fiscal del transportista.
   *
   * <p>SIFEN field {@code E992} ({@code dDomFisc}).
   *
   * @return the value
   */
  @SifenField(id = "E992", xml = "dDomFisc")
  public String taxAddress() {
    return taxAddress;
  }

  /**
   * Dirección del chofer.
   *
   * <p>SIFEN field {@code E993} ({@code dDirChof}).
   *
   * @return the value
   */
  @SifenField(id = "E993", xml = "dDirChof")
  public String driverAddress() {
    return driverAddress;
  }

  /**
   * Nombre o razón social del agente.
   *
   * <p>SIFEN field {@code E994} ({@code dNombAg}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E994", xml = "dNombAg")
  public @Nullable String agentName() {
    return agentName;
  }

  /**
   * RUC del agente.
   *
   * <p>SIFEN field {@code E995} ({@code dRucAg}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E995", xml = "dRucAg")
  public @Nullable String agentRuc() {
    return agentRuc;
  }

  /**
   * Dígito verificador del RUC del agente.
   *
   * <p>SIFEN field {@code E996} ({@code dDVAg}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E996", xml = "dDVAg")
  public @Nullable String agentCheckDigit() {
    return agentCheckDigit;
  }

  /**
   * Dirección del agente.
   *
   * <p>SIFEN field {@code E997} ({@code dDirAge}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E997", xml = "dDirAge")
  public @Nullable String agentAddress() {
    return agentAddress;
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
    if (!(other instanceof Carrier)) {
      return false;
    }
    Carrier that = (Carrier) other;
    return Objects.equals(natureCode, that.natureCode)
        && Objects.equals(name, that.name)
        && Objects.equals(ruc, that.ruc)
        && Objects.equals(checkDigit, that.checkDigit)
        && Objects.equals(idDocumentTypeCode, that.idDocumentTypeCode)
        && Objects.equals(idDocumentTypeDescription, that.idDocumentTypeDescription)
        && Objects.equals(idDocumentNumber, that.idDocumentNumber)
        && Objects.equals(nationalityCode, that.nationalityCode)
        && Objects.equals(nationalityDescription, that.nationalityDescription)
        && Objects.equals(driverIdDocumentNumber, that.driverIdDocumentNumber)
        && Objects.equals(driverName, that.driverName)
        && Objects.equals(taxAddress, that.taxAddress)
        && Objects.equals(driverAddress, that.driverAddress)
        && Objects.equals(agentName, that.agentName)
        && Objects.equals(agentRuc, that.agentRuc)
        && Objects.equals(agentCheckDigit, that.agentCheckDigit)
        && Objects.equals(agentAddress, that.agentAddress);
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
        name,
        ruc,
        checkDigit,
        idDocumentTypeCode,
        idDocumentTypeDescription,
        idDocumentNumber,
        nationalityCode,
        nationalityDescription,
        driverIdDocumentNumber,
        driverName,
        taxAddress,
        driverAddress,
        agentName,
        agentRuc,
        agentCheckDigit,
        agentAddress);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "Carrier";
  }

  /** Builder for {@link Carrier}. Not thread-safe; {@link #build()} returns an immutable copy. */
  public static final class Builder {

    private @Nullable String natureCode;
    private @Nullable String name;
    private @Nullable String ruc;
    private @Nullable String checkDigit;
    private @Nullable String idDocumentTypeCode;
    private @Nullable String idDocumentTypeDescription;
    private @Nullable String idDocumentNumber;
    private @Nullable String nationalityCode;
    private @Nullable String nationalityDescription;
    private @Nullable String driverIdDocumentNumber;
    private @Nullable String driverName;
    private @Nullable String taxAddress;
    private @Nullable String driverAddress;
    private @Nullable String agentName;
    private @Nullable String agentRuc;
    private @Nullable String agentCheckDigit;
    private @Nullable String agentAddress;

    private Builder() {}

    private Builder(Carrier source) {
      this.natureCode = source.natureCode;
      this.name = source.name;
      this.ruc = source.ruc;
      this.checkDigit = source.checkDigit;
      this.idDocumentTypeCode = source.idDocumentTypeCode;
      this.idDocumentTypeDescription = source.idDocumentTypeDescription;
      this.idDocumentNumber = source.idDocumentNumber;
      this.nationalityCode = source.nationalityCode;
      this.nationalityDescription = source.nationalityDescription;
      this.driverIdDocumentNumber = source.driverIdDocumentNumber;
      this.driverName = source.driverName;
      this.taxAddress = source.taxAddress;
      this.driverAddress = source.driverAddress;
      this.agentName = source.agentName;
      this.agentRuc = source.agentRuc;
      this.agentCheckDigit = source.agentCheckDigit;
      this.agentAddress = source.agentAddress;
    }

    /**
     * Sets {@code natureCode} (E981, {@code iNatTrans}).
     *
     * @param natureCode the value
     * @return this builder
     */
    public Builder natureCode(String natureCode) {
      this.natureCode = natureCode;
      return this;
    }

    /**
     * Sets {@code name} (E982, {@code dNomTrans}).
     *
     * @param name the value
     * @return this builder
     */
    public Builder name(String name) {
      this.name = name;
      return this;
    }

    /**
     * Sets {@code ruc} (E983, {@code dRucTrans}).
     *
     * @param ruc the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder ruc(@Nullable String ruc) {
      this.ruc = ruc;
      return this;
    }

    /**
     * Sets {@code checkDigit} (E984, {@code dDVTrans}).
     *
     * @param checkDigit the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder checkDigit(@Nullable String checkDigit) {
      this.checkDigit = checkDigit;
      return this;
    }

    /**
     * Sets {@code idDocumentTypeCode} (E985, {@code iTipIDTrans}).
     *
     * @param idDocumentTypeCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder idDocumentTypeCode(@Nullable String idDocumentTypeCode) {
      this.idDocumentTypeCode = idDocumentTypeCode;
      return this;
    }

    /**
     * Sets {@code idDocumentTypeDescription} (E986, {@code dDTipIDTrans}).
     *
     * @param idDocumentTypeDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder idDocumentTypeDescription(@Nullable String idDocumentTypeDescription) {
      this.idDocumentTypeDescription = idDocumentTypeDescription;
      return this;
    }

    /**
     * Sets {@code idDocumentNumber} (E987, {@code dNumIDTrans}).
     *
     * @param idDocumentNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder idDocumentNumber(@Nullable String idDocumentNumber) {
      this.idDocumentNumber = idDocumentNumber;
      return this;
    }

    /**
     * Sets {@code nationalityCode} (E988, {@code cNacTrans}).
     *
     * @param nationalityCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder nationalityCode(@Nullable String nationalityCode) {
      this.nationalityCode = nationalityCode;
      return this;
    }

    /**
     * Sets {@code nationalityDescription} (E989, {@code dDesNacTrans}).
     *
     * @param nationalityDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder nationalityDescription(@Nullable String nationalityDescription) {
      this.nationalityDescription = nationalityDescription;
      return this;
    }

    /**
     * Sets {@code driverIdDocumentNumber} (E990, {@code dNumIDChof}).
     *
     * @param driverIdDocumentNumber the value
     * @return this builder
     */
    public Builder driverIdDocumentNumber(String driverIdDocumentNumber) {
      this.driverIdDocumentNumber = driverIdDocumentNumber;
      return this;
    }

    /**
     * Sets {@code driverName} (E991, {@code dNomChof}).
     *
     * @param driverName the value
     * @return this builder
     */
    public Builder driverName(String driverName) {
      this.driverName = driverName;
      return this;
    }

    /**
     * Sets {@code taxAddress} (E992, {@code dDomFisc}).
     *
     * @param taxAddress the value
     * @return this builder
     */
    public Builder taxAddress(String taxAddress) {
      this.taxAddress = taxAddress;
      return this;
    }

    /**
     * Sets {@code driverAddress} (E993, {@code dDirChof}).
     *
     * @param driverAddress the value
     * @return this builder
     */
    public Builder driverAddress(String driverAddress) {
      this.driverAddress = driverAddress;
      return this;
    }

    /**
     * Sets {@code agentName} (E994, {@code dNombAg}).
     *
     * @param agentName the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder agentName(@Nullable String agentName) {
      this.agentName = agentName;
      return this;
    }

    /**
     * Sets {@code agentRuc} (E995, {@code dRucAg}).
     *
     * @param agentRuc the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder agentRuc(@Nullable String agentRuc) {
      this.agentRuc = agentRuc;
      return this;
    }

    /**
     * Sets {@code agentCheckDigit} (E996, {@code dDVAg}).
     *
     * @param agentCheckDigit the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder agentCheckDigit(@Nullable String agentCheckDigit) {
      this.agentCheckDigit = agentCheckDigit;
      return this;
    }

    /**
     * Sets {@code agentAddress} (E997, {@code dDirAge}).
     *
     * @param agentAddress the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder agentAddress(@Nullable String agentAddress) {
      this.agentAddress = agentAddress;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public Carrier build() {
      return new Carrier(this);
    }
  }
}
