/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.module.ModuleDescriptor;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class EnvironmentTest {

  @Test
  void testAndProductionNeverShareAnAddress() {
    assertThat(Environment.TEST.webServiceBaseUri())
        .isNotEqualTo(Environment.PRODUCTION.webServiceBaseUri());
    assertThat(Environment.TEST.qrBaseUri()).isNotEqualTo(Environment.PRODUCTION.qrBaseUri());
  }

  @Test
  void everyAddressUsesHttps() {
    for (Environment environment : Environment.values()) {
      assertThat(environment.webServiceBaseUri().getScheme()).isEqualTo("https");
      assertThat(environment.qrBaseUri().getScheme()).isEqualTo("https");
    }
  }

  @Test
  void qrAddressesMatchTheOnesPublishedBySifen() {
    assertThat(Environment.PRODUCTION.qrBaseUri())
        .hasToString("https://ekuatia.set.gov.py/consultas/qr");
    assertThat(Environment.TEST.qrBaseUri())
        .hasToString("https://ekuatia.set.gov.py/consultas-test/qr");
  }

  /** ADR-0002: the core depends on the JDK only. */
  @Test
  void coreModuleRequiresOnlyTheJdk() {
    ModuleDescriptor descriptor = Environment.class.getModule().getDescriptor();
    assertThat(descriptor).as("tests must run on the module path").isNotNull();
    assertThat(
            descriptor.requires().stream()
                .filter(r -> !r.modifiers().contains(ModuleDescriptor.Requires.Modifier.STATIC))
                .map(ModuleDescriptor.Requires::name)
                .collect(Collectors.toSet()))
        .allMatch(name -> name.startsWith("java."));
  }
}
