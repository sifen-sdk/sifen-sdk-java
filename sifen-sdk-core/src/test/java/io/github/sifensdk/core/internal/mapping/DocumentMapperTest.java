/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.sifensdk.core.internal.json.Json;
import io.github.sifensdk.core.internal.spec.Spec;
import io.github.sifensdk.core.internal.tree.Node;
import io.github.sifensdk.core.model.ElectronicDocument;
import io.github.sifensdk.core.model.Item;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class DocumentMapperTest {

  private static final LocalDateTime SIGNED = LocalDateTime.of(2026, 11, 26, 10, 5, 30);

  @Test
  void rootHasVersionAndDeWithTheCdcAndNoSignature() {
    Node root = DocumentMapper.toTree(FullDocument.document(), SIGNED);

    assertThat(root.name()).isEqualTo("rDE");
    assertThat(root.fieldId()).isEqualTo("AA001");
    assertThat(names(root)).containsExactly("dVerFor", "DE");
    assertThat(root.children().get(0).text()).isEqualTo("150");
    Node de = root.children().get(1);
    assertThat(de.attributes())
        .containsExactly(Map.entry("Id", "01444444017001001001452822017012515873260988"));
    assertThat(names(de))
        .containsExactly(
            "dDVId",
            "dFecFirma",
            "dSisFact",
            "gOpeDE",
            "gTimb",
            "gDatGralOpe",
            "gDtipDE",
            "gTotSub",
            "gCamGen",
            "gCamDEAsoc");
    assertThat(text(root, "A003")).isEqualTo("8");
    assertThat(text(root, "A004")).isEqualTo("2026-11-26T10:05:30");
    assertThat(text(root, "A005")).isEqualTo("1");
  }

  @Test
  void everyNodeCarriesItsFieldId() {
    Node root = DocumentMapper.toTree(FullDocument.document(), SIGNED);
    List<Node> all = new ArrayList<>();
    collect(root, all);
    assertThat(all).allSatisfy(n -> assertThat(n.fieldId()).as(n.path()).isNotNull());
    Spec spec = Spec.bundled();
    assertThat(all)
        .allSatisfy(n ->
            assertThat(spec.field(n.fieldId()).orElseThrow().xml().strip())
                .as(n.path())
                .isEqualTo(n.name()));
  }

  @Test
  void writesEveryInformedValueExactlyAsGiven() {
    Node root = DocumentMapper.toTree(FullDocument.document(), SIGNED);
    Map<String, String> expected = FullDocument.expectedTexts();

    Map<String, String> written = new HashMap<>();
    List<Node> all = new ArrayList<>();
    collect(root, all);
    for (Node n : all) {
      if (n.isLeaf()) {
        written.put(n.fieldId(), n.text());
      }
    }
    assertThat(written).containsAllEntriesOf(expected);
  }

  @Test
  void writesTheCatalogDescriptionOfEveryCode() {
    Node root = DocumentMapper.toTree(FullDocument.document(), SIGNED);
    for (String id : FullDocument.catalogDescriptionIds()) {
      assertThat(root.findAll(id)).as(id).isNotEmpty();
      assertThat(root.findAll(id))
          .as(id)
          .allSatisfy(n -> assertThat(n.text()).isNotEqualTo("Descripción libre"));
    }
    assertThat(text(root, "C003")).isEqualTo("Factura electrónica");
    assertThat(text(root, "B003")).isEqualTo("Normal");
    assertThat(text(root, "E710")).isEqualTo("m");
  }

  @Test
  void followsTheXsdSequenceAtEveryLevel() throws IOException {
    Map<String, Object> types = xsdTypes();
    Node root = DocumentMapper.toTree(FullDocument.document(), SIGNED);
    checkOrder(root, "rDE", types);
    checkOrder(DocumentMapper.toTree(Samples.invoice(), SIGNED), "rDE", types);
    checkOrder(DocumentMapper.toTree(Samples.creditNote(), SIGNED), "rDE", types);
  }

  @Test
  void mapsTheSampleInvoice() {
    ElectronicDocument invoice = Samples.invoice();
    Node root = DocumentMapper.toTree(invoice, SIGNED);

    assertThat(root.children().get(1).attributes().get("Id"))
        .isEqualTo(
            "0144444401700100100000012202611261123456789"
                + io.github.sifensdk.core.cdc.Cdc.checkDigit(
                    "0144444401700100100000012202611261123456789"));
    assertThat(texts(root, "C002")).containsExactly("1");
    assertThat(texts(root, "D011")).containsExactly("1");
    assertThat(texts(root, "D012")).containsExactly("Venta de mercadería");
    assertThat(texts(root, "D016")).containsExactly("Guarani");
    assertThat(texts(root, "E012")).containsExactly("Operación presencial");
    assertThat(texts(root, "E602")).containsExactly("Contado");
    assertThat(texts(root, "E607")).containsExactly("Efectivo");
    assertThat(texts(root, "E721")).containsExactly("110000", "10500");
    assertThat(texts(root, "E727")).containsExactly("110000", "21000");
    assertThat(texts(root, "E735")).containsExactly("100000", "20000");
    assertThat(texts(root, "E736")).containsExactly("10000", "1000");
    assertThat(texts(root, "F014")).containsExactly("131000");
    assertThat(texts(root, "F017")).containsExactly("11000");
    assertThat(root.findAll("E400")).isEmpty();
    assertThat(root.findAll("H001")).isEmpty();
    assertThat(root.findAll("E700")).hasSize(2);
    assertThat(root.findAll("E721").get(1).path())
        .isEqualTo("rDE/DE/gDtipDE/gCamItem[2]/gValorItem/dPUniProSer");
  }

  @Test
  void mapsTheSampleCreditNote() {
    Node root = DocumentMapper.toTree(Samples.creditNote(), SIGNED);

    assertThat(texts(root, "C002")).containsExactly("5");
    assertThat(texts(root, "C003")).containsExactly("Nota de crédito electrónica");
    assertThat(texts(root, "E401")).containsExactly("2");
    assertThat(texts(root, "E402")).containsExactly("Devolución");
    assertThat(texts(root, "H002")).containsExactly("1");
    assertThat(texts(root, "H003")).containsExactly("Electrónico");
    assertThat(texts(root, "H004")).containsExactly(Cdc.of(Samples.invoice()));
    assertThat(root.findAll("D011")).isEmpty();
    assertThat(root.findAll("E010")).isEmpty();
    assertThat(root.findAll("E600")).isEmpty();
    assertThat(texts(root, "F008")).containsExactly("110000");
  }

  @Test
  void amountsKeepTheirScaleAndAreNeverRounded() {
    ElectronicDocument usd = withFirstItemUnitPrice(new BigDecimal("1234.56780000"));
    Node root = DocumentMapper.toTree(usd, SIGNED);
    assertThat(texts(root, "E721").get(0)).isEqualTo("1234.56780000");

    ElectronicDocument exponent = withFirstItemUnitPrice(new BigDecimal("1E+5"));
    assertThat(texts(DocumentMapper.toTree(exponent, SIGNED), "E721").get(0)).isEqualTo("100000");

    ElectronicDocument tiny = withFirstItemUnitPrice(new BigDecimal("0.00000001"));
    assertThat(texts(DocumentMapper.toTree(tiny, SIGNED), "E721").get(0)).isEqualTo("0.00000001");
  }

  @Test
  void documentToStringShowsOnlyCounts() {
    assertThat(Samples.invoice().toString())
        .isEqualTo("ElectronicDocument[items=2, associatedDocuments=0]");
    assertThat(Samples.creditNote().toString()).doesNotContain("44444401", "110000");
  }

  @Test
  void leavesOutWhatIsNotInformed() {
    Node root = DocumentMapper.toTree(Samples.invoice(), SIGNED);
    assertThat(root.findAll("B005")).isEmpty();
    assertThat(root.findAll("C010")).isEmpty();
    assertThat(root.findAll("D017")).isEmpty();
    assertThat(root.findAll("F002")).isEmpty();
    assertThat(root.findAll("G001")).isEmpty();
    // Elements the XSD does not have are never written (C-105, C-505).
    assertThat(root.findAll("C009")).isEmpty();
    assertThat(root.findAll("F024")).isEmpty();
  }

  // ---------------------------------------------------------------------------------------------

  private static ElectronicDocument withFirstItemUnitPrice(BigDecimal price) {
    ElectronicDocument invoice = Samples.invoice();
    List<Item> items = new ArrayList<>(invoice.documentTypeData().items());
    Item first = items.get(0);
    items.set(
        0, first.toBuilder().values(first.values().toBuilder().unitPrice(price).build()).build());
    return invoice.toBuilder()
        .documentTypeData(invoice.documentTypeData().toBuilder().items(items).build())
        .build();
  }

  private static List<String> names(Node node) {
    return node.children().stream().map(Node::name).collect(Collectors.toList());
  }

  private static String text(Node root, String id) {
    List<Node> found = root.findAll(id);
    assertThat(found).as(id).hasSize(1);
    return found.get(0).text();
  }

  private static List<String> texts(Node root, String id) {
    return root.findAll(id).stream().map(Node::text).collect(Collectors.toList());
  }

  private static void collect(Node node, List<Node> out) {
    out.add(node);
    for (Node c : node.children()) {
      collect(c, out);
    }
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> xsdTypes() throws IOException {
    String json =
        Files.readString(Path.of("..", "spec", "xsd", "arbol.json"), StandardCharsets.UTF_8);
    Map<String, Object> tree = (Map<String, Object>) Json.parse(json);
    Map<String, Object> types = new HashMap<>();
    for (String file : List.of("DE_v150.xsd", "DE_Types_v150.xsd")) {
      Map<String, Object> schema = (Map<String, Object>) tree.get(file);
      types.putAll((Map<String, Object>) schema.get("tipos_complejos"));
    }
    return types;
  }

  /** Children must appear in the order of the XSD sequence of the node's type. */
  @SuppressWarnings("unchecked")
  private static void checkOrder(Node node, String type, Map<String, Object> types) {
    Map<String, Object> complex = (Map<String, Object>) types.get(type);
    assertThat(complex).as("type %s of %s", type, node.path()).isNotNull();
    List<Map<String, Object>> sequence = (List<Map<String, Object>>) complex.get("hijos");
    Set<String> repeatable = new HashSet<>();
    int position = 0;
    String previous = null;
    for (Node child : node.children()) {
      int found = -1;
      for (int j = position; j < sequence.size(); j++) {
        if (((String) sequence.get(j).get("nombre")).strip().equals(child.name())) {
          found = j;
          break;
        }
      }
      assertThat(found)
          .as("%s out of XSD order or unknown", child.path())
          .isGreaterThanOrEqualTo(0);
      Map<String, Object> element = sequence.get(found);
      if (child.name().equals(previous)) {
        assertThat(repeatable).as(child.path()).contains(child.name());
      }
      if (!"1".equals(element.get("max"))) {
        repeatable.add(child.name());
      }
      previous = child.name();
      position = found;
      Object childType = element.get("tipo");
      if (!child.isLeaf() && childType instanceof String && types.containsKey(childType)) {
        checkOrder(child, (String) childType, types);
      }
    }
  }
}
