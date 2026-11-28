/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import io.github.sifensdk.core.catalog.AssociatedDocumentType;
import io.github.sifensdk.core.catalog.CertificateType;
import io.github.sifensdk.core.catalog.PrintedDocumentType;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Campos que identifican al DE asociado.
 *
 * <p>SIFEN group {@code H001} ({@code gCamDEAsoc}). Immutable and thread-safe; create it with
 * {@link #builder()}. The builder only checks what Java needs (required elements and non-null
 * lists); the SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step,
 * never here. Amounts are kept exactly as given.
 */
@SifenField(id = "H001", xml = "gCamDEAsoc")
public final class AssociatedDocument {

  private final String typeCode;
  private final @Nullable String typeDescription;
  private final @Nullable String cdc;
  private final @Nullable String printedStamp;
  private final @Nullable String establishment;
  private final @Nullable String dispatchPoint;
  private final @Nullable String documentNumber;
  private final @Nullable String printedDocumentTypeCode;
  private final @Nullable String printedDocumentTypeDescription;
  private final @Nullable LocalDate printedIssueDate;
  private final @Nullable String withholdingReceiptNumber;
  private final @Nullable String fiscalCreditResolutionNumber;
  private final @Nullable String certificateTypeCode;
  private final @Nullable String certificateTypeDescription;
  private final @Nullable String certificateNumber;
  private final @Nullable String certificateControlNumber;
  private final @Nullable String mergedRuc;

