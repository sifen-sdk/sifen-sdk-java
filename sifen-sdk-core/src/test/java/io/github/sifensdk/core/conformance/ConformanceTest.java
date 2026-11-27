/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.conformance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.github.sifensdk.core.Environment;
import io.github.sifensdk.core.internal.json.Json;
import io.github.sifensdk.core.internal.rules.RuleEngine;
import io.github.sifensdk.core.internal.spec.Rule;
import io.github.sifensdk.core.internal.spec.Spec;
import io.github.sifensdk.core.internal.spec.Validity;
import io.github.sifensdk.core.internal.tree.Node;
import io.github.sifensdk.core.internal.xml.SchemaValidator;
import io.github.sifensdk.core.validation.Severity;
import io.github.sifensdk.core.validation.ValidationIssue;
import io.github.sifensdk.core.validation.ValidationResult;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * Runs the conformance battery of the specification ({@code spec/conformidad}, see its README) on
 * the core: the {@code validacion}, {@code aritmetica} and {@code vigencia} cases, one dynamic test
 * per case.
 *
 * <p>Each case is an unsigned XML next to its JSON. The XML is read into the neutral tree ({@link
 * XmlTreeReader}), validated against the official XSD ({@link SchemaValidator}) and evaluated by
 * the rule engine ({@link RuleEngine}) in the case's environment and on its date. The test checks
 * that the XSD result is the expected one, that every expected failure ({@code fallan}: rule and
 * field) is reported, that no rule expected to hold ({@code pasan}) is reported, and, when the case
 * says {@code "otras": "prohibidas"}, that the engine reports no other error. A failure the SDK
 * cannot check ({@link ValidationResult#uncheckedRules()}) is still a failure of the case.
 *
 * <p>The cases are read from the directory in the system property {@code sifen.conformance.dir}, by
 * default the {@code conformidad} folder of the specification submodule ({@code
 * ../spec/conformidad} from this module). When it has no case of these types (a specification older
 * than the battery), the tests are skipped.
 */
class ConformanceTest {

  /** System property with the conformance directory of the specification. */
  static final String DIRECTORY_PROPERTY = "sifen.conformance.dir";

  private static final List<String> DOCUMENT_TYPES =
      List.of("validacion", "aritmetica", "vigencia");

  static Path directory() {
    return Path.of(System.getProperty(DIRECTORY_PROPERTY, "../spec/conformidad"));
  }

  static List<Path> cases(Path directory, List<String> types) {
    List<Path> out = new ArrayList<>();
    for (String type : types) {
      Path folder = directory.resolve(type);
      if (!Files.isDirectory(folder)) {
        continue;
      }
      try (Stream<Path> files = Files.list(folder)) {
        files.filter(p -> p.toString().endsWith(".json")).sorted().forEach(out::add);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    return out;
  }

  @TestFactory
  Stream<DynamicTest> documentCases() {
    List<Path> cases = cases(directory(), DOCUMENT_TYPES);
    assumeTrue(
        !cases.isEmpty(),
        "No validation, arithmetic or validity cases in " + directory().toAbsolutePath());
    return cases.stream()
        .map(p ->
            DynamicTest.dynamicTest(
                p.getParent().getFileName() + "/" + p.getFileName(), () -> run(p)));
  }

  @Test
  void everyArithmeticRuleInForceHasAFailingCase() {
    Path directory = directory();
    List<Path> cases = cases(directory, DOCUMENT_TYPES);
    assumeTrue(!cases.isEmpty(), "No conformance cases in " + directory.toAbsolutePath());

    Set<String> withoutCase = new TreeSet<>();
    for (Object entry :
        list(object(read(directory.resolve("cobertura.json"))), "aritmetica_sin_caso")) {
      withoutCase.add(string(object(entry), "regla"));
    }
    Set<String> failing = new TreeSet<>();
    for (Path p : cases) {
      for (Object evaluation : list(object(read(p)), "evaluaciones")) {
        for (Object f : list(object(object(evaluation).get("esperado")), "fallan")) {
          failing.add(string(object(f), "regla"));
        }
      }
    }
    LocalDate today = LocalDate.now(java.time.ZoneId.of("America/Asuncion"));
    Set<String> missing = new TreeSet<>();
    for (Rule r : Spec.bundled().rules()) {
      if ("aritmetica".equals(r.type())
          && !r.discarded()
          && !withoutCase.contains(r.id())
          && inForce(r.validity(), today)
          && !failing.contains(r.id())) {
        missing.add(r.id());
      }
    }
    assertThat(missing).as("arithmetic rules in force without a failing case").isEmpty();
  }

  /**
   * Whether a rule is in force on a date in some environment: it applies on the date, and it has
   * not been ended by a later source without a published date (the later rule replaces it).
   */
  static boolean inForce(Validity v, LocalDate date) {
    boolean test = v.appliesOn(Environment.TEST, date) && !(v.hasUntil() && v.untilTest() == null);
    boolean production =
        v.appliesOn(Environment.PRODUCTION, date) && !(v.hasUntil() && v.untilProduction() == null);
    return test || production;
  }

  // --- one case ---------------------------------------------------------------------------------

  private static void run(Path casePath) throws IOException {
    Map<String, Object> c = object(read(casePath));
    List<String> problems = new ArrayList<>();
    List<Object> evaluations = list(c, "evaluaciones");
    for (int i = 0; i < evaluations.size(); i++) {
      Map<String, Object> evaluation = object(evaluations.get(i));
      String xmlName = evaluation.containsKey("xml") ? string(evaluation, "xml") : string(c, "xml");
      byte[] xml = Files.readAllBytes(casePath.resolveSibling(xmlName));
      Environment environment =
          "produccion".equals(string(evaluation, "ambiente"))
              ? Environment.PRODUCTION
              : Environment.TEST;
      LocalDate date = LocalDate.parse(string(evaluation, "fecha"));
      String where =
          "evaluaciones[" + i + "] (" + string(evaluation, "ambiente") + ", " + date + "): ";
      problems.addAll(
          prefixed(where, check(xml, environment, date, object(evaluation.get("esperado")))));
    }
    if (!problems.isEmpty()) {
      fail(string(c, "descripcion") + "\n  " + String.join("\n  ", problems));
    }
  }

  private static List<String> check(
      byte[] xml, Environment environment, LocalDate date, Map<String, Object> expected) {
    List<String> problems = new ArrayList<>();
    Node root = XmlTreeReader.read(xml);
    boolean event = "rGesEve".equals(root.name());

    List<ValidationIssue> schema =
        event ? SchemaValidator.validateEvent(xml) : SchemaValidator.validateDocument(xml);
    boolean schemaValid = schema.isEmpty();
    if (schemaValid != Boolean.TRUE.equals(expected.get("xsd"))) {
      problems.add("XSD: expected " + (schemaValid ? "invalid" : "valid") + ", got " + schema);
    }

    ValidationResult result =
        event
            ? RuleEngine.evaluateEvent(root, environment, date, Set.of())
            : RuleEngine.evaluate(root, environment, date, Set.of());

    Set<String> expectedFailing = new LinkedHashSet<>();
    for (Object f : list(expected, "fallan")) {
      String rule = string(object(f), "regla");
      String field = string(object(f), "campo");
      expectedFailing.add(rule);
      boolean reported =
          result.issues().stream()
              .anyMatch(x -> rule.equals(x.ruleId()) && field.equals(x.fieldId()));
      if (!reported) {
        problems.add(
            "expected failure not reported: "
                + rule
                + " on "
                + field
                + (result.uncheckedRules().contains(rule) ? " (the SDK lists it as unchecked)" : "")
                + reportedOn(result, rule));
      }
    }
    for (Object rule : list(expected, "pasan")) {
      List<ValidationIssue> reported = issuesOf(result, (String) rule);
      if (!reported.isEmpty()) {
        problems.add("rule expected to hold was reported: " + reported);
      }
    }
    if ("prohibidas".equals(expected.get("otras"))) {
      List<ValidationIssue> others =
          result.issues().stream()
              .filter(x -> x.severity() == Severity.ERROR)
              .filter(x -> !expectedFailing.contains(x.ruleId()))
              .collect(Collectors.toList());
      if (!others.isEmpty()) {
        problems.add("unexpected errors: " + others);
      }
    }
    return problems;
  }

  private static String reportedOn(ValidationResult result, String rule) {
    List<ValidationIssue> issues = issuesOf(result, rule);
    return issues.isEmpty() ? "" : " (reported as " + issues + ")";
  }

  private static List<ValidationIssue> issuesOf(ValidationResult result, String rule) {
    return result.issues().stream()
        .filter(x -> rule.equals(x.ruleId()))
        .collect(Collectors.toList());
  }

  private static List<String> prefixed(String prefix, List<String> lines) {
    return lines.stream().map(l -> prefix + l).collect(Collectors.toList());
  }

  // --- JSON -------------------------------------------------------------------------------------

  static @Nullable Object read(Path path) {
    try {
      return Json.parse(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @SuppressWarnings("unchecked")
  static Map<String, Object> object(@Nullable Object o) {
    if (!(o instanceof Map)) {
      throw new IllegalArgumentException("Expected a JSON object in a conformance case");
    }
    return (Map<String, Object>) o;
  }

  @SuppressWarnings("unchecked")
  static List<Object> list(Map<String, Object> o, String key) {
    Object v = o.get(key);
    if (!(v instanceof List)) {
      throw new IllegalArgumentException("Expected a JSON array in '" + key + "'");
    }
    return (List<Object>) v;
  }

  static String string(Map<String, Object> o, String key) {
    Object v = o.get(key);
    if (!(v instanceof String)) {
      throw new IllegalArgumentException("Expected a JSON string in '" + key + "'");
    }
    return (String) v;
  }
}
