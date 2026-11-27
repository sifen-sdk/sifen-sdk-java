/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Raiz Gestión de Eventos Inutilización.
 *
 * <p>Detail of the event of inutilización de números de documento: SIFEN group {@code GEI001}
 * ({@code rGeVeInu}). Immutable and thread-safe; create it with {@link #builder()}. The builder
 * only checks what Java needs (the elements the XSD always requires); the SIFEN rules are verified
 * by the validation step.
 */
@SifenField(id = "GEI001", xml = "rGeVeInu")
public final class NumberInvalidation implements EventDetail {

  private final String stampNumber;
  private final String establishment;
  private final String dispatchPoint;
  private final String rangeStartNumber;
  private final String rangeEndNumber;
  private final String documentTypeCode;
  private final String reason;
  private final @Nullable String documentSeries;

  private NumberInvalidation(Builder b) {
    this.stampNumber = Checks.required(b.stampNumber, "stampNumber", "GEI002", "dNumTim");
    this.establishment = Checks.required(b.establishment, "establishment", "GEI003", "dEst");
    this.dispatchPoint = Checks.required(b.dispatchPoint, "dispatchPoint", "GEI004", "dPunExp");
    this.rangeStartNumber =
        Checks.required(b.rangeStartNumber, "rangeStartNumber", "GEI005", "dNumIn");
    this.rangeEndNumber = Checks.required(b.rangeEndNumber, "rangeEndNumber", "GEI006", "dNumFin");
    this.documentTypeCode =
        Checks.required(b.documentTypeCode, "documentTypeCode", "GEI007", "iTiDE");
    this.reason = Checks.required(b.reason, "reason", "GEI008", "mOtEve");
    this.documentSeries = b.documentSeries;
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
   * Número del Timbrado.
   *
   * <p>SIFEN field {@code GEI002} ({@code dNumTim}).
   *
   * @return the value
   */
  @SifenField(id = "GEI002", xml = "dNumTim")
  public String stampNumber() {
    return stampNumber;
  }

  /**
   * Establecimiento.
   *
   * <p>SIFEN field {@code GEI003} ({@code dEst}).
   *
   * @return the value
   */
  @SifenField(id = "GEI003", xml = "dEst")
  public String establishment() {
    return establishment;
  }

  /**
   * Punto de expedición.
   *
   * <p>SIFEN field {@code GEI004} ({@code dPunExp}).
   *
   * @return the value
   */
  @SifenField(id = "GEI004", xml = "dPunExp")
  public String dispatchPoint() {
    return dispatchPoint;
  }

  /**
   * Número Inicio del rango del documento.
   *
   * <p>SIFEN field {@code GEI005} ({@code dNumIn}).
   *
   * @return the value
   */
  @SifenField(id = "GEI005", xml = "dNumIn")
  public String rangeStartNumber() {
    return rangeStartNumber;
  }

  /**
   * Número Final del rango del documento.
   *
   * <p>SIFEN field {@code GEI006} ({@code dNumFin}).
   *
   * @return the value
   */
  @SifenField(id = "GEI006", xml = "dNumFin")
  public String rangeEndNumber() {
    return rangeEndNumber;
  }

  /**
   * Tipo de Documento Electrónico.
   *
   * <p>SIFEN field {@code GEI007} ({@code iTiDE}).
   *
   * @return the value
   */
  @SifenField(id = "GEI007", xml = "iTiDE")
  public String documentTypeCode() {
    return documentTypeCode;
  }

  /**
   * Motivo del Evento.
   *
   * <p>SIFEN field {@code GEI008} ({@code mOtEve}).
   *
   * @return the value
   */
  @SifenField(id = "GEI008", xml = "mOtEve")
  public String reason() {
    return reason;
  }

  /**
   * Serie del número del documento.
   *
   * <p>SIFEN field {@code GEI009} ({@code dSerieNum}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GEI009", xml = "dSerieNum")
  public @Nullable String documentSeries() {
    return documentSeries;
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
    if (!(other instanceof NumberInvalidation)) {
      return false;
    }
    NumberInvalidation that = (NumberInvalidation) other;
    return Objects.equals(stampNumber, that.stampNumber)
        && Objects.equals(establishment, that.establishment)
        && Objects.equals(dispatchPoint, that.dispatchPoint)
        && Objects.equals(rangeStartNumber, that.rangeStartNumber)
        && Objects.equals(rangeEndNumber, that.rangeEndNumber)
        && Objects.equals(documentTypeCode, that.documentTypeCode)
        && Objects.equals(reason, that.reason)
        && Objects.equals(documentSeries, that.documentSeries);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        stampNumber,
        establishment,
        dispatchPoint,
        rangeStartNumber,
        rangeEndNumber,
        documentTypeCode,
        reason,
        documentSeries);
  }

  /**
   * The type name. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "NumberInvalidation";
  }

  /**
   * Builder for {@link NumberInvalidation}. Not thread-safe; {@link #build()} returns an immutable
   * copy.
   */
  public static final class Builder {

    private @Nullable String stampNumber;
    private @Nullable String establishment;
    private @Nullable String dispatchPoint;
    private @Nullable String rangeStartNumber;
    private @Nullable String rangeEndNumber;
    private @Nullable String documentTypeCode;
    private @Nullable String reason;
    private @Nullable String documentSeries;

    private Builder() {}

    private Builder(NumberInvalidation source) {
      this.stampNumber = source.stampNumber;
      this.establishment = source.establishment;
      this.dispatchPoint = source.dispatchPoint;
      this.rangeStartNumber = source.rangeStartNumber;
      this.rangeEndNumber = source.rangeEndNumber;
      this.documentTypeCode = source.documentTypeCode;
      this.reason = source.reason;
      this.documentSeries = source.documentSeries;
    }

    /**
     * Sets {@code stampNumber} (GEI002, {@code dNumTim}).
     *
     * @param stampNumber the value
     * @return this builder
     */
    public Builder stampNumber(String stampNumber) {
      this.stampNumber = stampNumber;
      return this;
    }

    /**
     * Sets {@code establishment} (GEI003, {@code dEst}).
     *
     * @param establishment the value
     * @return this builder
     */
    public Builder establishment(String establishment) {
      this.establishment = establishment;
      return this;
    }

    /**
     * Sets {@code dispatchPoint} (GEI004, {@code dPunExp}).
     *
     * @param dispatchPoint the value
     * @return this builder
     */
    public Builder dispatchPoint(String dispatchPoint) {
      this.dispatchPoint = dispatchPoint;
      return this;
    }

    /**
     * Sets {@code rangeStartNumber} (GEI005, {@code dNumIn}).
     *
     * @param rangeStartNumber the value
     * @return this builder
     */
    public Builder rangeStartNumber(String rangeStartNumber) {
      this.rangeStartNumber = rangeStartNumber;
      return this;
    }

    /**
     * Sets {@code rangeEndNumber} (GEI006, {@code dNumFin}).
     *
     * @param rangeEndNumber the value
     * @return this builder
     */
    public Builder rangeEndNumber(String rangeEndNumber) {
      this.rangeEndNumber = rangeEndNumber;
      return this;
    }

    /**
     * Sets {@code documentTypeCode} (GEI007, {@code iTiDE}).
     *
     * @param documentTypeCode the value
     * @return this builder
     */
    public Builder documentTypeCode(String documentTypeCode) {
      this.documentTypeCode = documentTypeCode;
      return this;
    }

    /**
     * Sets {@code reason} (GEI008, {@code mOtEve}).
     *
     * @param reason the value
     * @return this builder
     */
    public Builder reason(String reason) {
      this.reason = reason;
      return this;
    }

    /**
     * Sets {@code documentSeries} (GEI009, {@code dSerieNum}).
     *
     * @param documentSeries the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder documentSeries(@Nullable String documentSeries) {
      this.documentSeries = documentSeries;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public NumberInvalidation build() {
      return new NumberInvalidation(this);
    }
  }
}
