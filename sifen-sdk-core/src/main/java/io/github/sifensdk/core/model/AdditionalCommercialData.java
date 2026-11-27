/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Grupo de datos adicionales de uso comercial.
 *
 * <p>SIFEN group {@code E820} ({@code gGrupAdi}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "E820", xml = "gGrupAdi")
public final class AdditionalCommercialData {

  private final @Nullable String cycle;
  private final @Nullable LocalDate cycleStartDate;
  private final @Nullable LocalDate cycleEndDate;
  private final List<LocalDate> paymentDueDates;
  private final @Nullable String contractNumber;
  private final @Nullable BigDecimal previousBalanceAmount;
  private final @Nullable String dncpProcurementCode;

  private AdditionalCommercialData(Builder b) {
    this.cycle = b.cycle;
    this.cycleStartDate = b.cycleStartDate;
    this.cycleEndDate = b.cycleEndDate;
    this.paymentDueDates = List.copyOf(b.paymentDueDates);
    this.contractNumber = b.contractNumber;
    this.previousBalanceAmount = b.previousBalanceAmount;
    this.dncpProcurementCode = b.dncpProcurementCode;
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
   * Ciclo.
   *
   * <p>SIFEN field {@code E821} ({@code dCiclo}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E821", xml = "dCiclo")
  public @Nullable String cycle() {
    return cycle;
  }

  /**
   * Fecha de inicio de ciclo.
   *
   * <p>SIFEN field {@code E822} ({@code dFecIniC}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E822", xml = "dFecIniC")
  public @Nullable LocalDate cycleStartDate() {
    return cycleStartDate;
  }

  /**
   * Fecha de fin de ciclo.
   *
   * <p>SIFEN field {@code E823} ({@code dFecFinC}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E823", xml = "dFecFinC")
  public @Nullable LocalDate cycleEndDate() {
    return cycleEndDate;
  }

  /**
   * Fecha de vencimiento para el pago. Up to three.
   *
   * <p>SIFEN field {@code E824} ({@code dVencPag}).
   *
   * @return an unmodifiable list, possibly empty
   */
  @SifenField(id = "E824", xml = "dVencPag")
  public List<LocalDate> paymentDueDates() {
    return paymentDueDates;
  }

  /**
   * Número de contrato.
   *
   * <p>SIFEN field {@code E825} ({@code dContrato}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E825", xml = "dContrato")
  public @Nullable String contractNumber() {
    return contractNumber;
  }

  /**
   * Saldo anterior.
   *
   * <p>SIFEN field {@code E826} ({@code dSalAnt}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E826", xml = "dSalAnt")
  public @Nullable BigDecimal previousBalanceAmount() {
    return previousBalanceAmount;
  }

  /**
   * Código de contratación de la DNCP (NT 20).
   *
   * <p>SIFEN field {@code E827} ({@code dCodConDncp}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E827", xml = "dCodConDncp")
  public @Nullable String dncpProcurementCode() {
    return dncpProcurementCode;
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
    if (!(other instanceof AdditionalCommercialData)) {
      return false;
    }
    AdditionalCommercialData that = (AdditionalCommercialData) other;
    return Objects.equals(cycle, that.cycle)
        && Objects.equals(cycleStartDate, that.cycleStartDate)
        && Objects.equals(cycleEndDate, that.cycleEndDate)
        && Objects.equals(paymentDueDates, that.paymentDueDates)
        && Objects.equals(contractNumber, that.contractNumber)
        && Objects.equals(previousBalanceAmount, that.previousBalanceAmount)
        && Objects.equals(dncpProcurementCode, that.dncpProcurementCode);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        cycle,
        cycleStartDate,
        cycleEndDate,
        paymentDueDates,
        contractNumber,
        previousBalanceAmount,
        dncpProcurementCode);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "AdditionalCommercialData[paymentDueDates=" + paymentDueDates.size() + "]";
  }

  /**
   * Builder for {@link AdditionalCommercialData}. Not thread-safe; {@link #build()} returns an
   * immutable copy.
   */
  public static final class Builder {

    private @Nullable String cycle;
    private @Nullable LocalDate cycleStartDate;
    private @Nullable LocalDate cycleEndDate;
    private List<LocalDate> paymentDueDates = new ArrayList<>();
    private @Nullable String contractNumber;
    private @Nullable BigDecimal previousBalanceAmount;
    private @Nullable String dncpProcurementCode;

    private Builder() {}

    private Builder(AdditionalCommercialData source) {
      this.cycle = source.cycle;
      this.cycleStartDate = source.cycleStartDate;
      this.cycleEndDate = source.cycleEndDate;
      this.paymentDueDates = new ArrayList<>(source.paymentDueDates);
      this.contractNumber = source.contractNumber;
      this.previousBalanceAmount = source.previousBalanceAmount;
      this.dncpProcurementCode = source.dncpProcurementCode;
    }

    /**
     * Sets {@code cycle} (E821, {@code dCiclo}).
     *
     * @param cycle the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder cycle(@Nullable String cycle) {
      this.cycle = cycle;
      return this;
    }

    /**
     * Sets {@code cycleStartDate} (E822, {@code dFecIniC}).
     *
     * @param cycleStartDate the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder cycleStartDate(@Nullable LocalDate cycleStartDate) {
      this.cycleStartDate = cycleStartDate;
      return this;
    }

    /**
     * Sets {@code cycleEndDate} (E823, {@code dFecFinC}).
     *
     * @param cycleEndDate the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder cycleEndDate(@Nullable LocalDate cycleEndDate) {
      this.cycleEndDate = cycleEndDate;
      return this;
    }

    /**
     * Replaces the list of {@code paymentDueDates} (E824, {@code dVencPag}).
     *
     * @param paymentDueDates the elements, in document order
     * @return this builder
     */
    public Builder paymentDueDates(List<LocalDate> paymentDueDates) {
      this.paymentDueDates = new ArrayList<>(paymentDueDates);
      return this;
    }

    /**
     * Appends one element to {@code paymentDueDates} (E824, {@code dVencPag}).
     *
     * @param paymentDueDate the element
     * @return this builder
     */
    public Builder addPaymentDueDate(LocalDate paymentDueDate) {
      this.paymentDueDates.add(Objects.requireNonNull(paymentDueDate, "paymentDueDate"));
      return this;
    }

    /**
     * Sets {@code contractNumber} (E825, {@code dContrato}).
     *
     * @param contractNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder contractNumber(@Nullable String contractNumber) {
      this.contractNumber = contractNumber;
      return this;
    }

    /**
     * Sets {@code previousBalanceAmount} (E826, {@code dSalAnt}).
     *
     * @param previousBalanceAmount the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder previousBalanceAmount(@Nullable BigDecimal previousBalanceAmount) {
      this.previousBalanceAmount = previousBalanceAmount;
      return this;
    }

    /**
     * Sets {@code dncpProcurementCode} (E827, {@code dCodConDncp}).
     *
     * @param dncpProcurementCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder dncpProcurementCode(@Nullable String dncpProcurementCode) {
      this.dncpProcurementCode = dncpProcurementCode;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     */
    public AdditionalCommercialData build() {
      return new AdditionalCommercialData(this);
    }
  }
}
