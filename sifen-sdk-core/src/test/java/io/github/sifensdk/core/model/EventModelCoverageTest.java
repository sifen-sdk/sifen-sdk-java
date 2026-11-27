/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.sifensdk.core.internal.spec.Field;
import io.github.sifensdk.core.internal.spec.Spec;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

/**
 * ADR-0003 for the events: the event model and the event fields of the specification must not drift
 * apart. Every field of the events a taxpayer sends is in the model, except what the SDK completes
 * or what the XSD does not have.
 */
class EventModelCoverageTest {

  private static final List<Class<?>> TYPES =
      List.of(
          Event.class,
          Cancellation.class,
          NumberInvalidation.class,
          ReceiptNotification.class,
          Conformity.class,
          Disconformity.class,
          NonRecognition.class,
          Nomination.class,
          TransportUpdate.class);

  /** Fields of the enviable events that are deliberately not in the model, with the reason. */
  private static final Map<String, String> EXCLUDED =
      Map.of(
          "GDE000", "gGroupGesEve: web service envelope, built by the client",
          "GDE001", "rGesEve: built by the mapping",
          "GDE004", "dFecFirma: completed by the SDK",
          "GDE005", "dVerFor: completed by the SDK",
          "GDE006", "dTiGDE: not in Evento_v150.xsd",
          "GDE008", "ds:Signature: added by the signer");

  /** Groups ({@code grupo} of {@code spec/campos/ev-*.json}) of the events a taxpayer sends. */
  private static final Set<String> FILES = Set.of("GDE", "GEC", "GEI", "GER", "GENFE", "GET");

  @Test
  void eventModelCoversExactlyTheFieldsOfTheEnviableEvents() {
    Spec spec = Spec.bundled();
    Set<String> expected = new TreeSet<>();
    for (Field f : spec.fields().values()) {
      if ("vigente".equals(f.state())
          && FILES.contains(f.group())
          && !EXCLUDED.containsKey(f.id())) {
        expected.add(f.id());
      }
    }
    Set<String> modelled = new HashSet<>();
    for (Class<?> type : TYPES) {
      SifenField group = type.getAnnotation(SifenField.class);
      modelled.add(group.id());
      for (Method m : type.getDeclaredMethods()) {
        SifenField field = m.getAnnotation(SifenField.class);
        if (field != null) {
          modelled.add(field.id());
        }
      }
    }
    assertThat(new TreeSet<>(modelled)).isEqualTo(expected);
  }
}
