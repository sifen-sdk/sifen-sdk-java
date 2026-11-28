/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

/**
 * A value of a SIFEN catalog: the code that travels in the XML and its official description.
 *
 * <p>Every enum of this package implements it. Builders accept either the constant or the code as a
 * {@code String}; the latter is for codes SIFEN adds before the SDK knows them.
 */
public interface CatalogCode {

  /**
   * Returns the code as it travels in the XML.
   *
   * @return the code
   */
  String code();

  /**
   * Returns the official description the SDK writes next to the code.
   *
   * @return the description
   */
  String description();
}
