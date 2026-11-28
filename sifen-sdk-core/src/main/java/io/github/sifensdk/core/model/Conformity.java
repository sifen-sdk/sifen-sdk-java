/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import io.github.sifensdk.core.catalog.ConformityType;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Raiz Gestión de Eventos Conformidad.
 *
 * <p>Detail of the event of conformidad con un DE (receptor): SIFEN group {@code GCO001} ({@code
 * rGeVeConf}). Immutable and thread-safe; create it with {@link #builder()}. The builder only
 * checks what Java needs (the elements the XSD always requires); the SIFEN rules are verified by
 * the validation step.
 */
@SifenField(id = "GCO001", xml = "rGeVeConf")
public final class Conformity implements EventDetail {

  private final String cdc;
  private final String typeCode;
  private final @Nullable LocalDateTime estimatedReceptionDateTime;

  private Conformity(Builder b) {
    this.cdc = Checks.required(b.cdc, "cdc", "GCO002", "Id");
    this.typeCode = Checks.required(b.typeCode, "typeCode", "GCO003", "iTipConf");
    this.estimatedReceptionDateTime = b.estimatedReceptionDateTime;
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
   * CDC del DTE.
   *
   * <p>SIFEN field {@code GCO002} ({@code Id}).
   *
   * @return the value
   */
  @SifenField(id = "GCO002", xml = "Id")
  public String cdc() {
    return cdc;
  }

  /**
   * Tipo de Conformidad.
   *
   * <p>SIFEN field {@code GCO003} ({@code iTipConf}).
   *
   * @return the value
   */
  @SifenField(id = "GCO003", xml = "iTipConf")
  public String typeCode() {
    return typeCode;
  }

  /**
   * Returns {@link #typeCode()} (GCO003) as {@link ConformityType}.
   *
   * @return the constant, or empty if the code is not set or this SDK version does not know it
   */
  public Optional<ConformityType> type() {
    return ConformityType.fromCode(typeCode);
  }

  /**
   * Fecha Estimada de Recepción.
   *
   * <p>SIFEN field {@code GCO004} ({@code dFecRecep}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GCO004", xml = "dFecRecep")
  public @Nullable LocalDateTime estimatedReceptionDateTime() {
    return estimatedReceptionDateTime;
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
    if (!(other instanceof Conformity)) {
      return false;
    }
    Conformity that = (Conformity) other;
    return Objects.equals(cdc, that.cdc)
        && Objects.equals(typeCode, that.typeCode)
        && Objects.equals(estimatedReceptionDateTime, that.estimatedReceptionDateTime);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(cdc, typeCode, estimatedReceptionDateTime);
  }

  /**
   * The type name. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "Conformity";
  }

  /**
   * Builder for {@link Conformity}. Not thread-safe; {@link #build()} returns an immutable copy.
   */
  public static final class Builder {

    private @Nullable String cdc;
    private @Nullable String typeCode;
    private @Nullable LocalDateTime estimatedReceptionDateTime;

    private Builder() {}

    private Builder(Conformity source) {
      this.cdc = source.cdc;
      this.typeCode = source.typeCode;
      this.estimatedReceptionDateTime = source.estimatedReceptionDateTime;
    }

    /**
     * Sets {@code cdc} (GCO002, {@code Id}).
     *
     * @param cdc the value
     * @return this builder
     */
    public Builder cdc(String cdc) {
      this.cdc = cdc;
      return this;
    }

    /**
     * Sets {@code typeCode} (GCO003, {@code iTipConf}).
     *
     * @param typeCode the value
     * @return this builder
     */
    public Builder typeCode(String typeCode) {
      this.typeCode = typeCode;
      return this;
    }

    /**
     * Sets {@code typeCode} (GCO003, {@code iTipConf}) from {@link ConformityType}.
     *
     * @param type the catalog constant
     * @return this builder
     */
    public Builder type(ConformityType type) {
      this.typeCode = type.code();
      return this;
    }

    /**
     * Sets {@code estimatedReceptionDateTime} (GCO004, {@code dFecRecep}).
     *
     * @param estimatedReceptionDateTime the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder estimatedReceptionDateTime(@Nullable LocalDateTime estimatedReceptionDateTime) {
      this.estimatedReceptionDateTime = estimatedReceptionDateTime;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public Conformity build() {
      return new Conformity(this);
    }
  }
}
