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
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Every implemented arithmetic rule with a document that satisfies it and one that violates it.
 * Only the issue of the rule under test is asserted.
 */
class ArithmeticChecksTest {

  private static final LocalDate NT13 = LocalDate.of(2025, 3, 10);
  private static final LocalDate BEFORE_NT13 = LocalDate.of(2023, 1, 10);

  /** An exempt item (E731 = 3). */
  private static SampleDocument exempt(SampleDocument d) {
    return d.set("E731", "3")
        .set("E732", "Exento")
        .set("E733", "0")
        .set("E734", "0")
        .set("E735", "0")
        .set("E736", "0")
        .set("E737", "0");
  }

  /** An item taxed at 5% for 105000 (base 100000, VAT 5000). */
  private static SampleDocument fivePercent(SampleDocument d) {
    return d.set("E721", "105000")
        .set("E727", "105000")
        .set("EA008", "105000")
        .set("E734", "5")
        .set("E735", "100000")
        .set("E736", "5000");
  }

  /** A partially taxed item (E731 = 4, 50%, 10%) for 110000, NT 13 bases. */
  private static SampleDocument partial(SampleDocument d) {
    return d.set("E731", "4")
        .set("E732", SampleDocument.description("afectacion-iva", "4"))
        .set("E733", "50")
        .set("E735", "52381")
        .set("E736", "5238")
        .set("E737", "52381");
  }

  /** Foreign currency (USD) with a global exchange rate (D017 = 1). */
  private static SampleDocument usdGlobal(SampleDocument d) {
    return d.set("D015", "USD")
        .set("D016", SampleDocument.description("moneda", "USD"))
        .set("D017", "1")
        .set("D018", "7000")
        .set("E721", "110")
        .set("E727", "110")
        .set("EA008", "110")
        .set("E735", "100")
        .set("E736", "10")
        .set("F014", "110")
        .set("F023", "770000");
  }

  /** Foreign currency (USD) with an exchange rate per item (D017 = 2). */
  private static SampleDocument usdPerItem(SampleDocument d) {
    return usdGlobal(d).set("D017", "2").omit("D018").set("E725", "7000").set("EA009", "770000");
  }

  private static SampleDocument beforeNt13(SampleDocument d) {
    return d.set("D002", "2023-01-10T09:00:00").set("C008", "2023-01-02");
  }

