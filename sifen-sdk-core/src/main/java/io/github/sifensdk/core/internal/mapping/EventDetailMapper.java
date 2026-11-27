/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.mapping;

import io.github.sifensdk.core.internal.tree.Node;
import io.github.sifensdk.core.model.Cancellation;
import io.github.sifensdk.core.model.Conformity;
import io.github.sifensdk.core.model.Disconformity;
import io.github.sifensdk.core.model.Nomination;
import io.github.sifensdk.core.model.NonRecognition;
import io.github.sifensdk.core.model.NumberInvalidation;
import io.github.sifensdk.core.model.ReceiptNotification;
import io.github.sifensdk.core.model.TransportUpdate;

/**
 * Maps the detail of each event ({@code gGroupTiEvt}, GDE007) to its subtree, children in the order
 * of {@code Evento_v150.xsd}. Values are written as given; descriptions of catalog codes come from
 * the bundled specification. Generated from the specification and kept by hand.
 */
final class EventDetailMapper {

  private final Descriptions descriptions;

  EventDetailMapper(Descriptions descriptions) {
    this.descriptions = descriptions;
  }

  Node cancellation(Cancellation v) {
    Node n = Node.group("rGeVeCan", "GEC001");
    n.add(Node.leaf("Id", "GEC002", v.cdc()));
    n.add(Node.leaf("mOtEve", "GEC003", v.reason()));
    return n;
  }

  Node numberInvalidation(NumberInvalidation v) {
    Node n = Node.group("rGeVeInu", "GEI001");
    n.add(Node.leaf("dNumTim", "GEI002", v.stampNumber()));
    n.add(Node.leaf("dEst", "GEI003", v.establishment()));
    n.add(Node.leaf("dPunExp", "GEI004", v.dispatchPoint()));
    n.add(Node.leaf("dNumIn", "GEI005", v.rangeStartNumber()));
    n.add(Node.leaf("dNumFin", "GEI006", v.rangeEndNumber()));
    n.add(Node.leaf("iTiDE", "GEI007", v.documentTypeCode()));
    n.add(Node.leaf("mOtEve", "GEI008", v.reason()));
    n.addIfPresent("dSerieNum", "GEI009", v.documentSeries());
    return n;
  }

  Node receiptNotification(ReceiptNotification v) {
    Node n = Node.group("rGeVeNotRec", "GEN001");
    n.add(Node.leaf("Id", "GEN002", v.cdc()));
    n.add(Node.leaf("dFecEmi", "GEN003", Format.dateTime(v.issueDateTime())));
    n.add(Node.leaf("dFecRecep", "GEN004", Format.dateTime(v.receptionDateTime())));
    n.add(Node.leaf("iTipRec", "GEN005", v.receiverTypeCode()));
    n.add(Node.leaf("dNomRec", "GEN006", v.receiverName()));
    n.addIfPresent("dRucRec", "GEN007", v.receiverRuc());
    n.addIfPresent("dDVRec", "GEN008", v.receiverCheckDigit());
    n.addIfPresent("dTipIDRec", "GEN009", v.receiverIdDocumentTypeCode());
    n.addIfPresent("dNumID", "GEN010", v.receiverIdDocumentNumber());
    n.add(Node.leaf("dTotalGs", "GEN011", Format.amount(v.operationTotalPyg())));
    return n;
  }

  Node conformity(Conformity v) {
    Node n = Node.group("rGeVeConf", "GCO001");
    n.add(Node.leaf("Id", "GCO002", v.cdc()));
    n.add(Node.leaf("iTipConf", "GCO003", v.typeCode()));
    n.addIfPresent("dFecRecep", "GCO004", Format.dateTimeOrNull(v.estimatedReceptionDateTime()));
    return n;
  }

  Node disconformity(Disconformity v) {
    Node n = Node.group("rGeVeDisconf", "GDI001");
    n.add(Node.leaf("Id", "GDI002", v.cdc()));
    n.add(Node.leaf("mOtEve", "GDI004", v.reason()));
    return n;
  }

  Node nonRecognition(NonRecognition v) {
    Node n = Node.group("rGeVeDescon", "GED001");
    n.add(Node.leaf("Id", "GED002", v.cdc()));
    n.add(Node.leaf("dFecEmi", "GED003", Format.dateTime(v.issueDateTime())));
    n.add(Node.leaf("dFecRecep", "GED004", Format.dateTime(v.receptionDateTime())));
    n.add(Node.leaf("iTipRec", "GED005", v.receiverTypeCode()));
    n.add(Node.leaf("dNomRec", "GED006", v.receiverName()));
    n.addIfPresent("dRucRec", "GED007", v.receiverRuc());
    n.addIfPresent("dDVRec", "GED008", v.receiverCheckDigit());
    n.addIfPresent("dTipIDRec", "GED009", v.receiverIdDocumentTypeCode());
    n.addIfPresent("dNumID", "GED010", v.receiverIdDocumentNumber());
    n.add(Node.leaf("mOtEve", "GED011", v.reason()));
    return n;
  }

