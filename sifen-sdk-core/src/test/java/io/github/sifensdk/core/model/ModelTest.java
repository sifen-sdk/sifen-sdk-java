/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ModelTest {

  private static Item item(String code, String price) {
    return Item.builder()
        .internalCode(code)
        .description("Secreto comercial")
        .unitOfMeasureCode("77")
        .quantity(BigDecimal.ONE)
        .valuation(
            ItemValues.builder()
                .unitPrice(new BigDecimal(price))
                .grossTotal(new BigDecimal(price))
                .discountsAndAdvances(
                    ItemDiscountsAndAdvances.builder().total(new BigDecimal(price)).build())
                .build())
        .build();
  }

  @Test
  void keepsAmountsExactlyAsGiven() {
    Item item = item("A", "110000.50");
    assertThat(item.valuation().unitPrice().toPlainString()).isEqualTo("110000.50");
    assertThat(item.valuation().unitPrice().scale()).isEqualTo(2);
  }

  @Test
  void toBuilderCopiesEverythingAndEqualityIncludesTheScale() {
    Item a = item("A", "100");
    assertThat(a.toBuilder().build()).isEqualTo(a).hasSameHashCodeAs(a);
    assertThat(item("A", "100.0")).isNotEqualTo(a);
    assertThat(a.toBuilder().internalCode("B").build()).isNotEqualTo(a);
  }

  @Test
  void listsAreCopiedAndUnmodifiable() {
    List<Item> items = new ArrayList<>();
    items.add(item("A", "1"));
    DocumentTypeData data = DocumentTypeData.builder().items(items).build();
    items.add(item("B", "2"));

    assertThat(data.items()).hasSize(1);
    assertThatThrownBy(() -> data.items().add(item("C", "3")))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThat(data.toBuilder().addItem(item("D", "4")).build().items()).hasSize(2);
    assertThat(data.items()).hasSize(1);
    assertThat(DocumentTypeData.builder().build().items()).isEmpty();
  }

  @Test
  void requiredElementsAreNamedWithoutValues() {
    assertThatThrownBy(() -> Stamp.builder().documentTypeCode("1").build())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("number (C004 dNumTim) is required");
  }

  @Test
  void toStringNeverShowsDocumentContents() {
    Item item = item("A", "110000");
    assertThat(item.toString()).isEqualTo("Item").doesNotContain("Secreto", "110000");
    DocumentTypeData data = DocumentTypeData.builder().addItem(item).addItem(item).build();
    assertThat(data.toString()).isEqualTo("DocumentTypeData[items=2]");
  }

  @Test
  void accessorsCarryTheSifenIdAndXmlName() throws NoSuchMethodException {
    SifenField total = Totals.class.getMethod("netTotal").getAnnotation(SifenField.class);
    assertThat(total.id()).isEqualTo("F014");
    assertThat(total.xml()).isEqualTo("dTotGralOpe");
    SifenField stamp = Stamp.class.getAnnotation(SifenField.class);
    assertThat(stamp.id()).isEqualTo("C001");
    assertThat(stamp.xml()).isEqualTo("gTimb");
  }
}
