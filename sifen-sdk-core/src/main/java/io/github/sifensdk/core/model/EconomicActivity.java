/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Grupo de campos que describen la actividad económica del emisor.
 *
 * <p>SIFEN group {@code D130} ({@code gActEco}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "D130", xml = "gActEco")
public final class EconomicActivity {

  private final String code;
  private final String description;

  private EconomicActivity(Builder b) {
    this.code = Checks.required(b.code, "code", "D131", "cActEco");
    this.description = Checks.required(b.description, "description", "D132", "dDesActEco");
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
   * Código de la actividad económica del emisor.
   *
   * <p>SIFEN field {@code D131} ({@code cActEco}).
   *
   * @return the value
   */
  @SifenField(id = "D131", xml = "cActEco")
  public String code() {
    return code;
  }

  /**
   * Descripción de la actividad económica del emisor.
   *
   * <p>SIFEN field {@code D132} ({@code dDesActEco}).
   *
   * @return the value
   */
  @SifenField(id = "D132", xml = "dDesActEco")
  public String description() {
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
    if (!(other instanceof EconomicActivity)) {
      return false;
    }
    EconomicActivity that = (EconomicActivity) other;
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
    return "EconomicActivity";
  }

  /**
   * Builder for {@link EconomicActivity}. Not thread-safe; {@link #build()} returns an immutable
   * copy.
   */
  public static final class Builder {

    private @Nullable String code;
    private @Nullable String description;

    private Builder() {}

    private Builder(EconomicActivity source) {
      this.code = source.code;
      this.description = source.description;
    }

    /**
     * Sets {@code code} (D131, {@code cActEco}).
     *
     * @param code the value
     * @return this builder
     */
    public Builder code(String code) {
      this.code = code;
      return this;
    }

    /**
     * Sets {@code description} (D132, {@code dDesActEco}).
     *
     * @param description the value
     * @return this builder
     */
    public Builder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public EconomicActivity build() {
      return new EconomicActivity(this);
    }
  }
}
