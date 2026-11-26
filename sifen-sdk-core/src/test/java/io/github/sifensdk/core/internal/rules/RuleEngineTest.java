/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.rules;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.sifensdk.core.Environment;
import io.github.sifensdk.core.internal.tree.Node;
import io.github.sifensdk.core.validation.Severity;
import io.github.sifensdk.core.validation.ValidationIssue;
import io.github.sifensdk.core.validation.ValidationResult;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** The rule engine on hand-built trees, with the bundled specification. */
class RuleEngineTest {

  static final LocalDate ISSUED = LocalDate.of(2025, 3, 10);

  static ValidationResult evaluate(SampleDocument doc) {
    return RuleEngine.evaluate(doc.build(), Environment.TEST, ISSUED);
  }

  static List<ValidationIssue> issues(ValidationResult result, String ruleId) {
    return result.issues().stream().filter(i -> i.ruleId().equals(ruleId)).toList();
  }

  static Set<String> ruleIds(ValidationResult result) {
    return result.issues().stream().map(ValidationIssue::ruleId).collect(Collectors.toSet());
  }

  @Test
  void aConsistentInvoiceAndCreditNoteHaveNoIssues() {
    assertThat(evaluate(SampleDocument.invoice()).issues()).isEmpty();
    assertThat(evaluate(SampleDocument.creditNote()).issues()).isEmpty();
  }

  @Test
  void aMissingRequiredFieldIsReportedAtItsParentWithRuleAndSource() {
    ValidationResult result = evaluate(SampleDocument.invoice().omit("D101"));

    assertThat(result.isValid()).isFalse();
    ValidationIssue issue = issues(result, "R-D101-O1").get(0);
    assertThat(issue.severity()).isEqualTo(Severity.ERROR);
    assertThat(issue.fieldId()).isEqualTo("D101");
    assertThat(issue.xmlName()).isEqualTo("dRucEm");
    assertThat(issue.path()).isEqualTo("rDE/DE/gDatGralOpe/gEmis");
    assertThat(issue.sources()).containsExactly("MT150 D101");
    assertThat(issue.message()).isEqualTo("Required field is missing");
  }

  @Test
  void aRequiredFieldIsRequiredInEveryInstanceOfItsParent() {
    ValidationResult result =
        evaluate(
            SampleDocument.invoice()
                .items(3)
                .itemValues((i, id) -> i == 1 && id.equals("E708") ? SampleDocument.OMIT : null));

    assertThat(issues(result, "R-E708-O1"))
        .singleElement()
        .satisfies(i -> assertThat(i.path()).isEqualTo("rDE/DE/gDtipDE/gCamItem[2]"));
  }

  @Test
  void aRequiredFieldOfAnAbsentGroupIsNotRequired() {
    // gRespDE (D140) is optional and absent: its required children do not apply.
    ValidationResult result = evaluate(SampleDocument.invoice());
    assertThat(ruleIds(result)).doesNotContain("R-D141-O1", "R-D143-O1");
  }

  @Test
  void aConditionalFieldIsRequiredOnlyWhenTheConditionHolds() {
    // D208 is required for a non-taxpayer receiver (D201 = 2, D202 != 4): R-D208-C1.
    ValidationResult applies = evaluate(SampleDocument.invoice().omit("D208").omit("D209"));
    assertThat(issues(applies, "R-D208-C1")).hasSize(1);

    ValidationResult doesNotApply =
        evaluate(SampleDocument.invoice().set("D201", "1").omit("D208").omit("D209"));
    assertThat(ruleIds(doesNotApply)).doesNotContain("R-D208-C1");
  }

  @Test
  void aProhibitedFieldIsReportedAtTheField() {
    // D011 must not be informed when C002 is not 1 or 4 (NT 6, 1216).
    ValidationResult result =
        evaluate(
            SampleDocument.invoice().set("C002", "5").set("C003", "Nota de crédito electrónica"));
    ValidationIssue issue = issues(result, "R-D011-P1").get(0);
    assertThat(issue.path()).isEqualTo("rDE/DE/gDatGralOpe/gOpeCom/iTipTra");
    assertThat(issue.sifenCode()).isEqualTo("1216");
  }

  @Test
  void aConditionOnTheSameRepeatedGroupUsesTheSameItem() {
    // R-E735-A1: E735 = 0 when E731 is 2 or 3, read from the same item.
    SampleDocument mixed =
        SampleDocument.invoice()
            .items(2)
            .itemValues((i, id) -> {
              if (i == 0) {
                switch (id) {
                  case "E731":
                    return "3";
                  case "E732":
                    return "Exento";
                  case "E733":
                  case "E734":
                  case "E735":
                  case "E736":
                    return "0";
                  default:
                    return null;
                }
              }
              return null;
            });
    assertThat(ruleIds(evaluate(mixed))).doesNotContain("R-E735-A1");

    SampleDocument wrong =
        SampleDocument.invoice()
            .items(2)
            .itemValues((i, id) ->
                i == 1 && id.equals("E731") ? "3" : i == 1 && id.equals("E732") ? "Exento" : null);
    assertThat(issues(evaluate(wrong), "R-E735-A1"))
        .singleElement()
        .satisfies(
            i -> assertThat(i.path()).isEqualTo("rDE/DE/gDtipDE/gCamItem[2]/gCamIVA/dBasGravIVA"));
  }