  private AssociatedDocument(Builder b) {
    this.typeCode = Checks.required(b.typeCode, "typeCode", "H002", "iTipDocAso");
    this.typeDescription = b.typeDescription;
    this.cdc = b.cdc;
    this.printedStamp = b.printedStamp;
    this.establishment = b.establishment;
    this.dispatchPoint = b.dispatchPoint;
    this.documentNumber = b.documentNumber;
    this.printedDocumentTypeCode = b.printedDocumentTypeCode;
    this.printedDocumentTypeDescription = b.printedDocumentTypeDescription;
    this.printedIssueDate = b.printedIssueDate;
    this.withholdingReceiptNumber = b.withholdingReceiptNumber;
    this.fiscalCreditResolutionNumber = b.fiscalCreditResolutionNumber;
    this.certificateTypeCode = b.certificateTypeCode;
    this.certificateTypeDescription = b.certificateTypeDescription;
    this.certificateNumber = b.certificateNumber;
    this.certificateControlNumber = b.certificateControlNumber;
    this.mergedRuc = b.mergedRuc;
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
   * Tipo de documento asociado.
   *
   * <p>SIFEN field {@code H002} ({@code iTipDocAso}).
   *
   * @return the value
   */
  @SifenField(id = "H002", xml = "iTipDocAso")
  public String typeCode() {
    return typeCode;
  }

  /**
   * Returns {@link #typeCode()} (H002) as {@link AssociatedDocumentType}.
   *
   * @return the constant, or empty if the code is not set or this SDK version does not know it
   */
  public Optional<AssociatedDocumentType> type() {
    return AssociatedDocumentType.fromCode(typeCode);
  }

  /**
   * Descripción del tipo de documento asociado.
   *
   * <p>SIFEN field {@code H003} ({@code dDesTipDocAso}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code H002}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "H003", xml = "dDesTipDocAso")
  public @Nullable String typeDescription() {
    return typeDescription;
  }

  /**
   * CDC del DTE referenciado.
   *
   * <p>SIFEN field {@code H004} ({@code dCdCDERef}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "H004", xml = "dCdCDERef")
  public @Nullable String cdc() {
    return cdc;
  }

  /**
   * Nro. timbrado documento impreso de referencia.
   *
   * <p>SIFEN field {@code H005} ({@code dNTimDI}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "H005", xml = "dNTimDI")
  public @Nullable String printedStamp() {
    return printedStamp;
  }

  /**
   * Establecimiento.
   *
   * <p>SIFEN field {@code H006} ({@code dEstDocAso}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "H006", xml = "dEstDocAso")
  public @Nullable String establishment() {
    return establishment;
  }

  /**
   * Punto de expedición.
   *
   * <p>SIFEN field {@code H007} ({@code dPExpDocAso}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "H007", xml = "dPExpDocAso")
  public @Nullable String dispatchPoint() {
    return dispatchPoint;
  }

  /**
   * Número del documento.
   *
   * <p>SIFEN field {@code H008} ({@code dNumDocAso}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "H008", xml = "dNumDocAso")
  public @Nullable String documentNumber() {
    return documentNumber;
  }

  /**
   * Tipo de documento impreso.
   *
   * <p>SIFEN field {@code H009} ({@code iTipoDocAso}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "H009", xml = "iTipoDocAso")
  public @Nullable String printedDocumentTypeCode() {
    return printedDocumentTypeCode;
  }

  /**
   * Returns {@link #printedDocumentTypeCode()} (H009) as {@link PrintedDocumentType}.
   *
   * @return the constant, or empty if the code is not set or this SDK version does not know it
   */
  public Optional<PrintedDocumentType> printedDocumentType() {
    return printedDocumentTypeCode == null
        ? Optional.empty()
        : PrintedDocumentType.fromCode(printedDocumentTypeCode);
  }

  /**
   * Descripción del tipo de documento impreso.
   *
   * <p>SIFEN field {@code H010} ({@code dDTipoDocAso}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code H009}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "H010", xml = "dDTipoDocAso")
  public @Nullable String printedDocumentTypeDescription() {
    return printedDocumentTypeDescription;
  }

  /**
   * Fecha de emisión del documento impreso de referencia.
   *
   * <p>SIFEN field {@code H011} ({@code dFecEmiDI}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "H011", xml = "dFecEmiDI")
  public @Nullable LocalDate printedIssueDate() {
    return printedIssueDate;
  }

  /**
   * Número de comprobante de retención.
   *
   * <p>SIFEN field {@code H012} ({@code dNumComRet}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "H012", xml = "dNumComRet")
  public @Nullable String withholdingReceiptNumber() {
    return withholdingReceiptNumber;
  }

  /**
   * Número de resolución de crédito fiscal.
   *
   * <p>SIFEN field {@code H013} ({@code dNumResCF}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "H013", xml = "dNumResCF")
  public @Nullable String fiscalCreditResolutionNumber() {
    return fiscalCreditResolutionNumber;
  }

  /**
   * Tipo de constancia.
   *
   * <p>SIFEN field {@code H014} ({@code iTipCons}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "H014", xml = "iTipCons")
  public @Nullable String certificateTypeCode() {
    return certificateTypeCode;
  }

  /**
   * Returns {@link #certificateTypeCode()} (H014) as {@link CertificateType}.
   *
   * @return the constant, or empty if the code is not set or this SDK version does not know it
   */
  public Optional<CertificateType> certificateType() {
    return certificateTypeCode == null
        ? Optional.empty()
        : CertificateType.fromCode(certificateTypeCode);
  }

  /**
   * Descripción del tipo de constancia.
   *
   * <p>SIFEN field {@code H015} ({@code dDesTipCons}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code H014}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "H015", xml = "dDesTipCons")
  public @Nullable String certificateTypeDescription() {
    return certificateTypeDescription;
  }

  /**
   * Número de constancia.
   *
   * <p>SIFEN field {@code H016} ({@code dNumCons}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "H016", xml = "dNumCons")
  public @Nullable String certificateNumber() {
    return certificateNumber;
  }

  /**
   * Número de control de la constancia.
   *
   * <p>SIFEN field {@code H017} ({@code dNumControl}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "H017", xml = "dNumControl")
  public @Nullable String certificateControlNumber() {
    return certificateControlNumber;
  }

  /**
   * RUC fusionado.
   *
   * <p>SIFEN field {@code H018} ({@code dRucFus}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "H018", xml = "dRucFus")
  public @Nullable String mergedRuc() {
    return mergedRuc;
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
    if (!(other instanceof AssociatedDocument)) {
      return false;
    }
    AssociatedDocument that = (AssociatedDocument) other;
    return Objects.equals(typeCode, that.typeCode)
        && Objects.equals(typeDescription, that.typeDescription)
        && Objects.equals(cdc, that.cdc)
        && Objects.equals(printedStamp, that.printedStamp)
        && Objects.equals(establishment, that.establishment)
        && Objects.equals(dispatchPoint, that.dispatchPoint)
        && Objects.equals(documentNumber, that.documentNumber)
        && Objects.equals(printedDocumentTypeCode, that.printedDocumentTypeCode)
        && Objects.equals(printedDocumentTypeDescription, that.printedDocumentTypeDescription)
        && Objects.equals(printedIssueDate, that.printedIssueDate)
        && Objects.equals(withholdingReceiptNumber, that.withholdingReceiptNumber)
        && Objects.equals(fiscalCreditResolutionNumber, that.fiscalCreditResolutionNumber)
        && Objects.equals(certificateTypeCode, that.certificateTypeCode)
        && Objects.equals(certificateTypeDescription, that.certificateTypeDescription)
        && Objects.equals(certificateNumber, that.certificateNumber)
        && Objects.equals(certificateControlNumber, that.certificateControlNumber)
        && Objects.equals(mergedRuc, that.mergedRuc);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        typeCode,
        typeDescription,
        cdc,
        printedStamp,
        establishment,
        dispatchPoint,
        documentNumber,
        printedDocumentTypeCode,
        printedDocumentTypeDescription,
        printedIssueDate,
        withholdingReceiptNumber,
        fiscalCreditResolutionNumber,
        certificateTypeCode,
        certificateTypeDescription,
        certificateNumber,
        certificateControlNumber,
        mergedRuc);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "AssociatedDocument";
  }

  /**
   * Builder for {@link AssociatedDocument}. Not thread-safe; {@link #build()} returns an immutable
   * copy.
   */
  public static final class Builder {

    private @Nullable String typeCode;
    private @Nullable String typeDescription;
    private @Nullable String cdc;
    private @Nullable String printedStamp;
    private @Nullable String establishment;
    private @Nullable String dispatchPoint;
    private @Nullable String documentNumber;
    private @Nullable String printedDocumentTypeCode;
    private @Nullable String printedDocumentTypeDescription;
    private @Nullable LocalDate printedIssueDate;
    private @Nullable String withholdingReceiptNumber;
    private @Nullable String fiscalCreditResolutionNumber;
    private @Nullable String certificateTypeCode;
    private @Nullable String certificateTypeDescription;
    private @Nullable String certificateNumber;
    private @Nullable String certificateControlNumber;
    private @Nullable String mergedRuc;

    private Builder() {}

    private Builder(AssociatedDocument source) {
      this.typeCode = source.typeCode;
      this.typeDescription = source.typeDescription;
      this.cdc = source.cdc;
      this.printedStamp = source.printedStamp;
      this.establishment = source.establishment;
      this.dispatchPoint = source.dispatchPoint;
      this.documentNumber = source.documentNumber;
      this.printedDocumentTypeCode = source.printedDocumentTypeCode;
      this.printedDocumentTypeDescription = source.printedDocumentTypeDescription;
      this.printedIssueDate = source.printedIssueDate;
      this.withholdingReceiptNumber = source.withholdingReceiptNumber;
      this.fiscalCreditResolutionNumber = source.fiscalCreditResolutionNumber;
      this.certificateTypeCode = source.certificateTypeCode;
      this.certificateTypeDescription = source.certificateTypeDescription;
      this.certificateNumber = source.certificateNumber;
      this.certificateControlNumber = source.certificateControlNumber;
      this.mergedRuc = source.mergedRuc;
    }

    /**
     * Sets {@code typeCode} (H002, {@code iTipDocAso}).
     *
     * @param typeCode the value
     * @return this builder
     */
    public Builder typeCode(String typeCode) {
      this.typeCode = typeCode;
      return this;
    }

    /**
     * Sets {@code typeCode} (H002, {@code iTipDocAso}) from {@link AssociatedDocumentType}.
     *
     * @param type the catalog constant
     * @return this builder
     */
    public Builder type(AssociatedDocumentType type) {
      this.typeCode = type.code();
      return this;
    }

    /**
     * Sets {@code typeDescription} (H003, {@code dDesTipDocAso}).
     *
     * @param typeDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder typeDescription(@Nullable String typeDescription) {
      this.typeDescription = typeDescription;
      return this;
    }

    /**
     * Sets {@code cdc} (H004, {@code dCdCDERef}).
     *
     * @param cdc the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder cdc(@Nullable String cdc) {
      this.cdc = cdc;
      return this;
    }

    /**
     * Sets {@code printedStamp} (H005, {@code dNTimDI}).
     *
     * @param printedStamp the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder printedStamp(@Nullable String printedStamp) {
      this.printedStamp = printedStamp;
      return this;
    }

    /**
     * Sets {@code establishment} (H006, {@code dEstDocAso}).
     *
     * @param establishment the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder establishment(@Nullable String establishment) {
      this.establishment = establishment;
      return this;
    }

    /**
     * Sets {@code dispatchPoint} (H007, {@code dPExpDocAso}).
     *
     * @param dispatchPoint the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder dispatchPoint(@Nullable String dispatchPoint) {
      this.dispatchPoint = dispatchPoint;
      return this;
    }

    /**
     * Sets {@code documentNumber} (H008, {@code dNumDocAso}).
     *
     * @param documentNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder documentNumber(@Nullable String documentNumber) {
      this.documentNumber = documentNumber;
      return this;
    }

    /**
     * Sets {@code printedDocumentTypeCode} (H009, {@code iTipoDocAso}).
     *
     * @param printedDocumentTypeCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder printedDocumentTypeCode(@Nullable String printedDocumentTypeCode) {
      this.printedDocumentTypeCode = printedDocumentTypeCode;
      return this;
    }

    /**
     * Sets {@code printedDocumentTypeCode} (H009, {@code iTipoDocAso}) from {@link
     * PrintedDocumentType}.
     *
     * @param printedDocumentType the catalog constant
     * @return this builder
     */
    public Builder printedDocumentType(@Nullable PrintedDocumentType printedDocumentType) {
      this.printedDocumentTypeCode =
          printedDocumentType == null ? null : printedDocumentType.code();
      return this;
    }

    /**
     * Sets {@code printedDocumentTypeDescription} (H010, {@code dDTipoDocAso}).
     *
     * @param printedDocumentTypeDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder printedDocumentTypeDescription(@Nullable String printedDocumentTypeDescription) {
      this.printedDocumentTypeDescription = printedDocumentTypeDescription;
      return this;
    }

    /**
     * Sets {@code printedIssueDate} (H011, {@code dFecEmiDI}).
     *
     * @param printedIssueDate the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder printedIssueDate(@Nullable LocalDate printedIssueDate) {
      this.printedIssueDate = printedIssueDate;
      return this;
    }

    /**
     * Sets {@code withholdingReceiptNumber} (H012, {@code dNumComRet}).
     *
     * @param withholdingReceiptNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder withholdingReceiptNumber(@Nullable String withholdingReceiptNumber) {
      this.withholdingReceiptNumber = withholdingReceiptNumber;
      return this;
    }

    /**
     * Sets {@code fiscalCreditResolutionNumber} (H013, {@code dNumResCF}).
     *
     * @param fiscalCreditResolutionNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder fiscalCreditResolutionNumber(@Nullable String fiscalCreditResolutionNumber) {
      this.fiscalCreditResolutionNumber = fiscalCreditResolutionNumber;
      return this;
    }

    /**
     * Sets {@code certificateTypeCode} (H014, {@code iTipCons}).
     *
     * @param certificateTypeCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder certificateTypeCode(@Nullable String certificateTypeCode) {
      this.certificateTypeCode = certificateTypeCode;
      return this;
    }

    /**
     * Sets {@code certificateTypeCode} (H014, {@code iTipCons}) from {@link CertificateType}.
     *
     * @param certificateType the catalog constant
     * @return this builder
     */
    public Builder certificateType(@Nullable CertificateType certificateType) {
      this.certificateTypeCode = certificateType == null ? null : certificateType.code();
      return this;
    }

    /**
     * Sets {@code certificateTypeDescription} (H015, {@code dDesTipCons}).
     *
     * @param certificateTypeDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder certificateTypeDescription(@Nullable String certificateTypeDescription) {
      this.certificateTypeDescription = certificateTypeDescription;
      return this;
    }

    /**
     * Sets {@code certificateNumber} (H016, {@code dNumCons}).
     *
     * @param certificateNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder certificateNumber(@Nullable String certificateNumber) {
      this.certificateNumber = certificateNumber;
      return this;
    }

    /**
     * Sets {@code certificateControlNumber} (H017, {@code dNumControl}).
     *
     * @param certificateControlNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder certificateControlNumber(@Nullable String certificateControlNumber) {
      this.certificateControlNumber = certificateControlNumber;
      return this;
    }

    /**
     * Sets {@code mergedRuc} (H018, {@code dRucFus}).
     *
     * @param mergedRuc the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder mergedRuc(@Nullable String mergedRuc) {
      this.mergedRuc = mergedRuc;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public AssociatedDocument build() {
      return new AssociatedDocument(this);
    }
  }
}
