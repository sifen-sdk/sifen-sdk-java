/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.mapping;

import io.github.sifensdk.core.internal.tree.Node;
import io.github.sifensdk.core.model.AffectedObligation;
import io.github.sifensdk.core.model.CommercialOperation;
import io.github.sifensdk.core.model.EconomicActivity;
import io.github.sifensdk.core.model.GeneralData;
import io.github.sifensdk.core.model.GenerationResponsible;
import io.github.sifensdk.core.model.Issuer;
import io.github.sifensdk.core.model.Operation;
import io.github.sifensdk.core.model.Receiver;
import io.github.sifensdk.core.model.Stamp;

/**
 * Maps the operation (B), the stamp (C) and the general data (D): commercial operation, issuer and
 * receiver.
 */
final class HeaderMapper {

  private final Descriptions descriptions;

  HeaderMapper(Descriptions descriptions) {
    this.descriptions = descriptions;
  }

  Node generalData(GeneralData v) {
    Node n = Node.group("gDatGralOpe", "D001");
    n.add(Node.leaf("dFeEmiDE", "D002", Format.dateTime(v.issueDateTime())));
    CommercialOperation commercialOperation = v.commercialOperation();
    if (commercialOperation != null) {
      n.add(commercialOperation(commercialOperation));
    }
    n.add(issuer(v.issuer()));
    n.add(receiver(v.receiver()));
    return n;
  }

  Node receiver(Receiver v) {
    Node n = Node.group("gDatRec", "D200");
    n.add(Node.leaf("iNatRec", "D201", v.natureCode()));
    n.add(Node.leaf("iTiOpe", "D202", v.operationTypeCode()));
    n.add(Node.leaf("cPaisRec", "D203", v.countryCode()));
    n.addIfPresent(
        "dDesPaisRe", "D204", descriptions.of("D203", v.countryCode(), v.countryDescription()));
    n.addIfPresent("iTiContRec", "D205", v.taxpayerTypeCode());
    n.addIfPresent("dRucRec", "D206", v.ruc());
    n.addIfPresent("dDVRec", "D207", v.checkDigit());
    n.addIfPresent("iTipIDRec", "D208", v.idDocumentTypeCode());
    n.addIfPresent(
        "dDTipIDRec",
        "D209",
        descriptions.of("D208", v.idDocumentTypeCode(), v.idDocumentTypeDescription()));
    n.addIfPresent("dNumIDRec", "D210", v.idDocumentNumber());
    n.add(Node.leaf("dNomRec", "D211", v.name()));
    n.addIfPresent("dNomFanRec", "D212", v.tradeName());
    n.addIfPresent("dDirRec", "D213", v.address());
    n.addIfPresent("dNumCasRec", "D218", v.houseNumber());
    n.addIfPresent("cDepRec", "D219", v.departmentCode());
    n.addIfPresent(
        "dDesDepRec",
        "D220",
        descriptions.of("D219", v.departmentCode(), v.departmentDescription()));
    n.addIfPresent("cDisRec", "D221", v.districtCode());
    n.addIfPresent(
        "dDesDisRec", "D222", descriptions.of("D221", v.districtCode(), v.districtDescription()));
    n.addIfPresent("cCiuRec", "D223", v.cityCode());
    n.addIfPresent(
        "dDesCiuRec", "D224", descriptions.of("D223", v.cityCode(), v.cityDescription()));
    n.addIfPresent("dTelRec", "D214", v.phone());
    n.addIfPresent("dCelRec", "D215", v.mobilePhone());
    n.addIfPresent("dEmailRec", "D216", v.email());
    n.addIfPresent("dCodCliente", "D217", v.customerCode());
    return n;
  }

  Node issuer(Issuer v) {
    Node n = Node.group("gEmis", "D100");
    n.add(Node.leaf("dRucEm", "D101", v.ruc()));
    n.add(Node.leaf("dDVEmi", "D102", v.checkDigit()));
    n.add(Node.leaf("iTipCont", "D103", v.taxpayerTypeCode()));
    n.addIfPresent("cTipReg", "D104", v.regimeTypeCode());
    n.add(Node.leaf("dNomEmi", "D105", v.name()));
    n.addIfPresent("dNomFanEmi", "D106", v.tradeName());
    n.add(Node.leaf("dDirEmi", "D107", v.address()));
    n.add(Node.leaf("dNumCas", "D108", v.houseNumber()));
    n.addIfPresent("dCompDir1", "D109", v.addressComplement1());
    n.addIfPresent("dCompDir2", "D110", v.addressComplement2());
    n.add(Node.leaf("cDepEmi", "D111", v.departmentCode()));
    n.addIfPresent(
        "dDesDepEmi",
        "D112",
        descriptions.of("D111", v.departmentCode(), v.departmentDescription()));
    n.addIfPresent("cDisEmi", "D113", v.districtCode());
    n.addIfPresent(
        "dDesDisEmi", "D114", descriptions.of("D113", v.districtCode(), v.districtDescription()));
    n.add(Node.leaf("cCiuEmi", "D115", v.cityCode()));
    n.addIfPresent(
        "dDesCiuEmi", "D116", descriptions.of("D115", v.cityCode(), v.cityDescription()));
    n.add(Node.leaf("dTelEmi", "D117", v.phone()));
    n.add(Node.leaf("dEmailE", "D118", v.email()));
    n.addIfPresent("dDenSuc", "D119", v.branchName());
    for (EconomicActivity e : v.economicActivities()) {
      n.add(economicActivity(e));
    }
    GenerationResponsible generationResponsible = v.generationResponsible();
    if (generationResponsible != null) {
      n.add(generationResponsible(generationResponsible));
    }
    return n;
  }

