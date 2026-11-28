/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import io.github.sifensdk.core.catalog.EventReceiverType;
import io.github.sifensdk.core.catalog.IdDocumentType;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Raiz Gestión de Eventos Desconocimiento.
 *
 * <p>Detail of the event of desconocimiento de un DE (receptor): SIFEN group {@code GED001} ({@code
 * rGeVeDescon}). Immutable and thread-safe; create it with {@link #builder()}. The builder only
 * checks what Java needs (the elements the XSD always requires); the SIFEN rules are verified by
 * the validation step.
 */
@SifenField(id = "GED001", xml = "rGeVeDescon")
public final class NonRecognition implements EventDetail {

  private final String cdc;
  private final LocalDateTime issueDateTime;
  private final LocalDateTime receptionDateTime;
  private final String receiverTypeCode;
  private final String receiverName;
  private final @Nullable String receiverRuc;
  private final @Nullable String receiverCheckDigit;
  private final @Nullable String receiverIdDocumentTypeCode;
  private final @Nullable String receiverIdDocumentNumber;
  private final String reason;

  private NonRecognition(Builder b) {
    this.cdc = Checks.required(b.cdc, "cdc", "GED002", "Id");
    this.issueDateTime = Checks.required(b.issueDateTime, "issueDateTime", "GED003", "dFecEmi");
    this.receptionDateTime =
        Checks.required(b.receptionDateTime, "receptionDateTime", "GED004", "dFecRecep");
    this.receiverTypeCode =
        Checks.required(b.receiverTypeCode, "receiverTypeCode", "GED005", "iTipRec");
    this.receiverName = Checks.required(b.receiverName, "receiverName", "GED006", "dNomRec");
    this.receiverRuc = b.receiverRuc;
    this.receiverCheckDigit = b.receiverCheckDigit;
    this.receiverIdDocumentTypeCode = b.receiverIdDocumentTypeCode;
    this.receiverIdDocumentNumber = b.receiverIdDocumentNumber;
    this.reason = Checks.required(b.reason, "reason", "GED011", "mOtEve");
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
   * CDC del DE/DTE.
   *
   * <p>SIFEN field {@code GED002} ({@code Id}).
   *
   * @return the value
   */
  @SifenField(id = "GED002", xml = "Id")
  public String cdc() {
    return cdc;
  }

  /**
   * Fecha de emisión del DE/DTE.
   *
   * <p>SIFEN field {@code GED003} ({@code dFecEmi}).
   *
   * @return the value
   */
  @SifenField(id = "GED003", xml = "dFecEmi")
  public LocalDateTime issueDateTime() {
    return issueDateTime;
  }

  /**
   * Fecha Recepción DE.
   *
   * <p>SIFEN field {@code GED004} ({@code dFecRecep}).
   *
   * @return the value
   */
  @SifenField(id = "GED004", xml = "dFecRecep")
  public LocalDateTime receptionDateTime() {
    return receptionDateTime;
  }

  /**
   * Tipo de Receptor.
   *
   * <p>SIFEN field {@code GED005} ({@code iTipRec}).
   *
   * @return the value
   */
  @SifenField(id = "GED005", xml = "iTipRec")
  public String receiverTypeCode() {
    return receiverTypeCode;
  }

  /**
   * Returns {@link #receiverTypeCode()} (GED005) as {@link EventReceiverType}.
   *
   * @return the constant, or empty if the code is not set or this SDK version does not know it
   */
  public Optional<EventReceiverType> receiverType() {
    return EventReceiverType.fromCode(receiverTypeCode);
  }

  /**
   * Nombre o Razón Social del Receptor del DE/DTE.
   *
   * <p>SIFEN field {@code GED006} ({@code dNomRec}).
   *
   * @return the value
   */
  @SifenField(id = "GED006", xml = "dNomRec")
  public String receiverName() {
    return receiverName;
  }

  /**
   * Ruc del Receptor.
   *
   * <p>SIFEN field {@code GED007} ({@code dRucRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GED007", xml = "dRucRec")
  public @Nullable String receiverRuc() {
    return receiverRuc;
  }

  /**
   * Dígito verificador del RUC del contribuyente receptor.
   *
   * <p>SIFEN field {@code GED008} ({@code dDVRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GED008", xml = "dDVRec")
  public @Nullable String receiverCheckDigit() {
    return receiverCheckDigit;
  }

  /**
   * Tipo de documento de identidad del receptor.
   *
   * <p>SIFEN field {@code GED009} ({@code dTipIDRec}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GED009", xml = "dTipIDRec")
  public @Nullable String receiverIdDocumentTypeCode() {
    return receiverIdDocumentTypeCode;
  }

  /**
   * Returns {@link #receiverIdDocumentTypeCode()} (GED009) as {@link IdDocumentType}.
   *
   * @return the constant, or empty if the code is not set or this SDK version does not know it
   */
  public Optional<IdDocumentType> receiverIdDocumentType() {
    return receiverIdDocumentTypeCode == null
        ? Optional.empty()
        : IdDocumentType.fromCode(receiverIdDocumentTypeCode);
  }

  /**
   * Número de documento de identidad.
   *
   * <p>SIFEN field {@code GED010} ({@code dNumID}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "GED010", xml = "dNumID")
  public @Nullable String receiverIdDocumentNumber() {
    return receiverIdDocumentNumber;
  }

  /**
   * Motivo del Evento.
   *
   * <p>SIFEN field {@code GED011} ({@code mOtEve}).
   *
   * @return the value
   */
  @SifenField(id = "GED011", xml = "mOtEve")
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
    if (!(other instanceof NonRecognition)) {
      return false;
    }
    NonRecognition that = (NonRecognition) other;
    return Objects.equals(cdc, that.cdc)
        && Objects.equals(issueDateTime, that.issueDateTime)
        && Objects.equals(receptionDateTime, that.receptionDateTime)
        && Objects.equals(receiverTypeCode, that.receiverTypeCode)
        && Objects.equals(receiverName, that.receiverName)
        && Objects.equals(receiverRuc, that.receiverRuc)
        && Objects.equals(receiverCheckDigit, that.receiverCheckDigit)
        && Objects.equals(receiverIdDocumentTypeCode, that.receiverIdDocumentTypeCode)
        && Objects.equals(receiverIdDocumentNumber, that.receiverIdDocumentNumber)
        && Objects.equals(reason, that.reason);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        cdc,
        issueDateTime,
        receptionDateTime,
        receiverTypeCode,
        receiverName,
        receiverRuc,
        receiverCheckDigit,
        receiverIdDocumentTypeCode,
        receiverIdDocumentNumber,
        reason);
  }

  /**
   * The type name. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "NonRecognition";
  }

  /**
   * Builder for {@link NonRecognition}. Not thread-safe; {@link #build()} returns an immutable
   * copy.
   */
  public static final class Builder {

    private @Nullable String cdc;
    private @Nullable LocalDateTime issueDateTime;
    private @Nullable LocalDateTime receptionDateTime;
    private @Nullable String receiverTypeCode;
    private @Nullable String receiverName;
    private @Nullable String receiverRuc;
    private @Nullable String receiverCheckDigit;
    private @Nullable String receiverIdDocumentTypeCode;
    private @Nullable String receiverIdDocumentNumber;
    private @Nullable String reason;

    private Builder() {}

    private Builder(NonRecognition source) {
      this.cdc = source.cdc;
      this.issueDateTime = source.issueDateTime;
      this.receptionDateTime = source.receptionDateTime;
      this.receiverTypeCode = source.receiverTypeCode;
      this.receiverName = source.receiverName;
      this.receiverRuc = source.receiverRuc;
      this.receiverCheckDigit = source.receiverCheckDigit;
      this.receiverIdDocumentTypeCode = source.receiverIdDocumentTypeCode;
      this.receiverIdDocumentNumber = source.receiverIdDocumentNumber;
      this.reason = source.reason;
    }

    /**
     * Sets {@code cdc} (GED002, {@code Id}).
     *
     * @param cdc the value
     * @return this builder
     */
    public Builder cdc(String cdc) {
      this.cdc = cdc;
      return this;
    }

    /**
     * Sets {@code issueDateTime} (GED003, {@code dFecEmi}).
     *
     * @param issueDateTime the value
     * @return this builder
     */
    public Builder issueDateTime(LocalDateTime issueDateTime) {
      this.issueDateTime = issueDateTime;
      return this;
    }

    /**
     * Sets {@code receptionDateTime} (GED004, {@code dFecRecep}).
     *
     * @param receptionDateTime the value
     * @return this builder
     */
    public Builder receptionDateTime(LocalDateTime receptionDateTime) {
      this.receptionDateTime = receptionDateTime;
      return this;
    }

    /**
     * Sets {@code receiverTypeCode} (GED005, {@code iTipRec}).
     *
     * @param receiverTypeCode the value
     * @return this builder
     */
    public Builder receiverTypeCode(String receiverTypeCode) {
      this.receiverTypeCode = receiverTypeCode;
      return this;
    }

    /**
     * Sets {@code receiverTypeCode} (GED005, {@code iTipRec}) from {@link EventReceiverType}.
     *
     * @param receiverType the catalog constant
     * @return this builder
     */
    public Builder receiverType(EventReceiverType receiverType) {
      this.receiverTypeCode = receiverType.code();
      return this;
    }

    /**
     * Sets {@code receiverName} (GED006, {@code dNomRec}).
     *
     * @param receiverName the value
     * @return this builder
     */
    public Builder receiverName(String receiverName) {
      this.receiverName = receiverName;
      return this;
    }

    /**
     * Sets {@code receiverRuc} (GED007, {@code dRucRec}).
     *
     * @param receiverRuc the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder receiverRuc(@Nullable String receiverRuc) {
      this.receiverRuc = receiverRuc;
      return this;
    }

    /**
     * Sets {@code receiverCheckDigit} (GED008, {@code dDVRec}).
     *
     * @param receiverCheckDigit the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder receiverCheckDigit(@Nullable String receiverCheckDigit) {
      this.receiverCheckDigit = receiverCheckDigit;
      return this;
    }

    /**
     * Sets {@code receiverIdDocumentTypeCode} (GED009, {@code dTipIDRec}).
     *
     * @param receiverIdDocumentTypeCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder receiverIdDocumentTypeCode(@Nullable String receiverIdDocumentTypeCode) {
      this.receiverIdDocumentTypeCode = receiverIdDocumentTypeCode;
      return this;
    }

    /**
     * Sets {@code receiverIdDocumentTypeCode} (GED009, {@code dTipIDRec}) from {@link
     * IdDocumentType}.
     *
     * @param receiverIdDocumentType the catalog constant
     * @return this builder
     */
    public Builder receiverIdDocumentType(@Nullable IdDocumentType receiverIdDocumentType) {
      this.receiverIdDocumentTypeCode =
          receiverIdDocumentType == null ? null : receiverIdDocumentType.code();
      return this;
    }

    /**
     * Sets {@code receiverIdDocumentNumber} (GED010, {@code dNumID}).
     *
     * @param receiverIdDocumentNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder receiverIdDocumentNumber(@Nullable String receiverIdDocumentNumber) {
      this.receiverIdDocumentNumber = receiverIdDocumentNumber;
      return this;
    }

    /**
     * Sets {@code reason} (GED011, {@code mOtEve}).
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
    public NonRecognition build() {
      return new NonRecognition(this);
    }
  }
}