  @Test
  void aConditionOnAnotherRepeatedGroupHoldsWhenAnyItemSatisfiesIt() {
    // R-F002-C1: the exempt subtotal is required when some item is exempt (E731 = 3).
    SampleDocument oneExempt =
        SampleDocument.invoice()
            .items(3)
            .itemValues((i, id) -> i == 2 && id.equals("E731") ? "3" : null);
    assertThat(issues(evaluate(oneExempt), "R-F002-C1")).hasSize(1);
    assertThat(ruleIds(evaluate(SampleDocument.invoice().items(3)))).doesNotContain("R-F002-C1");
  }

  @Test
  void theUnnamedReceiverLimitFollowsTheTechnicalNotesByDate() {
    // F014 = 10,000,000 PYG with an unnamed receiver (D208 = 5).
    LocalDate before2024 = LocalDate.of(2023, 10, 2);
    LocalDate nt21 = LocalDate.of(2024, 6, 3);
    LocalDate nt24 = LocalDate.of(2025, 3, 10);
    for (LocalDate date : List.of(before2024, nt21, nt24)) {
      SampleDocument doc =
          SampleDocument.invoice()
              .set("D002", date + "T09:00:00")
              .set("C008", "2023-01-02")
              .set("D208", "5")
              .set("D209", "Innominado")
              .set("D210", "0")
              .set("D211", "Sin Nombre")
              .set("F014", "10000000");
      ValidationResult result = RuleEngine.evaluate(doc.build(), Environment.PRODUCTION, date);
      Set<String> ids = ruleIds(result);
      if (date.equals(nt24)) {
        assertThat(ids).contains("R-D208-F5").doesNotContain("R-D208-F3", "R-D208-F4");
      } else {
        assertThat(ids).doesNotContain("R-D208-F3", "R-D208-F4", "R-D208-F5");
      }
    }
  }

  @Test
  void theVatBaseFormulaChangesWithTheNt13PerEnvironment() {
    // Partially taxed item (E731 = 4, E733 = 50, 10%): NT 13 gives 52380.95..., the formula
    // before it gives 50000. On 2023-05-01 the NT 13 applies in test but not yet in production.
    LocalDate date = LocalDate.of(2023, 5, 2);
    SampleDocument doc =
        SampleDocument.invoice()
            .set("D002", "2023-05-02T09:00:00")
            .set("C008", "2023-01-02")
            .set("E731", "4")
            .set("E732", SampleDocument.description("afectacion-iva", "4"))
            .set("E733", "50")
            .set("E735", "52381");
    ValidationResult test = RuleEngine.evaluate(doc.build(), Environment.TEST, date);
    ValidationResult production = RuleEngine.evaluate(doc.build(), Environment.PRODUCTION, date);

    assertThat(ruleIds(test)).doesNotContain("R-E735-A5", "R-E735-A3");
    assertThat(ruleIds(production)).contains("R-E735-A3").doesNotContain("R-E735-A5");
  }

  @Test
  void aSupersededRuleWithoutEndDateYieldsToItsReplacement() {
    // R-F009-A1 (sum of EA002) is superseded by R-F009-A2 (sum of EA002 * E711), NT 1.
    SampleDocument doc =
        SampleDocument.invoice().set("E711", "2").set("EA002", "1000").set("F009", "2000");
    ValidationResult result = evaluate(doc);
    assertThat(ruleIds(result)).doesNotContain("R-F009-A1", "R-F009-A2");
    assertThat(result.uncheckedRules()).doesNotContain("R-F009-A1");
  }

  @Test
  void anUnknownOrNotYetValidCatalogCodeIsReported() {
    ValidationResult unknown = evaluate(SampleDocument.invoice().set("E709", "99999"));
    assertThat(issues(unknown, "CATALOG"))
        .singleElement()
        .satisfies(i -> {
          assertThat(i.fieldId()).isEqualTo("E709");
          assertThat(i.message()).isEqualTo("Code is not in the catalog 'unidad-medida'");
        });

    // Unit 111 exists only since the NT 23 (2024-09-27 in production).
    SampleDocument early =
        SampleDocument.invoice()
            .set("D002", "2024-05-02T09:00:00")
            .set("C008", "2024-01-02")
            .set("E709", "111")
            .set("E710", SampleDocument.description("unidad-medida", "111"));
    ValidationResult before =
        RuleEngine.evaluate(early.build(), Environment.PRODUCTION, LocalDate.of(2024, 5, 2));
    assertThat(issues(before, "CATALOG"))
        .singleElement()
        .satisfies(i -> assertThat(i.message()).startsWith("Code is not in force"));

    // Document type 2 (export invoice) is documented as future.
    assertThat(issues(evaluate(SampleDocument.invoice().set("C002", "2")), "CATALOG"))
        .extracting(ValidationIssue::fieldId)
        .contains("C002");
  }

