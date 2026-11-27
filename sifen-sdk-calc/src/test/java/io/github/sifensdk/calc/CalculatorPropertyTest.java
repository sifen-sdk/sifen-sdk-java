/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.calc;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.sifensdk.core.Environment;
import io.github.sifensdk.core.SifenDocuments;
import io.github.sifensdk.core.model.ElectronicDocument;
import io.github.sifensdk.core.model.Item;
import io.github.sifensdk.core.validation.ValidationIssue;
import io.github.sifensdk.core.validation.ValidationResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * A document completed by the calculator never has errors in the core's checks: random documents of
 * every supported type, currency, VAT treatment, discount and advance, and rounding setting.
 */
class CalculatorPropertyTest {

  private static final int DOCUMENTS = 3000;
  private static final SifenDocuments CORE =
      SifenDocuments.builder()
          .environment(Environment.TEST)
          .clock(Clock.fixed(Instant.parse("2026-11-27T13:00:00Z"), ZoneOffset.UTC))
          .build();

  @Test
  void documentsCompletedByTheCalculatorPassEveryCheckOfTheCore() {
    long seed = Long.getLong("calc.seed", 20261127L);
    Random random = new Random(seed);
    List<String> failures = new ArrayList<>();
    for (int n = 0; n < DOCUMENTS && failures.size() < 5; n++) {
      ElectronicDocument document = randomDocument(random);
      ValidationResult result = CORE.validate(document);
      if (!result.errors().isEmpty()) {
        failures.add(
            "document "
                + n
                + " (seed "
                + seed
                + "): "
                + result.errors().stream()
                    .map(ValidationIssue::toString)
                    .collect(Collectors.joining(" | ")));
      }
    }
    assertThat(failures).isEmpty();
  }

  private static ElectronicDocument randomDocument(Random random) {
    String type = pick(random, "1", "1", "4", "5", "6");
    boolean self = type.equals("4");
    String currency = self ? "PYG" : pick(random, "PYG", "PYG", "USD");
    String condition = currency.equals("PYG") ? null : pick(random, "1", "2");
    BigDecimal exchangeRate =
        currency.equals("PYG")
            ? null
            : new BigDecimal(6500 + random.nextInt(2000) + "." + random.nextInt(100));
    Calculator calculator =
        Calculator.builder()
            .roundingMode(
                pick(random, RoundingMode.HALF_UP, RoundingMode.HALF_EVEN, RoundingMode.HALF_DOWN))
            .decimals("USD", random.nextInt(5))
            .roundToFifty(random.nextBoolean())
            .build();
    BigDecimal globalRate =
        self || random.nextInt(3) > 0
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(random.nextInt(2000), 2);
    int count = 1 + random.nextInt(12);
    List<Item> items = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      BigDecimal unitPrice =
          currency.equals("PYG")
              ? BigDecimal.valueOf(1 + random.nextInt(3_000_000))
              : BigDecimal.valueOf(1 + random.nextInt(500_000), random.nextInt(3));
      BigDecimal quantity = BigDecimal.valueOf(1 + random.nextInt(5000), random.nextInt(4));
      ItemPrice.Builder price =
          ItemPrice.builder().currencyCode(currency).quantity(quantity).unitPrice(unitPrice);
      if (!self) {
        price.globalDiscountRate(globalRate);
        if (random.nextInt(3) == 0) {
          price.discountPerUnit(fraction(random, unitPrice, 30));
        }
        if (random.nextInt(5) == 0) {
          price.advancePerUnit(fraction(random, unitPrice, 20));
        }
        if (random.nextInt(8) == 0) {
          price.globalAdvancePerUnit(fraction(random, unitPrice, 10));
        }
        BigDecimal rate = BigDecimal.valueOf(random.nextBoolean() ? 10 : 5);
        switch (random.nextInt(6)) {
          case 0:
            price.exempt();
            break;
          case 1:
            price.exonerated();
            break;
          case 2:
            price.partiallyTaxed(rate, BigDecimal.valueOf(1 + random.nextInt(99)));
            break;
          default:
            price.taxed(rate);
        }
        if ("2".equals(condition)) {
          price.exchangeRate(exchangeRate);
        }
      } else {
        price.selfInvoice();
      }
      ItemAmounts amounts = calculator.item(price.build());
      items.add(Documents.item(i, quantity, amounts));
    }
    ElectronicDocument draft =
        Documents.document(type, currency, condition, exchangeRate, items, BigDecimal.ONE);
    ElectronicDocument done = calculator.withTotals(draft, globalRate);
    // The payment of an invoice is its total.
    return Documents.document(
            type,
            currency,
            condition,
            exchangeRate,
            items,
            done.totals().netTotal().signum() > 0 ? done.totals().netTotal() : BigDecimal.ONE)
        .toBuilder()
        .totals(done.totals())
        .build();
  }

  /** Up to the given percent of the price, with the price's scale. */
  private static BigDecimal fraction(Random random, BigDecimal price, int percent) {
    return price
        .multiply(BigDecimal.valueOf(random.nextInt(percent + 1)))
        .divide(BigDecimal.valueOf(100), price.scale(), RoundingMode.DOWN);
  }

  @SafeVarargs
  private static <T> T pick(Random random, T... values) {
    return values[random.nextInt(values.length)];
  }
}