  Node generationResponsible(GenerationResponsible v) {
    Node n = Node.group("gRespDE", "D140");
    n.add(Node.leaf("iTipIDRespDE", "D141", v.idDocumentTypeCode()));
    n.addIfPresent(
        "dDTipIDRespDE",
        "D142",
        descriptions.of("D141", v.idDocumentTypeCode(), v.idDocumentTypeDescription()));
    n.add(Node.leaf("dNumIDRespDE", "D143", v.idDocumentNumber()));
    n.add(Node.leaf("dNomRespDE", "D144", v.name()));
    n.add(Node.leaf("dCarRespDE", "D145", v.position()));
    return n;
  }

  Node economicActivity(EconomicActivity v) {
    Node n = Node.group("gActEco", "D130");
    n.add(Node.leaf("cActEco", "D131", v.code()));
    n.add(Node.leaf("dDesActEco", "D132", v.description()));
    return n;
  }

  Node commercialOperation(CommercialOperation v) {
    Node n = Node.group("gOpeCom", "D010");
    n.addIfPresent("iTipTra", "D011", v.transactionTypeCode());
    n.addIfPresent(
        "dDesTipTra",
        "D012",
        descriptions.of("D011", v.transactionTypeCode(), v.transactionTypeDescription()));
    n.add(Node.leaf("iTImp", "D013", v.taxTypeCode()));
    n.addIfPresent(
        "dDesTImp", "D014", descriptions.of("D013", v.taxTypeCode(), v.taxTypeDescription()));
    n.add(Node.leaf("cMoneOpe", "D015", v.currencyCode()));
    n.addIfPresent(
        "dDesMoneOpe", "D016", descriptions.of("D015", v.currencyCode(), v.currencyDescription()));
    n.addIfPresent("dCondTiCam", "D017", v.exchangeRateConditionCode());
    n.addIfPresent("dTiCam", "D018", Format.amountOrNull(v.exchangeRate()));
    n.addIfPresent("iCondAnt", "D019", v.advanceConditionCode());
    n.addIfPresent(
        "dDesCondAnt",
        "D020",
        descriptions.of("D019", v.advanceConditionCode(), v.advanceConditionDescription()));
    for (AffectedObligation e : v.affectedObligations()) {
      n.add(affectedObligation(e));
    }
    return n;
  }

  Node affectedObligation(AffectedObligation v) {
    Node n = Node.group("gOblAfe", "D030");
    n.add(Node.leaf("cOblAfe", "D031", v.code()));
    n.addIfPresent("dDesOblAfe", "D032", descriptions.of("D031", v.code(), v.description()));
    return n;
  }

  Node stamp(Stamp v) {
    Node n = Node.group("gTimb", "C001");
    n.add(Node.leaf("iTiDE", "C002", v.documentTypeCode()));
    n.addIfPresent(
        "dDesTiDE",
        "C003",
        descriptions.of("C002", v.documentTypeCode(), v.documentTypeDescription()));
    n.add(Node.leaf("dNumTim", "C004", v.number()));
    n.add(Node.leaf("dEst", "C005", v.establishment()));
    n.add(Node.leaf("dPunExp", "C006", v.dispatchPoint()));
    n.add(Node.leaf("dNumDoc", "C007", v.documentNumber()));
    n.addIfPresent("dSerieNum", "C010", v.series());
    n.add(Node.leaf("dFeIniT", "C008", Format.date(v.startDate())));
    return n;
  }

  Node operation(Operation v) {
    Node n = Node.group("gOpeDE", "B001");
    n.add(Node.leaf("iTipEmi", "B002", v.emissionTypeCode()));
    n.addIfPresent(
        "dDesTipEmi",
        "B003",
        descriptions.of("B002", v.emissionTypeCode(), v.emissionTypeDescription()));
    n.add(Node.leaf("dCodSeg", "B004", v.securityCode()));
    n.addIfPresent("dInfoEmi", "B005", v.issuerInformation());
    n.addIfPresent("dInfoFisc", "B006", v.taxAuthorityInformation());
    return n;
  }
}