  static Stream<Arguments> cases() {
    return Stream.of(
        // ruleId, date, setup that satisfies the rule, setup that violates it
        c("R-E727-A1", NT13, d -> d, d -> d.set("E727", "110001")),
        c(
            "R-EA003-A1",
            NT13,
            d -> d.set("EA002", "11000").set("EA003", "10.5"), // 10 + 0.8 tolerance
            d -> d.set("EA002", "11000").set("EA003", "11.5")),
        c(
            "R-EA004-A2",
            NT13,
            d -> d.set("F010", "10").set("EA004", "11000"),
            d -> d.set("F010", "10").set("EA004", "11001")), // beyond 0.8, without adding 0.5
        c(
            "R-EA008-A1",
            NT13,
            d -> d.set("EA002", "1000").set("E711", "2").set("EA008", "218000"),
            d -> d.set("EA002", "1000").set("E711", "2").set("EA008", "220000")),
        c(
            "R-EA008-A2",
            NT13,
            d -> d.set("C002", "4").set("EA002", "1000"),
            d -> d.set("C002", "4").set("EA002", "1000").set("EA008", "109000")),
        c("R-EA009-A1", NT13, d -> usdPerItem(d), d -> usdPerItem(d).set("EA009", "770001")),
        c("R-E735-A1", NT13, d -> exempt(d), d -> exempt(d).set("E735", "1")),
        c(
            "R-E735-A2",
            BEFORE_NT13,
            d -> fivePercent(beforeNt13(d)),
            d -> fivePercent(beforeNt13(d)).set("E735", "100001")),
        c("R-E735-A3", BEFORE_NT13, d -> beforeNt13(d), d -> beforeNt13(d).set("E735", "99999")),
        c("R-E735-A4", NT13, d -> fivePercent(d), d -> fivePercent(d).set("E735", "100001")),
        // Exact 100000: 100000.5 is accepted (difference 0.5), 100000.6 is not.
        c("R-E735-A5", NT13, d -> d.set("E735", "100000.5"), d -> d.set("E735", "100000.6")),
        c("R-E736-A1", NT13, d -> exempt(d), d -> exempt(d).set("E736", "1")),
        c("R-E736-A2", NT13, d -> d, d -> d.set("E736", "10001")),
        // Exact 52380.952...: 52381.4 differs by 0.448, 52381.5 by 0.548.
        c(
            "R-E737-A1",
            NT13,
            d -> partial(d).set("E737", "52381.4"),
            d -> partial(d).set("E737", "52381.5")),
        c("R-E737-A2", NT13, d -> d, d -> d.set("E737", "1")),
        c(
            "R-F002-A1",
            BEFORE_NT13,
            d -> exempt(beforeNt13(d)).set("F002", "110000"),
            d -> exempt(beforeNt13(d)).set("F002", "100000")),
        c(
            "R-F002-A2",
            NT13,
            d -> partial(d).set("F002", "52381"),
            d -> partial(d).set("F002", "110000")),
        c(
            "R-F003-A1",
            NT13,
            d ->
                exempt(d)
                    .set("E731", "2")
                    .set("E732", SampleDocument.description("afectacion-iva", "2"))
                    .add("F001", "F003", "dSubExo", "110000"),
            d ->
                exempt(d)
                    .set("E731", "2")
                    .set("E732", SampleDocument.description("afectacion-iva", "2"))
                    .add("F001", "F003", "dSubExo", "1")),
        c(
            "R-F004-A1",
            BEFORE_NT13,
            d -> fivePercent(beforeNt13(d)).set("F004", "105000"),
            d -> fivePercent(beforeNt13(d)).set("F004", "100000")),
        c(
            "R-F004-A2",
            NT13,
            d -> fivePercent(d).set("F004", "105000"),
            d -> fivePercent(d).set("F004", "100000")),
        c("R-F005-A1", BEFORE_NT13, d -> beforeNt13(d), d -> beforeNt13(d).set("F005", "100000")),
        c(
            "R-F005-A2",
            NT13,
            d -> partial(d).set("F005", "57619"),
            d -> partial(d).set("F005", "110000")),
        c("R-F008-A1", NT13, d -> d, d -> d.set("F008", "100000")),
        c(
            "R-F008-A3",
            NT13,
            d -> d.set("C002", "4"),
            d -> d.set("C002", "4").set("F008", "100000")),
        c(
            "R-F009-A2",
            NT13,
            d -> d.set("EA002", "1000").set("E711", "2").set("F009", "2000"),
            d -> d.set("EA002", "1000").set("E711", "2").set("F009", "1000")),
        c(
            "R-F033-A2",
            NT13,
            d -> d.set("EA004", "1000").set("E711", "2").set("F033", "2000"),
            d -> d.set("EA004", "1000").set("E711", "2").set("F033", "1000")),
        c(
            "R-F034-A2",
            NT13,
            d -> d.set("EA006", "1000").set("E711", "2").set("F034", "2000"),
            d -> d.set("EA006", "1000").set("E711", "2").set("F034", "1000")),
        c(
            "R-F035-A2",
            NT13,
            d -> d.set("EA007", "1000").set("E711", "2").set("F035", "2000"),
            d -> d.set("EA007", "1000").set("E711", "2").set("F035", "1000")),
        c(
            "R-F011-A2",
            NT13,
            d -> d.set("F009", "100").set("F033", "50").set("F011", "150"),
            d -> d.set("F009", "100").set("F033", "50").set("F011", "100")),
        c(
            "R-F012-A2",
            NT13,
            d -> d.set("F034", "100").set("F035", "50").set("F012", "150"),
            d -> d.set("F034", "100").set("F035", "50").set("F012", "100")),
        c(
            "R-F014-A1",
            NT13,
            d -> d.set("C002", "4").set("F013", "50").set("F014", "109950"),
            d -> d.set("C002", "4").set("F014", "100000")),
        c(
            "R-F014-A3",
            NT13,
            d -> d.set("F013", "50").add("F001", "F025", "dComi", "100").set("F014", "110050"),
            d -> d.set("F014", "100000")),
        c(
            "R-F015-A1",
            NT13,
            d -> fivePercent(d).set("F015", "5000"),
            d -> fivePercent(d).set("F015", "5001")),
        c("R-F016-A1", NT13, d -> d, d -> d.set("F016", "10001")),
        c("R-F017-A1", NT13, d -> d, d -> d.set("F017", "10001")),
        c(
            "R-F018-A1",
            NT13,
            d -> fivePercent(d).set("F018", "100000"),
            d -> fivePercent(d).set("F018", "1")),
        c("R-F019-A1", NT13, d -> d, d -> d.set("F019", "100001")),
        c("R-F020-A1", NT13, d -> d, d -> d.set("F020", "100001")),
        c("R-F023-A1", NT13, d -> usdGlobal(d), d -> usdGlobal(d).set("F023", "770001")),
        c("R-F023-A2", NT13, d -> usdPerItem(d), d -> usdPerItem(d).set("F023", "770001")));
  }

