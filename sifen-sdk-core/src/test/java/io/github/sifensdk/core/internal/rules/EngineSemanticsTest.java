/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.rules;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.sifensdk.core.Environment;
import io.github.sifensdk.core.internal.spec.Condition;
import io.github.sifensdk.core.internal.spec.Field;
import io.github.sifensdk.core.internal.spec.Rule;
import io.github.sifensdk.core.internal.spec.Source;
import io.github.sifensdk.core.internal.spec.Spec;
import io.github.sifensdk.core.internal.spec.Validity;
import io.github.sifensdk.core.internal.tree.Node;
import io.github.sifensdk.core.validation.ValidationIssue;
import io.github.sifensdk.core.validation.ValidationResult;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

/** Condition and selection semantics on a small synthetic specification. */
class EngineSemanticsTest {

  private static final LocalDate DATE = LocalDate.of(2025, 3, 10);
  private static final List<Source> SRC = List.of(new Source("MT150", "test"));

  private static Field field(
      String id, String xml, @Nullable String parent, int max, String state) {
    return new Field(
        id,
        xml,
        id,
        parent,
        "alfanumerico",
        null,
        0,
        max,
        id,
        null,
        state,
        SRC,
        Validity.ALWAYS,
        List.of(),
        "T");
  }

  private static Map<String, Field> fields() {
    Map<String, Field> m = new LinkedHashMap<>();
    for (Field f :
        List.of(
            field("AA001", "rDE", null, 1, "vigente"),
            field("A001", "DE", "AA001", 1, "vigente"),
            field("X1", "code", "A001", 1, "vigente"),
            field("G", "item", "A001", 99, "vigente"),
            field("G1", "v", "G", 1, "vigente"),
            field("G2", "w", "G", 1, "vigente"),
            field("T", "total", "A001", 1, "vigente"),
            field("FU", "future", "A001", 1, "futuro"))) {
      m.put(f.id(), f);
    }
    return m;
  }

  private static Rule rule(
      String id, String fieldId, String type, @Nullable Condition condition, Validity validity) {
    return new Rule(
        id, fieldId, type, "text", condition, true, null, List.of(), null, null, SRC, validity,
        List.of(), false);
  }

  private static Rule rule(String id, String fieldId, String type, @Nullable Condition condition) {
    return rule(id, fieldId, type, condition, Validity.ALWAYS);
  }

  private static Condition cmp(String fieldId, String op, String... values) {
    return new Condition.Compare(fieldId, op, List.of(values), "alguno");
  }

  private static ValidationResult evaluate(List<Rule> rules, Node tree) {
    Set<String> xml = Set.of("rDE", "DE", "code", "item", "v", "w", "total", "future");
    Engine engine = new Engine(fields(), rules, Map.of(), new XsdNames(xml, Set.of()), Map.of());
    return engine.evaluate(tree, Environment.TEST, DATE, Set.of());
  }

  /** A tree with one item per value of G1 (null leaves G1 out of that item). */
  private static Node tree(@Nullable String code, @Nullable String total, String... itemValues) {
    Node de = Node.group("DE", "A001");
    de.addIfPresent("code", "X1", code);
    for (String v : itemValues) {
      Node item = Node.group("item", "G");
      item.addIfPresent("v", "G1", v.isEmpty() ? null : v);
      item.add(Node.leaf("w", "G2", "x"));
      de.add(item);
    }
    de.addIfPresent("total", "T", total);
    return Node.group("rDE", "AA001").add(de);
  }

  private static List<String> ids(ValidationResult r) {
    List<String> out = new ArrayList<>();
    for (ValidationIssue i : r.issues()) {
      out.add(i.ruleId());
    }
    return out;
  }

  @Test
  void anyRepetitionSatisfiesAConditionUnlessTheQuantifierSaysAll() {
    Rule any = rule("R-T-C1", "T", "condicional", cmp("G1", "igual", "1"));
    Rule all =
        rule(
            "R-T-C2",
            "T",
            "condicional",
            new Condition.Compare("G1", "igual", List.of("1"), "todos"));

    assertThat(ids(evaluate(List.of(any, all), tree("a", null, "1", "0"))))
        .containsExactly("R-T-C1");
    assertThat(ids(evaluate(List.of(any, all), tree("a", null, "1", "1"))))
        .containsExactly("R-T-C1", "R-T-C2");
    assertThat(ids(evaluate(List.of(any, all), tree("a", null, "0", "0")))).isEmpty();
  }

