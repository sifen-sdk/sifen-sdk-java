/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.mapping;

import io.github.sifensdk.core.internal.spec.Spec;
import io.github.sifensdk.core.internal.tree.Node;
import io.github.sifensdk.core.model.AffectedObligation;
import io.github.sifensdk.core.model.AssociatedDocument;
import io.github.sifensdk.core.model.CardPayment;
import io.github.sifensdk.core.model.Cargo;
import io.github.sifensdk.core.model.CheckPayment;
import io.github.sifensdk.core.model.CommercialOperation;
import io.github.sifensdk.core.model.ComplementaryData;
import io.github.sifensdk.core.model.Credit;
import io.github.sifensdk.core.model.CreditDebitNote;
import io.github.sifensdk.core.model.DocumentTypeData;
import io.github.sifensdk.core.model.EconomicActivity;
import io.github.sifensdk.core.model.GeneralData;
import io.github.sifensdk.core.model.GenerationResponsible;
import io.github.sifensdk.core.model.GoodsTracking;
import io.github.sifensdk.core.model.Installment;
import io.github.sifensdk.core.model.Invoice;
import io.github.sifensdk.core.model.Issuer;
import io.github.sifensdk.core.model.Item;
import io.github.sifensdk.core.model.ItemDiscountsAndAdvances;
import io.github.sifensdk.core.model.ItemValues;
import io.github.sifensdk.core.model.ItemVat;
import io.github.sifensdk.core.model.NewVehicle;
import io.github.sifensdk.core.model.Operation;
import io.github.sifensdk.core.model.OperationCondition;
import io.github.sifensdk.core.model.Payment;
import io.github.sifensdk.core.model.PublicProcurement;
import io.github.sifensdk.core.model.Receiver;
import io.github.sifensdk.core.model.Stamp;
import io.github.sifensdk.core.model.Totals;

/**
 * Maps each group of the model to its subtree, children in the order of the {@code DE_v150.xsd}
 * sequence. Values are written as given; descriptions of catalog codes come from the bundled
 * specification. The root ({@code rDE}, {@code DE}) is built by {@link DocumentMapper}.
 */
final class GroupMapper {

  private final Descriptions descriptions;

