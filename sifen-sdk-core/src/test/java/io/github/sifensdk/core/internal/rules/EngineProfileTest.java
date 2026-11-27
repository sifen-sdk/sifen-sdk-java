/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.rules;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.sifensdk.core.internal.spec.Rule;
import org.junit.jupiter.api.Test;

class EngineProfileTest {

  @Test
  void theDocumentEngineOnlyHasRulesOfTheDocument() {
    Engine document = Engine.bundled();
    assertThat(document.inTree("A001")).isTrue();
    assertThat(document.inTree("GEC003")).isFalse();
    assertThat(document.rules()).extracting(Rule::field).doesNotContain("GEC003", "GDE004");
    assertThat(document.inTree("I001")).isFalse();
  }

  @Test
  void theEventEngineHasTheRulesOfTheEventsBelowRGesEve() {
    Engine events = Engine.bundledEvents();
    assertThat(events.inTree("GDE001")).isTrue();
    assertThat(events.inTree("GEC003")).isTrue();
    // Above the root (web service envelope) and the signature are not part of the tree.
    assertThat(events.inTree("GDE000")).isFalse();
    assertThat(events.inTree("GDE008")).isFalse();
    assertThat(events.inTree("A001")).isFalse();
    assertThat(events.rules()).extracting(Rule::field).contains("GEC003").doesNotContain("E727");
  }
}
