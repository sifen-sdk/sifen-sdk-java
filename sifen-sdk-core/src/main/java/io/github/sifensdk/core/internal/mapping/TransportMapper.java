/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.mapping;

import io.github.sifensdk.core.internal.tree.Node;
import io.github.sifensdk.core.model.Carrier;
import io.github.sifensdk.core.model.Transport;
import io.github.sifensdk.core.model.TransportDelivery;
import io.github.sifensdk.core.model.TransportDeparture;
import io.github.sifensdk.core.model.TransportVehicle;

/**
 * Maps the transport of goods ({@code gTransp}, E900): departure and delivery locations, vehicles
 * and carrier, in the order of the XSD sequence {@code tgTransp}.
 */
final class TransportMapper {

  private final Descriptions descriptions;

  TransportMapper(Descriptions descriptions) {
    this.descriptions = descriptions;
  }

  Node transport(Transport v) {
    Node n = Node.group("gTransp", "E900");
    n.addIfPresent("iTipTrans", "E901", v.typeCode());
    n.addIfPresent(
        "dDesTipTrans", "E902", descriptions.of("E901", v.typeCode(), v.typeDescription()));
    n.add(Node.leaf("iModTrans", "E903", v.modeCode()));
    n.addIfPresent(
        "dDesModTrans", "E904", descriptions.of("E903", v.modeCode(), v.modeDescription()));
    n.add(Node.leaf("iRespFlete", "E905", v.freightPayerCode()));
    n.addIfPresent("cCondNeg", "E906", v.negotiationConditionCode());
    n.addIfPresent("dNuManif", "E907", v.manifestNumber());
    n.addIfPresent("dNuDespImp", "E908", v.importDispatchNumber());
    n.addIfPresent("dIniTras", "E909", Format.dateOrNull(v.estimatedStartDate()));
    n.addIfPresent("dFinTras", "E910", Format.dateOrNull(v.estimatedEndDate()));
    n.addIfPresent("cPaisDest", "E911", v.destinationCountryCode());
    n.addIfPresent(
        "dDesPaisDest",
        "E912",
        descriptions.of("E911", v.destinationCountryCode(), v.destinationCountryDescription()));
    TransportDeparture departure = v.departureLocation();
    if (departure != null) {
      n.add(departure(departure));
    }
    for (TransportDelivery e : v.deliveryLocations()) {
      n.add(delivery(e));
    }
    for (TransportVehicle e : v.vehicles()) {
      n.add(vehicle(e));
    }
    Carrier carrier = v.carrier();
    if (carrier != null) {
      n.add(carrier(carrier));
    }
    return n;
  }

  Node departure(TransportDeparture v) {
    Node n = Node.group("gCamSal", "E920");
    n.add(Node.leaf("dDirLocSal", "E921", v.address()));
    n.add(Node.leaf("dNumCasSal", "E922", v.houseNumber()));
    n.addIfPresent("dComp1Sal", "E923", v.addressComplement1());
    n.addIfPresent("dComp2Sal", "E924", v.addressComplement2());
    n.add(Node.leaf("cDepSal", "E925", v.departmentCode()));
    n.addIfPresent(
        "dDesDepSal",
        "E926",
        descriptions.of("E925", v.departmentCode(), v.departmentDescription()));
    n.addIfPresent("cDisSal", "E927", v.districtCode());
    n.addIfPresent(
        "dDesDisSal", "E928", descriptions.of("E927", v.districtCode(), v.districtDescription()));
    n.add(Node.leaf("cCiuSal", "E929", v.cityCode()));
    n.addIfPresent(
        "dDesCiuSal", "E930", descriptions.of("E929", v.cityCode(), v.cityDescription()));
    n.addIfPresent("dTelSal", "E931", v.phone());
    return n;
  }

  Node delivery(TransportDelivery v) {
    Node n = Node.group("gCamEnt", "E940");
    n.add(Node.leaf("dDirLocEnt", "E941", v.address()));
    n.add(Node.leaf("dNumCasEnt", "E942", v.houseNumber()));
    n.addIfPresent("dComp1Ent", "E943", v.addressComplement1());
    n.addIfPresent("dComp2Ent", "E944", v.addressComplement2());
    n.add(Node.leaf("cDepEnt", "E945", v.departmentCode()));
    n.addIfPresent(
        "dDesDepEnt",
        "E946",
        descriptions.of("E945", v.departmentCode(), v.departmentDescription()));
    n.addIfPresent("cDisEnt", "E947", v.districtCode());
    n.addIfPresent(
        "dDesDisEnt", "E948", descriptions.of("E947", v.districtCode(), v.districtDescription()));
    n.add(Node.leaf("cCiuEnt", "E949", v.cityCode()));
    n.addIfPresent(
        "dDesCiuEnt", "E950", descriptions.of("E949", v.cityCode(), v.cityDescription()));
    n.addIfPresent("dTelEnt", "E951", v.phone());
    return n;
  }

  Node vehicle(TransportVehicle v) {
    Node n = Node.group("gVehTras", "E960");
    n.add(Node.leaf("dTiVehTras", "E961", v.type()));
    n.add(Node.leaf("dMarVeh", "E962", v.brand()));
    n.add(Node.leaf("dTipIdenVeh", "E967", v.identificationTypeCode()));
    n.addIfPresent("dNroIDVeh", "E963", v.identificationNumber());
    n.addIfPresent("dAdicVeh", "E964", v.additionalData());
    n.addIfPresent("dNroMatVeh", "E965", v.plateNumber());
    n.addIfPresent("dNroVuelo", "E966", v.flightNumber());
    return n;
  }

  Node carrier(Carrier v) {
    Node n = Node.group("gCamTrans", "E980");
    n.add(Node.leaf("iNatTrans", "E981", v.natureCode()));
    n.add(Node.leaf("dNomTrans", "E982", v.name()));
    n.addIfPresent("dRucTrans", "E983", v.ruc());
    n.addIfPresent("dDVTrans", "E984", v.checkDigit());
    n.addIfPresent("iTipIDTrans", "E985", v.idDocumentTypeCode());
    n.addIfPresent(
        "dDTipIDTrans",
        "E986",
        descriptions.of("E985", v.idDocumentTypeCode(), v.idDocumentTypeDescription()));
    n.addIfPresent("dNumIDTrans", "E987", v.idDocumentNumber());
    n.addIfPresent("cNacTrans", "E988", v.nationalityCode());
    n.addIfPresent(
        "dDesNacTrans",
        "E989",
        descriptions.of("E988", v.nationalityCode(), v.nationalityDescription()));
    n.add(Node.leaf("dNumIDChof", "E990", v.driverIdDocumentNumber()));
    n.add(Node.leaf("dNomChof", "E991", v.driverName()));
    n.add(Node.leaf("dDomFisc", "E992", v.taxAddress()));
    n.add(Node.leaf("dDirChof", "E993", v.driverAddress()));
    n.addIfPresent("dNombAg", "E994", v.agentName());
    n.addIfPresent("dRucAg", "E995", v.agentRuc());
    n.addIfPresent("dDVAg", "E996", v.agentCheckDigit());
    n.addIfPresent("dDirAge", "E997", v.agentAddress());
    return n;
  }
}
