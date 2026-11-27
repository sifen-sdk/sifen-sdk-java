/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.sifensdk.core.internal.spec.Field;
import io.github.sifensdk.core.internal.spec.Spec;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * ADR-0003: the model and the field table of the specification must not drift apart. Walks every
 * {@link SifenField} reachable from {@link ElectronicDocument} and compares it with the fields of
 * the groups this version covers.
 */
class ModelCoverageTest {

  /** Groups ({@code grupo} of {@code spec/campos/*.json}) covered: invoice and credit note. */
  private static final Set<String> GROUPS =
      Set.of(
          "AA-A", "B", "C", "D", "D1", "D2", "D3", "E", "E1", "E4", "E5", "E6", "E7", "E8", "E8.1",
          "E8.2", "E8.4", "E8.5", "E9", "E10", "F", "G", "H");

  /** Group roots of {@code gDtipDE} left for a later version, with all their fields: none. */
  private static final Map<String, String> OUT_OF_SCOPE = Map.of();

  /** Fields of the covered groups that are deliberately not in the model, with the reason. */
  private static final Map<String, String> EXCLUDED = new LinkedHashMap<>();

  static {
    EXCLUDED.put("AA001", "rDE envelope: built by the mapping");
    EXCLUDED.put("AA002", "dVerFor: completed by the SDK (ADR-0011)");
    EXCLUDED.put("A002", "Id (CDC): computed by the mapping (ADR-0011, C-101)");
    EXCLUDED.put("A003", "dDVId: computed by the mapping (ADR-0011)");
    EXCLUDED.put("A004", "dFecFirma: parameter signatureDateTime of the mapping (ADR-0011)");
    EXCLUDED.put("A005", "dSisFact: completed by the SDK while the XSD requires it (C-103)");
    EXCLUDED.put("C009", "dFeFinT: not in the XSD (C-105)");
    EXCLUDED.put("F024", "dTotCom: not in the XSD (C-505)");
    EXCLUDED.put("E756", "dNomImp: estado eliminado");
    EXCLUDED.put("E757", "dDirImp: estado eliminado");
    EXCLUDED.put("E758", "dNumFir: estado eliminado");
    EXCLUDED.put("X-H-dNumCuoDocAso", "only in the XSD, undocumented: not generated (C-507)");
    EXCLUDED.put("X-H-dImpCuoDocAso", "only in the XSD, undocumented: not generated (C-507)");
  }

  /** Accessors whose name differs from {@code nombre} in the specification, with the reason. */
  private static final Map<String, String> RENAMED =
      Map.of(
          "G001", "complementaryData", // the spec repeats "generalData" (D001) under A001
          "H001", "associatedDocuments", // a list of up to 99
          "E824", "paymentDueDates", // a list of up to 3
          "E940", "deliveryLocations"); // a list of up to 99

  private final Spec spec = Spec.bundled();

  @Test
  void modelCoversExactlyTheFieldsOfTheSpecification() {
    Map<String, Field> fields = spec.fields();
    Set<String> outOfScope = new HashSet<>();
    for (String root : OUT_OF_SCOPE.keySet()) {
      descendants(root, fields, outOfScope);
    }
    Set<String> expected =
        fields.values().stream()
            .filter(f -> GROUPS.contains(f.group()))
            .map(Field::id)
            .filter(id -> !EXCLUDED.containsKey(id) && !outOfScope.contains(id))
            .collect(Collectors.toCollection(TreeSet::new));

    Set<String> modeled = new TreeSet<>(modeledFields().keySet());

    assertThat(difference(expected, modeled)).as("fields missing in the model").isEmpty();
    assertThat(difference(modeled, expected)).as("fields in the model but not expected").isEmpty();
  }

  @Test
  void annotationsMatchTheSpecification() {
    for (Map.Entry<String, Annotated> e : modeledFields().entrySet()) {
      Field field = spec.field(e.getKey()).orElseThrow();
      Annotated a = e.getValue();
      assertThat(a.xml).as(e.getKey()).isEqualTo(field.xml().strip());
      if (a.accessor != null) {
        assertThat(a.accessor)
            .as(e.getKey())
            .isEqualTo(RENAMED.getOrDefault(e.getKey(), field.name()));
      }
      assertThat(field.state()).as(e.getKey()).isNotEqualTo("eliminado");
    }
  }