  @Test
  void aDescriptionMustMatchItsCode() {
    ValidationResult result = evaluate(SampleDocument.invoice().set("D016", "Dolar"));
    assertThat(issues(result, "R-D016-D1"))
        .singleElement()
        .satisfies(i ->
            assertThat(i.message())
                .isEqualTo(
                    "Does not match the catalog description of the code in cMoneOpe (D015)"));
  }

  @Test
  void departmentDistrictAndCityMustBeRelated() {
    ValidationResult result =
        evaluate(
            SampleDocument.invoice()
                .set("D111", "2")
                .set("D112", SampleDocument.description("departamento", "2")));
    assertThat(ruleIds(result)).contains("R-D111-F1", "R-D115-F1");
  }

  @Test
  void theCdcAndTheCheckDigitsMustMatchTheirFields() {
    assertThat(ruleIds(evaluate(SampleDocument.invoice().set("A003", "0"))))
        .contains("R-A003-F1", "R-A002-D1");
    assertThat(ruleIds(evaluate(SampleDocument.invoice().set("D102", "3"))))
        .contains("R-D102-F1")
        .doesNotContain("R-A002-D1");
    assertThat(
            ruleIds(
                evaluate(
                    SampleDocument.invoice()
                        .set("A002", "01444444017001001000000122025031015873260980"))))
        .contains("R-A002-D1");
  }

  @Test
  void valueRulesThatCanBeCheckedOfflineAreChecked() {
    assertThat(ruleIds(evaluate(SampleDocument.invoice().set("C007", "1")))).contains("R-C007-F1");
    assertThat(ruleIds(evaluate(SampleDocument.invoice().set("E701", "x".repeat(51)))))
        .contains("R-E701-F2");
    assertThat(ruleIds(evaluate(SampleDocument.invoice().set("E711", "1.123456789"))))
        .contains("R-E711-F2");
    assertThat(ruleIds(evaluate(SampleDocument.invoice().set("B002", "2")))).contains("R-B002-F1");
    assertThat(ruleIds(evaluate(SampleDocument.invoice().set("C008", "2025-03-11"))))
        .contains("R-C004-F4");
    assertThat(ruleIds(evaluate(SampleDocument.invoice().set("C008", "2025-03-10"))))
        .doesNotContain("R-C004-F4");
    ValidationResult production =
        RuleEngine.evaluate(SampleDocument.invoice().build(), Environment.PRODUCTION, ISSUED);
    assertThat(ruleIds(production)).contains("R-D105-F1");
  }

  @Test
  void uncheckedRulesListWhatCannotBeVerifiedOffline() {
    ValidationResult result = evaluate(SampleDocument.invoice());
    assertThat(result.uncheckedRules())
        // Non-declarative rules that need SIFEN's data (duplicate CDC, registered RUC).
        .contains("R-A002-F2", "R-D101-F2", "R-C004-F5")
        // A field the current XSD does not have (dFeFinT, C-105).
        .contains("R-C009-O1")
        // An arithmetic rule without implementation (optional rounding to 50).
        .contains("R-F013-A1")
        // Implemented ones are not listed.
        .doesNotContain("R-A002-D1", "R-D102-F1", "R-F014-A3", "R-E727-A1");
    assertThat(result.uncheckedRules()).doesNotHaveDuplicates();
  }

  @Test
  void rulesCanBeConfiguredAsWarnings() {
    SampleDocument doc = SampleDocument.invoice().omit("D101");
    ValidationResult result =
        RuleEngine.evaluate(doc.build(), Environment.TEST, ISSUED, Set.of("R-D101-O1"));
    assertThat(issues(result, "R-D101-O1"))
        .allSatisfy(i -> assertThat(i.severity()).isEqualTo(Severity.WARNING));
  }

  @Test
  void messagesNeverContainTheValueOfAField() {
    String marker = "Zq9Unique";
    SampleDocument doc =
        SampleDocument.invoice()
            .set("D016", marker)
            .set("E701", marker.repeat(10))
            .set("D105", marker + " name")
            .set("E709", "98765");
    ValidationResult result = RuleEngine.evaluate(doc.build(), Environment.TEST, ISSUED);
    assertThat(result.issues()).isNotEmpty();
    assertThat(result.issues())
        .allSatisfy(i -> {
          assertThat(i.message()).doesNotContain(marker).doesNotContain("98765");
          assertThat(i.toString()).doesNotContain(marker).doesNotContain("98765");
        });
  }

  @Test
  void aThousandItemsAreEvaluatedQuickly() {
    Node tree = SampleDocument.invoice().items(1000).build();
    RuleEngine.evaluate(tree, Environment.TEST, ISSUED); // warm up the bundled specification
    long start = System.nanoTime();
    ValidationResult result = RuleEngine.evaluate(tree, Environment.TEST, ISSUED);
    long millis = (System.nanoTime() - start) / 1_000_000;

    assertThat(result.issues()).isEmpty();
    assertThat(millis).isLessThan(5_000);
  }
}
