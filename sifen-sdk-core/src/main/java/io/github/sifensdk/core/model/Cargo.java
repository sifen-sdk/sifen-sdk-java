/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.math.BigDecimal;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Campos generales de la carga.
 *
 * <p>SIFEN group {@code G050} ({@code gCamCarg}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "G050", xml = "gCamCarg")
public final class Cargo {

  private final @Nullable String volumeUnitCode;
  private final @Nullable String volumeUnitDescription;
  private final @Nullable BigDecimal totalVolume;
  private final @Nullable String weightUnitCode;
  private final @Nullable String weightUnitDescription;
  private final @Nullable BigDecimal totalWeight;
  private final @Nullable String characteristicCode;
  private final @Nullable String characteristicDescription;

  private Cargo(Builder b) {
    this.volumeUnitCode = b.volumeUnitCode;
    this.volumeUnitDescription = b.volumeUnitDescription;
    this.totalVolume = b.totalVolume;
    this.weightUnitCode = b.weightUnitCode;
    this.weightUnitDescription = b.weightUnitDescription;
    this.totalWeight = b.totalWeight;
    this.characteristicCode = b.characteristicCode;
    this.characteristicDescription = b.characteristicDescription;
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
   * Unidad de medida del total de volumen de la mercadería.
   *
   * <p>SIFEN field {@code G051} ({@code cUniMedTotVol}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "G051", xml = "cUniMedTotVol")
  public @Nullable String volumeUnitCode() {
    return volumeUnitCode;
  }

  /**
   * Descripción de la unidad de medida del total de volumen de la mercadería.
   *
   * <p>SIFEN field {@code G052} ({@code dDesUniMedTotVol}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code G051}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "G052", xml = "dDesUniMedTotVol")
  public @Nullable String volumeUnitDescription() {
    return volumeUnitDescription;
  }

  /**
   * Total volumen de la mercadería.
   *
   * <p>SIFEN field {@code G053} ({@code dTotVolMerc}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "G053", xml = "dTotVolMerc")
  public @Nullable BigDecimal totalVolume() {
    return totalVolume;
  }

  /**
   * Unidad de medida del peso total de la mercadería.
   *
   * <p>SIFEN field {@code G054} ({@code cUniMedTotPes}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "G054", xml = "cUniMedTotPes")
  public @Nullable String weightUnitCode() {
    return weightUnitCode;
  }

  /**
   * Descripción de la unidad de medida del peso total de la mercadería.
   *
   * <p>SIFEN field {@code G055} ({@code dDesUniMedTotPes}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code G054}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "G055", xml = "dDesUniMedTotPes")
  public @Nullable String weightUnitDescription() {
    return weightUnitDescription;
  }

  /**
   * Total peso de la mercadería.
   *
   * <p>SIFEN field {@code G056} ({@code dTotPesMerc}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "G056", xml = "dTotPesMerc")
  public @Nullable BigDecimal totalWeight() {
    return totalWeight;
  }

  /**
   * Características de la Carga.
   *
   * <p>SIFEN field {@code G057} ({@code iCarCarga}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "G057", xml = "iCarCarga")
  public @Nullable String characteristicCode() {
    return characteristicCode;
  }

  /**
   * Descripción de las características de la carga.
   *
   * <p>SIFEN field {@code G058} ({@code dDesCarCarga}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code G057}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "G058", xml = "dDesCarCarga")
  public @Nullable String characteristicDescription() {
    return characteristicDescription;
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
    if (!(other instanceof Cargo)) {
      return false;
    }
    Cargo that = (Cargo) other;
    return Objects.equals(volumeUnitCode, that.volumeUnitCode)
        && Objects.equals(volumeUnitDescription, that.volumeUnitDescription)
        && Objects.equals(totalVolume, that.totalVolume)
        && Objects.equals(weightUnitCode, that.weightUnitCode)
        && Objects.equals(weightUnitDescription, that.weightUnitDescription)
        && Objects.equals(totalWeight, that.totalWeight)
        && Objects.equals(characteristicCode, that.characteristicCode)
        && Objects.equals(characteristicDescription, that.characteristicDescription);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        volumeUnitCode,
        volumeUnitDescription,
        totalVolume,
        weightUnitCode,
        weightUnitDescription,
        totalWeight,
        characteristicCode,
        characteristicDescription);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "Cargo";
  }

  /** Builder for {@link Cargo}. Not thread-safe; {@link #build()} returns an immutable copy. */
  public static final class Builder {

    private @Nullable String volumeUnitCode;
    private @Nullable String volumeUnitDescription;
    private @Nullable BigDecimal totalVolume;
    private @Nullable String weightUnitCode;
    private @Nullable String weightUnitDescription;
    private @Nullable BigDecimal totalWeight;
    private @Nullable String characteristicCode;
    private @Nullable String characteristicDescription;

    private Builder() {}

    private Builder(Cargo source) {
      this.volumeUnitCode = source.volumeUnitCode;
      this.volumeUnitDescription = source.volumeUnitDescription;
      this.totalVolume = source.totalVolume;
      this.weightUnitCode = source.weightUnitCode;
      this.weightUnitDescription = source.weightUnitDescription;
      this.totalWeight = source.totalWeight;
      this.characteristicCode = source.characteristicCode;
      this.characteristicDescription = source.characteristicDescription;
    }

    /**
     * Sets {@code volumeUnitCode} (G051, {@code cUniMedTotVol}).
     *
     * @param volumeUnitCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder volumeUnitCode(@Nullable String volumeUnitCode) {
      this.volumeUnitCode = volumeUnitCode;
      return this;
    }

    /**
     * Sets {@code volumeUnitDescription} (G052, {@code dDesUniMedTotVol}).
     *
     * @param volumeUnitDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder volumeUnitDescription(@Nullable String volumeUnitDescription) {
      this.volumeUnitDescription = volumeUnitDescription;
      return this;
    }

    /**
     * Sets {@code totalVolume} (G053, {@code dTotVolMerc}).
     *
     * @param totalVolume the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder totalVolume(@Nullable BigDecimal totalVolume) {
      this.totalVolume = totalVolume;
      return this;
    }

    /**
     * Sets {@code weightUnitCode} (G054, {@code cUniMedTotPes}).
     *
     * @param weightUnitCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder weightUnitCode(@Nullable String weightUnitCode) {
      this.weightUnitCode = weightUnitCode;
      return this;
    }

    /**
     * Sets {@code weightUnitDescription} (G055, {@code dDesUniMedTotPes}).
     *
     * @param weightUnitDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder weightUnitDescription(@Nullable String weightUnitDescription) {
      this.weightUnitDescription = weightUnitDescription;
      return this;
    }

    /**
     * Sets {@code totalWeight} (G056, {@code dTotPesMerc}).
     *
     * @param totalWeight the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder totalWeight(@Nullable BigDecimal totalWeight) {
      this.totalWeight = totalWeight;
      return this;
    }

    /**
     * Sets {@code characteristicCode} (G057, {@code iCarCarga}).
     *
     * @param characteristicCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder characteristicCode(@Nullable String characteristicCode) {
      this.characteristicCode = characteristicCode;
      return this;
    }

    /**
     * Sets {@code characteristicDescription} (G058, {@code dDesCarCarga}).
     *
     * @param characteristicDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder characteristicDescription(@Nullable String characteristicDescription) {
      this.characteristicDescription = characteristicDescription;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     */
    public Cargo build() {
      return new Cargo(this);
    }
  }
}