  GroupMapper(Spec spec) {
    this.descriptions = new Descriptions(spec);
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

  Node documentTypeData(DocumentTypeData v) {
    Node n = Node.group("gDtipDE", "E001");
    Invoice invoice = v.invoice();
    if (invoice != null) {
      n.add(invoice(invoice));
    }
    CreditDebitNote creditDebitNote = v.creditDebitNote();
    if (creditDebitNote != null) {
      n.add(creditDebitNote(creditDebitNote));
    }
    OperationCondition operationCondition = v.operationCondition();
    if (operationCondition != null) {
      n.add(operationCondition(operationCondition));
    }
    for (Item e : v.items()) {
      n.add(item(e));
    }
    return n;
  }

  Node item(Item v) {
    Node n = Node.group("gCamItem", "E700");
    n.add(Node.leaf("dCodInt", "E701", v.internalCode()));
    n.addIfPresent("dParAranc", "E702", v.tariffHeading());
    n.addIfPresent("dNCM", "E703", v.mercosurCommonNomenclature());
    n.addIfPresent("dDncpG", "E704", v.dncpGeneralCode());
    n.addIfPresent("dDncpE", "E705", v.dncpSpecificCode());
    n.addIfPresent("dGtin", "E706", v.productGtin());
    n.addIfPresent("dGtinPq", "E707", v.packageGtin());
    n.add(Node.leaf("dDesProSer", "E708", v.description()));
    n.add(Node.leaf("cUniMed", "E709", v.unitOfMeasureCode()));
    n.addIfPresent(
        "dDesUniMed",
        "E710",
        descriptions.of("E709", v.unitOfMeasureCode(), v.unitOfMeasureDescription()));
    n.add(Node.leaf("dCantProSer", "E711", Format.amount(v.quantity())));
    n.addIfPresent("cPaisOrig", "E712", v.originCountryCode());
    n.addIfPresent(
        "dDesPaisOrig",
        "E713",
        descriptions.of("E712", v.originCountryCode(), v.originCountryDescription()));
    n.addIfPresent("dInfItem", "E714", v.additionalInformation());
    n.addIfPresent("cRelMerc", "E715", v.goodsRelevanceCode());
    n.addIfPresent(
        "dDesRelMerc",
        "E716",
        descriptions.of("E715", v.goodsRelevanceCode(), v.goodsRelevanceDescription()));
    n.addIfPresent("dCanQuiMer", "E717", Format.amountOrNull(v.breakageOrShrinkageQuantity()));
    n.addIfPresent("dPorQuiMer", "E718", Format.amountOrNull(v.breakageOrShrinkageRate()));
    n.addIfPresent("dCDCAnticipo", "E719", v.advanceCdc());
    ItemValues valuation = v.valuation();
    if (valuation != null) {
      n.add(itemValues(valuation));
    }
    ItemVat vat = v.vat();
    if (vat != null) {
      n.add(itemVat(vat));
    }
    GoodsTracking goodsTracking = v.goodsTracking();
    if (goodsTracking != null) {
      n.add(goodsTracking(goodsTracking));
    }
    NewVehicle newVehicle = v.newVehicle();
    if (newVehicle != null) {
      n.add(newVehicle(newVehicle));
    }
    return n;
  }

  Node newVehicle(NewVehicle v) {
    Node n = Node.group("gVehNuevo", "E770");
    n.addIfPresent("iTipOpVN", "E771", v.saleOperationTypeCode());
    n.addIfPresent(
        "dDesTipOpVN",
        "E772",
        descriptions.of("E771", v.saleOperationTypeCode(), v.saleOperationTypeDescription()));
    n.addIfPresent("dChasis", "E773", v.chassis());
    n.addIfPresent("dColor", "E774", v.color());
    n.addIfPresent("dPotencia", "E775", Format.integerOrNull(v.enginePower()));
    n.addIfPresent("dCapMot", "E776", Format.integerOrNull(v.engineCapacity()));
    n.addIfPresent("dPNet", "E777", Format.amountOrNull(v.netWeight()));
    n.addIfPresent("dPBruto", "E778", Format.amountOrNull(v.grossWeight()));
    n.addIfPresent("iTipCom", "E779", v.fuelTypeCode());
    n.addIfPresent(
        "dDesTipCom", "E780", descriptions.of("E779", v.fuelTypeCode(), v.fuelTypeDescription()));
    n.addIfPresent("dNroMotor", "E781", v.engineNumber());
    n.addIfPresent("dCapTracc", "E782", Format.amountOrNull(v.maxTractionCapacity()));
    n.addIfPresent("dAnoFab", "E783", Format.integerOrNull(v.manufactureYear()));
    n.addIfPresent("cTipVeh", "E784", v.vehicleType());
    n.addIfPresent("dCapac", "E785", Format.integerOrNull(v.passengerCapacity()));
    n.addIfPresent("dCilin", "E786", v.cylinders());
    return n;
  }

  Node goodsTracking(GoodsTracking v) {
    Node n = Node.group("gRasMerc", "E750");
    n.addIfPresent("dNumLote", "E751", v.lotNumber());
    n.addIfPresent("dVencMerc", "E752", Format.dateOrNull(v.expirationDate()));
    n.addIfPresent("dNSerie", "E753", v.serialNumber());
    n.addIfPresent("dNumPedi", "E754", v.orderNumber());
    n.addIfPresent("dNumSegui", "E755", v.shipmentTrackingNumber());
    n.addIfPresent("dNumReg", "E759", v.productRegistrationNumber());
    n.addIfPresent("dNumRegEntCom", "E760", v.commercialEntityRegistrationNumber());
    n.addIfPresent("dNomPro", "E761", v.productName());
    return n;
  }

  Node itemVat(ItemVat v) {
    Node n = Node.group("gCamIVA", "E730");
    n.add(Node.leaf("iAfecIVA", "E731", v.treatmentCode()));
    n.addIfPresent(
        "dDesAfecIVA",
        "E732",
        descriptions.of("E731", v.treatmentCode(), v.treatmentDescription()));
    n.add(Node.leaf("dPropIVA", "E733", Format.amount(v.taxableProportionRate())));
    n.add(Node.leaf("dTasaIVA", "E734", Format.amount(v.rate())));
    n.add(Node.leaf("dBasGravIVA", "E735", Format.amount(v.taxableBase())));
    n.add(Node.leaf("dLiqIVAItem", "E736", Format.amount(v.amount())));
    n.add(Node.leaf("dBasExe", "E737", Format.amount(v.exemptBase())));
    return n;
  }

  Node itemValues(ItemValues v) {
    Node n = Node.group("gValorItem", "E720");
    n.add(Node.leaf("dPUniProSer", "E721", Format.amount(v.unitPrice())));
    n.addIfPresent("dTiCamIt", "E725", Format.amountOrNull(v.exchangeRate()));
    n.add(Node.leaf("dTotBruOpeItem", "E727", Format.amount(v.grossTotal())));
    n.add(itemDiscountsAndAdvances(v.discountsAndAdvances()));
    return n;
  }

  Node itemDiscountsAndAdvances(ItemDiscountsAndAdvances v) {
    Node n = Node.group("gValorRestaItem", "EA001");
    n.addIfPresent("dDescItem", "EA002", Format.amountOrNull(v.discountAmount()));
    n.addIfPresent("dPorcDesIt", "EA003", Format.amountOrNull(v.discountRate()));
    n.addIfPresent("dDescGloItem", "EA004", Format.amountOrNull(v.globalDiscountAmount()));
    n.addIfPresent("dAntPreUniIt", "EA006", Format.amountOrNull(v.advanceAmount()));
    n.addIfPresent("dAntGloPreUniIt", "EA007", Format.amountOrNull(v.globalAdvanceAmount()));
    n.add(Node.leaf("dTotOpeItem", "EA008", Format.amount(v.total())));
    n.addIfPresent("dTotOpeGs", "EA009", Format.amountOrNull(v.guaraniTotal()));
    return n;
  }

  Node operationCondition(OperationCondition v) {
    Node n = Node.group("gCamCond", "E600");
    n.add(Node.leaf("iCondOpe", "E601", v.conditionCode()));
    n.addIfPresent(
        "dDCondOpe", "E602", descriptions.of("E601", v.conditionCode(), v.conditionDescription()));
    for (Payment e : v.payments()) {
      n.add(payment(e));
    }
    Credit credit = v.credit();
    if (credit != null) {
      n.add(credit(credit));
    }
    return n;
  }

  Node credit(Credit v) {
    Node n = Node.group("gPagCred", "E640");
    n.add(Node.leaf("iCondCred", "E641", v.conditionCode()));
    n.addIfPresent(
        "dDCondCred", "E642", descriptions.of("E641", v.conditionCode(), v.conditionDescription()));
    n.addIfPresent("dPlazoCre", "E643", v.term());
    n.addIfPresent("dCuotas", "E644", Format.integerOrNull(v.installmentCount()));
    n.addIfPresent("dMonEnt", "E645", Format.amountOrNull(v.downPaymentAmount()));
    for (Installment e : v.installments()) {
      n.add(installment(e));
    }
    return n;
  }

  Node installment(Installment v) {
    Node n = Node.group("gCuotas", "E650");
    n.add(Node.leaf("cMoneCuo", "E653", v.currencyCode()));
    n.addIfPresent(
        "dDMoneCuo", "E654", descriptions.of("E653", v.currencyCode(), v.currencyDescription()));
    n.add(Node.leaf("dMonCuota", "E651", Format.amount(v.amount())));
    n.addIfPresent("dVencCuo", "E652", Format.dateOrNull(v.dueDate()));
    return n;
  }

  Node payment(Payment v) {
    Node n = Node.group("gPaConEIni", "E605");
    n.add(Node.leaf("iTiPago", "E606", v.typeCode()));
    n.addIfPresent("dDesTiPag", "E607", descriptions.of("E606", v.typeCode(), v.typeDescription()));
    n.add(Node.leaf("dMonTiPag", "E608", Format.amount(v.amount())));
    n.add(Node.leaf("cMoneTiPag", "E609", v.currencyCode()));
    n.addIfPresent(
        "dDMoneTiPag", "E610", descriptions.of("E609", v.currencyCode(), v.currencyDescription()));
    n.addIfPresent("dTiCamTiPag", "E611", Format.amountOrNull(v.exchangeRate()));
    CardPayment card = v.card();
    if (card != null) {
      n.add(cardPayment(card));
    }
    CheckPayment check = v.check();
    if (check != null) {
      n.add(checkPayment(check));
    }
    return n;
  }

  Node checkPayment(CheckPayment v) {
    Node n = Node.group("gPagCheq", "E630");
    n.add(Node.leaf("dNumCheq", "E631", v.number()));
    n.add(Node.leaf("dBcoEmi", "E632", v.issuingBank()));
    return n;
  }

  Node cardPayment(CardPayment v) {
    Node n = Node.group("gPagTarCD", "E620");
    n.add(Node.leaf("iDenTarj", "E621", v.brandCode()));
    n.addIfPresent(
        "dDesDenTarj", "E622", descriptions.of("E621", v.brandCode(), v.brandDescription()));
    n.addIfPresent("dRSProTar", "E623", v.processorName());
    n.addIfPresent("dRUCProTar", "E624", v.processorRuc());
    n.addIfPresent("dDVProTar", "E625", v.processorCheckDigit());
    n.add(Node.leaf("iForProPa", "E626", v.processingMethodCode()));
    n.addIfPresent("dCodAuOpe", "E627", v.authorizationCode());
    n.addIfPresent("dNomTit", "E628", v.holderName());
    n.addIfPresent("dNumTarj", "E629", v.number());
    return n;
  }

  Node creditDebitNote(CreditDebitNote v) {
    Node n = Node.group("gCamNCDE", "E400");
    n.add(Node.leaf("iMotEmi", "E401", v.reasonCode()));
    n.addIfPresent(
        "dDesMotEmi", "E402", descriptions.of("E401", v.reasonCode(), v.reasonDescription()));
    return n;
  }

  Node invoice(Invoice v) {
    Node n = Node.group("gCamFE", "E010");
    n.add(Node.leaf("iIndPres", "E011", v.presenceIndicatorCode()));
    n.addIfPresent(
        "dDesIndPres",
        "E012",
        descriptions.of("E011", v.presenceIndicatorCode(), v.presenceIndicatorDescription()));
    n.addIfPresent("dFecEmNR", "E013", Format.dateOrNull(v.futureShipmentDate()));
    PublicProcurement publicProcurement = v.publicProcurement();
    if (publicProcurement != null) {
      n.add(publicProcurement(publicProcurement));
    }
    return n;
  }

  Node publicProcurement(PublicProcurement v) {
    Node n = Node.group("gCompPub", "E020");
    n.add(Node.leaf("dModCont", "E021", v.modalityCode()));
    n.add(Node.leaf("dEntCont", "E022", v.entityCode()));
    n.add(Node.leaf("dAnoCont", "E023", v.yearCode()));
    n.add(Node.leaf("dSecCont", "E024", v.sequence()));
    n.add(Node.leaf("dFeCodCont", "E025", Format.date(v.contractingCodeIssueDate())));
    return n;
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
    n.addIfPresent("iTipIDRec", "D208", v.idTypeCode());
    n.addIfPresent(
        "dDTipIDRec", "D209", descriptions.of("D208", v.idTypeCode(), v.idTypeDescription()));
    n.addIfPresent("dNumIDRec", "D210", v.idNumber());
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
    n.add(Node.leaf("iTipIDRespDE", "D141", v.idTypeCode()));
    n.addIfPresent(
        "dDTipIDRespDE", "D142", descriptions.of("D141", v.idTypeCode(), v.idTypeDescription()));
    n.add(Node.leaf("dNumIDRespDE", "D143", v.idNumber()));
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
