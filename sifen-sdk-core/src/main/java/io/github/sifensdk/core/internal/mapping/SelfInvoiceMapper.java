/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.mapping;

import io.github.sifensdk.core.internal.tree.Node;
import io.github.sifensdk.core.model.SelfInvoice;

/**
 * Maps the fields of the self-invoice ({@code gCamAE}, E300, Manual Técnico group E4): the seller
 * and the place of the transaction, in the order of {@code tgCamAE} in {@code DE_v150.xsd}. The
 * catalog descriptions are completed as ADR-0011 says.
 */
final class SelfInvoiceMapper {

  private final Descriptions descriptions;

  SelfInvoiceMapper(Descriptions descriptions) {
    this.descriptions = descriptions;
  }

  Node selfInvoice(SelfInvoice v) {
    Node n = Node.group("gCamAE", "E300");
    n.add(Node.leaf("iNatVen", "E301", v.sellerNatureCode()));
    n.addIfPresent(
        "dDesNatVen",
        "E302",
        descriptions.of("E301", v.sellerNatureCode(), v.sellerNatureDescription()));
    n.add(Node.leaf("iTipIDVen", "E304", v.sellerIdDocumentTypeCode()));
    n.addIfPresent(
        "dDTipIDVen",
        "E305",
        descriptions.of("E304", v.sellerIdDocumentTypeCode(), v.sellerIdDocumentTypeDescription()));
    n.add(Node.leaf("dNumIDVen", "E306", v.sellerIdDocumentNumber()));
    n.add(Node.leaf("dNomVen", "E307", v.sellerName()));
    n.add(Node.leaf("dDirVen", "E308", v.sellerAddress()));
    n.add(Node.leaf("dNumCasVen", "E309", v.sellerHouseNumber()));
    n.add(Node.leaf("cDepVen", "E310", v.sellerDepartmentCode()));
    n.addIfPresent(
        "dDesDepVen",
        "E311",
        descriptions.of("E310", v.sellerDepartmentCode(), v.sellerDepartmentDescription()));
    n.addIfPresent("cDisVen", "E312", v.sellerDistrictCode());
    n.addIfPresent(
        "dDesDisVen",
        "E313",
        descriptions.of("E312", v.sellerDistrictCode(), v.sellerDistrictDescription()));
    n.add(Node.leaf("cCiuVen", "E314", v.sellerCityCode()));
    n.addIfPresent(
        "dDesCiuVen",
        "E315",
        descriptions.of("E314", v.sellerCityCode(), v.sellerCityDescription()));
    n.add(Node.leaf("dDirProv", "E316", v.transactionAddress()));
    n.add(Node.leaf("cDepProv", "E317", v.transactionDepartmentCode()));
    n.addIfPresent(
        "dDesDepProv",
        "E318",
        descriptions.of(
            "E317", v.transactionDepartmentCode(), v.transactionDepartmentDescription()));
    n.addIfPresent("cDisProv", "E319", v.transactionDistrictCode());
    n.addIfPresent(
        "dDesDisProv",
        "E320",
        descriptions.of("E319", v.transactionDistrictCode(), v.transactionDistrictDescription()));
    n.add(Node.leaf("cCiuProv", "E321", v.transactionCityCode()));
    n.addIfPresent(
        "dDesCiuProv",
        "E322",
        descriptions.of("E321", v.transactionCityCode(), v.transactionCityDescription()));
    return n;
  }
}
