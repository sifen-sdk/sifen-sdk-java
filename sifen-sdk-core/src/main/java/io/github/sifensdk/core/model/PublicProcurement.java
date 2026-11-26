/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.time.LocalDate;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Campos que describen las informaciones de compras públicas.
 *
 * <p>SIFEN group {@code E020} ({@code gCompPub}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "E020", xml = "gCompPub")
public final class PublicProcurement {

  private final String modalityCode;
  private final String entityCode;
  private final String yearCode;
  private final String sequence;
  private final LocalDate contractingCodeIssueDate;

  private PublicProcurement(Builder b) {
    this.modalityCode = Checks.required(b.modalityCode, "modalityCode", "E021", "dModCont");
    this.entityCode = Checks.required(b.entityCode, "entityCode", "E022", "dEntCont");
    this.yearCode = Checks.required(b.yearCode, "yearCode", "E023", "dAnoCont");
    this.sequence = Checks.required(b.sequence, "sequence", "E024", "dSecCont");
    this.contractingCodeIssueDate =
        Checks.required(
            b.contractingCodeIssueDate, "contractingCodeIssueDate", "E025", "dFeCodCont");
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
   * Modalidad - Código emitido por la DNCP.
   *
   * <p>SIFEN field {@code E021} ({@code dModCont}).
   *
   * @return the value
   */
  @SifenField(id = "E021", xml = "dModCont")
  public String modalityCode() {
    return modalityCode;
  }

  /**
   * Entidad - Código emitido por la DNCP.
   *
   * <p>SIFEN field {@code E022} ({@code dEntCont}).
   *
   * @return the value
   */
  @SifenField(id = "E022", xml = "dEntCont")
  public String entityCode() {
    return entityCode;
  }

  /**
   * Año - Código emitido por la DNCP.
   *
   * <p>SIFEN field {@code E023} ({@code dAnoCont}).
   *
   * @return the value
   */
  @SifenField(id = "E023", xml = "dAnoCont")
  public String yearCode() {
    return yearCode;
  }

  /**
   * Secuencia - emitido por la DNCP.
   *
   * <p>SIFEN field {@code E024} ({@code dSecCont}).
   *
   * @return the value
   */
  @SifenField(id = "E024", xml = "dSecCont")
  public String sequence() {
    return sequence;
  }

  /**
   * Fecha de emisión del código de contratación por la DNCP.
   *
   * <p>SIFEN field {@code E025} ({@code dFeCodCont}).
   *
   * @return the value
   */
  @SifenField(id = "E025", xml = "dFeCodCont")
  public LocalDate contractingCodeIssueDate() {
    return contractingCodeIssueDate;
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
    if (!(other instanceof PublicProcurement)) {
      return false;
    }
    PublicProcurement that = (PublicProcurement) other;
    return Objects.equals(modalityCode, that.modalityCode)
        && Objects.equals(entityCode, that.entityCode)
        && Objects.equals(yearCode, that.yearCode)
        && Objects.equals(sequence, that.sequence)
        && Objects.equals(contractingCodeIssueDate, that.contractingCodeIssueDate);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(modalityCode, entityCode, yearCode, sequence, contractingCodeIssueDate);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "PublicProcurement";
  }

  /**
   * Builder for {@link PublicProcurement}. Not thread-safe; {@link #build()} returns an immutable
   * copy.
   */
  public static final class Builder {

    private @Nullable String modalityCode;
    private @Nullable String entityCode;
    private @Nullable String yearCode;
    private @Nullable String sequence;
    private @Nullable LocalDate contractingCodeIssueDate;

    private Builder() {}

    private Builder(PublicProcurement source) {
      this.modalityCode = source.modalityCode;
      this.entityCode = source.entityCode;
      this.yearCode = source.yearCode;
      this.sequence = source.sequence;
      this.contractingCodeIssueDate = source.contractingCodeIssueDate;
    }

    /**
     * Sets {@code modalityCode} (E021, {@code dModCont}).
     *
     * @param modalityCode the value
     * @return this builder
     */
    public Builder modalityCode(String modalityCode) {
      this.modalityCode = modalityCode;
      return this;
    }

    /**
     * Sets {@code entityCode} (E022, {@code dEntCont}).
     *
     * @param entityCode the value
     * @return this builder
     */
    public Builder entityCode(String entityCode) {
      this.entityCode = entityCode;
      return this;
    }

    /**
     * Sets {@code yearCode} (E023, {@code dAnoCont}).
     *
     * @param yearCode the value
     * @return this builder
     */
    public Builder yearCode(String yearCode) {
      this.yearCode = yearCode;
      return this;
    }

    /**
     * Sets {@code sequence} (E024, {@code dSecCont}).
     *
     * @param sequence the value
     * @return this builder
     */
    public Builder sequence(String sequence) {
      this.sequence = sequence;
      return this;
    }

    /**
     * Sets {@code contractingCodeIssueDate} (E025, {@code dFeCodCont}).
     *
     * @param contractingCodeIssueDate the value
     * @return this builder
     */
    public Builder contractingCodeIssueDate(LocalDate contractingCodeIssueDate) {
      this.contractingCodeIssueDate = contractingCodeIssueDate;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public PublicProcurement build() {
      return new PublicProcurement(this);
    }
  }
}
