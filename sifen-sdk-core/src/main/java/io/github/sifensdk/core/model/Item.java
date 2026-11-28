/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import io.github.sifensdk.core.catalog.GoodsRelevance;
import io.github.sifensdk.core.catalog.UnitOfMeasure;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Campos que describen los ítems de la operación.
 *
 * <p>SIFEN group {@code E700} ({@code gCamItem}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "E700", xml = "gCamItem")
public final class Item {

  private final String internalCode;
  private final @Nullable String tariffHeading;
  private final @Nullable String mercosurCommonNomenclature;
  private final @Nullable String dncpGeneralCode;
  private final @Nullable String dncpSpecificCode;
  private final @Nullable String productGtin;
  private final @Nullable String packageGtin;
  private final String description;
  private final String unitOfMeasureCode;
  private final @Nullable String unitOfMeasureDescription;
  private final BigDecimal quantity;
  private final @Nullable String originCountryCode;
  private final @Nullable String originCountryDescription;
  private final @Nullable String additionalInformation;
  private final @Nullable String goodsRelevanceCode;
  private final @Nullable String goodsRelevanceDescription;
  private final @Nullable BigDecimal breakageOrShrinkageQuantity;
  private final @Nullable BigDecimal breakageOrShrinkageRate;
  private final @Nullable String advanceCdc;
  private final @Nullable ItemValues values;
  private final @Nullable ItemVat vat;
  private final @Nullable GoodsTracking goodsTracking;
  private final @Nullable NewVehicle newVehicle;

