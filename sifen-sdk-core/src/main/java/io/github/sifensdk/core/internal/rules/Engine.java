/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.rules;

import io.github.sifensdk.core.Environment;
import io.github.sifensdk.core.internal.spec.Catalog;
import io.github.sifensdk.core.internal.spec.CatalogEntry;
import io.github.sifensdk.core.internal.spec.Field;
import io.github.sifensdk.core.internal.spec.Rule;
import io.github.sifensdk.core.internal.spec.Spec;
import io.github.sifensdk.core.internal.tree.Node;
import io.github.sifensdk.core.validation.ValidationResult;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * The specification prepared for evaluation: rules of the electronic document, the fields they
 * apply to, catalogs indexed by code and the Java checks registered by rule id.
 *
 * <p>Immutable and thread-safe once built. Everything that depends on the environment, the date or
 * the document is computed per evaluation in {@link Evaluation}.
 */
final class Engine {

  /** Root of the electronic document tree ({@code rDE}). */
  static final String ROOT = "AA001";

  /**
   * Groups added by the signer ({@code ds:Signature}, I001, and {@code gCamFuFD}, J001): they are
   * not in the tree the rule engine evaluates (contract of the mapper), so their rules do not apply
   * at this stage; the signed document is validated against the XSD instead.
   */
  static final Set<String> SIGNER_GROUPS = Set.of("I001", "J001");

  /**
   * Rules reported as warnings unless configured otherwise, each by a decision of the
   * specification. None at present: rules that SIFEN does not apply are recorded as {@code
   * descartada} in the specification and never evaluated (for example {@code R-F017-A2}, C-502).
   */
  static final Set<String> DEFAULT_WARNINGS = Set.of();

  /**
   * {@code formato} rules whose only requirement is that the code belongs to the field's catalog.
   * The catalog verification (code exists and is in force on the reference date) implements them;
   * the issue is reported with rule id {@link #CATALOG_RULE}.
   */
  static final Set<String> COVERED_BY_CATALOG =
      Set.of(
          "R-D031-F1", // NT018 1220: code of Tabla 12
          "R-E709-F1", // MT150 E709: Tabla 5
          "R-E709-F2", // NT023 Tabla 5 extension (entries 111-140 carry their validity)
          "R-E712-F1", // MT150 E712: country codes
          "R-E715-F1", // MT150 E715: admitted values
          "R-E731-F1", // MT150 E731 / NT010 Tabla 6: admitted values
          "R-E771-F1", // MT150 E771: admitted values
          "R-E779-F1", // MT150 E779: admitted values
          "R-G051-F1", // MT150 G051: Tabla 5
          "R-G054-F1"); // MT150 G054: Tabla 5

  /** Rule id of catalog issues (a code that is not in its catalog or not in force). */
  static final String CATALOG_RULE = "CATALOG";

  /** A code field and its catalog, for a description field. */
  record CodeRef(String codeField, String catalogId) {}

  private final Map<String, Field> fields;
  private final List<Rule> rules;
  private final Map<String, Catalog> catalogs;
  private final Map<String, Map<String, List<CatalogEntry>>> entriesByCode;
  private final Map<String, List<String>> chains;
  private final Set<String> notInXsd;
  private final Map<String, String> attributeFields;
  private final Map<String, CodeRef> descriptionOf;
  private final List<Field> catalogCodeFields;
  private final Map<String, List<Rule>> byFieldAndType;
  private final Map<String, Check> checks;

  Engine(
      Map<String, Field> fields,
      List<Rule> allRules,
      Map<String, Catalog> catalogs,
      XsdNames xsd,
      Map<String, Check> registeredChecks) {
    this.fields = Map.copyOf(fields);
    this.catalogs = Map.copyOf(catalogs);

    Map<String, List<String>> chainMap = new HashMap<>();
    for (Field f : fields.values()) {
      List<String> chain = chainOf(fields, f.id());
      if (!chain.isEmpty()
          && chain.get(chain.size() - 1).equals(ROOT)
          && Collections.disjoint(chain, SIGNER_GROUPS)) {
        chainMap.put(f.id(), List.copyOf(chain));
      }
    }
    this.chains = Map.copyOf(chainMap);

    Set<String> missing = new HashSet<>();
    Map<String, String> attrs = new HashMap<>();
    for (String id : chains.keySet()) {
      Field f = fields.get(id);
      if (f == null || f.parent() == null) {
        continue;
      }
      if (xsd.elements().contains(f.xml())) {
        continue;
      }
      if (xsd.attributes().contains(f.xml())) {
        attrs.put(f.parent() + "@" + f.xml(), id);
      } else {
        missing.add(id);
      }
    }
    this.notInXsd = Set.copyOf(missing);
    this.attributeFields = Map.copyOf(attrs);

    Map<String, Map<String, List<CatalogEntry>>> byCode = new HashMap<>();
    Map<String, CodeRef> descriptions = new HashMap<>();
    for (Catalog c : catalogs.values()) {
      Map<String, List<CatalogEntry>> codes = new HashMap<>();
      for (CatalogEntry e : c.entries()) {
        codes.computeIfAbsent(e.code(), k -> new ArrayList<>(1)).add(e);
      }
      codes.replaceAll((k, v) -> List.copyOf(v));
      byCode.put(c.id(), Map.copyOf(codes));
      for (Map.Entry<String, String> d : c.descriptionFields().entrySet()) {
        if (chains.containsKey(d.getValue())) {
          descriptions.put(d.getValue(), new CodeRef(d.getKey(), c.id()));
        }
      }
    }
    this.entriesByCode = Map.copyOf(byCode);
    this.descriptionOf = Map.copyOf(descriptions);

    List<Field> codeFields = new ArrayList<>();
    for (Field f : fields.values()) {
      if (f.catalog() != null
          && chains.containsKey(f.id())
          && !descriptionOf.containsKey(f.id())
          && catalogs.containsKey(f.catalog())) {
        codeFields.add(f);
      }
    }
    this.catalogCodeFields = List.copyOf(codeFields);

    List<Rule> scoped = new ArrayList<>();
    Map<String, List<Rule>> grouped = new LinkedHashMap<>();
    for (Rule r : allRules) {
      if (chains.containsKey(r.field()) && !r.discarded()) {
        scoped.add(r);
        grouped.computeIfAbsent(r.field() + "|" + r.type(), k -> new ArrayList<>()).add(r);
      }
    }
    grouped.replaceAll((k, v) -> List.copyOf(v));
    this.rules = List.copyOf(scoped);
    this.byFieldAndType = Map.copyOf(grouped);

    Map<String, Check> all = new HashMap<>(registeredChecks);
    for (Rule r : rules) {
      CodeRef ref = descriptionOf.get(r.field());
      if (ref != null
          && "dependencia".equals(r.type())
          && r.declarative()
          && r.text().toLowerCase(Locale.ROOT).contains("descripci")
          && !all.containsKey(r.id())) {
        all.put(r.id(), ValueChecks.description(ref.codeField(), ref.catalogId()));
      }
    }
    this.checks = Map.copyOf(all);
  }