  @Test
  void eachFieldIsModeledOnce() {
    List<String> all = new ArrayList<>();
    walk(ElectronicDocument.class, new HashSet<>(), all, new LinkedHashMap<>());
    assertThat(all).doesNotHaveDuplicates();
  }

  @Test
  void exclusionsAreRealAndJustified() {
    for (String id : EXCLUDED.keySet()) {
      assertThat(spec.field(id)).as(id).isPresent();
    }
    for (String id : List.of("E756", "E757", "E758")) {
      assertThat(spec.field(id).orElseThrow().state()).isEqualTo("eliminado");
    }
    assertThat(spec.field("C009").orElseThrow().contradictions()).contains("C-105");
    assertThat(spec.field("F024").orElseThrow().contradictions()).contains("C-505");
    assertThat(modeledFields()).doesNotContainKeys(EXCLUDED.keySet().toArray(new String[0]));
  }

  @Test
  void groupTypesAreImmutableFinalClasses() {
    Set<Class<?>> types = new HashSet<>();
    walk(ElectronicDocument.class, types, new ArrayList<>(), new LinkedHashMap<>());
    assertThat(types).hasSize(43);
    for (Class<?> type : types) {
      assertThat(Modifier.isFinal(type.getModifiers())).as(type.getName()).isTrue();
      for (java.lang.reflect.Field f : type.getDeclaredFields()) {
        assertThat(Modifier.isFinal(f.getModifiers())).as(f.toString()).isTrue();
        assertThat(Modifier.isPrivate(f.getModifiers())).as(f.toString()).isTrue();
      }
    }
  }

  // ---------------------------------------------------------------------------------------------

  private static final class Annotated {
    final String xml;
    final String accessor;

    Annotated(String xml, String accessor) {
      this.xml = xml;
      this.accessor = accessor;
    }
  }

  private static Map<String, Annotated> modeledFields() {
    Map<String, Annotated> out = new LinkedHashMap<>();
    walk(ElectronicDocument.class, new HashSet<>(), new ArrayList<>(), out);
    return out;
  }

  private static void walk(
      Class<?> type, Set<Class<?>> seen, List<String> all, Map<String, Annotated> out) {
    if (!seen.add(type)) {
      return;
    }
    SifenField group = type.getAnnotation(SifenField.class);
    assertThat(group).as(type.getName()).isNotNull();
    if (!out.containsKey(group.id())) {
      out.put(group.id(), new Annotated(group.xml(), null));
      all.add(group.id());
    }
    for (Method m : type.getDeclaredMethods()) {
      SifenField f = m.getAnnotation(SifenField.class);
      if (f == null) {
        continue;
      }
      Class<?> target = elementType(m);
      if (target.isAnnotationPresent(SifenField.class)) {
        assertThat(target.getAnnotation(SifenField.class).id()).isEqualTo(f.id());
        out.put(f.id(), new Annotated(f.xml(), m.getName()));
        walk(target, seen, all, out);
      } else {
        all.add(f.id());
        out.put(f.id(), new Annotated(f.xml(), m.getName()));
      }
    }
  }

  private static Class<?> elementType(Method m) {
    if (List.class.isAssignableFrom(m.getReturnType())) {
      Type t = ((ParameterizedType) m.getGenericReturnType()).getActualTypeArguments()[0];
      return (Class<?>) t;
    }
    return m.getReturnType();
  }

  private static void descendants(String id, Map<String, Field> fields, Set<String> out) {
    out.add(id);
    for (Field f : fields.values()) {
      if (id.equals(f.parent())) {
        descendants(f.id(), fields, out);
      }
    }
  }

  private static Set<String> difference(Set<String> a, Set<String> b) {
    Set<String> d = new TreeSet<>(a);
    d.removeAll(b);
    return d;
  }
}
