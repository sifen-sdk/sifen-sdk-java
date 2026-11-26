/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.spec;

/**
 * An official source cited by a specification entry.
 *
 * @param doc document code, for example {@code MT150} or {@code NT013}
 * @param ref reference inside the document (field, validation or section)
 */
public record Source(String doc, String ref) {
  @Override
  public String toString() {
    return doc + " " + ref;
  }
}
