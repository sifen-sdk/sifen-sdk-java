/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Campos inherentes a la operación comercial.
 *
 * <p>SIFEN group {@code D010} ({@code gOpeCom}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "D010", xml = "gOpeCom")
public final class CommercialOperation {

  private final @Nullable String transactionTypeCode;
  private final @Nullable String transactionTypeDescription;
  private final String taxTypeCode;
  private final @Nullable String taxTypeDescription;
  private final String currencyCode;
  private final @Nullable String currencyDescription;
  private final @Nullable String exchangeRateConditionCode;
  private final @Nullable BigDecimal exchangeRate;
  private final @Nullable String advanceConditionCode;
  private final @Nullable String advanceConditionDescription;
  private final List<AffectedObligation> affectedObligations;

  private CommercialOperation(Builder b) {
    this.transactionTypeCode = b.transactionTypeCode;
    this.transactionTypeDescription = b.transactionTypeDescription;
    this.taxTypeCode = Checks.required(b.taxTypeCode, "taxTypeCode", "D013", "iTImp");
    this.taxTypeDescription = b.taxTypeDescription;
    this.currencyCode = Checks.required(b.currencyCode, "currencyCode", "D015", "cMoneOpe");
    this.currencyDescription = b.currencyDescription;
    this.exchangeRateConditionCode = b.exchangeRateConditionCode;
    this.exchangeRate = b.exchangeRate;
    this.advanceConditionCode = b.advanceConditionCode;
    this.advanceConditionDescription = b.advanceConditionDescription;
    this.affectedObligations = List.copyOf(b.affectedObligations);
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
   * Tipo de transacción.
   *
   * <p>SIFEN field {@code D011} ({@code iTipTra}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D011", xml = "iTipTra")
  public @Nullable String transactionTypeCode() {
    return transactionTypeCode;
  }

  /**
   * Descripción del tipo de transacción.
   *
   * <p>SIFEN field {@code D012} ({@code dDesTipTra}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code D011}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D012", xml = "dDesTipTra")
  public @Nullable String transactionTypeDescription() {
    return transactionTypeDescription;
  }

  /**
   * Tipo de impuesto afectado.
   *
   * <p>SIFEN field {@code D013} ({@code iTImp}).
   *
   * @return the value
   */
  @SifenField(id = "D013", xml = "iTImp")
  public String taxTypeCode() {
    return taxTypeCode;
  }

  /**
   * Descripción del tipo de impuesto afectado.
   *
   * <p>SIFEN field {@code D014} ({@code dDesTImp}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code D013}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D014", xml = "dDesTImp")
  public @Nullable String taxTypeDescription() {
    return taxTypeDescription;
  }

  /**
   * Moneda de la operación.
   *
   * <p>SIFEN field {@code D015} ({@code cMoneOpe}).
   *
   * @return the value
   */
  @SifenField(id = "D015", xml = "cMoneOpe")
  public String currencyCode() {
    return currencyCode;
  }

  /**
   * Descripción de la moneda de la operación.
   *
   * <p>SIFEN field {@code D016} ({@code dDesMoneOpe}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code D015}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D016", xml = "dDesMoneOpe")
  public @Nullable String currencyDescription() {
    return currencyDescription;
  }

  /**
   * Condición del tipo de cambio.
   *
   * <p>SIFEN field {@code D017} ({@code dCondTiCam}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D017", xml = "dCondTiCam")
  public @Nullable String exchangeRateConditionCode() {
    return exchangeRateConditionCode;
  }

  /**
   * Tipo de cambio de la operación.
   *
   * <p>SIFEN field {@code D018} ({@code dTiCam}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D018", xml = "dTiCam")
  public @Nullable BigDecimal exchangeRate() {
    return exchangeRate;
  }

  /**
   * Condición del Anticipo.
   *
   * <p>SIFEN field {@code D019} ({@code iCondAnt}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D019", xml = "iCondAnt")
  public @Nullable String advanceConditionCode() {
    return advanceConditionCode;
  }

  /**
   * Descripción de la condición del Anticipo.
   *
   * <p>SIFEN field {@code D020} ({@code dDesCondAnt}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code D019}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "D020", xml = "dDesCondAnt")
  public @Nullable String advanceConditionDescription() {
    return advanceConditionDescription;
  }

  /**
   * Grupo de campos que identifican las obligaciones afectadas.
   *
   * <p>SIFEN group {@code D030} ({@code gOblAfe}).
   *
   * @return an unmodifiable list, possibly empty
   */
  @SifenField(id = "D030", xml = "gOblAfe")
  public List<AffectedObligation> affectedObligations() {
    return affectedObligations;
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
    if (!(other instanceof CommercialOperation)) {
      return false;
    }
    CommercialOperation that = (CommercialOperation) other;
    return Objects.equals(transactionTypeCode, that.transactionTypeCode)
        && Objects.equals(transactionTypeDescription, that.transactionTypeDescription)
        && Objects.equals(taxTypeCode, that.taxTypeCode)
        && Objects.equals(taxTypeDescription, that.taxTypeDescription)
        && Objects.equals(currencyCode, that.currencyCode)
        && Objects.equals(currencyDescription, that.currencyDescription)
        && Objects.equals(exchangeRateConditionCode, that.exchangeRateConditionCode)
        && Objects.equals(exchangeRate, that.exchangeRate)
        && Objects.equals(advanceConditionCode, that.advanceConditionCode)
        && Objects.equals(advanceConditionDescription, that.advanceConditionDescription)
        && Objects.equals(affectedObligations, that.affectedObligations);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        transactionTypeCode,
        transactionTypeDescription,
        taxTypeCode,
        taxTypeDescription,
        currencyCode,
        currencyDescription,
        exchangeRateConditionCode,
        exchangeRate,
        advanceConditionCode,
        advanceConditionDescription,
        affectedObligations);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "CommercialOperation[affectedObligations=" + affectedObligations.size() + "]";
  }

  /**
   * Builder for {@link CommercialOperation}. Not thread-safe; {@link #build()} returns an immutable
   * copy.
   */
  public static final class Builder {

    private @Nullable String transactionTypeCode;
    private @Nullable String transactionTypeDescription;
    private @Nullable String taxTypeCode;
    private @Nullable String taxTypeDescription;
    private @Nullable String currencyCode;
    private @Nullable String currencyDescription;
    private @Nullable String exchangeRateConditionCode;
    private @Nullable BigDecimal exchangeRate;
    private @Nullable String advanceConditionCode;
    private @Nullable String advanceConditionDescription;
    private List<AffectedObligation> affectedObligations = new ArrayList<>();

    private Builder() {}

    private Builder(CommercialOperation source) {
      this.transactionTypeCode = source.transactionTypeCode;
      this.transactionTypeDescription = source.transactionTypeDescription;
      this.taxTypeCode = source.taxTypeCode;
      this.taxTypeDescription = source.taxTypeDescription;
      this.currencyCode = source.currencyCode;
      this.currencyDescription = source.currencyDescription;
      this.exchangeRateConditionCode = source.exchangeRateConditionCode;
      this.exchangeRate = source.exchangeRate;
      this.advanceConditionCode = source.advanceConditionCode;
      this.advanceConditionDescription = source.advanceConditionDescription;
      this.affectedObligations = new ArrayList<>(source.affectedObligations);
    }

    /**
     * Sets {@code transactionTypeCode} (D011, {@code iTipTra}).
     *
     * @param transactionTypeCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder transactionTypeCode(@Nullable String transactionTypeCode) {
      this.transactionTypeCode = transactionTypeCode;
      return this;
    }

    /**
     * Sets {@code transactionTypeDescription} (D012, {@code dDesTipTra}).
     *
     * @param transactionTypeDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder transactionTypeDescription(@Nullable String transactionTypeDescription) {
      this.transactionTypeDescription = transactionTypeDescription;
      return this;
    }

    /**
     * Sets {@code taxTypeCode} (D013, {@code iTImp}).
     *
     * @param taxTypeCode the value
     * @return this builder
     */
    public Builder taxTypeCode(String taxTypeCode) {
      this.taxTypeCode = taxTypeCode;
      return this;
    }

    /**
     * Sets {@code taxTypeDescription} (D014, {@code dDesTImp}).
     *
     * @param taxTypeDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder taxTypeDescription(@Nullable String taxTypeDescription) {
      this.taxTypeDescription = taxTypeDescription;
      return this;
    }

    /**
     * Sets {@code currencyCode} (D015, {@code cMoneOpe}).
     *
     * @param currencyCode the value
     * @return this builder
     */
    public Builder currencyCode(String currencyCode) {
      this.currencyCode = currencyCode;
      return this;
    }

    /**
     * Sets {@code currencyDescription} (D016, {@code dDesMoneOpe}).
     *
     * @param currencyDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder currencyDescription(@Nullable String currencyDescription) {
      this.currencyDescription = currencyDescription;
      return this;
    }

    /**
     * Sets {@code exchangeRateConditionCode} (D017, {@code dCondTiCam}).
     *
     * @param exchangeRateConditionCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder exchangeRateConditionCode(@Nullable String exchangeRateConditionCode) {
      this.exchangeRateConditionCode = exchangeRateConditionCode;
      return this;
    }

    /**
     * Sets {@code exchangeRate} (D018, {@code dTiCam}).
     *
     * @param exchangeRate the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder exchangeRate(@Nullable BigDecimal exchangeRate) {
      this.exchangeRate = exchangeRate;
      return this;
    }

    /**
     * Sets {@code advanceConditionCode} (D019, {@code iCondAnt}).
     *
     * @param advanceConditionCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder advanceConditionCode(@Nullable String advanceConditionCode) {
      this.advanceConditionCode = advanceConditionCode;
      return this;
    }

    /**
     * Sets {@code advanceConditionDescription} (D020, {@code dDesCondAnt}).
     *
     * @param advanceConditionDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder advanceConditionDescription(@Nullable String advanceConditionDescription) {
      this.advanceConditionDescription = advanceConditionDescription;
      return this;
    }

    /**
     * Replaces the list of {@code affectedObligations} (D030, {@code gOblAfe}).
     *
     * @param affectedObligations the elements, in document order
     * @return this builder
     */
    public Builder affectedObligations(List<AffectedObligation> affectedObligations) {
      this.affectedObligations = new ArrayList<>(affectedObligations);
      return this;
    }

    /**
     * Appends one element to {@code affectedObligations} (D030, {@code gOblAfe}).
     *
     * @param affectedObligation the element
     * @return this builder
     */
    public Builder addAffectedObligation(AffectedObligation affectedObligation) {
      this.affectedObligations.add(
          Objects.requireNonNull(affectedObligation, "affectedObligation"));
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public CommercialOperation build() {
      return new CommercialOperation(this);
    }
  }
}
