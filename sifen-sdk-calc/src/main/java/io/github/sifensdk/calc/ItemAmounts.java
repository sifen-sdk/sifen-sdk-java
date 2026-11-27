/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.calc;

import io.github.sifensdk.core.model.ItemValues;
import io.github.sifensdk.core.model.ItemVat;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * The derived amounts of one item, ready for {@code Item.builder().valuation(values()).vat(vat())}.
 *
 * @param values the item's values and discounts (E720, EA001)
 * @param vat the item's VAT (E730), or {@code null} for an item of a self-invoice
 */
public record ItemAmounts(ItemValues values, @Nullable ItemVat vat) {

  /**
   * Creates the result.
   *
   * @param values the item's values
   * @param vat the item's VAT, or {@code null}
   */
  public ItemAmounts {
    Objects.requireNonNull(values, "values");
  }
}
