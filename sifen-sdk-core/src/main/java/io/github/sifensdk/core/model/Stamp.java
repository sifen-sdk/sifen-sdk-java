/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import io.github.sifensdk.core.catalog.DocumentType;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Datos del timbrado.
 *
 * <p>SIFEN group {@code C001} ({@code gTimb}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "C001", xml = "gTimb")
public final class Stamp {

  private final String documentTypeCode;
  private final @Nullable String documentTypeDescription;
  private final String number;
  private final String establishment;
  private final String dispatchPoint;
  private final String documentNumber;
  private final @Nullable String series;
  private final LocalDate startDate;

  private Stamp(Builder b) {
    this.documentTypeCode =
        Checks.required(b.documentTypeCode, "documentTypeCode", "C002", "iTiDE");
    this.documentTypeDescription = b.documentTypeDescription;
    this.number = Checks.required(b.number, "number", "C004", "dNumTim");
    this.establishment = Checks.required(b.establishment, "establishment", "C005", "dEst");
    this.dispatchPoint = Checks.required(b.dispatchPoint, "dispatchPoint", "C006", "dPunExp");
    this.documentNumber = Checks.required(b.documentNumber, "documentNumber", "C007", "dNumDoc");
    this.series = b.series;
    this.startDate = Checks.required(b.startDate, "startDate", "C008", "dFeIniT");
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
   * Tipo de Documento Electrónico.
   *
   * <p>SIFEN field {@code C002} ({@code iTiDE}).
   *
   * @return the value
   */
  @SifenField(id = "C002", xml = "iTiDE")
  public String documentTypeCode() {
    return documentTypeCode;
  }

  /**
   * Returns {@link #documentTypeCode()} (C002) as {@link DocumentType}.
   *
   * @return the constant, or empty if the code is not set or this SDK version does not know it
   */
  public Optional<DocumentType> documentType() {
    return DocumentType.fromCode(documentTypeCode);
  }

  /**
   * Descripción del tipo de documento electrónico.
   *
   * <p>SIFEN field {@code C003} ({@code dDesTiDE}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code C002}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "C003", xml = "dDesTiDE")
  public @Nullable String documentTypeDescription() {
    return documentTypeDescription;
  }

  /**
   * Número del timbrado.
   *
   * <p>SIFEN field {@code C004} ({@code dNumTim}).
   *
   * @return the value
   */
  @SifenField(id = "C004", xml = "dNumTim")
  public String number() {
    return number;
  }

  /**
   * Establecimiento.
   *
   * <p>SIFEN field {@code C005} ({@code dEst}).
   *
   * @return the value
   */
  @SifenField(id = "C005", xml = "dEst")
  public String establishment() {
    return establishment;
  }

  /**
   * Punto de expedición.
   *
   * <p>SIFEN field {@code C006} ({@code dPunExp}).
   *
   * @return the value
   */
  @SifenField(id = "C006", xml = "dPunExp")
  public String dispatchPoint() {
    return dispatchPoint;
  }

  /**
   * Número del documento.
   *
   * <p>SIFEN field {@code C007} ({@code dNumDoc}).
   *
   * @return the value
   */
  @SifenField(id = "C007", xml = "dNumDoc")
  public String documentNumber() {
    return documentNumber;
  }

  /**
   * Serie del número de timbrado.
   *
   * <p>SIFEN field {@code C010} ({@code dSerieNum}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "C010", xml = "dSerieNum")
  public @Nullable String series() {
    return series;
  }

  /**
   * Fecha inicio de vigencia del timbrado.
   *
   * <p>SIFEN field {@code C008} ({@code dFeIniT}).
   *
   * @return the value
   */
  @SifenField(id = "C008", xml = "dFeIniT")
  public LocalDate startDate() {
    return startDate;
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
    if (!(other instanceof Stamp)) {
      return false;
    }
    Stamp that = (Stamp) other;
    return Objects.equals(documentTypeCode, that.documentTypeCode)
        && Objects.equals(documentTypeDescription, that.documentTypeDescription)
        && Objects.equals(number, that.number)
        && Objects.equals(establishment, that.establishment)
        && Objects.equals(dispatchPoint, that.dispatchPoint)
        && Objects.equals(documentNumber, that.documentNumber)
        && Objects.equals(series, that.series)
        && Objects.equals(startDate, that.startDate);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        documentTypeCode,
        documentTypeDescription,
        number,
        establishment,
        dispatchPoint,
        documentNumber,
        series,
        startDate);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "Stamp";
  }

  /** Builder for {@link Stamp}. Not thread-safe; {@link #build()} returns an immutable copy. */
  public static final class Builder {

    private @Nullable String documentTypeCode;
    private @Nullable String documentTypeDescription;
    private @Nullable String number;
    private @Nullable String establishment;
    private @Nullable String dispatchPoint;
    private @Nullable String documentNumber;
    private @Nullable String series;
    private @Nullable LocalDate startDate;

    private Builder() {}

    private Builder(Stamp source) {
      this.documentTypeCode = source.documentTypeCode;
      this.documentTypeDescription = source.documentTypeDescription;
      this.number = source.number;
      this.establishment = source.establishment;
      this.dispatchPoint = source.dispatchPoint;
      this.documentNumber = source.documentNumber;
      this.series = source.series;
      this.startDate = source.startDate;
    }

    /**
     * Sets {@code documentTypeCode} (C002, {@code iTiDE}).
     *
     * @param documentTypeCode the value
     * @return this builder
     */
    public Builder documentTypeCode(String documentTypeCode) {
      this.documentTypeCode = documentTypeCode;
      return this;
    }

    /**
     * Sets {@code documentTypeCode} (C002, {@code iTiDE}) from {@link DocumentType}.
     *
     * @param documentType the catalog constant
     * @return this builder
     */
    public Builder documentType(DocumentType documentType) {
      this.documentTypeCode = documentType.code();
      return this;
    }

    /**
     * Sets {@code documentTypeDescription} (C003, {@code dDesTiDE}).
     *
     * @param documentTypeDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder documentTypeDescription(@Nullable String documentTypeDescription) {
      this.documentTypeDescription = documentTypeDescription;
      return this;
    }

    /**
     * Sets {@code number} (C004, {@code dNumTim}).
     *
     * @param number the value
     * @return this builder
     */
    public Builder number(String number) {
      this.number = number;
      return this;
    }

    /**
     * Sets {@code establishment} (C005, {@code dEst}).
     *
     * @param establishment the value
     * @return this builder
     */
    public Builder establishment(String establishment) {
      this.establishment = establishment;
      return this;
    }

    /**
     * Sets {@code dispatchPoint} (C006, {@code dPunExp}).
     *
     * @param dispatchPoint the value
     * @return this builder
     */
    public Builder dispatchPoint(String dispatchPoint) {
      this.dispatchPoint = dispatchPoint;
      return this;
    }

    /**
     * Sets {@code documentNumber} (C007, {@code dNumDoc}).
     *
     * @param documentNumber the value
     * @return this builder
     */
    public Builder documentNumber(String documentNumber) {
      this.documentNumber = documentNumber;
      return this;
    }

    /**
     * Sets {@code series} (C010, {@code dSerieNum}).
     *
     * @param series the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder series(@Nullable String series) {
      this.series = series;
      return this;
    }

    /**
     * Sets {@code startDate} (C008, {@code dFeIniT}).
     *
     * @param startDate the value
     * @return this builder
     */
    public Builder startDate(LocalDate startDate) {
      this.startDate = startDate;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public Stamp build() {
      return new Stamp(this);
    }
  }
}
