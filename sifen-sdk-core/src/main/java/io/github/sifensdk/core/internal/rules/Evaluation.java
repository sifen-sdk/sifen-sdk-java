/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.rules;

import io.github.sifensdk.core.Environment;
import io.github.sifensdk.core.internal.spec.Catalog;
import io.github.sifensdk.core.internal.spec.CatalogEntry;
import io.github.sifensdk.core.internal.spec.Condition;
import io.github.sifensdk.core.internal.spec.Field;
import io.github.sifensdk.core.internal.spec.Rule;
import io.github.sifensdk.core.internal.spec.Source;
import io.github.sifensdk.core.internal.spec.Validity;
import io.github.sifensdk.core.internal.tree.Node;
import io.github.sifensdk.core.validation.Severity;
import io.github.sifensdk.core.validation.ValidationIssue;
import io.github.sifensdk.core.validation.ValidationResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * One evaluation of the rules on one document. Not thread-safe; created per call.
 *
 * <p>Indexes the tree once (field id to occurrences, in document order) and evaluates each selected
 * rule. Lookups scoped to a repeated group (the same item) use a per-group index built on first
 * use, so the cost grows linearly with the number of items.
 */
final class Evaluation {

  private static final List<Occurrence> NONE = List.of();

  private final Engine engine;
  private final Node root;
  private final Environment environment;
  private final LocalDate referenceDate;
  private final Set<String> warnings;

  private final Map<String, List<Occurrence>> index;
  private final Map<String, Map<Node, List<Occurrence>>> byContainer = new HashMap<>();
  private final Map<Node, Map<String, List<Occurrence>>> subtrees = new IdentityHashMap<>();
  private final Map<String, String> scopes = new HashMap<>();

  private final List<ValidationIssue> issues = new ArrayList<>();
  private final Set<String> unchecked = new LinkedHashSet<>();

  Evaluation(
      Engine engine,
      Node root,
      Environment environment,
      LocalDate referenceDate,
      Set<String> warningRuleIds) {
    this.engine = engine;
    this.root = root;
    this.environment = environment;
    this.referenceDate = referenceDate;
    this.warnings = Set.copyOf(warningRuleIds);
    this.index = indexOf(root);
  }

  ValidationResult run() {
    for (Rule rule : engine.rules()) {
      Field field = engine.field(rule.field());
      if (field != null && isSelected(rule, field)) {
        evaluate(rule, field);
      }
    }
    checkCatalogs();
    return new ValidationResult(issues, new ArrayList<>(unchecked));
  }

  // --- selection --------------------------------------------------------------------------------

  /**
   * A rule is evaluated when its field is in force (state {@code vigente} and its validity applies)
   * and the rule applies on the reference date in the environment. A rule superseded by a later
   * source without a published end date for this environment is skipped when another rule of the
   * same field and type, not superseded, applies (the later one wins); otherwise it still applies.
   */
  private boolean isSelected(Rule rule, Field field) {
    if (!isActive(field)) {
      return false;
    }
    Validity v = rule.validity();
    if (!v.appliesOn(environment, referenceDate)) {
      return false;
    }
    LocalDate until = environment == Environment.TEST ? v.untilTest() : v.untilProduction();
    return !(v.isSuperseded()
        && until == null
        && engine.hasReplacement(rule, environment, referenceDate));
  }

  private boolean isActive(Field field) {
    return "vigente".equals(field.state())
        && field.validity().appliesOn(environment, referenceDate);
  }

  // --- rule types -------------------------------------------------------------------------------

  private void evaluate(Rule rule, Field field) {
    switch (rule.type()) {
      case "opcional":
        return;
      case "obligatorio":
      case "condicional":
        presence(rule, field);
        return;
      case "prohibido":
        prohibited(rule, field);
        return;
      case "formato":
      case "dependencia":
      case "aritmetica":
        value(rule, field);
        return;
      default:
        unchecked.add(rule.id());
    }
  }

