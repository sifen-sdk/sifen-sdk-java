/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Campos de uso general.
 *
 * <p>SIFEN group {@code G001} ({@code gCamGen}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "G001", xml = "gCamGen")
public final class ComplementaryData {

  private final @Nullable String purchaseOrderNumber;
  private final @Nullable String salesOrderNumber;
  private final @Nullable String accountingEntryNumber;
  private final @Nullable Cargo cargo;

  private ComplementaryData(Builder b) {
    this.purchaseOrderNumber = b.purchaseOrderNumber;
    this.salesOrderNumber = b.salesOrderNumber;
    this.accountingEntryNumber = b.accountingEntryNumber;
    this.cargo = b.cargo;
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
   * Número de orden de compra.
   *
   * <p>SIFEN field {@code G002} ({@code dOrdCompra}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "G002", xml = "dOrdCompra")
  public @Nullable String purchaseOrderNumber() {
    return purchaseOrderNumber;
  }

  /**
   * Número de orden de venta.
   *
   * <p>SIFEN field {@code G003} ({@code dOrdVta}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "G003", xml = "dOrdVta")
  public @Nullable String salesOrderNumber() {
    return salesOrderNumber;
  }

  /**
   * Número de asiento contable.
   *
   * <p>SIFEN field {@code G004} ({@code dAsiento}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "G004", xml = "dAsiento")
  public @Nullable String accountingEntryNumber() {
    return accountingEntryNumber;
  }

  /**
   * Campos generales de la carga.
   *
   * <p>SIFEN group {@code G050} ({@code gCamCarg}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "G050", xml = "gCamCarg")
  public @Nullable Cargo cargo() {
    return cargo;
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
    if (!(other instanceof ComplementaryData)) {
      return false;
    }
    ComplementaryData that = (ComplementaryData) other;
    return Objects.equals(purchaseOrderNumber, that.purchaseOrderNumber)
        && Objects.equals(salesOrderNumber, that.salesOrderNumber)
        && Objects.equals(accountingEntryNumber, that.accountingEntryNumber)
        && Objects.equals(cargo, that.cargo);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(purchaseOrderNumber, salesOrderNumber, accountingEntryNumber, cargo);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "ComplementaryData";
  }

  /**
   * Builder for {@link ComplementaryData}. Not thread-safe; {@link #build()} returns an immutable
   * copy.
   */
  public static final class Builder {

    private @Nullable String purchaseOrderNumber;
    private @Nullable String salesOrderNumber;
    private @Nullable String accountingEntryNumber;
    private @Nullable Cargo cargo;

    private Builder() {}

    private Builder(ComplementaryData source) {
      this.purchaseOrderNumber = source.purchaseOrderNumber;
      this.salesOrderNumber = source.salesOrderNumber;
      this.accountingEntryNumber = source.accountingEntryNumber;
      this.cargo = source.cargo;
    }

    /**
     * Sets {@code purchaseOrderNumber} (G002, {@code dOrdCompra}).
     *
     * @param purchaseOrderNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder purchaseOrderNumber(@Nullable String purchaseOrderNumber) {
      this.purchaseOrderNumber = purchaseOrderNumber;
      return this;
    }

    /**
     * Sets {@code salesOrderNumber} (G003, {@code dOrdVta}).
     *
     * @param salesOrderNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder salesOrderNumber(@Nullable String salesOrderNumber) {
      this.salesOrderNumber = salesOrderNumber;
      return this;
    }

    /**
     * Sets {@code accountingEntryNumber} (G004, {@code dAsiento}).
     *
     * @param accountingEntryNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder accountingEntryNumber(@Nullable String accountingEntryNumber) {
      this.accountingEntryNumber = accountingEntryNumber;
      return this;
    }

    /**
     * Sets {@code cargo} (G050, {@code gCamCarg}).
     *
     * @param cargo the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder cargo(@Nullable Cargo cargo) {
      this.cargo = cargo;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     */
    public ComplementaryData build() {
      return new ComplementaryData(this);
    }
  }
}
