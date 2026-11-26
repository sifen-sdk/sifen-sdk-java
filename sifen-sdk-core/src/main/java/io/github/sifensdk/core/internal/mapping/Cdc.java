/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.mapping;

import io.github.sifensdk.core.model.ElectronicDocument;

/**
 * Builds the CDC of a document from its fields, with the public {@link
 * io.github.sifensdk.core.cdc.Cdc}.
 */
final class Cdc {

  private Cdc() {}

  /**
   * The 44-character CDC of a document.
   *
   * @param document the document
   * @return the CDC, check digit included
   */
  static String of(ElectronicDocument document) {
    return io.github.sifensdk.core.cdc.Cdc.compose(
            document.stamp().documentTypeCode(),
            document.generalData().issuer().ruc(),
            document.generalData().issuer().checkDigit(),
            document.stamp().establishment(),
            document.stamp().dispatchPoint(),
            document.stamp().documentNumber(),
            document.generalData().issuer().taxpayerTypeCode(),
            document.generalData().issueDateTime().toLocalDate(),
            document.operation().emissionTypeCode(),
            document.operation().securityCode())
        .value();
  }
}
