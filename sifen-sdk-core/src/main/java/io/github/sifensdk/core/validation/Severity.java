/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.validation;

/** How serious a validation issue is. */
public enum Severity {
  /** SIFEN would reject the document. The SDK refuses to sign it. */
  ERROR,
  /** Worth reviewing, but does not block signing (for example, a rule configured as a warning). */
  WARNING
}
