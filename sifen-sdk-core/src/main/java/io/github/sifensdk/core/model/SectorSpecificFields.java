/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Campos complementarios comerciales de uso específico.
 *
 * <p>SIFEN group {@code E790} ({@code gCamEsp}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "E790", xml = "gCamEsp")
public final class SectorSpecificFields {

  private final List<Electricity> electricity;
  private final @Nullable Insurance insurance;
  private final @Nullable Supermarket supermarket;
  private final @Nullable AdditionalCommercialData additionalCommercialData;

  private SectorSpecificFields(Builder b) {
    this.electricity = List.copyOf(b.electricity);
    this.insurance = b.insurance;
    this.supermarket = b.supermarket;
    this.additionalCommercialData = b.additionalCommercialData;
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
   * Grupo del sector de energía eléctrica. Up to nine (NT 23; the MT150 said one).
   *
   * <p>SIFEN group {@code E791} ({@code gGrupEner}).
   *
   * @return an unmodifiable list, possibly empty
   */
  @SifenField(id = "E791", xml = "gGrupEner")
  public List<Electricity> electricity() {
    return electricity;
  }

  /**
   * Grupo del sector de seguros.
   *
   * <p>SIFEN group {@code E800} ({@code gGrupSeg}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E800", xml = "gGrupSeg")
  public @Nullable Insurance insurance() {
    return insurance;
  }

  /**
   * Grupo del sector supermercados.
   *
   * <p>SIFEN group {@code E810} ({@code gGrupSup}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E810", xml = "gGrupSup")
  public @Nullable Supermarket supermarket() {
    return supermarket;
  }

  /**
   * Grupo de datos adicionales de uso comercial.
   *
   * <p>SIFEN group {@code E820} ({@code gGrupAdi}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E820", xml = "gGrupAdi")
  public @Nullable AdditionalCommercialData additionalCommercialData() {
    return additionalCommercialData;
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
    if (!(other instanceof SectorSpecificFields)) {
      return false;
    }
    SectorSpecificFields that = (SectorSpecificFields) other;
    return Objects.equals(electricity, that.electricity)
        && Objects.equals(insurance, that.insurance)
        && Objects.equals(supermarket, that.supermarket)
        && Objects.equals(additionalCommercialData, that.additionalCommercialData);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(electricity, insurance, supermarket, additionalCommercialData);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "SectorSpecificFields[electricity=" + electricity.size() + "]";
  }

  /**
   * Builder for {@link SectorSpecificFields}. Not thread-safe; {@link #build()} returns an
   * immutable copy.
   */
  public static final class Builder {

    private List<Electricity> electricity = new ArrayList<>();
    private @Nullable Insurance insurance;
    private @Nullable Supermarket supermarket;
    private @Nullable AdditionalCommercialData additionalCommercialData;

    private Builder() {}

    private Builder(SectorSpecificFields source) {
      this.electricity = new ArrayList<>(source.electricity);
      this.insurance = source.insurance;
      this.supermarket = source.supermarket;
      this.additionalCommercialData = source.additionalCommercialData;
    }

    /**
     * Replaces the list of {@code electricity} (E791, {@code gGrupEner}).
     *
     * @param electricity the elements, in document order
     * @return this builder
     */
    public Builder electricity(List<Electricity> electricity) {
      this.electricity = new ArrayList<>(electricity);
      return this;
    }

    /**
     * Appends one element to {@code electricity} (E791, {@code gGrupEner}).
     *
     * @param electricity the element
     * @return this builder
     */
    public Builder addElectricity(Electricity electricity) {
      this.electricity.add(Objects.requireNonNull(electricity, "electricity"));
      return this;
    }

    /**
     * Sets {@code insurance} (E800, {@code gGrupSeg}).
     *
     * @param insurance the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder insurance(@Nullable Insurance insurance) {
      this.insurance = insurance;
      return this;
    }

    /**
     * Sets {@code supermarket} (E810, {@code gGrupSup}).
     *
     * @param supermarket the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder supermarket(@Nullable Supermarket supermarket) {
      this.supermarket = supermarket;
      return this;
    }

    /**
     * Sets {@code additionalCommercialData} (E820, {@code gGrupAdi}).
     *
     * @param additionalCommercialData the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder additionalCommercialData(
        @Nullable AdditionalCommercialData additionalCommercialData) {
      this.additionalCommercialData = additionalCommercialData;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     */
    public SectorSpecificFields build() {
      return new SectorSpecificFields(this);
    }
  }
}
