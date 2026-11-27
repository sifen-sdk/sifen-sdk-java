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
 * Campos específicos por tipo de Documento Electrónico.
 *
 * <p>SIFEN group {@code E001} ({@code gDtipDE}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "E001", xml = "gDtipDE")
public final class DocumentTypeData {

  private final @Nullable Invoice invoice;
  private final @Nullable SelfInvoice selfInvoice;
  private final @Nullable CreditDebitNote creditDebitNote;
  private final @Nullable OperationCondition operationCondition;
  private final List<Item> items;

  private DocumentTypeData(Builder b) {
    this.invoice = b.invoice;
    this.selfInvoice = b.selfInvoice;
    this.creditDebitNote = b.creditDebitNote;
    this.operationCondition = b.operationCondition;
    this.items = List.copyOf(b.items);
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
   * Campos que componen la FE.
   *
   * <p>SIFEN group {@code E010} ({@code gCamFE}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E010", xml = "gCamFE")
  public @Nullable Invoice invoice() {
    return invoice;
  }

  /**
   * Campos que componen la Autofactura Electrónica.
   *
   * <p>SIFEN group {@code E300} ({@code gCamAE}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E300", xml = "gCamAE")
  public @Nullable SelfInvoice selfInvoice() {
    return selfInvoice;
  }

  /**
   * Campos de la Nota de Crédito/Débito Electrónica.
   *
   * <p>SIFEN group {@code E400} ({@code gCamNCDE}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E400", xml = "gCamNCDE")
  public @Nullable CreditDebitNote creditDebitNote() {
    return creditDebitNote;
  }

  /**
   * Campos que describen la condición de la operación.
   *
   * <p>SIFEN group {@code E600} ({@code gCamCond}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E600", xml = "gCamCond")
  public @Nullable OperationCondition operationCondition() {
    return operationCondition;
  }

  /**
   * Campos que describen los ítems de la operación.
   *
   * <p>SIFEN group {@code E700} ({@code gCamItem}).
   *
   * @return an unmodifiable list, possibly empty
   */
  @SifenField(id = "E700", xml = "gCamItem")
  public List<Item> items() {
    return items;
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
    if (!(other instanceof DocumentTypeData)) {
      return false;
    }
    DocumentTypeData that = (DocumentTypeData) other;
    return Objects.equals(invoice, that.invoice)
        && Objects.equals(selfInvoice, that.selfInvoice)
        && Objects.equals(creditDebitNote, that.creditDebitNote)
        && Objects.equals(operationCondition, that.operationCondition)
        && Objects.equals(items, that.items);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(invoice, selfInvoice, creditDebitNote, operationCondition, items);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "DocumentTypeData[items=" + items.size() + "]";
  }

  /**
   * Builder for {@link DocumentTypeData}. Not thread-safe; {@link #build()} returns an immutable
   * copy.
   */
  public static final class Builder {

    private @Nullable Invoice invoice;
    private @Nullable SelfInvoice selfInvoice;
    private @Nullable CreditDebitNote creditDebitNote;
    private @Nullable OperationCondition operationCondition;
    private List<Item> items = new ArrayList<>();

    private Builder() {}

    private Builder(DocumentTypeData source) {
      this.invoice = source.invoice;
      this.selfInvoice = source.selfInvoice;
      this.creditDebitNote = source.creditDebitNote;
      this.operationCondition = source.operationCondition;
      this.items = new ArrayList<>(source.items);
    }

    /**
     * Sets {@code invoice} (E010, {@code gCamFE}).
     *
     * @param invoice the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder invoice(@Nullable Invoice invoice) {
      this.invoice = invoice;
      return this;
    }

    /**
     * Sets {@code selfInvoice} (E300, {@code gCamAE}).
     *
     * @param selfInvoice the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder selfInvoice(@Nullable SelfInvoice selfInvoice) {
      this.selfInvoice = selfInvoice;
      return this;
    }

    /**
     * Sets {@code creditDebitNote} (E400, {@code gCamNCDE}).
     *
     * @param creditDebitNote the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder creditDebitNote(@Nullable CreditDebitNote creditDebitNote) {
      this.creditDebitNote = creditDebitNote;
      return this;
    }

    /**
     * Sets {@code operationCondition} (E600, {@code gCamCond}).
     *
     * @param operationCondition the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder operationCondition(@Nullable OperationCondition operationCondition) {
      this.operationCondition = operationCondition;
      return this;
    }

    /**
     * Replaces the list of {@code items} (E700, {@code gCamItem}).
     *
     * @param items the elements, in document order
     * @return this builder
     */
    public Builder items(List<Item> items) {
      this.items = new ArrayList<>(items);
      return this;
    }

    /**
     * Appends one element to {@code items} (E700, {@code gCamItem}).
     *
     * @param item the element
     * @return this builder
     */
    public Builder addItem(Item item) {
      this.items.add(Objects.requireNonNull(item, "item"));
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     */
    public DocumentTypeData build() {
      return new DocumentTypeData(this);
    }
  }
}
