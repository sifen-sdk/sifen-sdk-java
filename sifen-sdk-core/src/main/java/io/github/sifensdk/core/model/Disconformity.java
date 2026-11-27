/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Raiz Gestión de Eventos Disconformidad.
 *
 * <p>Detail of the event of disconformidad con un DE (receptor): SIFEN group {@code GDI001} ({@code
 * rGeVeDisconf}). Immutable and thread-safe; create it with {@link #builder()}. The builder only
 * checks what Java needs (the elements the XSD always requires); the SIFEN rules are verified by
 * the validation step.
 */
@SifenField(id = "GDI001", xml = "rGeVeDisconf")
public final class Disconformity implements EventDetail {

  private final String cdc;
  private final String reason;

  private Disconformity(Builder b) {
    this.cdc = Checks.required(b.cdc, "cdc", "GDI002", "Id");
    this.reason = Checks.required(b.reason, "reason", "GDI004", "mOtEve");
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
   * <p>SIFEN field {@code GDI002} ({@code Id}).
   *
   * @return the value
   */
  @SifenField(id = "GDI002", xml = "Id")
  public String cdc() {
    return cdc;
  }

  /**
   * Motivo del Evento.
   *
   * <p>SIFEN field {@code GDI004} ({@code mOtEve}).
   *
   * @return the value
   */
  @SifenField(id = "GDI004", xml = "mOtEve")
  public String reason() {
    return reason;
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
    if (!(other instanceof Disconformity)) {
      return false;
    }
    Disconformity that = (Disconformity) other;
    return Objects.equals(cdc, that.cdc) && Objects.equals(reason, that.reason);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(cdc, reason);
  }

  /**
   * The type name. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "Disconformity";
  }

  /**
   * Builder for {@link Disconformity}. Not thread-safe; {@link #build()} returns an immutable copy.
   */
  public static final class Builder {

    private @Nullable String cdc;
    private @Nullable String reason;

    private Builder() {}

    private Builder(Disconformity source) {
      this.cdc = source.cdc;
      this.reason = source.reason;
    }

    /**
     * Sets {@code cdc} (GDI002, {@code Id}).
     *
     * @param cdc the value
     * @return this builder
     */
    public Builder cdc(String cdc) {
      this.cdc = cdc;
      return this;
    }

    /**
     * Sets {@code reason} (GDI004, {@code mOtEve}).
     *
     * @param reason the value
     * @return this builder
     */
    public Builder reason(String reason) {
      this.reason = reason;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public Disconformity build() {
      return new Disconformity(this);
    }
  }
}
