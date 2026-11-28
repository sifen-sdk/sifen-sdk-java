/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.sifensdk.core.internal.json.Json;
import io.github.sifensdk.core.internal.spec.Spec;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

/**
 * ADR 0014: every catalog of {@code spec} that declares {@code nombre} has an enum here with the
 * same codes, names and descriptions, and nothing else. When SIFEN adds a code and {@code spec}
 * takes it, this test asks to run {@code scripts/generar-catalogos.py}.
 */
class CatalogEnumsTest {

  @Test
  void everyNamedCatalogHasAnEnumThatMatchesIt() throws Exception {
    List<String> problems = new ArrayList<>();
    TreeSet<String> expectedTypes = new TreeSet<>();
    for (String id : Spec.bundled().catalogs().keySet()) {
      Map<String, Object> catalog = object(read("catalogos/" + id + ".json"));
      if (!catalog.containsKey("nombre")) {
        continue;
      }
      String type = pascal((String) catalog.get("nombre"));
      expectedTypes.add(type);
      Class<?> cls;
      try {
        cls = Class.forName("io.github.sifensdk.core.catalog." + type);
      } catch (ClassNotFoundException e) {
        problems.add(id + ": missing enum " + type);
        continue;
      }
      Map<String, String[]> expected = new LinkedHashMap<>();
      for (Object o : (List<?>) Objects.requireNonNull(catalog.get("entradas"))) {
        Map<String, Object> e = object(o);
        String description =
            (String)
                e.getOrDefault("descripcion_xsd", Objects.requireNonNull(e.get("descripcion")));
        expected.put(
            ((String) e.get("nombre")).toUpperCase(Locale.ROOT),
            new String[] {String.valueOf(e.get("codigo")), description});
      }
      Map<String, String[]> actual = new LinkedHashMap<>();
      for (Object constant : Objects.requireNonNull(cls.getEnumConstants())) {
        CatalogCode c = (CatalogCode) constant;
        actual.put(((Enum<?>) constant).name(), new String[] {c.code(), c.description()});
        Object back = cls.getMethod("fromCode", String.class).invoke(null, c.code());
        if (!Optional.of(constant).equals(back)) {
          problems.add(type + "." + ((Enum<?>) constant).name() + ": fromCode does not return it");
        }
      }
      if (!expected.keySet().equals(actual.keySet())) {
        problems.add(
            type + ": constants " + actual.keySet() + " but spec has " + expected.keySet());
        continue;
      }
      for (Map.Entry<String, String[]> e : expected.entrySet()) {
        String[] a = actual.get(e.getKey());
        if (!e.getValue()[0].equals(a[0]) || !e.getValue()[1].equals(a[1])) {
          problems.add(
              type
                  + "."
                  + e.getKey()
                  + ": ("
                  + a[0]
                  + ", "
                  + a[1]
                  + ") but spec has ("
                  + e.getValue()[0]
                  + ", "
                  + e.getValue()[1]
                  + ")");
        }
      }
    }
    assertThat(problems).as("Run scripts/generar-catalogos.py and ./mvnw spotless:apply").isEmpty();
    assertThat(generatedTypes()).isEqualTo(expectedTypes);
  }

  @Test
  void unknownCodesAreEmptyNotErrors() {
    assertThat(EmissionType.fromCode("1")).contains(EmissionType.NORMAL);
    assertThat(EmissionType.fromCode("7")).isEmpty();
    assertThat(DocumentType.INVOICE.code()).isEqualTo("1");
    assertThat(DocumentType.INVOICE.description()).isEqualTo("Factura electrónica");
  }

  /** The enums compiled in this package, so that a catalog that loses its name loses its enum. */
  private static TreeSet<String> generatedTypes() {
    TreeSet<String> out = new TreeSet<>();
    URL url = Objects.requireNonNull(EmissionType.class.getResource("EmissionType.class"));
    assertThat(url.getProtocol()).isEqualTo("file");
    File dir = new File(url.getPath()).getParentFile();
    for (String f : Objects.requireNonNull(dir.list())) {
      String n = f.substring(0, f.length() - ".class".length());
      if (f.endsWith(".class")
          && !n.contains("$")
          && !n.equals("CatalogCode")
          && !n.equals("package-info")) {
        out.add(n);
      }
    }
    return out;
  }

  private static String pascal(String snake) {
    StringBuilder b = new StringBuilder();
    for (String p : snake.split("_")) {
      b.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
    }
    return b.toString();
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> object(Object o) {
    return (Map<String, Object>) o;
  }

  private static Object read(String path) throws IOException {
    try (InputStream in = Spec.class.getResourceAsStream(Spec.BASE + path)) {
      Objects.requireNonNull(in, path);
      return Objects.requireNonNull(
          Json.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8)));
    }
  }
}
