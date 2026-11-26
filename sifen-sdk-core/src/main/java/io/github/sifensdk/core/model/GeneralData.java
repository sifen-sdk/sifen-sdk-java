/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.time.LocalDateTime;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Campos generales del DE.
 *
 * <p>SIFEN group {@code D001} ({@code gDatGralOpe}). Immutable and thread-safe; create it with
 * {@link #builder()}. The builder only checks what Java needs (required elements and non-null
 * lists); the SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step,
 * never here. Amounts are kept exactly as given.
 */
@SifenField(id = "D001", xml = "gDatGralOpe")
public final class GeneralData {

  private final LocalDateTime issueDateTime;
  private final @Nullable CommercialOperation commercialOperation;
  private final Issuer issuer;
  private final Receiver receiver;

  private GeneralData(Builder b) {
    this.issueDateTime = Checks.required(b.issueDateTime, "issueDateTime", "D002", "dFeEmiDE");
    this.commercialOperation = b.commercialOperation;
    this.issuer = Checks.required(b.issuer, "issuer", "D100", "gEmis");
    this.receiver = Checks.required(b.receiver, "receiver", "D200", "gDatRec");
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
   * Fecha y hora de emisión del DE.
   *
   * <p>SIFEN field {@code D002} ({@code dFeEmiDE}).
   *
   * @return the value
   */
  @SifenField(id = "D002", xml = "dFeEmiDE")
  public LocalDateTime issueDateTime() {
    return issueDateTime;
  }

  /**
   * Campos inherentes a la operación comercial.
   *
   * <p>SIFEN group {@code D010} ({@code gOpeCom}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D010", xml = "gOpeCom")
  public @Nullable CommercialOperation commercialOperation() {
    return commercialOperation;
  }

  /**
   * Grupo de campos que identifican al emisor.
   *
   * <p>SIFEN group {@code D100} ({@code gEmis}).
   *
   * @return the value
   */
  @SifenField(id = "D100", xml = "gEmis")
  public Issuer issuer() {
    return issuer;
  }

  /**
   * Grupo de campos que identifican al receptor.
   *
   * <p>SIFEN group {@code D200} ({@code gDatRec}).
   *
   * @return the value
   */
  @SifenField(id = "D200", xml = "gDatRec")
  public Receiver receiver() {
    return receiver;
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
    if (!(other instanceof GeneralData)) {
      return false;
    }
    GeneralData that = (GeneralData) other;
    return Objects.equals(issueDateTime, that.issueDateTime)
        && Objects.equals(commercialOperation, that.commercialOperation)
        && Objects.equals(issuer, that.issuer)
        && Objects.equals(receiver, that.receiver);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(issueDateTime, commercialOperation, issuer, receiver);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "GeneralData";
  }

  /**
   * Builder for {@link GeneralData}. Not thread-safe; {@link #build()} returns an immutable copy.
   */
  public static final class Builder {

    private @Nullable LocalDateTime issueDateTime;
    private @Nullable CommercialOperation commercialOperation;
    private @Nullable Issuer issuer;
    private @Nullable Receiver receiver;

    private Builder() {}

    private Builder(GeneralData source) {
      this.issueDateTime = source.issueDateTime;
      this.commercialOperation = source.commercialOperation;
      this.issuer = source.issuer;
      this.receiver = source.receiver;
    }

    /**
     * Sets {@code issueDateTime} (D002, {@code dFeEmiDE}).
     *
     * @param issueDateTime the value
     * @return this builder
     */
    public Builder issueDateTime(LocalDateTime issueDateTime) {
      this.issueDateTime = issueDateTime;
      return this;
    }

    /**
     * Sets {@code commercialOperation} (D010, {@code gOpeCom}).
     *
     * @param commercialOperation the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder commercialOperation(@Nullable CommercialOperation commercialOperation) {
      this.commercialOperation = commercialOperation;
      return this;
    }

    /**
     * Sets {@code issuer} (D100, {@code gEmis}).
     *
     * @param issuer the value
     * @return this builder
     */
    public Builder issuer(Issuer issuer) {
      this.issuer = issuer;
      return this;
    }

    /**
     * Sets {@code receiver} (D200, {@code gDatRec}).
     *
     * @param receiver the value
     * @return this builder
     */
    public Builder receiver(Receiver receiver) {
      this.receiver = receiver;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public GeneralData build() {
      return new GeneralData(this);
    }
  }
}
