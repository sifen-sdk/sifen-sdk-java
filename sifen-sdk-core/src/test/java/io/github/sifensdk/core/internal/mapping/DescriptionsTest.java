/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.sifensdk.core.internal.spec.Spec;
import org.junit.jupiter.api.Test;

class DescriptionsTest {

  private final Descriptions descriptions = new Descriptions(Spec.bundled());

  @Test
  void theCatalogDescriptionWinsForAKnownCode() {
    assertThat(descriptions.of("C002", "5", null)).isEqualTo("Nota de crédito electrónica");
    assertThat(descriptions.of("C002", "5", "otra cosa")).isEqualTo("Nota de crédito electrónica");
    assertThat(descriptions.of("E606", "1", null)).isEqualTo("Efectivo");
  }

  @Test
  void usesTheXsdTextWhenTheCatalogRecordsOne() {
    // C-309: the XSD enumeration spells "Venta al Consumidor final".
    assertThat(descriptions.of("E771", "2", null)).isEqualTo("Venta al Consumidor final");
  }

  @Test
  void theModelBringsFreeDescriptions() {
    assertThat(descriptions.of("E011", "9", "Venta por catálogo")).isEqualTo("Venta por catálogo");
    assertThat(descriptions.of("E606", "99", "Canje")).isEqualTo("Canje");
    assertThat(descriptions.of("E011", "9", null)).isNull();
  }

  @Test
  void theModelBringsTheDescriptionOfACodeTheCatalogDoesNotKnow() {
    assertThat(descriptions.of("E709", "999999", "XYZ")).isEqualTo("XYZ");
    assertThat(descriptions.of("E709", "999999", null)).isNull();
  }

  @Test
  void withoutCodeTheGivenDescriptionIsKept() {
    assertThat(descriptions.of("D011", null, null)).isNull();
  }
}
