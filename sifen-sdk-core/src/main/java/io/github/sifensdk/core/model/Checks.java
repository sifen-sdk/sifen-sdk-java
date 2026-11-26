/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import org.jspecify.annotations.Nullable;

/** Null checks shared by the builders. Messages name the field, never its value. */
final class Checks {

  private Checks() {}

  static <T> T required(@Nullable T value, String name, String id, String xml) {
    if (value == null) {
      throw new NullPointerException(name + " (" + id + " " + xml + ") is required");
    }
    return value;
  }
}
