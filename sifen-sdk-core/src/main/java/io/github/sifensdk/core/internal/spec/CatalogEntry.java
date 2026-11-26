/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.spec;

import org.jspecify.annotations.Nullable;

/**
 * An entry of a catalog.
 *
 * @param code the code that travels in the XML
 * @param description the description from the Manual or Nota Técnica
 * @param xsdDescription the description required by the XSD when it differs, or null
 * @param freeDescription whether the issuer writes the description (for example "Otro")
 * @param parent parent code in hierarchical catalogs, or null
 * @param state vigente, futuro or eliminado
 * @param validity validity
 */
public record CatalogEntry(
    String code,
    String description,
    @Nullable String xsdDescription,
    boolean freeDescription,
    @Nullable String parent,
    String state,
    Validity validity) {

  /**
   * The description to write in the XML: the XSD one when it differs, since SIFEN rejects any other
   * text (spec FORMATO.md).
   *
   * @return the description to emit
   */
  public String descriptionToEmit() {
    return xsdDescription != null ? xsdDescription : description;
  }
}
