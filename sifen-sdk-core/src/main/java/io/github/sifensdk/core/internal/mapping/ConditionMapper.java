/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.mapping;

import io.github.sifensdk.core.internal.tree.Node;
import io.github.sifensdk.core.model.CardPayment;
import io.github.sifensdk.core.model.CheckPayment;
import io.github.sifensdk.core.model.Credit;
import io.github.sifensdk.core.model.Installment;
import io.github.sifensdk.core.model.OperationCondition;
import io.github.sifensdk.core.model.Payment;

/** Maps the operation condition ({@code gCamCond}, E600): payments, cards, checks and credit. */
final class ConditionMapper {

  private final Descriptions descriptions;

  ConditionMapper(Descriptions descriptions) {
    this.descriptions = descriptions;
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
}
