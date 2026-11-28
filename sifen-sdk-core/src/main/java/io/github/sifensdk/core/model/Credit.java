/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import io.github.sifensdk.core.catalog.CreditCondition;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Campos que describen la operación a crédito.
 *
 * <p>SIFEN group {@code E640} ({@code gPagCred}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "E640", xml = "gPagCred")
public final class Credit {

  private final String conditionCode;
  private final @Nullable String conditionDescription;
  private final @Nullable String term;
  private final @Nullable Integer installmentCount;
  private final @Nullable BigDecimal downPaymentAmount;
  private final List<Installment> installments;

  private Credit(Builder b) {
    this.conditionCode = Checks.required(b.conditionCode, "conditionCode", "E641", "iCondCred");
    this.conditionDescription = b.conditionDescription;
    this.term = b.term;
    this.installmentCount = b.installmentCount;
    this.downPaymentAmount = b.downPaymentAmount;
    this.installments = List.copyOf(b.installments);
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
   * Condición de la operación a crédito.
   *
   * <p>SIFEN field {@code E641} ({@code iCondCred}).
   *
   * @return the value
   */
  @SifenField(id = "E641", xml = "iCondCred")
  public String conditionCode() {
    return conditionCode;
  }

  /**
   * Returns {@link #conditionCode()} (E641) as {@link CreditCondition}.
   *
   * @return the constant, or empty if the code is not set or this SDK version does not know it
   */
  public Optional<CreditCondition> condition() {
    return CreditCondition.fromCode(conditionCode);
  }

  /**
   * Descripción de la condición de la operación a crédito.
   *
   * <p>SIFEN field {@code E642} ({@code dDCondCred}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E641}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E642", xml = "dDCondCred")
  public @Nullable String conditionDescription() {
    return conditionDescription;
  }

  /**
   * Plazo del crédito.
   *
   * <p>SIFEN field {@code E643} ({@code dPlazoCre}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E643", xml = "dPlazoCre")
  public @Nullable String term() {
    return term;
  }

  /**
   * Cantidad de cuotas.
   *
   * <p>SIFEN field {@code E644} ({@code dCuotas}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E644", xml = "dCuotas")
  public @Nullable Integer installmentCount() {
    return installmentCount;
  }

  /**
   * Monto de la entrega inicial.
   *
   * <p>SIFEN field {@code E645} ({@code dMonEnt}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E645", xml = "dMonEnt")
  public @Nullable BigDecimal downPaymentAmount() {
    return downPaymentAmount;
  }

  /**
   * Campos que describen las cuotas.
   *
   * <p>SIFEN group {@code E650} ({@code gCuotas}).
   *
   * @return an unmodifiable list, possibly empty
   */
  @SifenField(id = "E650", xml = "gCuotas")
  public List<Installment> installments() {
    return installments;
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
    if (!(other instanceof Credit)) {
      return false;
    }
    Credit that = (Credit) other;
    return Objects.equals(conditionCode, that.conditionCode)
        && Objects.equals(conditionDescription, that.conditionDescription)
        && Objects.equals(term, that.term)
        && Objects.equals(installmentCount, that.installmentCount)
        && Objects.equals(downPaymentAmount, that.downPaymentAmount)
        && Objects.equals(installments, that.installments);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        conditionCode,
        conditionDescription,
        term,
        installmentCount,
        downPaymentAmount,
        installments);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "Credit[installments=" + installments.size() + "]";
  }

  /** Builder for {@link Credit}. Not thread-safe; {@link #build()} returns an immutable copy. */
  public static final class Builder {

    private @Nullable String conditionCode;
    private @Nullable String conditionDescription;
    private @Nullable String term;
    private @Nullable Integer installmentCount;
    private @Nullable BigDecimal downPaymentAmount;
    private List<Installment> installments = new ArrayList<>();

    private Builder() {}

    private Builder(Credit source) {
      this.conditionCode = source.conditionCode;
      this.conditionDescription = source.conditionDescription;
      this.term = source.term;
      this.installmentCount = source.installmentCount;
      this.downPaymentAmount = source.downPaymentAmount;
      this.installments = new ArrayList<>(source.installments);
    }

    /**
     * Sets {@code conditionCode} (E641, {@code iCondCred}).
     *
     * @param conditionCode the value
     * @return this builder
     */
    public Builder conditionCode(String conditionCode) {
      this.conditionCode = conditionCode;
      return this;
    }

    /**
     * Sets {@code conditionCode} (E641, {@code iCondCred}) from {@link CreditCondition}.
     *
     * @param condition the catalog constant
     * @return this builder
     */
    public Builder condition(CreditCondition condition) {
      this.conditionCode = condition.code();
      return this;
    }

    /**
     * Sets {@code conditionDescription} (E642, {@code dDCondCred}).
     *
     * @param conditionDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder conditionDescription(@Nullable String conditionDescription) {
      this.conditionDescription = conditionDescription;
      return this;
    }

    /**
     * Sets {@code term} (E643, {@code dPlazoCre}).
     *
     * @param term the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder term(@Nullable String term) {
      this.term = term;
      return this;
    }

    /**
     * Sets {@code installmentCount} (E644, {@code dCuotas}).
     *
     * @param installmentCount the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder installmentCount(@Nullable Integer installmentCount) {
      this.installmentCount = installmentCount;
      return this;
    }

    /**
     * Sets {@code downPaymentAmount} (E645, {@code dMonEnt}).
     *
     * @param downPaymentAmount the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder downPaymentAmount(@Nullable BigDecimal downPaymentAmount) {
      this.downPaymentAmount = downPaymentAmount;
      return this;
    }

    /**
     * Replaces the list of {@code installments} (E650, {@code gCuotas}).
     *
     * @param installments the elements, in document order
     * @return this builder
     */
    public Builder installments(List<Installment> installments) {
      this.installments = new ArrayList<>(installments);
      return this;
    }

    /**
     * Appends one element to {@code installments} (E650, {@code gCuotas}).
     *
     * @param installment the element
     * @return this builder
     */
    public Builder addInstallment(Installment installment) {
      this.installments.add(Objects.requireNonNull(installment, "installment"));
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public Credit build() {
      return new Credit(this);
    }
  }
}