  /**
   * The field must exist in each existing instance of its parent group (when the condition holds).
   */
  private void presence(Rule rule, Field field) {
    String parent = field.parent();
    if (parent == null) {
      return;
    }
    List<Occurrence> parents = all(parent);
    if (parents.isEmpty()) {
      return;
    }
    Condition condition = rule.condition();
    if (!rule.declarative() || engine.isNotInXsd(field.id())) {
      for (Occurrence p : parents) {
        if (condition == null || holds(condition, p.anchor(), parent)) {
          unchecked.add(rule.id());
          return;
        }
      }
      return;
    }
    Map<Node, List<Occurrence>> present = byContainer(field.id(), parent);
    for (Occurrence p : parents) {
      if (present.containsKey(p.anchor())) {
        continue;
      }
      if (condition == null) {
        report(rule, field, p.path(), "Required field is missing");
      } else if (holds(condition, p.anchor(), parent)) {
        report(rule, field, p.path(), "Required field is missing: the rule's condition holds");
      }
    }
  }

  /** The field must not exist when the condition holds (or never, without a condition). */
  private void prohibited(Rule rule, Field field) {
    List<Occurrence> occurrences = all(field.id());
    if (occurrences.isEmpty()) {
      return;
    }
    Condition condition = rule.condition();
    for (Occurrence o : occurrences) {
      if (condition == null || holds(condition, o.anchor(), field.id())) {
        if (!rule.declarative()) {
          unchecked.add(rule.id());
          return;
        }
        report(
            rule,
            field,
            o.path(),
            condition == null
                ? "Field must not be present"
                : "Field must not be present: the rule's condition holds");
      }
    }
  }

  /** Format, dependency and arithmetic rules: a Java check registered by rule id, or unchecked. */
  private void value(Rule rule, Field field) {
    List<Occurrence> occurrences = all(field.id());
    if (occurrences.isEmpty()) {
      return;
    }
    if (Engine.COVERED_BY_CATALOG.contains(rule.id()) && field.catalog() != null) {
      return;
    }
    Condition condition = rule.condition();
    Check check = engine.check(rule.id());
    for (Occurrence o : occurrences) {
      if (condition != null && !holds(condition, o.anchor(), field.id())) {
        continue;
      }
      if (check == null) {
        unchecked.add(rule.id());
        return;
      }
      String message = check.verify(this, rule, o);
      if (message != null) {
        report(rule, field, o.path(), message);
      }
    }
  }

  // --- catalogs ---------------------------------------------------------------------------------

  /** Every code field: the code exists in its catalog and is in force on the reference date. */
  private void checkCatalogs() {
    for (Field field : engine.catalogCodeFields()) {
      if (!isActive(field)) {
        continue;
      }
      Catalog catalog = engine.catalog(field.catalog());
      if (catalog == null) {
        continue;
      }
      for (Occurrence o : all(field.id())) {
        String code = o.text();
        if (code == null) {
          continue;
        }
        List<CatalogEntry> entries = engine.entries(catalog.id(), code);
        if (entries.isEmpty()) {
          reportCatalog(field, o, "Code is not in the catalog '" + catalog.id() + "'");
        } else if (entries.stream().noneMatch(this::isInForce)) {
          reportCatalog(
              field,
              o,
              "Code is not in force on the reference date in the catalog '" + catalog.id() + "'");
        }
      }
    }
  }

  boolean isInForce(CatalogEntry entry) {
    return "vigente".equals(entry.state())
        && entry.validity().appliesOn(environment, referenceDate);
  }

  // --- conditions -------------------------------------------------------------------------------

