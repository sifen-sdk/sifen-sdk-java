/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.spec;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.sifensdk.core.Environment;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class SpecTest {

  private final Spec spec = Spec.bundled();

  @Test
  void loadsEverySectionOfTheBundledSpecification() {
    assertThat(spec.fields()).hasSizeGreaterThan(700);
    assertThat(spec.rules()).hasSizeGreaterThan(1200);
    assertThat(spec.catalogs()).hasSizeGreaterThan(60);
    assertThat(spec.responses()).hasSizeGreaterThan(500);
  }

  @Test
  void everyRuleAndFieldCitesAnOfficialSource() {
    assertThat(spec.rules()).allSatisfy(r -> assertThat(r.sources()).isNotEmpty());
    assertThat(spec.fields().values()).allSatisfy(f -> assertThat(f.sources()).isNotEmpty());
  }

  @Test
  void everyRuleReferencesAnExistingField() {
    assertThat(spec.rules()).allSatisfy(r -> assertThat(spec.fields()).containsKey(r.field()));
  }

  @Test
  void readsFieldsCatalogsAndDescriptionsToEmit() {
    Field total = spec.field("F008").orElseThrow();
    assertThat(total.xml()).isEqualTo("dTotOpe");
    assertThat(spec.catalogs().get("tipo-documento").find("1", null)).isPresent();
    assertThat(spec.catalogs().get("ciudad").entries()).hasSizeGreaterThan(6000);
  }

  @Test
  void validityIsEvaluatedPerEnvironmentAndDate() {
    Validity nt13 =
        new Validity(LocalDate.of(2023, 4, 21), LocalDate.of(2023, 6, 17), null, null, false);
    assertThat(nt13.appliesOn(Environment.TEST, LocalDate.of(2023, 5, 1))).isTrue();
    assertThat(nt13.appliesOn(Environment.PRODUCTION, LocalDate.of(2023, 5, 1))).isFalse();
    assertThat(nt13.appliesOn(Environment.PRODUCTION, LocalDate.of(2023, 6, 17))).isTrue();
  }
}
