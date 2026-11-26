/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.mapping;

import io.github.sifensdk.core.internal.spec.Catalog;
import io.github.sifensdk.core.internal.spec.CatalogEntry;
import io.github.sifensdk.core.internal.spec.Field;
import io.github.sifensdk.core.internal.spec.Spec;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Completes the descriptions of catalog codes ({@code dDes…}) as ADR-0011 says: the SDK writes the
 * description that corresponds to the code, exactly as the XSD requires it ({@link
 * CatalogEntry#descriptionToEmit()}). The description given in the model is used only when the code
 * admits a free description or the bundled catalog does not know the code.
 */
final class Descriptions {

  private final Spec spec;

  Descriptions(Spec spec) {
    this.spec = spec;
  }

  /**
   * The description to write next to a code.
   *
   * @param codeFieldId id of the code field (for example {@code C002})
   * @param code the code, or null if not informed
   * @param given the description from the model, or null
   * @return the description, or null to leave the element out
   */
  @Nullable String of(String codeFieldId, @Nullable String code, @Nullable String given) {
    if (code == null) {
      return given;
    }
    Optional<CatalogEntry> entry = entry(codeFieldId, code);
    if (entry.isPresent() && !entry.get().freeDescription()) {
      return entry.get().descriptionToEmit();
    }
    return given;
  }

  private Optional<CatalogEntry> entry(String codeFieldId, String code) {
    Field field = spec.fields().get(codeFieldId);
    if (field == null || field.catalog() == null) {
      return Optional.empty();
    }
    Catalog catalog = spec.catalogs().get(field.catalog());
    return catalog == null ? Optional.empty() : catalog.find(code, null);
  }
}
