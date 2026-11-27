/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.mapping;

import io.github.sifensdk.core.internal.tree.Node;
import io.github.sifensdk.core.model.AdditionalCommercialData;
import io.github.sifensdk.core.model.Electricity;
import io.github.sifensdk.core.model.Insurance;
import io.github.sifensdk.core.model.InsurancePolicy;
import io.github.sifensdk.core.model.SectorSpecificFields;
import io.github.sifensdk.core.model.Supermarket;
import java.time.LocalDate;

/**
 * Maps the sector-specific commercial fields ({@code gCamEsp}, E790) and their groups, children in
 * the order of {@code DE_v150.xsd} ({@code tgCamEsp}, {@code tgGrupEner}, {@code tgGrupSeg}, {@code
 * tgGrupPolSeg}, {@code tgGrupSup}, {@code tgGrupAdi}). Values are written as given.
 */
final class SectorMapper {

  Node sectorSpecificFields(SectorSpecificFields v) {
    Node n = Node.group("gCamEsp", "E790");
    for (Electricity e : v.electricity()) {
      n.add(electricity(e));
    }
    Insurance insurance = v.insurance();
    if (insurance != null) {
      n.add(insurance(insurance));
    }
    Supermarket supermarket = v.supermarket();
    if (supermarket != null) {
      n.add(supermarket(supermarket));
    }
    AdditionalCommercialData additional = v.additionalCommercialData();
    if (additional != null) {
      n.add(additionalCommercialData(additional));
    }
    return n;
  }

  Node electricity(Electricity v) {
    Node n = Node.group("gGrupEner", "E791");
    n.addIfPresent("dNroMed", "E792", v.meterNumber());
    n.addIfPresent("dActiv", "E793", v.activityCode());
    n.addIfPresent("dCateg", "E794", v.categoryCode());
    n.addIfPresent("dLecAnt", "E795", Format.amountOrNull(v.previousReading()));
    n.addIfPresent("dLecAct", "E796", Format.amountOrNull(v.currentReading()));
    n.addIfPresent("dConKwh", "E797", Format.amountOrNull(v.consumption()));
    return n;
  }

  Node insurance(Insurance v) {
    Node n = Node.group("gGrupSeg", "E800");
    n.addIfPresent("dCodEmpSeg", "E801", v.companyCode());
    for (InsurancePolicy p : v.policies()) {
      n.add(policy(p));
    }
    return n;
  }

  Node policy(InsurancePolicy v) {
    Node n = Node.group("gGrupPolSeg", "EA790");
    n.add(Node.leaf("dPoliza", "EA791", v.code()));
    n.add(Node.leaf("dUnidVig", "EA792", v.validityUnitDescription()));
    n.add(Node.leaf("dVigencia", "EA793", Format.amount(v.validityPeriod())));
    n.add(Node.leaf("dNumPoliza", "EA794", v.number()));
    n.addIfPresent("dFecIniVig", "EA795", Format.dateTimeOrNull(v.validityStartDateTime()));
    n.addIfPresent("dFecFinVig", "EA796", Format.dateTimeOrNull(v.validityEndDateTime()));
    n.addIfPresent("dCodInt", "EA797", v.itemInternalCode());
    return n;
  }

  Node supermarket(Supermarket v) {
    Node n = Node.group("gGrupSup", "E810");
    n.addIfPresent("dNomCaj", "E811", v.cashierName());
    n.addIfPresent("dEfectivo", "E812", Format.amountOrNull(v.cashAmount()));
    n.addIfPresent("dVuelto", "E813", Format.amountOrNull(v.changeAmount()));
    n.addIfPresent("dDonac", "E814", Format.amountOrNull(v.donationAmount()));
    n.addIfPresent("dDesDonac", "E815", v.donationDescription());
    return n;
  }

  Node additionalCommercialData(AdditionalCommercialData v) {
    Node n = Node.group("gGrupAdi", "E820");
    n.addIfPresent("dCiclo", "E821", v.cycle());
    n.addIfPresent("dFecIniC", "E822", Format.dateOrNull(v.cycleStartDate()));
    n.addIfPresent("dFecFinC", "E823", Format.dateOrNull(v.cycleEndDate()));
    for (LocalDate d : v.paymentDueDates()) {
      n.add(Node.leaf("dVencPag", "E824", Format.date(d)));
    }
    n.addIfPresent("dContrato", "E825", v.contractNumber());
    n.addIfPresent("dSalAnt", "E826", Format.amountOrNull(v.previousBalanceAmount()));
    n.addIfPresent("dCodConDncp", "E827", v.dncpProcurementCode());
    return n;
  }
}
