/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Campos que describen el pago o entrega inicial de la operación con cheque.
 *
 * <p>SIFEN group {@code E630} ({@code gPagCheq}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "E630", xml = "gPagCheq")
public final class CheckPayment {

  private final String number;
  private final String issuingBank;

  private CheckPayment(Builder b) {
    this.number = Checks.required(b.number, "number", "E631", "dNumCheq");
    this.issuingBank = Checks.required(b.issuingBank, "issuingBank", "E632", "dBcoEmi");
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
   * Número de cheque.
   *
   * <p>SIFEN field {@code E631} ({@code dNumCheq}).
   *
   * @return the value
   */
  @SifenField(id = "E631", xml = "dNumCheq")
  public String number() {
    return number;
  }

  /**
   * Banco emisor.
   *
   * <p>SIFEN field {@code E632} ({@code dBcoEmi}).
   *
   * @return the value
   */
  @SifenField(id = "E632", xml = "dBcoEmi")
  public String issuingBank() {
    return issuingBank;
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
    if (!(other instanceof CheckPayment)) {
      return false;
    }
    CheckPayment that = (CheckPayment) other;
    return Objects.equals(number, that.number) && Objects.equals(issuingBank, that.issuingBank);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(number, issuingBank);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "CheckPayment";
  }

  /**
   * Builder for {@link CheckPayment}. Not thread-safe; {@link #build()} returns an immutable copy.
   */
  public static final class Builder {

    private @Nullable String number;
    private @Nullable String issuingBank;

    private Builder() {}

    private Builder(CheckPayment source) {
      this.number = source.number;
      this.issuingBank = source.issuingBank;
    }

    /**
     * Sets {@code number} (E631, {@code dNumCheq}).
     *
     * @param number the value
     * @return this builder
     */
    public Builder number(String number) {
      this.number = number;
      return this;
    }

    /**
     * Sets {@code issuingBank} (E632, {@code dBcoEmi}).
     *
     * @param issuingBank the value
     * @return this builder
     */
    public Builder issuingBank(String issuingBank) {
      this.issuingBank = issuingBank;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public CheckPayment build() {
      return new CheckPayment(this);
    }
  }
}
