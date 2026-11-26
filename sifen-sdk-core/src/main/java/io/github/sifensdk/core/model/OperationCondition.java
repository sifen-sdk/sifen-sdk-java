/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Campos que describen la condición de la operación.
 *
 * <p>SIFEN group {@code E600} ({@code gCamCond}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "E600", xml = "gCamCond")
public final class OperationCondition {

  private final String conditionCode;
  private final @Nullable String conditionDescription;
  private final List<Payment> payments;
  private final @Nullable Credit credit;

  private OperationCondition(Builder b) {
    this.conditionCode = Checks.required(b.conditionCode, "conditionCode", "E601", "iCondOpe");
    this.conditionDescription = b.conditionDescription;
    this.payments = List.copyOf(b.payments);
    this.credit = b.credit;
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
   * Condición de la operación.
   *
   * <p>SIFEN field {@code E601} ({@code iCondOpe}).
   *
   * @return the value
   */
  @SifenField(id = "E601", xml = "iCondOpe")
  public String conditionCode() {
    return conditionCode;
  }

  /**
   * Descripción de la condición de operación.
   *
   * <p>SIFEN field {@code E602} ({@code dDCondOpe}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E601}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E602", xml = "dDCondOpe")
  public @Nullable String conditionDescription() {
    return conditionDescription;
  }

  /**
   * Campos que describen la forma de pago al contado o del monto de la entrega inicial.
   *
   * <p>SIFEN group {@code E605} ({@code gPaConEIni}).
   *
   * @return an unmodifiable list, possibly empty
   */
  @SifenField(id = "E605", xml = "gPaConEIni")
  public List<Payment> payments() {
    return payments;
  }

  /**
   * Campos que describen la operación a crédito.
   *
   * <p>SIFEN group {@code E640} ({@code gPagCred}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E640", xml = "gPagCred")
  public @Nullable Credit credit() {
    return credit;
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
    if (!(other instanceof OperationCondition)) {
      return false;
    }
    OperationCondition that = (OperationCondition) other;
    return Objects.equals(conditionCode, that.conditionCode)
        && Objects.equals(conditionDescription, that.conditionDescription)
        && Objects.equals(payments, that.payments)
        && Objects.equals(credit, that.credit);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(conditionCode, conditionDescription, payments, credit);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "OperationCondition[payments=" + payments.size() + "]";
  }

  /**
   * Builder for {@link OperationCondition}. Not thread-safe; {@link #build()} returns an immutable
   * copy.
   */
  public static final class Builder {

    private @Nullable String conditionCode;
    private @Nullable String conditionDescription;
    private List<Payment> payments = new ArrayList<>();
    private @Nullable Credit credit;

    private Builder() {}

    private Builder(OperationCondition source) {
      this.conditionCode = source.conditionCode;
      this.conditionDescription = source.conditionDescription;
      this.payments = new ArrayList<>(source.payments);
      this.credit = source.credit;
    }

    /**
     * Sets {@code conditionCode} (E601, {@code iCondOpe}).
     *
     * @param conditionCode the value
     * @return this builder
     */
    public Builder conditionCode(String conditionCode) {
      this.conditionCode = conditionCode;
      return this;
    }

    /**
     * Sets {@code conditionDescription} (E602, {@code dDCondOpe}).
     *
     * @param conditionDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder conditionDescription(@Nullable String conditionDescription) {
      this.conditionDescription = conditionDescription;
      return this;
    }

    /**
     * Replaces the list of {@code payments} (E605, {@code gPaConEIni}).
     *
     * @param payments the elements, in document order
     * @return this builder
     */
    public Builder payments(List<Payment> payments) {
      this.payments = new ArrayList<>(payments);
      return this;
    }

    /**
     * Appends one element to {@code payments} (E605, {@code gPaConEIni}).
     *
     * @param payment the element
     * @return this builder
     */
    public Builder addPayment(Payment payment) {
      this.payments.add(Objects.requireNonNull(payment, "payment"));
      return this;
    }

    /**
     * Sets {@code credit} (E640, {@code gPagCred}).
     *
     * @param credit the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder credit(@Nullable Credit credit) {
      this.credit = credit;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public OperationCondition build() {
      return new OperationCondition(this);
    }
  }
}
