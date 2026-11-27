/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.mapping;

import io.github.sifensdk.core.internal.spec.Spec;
import io.github.sifensdk.core.internal.tree.Node;
import io.github.sifensdk.core.model.AssociatedDocument;
import io.github.sifensdk.core.model.ComplementaryData;
import io.github.sifensdk.core.model.ElectronicDocument;
import io.github.sifensdk.core.model.Totals;
import java.time.LocalDateTime;

/**
 * Maps the public model to the neutral tree that the rule engine evaluates and the serializer
 * writes.
 *
 * <p>The tree is the unsigned {@code rDE}: {@code dVerFor} and {@code DE} (with the CDC as
 * attribute {@code Id}), without {@code Signature} or {@code gCamFuFD}, which the signer adds.
 * Children follow the sequence of {@code DE_v150.xsd}. The SDK completes what ADR-0011 assigns to
 * it: {@code dVerFor}, the CDC and {@code dDVId}, {@code dFecFirma}, {@code dSisFact} and the
 * catalog descriptions. Amounts are written exactly as given ({@link
 * java.math.BigDecimal#toPlainString()}); nothing is computed, rounded or checked here.
 */
public final class DocumentMapper {

  /** Format version, MT150 field AA002 ({@code dVerFor}); the XSD only admits 150. */
  static final String FORMAT_VERSION = "150";

  /**
   * Billing system, MT150 field A005 ({@code dSisFact}): the XSD requires it and only admits 1
   * (contradiction C-103 of the specification).
   */
  static final String BILLING_SYSTEM = "1";

  private DocumentMapper() {}

  /**
   * Maps a document to its tree.
   *
   * @param document the document
   * @param signatureDateTime the signature date and time ({@code dFecFirma}, A004), local time of
   *     Paraguay
   * @return the root {@code rDE}
   */
  public static Node toTree(ElectronicDocument document, LocalDateTime signatureDateTime) {
    Descriptions descriptions = new Descriptions(Spec.bundled());
    HeaderMapper header = new HeaderMapper(descriptions);
    TypeDataMapper typeData = new TypeDataMapper(descriptions);
    TotalsMapper closing = new TotalsMapper(descriptions);
    String cdc = Cdc.of(document);

    Node de = Node.group("DE", "A001").attribute("Id", cdc);
    de.add(Node.leaf("dDVId", "A003", cdc.substring(cdc.length() - 1)));
    de.add(Node.leaf("dFecFirma", "A004", Format.dateTime(signatureDateTime)));
    de.add(Node.leaf("dSisFact", "A005", BILLING_SYSTEM));
    de.add(header.operation(document.operation()));
    de.add(header.stamp(document.stamp()));
    de.add(header.generalData(document.generalData()));
    de.add(typeData.documentTypeData(document.documentTypeData()));
    Totals totals = document.totals();
    if (totals != null) {
      de.add(closing.totals(totals));
    }
    ComplementaryData complementary = document.complementaryData();
    if (complementary != null) {
      de.add(closing.complementaryData(complementary));
    }
    for (AssociatedDocument associated : document.associatedDocuments()) {
      de.add(closing.associatedDocument(associated));
    }

    return Node.group("rDE", "AA001").add(Node.leaf("dVerFor", "AA002", FORMAT_VERSION)).add(de);
  }
}
