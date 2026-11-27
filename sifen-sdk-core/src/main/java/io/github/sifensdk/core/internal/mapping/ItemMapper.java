/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.mapping;

import io.github.sifensdk.core.internal.tree.Node;
import io.github.sifensdk.core.model.GoodsTracking;
import io.github.sifensdk.core.model.Item;
import io.github.sifensdk.core.model.ItemDiscountsAndAdvances;
import io.github.sifensdk.core.model.ItemValues;
import io.github.sifensdk.core.model.ItemVat;
import io.github.sifensdk.core.model.NewVehicle;

/** Maps the items ({@code gCamItem}, E700) and their groups. */
final class ItemMapper {

  private final Descriptions descriptions;

  ItemMapper(Descriptions descriptions) {
    this.descriptions = descriptions;
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
    ItemValues values = v.values();
    if (values != null) {
      n.add(itemValues(values));
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
}