  private static Arguments c(
      String ruleId,
      LocalDate date,
      UnaryOperator<SampleDocument> passing,
      UnaryOperator<SampleDocument> failing) {
    return Arguments.of(ruleId, date, passing, failing);
  }

  private static ValidationResult run(UnaryOperator<SampleDocument> setup, LocalDate date) {
    Node tree = setup.apply(SampleDocument.invoice()).build();
    return RuleEngine.evaluate(tree, Environment.PRODUCTION, date);
  }

  private static List<ValidationIssue> issues(ValidationResult r, String ruleId) {
    return r.issues().stream().filter(i -> i.ruleId().equals(ruleId)).toList();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("cases")
  void passesAndFails(
      String ruleId,
      LocalDate date,
      UnaryOperator<SampleDocument> passing,
      UnaryOperator<SampleDocument> failing) {
    ValidationResult ok = run(passing, date);
    assertThat(issues(ok, ruleId)).as("passing case").isEmpty();
    assertThat(ok.uncheckedRules()).doesNotContain(ruleId);

    ValidationResult bad = run(failing, date);
    assertThat(issues(bad, ruleId))
        .as("failing case")
        .singleElement()
        .satisfies(i -> {
          assertThat(i.message()).startsWith("Does not match ");
          assertThat(i.sources()).isNotEmpty();
        });
  }

  @Test
  void theDifferenceWithTheExactResultIsAtMostHalfAUnit() {
    // 100000 taxed at 10%: exact base 90909.0909... SIFEN accepts a difference of up to 0.5 in
    // the unit of the field, whatever the decimals (specification evidence E-001).
    Consumer<String> accepted =
        base -> assertThat(issues(run(d -> amount(d, base), NT13), "R-E735-A5")).isEmpty();
    Consumer<String> rejected =
        base -> assertThat(issues(run(d -> amount(d, base), NT13), "R-E735-A5")).hasSize(1);
    accepted.accept("90909");
    accepted.accept("90909.09");
    accepted.accept("90909.5");
    accepted.accept("90909.59");
    accepted.accept("90908.60");
    rejected.accept("90909.60");
    rejected.accept("90908.59");
    rejected.accept("90910");
  }

  @Test
  void aDocumentedToleranceReplacesTheHalfUnit() {
    // EA003 = EA002 * 100 / E721 with a tolerance of 0.8: 11000 over 110000 is exactly 10.
    Consumer<String> accepted =
        rate ->
            assertThat(
                    issues(
                        run(d -> d.set("EA002", "11000").set("EA003", rate), NT13), "R-EA003-A1"))
                .isEmpty();
    Consumer<String> rejected =
        rate ->
            assertThat(
                    issues(
                        run(d -> d.set("EA002", "11000").set("EA003", rate), NT13), "R-EA003-A1"))
                .hasSize(1);
    accepted.accept("10.80");
    accepted.accept("9.20");
    rejected.accept("10.81");
    rejected.accept("9.19");
  }

  private static SampleDocument amount(SampleDocument d, String base) {
    return d.set("E721", "100000").set("E727", "100000").set("EA008", "100000").set("E735", base);
  }

  @Test
  void theTotalVatFollowsTheFieldTable() {
    // C-502, evidence E-001: SIFEN verifies F017 = F015 + F016 - F036 - F037 + F026 (R-F017-A1)
    // and rejects with 2371; the formula of validation 2371 (R-F017-A2) is discarded.
    UnaryOperator<SampleDocument> commission =
        d -> d.add("F001", "F025", "dComi", "1100").add("F001", "F026", "dIVAComi", "100");
    ValidationResult withoutCommissionVat = run(commission, NT13);
    assertThat(issues(withoutCommissionVat, "R-F017-A1"))
        .singleElement()
        .satisfies(i -> assertThat(i.severity()).isEqualTo(Severity.ERROR));
    ValidationResult withCommissionVat = run(d -> commission.apply(d).set("F017", "10100"), NT13);
    assertThat(issues(withCommissionVat, "R-F017-A1")).isEmpty();
    assertThat(withoutCommissionVat.issues()).noneMatch(i -> i.ruleId().equals("R-F017-A2"));
    assertThat(withCommissionVat.issues()).noneMatch(i -> i.ruleId().equals("R-F017-A2"));
    assertThat(withCommissionVat.uncheckedRules()).doesNotContain("R-F017-A2");
  }

  @Test
  void thePerItemExchangeRateIsNotRequiredWithAGlobalOne() {
    // C-303, evidence E-001: R-E725-C2 is discarded.
    ValidationResult result = run(d -> usdGlobal(d), NT13);
    assertThat(result.issues()).noneMatch(i -> i.ruleId().startsWith("R-E725"));
  }

  @Test
  void theEnergyConsumptionIsTheDifferenceOfTheReadings() {
    for (String consumption : List.of("50", "51")) {
      Node tree = SampleDocument.invoice().build();
      Node energy = Node.group("gGrupEner", "E791");
      energy.add(Node.leaf("dLecAnt", "E795", "100.00"));
      energy.add(Node.leaf("dLecAct", "E796", "150.00"));
      energy.add(Node.leaf("dConKwh", "E797", consumption));
      tree.findAll("E001").get(0).add(Node.group("gCamEsp", "E790").add(energy));
      ValidationResult result = RuleEngine.evaluate(tree, Environment.PRODUCTION, NT13);
      assertThat(issues(result, "R-E797-A1")).hasSize(consumption.equals("50") ? 0 : 1);
    }
  }

  @Test
  void theCreditNoteIsCheckedWithTheSameFormulas() {
    Node tree = SampleDocument.creditNote().set("F014", "100000").build();
    ValidationResult result = RuleEngine.evaluate(tree, Environment.PRODUCTION, NT13);
    assertThat(issues(result, "R-F014-A3")).hasSize(1);
    assertThat(issues(result, "R-F014-A1")).isEmpty();
  }

  @Test
  void rulesWithoutImplementationAreUncheckedWhenTheyApply() {
    ValidationResult result = run(d -> d.add("F001", "F037", "dLiqTotIVA10", "0"), NT13);
    assertThat(result.uncheckedRules()).contains("R-F013-A1", "R-F037-A1");
  }
}
