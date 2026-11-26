/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.time.LocalDate;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Grupo de rastreo de la mercadería.
 *
 * <p>SIFEN group {@code E750} ({@code gRasMerc}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "E750", xml = "gRasMerc")
public final class GoodsTracking {

  private final @Nullable String lotNumber;
  private final @Nullable LocalDate expirationDate;
  private final @Nullable String serialNumber;
  private final @Nullable String orderNumber;
  private final @Nullable String shipmentTrackingNumber;
  private final @Nullable String productRegistrationNumber;
  private final @Nullable String commercialEntityRegistrationNumber;
  private final @Nullable String productName;

  private GoodsTracking(Builder b) {
    this.lotNumber = b.lotNumber;
    this.expirationDate = b.expirationDate;
    this.serialNumber = b.serialNumber;
    this.orderNumber = b.orderNumber;
    this.shipmentTrackingNumber = b.shipmentTrackingNumber;
    this.productRegistrationNumber = b.productRegistrationNumber;
    this.commercialEntityRegistrationNumber = b.commercialEntityRegistrationNumber;
    this.productName = b.productName;
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
   * Número de lote.
   *
   * <p>SIFEN field {@code E751} ({@code dNumLote}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E751", xml = "dNumLote")
  public @Nullable String lotNumber() {
    return lotNumber;
  }

  /**
   * Fecha de vencimiento de la mercadería.
   *
   * <p>SIFEN field {@code E752} ({@code dVencMerc}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E752", xml = "dVencMerc")
  public @Nullable LocalDate expirationDate() {
    return expirationDate;
  }

  /**
   * Número de serie.
   *
   * <p>SIFEN field {@code E753} ({@code dNSerie}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E753", xml = "dNSerie")
  public @Nullable String serialNumber() {
    return serialNumber;
  }

  /**
   * Número de pedido.
   *
   * <p>SIFEN field {@code E754} ({@code dNumPedi}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E754", xml = "dNumPedi")
  public @Nullable String orderNumber() {
    return orderNumber;
  }

  /**
   * Número de seguimiento del envío.
   *
   * <p>SIFEN field {@code E755} ({@code dNumSegui}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E755", xml = "dNumSegui")
  public @Nullable String shipmentTrackingNumber() {
    return shipmentTrackingNumber;
  }

  /**
   * Número de registro del producto otorgado por el SENAVE.
   *
   * <p>SIFEN field {@code E759} ({@code dNumReg}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E759", xml = "dNumReg")
  public @Nullable String productRegistrationNumber() {
    return productRegistrationNumber;
  }

  /**
   * Número de registro de entidad comercial otorgado por el SENAVE.
   *
   * <p>SIFEN field {@code E760} ({@code dNumRegEntCom}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E760", xml = "dNumRegEntCom")
  public @Nullable String commercialEntityRegistrationNumber() {
    return commercialEntityRegistrationNumber;
  }

  /**
   * Nombre del Producto.
   *
   * <p>SIFEN field {@code E761} ({@code dNomPro}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E761", xml = "dNomPro")
  public @Nullable String productName() {
    return productName;
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
    if (!(other instanceof GoodsTracking)) {
      return false;
    }
    GoodsTracking that = (GoodsTracking) other;
    return Objects.equals(lotNumber, that.lotNumber)
        && Objects.equals(expirationDate, that.expirationDate)
        && Objects.equals(serialNumber, that.serialNumber)
        && Objects.equals(orderNumber, that.orderNumber)
        && Objects.equals(shipmentTrackingNumber, that.shipmentTrackingNumber)
        && Objects.equals(productRegistrationNumber, that.productRegistrationNumber)
        && Objects.equals(
            commercialEntityRegistrationNumber, that.commercialEntityRegistrationNumber)
        && Objects.equals(productName, that.productName);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        lotNumber,
        expirationDate,
        serialNumber,
        orderNumber,
        shipmentTrackingNumber,
        productRegistrationNumber,
        commercialEntityRegistrationNumber,
        productName);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "GoodsTracking";
  }

  /**
   * Builder for {@link GoodsTracking}. Not thread-safe; {@link #build()} returns an immutable copy.
   */
  public static final class Builder {

    private @Nullable String lotNumber;
    private @Nullable LocalDate expirationDate;
    private @Nullable String serialNumber;
    private @Nullable String orderNumber;
    private @Nullable String shipmentTrackingNumber;
    private @Nullable String productRegistrationNumber;
    private @Nullable String commercialEntityRegistrationNumber;
    private @Nullable String productName;

    private Builder() {}

    private Builder(GoodsTracking source) {
      this.lotNumber = source.lotNumber;
      this.expirationDate = source.expirationDate;
      this.serialNumber = source.serialNumber;
      this.orderNumber = source.orderNumber;
      this.shipmentTrackingNumber = source.shipmentTrackingNumber;
      this.productRegistrationNumber = source.productRegistrationNumber;
      this.commercialEntityRegistrationNumber = source.commercialEntityRegistrationNumber;
      this.productName = source.productName;
    }

    /**
     * Sets {@code lotNumber} (E751, {@code dNumLote}).
     *
     * @param lotNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder lotNumber(@Nullable String lotNumber) {
      this.lotNumber = lotNumber;
      return this;
    }

    /**
     * Sets {@code expirationDate} (E752, {@code dVencMerc}).
     *
     * @param expirationDate the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder expirationDate(@Nullable LocalDate expirationDate) {
      this.expirationDate = expirationDate;
      return this;
    }

    /**
     * Sets {@code serialNumber} (E753, {@code dNSerie}).
     *
     * @param serialNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder serialNumber(@Nullable String serialNumber) {
      this.serialNumber = serialNumber;
      return this;
    }

    /**
     * Sets {@code orderNumber} (E754, {@code dNumPedi}).
     *
     * @param orderNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder orderNumber(@Nullable String orderNumber) {
      this.orderNumber = orderNumber;
      return this;
    }

    /**
     * Sets {@code shipmentTrackingNumber} (E755, {@code dNumSegui}).
     *
     * @param shipmentTrackingNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder shipmentTrackingNumber(@Nullable String shipmentTrackingNumber) {
      this.shipmentTrackingNumber = shipmentTrackingNumber;
      return this;
    }

    /**
     * Sets {@code productRegistrationNumber} (E759, {@code dNumReg}).
     *
     * @param productRegistrationNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder productRegistrationNumber(@Nullable String productRegistrationNumber) {
      this.productRegistrationNumber = productRegistrationNumber;
      return this;
    }

    /**
     * Sets {@code commercialEntityRegistrationNumber} (E760, {@code dNumRegEntCom}).
     *
     * @param commercialEntityRegistrationNumber the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder commercialEntityRegistrationNumber(
        @Nullable String commercialEntityRegistrationNumber) {
      this.commercialEntityRegistrationNumber = commercialEntityRegistrationNumber;
      return this;
    }

    /**
     * Sets {@code productName} (E761, {@code dNomPro}).
     *
     * @param productName the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder productName(@Nullable String productName) {
      this.productName = productName;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     */
    public GoodsTracking build() {
      return new GoodsTracking(this);
    }
  }
}
