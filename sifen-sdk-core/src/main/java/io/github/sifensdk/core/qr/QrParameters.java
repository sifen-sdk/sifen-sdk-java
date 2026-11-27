/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.qr;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * The values of a document that go into its QR (Manual Técnico v150, 13.8.2, table of parameters;
 * contradiction C-512: the table is applied).
 *
 * <p>Every value is the text exactly as it is written in the XML; nothing is formatted or rounded
 * here.
 *
 * @param version {@code nVersion}: the format version, AA002 ({@code dVerFor}), for example {@code
 *     150}
 * @param cdc {@code Id}: the CDC, A002
 * @param issueDateTime {@code dFeEmiDE}: the issue date and time, D002, as written in the XML (
 *     {@code yyyy-MM-ddTHH:mm:ss}); it is converted to hexadecimal in the QR
 * @param receiverRuc {@code dRucRec}: the receiver's RUC without check digit, D206, when the
 *     receiver is a taxpayer; otherwise {@code null}
 * @param receiverIdDocumentNumber {@code dNumIDRec}: the receiver's identity document number, D210,
 *     used when there is no {@code receiverRuc}; {@code null} if not informed ({@code 0} is
 *     written)
 * @param generalOperationTotal {@code dTotGralOpe}: total of the operation, F014; {@code null} if
 *     the document does not have it ({@code 0} is written, Manual 13.8.4.1)
 * @param vatTotal {@code dTotIVA}: total VAT, F017; {@code null} if the document does not have it
 *     ({@code 0} is written, Manual 13.8.4.1)
 * @param itemCount {@code cItems}: number of items (occurrences of E701, {@code gCamItem/dCodInt})
 * @param digestValue {@code DigestValue}: the Base64 text of the signature's {@code DigestValue},
 *     XS17; it is converted to hexadecimal in the QR
 */
public record QrParameters(
    String version,
    String cdc,
    String issueDateTime,
    @Nullable String receiverRuc,
    @Nullable String receiverIdDocumentNumber,
    @Nullable String generalOperationTotal,
    @Nullable String vatTotal,
    int itemCount,
    String digestValue) {

  /**
   * Checks the parameters.
   *
   * @throws IllegalArgumentException if a required value is empty, both receiver identifications
   *     are given, or the number of items is negative
   */
  public QrParameters {
    requireText(version, "version (nVersion)");
    requireText(cdc, "cdc (Id)");
    requireText(issueDateTime, "issueDateTime (dFeEmiDE)");
    requireText(digestValue, "digestValue (DigestValue)");
    if (receiverRuc != null && receiverIdDocumentNumber != null) {
      throw new IllegalArgumentException(
          "The QR has either dRucRec or dNumIDRec, not both (Manual Técnico v150, 13.8.2)");
    }
    if (itemCount < 0) {
      throw new IllegalArgumentException("itemCount (cItems) cannot be negative");
    }
  }

  /** Returns a description without any content of the document. */
  @Override
  public String toString() {
    return "QrParameters[...]";
  }

  private static void requireText(@Nullable String value, String name) {
    Objects.requireNonNull(value, name);
    if (value.isEmpty()) {
      throw new IllegalArgumentException(name + " cannot be empty");
    }
  }
}
