/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Campos inherentes a la operación de DE.
 *
 * <p>SIFEN group {@code B001} ({@code gOpeDE}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "B001", xml = "gOpeDE")
public final class Operation {

  private final String emissionTypeCode;
  private final @Nullable String emissionTypeDescription;
  private final String securityCode;
  private final @Nullable String issuerInformation;
  private final @Nullable String taxAuthorityInformation;

  private Operation(Builder b) {
    this.emissionTypeCode =
        Checks.required(b.emissionTypeCode, "emissionTypeCode", "B002", "iTipEmi");
    this.emissionTypeDescription = b.emissionTypeDescription;
    this.securityCode = Checks.required(b.securityCode, "securityCode", "B004", "dCodSeg");
    this.issuerInformation = b.issuerInformation;
    this.taxAuthorityInformation = b.taxAuthorityInformation;
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
   * Tipo de emisión.
   *
   * <p>SIFEN field {@code B002} ({@code iTipEmi}).
   *
   * @return the value
   */
  @SifenField(id = "B002", xml = "iTipEmi")
  public String emissionTypeCode() {
    return emissionTypeCode;
  }

  /**
   * Descripción del tipo de emisión.
   *
   * <p>SIFEN field {@code B003} ({@code dDesTipEmi}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code B002}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "B003", xml = "dDesTipEmi")
  public @Nullable String emissionTypeDescription() {
    return emissionTypeDescription;
  }

  /**
   * Código de seguridad.
   *
   * <p>SIFEN field {@code B004} ({@code dCodSeg}).
   *
   * @return the value
   */
  @SifenField(id = "B004", xml = "dCodSeg")
  public String securityCode() {
    return securityCode;
  }

  /**
   * Información de interés del emisor respecto al DE.
   *
   * <p>SIFEN field {@code B005} ({@code dInfoEmi}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "B005", xml = "dInfoEmi")
  public @Nullable String issuerInformation() {
    return issuerInformation;
  }

  /**
   * Información de interés del Fisco respecto al DE.
   *
   * <p>SIFEN field {@code B006} ({@code dInfoFisc}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "B006", xml = "dInfoFisc")
  public @Nullable String taxAuthorityInformation() {
    return taxAuthorityInformation;
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
    if (!(other instanceof Operation)) {
      return false;
    }
    Operation that = (Operation) other;
    return Objects.equals(emissionTypeCode, that.emissionTypeCode)
        && Objects.equals(emissionTypeDescription, that.emissionTypeDescription)
        && Objects.equals(securityCode, that.securityCode)
        && Objects.equals(issuerInformation, that.issuerInformation)
        && Objects.equals(taxAuthorityInformation, that.taxAuthorityInformation);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        emissionTypeCode,
        emissionTypeDescription,
        securityCode,
        issuerInformation,
        taxAuthorityInformation);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "Operation";
  }

  /** Builder for {@link Operation}. Not thread-safe; {@link #build()} returns an immutable copy. */
  public static final class Builder {

    private @Nullable String emissionTypeCode;
    private @Nullable String emissionTypeDescription;
    private @Nullable String securityCode;
    private @Nullable String issuerInformation;
    private @Nullable String taxAuthorityInformation;

    private Builder() {}

    private Builder(Operation source) {
      this.emissionTypeCode = source.emissionTypeCode;
      this.emissionTypeDescription = source.emissionTypeDescription;
      this.securityCode = source.securityCode;
      this.issuerInformation = source.issuerInformation;
      this.taxAuthorityInformation = source.taxAuthorityInformation;
    }

    /**
     * Sets {@code emissionTypeCode} (B002, {@code iTipEmi}).
     *
     * @param emissionTypeCode the value
     * @return this builder
     */
    public Builder emissionTypeCode(String emissionTypeCode) {
      this.emissionTypeCode = emissionTypeCode;
      return this;
    }

    /**
     * Sets {@code emissionTypeDescription} (B003, {@code dDesTipEmi}).
     *
     * @param emissionTypeDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder emissionTypeDescription(@Nullable String emissionTypeDescription) {
      this.emissionTypeDescription = emissionTypeDescription;
      return this;
    }

    /**
     * Sets {@code securityCode} (B004, {@code dCodSeg}).
     *
     * @param securityCode the value
     * @return this builder
     */
    public Builder securityCode(String securityCode) {
      this.securityCode = securityCode;
      return this;
    }

    /**
     * Sets {@code issuerInformation} (B005, {@code dInfoEmi}).
     *
     * @param issuerInformation the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder issuerInformation(@Nullable String issuerInformation) {
      this.issuerInformation = issuerInformation;
      return this;
    }

    /**
     * Sets {@code taxAuthorityInformation} (B006, {@code dInfoFisc}).
     *
     * @param taxAuthorityInformation the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder taxAuthorityInformation(@Nullable String taxAuthorityInformation) {
      this.taxAuthorityInformation = taxAuthorityInformation;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public Operation build() {
      return new Operation(this);
    }
  }
}
