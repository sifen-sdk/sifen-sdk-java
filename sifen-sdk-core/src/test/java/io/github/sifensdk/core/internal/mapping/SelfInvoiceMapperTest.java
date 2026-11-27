/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.sifensdk.core.internal.spec.Spec;
import io.github.sifensdk.core.internal.tree.Node;
import io.github.sifensdk.core.internal.xml.SchemaValidator;
import io.github.sifensdk.core.internal.xml.XmlWriter;
import io.github.sifensdk.core.model.SelfInvoice;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** The self-invoice group ({@code gCamAE}, E300) and the self-invoice and debit note samples. */
class SelfInvoiceMapperTest {

  private static final LocalDateTime SIGNED = LocalDateTime.of(2026, 11, 27, 15, 5, 0);

  private final SelfInvoiceMapper mapper = new SelfInvoiceMapper(new Descriptions(Spec.bundled()));

  private static SelfInvoice full() {
    return SelfInvoice.builder()
        .sellerNatureCode("2")
        .sellerIdDocumentTypeCode("2")
        .sellerIdDocumentNumber("X1234567")
        .sellerName("Vendedor Extranjero de Ejemplo")
        .sellerAddress("Calle del Vendedor")
        .sellerHouseNumber("15")
        .sellerDepartmentCode("1")
        .sellerDistrictCode("1")
        .sellerCityCode("1")
        .transactionAddress("Calle de la Transacción")
        .transactionDepartmentCode("1")
        .transactionDistrictCode("1")
        .transactionCityCode("1")
        .build();
  }

  @Test
  void writesEveryFieldInTheOrderOfTheXsd() {
    Node n = mapper.selfInvoice(full());
    assertThat(n.name()).isEqualTo("gCamAE");
    assertThat(n.fieldId()).isEqualTo("E300");
    // tgCamAE in DE_v150.xsd.
    assertThat(names(n))
        .containsExactly(
            "iNatVen",
            "dDesNatVen",
            "iTipIDVen",
            "dDTipIDVen",
            "dNumIDVen",
            "dNomVen",
            "dDirVen",
            "dNumCasVen",
            "cDepVen",
            "dDesDepVen",
            "cDisVen",
            "dDesDisVen",
            "cCiuVen",
            "dDesCiuVen",
            "dDirProv",
            "cDepProv",
            "dDesDepProv",
            "cDisProv",
            "dDesDisProv",
            "cCiuProv",
            "dDesCiuProv");
    Spec spec = Spec.bundled();
    assertThat(n.children())
        .allSatisfy(c ->
            assertThat(spec.field(c.fieldId()).orElseThrow().xml().strip()).isEqualTo(c.name()));
  }

  @Test
  void completesTheCatalogDescriptions() {
    Node n = mapper.selfInvoice(full());
    assertThat(text(n, "E302")).isEqualTo("Extranjero");
    assertThat(text(n, "E305")).isEqualTo("Pasaporte");
    assertThat(text(n, "E311")).isEqualTo("CAPITAL");
    assertThat(text(n, "E315")).isEqualTo("ASUNCION (DISTRITO)");
    assertThat(text(n, "E318")).isEqualTo("CAPITAL");
    assertThat(text(n, "E322")).isEqualTo("ASUNCION (DISTRITO)");
    assertThat(text(n, "E313")).isEqualTo(text(n, "E320"));
  }

  @Test
  void leavesOutTheOptionalDistricts() {
    Node n = mapper.selfInvoice(Samples.selfInvoice().documentTypeData().selfInvoice());
    assertThat(n.findAll("E312")).isEmpty();
    assertThat(n.findAll("E313")).isEmpty();
    assertThat(n.findAll("E319")).isEmpty();
    assertThat(n.findAll("E320")).isEmpty();
    assertThat(text(n, "E309")).isEqualTo("0");
  }

  @Test
  void theSampleSelfInvoiceIsMappedAndValidAgainstTheXsd() {
    Node root = DocumentMapper.toTree(Samples.selfInvoice(), SIGNED);
    assertThat(texts(root, "C002")).containsExactly("4");
    assertThat(texts(root, "C003")).containsExactly("Autofactura electrónica");
    Node typeData = root.findAll("E001").get(0);
    assertThat(names(typeData)).containsExactly("gCamAE", "gCamCond", "gCamItem");
    assertThat(root.findAll("E730")).isEmpty();
    for (String id : List.of("F002", "F003", "F004", "F005", "F015", "F016", "F017", "F023")) {
      assertThat(root.findAll(id)).as(id).isEmpty();
    }
    assertThat(texts(root, "EA008")).containsExactly("100000");
    assertThat(texts(root, "F008")).containsExactly("100000");
    assertThat(texts(root, "D206")).containsExactly("44444401");
    assertThat(texts(root, "H002")).containsExactly("3");
    assertThat(texts(root, "H014")).containsExactly("1");
    byte[] xml = XmlWriter.write(root);
    assertThat(SchemaValidator.validateDocument(xml)).isEmpty();
    assertThat(new String(xml, StandardCharsets.UTF_8)).contains("<dNomVen>Vendedor de Ejemplo");
  }

  @Test
  void theSampleDebitNoteIsMappedAndValidAgainstTheXsd() {
    Node root = DocumentMapper.toTree(Samples.debitNote(), SIGNED);
    assertThat(texts(root, "C002")).containsExactly("6");
    assertThat(texts(root, "C003")).containsExactly("Nota de débito electrónica");
    assertThat(texts(root, "E401")).containsExactly("6");
    assertThat(texts(root, "E402")).containsExactly("Recupero de costo");
    assertThat(texts(root, "H004")).containsExactly(Cdc.of(Samples.invoice()));
    assertThat(root.findAll("E300")).isEmpty();
    assertThat(root.findAll("E600")).isEmpty();
    assertThat(SchemaValidator.validateDocument(XmlWriter.write(root))).isEmpty();
  }

  @Test
  void toStringNeverShowsTheSeller() {
    assertThat(full().toString()).isEqualTo("SelfInvoice");
    assertThat(full()).isEqualTo(full().toBuilder().build()).hasSameHashCodeAs(full());
    assertThat(full()).isNotEqualTo(full().toBuilder().sellerHouseNumber("16").build());
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
}