  private Item(Builder b) {
    this.internalCode = Checks.required(b.internalCode, "internalCode", "E701", "dCodInt");
    this.tariffHeading = b.tariffHeading;
    this.mercosurCommonNomenclature = b.mercosurCommonNomenclature;
    this.dncpGeneralCode = b.dncpGeneralCode;
    this.dncpSpecificCode = b.dncpSpecificCode;
    this.productGtin = b.productGtin;
    this.packageGtin = b.packageGtin;
    this.description = Checks.required(b.description, "description", "E708", "dDesProSer");
    this.unitOfMeasureCode =
        Checks.required(b.unitOfMeasureCode, "unitOfMeasureCode", "E709", "cUniMed");
    this.unitOfMeasureDescription = b.unitOfMeasureDescription;
    this.quantity = Checks.required(b.quantity, "quantity", "E711", "dCantProSer");
    this.originCountryCode = b.originCountryCode;
    this.originCountryDescription = b.originCountryDescription;
    this.additionalInformation = b.additionalInformation;
    this.goodsRelevanceCode = b.goodsRelevanceCode;
    this.goodsRelevanceDescription = b.goodsRelevanceDescription;
    this.breakageOrShrinkageQuantity = b.breakageOrShrinkageQuantity;
    this.breakageOrShrinkageRate = b.breakageOrShrinkageRate;
    this.advanceCdc = b.advanceCdc;
    this.values = b.values;
    this.vat = b.vat;
    this.goodsTracking = b.goodsTracking;
    this.newVehicle = b.newVehicle;
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
   * Código interno.
   *
   * <p>SIFEN field {@code E701} ({@code dCodInt}).
   *
   * @return the value
   */
  @SifenField(id = "E701", xml = "dCodInt")
  public String internalCode() {
    return internalCode;
  }

  /**
   * Partida arancelaria.
   *
   * <p>SIFEN field {@code E702} ({@code dParAranc}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E702", xml = "dParAranc")
  public @Nullable String tariffHeading() {
    return tariffHeading;
  }

  /**
   * Nomenclatura común del Mercosur (NCM).
   *
   * <p>SIFEN field {@code E703} ({@code dNCM}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E703", xml = "dNCM")
  public @Nullable String mercosurCommonNomenclature() {
    return mercosurCommonNomenclature;
  }

  /**
   * Código DNCP – Nivel General.
   *
   * <p>SIFEN field {@code E704} ({@code dDncpG}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E704", xml = "dDncpG")
  public @Nullable String dncpGeneralCode() {
    return dncpGeneralCode;
  }

  /**
   * Código DNCP – Nivel Especifico.
   *
   * <p>SIFEN field {@code E705} ({@code dDncpE}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E705", xml = "dDncpE")
  public @Nullable String dncpSpecificCode() {
    return dncpSpecificCode;
  }

  /**
   * Código GTIN por producto.
   *
   * <p>SIFEN field {@code E706} ({@code dGtin}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E706", xml = "dGtin")
  public @Nullable String productGtin() {
    return productGtin;
  }

  /**
   * Código GTIN por paquete.
   *
   * <p>SIFEN field {@code E707} ({@code dGtinPq}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E707", xml = "dGtinPq")
  public @Nullable String packageGtin() {
    return packageGtin;
  }

  /**
   * Descripción del producto y/o servicio.
   *
   * <p>SIFEN field {@code E708} ({@code dDesProSer}).
   *
   * @return the value
   */
  @SifenField(id = "E708", xml = "dDesProSer")
  public String description() {
    return description;
  }

  /**
   * Unidad de medida.
   *
   * <p>SIFEN field {@code E709} ({@code cUniMed}).
   *
   * @return the value
   */
  @SifenField(id = "E709", xml = "cUniMed")
  public String unitOfMeasureCode() {
    return unitOfMeasureCode;
  }

  /**
   * Returns {@link #unitOfMeasureCode()} (E709) as {@link UnitOfMeasure}.
   *
   * @return the constant, or empty if the code is not set or this SDK version does not know it
   */
  public Optional<UnitOfMeasure> unitOfMeasure() {
    return UnitOfMeasure.fromCode(unitOfMeasureCode);
  }

  /**
   * Descripción de la unidad de medida.
   *
   * <p>SIFEN field {@code E710} ({@code dDesUniMed}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E709}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E710", xml = "dDesUniMed")
  public @Nullable String unitOfMeasureDescription() {
    return unitOfMeasureDescription;
  }

  /**
   * Cantidad del producto y/o servicio.
   *
   * <p>SIFEN field {@code E711} ({@code dCantProSer}).
   *
   * @return the value
   */
  @SifenField(id = "E711", xml = "dCantProSer")
  public BigDecimal quantity() {
    return quantity;
  }

  /**
   * Código del país de origen del producto.
   *
   * <p>SIFEN field {@code E712} ({@code cPaisOrig}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E712", xml = "cPaisOrig")
  public @Nullable String originCountryCode() {
    return originCountryCode;
  }

  /**
   * Descripción del país de origen del producto.
   *
   * <p>SIFEN field {@code E713} ({@code dDesPaisOrig}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E712}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E713", xml = "dDesPaisOrig")
  public @Nullable String originCountryDescription() {
    return originCountryDescription;
  }

  /**
   * Información de interés del emisor con respecto al ítem.
   *
   * <p>SIFEN field {@code E714} ({@code dInfItem}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E714", xml = "dInfItem")
  public @Nullable String additionalInformation() {
    return additionalInformation;
  }

  /**
   * Código de datos de relevancia de las mercaderías.
   *
   * <p>SIFEN field {@code E715} ({@code cRelMerc}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E715", xml = "cRelMerc")
  public @Nullable String goodsRelevanceCode() {
    return goodsRelevanceCode;
  }

  /**
   * Returns {@link #goodsRelevanceCode()} (E715) as {@link GoodsRelevance}.
   *
   * @return the constant, or empty if the code is not set or this SDK version does not know it
   */
  public Optional<GoodsRelevance> goodsRelevance() {
    return goodsRelevanceCode == null
        ? Optional.empty()
        : GoodsRelevance.fromCode(goodsRelevanceCode);
  }

  /**
   * Descripción del código de datos de relevancia de las mercaderías.
   *
   * <p>SIFEN field {@code E716} ({@code dDesRelMerc}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E715}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E716", xml = "dDesRelMerc")
  public @Nullable String goodsRelevanceDescription() {
    return goodsRelevanceDescription;
  }

  /**
   * Cantidad de quiebra o merma.
   *
   * <p>SIFEN field {@code E717} ({@code dCanQuiMer}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E717", xml = "dCanQuiMer")
  public @Nullable BigDecimal breakageOrShrinkageQuantity() {
    return breakageOrShrinkageQuantity;
  }

  /**
   * Porcentaje de quiebra o merma.
   *
   * <p>SIFEN field {@code E718} ({@code dPorQuiMer}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E718", xml = "dPorQuiMer")
  public @Nullable BigDecimal breakageOrShrinkageRate() {
    return breakageOrShrinkageRate;
  }

  /**
   * CDC del anticipo.
   *
   * <p>SIFEN field {@code E719} ({@code dCDCAnticipo}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E719", xml = "dCDCAnticipo")
  public @Nullable String advanceCdc() {
    return advanceCdc;
  }

  /**
   * Campos que describen los precios, descuentos y valor total por ítem.
   *
   * <p>SIFEN group {@code E720} ({@code gValorItem}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E720", xml = "gValorItem")
  public @Nullable ItemValues values() {
    return values;
  }

  /**
   * Campos que describen el IVA de la operación.
   *
   * <p>SIFEN group {@code E730} ({@code gCamIVA}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E730", xml = "gCamIVA")
  public @Nullable ItemVat vat() {
    return vat;
  }

  /**
   * Grupo de rastreo de la mercadería.
   *
   * <p>SIFEN group {@code E750} ({@code gRasMerc}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E750", xml = "gRasMerc")
  public @Nullable GoodsTracking goodsTracking() {
    return goodsTracking;
  }

  /**
   * Grupo de detalle de vehículos nuevos.
   *
   * <p>SIFEN group {@code E770} ({@code gVehNuevo}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E770", xml = "gVehNuevo")
  public @Nullable NewVehicle newVehicle() {
    return newVehicle;
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
    if (!(other instanceof Item)) {
      return false;
    }
    Item that = (Item) other;
    return Objects.equals(internalCode, that.internalCode)
        && Objects.equals(tariffHeading, that.tariffHeading)
        && Objects.equals(mercosurCommonNomenclature, that.mercosurCommonNomenclature)
        && Objects.equals(dncpGeneralCode, that.dncpGeneralCode)
        && Objects.equals(dncpSpecificCode, that.dncpSpecificCode)
        && Objects.equals(productGtin, that.productGtin)
        && Objects.equals(packageGtin, that.packageGtin)
        && Objects.equals(description, that.description)
        && Objects.equals(unitOfMeasureCode, that.unitOfMeasureCode)
        && Objects.equals(unitOfMeasureDescription, that.unitOfMeasureDescription)
        && Objects.equals(quantity, that.quantity)
        && Objects.equals(originCountryCode, that.originCountryCode)
        && Objects.equals(originCountryDescription, that.originCountryDescription)
        && Objects.equals(additionalInformation, that.additionalInformation)
        && Objects.equals(goodsRelevanceCode, that.goodsRelevanceCode)
        && Objects.equals(goodsRelevanceDescription, that.goodsRelevanceDescription)
        && Objects.equals(breakageOrShrinkageQuantity, that.breakageOrShrinkageQuantity)
        && Objects.equals(breakageOrShrinkageRate, that.breakageOrShrinkageRate)
        && Objects.equals(advanceCdc, that.advanceCdc)
        && Objects.equals(values, that.values)
        && Objects.equals(vat, that.vat)
        && Objects.equals(goodsTracking, that.goodsTracking)
        && Objects.equals(newVehicle, that.newVehicle);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        internalCode,
        tariffHeading,
        mercosurCommonNomenclature,
        dncpGeneralCode,
        dncpSpecificCode,
        productGtin,
        packageGtin,
        description,
        unitOfMeasureCode,
        unitOfMeasureDescription,
        quantity,
        originCountryCode,
        originCountryDescription,
        additionalInformation,
        goodsRelevanceCode,
        goodsRelevanceDescription,
        breakageOrShrinkageQuantity,
        breakageOrShrinkageRate,
        advanceCdc,
        values,
        vat,
        goodsTracking,
        newVehicle);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "Item";
  }

  /** Builder for {@link Item}. Not thread-safe; {@link #build()} returns an immutable copy. */
  public static final class Builder {

    private @Nullable String internalCode;
    private @Nullable String tariffHeading;
    private @Nullable String mercosurCommonNomenclature;
    private @Nullable String dncpGeneralCode;
    private @Nullable String dncpSpecificCode;
    private @Nullable String productGtin;
    private @Nullable String packageGtin;
    private @Nullable String description;
    private @Nullable String unitOfMeasureCode;
    private @Nullable String unitOfMeasureDescription;
    private @Nullable BigDecimal quantity;
    private @Nullable String originCountryCode;
    private @Nullable String originCountryDescription;
    private @Nullable String additionalInformation;
    private @Nullable String goodsRelevanceCode;
    private @Nullable String goodsRelevanceDescription;
    private @Nullable BigDecimal breakageOrShrinkageQuantity;
    private @Nullable BigDecimal breakageOrShrinkageRate;
    private @Nullable String advanceCdc;
    private @Nullable ItemValues values;
    private @Nullable ItemVat vat;
    private @Nullable GoodsTracking goodsTracking;
    private @Nullable NewVehicle newVehicle;

    private Builder() {}

    private Builder(Item source) {
      this.internalCode = source.internalCode;
      this.tariffHeading = source.tariffHeading;
      this.mercosurCommonNomenclature = source.mercosurCommonNomenclature;
      this.dncpGeneralCode = source.dncpGeneralCode;
      this.dncpSpecificCode = source.dncpSpecificCode;
      this.productGtin = source.productGtin;
      this.packageGtin = source.packageGtin;
      this.description = source.description;
      this.unitOfMeasureCode = source.unitOfMeasureCode;
      this.unitOfMeasureDescription = source.unitOfMeasureDescription;
      this.quantity = source.quantity;
      this.originCountryCode = source.originCountryCode;
      this.originCountryDescription = source.originCountryDescription;
      this.additionalInformation = source.additionalInformation;
      this.goodsRelevanceCode = source.goodsRelevanceCode;
      this.goodsRelevanceDescription = source.goodsRelevanceDescription;
      this.breakageOrShrinkageQuantity = source.breakageOrShrinkageQuantity;
      this.breakageOrShrinkageRate = source.breakageOrShrinkageRate;
      this.advanceCdc = source.advanceCdc;
      this.values = source.values;
      this.vat = source.vat;
      this.goodsTracking = source.goodsTracking;
      this.newVehicle = source.newVehicle;
    }

    /**
     * Sets {@code internalCode} (E701, {@code dCodInt}).
     *
     * @param internalCode the value
     * @return this builder
     */
    public Builder internalCode(String internalCode) {
      this.internalCode = internalCode;
      return this;
    }

    /**
     * Sets {@code tariffHeading} (E702, {@code dParAranc}).
     *
     * @param tariffHeading the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder tariffHeading(@Nullable String tariffHeading) {
      this.tariffHeading = tariffHeading;
      return this;
    }

    /**
     * Sets {@code mercosurCommonNomenclature} (E703, {@code dNCM}).
     *
     * @param mercosurCommonNomenclature the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder mercosurCommonNomenclature(@Nullable String mercosurCommonNomenclature) {
      this.mercosurCommonNomenclature = mercosurCommonNomenclature;
      return this;
    }

    /**
     * Sets {@code dncpGeneralCode} (E704, {@code dDncpG}).
     *
     * @param dncpGeneralCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder dncpGeneralCode(@Nullable String dncpGeneralCode) {
      this.dncpGeneralCode = dncpGeneralCode;
      return this;
    }

    /**
     * Sets {@code dncpSpecificCode} (E705, {@code dDncpE}).
     *
     * @param dncpSpecificCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder dncpSpecificCode(@Nullable String dncpSpecificCode) {
      this.dncpSpecificCode = dncpSpecificCode;
      return this;
    }

    /**
     * Sets {@code productGtin} (E706, {@code dGtin}).
     *
     * @param productGtin the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder productGtin(@Nullable String productGtin) {
      this.productGtin = productGtin;
      return this;
    }

    /**
     * Sets {@code packageGtin} (E707, {@code dGtinPq}).
     *
     * @param packageGtin the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder packageGtin(@Nullable String packageGtin) {
      this.packageGtin = packageGtin;
      return this;
    }

    /**
     * Sets {@code description} (E708, {@code dDesProSer}).
     *
     * @param description the value
     * @return this builder
     */
    public Builder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets {@code unitOfMeasureCode} (E709, {@code cUniMed}).
     *
     * @param unitOfMeasureCode the value
     * @return this builder
     */
    public Builder unitOfMeasureCode(String unitOfMeasureCode) {
      this.unitOfMeasureCode = unitOfMeasureCode;
      return this;
    }

    /**
     * Sets {@code unitOfMeasureCode} (E709, {@code cUniMed}) from {@link UnitOfMeasure}.
     *
     * @param unitOfMeasure the catalog constant
     * @return this builder
     */
    public Builder unitOfMeasure(UnitOfMeasure unitOfMeasure) {
      this.unitOfMeasureCode = unitOfMeasure.code();
      return this;
    }

    /**
     * Sets {@code unitOfMeasureDescription} (E710, {@code dDesUniMed}).
     *
     * @param unitOfMeasureDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder unitOfMeasureDescription(@Nullable String unitOfMeasureDescription) {
      this.unitOfMeasureDescription = unitOfMeasureDescription;
      return this;
    }

    /**
     * Sets {@code quantity} (E711, {@code dCantProSer}).
     *
     * @param quantity the value
     * @return this builder
     */
    public Builder quantity(BigDecimal quantity) {
      this.quantity = quantity;
      return this;
    }

    /**
     * Sets {@code originCountryCode} (E712, {@code cPaisOrig}).
     *
     * @param originCountryCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder originCountryCode(@Nullable String originCountryCode) {
      this.originCountryCode = originCountryCode;
      return this;
    }

    /**
     * Sets {@code originCountryDescription} (E713, {@code dDesPaisOrig}).
     *
     * @param originCountryDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder originCountryDescription(@Nullable String originCountryDescription) {
      this.originCountryDescription = originCountryDescription;
      return this;
    }

    /**
     * Sets {@code additionalInformation} (E714, {@code dInfItem}).
     *
     * @param additionalInformation the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder additionalInformation(@Nullable String additionalInformation) {
      this.additionalInformation = additionalInformation;
      return this;
    }

    /**
     * Sets {@code goodsRelevanceCode} (E715, {@code cRelMerc}).
     *
     * @param goodsRelevanceCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder goodsRelevanceCode(@Nullable String goodsRelevanceCode) {
      this.goodsRelevanceCode = goodsRelevanceCode;
      return this;
    }

    /**
     * Sets {@code goodsRelevanceCode} (E715, {@code cRelMerc}) from {@link GoodsRelevance}.
     *
     * @param goodsRelevance the catalog constant
     * @return this builder
     */
    public Builder goodsRelevance(@Nullable GoodsRelevance goodsRelevance) {
      this.goodsRelevanceCode = goodsRelevance == null ? null : goodsRelevance.code();
      return this;
    }

    /**
     * Sets {@code goodsRelevanceDescription} (E716, {@code dDesRelMerc}).
     *
     * @param goodsRelevanceDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder goodsRelevanceDescription(@Nullable String goodsRelevanceDescription) {
      this.goodsRelevanceDescription = goodsRelevanceDescription;
      return this;
    }

    /**
     * Sets {@code breakageOrShrinkageQuantity} (E717, {@code dCanQuiMer}).
     *
     * @param breakageOrShrinkageQuantity the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder breakageOrShrinkageQuantity(@Nullable BigDecimal breakageOrShrinkageQuantity) {
      this.breakageOrShrinkageQuantity = breakageOrShrinkageQuantity;
      return this;
    }

    /**
     * Sets {@code breakageOrShrinkageRate} (E718, {@code dPorQuiMer}).
     *
     * @param breakageOrShrinkageRate the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder breakageOrShrinkageRate(@Nullable BigDecimal breakageOrShrinkageRate) {
      this.breakageOrShrinkageRate = breakageOrShrinkageRate;
      return this;
    }

    /**
     * Sets {@code advanceCdc} (E719, {@code dCDCAnticipo}).
     *
     * @param advanceCdc the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder advanceCdc(@Nullable String advanceCdc) {
      this.advanceCdc = advanceCdc;
      return this;
    }

    /**
     * Sets {@code values} (E720, {@code gValorItem}).
     *
     * @param values the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder values(@Nullable ItemValues values) {
      this.values = values;
      return this;
    }

    /**
     * Sets {@code vat} (E730, {@code gCamIVA}).
     *
     * @param vat the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder vat(@Nullable ItemVat vat) {
      this.vat = vat;
      return this;
    }

    /**
     * Sets {@code goodsTracking} (E750, {@code gRasMerc}).
     *
     * @param goodsTracking the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder goodsTracking(@Nullable GoodsTracking goodsTracking) {
      this.goodsTracking = goodsTracking;
      return this;
    }

    /**
     * Sets {@code newVehicle} (E770, {@code gVehNuevo}).
     *
     * @param newVehicle the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder newVehicle(@Nullable NewVehicle newVehicle) {
      this.newVehicle = newVehicle;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public Item build() {
      return new Item(this);
    }
  }
}
