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
 * Campos firmados del DE.
 *
 * <p>SIFEN group {@code A001} ({@code DE}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 *
 * <p>The SDK completes what it can derive (ADR-0011): the format version ({@code dVerFor}), the CDC
 * ({@code Id}) and its check digit ({@code dDVId}), the signature date and time ({@code
 * dFecFirma}), {@code dSisFact} and the catalog descriptions. This version models the electronic
 * invoice (C002 = 1) and the electronic credit note (C002 = 5).
 */
@SifenField(id = "A001", xml = "DE")
public final class ElectronicDocument {

  private final Operation operation;
  private final Stamp stamp;
  private final GeneralData generalData;
  private final DocumentTypeData documentTypeData;
  private final @Nullable Totals totals;
  private final @Nullable ComplementaryData complementaryData;
  private final List<AssociatedDocument> associatedDocuments;

  private ElectronicDocument(Builder b) {
    this.operation = Checks.required(b.operation, "operation", "B001", "gOpeDE");
    this.stamp = Checks.required(b.stamp, "stamp", "C001", "gTimb");
    this.generalData = Checks.required(b.generalData, "generalData", "D001", "gDatGralOpe");
    this.documentTypeData =
        Checks.required(b.documentTypeData, "documentTypeData", "E001", "gDtipDE");
    this.totals = b.totals;
    this.complementaryData = b.complementaryData;
    this.associatedDocuments = List.copyOf(b.associatedDocuments);
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
   * Campos inherentes a la operación de DE.
   *
   * <p>SIFEN group {@code B001} ({@code gOpeDE}).
   *
   * @return the value
   */
  @SifenField(id = "B001", xml = "gOpeDE")
  public Operation operation() {
    return operation;
  }

  /**
   * Datos del timbrado.
   *
   * <p>SIFEN group {@code C001} ({@code gTimb}).
   *
   * @return the value
   */
  @SifenField(id = "C001", xml = "gTimb")
  public Stamp stamp() {
    return stamp;
  }

  /**
   * Campos generales del DE.
   *
   * <p>SIFEN group {@code D001} ({@code gDatGralOpe}).
   *
   * @return the value
   */
  @SifenField(id = "D001", xml = "gDatGralOpe")
  public GeneralData generalData() {
    return generalData;
  }

  /**
   * Campos específicos por tipo de Documento Electrónico.
   *
   * <p>SIFEN group {@code E001} ({@code gDtipDE}).
   *
   * @return the value
   */
  @SifenField(id = "E001", xml = "gDtipDE")
  public DocumentTypeData documentTypeData() {
    return documentTypeData;
  }

  /**
   * Campos de subtotales y totales.
   *
   * <p>SIFEN group {@code F001} ({@code gTotSub}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "F001", xml = "gTotSub")
  public @Nullable Totals totals() {
    return totals;
  }

  /**
   * Campos de uso general.
   *
   * <p>SIFEN group {@code G001} ({@code gCamGen}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "G001", xml = "gCamGen")
  public @Nullable ComplementaryData complementaryData() {
    return complementaryData;
  }

  /**
   * Campos que identifican al DE asociado.
   *
   * <p>SIFEN group {@code H001} ({@code gCamDEAsoc}).
   *
   * @return an unmodifiable list, possibly empty
   */
  @SifenField(id = "H001", xml = "gCamDEAsoc")
  public List<AssociatedDocument> associatedDocuments() {
    return associatedDocuments;
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
    if (!(other instanceof ElectronicDocument)) {
      return false;
    }
    ElectronicDocument that = (ElectronicDocument) other;
    return Objects.equals(operation, that.operation)
        && Objects.equals(stamp, that.stamp)
        && Objects.equals(generalData, that.generalData)
        && Objects.equals(documentTypeData, that.documentTypeData)
        && Objects.equals(totals, that.totals)
        && Objects.equals(complementaryData, that.complementaryData)
        && Objects.equals(associatedDocuments, that.associatedDocuments);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        operation,
        stamp,
        generalData,
        documentTypeData,
        totals,
        complementaryData,
        associatedDocuments);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "ElectronicDocument[items="
        + documentTypeData.items().size()
        + ", associatedDocuments="
        + associatedDocuments.size()
        + "]";
  }

  /**
   * Builder for {@link ElectronicDocument}. Not thread-safe; {@link #build()} returns an immutable
   * copy.
   */
  public static final class Builder {

    private @Nullable Operation operation;
    private @Nullable Stamp stamp;
    private @Nullable GeneralData generalData;
    private @Nullable DocumentTypeData documentTypeData;
    private @Nullable Totals totals;
    private @Nullable ComplementaryData complementaryData;
    private List<AssociatedDocument> associatedDocuments = new ArrayList<>();

    private Builder() {}

    private Builder(ElectronicDocument source) {
      this.operation = source.operation;
      this.stamp = source.stamp;
      this.generalData = source.generalData;
      this.documentTypeData = source.documentTypeData;
      this.totals = source.totals;
      this.complementaryData = source.complementaryData;
      this.associatedDocuments = new ArrayList<>(source.associatedDocuments);
    }

    /**
     * Sets {@code operation} (B001, {@code gOpeDE}).
     *
     * @param operation the value
     * @return this builder
     */
    public Builder operation(Operation operation) {
      this.operation = operation;
      return this;
    }

    /**
     * Sets {@code stamp} (C001, {@code gTimb}).
     *
     * @param stamp the value
     * @return this builder
     */
    public Builder stamp(Stamp stamp) {
      this.stamp = stamp;
      return this;
    }

    /**
     * Sets {@code generalData} (D001, {@code gDatGralOpe}).
     *
     * @param generalData the value
     * @return this builder
     */
    public Builder generalData(GeneralData generalData) {
      this.generalData = generalData;
      return this;
    }

    /**
     * Sets {@code documentTypeData} (E001, {@code gDtipDE}).
     *
     * @param documentTypeData the value
     * @return this builder
     */
    public Builder documentTypeData(DocumentTypeData documentTypeData) {
      this.documentTypeData = documentTypeData;
      return this;
    }

    /**
     * Sets {@code totals} (F001, {@code gTotSub}).
     *
     * @param totals the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder totals(@Nullable Totals totals) {
      this.totals = totals;
      return this;
    }

    /**
     * Sets {@code complementaryData} (G001, {@code gCamGen}).
     *
     * @param complementaryData the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder complementaryData(@Nullable ComplementaryData complementaryData) {
      this.complementaryData = complementaryData;
      return this;
    }

    /**
     * Replaces the list of {@code associatedDocuments} (H001, {@code gCamDEAsoc}).
     *
     * @param associatedDocuments the elements, in document order
     * @return this builder
     */
    public Builder associatedDocuments(List<AssociatedDocument> associatedDocuments) {
      this.associatedDocuments = new ArrayList<>(associatedDocuments);
      return this;
    }

    /**
     * Appends one element to {@code associatedDocuments} (H001, {@code gCamDEAsoc}).
     *
     * @param associatedDocument the element
     * @return this builder
     */
    public Builder addAssociatedDocument(AssociatedDocument associatedDocument) {
      this.associatedDocuments.add(
          Objects.requireNonNull(associatedDocument, "associatedDocument"));
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public ElectronicDocument build() {
      return new ElectronicDocument(this);
    }
  }
}