  @Test
  void aConditionInsideTheSameRepeatedGroupIsReadFromTheSameRepetition() {
    Rule prohibited = rule("R-G2-P1", "G2", "prohibido", cmp("G1", "igual", "1"));
    ValidationResult r = evaluate(List.of(prohibited), tree("a", null, "0", "1", "0"));
    assertThat(r.issues())
        .singleElement()
        .satisfies(i -> assertThat(i.path()).isEqualTo("rDE/DE/item[2]/w"));
  }

  @Test
  void anAbsentFieldOnlySatisfiesAbsentAndNotIn() {
    Node noCode = tree(null, null);
    assertThat(
            ids(
                evaluate(
                    List.of(rule("R1", "T", "condicional", cmp("X1", "distinto", "z"))), noCode)))
        .isEmpty();
    assertThat(
            ids(evaluate(List.of(rule("R2", "T", "condicional", cmp("X1", "igual", "z"))), noCode)))
        .isEmpty();
    assertThat(
            ids(evaluate(List.of(rule("R3", "T", "condicional", cmp("X1", "no_en", "z"))), noCode)))
        .containsExactly("R3");
    assertThat(ids(evaluate(List.of(rule("R4", "T", "condicional", cmp("X1", "ausente"))), noCode)))
        .containsExactly("R4");
    assertThat(
            ids(evaluate(List.of(rule("R5", "T", "condicional", cmp("X1", "presente"))), noCode)))
        .isEmpty();
    assertThat(
            ids(
                evaluate(
                    List.of(
                        rule("R6", "T", "condicional", new Condition.Not(cmp("X1", "presente")))),
                    noCode)))
        .containsExactly("R6");
  }

  @Test
  void greaterAndLessCompareAsDecimals() {
    Rule greater = rule("R1", "T", "condicional", cmp("X1", "mayor", "9"));
    // As text "10.50" < "9"; as decimals it is greater.
    assertThat(ids(evaluate(List.of(greater), tree("10.50", null)))).containsExactly("R1");
    assertThat(ids(evaluate(List.of(greater), tree("9.0", null)))).isEmpty();
    assertThat(ids(evaluate(List.of(greater), tree("abc", null)))).isEmpty();
  }

  @Test
  void equalityComparesNumbersAsNumbers() {
    // The issuer keeps its own scale (10.00), the specification writes 10: same number.
    Rule equal = rule("R1", "T", "condicional", cmp("X1", "igual", "10"));
    Rule in = rule("R2", "T", "condicional", cmp("X1", "en", "5", "10"));
    Rule notIn = rule("R3", "T", "condicional", cmp("X1", "no_en", "5", "10"));
    assertThat(ids(evaluate(List.of(equal, in, notIn), tree("10.00", null))))
        .containsExactly("R1", "R2");
    assertThat(ids(evaluate(List.of(equal), tree("10.5", null)))).isEmpty();
  }

  @Test
  void combinatorsAllAndAny() {
    Condition both = new Condition.All(List.of(cmp("X1", "igual", "a"), cmp("G1", "en", "1", "2")));
    Condition either =
        new Condition.Any(List.of(cmp("X1", "igual", "b"), cmp("G1", "en", "1", "2")));
    List<Rule> rules =
        List.of(rule("R-ALL", "T", "condicional", both), rule("R-ANY", "T", "condicional", either));
    assertThat(ids(evaluate(rules, tree("a", null, "2")))).containsExactly("R-ALL", "R-ANY");
    assertThat(ids(evaluate(rules, tree("c", null, "2")))).containsExactly("R-ANY");
    assertThat(ids(evaluate(rules, tree("c", null, "3")))).isEmpty();
  }

  @Test
  void aSupersededRuleWithoutEndDateAppliesOnlyWithoutReplacement() {
    Validity superseded = new Validity(null, null, null, null, true);
    Rule old = rule("R-T-O0", "T", "obligatorio", null, superseded);
    Rule replacement = rule("R-T-O1", "T", "obligatorio", null);
    assertThat(ids(evaluate(List.of(old), tree("a", null)))).containsExactly("R-T-O0");
    assertThat(ids(evaluate(List.of(old, replacement), tree("a", null)))).containsExactly("R-T-O1");
  }

