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
 * Grupo del sector de seguros.
 *
 * <p>SIFEN group {@code E800} ({@code gGrupSeg}). Immutable and thread-safe; create it with {@link
 * #builder()}. The builder only checks what Java needs (required elements and non-null lists); the
 * SIFEN rules (mandatory, conditional, arithmetic) are verified by the validation step, never here.
 * Amounts are kept exactly as given.
 */
@SifenField(id = "E800", xml = "gGrupSeg")
public final class Insurance {

  private final @Nullable String companyCode;
  private final List<InsurancePolicy> policies;

  private Insurance(Builder b) {
    this.companyCode = b.companyCode;
    this.policies = List.copyOf(b.policies);
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
   * Código de la empresa de seguros en la Superintendencia de Seguros.
   *
   * <p>SIFEN field {@code E801} ({@code dCodEmpSeg}).
   *
   * @return the value, or {@code null} if not informed
   */
  @SifenField(id = "E801", xml = "dCodEmpSeg")
  public @Nullable String companyCode() {
    return companyCode;
  }

  /**
   * Grupo de póliza de seguros. At least one, up to 999.
   *
   * <p>SIFEN group {@code EA790} ({@code gGrupPolSeg}).
   *
   * @return an unmodifiable list, possibly empty
   */
  @SifenField(id = "EA790", xml = "gGrupPolSeg")
  public List<InsurancePolicy> policies() {
    return policies;
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
    if (!(other instanceof Insurance)) {
      return false;
    }
    Insurance that = (Insurance) other;
    return Objects.equals(companyCode, that.companyCode) && Objects.equals(policies, that.policies);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(companyCode, policies);
  }

  /**
   * The type name and, for lists, how many elements they hold. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "Insurance[policies=" + policies.size() + "]";
  }

  /** Builder for {@link Insurance}. Not thread-safe; {@link #build()} returns an immutable copy. */
  public static final class Builder {

    private @Nullable String companyCode;
    private List<InsurancePolicy> policies = new ArrayList<>();

    private Builder() {}

    private Builder(Insurance source) {
      this.companyCode = source.companyCode;
      this.policies = new ArrayList<>(source.policies);
    }

    /**
     * Sets {@code companyCode} (E801, {@code dCodEmpSeg}).
     *
     * @param companyCode the value, or {@code null} to leave it out
     * @return this builder
     */
    public Builder companyCode(@Nullable String companyCode) {
      this.companyCode = companyCode;
      return this;
    }

    /**
     * Replaces the list of {@code policies} (EA790, {@code gGrupPolSeg}).
     *
     * @param policies the elements, in document order
     * @return this builder
     */
    public Builder policies(List<InsurancePolicy> policies) {
      this.policies = new ArrayList<>(policies);
      return this;
    }

    /**
     * Appends one element to {@code policies} (EA790, {@code gGrupPolSeg}).
     *
     * @param policy the element
     * @return this builder
     */
    public Builder addPolicy(InsurancePolicy policy) {
      this.policies.add(Objects.requireNonNull(policy, "policy"));
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     */
    public Insurance build() {
      return new Insurance(this);
    }
  }
}
