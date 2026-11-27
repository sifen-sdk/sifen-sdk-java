/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Campos que identifican el local de la entrega de las mercaderías.
 *
 * <p>SIFEN group {@code E940} ({@code gCamEnt}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "E940", xml = "gCamEnt")
public final class TransportDelivery {

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
  private final @Nullable String phone;

  private TransportDelivery(Builder b) {
    this.address = Checks.required(b.address, "address", "E941", "dDirLocEnt");
    this.houseNumber = Checks.required(b.houseNumber, "houseNumber", "E942", "dNumCasEnt");
    this.addressComplement1 = b.addressComplement1;
    this.addressComplement2 = b.addressComplement2;
    this.departmentCode = Checks.required(b.departmentCode, "departmentCode", "E945", "cDepEnt");
    this.departmentDescription = b.departmentDescription;
    this.districtCode = b.districtCode;
    this.districtDescription = b.districtDescription;
    this.cityCode = Checks.required(b.cityCode, "cityCode", "E949", "cCiuEnt");
    this.cityDescription = b.cityDescription;
    this.phone = b.phone;
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
   * Dirección del local de la entrega.
   *
   * <p>SIFEN field {@code E941} ({@code dDirLocEnt}).
   *
   * @return the value
   */
  @SifenField(id = "E941", xml = "dDirLocEnt")
  public String address() {
    return address;
  }

  /**
   * Número de casa de la entrega.
   *
   * <p>SIFEN field {@code E942} ({@code dNumCasEnt}).
   *
   * @return the value
   */
  @SifenField(id = "E942", xml = "dNumCasEnt")
  public String houseNumber() {
    return houseNumber;
  }

  /**
   * Complemento de dirección 1 entrega.
   *
   * <p>SIFEN field {@code E943} ({@code dComp1Ent}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E943", xml = "dComp1Ent")
  public @Nullable String addressComplement1() {
    return addressComplement1;
  }

  /**
   * Complemento de dirección 2 entrega.
   *
   * <p>SIFEN field {@code E944} ({@code dComp2Ent}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E944", xml = "dComp2Ent")
  public @Nullable String addressComplement2() {
    return addressComplement2;
  }

  /**
   * Código del departamento del local de la entrega.
   *
   * <p>SIFEN field {@code E945} ({@code cDepEnt}).
   *
   * @return the value
   */
  @SifenField(id = "E945", xml = "cDepEnt")
  public String departmentCode() {
    return departmentCode;
  }

  /**
   * Descripción del departamento del local de la entrega.
   *
   * <p>SIFEN field {@code E946} ({@code dDesDepEnt}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E945}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E946", xml = "dDesDepEnt")
  public @Nullable String departmentDescription() {
    return departmentDescription;
  }

  /**
   * Código del distrito del local de la entrega.
   *
   * <p>SIFEN field {@code E947} ({@code cDisEnt}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E947", xml = "cDisEnt")
  public @Nullable String districtCode() {
    return districtCode;
  }

  /**
   * Descripción de distrito del local de la entrega.
   *
   * <p>SIFEN field {@code E948} ({@code dDesDisEnt}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E947}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E948", xml = "dDesDisEnt")
  public @Nullable String districtDescription() {
    return districtDescription;
  }

  /**
   * Código de la ciudad del local de la entrega.
   *
   * <p>SIFEN field {@code E949} ({@code cCiuEnt}).
   *
   * @return the value
   */
  @SifenField(id = "E949", xml = "cCiuEnt")
  public String cityCode() {
    return cityCode;
  }

  /**
   * Descripción de ciudad del local de la entrega.
   *
   * <p>SIFEN field {@code E950} ({@code dDesCiuEnt}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E949}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E950", xml = "dDesCiuEnt")
  public @Nullable String cityDescription() {
    return cityDescription;
  }

  /**
   * Teléfono de contacto del local de la entrega.
   *
   * <p>SIFEN field {@code E951} ({@code dTelEnt}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E951", xml = "dTelEnt")
  public @Nullable String phone() {
    return phone;
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
    if (!(other instanceof TransportDelivery)) {
      return false;
    }
    TransportDelivery that = (TransportDelivery) other;
    return Objects.equals(address, that.address)
        && Objects.equals(houseNumber, that.houseNumber)
        && Objects.equals(addressComplement1, that.addressComplement1)
        && Objects.equals(addressComplement2, that.addressComplement2)
        && Objects.equals(departmentCode, that.departmentCode)
        && Objects.equals(departmentDescription, that.departmentDescription)
        && Objects.equals(districtCode, that.districtCode)
        && Objects.equals(districtDescription, that.districtDescription)
        && Objects.equals(cityCode, that.cityCode)
        && Objects.equals(cityDescription, that.cityDescription)
        && Objects.equals(phone, that.phone);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
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
        phone);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "TransportDelivery";
  }

  /**
   * Builder for {@link TransportDelivery}. Not thread-safe; {@link #build()} returns an immutable
   * copy.
   */
  public static final class Builder {

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

    private Builder() {}

    private Builder(TransportDelivery source) {
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
    }

    /**
     * Sets {@code address} (E941, {@code dDirLocEnt}).
     *
     * @param address the value
     * @return this builder
     */
    public Builder address(String address) {
      this.address = address;
      return this;
    }

    /**
     * Sets {@code houseNumber} (E942, {@code dNumCasEnt}).
     *
     * @param houseNumber the value
     * @return this builder
     */
    public Builder houseNumber(String houseNumber) {
      this.houseNumber = houseNumber;
      return this;
    }

    /**
     * Sets {@code addressComplement1} (E943, {@code dComp1Ent}).
     *
     * @param addressComplement1 the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder addressComplement1(@Nullable String addressComplement1) {
      this.addressComplement1 = addressComplement1;
      return this;
    }

    /**
     * Sets {@code addressComplement2} (E944, {@code dComp2Ent}).
     *
     * @param addressComplement2 the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder addressComplement2(@Nullable String addressComplement2) {
      this.addressComplement2 = addressComplement2;
      return this;
    }

    /**
     * Sets {@code departmentCode} (E945, {@code cDepEnt}).
     *
     * @param departmentCode the value
     * @return this builder
     */
    public Builder departmentCode(String departmentCode) {
      this.departmentCode = departmentCode;
      return this;
    }

    /**
     * Sets {@code departmentDescription} (E946, {@code dDesDepEnt}).
     *
     * @param departmentDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder departmentDescription(@Nullable String departmentDescription) {
      this.departmentDescription = departmentDescription;
      return this;
    }

    /**
     * Sets {@code districtCode} (E947, {@code cDisEnt}).
     *
     * @param districtCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder districtCode(@Nullable String districtCode) {
      this.districtCode = districtCode;
      return this;
    }

    /**
     * Sets {@code districtDescription} (E948, {@code dDesDisEnt}).
     *
     * @param districtDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder districtDescription(@Nullable String districtDescription) {
      this.districtDescription = districtDescription;
      return this;
    }

    /**
     * Sets {@code cityCode} (E949, {@code cCiuEnt}).
     *
     * @param cityCode the value
     * @return this builder
     */
    public Builder cityCode(String cityCode) {
      this.cityCode = cityCode;
      return this;
    }

    /**
     * Sets {@code cityDescription} (E950, {@code dDesCiuEnt}).
     *
     * @param cityDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder cityDescription(@Nullable String cityDescription) {
      this.cityDescription = cityDescription;
      return this;
    }

    /**
     * Sets {@code phone} (E951, {@code dTelEnt}).
     *
     * @param phone the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder phone(@Nullable String phone) {
      this.phone = phone;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public TransportDelivery build() {
      return new TransportDelivery(this);
    }
  }
}
