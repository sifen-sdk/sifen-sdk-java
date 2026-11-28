/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import io.github.sifensdk.core.catalog.TaxObligation;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Grupo de campos que identifican las obligaciones afectadas.
 *
 * <p>SIFEN group {@code D030} ({@code gOblAfe}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "D030", xml = "gOblAfe")
public final class AffectedObligation {

  private final String code;
  private final @Nullable String description;

  private AffectedObligation(Builder b) {
    this.code = Checks.required(b.code, "code", "D031", "cOblAfe");
    this.description = b.description;
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
   * Código de la obligación afectada.
   *
   * <p>SIFEN field {@code D031} ({@code cOblAfe}).
   *
   * @return the value
   */
  @SifenField(id = "D031", xml = "cOblAfe")
  public String code() {
    return code;
  }

  /**
   * Returns {@link #code()} (D031) as {@link TaxObligation}.
   *
   * @return the constant, or empty if the code is not set or this SDK version does not know it
   */
  public Optional<TaxObligation> taxObligation() {
    return TaxObligation.fromCode(code);
  }

  /**
   * Descripción de la obligación afectada.
   *
   * <p>SIFEN field {@code D032} ({@code dDesOblAfe}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code D031}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D032", xml = "dDesOblAfe")
  public @Nullable String description() {
    return description;
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
    if (!(other instanceof AffectedObligation)) {
      return false;
    }
    AffectedObligation that = (AffectedObligation) other;
    return Objects.equals(code, that.code) && Objects.equals(description, that.description);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(code, description);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "AffectedObligation";
  }

  /**
   * Builder for {@link AffectedObligation}. Not thread-safe; {@link #build()} returns an immutable
   * copy.
   */
  public static final class Builder {

    private @Nullable String code;
    private @Nullable String description;

    private Builder() {}

    private Builder(AffectedObligation source) {
      this.code = source.code;
      this.description = source.description;
    }

    /**
     * Sets {@code code} (D031, {@code cOblAfe}).
     *
     * @param code the value
     * @return this builder
     */
    public Builder code(String code) {
      this.code = code;
      return this;
    }

    /**
     * Sets {@code code} (D031, {@code cOblAfe}) from {@link TaxObligation}.
     *
     * @param taxObligation the catalog constant
     * @return this builder
     */
    public Builder taxObligation(TaxObligation taxObligation) {
      this.code = taxObligation.code();
      return this;
    }

    /**
     * Sets {@code description} (D032, {@code dDesOblAfe}).
     *
     * @param description the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder description(@Nullable String description) {
      this.description = description;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public AffectedObligation build() {
      return new AffectedObligation(this);
    }
  }
}