  /**
   * Evaluates a declarative condition in the context of a node (an occurrence of {@code
   * contextField}, or the parent group instance where a field is missing).
   */
  boolean holds(Condition condition, Node context, String contextField) {
    if (condition instanceof Condition.All) {
      for (Condition c : ((Condition.All) condition).conditions()) {
        if (!holds(c, context, contextField)) {
          return false;
        }
      }
      return true;
    }
    if (condition instanceof Condition.Any) {
      for (Condition c : ((Condition.Any) condition).conditions()) {
        if (holds(c, context, contextField)) {
          return true;
        }
      }
      return false;
    }
    if (condition instanceof Condition.Not) {
      return !holds(((Condition.Not) condition).condition(), context, contextField);
    }
    Condition.Compare cmp = (Condition.Compare) condition;
    List<Occurrence> occurrences = scoped(context, contextField, cmp.field());
    String op = cmp.op();
    if (occurrences.isEmpty()) {
      // An absent field satisfies no value comparison; only "ausente" and "no_en".
      return "ausente".equals(op) || "no_en".equals(op);
    }
    if ("presente".equals(op)) {
      return true;
    }
    if ("ausente".equals(op)) {
      return false;
    }
    boolean every = "todos".equals(cmp.quantifier());
    for (Occurrence o : occurrences) {
      boolean result = compare(op, o.text(), cmp.values());
      if (every && !result) {
        return false;
      }
      if (!every && result) {
        return true;
      }
    }
    return every;
  }

  private static boolean compare(String op, @Nullable String text, List<String> values) {
    switch (op) {
      case "igual":
        return text != null && !values.isEmpty() && same(text, values.get(0));
      case "distinto":
        return text != null && !values.isEmpty() && !same(text, values.get(0));
      case "en":
        return text != null && values.stream().anyMatch(v -> same(text, v));
      case "no_en":
        return text == null || values.stream().noneMatch(v -> same(text, v));
      case "mayor":
      case "mayor_igual":
      case "menor":
      case "menor_igual":
        return numericCompare(op, text, values);
      default:
        throw new IllegalStateException("Unknown condition operator in the specification: " + op);
    }
  }

  /**
   * Equality of a value in the document and a value in a condition: equal text, or the same number
   * when both are plain decimals. The issuer may write {@code 10.00} where the condition says
   * {@code 10} (amounts and rates are transported with the issuer's scale), and SIFEN compares them
   * as numbers; comparing only text would silently skip the rule.
   */
  private static boolean same(String text, String value) {
    if (text.equals(value)) {
      return true;
    }
    BigDecimal left = decimal(text);
    BigDecimal right = decimal(value);
    return left != null && right != null && left.compareTo(right) == 0;
  }

  private static boolean numericCompare(String op, @Nullable String text, List<String> values) {
    BigDecimal left = decimal(text);
    BigDecimal right = values.isEmpty() ? null : decimal(values.get(0));
    if (left == null || right == null) {
      return false;
    }
    int c = left.compareTo(right);
    switch (op) {
      case "mayor":
        return c > 0;
      case "mayor_igual":
        return c >= 0;
      case "menor":
        return c < 0;
      default:
        return c <= 0;
    }
  }

  // --- lookups ----------------------------------------------------------------------------------

  /** All occurrences of a field in document order. */
  List<Occurrence> all(String fieldId) {
    return index.getOrDefault(fieldId, NONE);
  }

  /**
   * Occurrences of a field as seen from a context: when both share a repeated group, only those in
   * the same repetition as the context; otherwise all of them.
   */
  List<Occurrence> scoped(Node context, String contextField, String targetField) {
    String key = contextField + '|' + targetField;
    String scope = scopes.get(key);
    if (scope == null) {
      String shared = engine.sharedRepeatedGroup(contextField, targetField);
      scope = shared == null ? "" : shared;
      scopes.put(key, scope);
    }
    if (!scope.isEmpty()) {
      for (Node n = context; n != null; n = n.parent()) {
        if (scope.equals(n.fieldId())) {
          return subtree(n).getOrDefault(targetField, NONE);
        }
      }
    }
    return all(targetField);
  }

