/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Grupo de campos que identifican al responsable de la generación del DE.
 *
 * <p>SIFEN group {@code D140} ({@code gRespDE}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "D140", xml = "gRespDE")
public final class GenerationResponsible {

  private final String idDocumentTypeCode;
  private final @Nullable String idDocumentTypeDescription;
  private final String idDocumentNumber;
  private final String name;
  private final String position;

  private GenerationResponsible(Builder b) {
    this.idDocumentTypeCode =
        Checks.required(b.idDocumentTypeCode, "idDocumentTypeCode", "D141", "iTipIDRespDE");
    this.idDocumentTypeDescription = b.idDocumentTypeDescription;
    this.idDocumentNumber =
        Checks.required(b.idDocumentNumber, "idDocumentNumber", "D143", "dNumIDRespDE");
    this.name = Checks.required(b.name, "name", "D144", "dNomRespDE");
    this.position = Checks.required(b.position, "position", "D145", "dCarRespDE");
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
   * Tipo de documento de identidad del responsable de la generación del DE.
   *
   * <p>SIFEN field {@code D141} ({@code iTipIDRespDE}).
   *
   * @return the value
   */
  @SifenField(id = "D141", xml = "iTipIDRespDE")
  public String idDocumentTypeCode() {
    return idDocumentTypeCode;
  }

  /**
   * Descripción del tipo de documento de identidad del responsable de la generación del DE.
   *
   * <p>SIFEN field {@code D142} ({@code dDTipIDRespDE}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code D141}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D142", xml = "dDTipIDRespDE")
  public @Nullable String idDocumentTypeDescription() {
    return idDocumentTypeDescription;
  }

  /**
   * Número de documento de identidad del responsable de la generación del DE.
   *
   * <p>SIFEN field {@code D143} ({@code dNumIDRespDE}).
   *
   * @return the value
   */
  @SifenField(id = "D143", xml = "dNumIDRespDE")
  public String idDocumentNumber() {
    return idDocumentNumber;
  }

  /**
   * Nombre o razón social del responsable de la generación del DE.
   *
   * <p>SIFEN field {@code D144} ({@code dNomRespDE}).
   *
   * @return the value
   */
  @SifenField(id = "D144", xml = "dNomRespDE")
  public String name() {
    return name;
  }

  /**
   * Cargo del responsable de la generación del DE.
   *
   * <p>SIFEN field {@code D145} ({@code dCarRespDE}).
   *
   * @return the value
   */
  @SifenField(id = "D145", xml = "dCarRespDE")
  public String position() {
    return position;
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
    if (!(other instanceof GenerationResponsible)) {
      return false;
    }
    GenerationResponsible that = (GenerationResponsible) other;
    return Objects.equals(idDocumentTypeCode, that.idDocumentTypeCode)
        && Objects.equals(idDocumentTypeDescription, that.idDocumentTypeDescription)
        && Objects.equals(idDocumentNumber, that.idDocumentNumber)
        && Objects.equals(name, that.name)
        && Objects.equals(position, that.position);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        idDocumentTypeCode, idDocumentTypeDescription, idDocumentNumber, name, position);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "GenerationResponsible";
  }

  /**
   * Builder for {@link GenerationResponsible}. Not thread-safe; {@link #build()} returns an
   * immutable copy.
   */
  public static final class Builder {

    private @Nullable String idDocumentTypeCode;
    private @Nullable String idDocumentTypeDescription;
    private @Nullable String idDocumentNumber;
    private @Nullable String name;
    private @Nullable String position;

    private Builder() {}

    private Builder(GenerationResponsible source) {
      this.idDocumentTypeCode = source.idDocumentTypeCode;
      this.idDocumentTypeDescription = source.idDocumentTypeDescription;
      this.idDocumentNumber = source.idDocumentNumber;
      this.name = source.name;
      this.position = source.position;
    }

    /**
     * Sets {@code idDocumentTypeCode} (D141, {@code iTipIDRespDE}).
     *
     * @param idDocumentTypeCode the value
     * @return this builder
     */
    public Builder idDocumentTypeCode(String idDocumentTypeCode) {
      this.idDocumentTypeCode = idDocumentTypeCode;
      return this;
    }

    /**
     * Sets {@code idDocumentTypeDescription} (D142, {@code dDTipIDRespDE}).
     *
     * @param idDocumentTypeDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder idDocumentTypeDescription(@Nullable String idDocumentTypeDescription) {
      this.idDocumentTypeDescription = idDocumentTypeDescription;
      return this;
    }

    /**
     * Sets {@code idDocumentNumber} (D143, {@code dNumIDRespDE}).
     *
     * @param idDocumentNumber the value
     * @return this builder
     */
    public Builder idDocumentNumber(String idDocumentNumber) {
      this.idDocumentNumber = idDocumentNumber;
      return this;
    }

    /**
     * Sets {@code name} (D144, {@code dNomRespDE}).
     *
     * @param name the value
     * @return this builder
     */
    public Builder name(String name) {
      this.name = name;
      return this;
    }

    /**
     * Sets {@code position} (D145, {@code dCarRespDE}).
     *
     * @param position the value
     * @return this builder
     */
    public Builder position(String position) {
      this.position = position;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public GenerationResponsible build() {
      return new GenerationResponsible(this);
    }
  }
}
