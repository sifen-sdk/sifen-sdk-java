/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.mapping;

import io.github.sifensdk.core.internal.tree.Node;
import io.github.sifensdk.core.model.DeliveryNote;

/** Maps the fields of the electronic delivery note ({@code gCamNRE}, E500). */
final class DeliveryNoteMapper {

  private final Descriptions descriptions;

  DeliveryNoteMapper(Descriptions descriptions) {
    this.descriptions = descriptions;
  }

  Node deliveryNote(DeliveryNote v) {
    Node n = Node.group("gCamNRE", "E500");
    n.add(Node.leaf("iMotEmiNR", "E501", v.reasonCode()));
    // C-206: the catalog emits the XSD text ("Traslado de bienes para reparación").
    n.addIfPresent(
        "dDesMotEmiNR", "E502", descriptions.of("E501", v.reasonCode(), v.reasonDescription()));
    n.add(Node.leaf("iRespEmiNR", "E503", v.responsibleCode()));
    n.addIfPresent(
        "dDesRespEmiNR",
        "E504",
        descriptions.of("E503", v.responsibleCode(), v.responsibleDescription()));
    n.add(Node.leaf("dKmR", "E505", Format.integer(v.estimatedKilometers())));
    n.addIfPresent("dFecEm", "E506", Format.dateOrNull(v.futureInvoiceDate()));
    n.addIfPresent("cPreFle", "E507", Format.amountOrNull(v.freightAmount()));
    return n;
  }
}