  /** The first occurrence of a field as seen from another occurrence, or null. */
  @Nullable Occurrence related(Occurrence context, String fieldId) {
    List<Occurrence> found = scoped(context.anchor(), context.fieldId(), fieldId);
    return found.isEmpty() ? null : found.get(0);
  }

  /** The text of a field as seen from another occurrence, or null. */
  @Nullable String text(Occurrence context, String fieldId) {
    Occurrence o = related(context, fieldId);
    return o == null ? null : o.text();
  }

  /** Occurrences of fields inside a node (the node included), by field id. */
  Map<String, List<Occurrence>> subtree(Node node) {
    Map<String, List<Occurrence>> found = subtrees.get(node);
    if (found == null) {
      found = indexOf(node);
      subtrees.put(node, found);
    }
    return found;
  }

  /** The first text of a field inside a node, or null. */
  @Nullable String textIn(Node node, String fieldId) {
    List<Occurrence> found = subtree(node).get(fieldId);
    return found == null || found.isEmpty() ? null : found.get(0).text();
  }

  Environment environment() {
    return environment;
  }

  LocalDate referenceDate() {
    return referenceDate;
  }

  Engine engine() {
    return engine;
  }

  Node root() {
    return root;
  }

  private Map<Node, List<Occurrence>> byContainer(String fieldId, String parentId) {
    Map<Node, List<Occurrence>> grouped = byContainer.get(fieldId);
    if (grouped == null) {
      grouped = new IdentityHashMap<>();
      for (Occurrence o : all(fieldId)) {
        for (Node n = o.container(); n != null; n = n.parent()) {
          if (parentId.equals(n.fieldId())) {
            grouped.computeIfAbsent(n, k -> new ArrayList<>(1)).add(o);
            break;
          }
        }
      }
      byContainer.put(fieldId, grouped);
    }
    return grouped;
  }

  private Map<String, List<Occurrence>> indexOf(Node top) {
    Map<String, List<Occurrence>> out = new HashMap<>();
    collect(top, out);
    return out;
  }

  private void collect(Node node, Map<String, List<Occurrence>> out) {
    String id = node.fieldId();
    if (id != null) {
      out.computeIfAbsent(id, k -> new ArrayList<>()).add(new Occurrence(id, node, null));
    }
    for (String attribute : node.attributes().keySet()) {
      String attributeId = engine.attributeField(id, attribute);
      if (attributeId != null) {
        out.computeIfAbsent(attributeId, k -> new ArrayList<>(1))
            .add(new Occurrence(attributeId, node, attribute));
      }
    }
    for (Node child : node.children()) {
      collect(child, out);
    }
  }

  // --- issues -----------------------------------------------------------------------------------

  private Severity severityOf(String ruleId) {
    return warnings.contains(ruleId) || Engine.DEFAULT_WARNINGS.contains(ruleId)
        ? Severity.WARNING
        : Severity.ERROR;
  }

  private void report(Rule rule, Field field, String path, String message) {
    issues.add(
        new ValidationIssue(
            severityOf(rule.id()),
            rule.id(),
            field.id(),
            field.xml(),
            path,
            message,
            rule.sifenCode(),
            sources(rule.sources()),
            rule.contradictions()));
  }

  private void reportCatalog(Field field, Occurrence o, String message) {
    issues.add(
        new ValidationIssue(
            severityOf(Engine.CATALOG_RULE),
            Engine.CATALOG_RULE,
            field.id(),
            field.xml(),
            o.path(),
            message,
            null,
            sources(field.sources()),
            field.contradictions()));
  }

  private static List<String> sources(List<Source> sources) {
    List<String> out = new ArrayList<>(sources.size());
    for (Source s : sources) {
      out.add(s.toString());
    }
    return out;
  }

  static @Nullable BigDecimal decimal(@Nullable String text) {
    if (text == null || text.isEmpty()) {
      return null;
    }
    try {
      return new BigDecimal(text);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