  @Test
  void rulesOutsideTheirValidityOrOfFieldsNotInForceAreSkipped() {
    Validity future = new Validity(DATE.plusDays(1), DATE.plusDays(1), null, null, false);
    Validity ended = new Validity(null, null, DATE, DATE, true);
    Rule notYet = rule("R-T-O0", "T", "obligatorio", null, future);
    Rule over = rule("R-T-O1", "T", "obligatorio", null, ended);
    Rule ofFutureField = rule("R-FU-O1", "FU", "obligatorio", null);
    assertThat(evaluate(List.of(notYet, over, ofFutureField), tree("a", null)).issues()).isEmpty();
  }

  @Test
  void discardedRulesAreNeverEvaluated() {
    Rule discarded =
        new Rule(
            "R-T-O9",
            "T",
            "obligatorio",
            "text",
            null,
            true,
            null,
            List.of(),
            null,
            null,
            SRC,
            Validity.ALWAYS,
            List.of("C-999"),
            true);
    ValidationResult r = evaluate(List.of(discarded), tree("a", null));
    assertThat(r.issues()).isEmpty();
    assertThat(r.uncheckedRules()).doesNotContain("R-T-O9");
  }

  @Test
  void nonDeclarativeAndUnimplementedRulesAreListedAsUnchecked() {
    Rule nonDeclarative =
        new Rule(
            "R-X1-C9",
            "X1",
            "condicional",
            "text",
            null,
            false,
            null,
            List.of(),
            null,
            null,
            SRC,
            Validity.ALWAYS,
            List.of(),
            false);
    Rule format = rule("R-X1-F1", "X1", "formato", null);
    Rule formatOnAbsent = rule("R-T-F1", "T", "formato", null);
    ValidationResult r = evaluate(List.of(nonDeclarative, format, formatOnAbsent), tree("a", null));
    assertThat(r.issues()).isEmpty();
    assertThat(r.uncheckedRules()).containsExactly("R-X1-C9", "R-X1-F1");
  }

  @Test
  void everyConditionOfTheBundledSpecificationUsesAKnownOperator() {
    Set<String> known =
        Set.of(
            "igual",
            "distinto",
            "mayor",
            "mayor_igual",
            "menor",
            "menor_igual",
            "en",
            "no_en",
            "presente",
            "ausente");
    for (Rule r : Spec.bundled().rules()) {
      if (r.condition() != null) {
        assertOperators(r.condition(), known);
      }
    }
  }

  private static void assertOperators(Condition c, Set<String> known) {
    if (c instanceof Condition.All) {
      ((Condition.All) c).conditions().forEach(x -> assertOperators(x, known));
    } else if (c instanceof Condition.Any) {
      ((Condition.Any) c).conditions().forEach(x -> assertOperators(x, known));
    } else if (c instanceof Condition.Not) {
      assertOperators(((Condition.Not) c).condition(), known);
    } else {
      assertThat(known).contains(((Condition.Compare) c).op());
    }
  }

  @Test
  void everyRegisteredCheckBelongsToARuleOfTheBundledSpecification() {
    Set<String> ruleIds = new java.util.HashSet<>();
    Spec.bundled().rules().forEach(r -> ruleIds.add(r.id()));
    assertThat(ruleIds).containsAll(ValueChecks.all().keySet());
    assertThat(ruleIds).containsAll(ArithmeticChecks.all().keySet());
    assertThat(ruleIds).containsAll(Engine.COVERED_BY_CATALOG);
    for (String id : ArithmeticChecks.all().keySet()) {
      assertThat(Spec.bundled().rules().stream().filter(r -> r.id().equals(id)).findFirst())
          .hasValueSatisfying(r -> assertThat(r.type()).isEqualTo("aritmetica"));
    }
  }

  @Test
  void checkDigitMatchesTheManualExamples() {
    assertThat(CheckDigit.modulo11("44444401")).isEqualTo(7);
    assertThat(CheckDigit.modulo11("0144444401700100100145282201701251587326098")).isEqualTo(8);
    assertThat(CheckDigit.modulo11("8000A")).isNull();
  }
}
