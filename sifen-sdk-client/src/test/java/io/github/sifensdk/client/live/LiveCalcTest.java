/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.live;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.github.sifensdk.calc.Calculator;
import io.github.sifensdk.calc.ItemPrice;
import io.github.sifensdk.client.ReceptionResult;
import io.github.sifensdk.client.SifenClient;
import io.github.sifensdk.core.Environment;
import io.github.sifensdk.core.SifenDocuments;
import io.github.sifensdk.core.model.CommercialOperation;
import io.github.sifensdk.core.model.ElectronicDocument;
import io.github.sifensdk.core.model.Item;
import io.github.sifensdk.core.model.OperationCondition;
import io.github.sifensdk.core.model.Payment;
import io.github.sifensdk.core.signing.Csc;
import io.github.sifensdk.core.signing.KeyMaterial;
import io.github.sifensdk.core.validation.ValidationIssue;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

/**
 * Sends to SIFEN's test environment invoices whose amounts the optional calculator computed: in
 * guaraníes and in dollars (one exchange rate or one per item), with discounts, a global discount,
 * advances, every VAT treatment, and the rounding to 50. Local only, with {@code
 * SIFEN_TEST_CALC=true}; see {@code docs/pruebas-contra-sifen.md}.
 */
class LiveCalcTest {

  private static final BigDecimal RATE = new BigDecimal("7315.50");

  private Properties p;
  private SecureRandom random;
  private long number;

  @Test
  void sifenApprovesDocumentsCompletedByTheCalculator() throws Exception {
    assumeTrue("true".equals(System.getenv("SIFEN_TEST_CALC")), "SIFEN_TEST_CALC is not true");
    p = new Properties();
    try (Reader r =
        Files.newBufferedReader(Path.of(env("SIFEN_TEST_ISSUER_FILE")), StandardCharsets.UTF_8)) {
      p.load(r);
    }
    KeyMaterial key = LiveKeys.key();
    Csc csc = Csc.of(env("SIFEN_TEST_CSC_ID"), env("SIFEN_TEST_CSC").toCharArray());
    SifenClient client =
        SifenClient.builder().environment(Environment.TEST).keyMaterial(key).build();
    SifenDocuments documents = SifenDocuments.builder().environment(Environment.TEST).build();
    random = new SecureRandom();
    number = (System.currentTimeMillis() / 1000L) % 8_000_000L + 1_000_000L;

    BigDecimal globalRate = new BigDecimal("7.5");
    send(
        client,
        documents,
        key,
        csc,
        "guaranies con descuentos y todas las afectaciones",
        invoice(
            Calculator.defaults(),
            "PYG",
            null,
            globalRate,
            c ->
                List.of(
                    price(c, "3", "12345").discountPerUnit(n("345")).taxed(n("10")),
                    price(c, "2.5", "8999").taxed(n("5")),
                    price(c, "1", "15000").exempt(),
                    price(c, "1", "17777").exonerated(),
                    price(c, "4", "250001")
                        .advancePerUnit(n("1000"))
                        .partiallyTaxed(n("10"), n("30")),
                    price(c, "7", "3333").partiallyTaxed(n("5"), n("50")))));
    send(
        client,
        documents,
        key,
        csc,
        "guaranies con redondeo a 50",
        invoice(
            Calculator.builder().roundToFifty(true).build(),
            "PYG",
            null,
            BigDecimal.ZERO,
            c -> List.of(price(c, "1", "107437").taxed(n("10")))));
    send(
        client,
        documents,
        key,
        csc,
        "dolares con tipo de cambio global",
        invoice(
            Calculator.defaults(),
            "USD",
            "1",
            BigDecimal.ZERO,
            c ->
                List.of(
                    price(c, "3", "19.99").discountPerUnit(n("1.33")).taxed(n("10")),
                    price(c, "1.5", "7.07").partiallyTaxed(n("5"), n("30")),
                    price(c, "2", "3.33").exempt())));
    send(
        client,
        documents,
        key,
        csc,
        "dolares con tipo de cambio por item",
        invoice(
            Calculator.defaults(),
            "USD",
            "2",
            BigDecimal.ZERO,
            c ->
                List.of(
                    price(c, "3", "19.99").exchangeRate(RATE).taxed(n("10")),
                    price(c, "2", "0.37").exchangeRate(RATE).taxed(n("5")))));
  }

  private static BigDecimal n(String value) {
    return new BigDecimal(value);
  }

  private static ItemPrice.Builder price(String currency, String quantity, String unitPrice) {
    return ItemPrice.builder().currencyCode(currency).quantity(n(quantity)).unitPrice(n(unitPrice));
  }

  private ElectronicDocument invoice(
      Calculator calculator,
      String currency,
      @Nullable String exchangeRateCondition,
      BigDecimal globalRate,
      Function<String, List<ItemPrice.Builder>> prices) {
    ElectronicDocument base = LiveSifenTest.invoice(p, number++, random);
    List<Item> items = new ArrayList<>();
    int i = 0;
    for (ItemPrice.Builder b : prices.apply(currency)) {
      ItemPrice price = b.globalDiscountRate(globalRate).build();
      var amounts = calculator.item(price);
      items.add(
          Item.builder()
              .internalCode("SDK-CALC-" + ++i)
              .description("Producto de prueba " + i)
              .unitOfMeasureCode("77")
              .quantity(price.quantity())
              .valuation(amounts.values())
              .vat(amounts.vat())
              .build());
    }
    CommercialOperation.Builder operation =
        base.generalData().commercialOperation().toBuilder().currencyCode(currency);
    if (exchangeRateCondition != null) {
      operation.exchangeRateConditionCode(exchangeRateCondition);
      if (exchangeRateCondition.equals("1")) {
        operation.exchangeRate(RATE);
      }
    }
    ElectronicDocument withItems =
        base.toBuilder()
            .generalData(
                base.generalData().toBuilder().commercialOperation(operation.build()).build())
            .documentTypeData(base.documentTypeData().toBuilder().items(items).build())
            .build();
    ElectronicDocument done = calculator.withTotals(withItems, globalRate);
    return done.toBuilder()
        .documentTypeData(
            done.documentTypeData().toBuilder()
                .operationCondition(
                    OperationCondition.builder()
                        .conditionCode("1")
                        .addPayment(
                            Payment.builder()
                                .typeCode("1")
                                .amount(done.totals().netTotal())
                                .currencyCode(currency)
                                .exchangeRate(currency.equals("PYG") ? null : RATE)
                                .build())
                        .build())
                .build())
        .build();
  }

  private static void send(
      SifenClient client,
      SifenDocuments documents,
      KeyMaterial key,
      Csc csc,
      String label,
      ElectronicDocument document) {
    List<ValidationIssue> errors = documents.validate(document).errors();
    if (!errors.isEmpty()) {
      System.out.println(
          "CALC "
              + label
              + ": NO VALIDA "
              + errors.stream()
                  .map(e -> e.ruleId() + " " + e.path())
                  .collect(Collectors.joining(" | ")));
      return;
    }
    ReceptionResult r = client.send(documents.sign(document, key, csc));
    System.out.println(
        "CALC "
            + label
            + ": "
            + r.document().status()
            + " "
            + r.document().messages().stream()
                .map(m -> m.code() + " " + m.message())
                .collect(Collectors.joining(" | ")));
  }

  private static String env(String name) {
    String value = System.getenv(name);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException("Missing environment variable " + name);
    }
    return value;
  }
}
