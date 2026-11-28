/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import io.github.sifensdk.core.catalog.PresenceIndicator;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Campos que componen la FE.
 *
 * <p>SIFEN group {@code E010} ({@code gCamFE}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "E010", xml = "gCamFE")
public final class Invoice {

  private final String presenceIndicatorCode;
  private final @Nullable String presenceIndicatorDescription;
  private final @Nullable LocalDate futureShipmentDate;
  private final @Nullable PublicProcurement publicProcurement;

  private Invoice(Builder b) {
    this.presenceIndicatorCode =
        Checks.required(b.presenceIndicatorCode, "presenceIndicatorCode", "E011", "iIndPres");
    this.presenceIndicatorDescription = b.presenceIndicatorDescription;
    this.futureShipmentDate = b.futureShipmentDate;
    this.publicProcurement = b.publicProcurement;
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
   * Indicador de presencia.
   *
   * <p>SIFEN field {@code E011} ({@code iIndPres}).
   *
   * @return the value
   */
  @SifenField(id = "E011", xml = "iIndPres")
  public String presenceIndicatorCode() {
    return presenceIndicatorCode;
  }

  /**
   * Returns {@link #presenceIndicatorCode()} (E011) as {@link PresenceIndicator}.
   *
   * @return the constant, or empty if the code is not set or this SDK version does not know it
   */
  public Optional<PresenceIndicator> presenceIndicator() {
    return PresenceIndicator.fromCode(presenceIndicatorCode);
  }

  /**
   * Descripción del indicador de presencia.
   *
   * <p>SIFEN field {@code E012} ({@code dDesIndPres}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E011}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E012", xml = "dDesIndPres")
  public @Nullable String presenceIndicatorDescription() {
    return presenceIndicatorDescription;
  }

  /**
   * Fecha futura del traslado de mercadería.
   *
   * <p>SIFEN field {@code E013} ({@code dFecEmNR}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E013", xml = "dFecEmNR")
  public @Nullable LocalDate futureShipmentDate() {
    return futureShipmentDate;
  }

  /**
   * Campos que describen las informaciones de compras públicas.
   *
   * <p>SIFEN group {@code E020} ({@code gCompPub}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E020", xml = "gCompPub")
  public @Nullable PublicProcurement publicProcurement() {
    return publicProcurement;
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
    if (!(other instanceof Invoice)) {
      return false;
    }
    Invoice that = (Invoice) other;
    return Objects.equals(presenceIndicatorCode, that.presenceIndicatorCode)
        && Objects.equals(presenceIndicatorDescription, that.presenceIndicatorDescription)
        && Objects.equals(futureShipmentDate, that.futureShipmentDate)
        && Objects.equals(publicProcurement, that.publicProcurement);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        presenceIndicatorCode, presenceIndicatorDescription, futureShipmentDate, publicProcurement);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "Invoice";
  }

  /** Builder for {@link Invoice}. Not thread-safe; {@link #build()} returns an immutable copy. */
  public static final class Builder {

    private @Nullable String presenceIndicatorCode;
    private @Nullable String presenceIndicatorDescription;
    private @Nullable LocalDate futureShipmentDate;
    private @Nullable PublicProcurement publicProcurement;

    private Builder() {}

    private Builder(Invoice source) {
      this.presenceIndicatorCode = source.presenceIndicatorCode;
      this.presenceIndicatorDescription = source.presenceIndicatorDescription;
      this.futureShipmentDate = source.futureShipmentDate;
      this.publicProcurement = source.publicProcurement;
    }

    /**
     * Sets {@code presenceIndicatorCode} (E011, {@code iIndPres}).
     *
     * @param presenceIndicatorCode the value
     * @return this builder
     */
    public Builder presenceIndicatorCode(String presenceIndicatorCode) {
      this.presenceIndicatorCode = presenceIndicatorCode;
      return this;
    }

    /**
     * Sets {@code presenceIndicatorCode} (E011, {@code iIndPres}) from {@link PresenceIndicator}.
     *
     * @param presenceIndicator the catalog constant
     * @return this builder
     */
    public Builder presenceIndicator(PresenceIndicator presenceIndicator) {
      this.presenceIndicatorCode = presenceIndicator.code();
      return this;
    }

    /**
     * Sets {@code presenceIndicatorDescription} (E012, {@code dDesIndPres}).
     *
     * @param presenceIndicatorDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder presenceIndicatorDescription(@Nullable String presenceIndicatorDescription) {
      this.presenceIndicatorDescription = presenceIndicatorDescription;
      return this;
    }

    /**
     * Sets {@code futureShipmentDate} (E013, {@code dFecEmNR}).
     *
     * @param futureShipmentDate the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder futureShipmentDate(@Nullable LocalDate futureShipmentDate) {
      this.futureShipmentDate = futureShipmentDate;
      return this;
    }

    /**
     * Sets {@code publicProcurement} (E020, {@code gCompPub}).
     *
     * @param publicProcurement the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder publicProcurement(@Nullable PublicProcurement publicProcurement) {
      this.publicProcurement = publicProcurement;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public Invoice build() {
      return new Invoice(this);
    }
  }
}
