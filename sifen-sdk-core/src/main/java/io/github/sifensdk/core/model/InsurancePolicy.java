/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Grupo de póliza de seguros.
 *
 * <p>SIFEN group {@code EA790} ({@code gGrupPolSeg}). Immutable and thread-safe; create it with
 * {@link #builder()}. The builder only checks what Java needs (required elements and non-null
 * lists); the SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step,
 * never here. Amounts are kept exactly as given.
 */
@SifenField(id = "EA790", xml = "gGrupPolSeg")
public final class InsurancePolicy {

  private final String code;
  private final String validityUnitDescription;
  private final BigDecimal validityPeriod;
  private final String number;
  private final @Nullable LocalDateTime validityStartDateTime;
  private final @Nullable LocalDateTime validityEndDateTime;
  private final @Nullable String itemInternalCode;

  private InsurancePolicy(Builder b) {
    this.code = Checks.required(b.code, "code", "EA791", "dPoliza");
    this.validityUnitDescription =
        Checks.required(b.validityUnitDescription, "validityUnitDescription", "EA792", "dUnidVig");
    this.validityPeriod = Checks.required(b.validityPeriod, "validityPeriod", "EA793", "dVigencia");
    this.number = Checks.required(b.number, "number", "EA794", "dNumPoliza");
    this.validityStartDateTime = b.validityStartDateTime;
    this.validityEndDateTime = b.validityEndDateTime;
    this.itemInternalCode = b.itemInternalCode;
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
   * Código de la póliza.
   *
   * <p>SIFEN field {@code EA791} ({@code dPoliza}).
   *
   * @return the value
   */
  @SifenField(id = "EA791", xml = "dPoliza")
  public String code() {
    return code;
  }

  /**
   * Descripción de la unidad de tiempo de vigencia (for example hora, día, mes, año).
   *
   * <p>SIFEN field {@code EA792} ({@code dUnidVig}).
   *
   * @return the value
   */
  @SifenField(id = "EA792", xml = "dUnidVig")
  public String validityUnitDescription() {
    return validityUnitDescription;
  }

  /**
   * Vigencia de la póliza.
   *
   * <p>SIFEN field {@code EA793} ({@code dVigencia}).
   *
   * @return the value
   */
  @SifenField(id = "EA793", xml = "dVigencia")
  public BigDecimal validityPeriod() {
    return validityPeriod;
  }

  /**
   * Número de la póliza.
   *
   * <p>SIFEN field {@code EA794} ({@code dNumPoliza}).
   *
   * @return the value
   */
  @SifenField(id = "EA794", xml = "dNumPoliza")
  public String number() {
    return number;
  }

  /**
   * Fecha de inicio de vigencia.
   *
   * <p>SIFEN field {@code EA795} ({@code dFecIniVig}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "EA795", xml = "dFecIniVig")
  public @Nullable LocalDateTime validityStartDateTime() {
    return validityStartDateTime;
  }

  /**
   * Fecha de fin de vigencia.
   *
   * <p>SIFEN field {@code EA796} ({@code dFecFinVig}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "EA796", xml = "dFecFinVig")
  public @Nullable LocalDateTime validityEndDateTime() {
    return validityEndDateTime;
  }

  /**
   * Código interno del ítem (E701) the policy refers to, if any.
   *
   * <p>SIFEN field {@code EA797} ({@code dCodInt}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "EA797", xml = "dCodInt")
  public @Nullable String itemInternalCode() {
    return itemInternalCode;
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
    if (!(other instanceof InsurancePolicy)) {
      return false;
    }
    InsurancePolicy that = (InsurancePolicy) other;
    return Objects.equals(code, that.code)
        && Objects.equals(validityUnitDescription, that.validityUnitDescription)
        && Objects.equals(validityPeriod, that.validityPeriod)
        && Objects.equals(number, that.number)
        && Objects.equals(validityStartDateTime, that.validityStartDateTime)
        && Objects.equals(validityEndDateTime, that.validityEndDateTime)
        && Objects.equals(itemInternalCode, that.itemInternalCode);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        code,
        validityUnitDescription,
        validityPeriod,
        number,
        validityStartDateTime,
        validityEndDateTime,
        itemInternalCode);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "InsurancePolicy";
  }

  /**
   * Builder for {@link InsurancePolicy}. Not thread-safe; {@link #build()} returns an immutable
   * copy.
   */
  public static final class Builder {

    private @Nullable String code;
    private @Nullable String validityUnitDescription;
    private @Nullable BigDecimal validityPeriod;
    private @Nullable String number;
    private @Nullable LocalDateTime validityStartDateTime;
    private @Nullable LocalDateTime validityEndDateTime;
    private @Nullable String itemInternalCode;

    private Builder() {}

    private Builder(InsurancePolicy source) {
      this.code = source.code;
      this.validityUnitDescription = source.validityUnitDescription;
      this.validityPeriod = source.validityPeriod;
      this.number = source.number;
      this.validityStartDateTime = source.validityStartDateTime;
      this.validityEndDateTime = source.validityEndDateTime;
      this.itemInternalCode = source.itemInternalCode;
    }

    /**
     * Sets {@code code} (EA791, {@code dPoliza}).
     *
     * @param code the value
     * @return this builder
     */
    public Builder code(String code) {
      this.code = code;
      return this;
    }

    /**
     * Sets {@code validityUnitDescription} (EA792, {@code dUnidVig}).
     *
     * @param validityUnitDescription the value
     * @return this builder
     */
    public Builder validityUnitDescription(String validityUnitDescription) {
      this.validityUnitDescription = validityUnitDescription;
      return this;
    }

    /**
     * Sets {@code validityPeriod} (EA793, {@code dVigencia}).
     *
     * @param validityPeriod the value
     * @return this builder
     */
    public Builder validityPeriod(BigDecimal validityPeriod) {
      this.validityPeriod = validityPeriod;
      return this;
    }

    /**
     * Sets {@code number} (EA794, {@code dNumPoliza}).
     *
     * @param number the value
     * @return this builder
     */
    public Builder number(String number) {
      this.number = number;
      return this;
    }

    /**
     * Sets {@code validityStartDateTime} (EA795, {@code dFecIniVig}).
     *
     * @param validityStartDateTime the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder validityStartDateTime(@Nullable LocalDateTime validityStartDateTime) {
      this.validityStartDateTime = validityStartDateTime;
      return this;
    }

    /**
     * Sets {@code validityEndDateTime} (EA796, {@code dFecFinVig}).
     *
     * @param validityEndDateTime the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder validityEndDateTime(@Nullable LocalDateTime validityEndDateTime) {
      this.validityEndDateTime = validityEndDateTime;
      return this;
    }

    /**
     * Sets {@code itemInternalCode} (EA797, {@code dCodInt}).
     *
     * @param itemInternalCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder itemInternalCode(@Nullable String itemInternalCode) {
      this.itemInternalCode = itemInternalCode;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     */
    public InsurancePolicy build() {
      return new InsurancePolicy(this);
    }
  }
}
