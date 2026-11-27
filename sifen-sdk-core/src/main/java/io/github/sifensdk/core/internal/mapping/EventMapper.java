/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.mapping;

import io.github.sifensdk.core.internal.spec.Spec;
import io.github.sifensdk.core.internal.tree.Node;
import io.github.sifensdk.core.model.Cancellation;
import io.github.sifensdk.core.model.Conformity;
import io.github.sifensdk.core.model.Disconformity;
import io.github.sifensdk.core.model.Event;
import io.github.sifensdk.core.model.EventDetail;
import io.github.sifensdk.core.model.Nomination;
import io.github.sifensdk.core.model.NonRecognition;
import io.github.sifensdk.core.model.NumberInvalidation;
import io.github.sifensdk.core.model.ReceiptNotification;
import io.github.sifensdk.core.model.TransportUpdate;
import java.time.LocalDateTime;

/**
 * Maps an event to the neutral tree: root {@code rGesEve} (GDE001) with {@code rEve} (GDE002,
 * attribute {@code Id}), without {@code Signature}, which the signer adds. Children follow {@code
 * Evento_v150.xsd}. The SDK completes {@code dFecFirma} and {@code dVerFor}; {@code dTiGDE} is not
 * in the XSD and is not written.
 */
public final class EventMapper {

  private EventMapper() {}

  /**
   * Maps an event to its tree.
   *
   * @param event the event
   * @param signatureDateTime the signature date and time ({@code dFecFirma}, GDE004), local time of
   *     Paraguay
   * @return the root {@code rGesEve}
   */
  public static Node toTree(Event event, LocalDateTime signatureDateTime) {
    EventDetailMapper details = new EventDetailMapper(new Descriptions(Spec.bundled()));
    Node rEve = Node.group("rEve", "GDE002").attribute("Id", event.id());
    rEve.add(Node.leaf("dFecFirma", "GDE004", Format.dateTime(signatureDateTime)));
    rEve.add(Node.leaf("dVerFor", "GDE005", DocumentMapper.FORMAT_VERSION));
    rEve.add(Node.group("gGroupTiEvt", "GDE007").add(detail(details, event.detail())));
    return Node.group("rGesEve", "GDE001").add(rEve);
  }

  private static Node detail(EventDetailMapper m, EventDetail d) {
    if (d instanceof Cancellation) {
      return m.cancellation((Cancellation) d);
    }
    if (d instanceof NumberInvalidation) {
      return m.numberInvalidation((NumberInvalidation) d);
    }
    if (d instanceof ReceiptNotification) {
      return m.receiptNotification((ReceiptNotification) d);
    }
    if (d instanceof Conformity) {
      return m.conformity((Conformity) d);
    }
    if (d instanceof Disconformity) {
      return m.disconformity((Disconformity) d);
    }
    if (d instanceof NonRecognition) {
      return m.nonRecognition((NonRecognition) d);
    }
    if (d instanceof Nomination) {
      return m.nomination((Nomination) d);
    }
    return m.transportUpdate((TransportUpdate) d);
  }
}
