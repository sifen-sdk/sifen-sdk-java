/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.sifensdk.core.catalog.AssociatedDocumentType;
import io.github.sifensdk.core.catalog.CatalogCode;
import io.github.sifensdk.core.catalog.DocumentType;
import io.github.sifensdk.core.catalog.EmissionType;
import io.github.sifensdk.core.catalog.PaymentType;
import io.github.sifensdk.core.catalog.PrintedDocumentType;
import io.github.sifensdk.core.internal.json.Json;
import io.github.sifensdk.core.internal.spec.Spec;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

/**
 * ADR 0014: every model field whose catalog has an enum offers a typed getter ({@code
 * Optional<Enum>}) and a typed builder setter, named like the {@code String} accessor without its
 * {@code Code} suffix; the two forms store the same code.
 */
class TypedCatalogAccessorsTest {

  private static final Set<String> CURRENCY_FIELDS = Set.of("D015", "E609", "E653");

  @Test
  void everyCatalogFieldHasTypedAccessors() throws Exception {
    Map<String, Class<?>> typed = typedFields();
    List<String> problems = new ArrayList<>();
    Set<String> seen = new HashSet<>();
    for (Class<?> cls : modelClasses()) {
      for (Method getter : cls.getDeclaredMethods()) {
        SifenField f = getter.getAnnotation(SifenField.class);
        if (f == null || getter.getReturnType() != String.class) {
          continue;
        }
        Class<?> type = CURRENCY_FIELDS.contains(f.id()) ? Currency.class : typed.get(f.id());
        if (type == null) {
          continue;
        }
        seen.add(f.id());
        String name = typedName(getter.getName(), type);
        String where = cls.getSimpleName() + "." + name + " (" + f.id() + ")";
        try {
          Method m = cls.getMethod(name);
          boolean ok =
              m.getReturnType() == Optional.class
                  && m.getGenericReturnType() instanceof ParameterizedType p
                  && p.getActualTypeArguments()[0] == type;
          if (!ok) {
            problems.add(where + ": getter must return Optional<" + type.getSimpleName() + ">");
          }
        } catch (NoSuchMethodException e) {
          problems.add(where + ": missing getter");
        }
        Class<?> builder = Class.forName(cls.getName() + "$Builder");
        try {
          builder.getMethod(name, type);
        } catch (NoSuchMethodException e) {
          problems.add(where + ": missing builder setter");
        }
      }
    }
    assertThat(problems).isEmpty();
    Set<String> expected = new HashSet<>(typed.keySet());
    expected.addAll(CURRENCY_FIELDS);
    expected.removeAll(Set.of("ContRUC04")); // RUC query response: RucQueryResult in the client
    assertThat(seen).as("catalog fields not found in the model").isEqualTo(expected);
  }

  @Test
  void typedAndStringSettersStoreTheSameCode() {
    Operation typed =
        Operation.builder().emissionType(EmissionType.CONTINGENCY).securityCode("1").build();
    Operation coded = Operation.builder().emissionTypeCode("2").securityCode("1").build();
    assertThat(typed).isEqualTo(coded);
    assertThat(typed.emissionType()).contains(EmissionType.CONTINGENCY);

    Payment payment =
        Payment.builder()
            .type(PaymentType.CASH)
            .amount(new BigDecimal("1000"))
            .currency(Currency.getInstance("PYG"))
            .build();
    assertThat(payment.typeCode()).isEqualTo("1");
    assertThat(payment.currencyCode()).isEqualTo("PYG");
    assertThat(payment.currency()).contains(Currency.getInstance("PYG"));
  }

  @Test
  void unknownOrMissingCodesGiveEmptyNotErrors() {
    Operation future = Operation.builder().emissionTypeCode("3").securityCode("1").build();
    assertThat(future.emissionTypeCode()).isEqualTo("3");
    assertThat(future.emissionType()).isEmpty();

    AssociatedDocument printed =
        AssociatedDocument.builder().type(AssociatedDocumentType.PRINTED).build();
    assertThat(printed.typeCode()).isEqualTo("2");
    assertThat(printed.printedDocumentTypeCode()).isNull();
    assertThat(printed.printedDocumentType()).isEmpty();
    assertThat(
            printed.toBuilder()
                .printedDocumentType(PrintedDocumentType.INVOICE)
                .build()
                .printedDocumentTypeCode())
        .isEqualTo("1");
    assertThat(printed.toBuilder().printedDocumentType(null).build().printedDocumentTypeCode())
        .isNull();
    assertThat(DocumentType.fromCode("99")).isEmpty();
  }

  private static String typedName(String getter, Class<?> type) {
    String name = getter.endsWith("Code") ? getter.substring(0, getter.length() - 4) : getter;
    if (name.isEmpty() || name.equals("code")) {
      String simple = type.getSimpleName();
      name = Character.toLowerCase(simple.charAt(0)) + simple.substring(1);
    }
    return name;
  }

  /** Field id → enum, from the catalogs of {@code spec} that declare {@code nombre}. */
  private static Map<String, Class<?>> typedFields() throws Exception {
    Map<String, Class<?>> out = new TreeMap<>();
    for (String id : Spec.bundled().catalogs().keySet()) {
      Map<String, Object> c = object(read("catalogos/" + id + ".json"));
      if (!c.containsKey("nombre")) {
        continue;
      }
      StringBuilder type = new StringBuilder("io.github.sifensdk.core.catalog.");
      for (String p : ((String) c.get("nombre")).split("_")) {
        type.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
      }
      Class<?> cls = Class.forName(type.toString());
      assertThat(CatalogCode.class).isAssignableFrom(cls);
      Set<String> descriptions = new HashSet<>();
      Object d = c.get("descripciones");
      if (d != null) {
        for (Object v : object(d).values()) {
          descriptions.add((String) v);
        }
      }
      for (Object f : (List<?>) c.getOrDefault("campos", List.of())) {
        if (!descriptions.contains(f)) {
          out.put((String) f, cls);
        }
      }
    }
    return out;
  }

  private static List<Class<?>> modelClasses() throws ClassNotFoundException {
    URL url = Objects.requireNonNull(Operation.class.getResource("Operation.class"));
    List<Class<?>> out = new ArrayList<>();
    for (String f : Objects.requireNonNull(new File(url.getPath()).getParentFile().list())) {
      if (f.endsWith(".class") && !f.contains("$") && !f.startsWith("package-info")) {
        out.add(Class.forName("io.github.sifensdk.core.model." + f.replace(".class", "")));
      }
    }
    return out;
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
