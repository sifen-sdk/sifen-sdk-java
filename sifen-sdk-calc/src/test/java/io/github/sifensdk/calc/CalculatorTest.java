/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.calc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.sifensdk.core.model.ElectronicDocument;
import io.github.sifensdk.core.model.ItemVat;
import io.github.sifensdk.core.model.Totals;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class CalculatorTest {

  private final Calculator calculator = Calculator.defaults();

  private static BigDecimal n(String value) {
    return new BigDecimal(value);
  }

  private static ItemPrice.Builder gs(String quantity, String unitPrice) {
    return ItemPrice.builder().currencyCode("PYG").quantity(n(quantity)).unitPrice(n(unitPrice));
  }

  @Test
  void reproducesTheManualsTaxedItems() {
    ItemAmounts ten = calculator.item(gs("1", "110000").taxed(n("10")).build());
    ItemAmounts five = calculator.item(gs("2", "10500").taxed(n("5")).build());

    assertThat(ten.values().grossTotal()).isEqualByComparingTo("110000");
    assertThat(ten.values().discountsAndAdvances().total()).isEqualByComparingTo("110000");
    assertThat(ten.vat().taxableBase()).isEqualByComparingTo("100000");
    assertThat(ten.vat().amount()).isEqualByComparingTo("10000");
    assertThat(five.values().discountsAndAdvances().total()).isEqualByComparingTo("21000");
    assertThat(five.vat().taxableBase()).isEqualByComparingTo("20000");
    assertThat(five.vat().amount()).isEqualByComparingTo("1000");
  }

  @Test
  void aPartiallyTaxedItemSplitsTheTaxedAndTheExemptBase() {
    ItemVat vat =
        calculator.item(gs("1", "1000000").partiallyTaxed(n("10"), n("30")).build()).vat();

    // 100 × 1000000 × 30 / 10300 = 291262.14; × 10 % = 29126.2; 100 × 1000000 × 70 / 10300 =
    // 679611.65
    assertThat(vat.taxableBase()).isEqualByComparingTo("291262");
    assertThat(vat.amount()).isEqualByComparingTo("29126");
    assertThat(vat.exemptBase()).isEqualByComparingTo("679612");
    assertThat(vat.taxableProportionRate()).isEqualByComparingTo("30");
  }

  @Test
  void discountsAndTheGlobalRateAreSubtractedPerUnit() {
    ItemAmounts a =
        calculator.item(
            gs("3", "10000")
                .discountPerUnit(n("1000"))
                .globalDiscountRate(n("5"))
                .advancePerUnit(n("500"))
                .taxed(n("10"))
                .build());

    var d = a.values().discountsAndAdvances();
    assertThat(d.discountRate()).isEqualByComparingTo("10.00");
    assertThat(d.globalDiscountAmount()).isEqualByComparingTo("500");
    // (10000 − 1000 − 500 − 500) × 3
    assertThat(d.total()).isEqualByComparingTo("24000");
    assertThat(a.values().grossTotal()).isEqualByComparingTo("30000");
  }

  @Test
  void totalsAreSumsOfTheRoundedItemAmounts() {
    ItemAmounts ten = calculator.item(gs("1", "110000").taxed(n("10")).build());
    ItemAmounts five = calculator.item(gs("2", "10500").taxed(n("5")).build());
    ItemAmounts exempt = calculator.item(gs("1", "5000").exempt().build());
    ElectronicDocument document =
        Documents.document(
            "1",
            "PYG",
            null,
            null,
            List.of(
                Documents.item(1, n("1"), ten),
                Documents.item(2, n("2"), five),
                Documents.item(3, n("1"), exempt)),
            n("136000"));

    Totals t = calculator.totals(document);

    assertThat(t.vat10Subtotal()).isEqualByComparingTo("110000");
    assertThat(t.vat5Subtotal()).isEqualByComparingTo("21000");
    assertThat(t.exemptSubtotal()).isEqualByComparingTo("5000");
    assertThat(t.grossTotal()).isEqualByComparingTo("136000");
    assertThat(t.netTotal()).isEqualByComparingTo("136000");
    assertThat(t.vatTotal()).isEqualByComparingTo("11000");
    assertThat(t.vatTaxableBaseTotal()).isEqualByComparingTo("120000");
    assertThat(document.totals()).as("the document passed is not changed").isNull();
    assertThat(calculator.withTotals(document).totals()).isEqualTo(t);
  }

  @Test
  void roundsToFiftyOnlyWhenAsked() {
    ItemAmounts item = calculator.item(gs("1", "107437").exempt().build());
    ElectronicDocument document =
        Documents.document(
            "1", "PYG", null, null, List.of(Documents.item(1, n("1"), item)), n("107400"));

    Totals rounded = Calculator.builder().roundToFifty(true).build().totals(document);

    assertThat(rounded.roundingAmount()).isEqualByComparingTo("37");
    assertThat(rounded.netTotal()).isEqualByComparingTo("107400");
    assertThat(calculator.totals(document).roundingAmount()).isEqualByComparingTo("0");
  }

  @Test
  void decimalsAndRoundingFollowTheSettings() {
    Calculator usd =
        Calculator.builder().decimals("USD", 3).roundingMode(RoundingMode.HALF_EVEN).build();
    ItemAmounts a =
        usd.item(
            ItemPrice.builder()
                .currencyCode("USD")
                .quantity(n("3"))
                .unitPrice(n("10.0005"))
                .taxed(n("10"))
                .build());

    assertThat(a.values().grossTotal()).isEqualByComparingTo("30.002");
    assertThat(a.values().grossTotal().scale()).isEqualTo(3);
    assertThatThrownBy(() -> Calculator.builder().roundingMode(RoundingMode.DOWN))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("half a unit");
  }

  @Test
  void refusesWhatItCannotCompute() {
    assertThatThrownBy(() -> gs("1", "100").build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("VAT treatment");
    assertThatThrownBy(() -> gs("1", "100").discountPerUnit(n("10")).selfInvoice().build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("self-invoice");
    assertThatThrownBy(
            () -> calculator.item(gs("1", "100").discountPerUnit(n("101")).exempt().build()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("exceed the unit price");
    assertThatThrownBy(() -> gs("1", "100").taxed(n("7")).build())
        .isInstanceOf(IllegalArgumentException.class);
  }

  /**
   * Every arithmetic rule of the items and totals in the specification is either computed or left
   * out on purpose. A new rule (a new Nota Técnica) fails here until it is handled.
   */
  @Test
  void everyArithmeticRuleOfItemsAndTotalsIsHandled() throws IOException {
    Set<String> handled =
        Set.of(
            "R-E727-A1",
            "R-EA003-A1",
            "R-EA004-A2",
            "R-EA008-A1",
            "R-EA008-A2",
            "R-EA009-A1",
            "R-E735-A1",
            "R-E735-A4",
            "R-E735-A5",
            "R-E736-A1",
            "R-E736-A2",
            "R-E737-A1",
            "R-E737-A2",
            "R-F002-A2",
            "R-F003-A1",
            "R-F004-A2",
            "R-F005-A2",
            "R-F008-A1",
            "R-F008-A3",
            "R-F009-A2",
            "R-F033-A2",
            "R-F034-A2",
            "R-F035-A2",
            "R-F011-A2",
            "R-F012-A2",
            "R-F013-A1",
            "R-F014-A3",
            "R-F015-A1",
            "R-F016-A1",
            "R-F017-A1",
            "R-F018-A1",
            "R-F019-A1",
            "R-F020-A1",
            "R-F023-A1",
            "R-F023-A2");
    Map<String, String> leftOut =
        Map.ofEntries(
            Map.entry("R-EA004-A1", "replaced by R-EA004-A2 (NT 1)"),
            Map.entry("R-E735-A2", "in force until 2023 (NT 13)"),
            Map.entry("R-E735-A3", "in force until 2023 (NT 13)"),
            Map.entry("R-F002-A1", "in force until 2023 (NT 13)"),
            Map.entry("R-F004-A1", "in force until 2023 (NT 13)"),
            Map.entry("R-F005-A1", "in force until 2023 (NT 13)"),
            Map.entry("R-F008-A2", "excise tax (ISC), not defined (C-501)"),
            Map.entry("R-F009-A1", "replaced by R-F009-A2"),
            Map.entry("R-F033-A1", "replaced by R-F033-A2 (NT 1)"),
            Map.entry("R-F034-A1", "replaced by R-F034-A2 (NT 1)"),
            Map.entry("R-F035-A1", "replaced by R-F035-A2 (NT 1)"),
            Map.entry("R-F011-A1", "replaced by R-F011-A2 (NT 1)"),
            Map.entry("R-F012-A1", "replaced by R-F012-A2 (NT 1)"),
            Map.entry("R-F014-A1", "same formula as R-F014-A3"),
            Map.entry("R-F014-A2", "replaced by R-F014-A3 (NT 1)"),
            Map.entry("R-F036-A1", "rounding VAT: not verified by SIFEN (C-504, E-001)"),
            Map.entry("R-F037-A1", "rounding VAT: not verified by SIFEN (C-504, E-001)"),
            Map.entry("R-F026-A1", "commission: formula not written, not verified (E-001)"),
            Map.entry("R-F017-A2", "descartada in the specification (C-502)"),
            Map.entry("R-F023-A3", "removed by NT 8; F023 is prohibited in self-invoices"));

    Set<String> inSpec = new TreeSet<>();
    Path rules = Path.of("..", "spec", "reglas");
    Pattern id = Pattern.compile("\"id\"\\s*:\\s*\"(R-[A-Z0-9]+-A[0-9]+)\"");
    for (String file :
        new String[] {"de-e8-1-valores-item.json", "de-e8-2-iva-item.json", "de-f-totales.json"}) {
      Matcher m = id.matcher(Files.readString(rules.resolve(file)));
      while (m.find()) {
        inSpec.add(m.group(1));
      }
    }
    Set<String> known = new TreeSet<>(handled);
    known.addAll(leftOut.keySet());

    assertThat(inSpec).isNotEmpty();
    assertThat(Stream.of(handled, leftOut.keySet()).flatMap(Set::stream).distinct().count())
        .as("no rule is both computed and left out")
        .isEqualTo((long) handled.size() + leftOut.size());
    assertThat(known).isEqualTo(inSpec);
  }
}
