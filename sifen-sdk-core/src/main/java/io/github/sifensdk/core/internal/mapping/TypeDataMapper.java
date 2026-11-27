/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.mapping;

import io.github.sifensdk.core.internal.tree.Node;
import io.github.sifensdk.core.model.CreditDebitNote;
import io.github.sifensdk.core.model.DocumentTypeData;
import io.github.sifensdk.core.model.Invoice;
import io.github.sifensdk.core.model.Item;
import io.github.sifensdk.core.model.OperationCondition;
import io.github.sifensdk.core.model.PublicProcurement;

/**
 * Maps the fields specific to each document type ({@code gDtipDE}, E001): the group of the type,
 * the operation condition and the items, delegating each area to its mapper.
 */
final class TypeDataMapper {

  private final Descriptions descriptions;
  private final ItemMapper items;
  private final ConditionMapper conditions;

  TypeDataMapper(Descriptions descriptions) {
    this.descriptions = descriptions;
    this.items = new ItemMapper(descriptions);
    this.conditions = new ConditionMapper(descriptions);
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
      n.add(conditions.operationCondition(operationCondition));
    }
    for (Item e : v.items()) {
      n.add(items.item(e));
    }
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
}
