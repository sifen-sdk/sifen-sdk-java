/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.mapping;

import io.github.sifensdk.core.internal.tree.Node;
import io.github.sifensdk.core.model.AssociatedDocument;
import io.github.sifensdk.core.model.Cargo;
import io.github.sifensdk.core.model.ComplementaryData;
import io.github.sifensdk.core.model.Totals;

/**
 * Maps the totals (F), the complementary data (G) and the associated documents (H), children in the
 * order of the {@code DE_v150.xsd} sequence.
 */
final class TotalsMapper {

  private final Descriptions descriptions;

  TotalsMapper(Descriptions descriptions) {
    this.descriptions = descriptions;
  }

  Node associatedDocument(AssociatedDocument v) {
    Node n = Node.group("gCamDEAsoc", "H001");
    n.add(Node.leaf("iTipDocAso", "H002", v.typeCode()));
    n.addIfPresent(
        "dDesTipDocAso", "H003", descriptions.of("H002", v.typeCode(), v.typeDescription()));
    n.addIfPresent("dCdCDERef", "H004", v.cdc());
    n.addIfPresent("dNTimDI", "H005", v.printedStamp());
    n.addIfPresent("dEstDocAso", "H006", v.establishment());
    n.addIfPresent("dPExpDocAso", "H007", v.dispatchPoint());
    n.addIfPresent("dNumDocAso", "H008", v.documentNumber());
    n.addIfPresent("iTipoDocAso", "H009", v.printedDocumentTypeCode());
    n.addIfPresent(
        "dDTipoDocAso",
        "H010",
        descriptions.of("H009", v.printedDocumentTypeCode(), v.printedDocumentTypeDescription()));
    n.addIfPresent("dFecEmiDI", "H011", Format.dateOrNull(v.printedIssueDate()));
    n.addIfPresent("dNumComRet", "H012", v.withholdingReceiptNumber());
    n.addIfPresent("dNumResCF", "H013", v.fiscalCreditResolutionNumber());
    n.addIfPresent("iTipCons", "H014", v.certificateTypeCode());
    n.addIfPresent(
        "dDesTipCons",
        "H015",
        descriptions.of("H014", v.certificateTypeCode(), v.certificateTypeDescription()));
    n.addIfPresent("dNumCons", "H016", v.certificateNumber());
    n.addIfPresent("dNumControl", "H017", v.certificateControlNumber());
    n.addIfPresent("dRucFus", "H018", v.mergedRuc());
    return n;
  }

  Node complementaryData(ComplementaryData v) {
    Node n = Node.group("gCamGen", "G001");
    n.addIfPresent("dOrdCompra", "G002", v.purchaseOrderNumber());
    n.addIfPresent("dOrdVta", "G003", v.salesOrderNumber());
    n.addIfPresent("dAsiento", "G004", v.accountingEntryNumber());
    Cargo cargo = v.cargo();
    if (cargo != null) {
      n.add(cargo(cargo));
    }
    return n;
  }

  Node cargo(Cargo v) {
    Node n = Node.group("gCamCarg", "G050");
    n.addIfPresent("cUniMedTotVol", "G051", v.volumeUnitCode());
    n.addIfPresent(
        "dDesUniMedTotVol",
        "G052",
        descriptions.of("G051", v.volumeUnitCode(), v.volumeUnitDescription()));
    n.addIfPresent("dTotVolMerc", "G053", Format.amountOrNull(v.totalVolume()));
    n.addIfPresent("cUniMedTotPes", "G054", v.weightUnitCode());
    n.addIfPresent(
        "dDesUniMedTotPes",
        "G055",
        descriptions.of("G054", v.weightUnitCode(), v.weightUnitDescription()));
    n.addIfPresent("dTotPesMerc", "G056", Format.amountOrNull(v.totalWeight()));
    n.addIfPresent("iCarCarga", "G057", v.characteristicCode());
    n.addIfPresent(
        "dDesCarCarga",
        "G058",
        descriptions.of("G057", v.characteristicCode(), v.characteristicDescription()));
    return n;
  }

  Node totals(Totals v) {
    Node n = Node.group("gTotSub", "F001");
    n.addIfPresent("dSubExe", "F002", Format.amountOrNull(v.exemptSubtotal()));
    n.addIfPresent("dSubExo", "F003", Format.amountOrNull(v.exoneratedSubtotal()));
    n.addIfPresent("dSub5", "F004", Format.amountOrNull(v.vat5Subtotal()));
    n.addIfPresent("dSub10", "F005", Format.amountOrNull(v.vat10Subtotal()));
    n.add(Node.leaf("dTotOpe", "F008", Format.amount(v.grossTotal())));
    n.add(Node.leaf("dTotDesc", "F009", Format.amount(v.itemDiscountTotal())));
    n.add(Node.leaf("dTotDescGlotem", "F033", Format.amount(v.itemGlobalDiscountTotal())));
    n.add(Node.leaf("dTotAntItem", "F034", Format.amount(v.itemAdvanceTotal())));
    n.add(Node.leaf("dTotAnt", "F035", Format.amount(v.itemGlobalAdvanceTotal())));
    n.add(Node.leaf("dPorcDescTotal", "F010", Format.amount(v.globalDiscountRate())));
    n.add(Node.leaf("dDescTotal", "F011", Format.amount(v.discountTotal())));
    n.add(Node.leaf("dAnticipo", "F012", Format.amount(v.advanceTotal())));
    n.add(Node.leaf("dRedon", "F013", Format.amount(v.roundingAmount())));
    n.addIfPresent("dComi", "F025", Format.amountOrNull(v.commissionAmount()));
    n.add(Node.leaf("dTotGralOpe", "F014", Format.amount(v.netTotal())));
    n.addIfPresent("dIVA5", "F015", Format.amountOrNull(v.vat5Amount()));
    n.addIfPresent("dIVA10", "F016", Format.amountOrNull(v.vat10Amount()));
    n.addIfPresent("dLiqTotIVA5", "F036", Format.amountOrNull(v.vat5RoundingAmount()));
    n.addIfPresent("dLiqTotIVA10", "F037", Format.amountOrNull(v.vat10RoundingAmount()));
    n.addIfPresent("dIVAComi", "F026", Format.amountOrNull(v.commissionVatAmount()));
    n.addIfPresent("dTotIVA", "F017", Format.amountOrNull(v.vatTotal()));
    n.addIfPresent("dBaseGrav5", "F018", Format.amountOrNull(v.vat5TaxableBaseTotal()));
    n.addIfPresent("dBaseGrav10", "F019", Format.amountOrNull(v.vat10TaxableBaseTotal()));
    n.addIfPresent("dTBasGraIVA", "F020", Format.amountOrNull(v.vatTaxableBaseTotal()));
    n.addIfPresent("dTotalGs", "F023", Format.amountOrNull(v.guaraniTotal()));
    return n;
  }
}