  /** The engine for the specification bundled in the core. */
  static Engine bundled() {
    return Holder.INSTANCE;
  }

  private static final class Holder {
    static final Engine INSTANCE = fromSpec(Spec.bundled(), XsdNames.bundled());
  }

  static Engine fromSpec(Spec spec, XsdNames xsd) {
    Map<String, Check> registered = new HashMap<>(ValueChecks.all());
    registered.putAll(ArithmeticChecks.all());
    return new Engine(spec.fields(), spec.rules(), spec.catalogs(), xsd, registered);
  }

  ValidationResult evaluate(
      Node rde, Environment environment, LocalDate referenceDate, Set<String> warningRuleIds) {
    return new Evaluation(this, rde, environment, referenceDate, warningRuleIds).run();
  }

  // --- accessors used by the evaluation and the checks ---------------------------------------

  List<Rule> rules() {
    return rules;
  }

  @Nullable Field field(String id) {
    return fields.get(id);
  }

  @Nullable Catalog catalog(@Nullable String id) {
    return id == null ? null : catalogs.get(id);
  }

  /** Entries of a catalog with a given code (several only in malformed data). */
  List<CatalogEntry> entries(String catalogId, String code) {
    Map<String, List<CatalogEntry>> codes = entriesByCode.get(catalogId);
    if (codes == null) {
      return List.of();
    }
    return codes.getOrDefault(code, List.of());
  }

  List<Field> catalogCodeFields() {
    return catalogCodeFields;
  }

  @Nullable Check check(String ruleId) {
    return checks.get(ruleId);
  }

  boolean hasCheck(String ruleId) {
    return checks.containsKey(ruleId);
  }

  /** Whether a field of the document has no element or attribute in the current XSD. */
  boolean isNotInXsd(String fieldId) {
    return notInXsd.contains(fieldId);
  }

  /** The field carried as an attribute of an element, or null. */
  @Nullable String attributeField(@Nullable String elementFieldId, String attribute) {
    return elementFieldId == null ? null : attributeFields.get(elementFieldId + "@" + attribute);
  }

  /** Whether a field belongs to the evaluated tree (descendant of rDE, outside signer groups). */
  boolean inTree(String fieldId) {
    return chains.containsKey(fieldId);
  }

  /**
   * Whether a rule superseded without a published end date has a replacement in force: another rule
   * of the same field and type, not superseded, that applies on the date.
   */
  boolean hasReplacement(Rule rule, Environment environment, LocalDate date) {
    for (Rule other : byFieldAndType.getOrDefault(rule.field() + "|" + rule.type(), List.of())) {
      if (other != rule
          && !other.validity().isSuperseded()
          && other.validity().appliesOn(environment, date)) {
        return true;
      }
    }
    return false;
  }

  /**
   * The deepest repeated group (maximum occurrence above 1) that contains, or is, both the context
   * field and the target field; null when they share none. Conditions on a target inside that group
   * are evaluated on the same repetition as the context (spec FORMATO.md, Condiciones).
   */
  @Nullable String sharedRepeatedGroup(String contextField, String targetField) {
    List<String> context = chains.getOrDefault(contextField, List.of(contextField));
    List<String> target = chains.getOrDefault(targetField, List.of(targetField));
    for (String id : context) {
      Field f = fields.get(id);
      if (f != null && f.maxOccurs() > 1 && target.contains(id)) {
        return id;
      }
    }
    return null;
  }

  private static List<String> chainOf(Map<String, Field> fields, String id) {
    List<String> chain = new ArrayList<>();
    String current = id;
    Set<String> seen = new HashSet<>();
    while (current != null && seen.add(current)) {
      chain.add(current);
      Field f = fields.get(current);
      current = f == null ? null : f.parent();
    }
    return chain;
  }
}
