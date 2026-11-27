/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.math.BigDecimal;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Grupo del sector de energía eléctrica.
 *
 * <p>SIFEN group {@code E791} ({@code gGrupEner}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "E791", xml = "gGrupEner")
public final class Electricity {

  private final @Nullable String meterNumber;
  private final @Nullable String activityCode;
  private final @Nullable String categoryCode;
  private final @Nullable BigDecimal previousReading;
  private final @Nullable BigDecimal currentReading;
  private final @Nullable BigDecimal consumption;

  private Electricity(Builder b) {
    this.meterNumber = b.meterNumber;
    this.activityCode = b.activityCode;
    this.categoryCode = b.categoryCode;
    this.previousReading = b.previousReading;
    this.currentReading = b.currentReading;
    this.consumption = b.consumption;
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
   * Número de medidor.
   *
   * <p>SIFEN field {@code E792} ({@code dNroMed}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E792", xml = "dNroMed")
  public @Nullable String meterNumber() {
    return meterNumber;
  }

  /**
   * Código de actividad.
   *
   * <p>SIFEN field {@code E793} ({@code dActiv}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E793", xml = "dActiv")
  public @Nullable String activityCode() {
    return activityCode;
  }

  /**
   * Código de categoría.
   *
   * <p>SIFEN field {@code E794} ({@code dCateg}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E794", xml = "dCateg")
  public @Nullable String categoryCode() {
    return categoryCode;
  }

  /**
   * Lectura anterior.
   *
   * <p>SIFEN field {@code E795} ({@code dLecAnt}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E795", xml = "dLecAnt")
  public @Nullable BigDecimal previousReading() {
    return previousReading;
  }

  /**
   * Lectura actual.
   *
   * <p>SIFEN field {@code E796} ({@code dLecAct}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E796", xml = "dLecAct")
  public @Nullable BigDecimal currentReading() {
    return currentReading;
  }

  /**
   * Consumo: the difference between the current reading (E796) and the previous one (E795) (NT 23,
   * rule R-E797-A1); verified, never computed.
   *
   * <p>SIFEN field {@code E797} ({@code dConKwh}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E797", xml = "dConKwh")
  public @Nullable BigDecimal consumption() {
    return consumption;
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
    if (!(other instanceof Electricity)) {
      return false;
    }
    Electricity that = (Electricity) other;
    return Objects.equals(meterNumber, that.meterNumber)
        && Objects.equals(activityCode, that.activityCode)
        && Objects.equals(categoryCode, that.categoryCode)
        && Objects.equals(previousReading, that.previousReading)
        && Objects.equals(currentReading, that.currentReading)
        && Objects.equals(consumption, that.consumption);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        meterNumber, activityCode, categoryCode, previousReading, currentReading, consumption);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "Electricity";
  }

  /**
   * Builder for {@link Electricity}. Not thread-safe; {@link #build()} returns an immutable copy.
   */
  public static final class Builder {

    private @Nullable String meterNumber;
    private @Nullable String activityCode;
    private @Nullable String categoryCode;
    private @Nullable BigDecimal previousReading;
    private @Nullable BigDecimal currentReading;
    private @Nullable BigDecimal consumption;

    private Builder() {}

    private Builder(Electricity source) {
      this.meterNumber = source.meterNumber;
      this.activityCode = source.activityCode;
      this.categoryCode = source.categoryCode;
      this.previousReading = source.previousReading;
      this.currentReading = source.currentReading;
      this.consumption = source.consumption;
    }

    /**
     * Sets {@code meterNumber} (E792, {@code dNroMed}).
     *
     * @param meterNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder meterNumber(@Nullable String meterNumber) {
      this.meterNumber = meterNumber;
      return this;
    }

    /**
     * Sets {@code activityCode} (E793, {@code dActiv}).
     *
     * @param activityCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder activityCode(@Nullable String activityCode) {
      this.activityCode = activityCode;
      return this;
    }

    /**
     * Sets {@code categoryCode} (E794, {@code dCateg}).
     *
     * @param categoryCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder categoryCode(@Nullable String categoryCode) {
      this.categoryCode = categoryCode;
      return this;
    }

    /**
     * Sets {@code previousReading} (E795, {@code dLecAnt}).
     *
     * @param previousReading the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder previousReading(@Nullable BigDecimal previousReading) {
      this.previousReading = previousReading;
      return this;
    }

    /**
     * Sets {@code currentReading} (E796, {@code dLecAct}).
     *
     * @param currentReading the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder currentReading(@Nullable BigDecimal currentReading) {
      this.currentReading = currentReading;
      return this;
    }

    /**
     * Sets {@code consumption} (E797, {@code dConKwh}).
     *
     * @param consumption the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder consumption(@Nullable BigDecimal consumption) {
      this.consumption = consumption;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     */
    public Electricity build() {
      return new Electricity(this);
    }
  }
}
