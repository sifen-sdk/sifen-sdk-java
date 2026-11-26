/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Campos de la Nota de Crédito/Débito Electrónica.
 *
 * <p>SIFEN group {@code E400} ({@code gCamNCDE}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "E400", xml = "gCamNCDE")
public final class CreditDebitNote {

  private final String reasonCode;
  private final @Nullable String reasonDescription;

  private CreditDebitNote(Builder b) {
    this.reasonCode = Checks.required(b.reasonCode, "reasonCode", "E401", "iMotEmi");
    this.reasonDescription = b.reasonDescription;
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
   * Motivo de emisión.
   *
   * <p>SIFEN field {@code E401} ({@code iMotEmi}).
   *
   * @return the value
   */
  @SifenField(id = "E401", xml = "iMotEmi")
  public String reasonCode() {
    return reasonCode;
  }

  /**
   * Descripción del motivo de emisión.
   *
   * <p>SIFEN field {@code E402} ({@code dDesMotEmi}).
   *
   * <p>Normally left empty: the SDK writes the catalog description of {@code E401}. Set it only
   * when that code admits a free description (for example "other") or when the bundled catalog does
   * not know the code yet.
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E402", xml = "dDesMotEmi")
  public @Nullable String reasonDescription() {
    return reasonDescription;
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
    if (!(other instanceof CreditDebitNote)) {
      return false;
    }
    CreditDebitNote that = (CreditDebitNote) other;
    return Objects.equals(reasonCode, that.reasonCode)
        && Objects.equals(reasonDescription, that.reasonDescription);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(reasonCode, reasonDescription);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "CreditDebitNote";
  }

  /**
   * Builder for {@link CreditDebitNote}. Not thread-safe; {@link #build()} returns an immutable
   * copy.
   */
  public static final class Builder {

    private @Nullable String reasonCode;
    private @Nullable String reasonDescription;

    private Builder() {}

    private Builder(CreditDebitNote source) {
      this.reasonCode = source.reasonCode;
      this.reasonDescription = source.reasonDescription;
    }

    /**
     * Sets {@code reasonCode} (E401, {@code iMotEmi}).
     *
     * @param reasonCode the value
     * @return this builder
     */
    public Builder reasonCode(String reasonCode) {
      this.reasonCode = reasonCode;
      return this;
    }

    /**
     * Sets {@code reasonDescription} (E402, {@code dDesMotEmi}).
     *
     * @param reasonDescription the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder reasonDescription(@Nullable String reasonDescription) {
      this.reasonDescription = reasonDescription;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if an element the XSD always requires is missing
     */
    public CreditDebitNote build() {
      return new CreditDebitNote(this);
    }
  }
}
