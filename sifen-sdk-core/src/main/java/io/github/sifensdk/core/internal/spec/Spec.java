/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.spec;

import io.github.sifensdk.core.internal.json.Json;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * The SIFEN specification bundled in the core (from the {@code sifen-sdk/spec} repository).
 *
 * <p>Immutable and thread-safe. Load it once with {@link #bundled()}.
 */
public final class Spec {

  /** Classpath folder of the bundled specification. */
  public static final String BASE = "/io/github/sifensdk/core/spec/";

  private static volatile @Nullable Spec bundled;

  private final Map<String, Field> fields;
  private final List<Rule> rules;
  private final Map<String, Catalog> catalogs;
  private final List<ResponseCode> responses;

  private Spec(
      Map<String, Field> fields,
      List<Rule> rules,
      Map<String, Catalog> catalogs,
      List<ResponseCode> responses) {
    this.fields = Collections.unmodifiableMap(fields);
    this.rules = Collections.unmodifiableList(rules);
    this.catalogs = Collections.unmodifiableMap(catalogs);
    this.responses = Collections.unmodifiableList(responses);
  }

  /**
   * Returns the specification bundled in this jar, loading it on first use.
   *
   * @return the specification
   */
  public static Spec bundled() {
    Spec s = bundled;
    if (s == null) {
      synchronized (Spec.class) {
        s = bundled;
        if (s == null) {
          s = load();
          bundled = s;
        }
      }
    }
    return s;
  }

  /**
   * All fields by id.
   *
   * @return fields by id
   */
  public Map<String, Field> fields() {
    return fields;
  }

  /**
   * A field by id.
   *
   * @param id field id
   * @return the field, if any
   */
  public Optional<Field> field(String id) {
    return Optional.ofNullable(fields.get(id));
  }

  /**
   * All rules, in file order.
   *
   * @return rules
   */
  public List<Rule> rules() {
    return rules;
  }

  /**
   * All catalogs by id.
   *
   * @return catalogs by id
   */
  public Map<String, Catalog> catalogs() {
    return catalogs;
  }

  /**
   * All response codes.
   *
   * @return response codes
   */
  public List<ResponseCode> responses() {
    return responses;
  }

  // ---------------------------------------------------------------------------------------------

  private static Spec load() {
    Map<String, Field> fields = new LinkedHashMap<>();
    for (String file : listing("campos")) {
      Map<String, Object> doc = object(read("campos/" + file));
      String group = str(doc.get("grupo"), "");
      for (Object o : list(doc.get("campos"))) {
        Field f = field(object(o), group);
        fields.put(f.id(), f);
      }
    }
    List<Rule> rules = new ArrayList<>();
    for (String file : listing("reglas")) {
      for (Object o : list(object(read("reglas/" + file)).get("reglas"))) {
        rules.add(rule(object(o)));
      }
    }
    Map<String, Catalog> catalogs = new LinkedHashMap<>();
    for (String file : listing("catalogos")) {
      Catalog c = catalog(object(read("catalogos/" + file)));
      catalogs.put(c.id(), c);
    }
    List<ResponseCode> responses = new ArrayList<>();
    for (String file : listing("respuestas")) {
      for (Object o : list(object(read("respuestas/" + file)).get("respuestas"))) {
        responses.add(response(object(o)));
      }
    }
    return new Spec(fields, rules, catalogs, responses);
  }

  /** The build writes no directory listing, so the file names come from an index resource. */
  private static List<String> listing(String folder) {
    String index = readText(BASE + "index/" + folder + ".txt");
    List<String> names = new ArrayList<>();
    for (String line : index.split("\\R")) {
      if (!line.isBlank()) {
        names.add(line.trim());
      }
    }
    return names;
  }

  private static @Nullable Object read(String path) {
    return Json.parse(readText(BASE + path));
  }

  private static String readText(String resource) {
    try (InputStream in = Spec.class.getResourceAsStream(resource)) {
      if (in == null) {
        throw new IllegalStateException("Missing bundled specification resource: " + resource);
      }
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static Field field(Map<String, Object> m, String group) {
    String occ = str(m.get("ocurrencia"), "0-1");
    String[] parts = occ.split("-");
    int min = Integer.parseInt(parts[0]);
    int max = "N".equals(parts[1]) ? Integer.MAX_VALUE : Integer.parseInt(parts[1]);
    return new Field(
        str(m.get("id"), ""),
        str(m.get("xml"), ""),
        str(m.get("nombre"), ""),
        nstr(m.get("padre")),
        str(m.get("tipo"), ""),
        nstr(m.get("longitud")),
        min,
        max,
        str(m.get("descripcion"), ""),
        nstr(m.get("catalogo")),
        str(m.get("estado"), "vigente"),
        sources(m.get("fuentes")),
        validity(m.get("vigencia")),
        strings(m.get("contradiccion")),
        group);
  }

  private static Rule rule(Map<String, Object> m) {
    Object cond = m.get("condicion");
    return new Rule(
        str(m.get("id"), ""),
        str(m.get("campo"), ""),
        str(m.get("tipo"), ""),
        str(m.get("texto"), ""),
        cond == null ? null : condition(object(cond)),
        !Boolean.TRUE.equals(m.get("no_declarativa")),
        nstr(m.get("formula")),
        strings(m.get("operandos")),
        m.get("holgura"),
        nstr(m.get("codigo_sifen")),
        sources(m.get("fuentes")),
        validity(m.get("vigencia")),
        strings(m.get("contradiccion")));
  }

  private static Condition condition(Map<String, Object> m) {
    if (m.containsKey("todas")) {
      return new Condition.All(conditions(m.get("todas")));
    }
    if (m.containsKey("alguna")) {
      return new Condition.Any(conditions(m.get("alguna")));
    }
    if (m.containsKey("no")) {
      return new Condition.Not(condition(object(m.get("no"))));
    }
    List<String> values = new ArrayList<>();
    if (m.containsKey("valor")) {
      values.add(scalar(m.get("valor")));
    }
    for (Object v : list(m.get("valores"))) {
      values.add(scalar(v));
    }
    return new Condition.Compare(
        str(m.get("campo"), ""),
        str(m.get("op"), ""),
        Collections.unmodifiableList(values),
        str(m.get("cuantificador"), "alguno"));
  }

  private static List<Condition> conditions(@Nullable Object o) {
    List<Condition> out = new ArrayList<>();
    for (Object c : list(o)) {
      out.add(condition(object(c)));
    }
    return Collections.unmodifiableList(out);
  }

  private static Catalog catalog(Map<String, Object> m) {
    List<CatalogEntry> entries = new ArrayList<>();
    for (Object o : list(m.get("entradas"))) {
      Map<String, Object> e = object(o);
      entries.add(
          new CatalogEntry(
              scalar(e.get("codigo")),
              str(e.get("descripcion"), ""),
              nstr(e.get("descripcion_xsd")),
              Boolean.TRUE.equals(e.get("descripcion_libre")),
              e.get("padre") == null ? null : scalar(e.get("padre")),
              str(e.get("estado"), "vigente"),
              validity(e.get("vigencia"))));
    }
    Map<String, String> descriptions = new LinkedHashMap<>();
    Object d = m.get("descripciones");
    if (d != null) {
      for (Map.Entry<String, Object> e : object(d).entrySet()) {
        descriptions.put(e.getKey(), scalar(e.getValue()));
      }
    }
    return new Catalog(
        str(m.get("id"), ""),
        str(m.get("titulo"), ""),
        strings(m.get("campos")),
        Collections.unmodifiableMap(descriptions),
        Collections.unmodifiableList(entries));
  }

  private static ResponseCode response(Map<String, Object> m) {
    return new ResponseCode(
        str(m.get("codigo"), ""),
        str(m.get("servicio"), ""),
        str(m.get("mensaje"), ""),
        str(m.get("clase"), ""),
        nstr(m.get("accion")),
        nstr(m.get("campo")),
        sources(m.get("fuentes")),
        validity(m.get("vigencia")));
  }

  private static List<Source> sources(@Nullable Object o) {
    List<Source> out = new ArrayList<>();
    for (Object s : list(o)) {
      Map<String, Object> m = object(s);
      out.add(new Source(str(m.get("doc"), ""), str(m.get("ref"), "")));
    }
    return Collections.unmodifiableList(out);
  }

  private static Validity validity(@Nullable Object o) {
    if (o == null) {
      return Validity.ALWAYS;
    }
    Map<String, Object> m = object(o);
    Map<String, Object> from = m.get("desde") == null ? Map.of() : object(m.get("desde"));
    Map<String, Object> until = m.get("hasta") == null ? Map.of() : object(m.get("hasta"));
    return new Validity(
        date(from.get("test")),
        date(from.get("produccion")),
        date(until.get("test")),
        date(until.get("produccion")),
        m.containsKey("hasta"));
  }

  private static @Nullable LocalDate date(@Nullable Object o) {
    return o == null ? null : LocalDate.parse(scalar(o));
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> object(@Nullable Object o) {
    if (!(o instanceof Map)) {
      throw new IllegalStateException("Malformed specification: expected an object");
    }
    return (Map<String, Object>) o;
  }

  @SuppressWarnings("unchecked")
  private static List<Object> list(@Nullable Object o) {
    if (o == null) {
      return List.of();
    }
    if (!(o instanceof List)) {
      throw new IllegalStateException("Malformed specification: expected an array");
    }
    return (List<Object>) o;
  }

  private static List<String> strings(@Nullable Object o) {
    if (o == null) {
      return List.of();
    }
    if (!(o instanceof List)) {
      return List.of(scalar(o));
    }
    List<String> out = new ArrayList<>();
    for (Object v : list(o)) {
      out.add(scalar(v));
    }
    return Collections.unmodifiableList(out);
  }

  private static String scalar(@Nullable Object o) {
    if (o instanceof BigDecimal) {
      return ((BigDecimal) o).toPlainString();
    }
    return String.valueOf(o);
  }

  private static String str(@Nullable Object o, String fallback) {
    return o == null ? fallback : scalar(o);
  }

  private static @Nullable String nstr(@Nullable Object o) {
    return o == null ? null : scalar(o);
  }
}