  Node nomination(Nomination v) {
    Node n = Node.group("rGEveNom", "GENFE001");
    n.add(Node.leaf("Id", "GENFE002", v.cdc()));
    n.add(Node.leaf("mOtEve", "GENFE003", v.reason()));
    n.add(Node.leaf("iNatRec", "GENFE004", v.receiverNatureCode()));
    n.add(Node.leaf("iTiOpe", "GENFE027", v.operationTypeCode()));
    n.add(Node.leaf("cPaisRec", "GENFE005", v.receiverCountryCode()));
    n.addIfPresent(
        "dDesPaisRe",
        "GENFE006",
        descriptions.of("GENFE005", v.receiverCountryCode(), v.receiverCountryDescription()));
    n.addIfPresent("iTiContRec", "GENFE007", v.receiverTaxpayerTypeCode());
    n.addIfPresent("dRucRec", "GENFE008", v.receiverRuc());
    n.addIfPresent("dDVRec", "GENFE009", v.receiverCheckDigit());
    n.addIfPresent("iTipIDRec", "GENFE010", v.receiverIdDocumentTypeCode());
    n.addIfPresent(
        "dDTipIDRec",
        "GENFE011",
        descriptions.of(
            "GENFE010", v.receiverIdDocumentTypeCode(), v.receiverIdDocumentTypeDescription()));
    n.addIfPresent("dNumIDRec", "GENFE012", v.receiverIdDocumentNumber());
    n.add(Node.leaf("dNomRec", "GENFE013", v.receiverName()));
    n.addIfPresent("dNomFanRec", "GENFE014", v.receiverTradeName());
    n.addIfPresent("dDirRec", "GENFE015", v.receiverAddress());
    n.addIfPresent("dNumCasRec", "GENFE016", v.receiverHouseNumber());
    n.addIfPresent("cDepRec", "GENFE017", v.receiverDepartmentCode());
    n.addIfPresent(
        "dDesDepRec",
        "GENFE018",
        descriptions.of("GENFE017", v.receiverDepartmentCode(), v.receiverDepartmentDescription()));
    n.addIfPresent("cDisRec", "GENFE019", v.receiverDistrictCode());
    n.addIfPresent(
        "dDesDisRec",
        "GENFE020",
        descriptions.of("GENFE019", v.receiverDistrictCode(), v.receiverDistrictDescription()));
    n.addIfPresent("cCiuRec", "GENFE021", v.receiverCityCode());
    n.addIfPresent(
        "dDesCiuRec",
        "GENFE022",
        descriptions.of("GENFE021", v.receiverCityCode(), v.receiverCityDescription()));
    n.addIfPresent("dTelRec", "GENFE023", v.receiverPhone());
    n.addIfPresent("dCelRec", "GENFE024", v.receiverMobilePhone());
    n.addIfPresent("dEmailRec", "GENFE025", v.receiverEmail());
    n.addIfPresent("dCodCliente", "GENFE026", v.customerCode());
    return n;
  }

  Node transportUpdate(TransportUpdate v) {
    Node n = Node.group("rGeVeTr", "GET001");
    n.add(Node.leaf("Id", "GET002", v.cdc()));
    n.add(Node.leaf("dMotEv", "GET003", v.reasonCode()));
    n.addIfPresent("cDepEnt", "GET004", v.deliveryDepartmentCode());
    n.addIfPresent(
        "dDesDepEnt",
        "GET005",
        descriptions.of("GET004", v.deliveryDepartmentCode(), v.deliveryDepartmentDescription()));
    n.addIfPresent("cDisEnt", "GET006", v.deliveryDistrictCode());
    n.addIfPresent(
        "dDesDisEnt",
        "GET007",
        descriptions.of("GET006", v.deliveryDistrictCode(), v.deliveryDistrictDescription()));
    n.addIfPresent("cCiuEnt", "GET008", v.deliveryCityCode());
    n.addIfPresent(
        "dDesCiuEnt",
        "GET009",
        descriptions.of("GET008", v.deliveryCityCode(), v.deliveryCityDescription()));
    n.addIfPresent("dDirEnt", "GET010", v.deliveryAddress());
    n.addIfPresent("dNumCas", "GET011", v.deliveryHouseNumber());
    n.addIfPresent("dCompDir1", "GET012", v.deliveryAddressComplement());
    n.addIfPresent("dNomChof", "GET013", v.driverName());
    n.addIfPresent("dNumIDChof", "GET014", v.driverIdDocumentNumber());
    n.addIfPresent("iNatTrans", "GET015", v.carrierNatureCode());
    n.addIfPresent("dRucTrans", "GET016", v.carrierRuc());
    n.addIfPresent("dDVTrans", "GET017", v.carrierCheckDigit());
    n.addIfPresent("dNomTrans", "GET018", v.carrierName());
    n.addIfPresent("iTipIDTrans", "GET019", v.carrierIdDocumentTypeCode());
    n.addIfPresent(
        "dDTipIDTrans",
        "GET020",
        descriptions.of(
            "GET019", v.carrierIdDocumentTypeCode(), v.carrierIdDocumentTypeDescription()));
    n.addIfPresent("dNumIDTrans", "GET021", v.carrierIdDocumentNumber());
    n.addIfPresent("iTipTrans", "GET022", v.transportTypeCode());
    n.addIfPresent(
        "dDesTipTrans",
        "GET023",
        descriptions.of("GET022", v.transportTypeCode(), v.transportTypeDescription()));
    n.addIfPresent("iModTrans", "GET024", v.transportModeCode());
    n.addIfPresent(
        "dDesModTrans",
        "GET025",
        descriptions.of("GET024", v.transportModeCode(), v.transportModeDescription()));
    n.addIfPresent("dTiVehTras", "GET026", v.vehicleType());
    n.addIfPresent("dMarVeh", "GET027", v.vehicleBrand());
    n.addIfPresent("dTipIdenVeh", "GET028", v.vehicleIdentificationTypeCode());
    n.addIfPresent("dNroIDVeh", "GET029", v.vehicleIdentificationNumber());
    n.addIfPresent("dNroMatVeh", "GET030", v.vehicleRegistrationNumber());
    return n;
  }
}
