/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.spec;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * A catalog of codes ({@code catalogos/}).
 *
 * @param id catalog id, equal to its file name
 * @param title title
 * @param fields ids of the fields that take their value from this catalog
 * @param descriptionFields code field id to description field id
 * @param entries entries, in source order
 */
public record Catalog(
    String id,
    String title,
    List<String> fields,
    Map<String, String> descriptionFields,
    List<CatalogEntry> entries) {

  /**
   * Finds an entry by code (and parent, for hierarchical catalogs).
   *
   * @param code the code
   * @param parent the parent code, or null
   * @return the entry, if any
   */
  public Optional<CatalogEntry> find(String code, @Nullable String parent) {
    for (CatalogEntry e : entries) {
      if (e.code().equals(code) && (parent == null || parent.equals(e.parent()))) {
        return Optional.of(e);
      }
    }
    return Optional.empty();
  }
}
