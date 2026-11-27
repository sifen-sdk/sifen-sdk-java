/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.live;

import io.github.sifensdk.client.ReceptionResult;
import io.github.sifensdk.client.ResultMessage;
import io.github.sifensdk.client.SifenClient;
import io.github.sifensdk.core.Environment;
import io.github.sifensdk.core.SifenDocuments;
import io.github.sifensdk.core.cdc.SecurityCode;
import io.github.sifensdk.core.model.CommercialOperation;
import io.github.sifensdk.core.model.DocumentTypeData;
import io.github.sifensdk.core.model.EconomicActivity;
import io.github.sifensdk.core.model.ElectronicDocument;
import io.github.sifensdk.core.model.GeneralData;
import io.github.sifensdk.core.model.Invoice;
import io.github.sifensdk.core.model.Issuer;
import io.github.sifensdk.core.model.Item;
import io.github.sifensdk.core.model.ItemDiscountsAndAdvances;
import io.github.sifensdk.core.model.ItemValues;
import io.github.sifensdk.core.model.ItemVat;
import io.github.sifensdk.core.model.Operation;
import io.github.sifensdk.core.model.OperationCondition;
import io.github.sifensdk.core.model.Payment;
import io.github.sifensdk.core.model.Receiver;
import io.github.sifensdk.core.model.Stamp;
import io.github.sifensdk.core.model.Totals;
import io.github.sifensdk.core.signing.Csc;
import io.github.sifensdk.core.signing.KeyMaterial;
import io.github.sifensdk.core.signing.KeyMaterials;
import io.github.sifensdk.core.signing.SignedDocument;
import io.github.sifensdk.core.validation.ValidationIssue;
import io.github.sifensdk.core.validation.ValidationResult;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Sends to SIFEN's test environment one invoice per arithmetic rule, each with a single amount off
 * by a known delta, to learn which formulas SIFEN actually enforces.
 *
 * <p>Runs only with {@code SIFEN_TEST_PROBE=true} and the credentials of {@code LiveSifenTest}. The
 * amounts of each probe are computed here, in the test, only to build the documents: the SDK itself
 * never computes them. Prints only case names, rule ids, states and SIFEN's codes.
 */
@EnabledIfEnvironmentVariable(named = "SIFEN_TEST_PROBE", matches = "true")
class LiveArithmeticProbeTest {

  private static final BigDecimal ZERO = BigDecimal.ZERO;
  private static final BigDecimal HUNDRED = new BigDecimal("100");

  /** One item line: what an issuer enters. Amounts are derived from it. */
  static final class Line {
    final String treatment; // E731: 1 gravado, 2 exonerado, 3 exento, 4 gravado parcial
    final int rate; // E734
    final BigDecimal proportion; // E733
    final BigDecimal qty;
    final BigDecimal price;
    BigDecimal discount = ZERO;
    BigDecimal globalDiscount = ZERO;
    BigDecimal advance = ZERO;
    BigDecimal globalAdvance = ZERO;
    BigDecimal itemExchangeRate;

    Line(String treatment, int rate, String proportion, String qty, String price) {
      this.treatment = treatment;
      this.rate = rate;
      this.proportion = new BigDecimal(proportion);
      this.qty = new BigDecimal(qty);
      this.price = new BigDecimal(price);
    }

    Line discount(String d) {
      discount = new BigDecimal(d);
      return this;
    }

    Line globalDiscount(String d) {
      globalDiscount = new BigDecimal(d);
      return this;
    }
  }

  /** The amounts of a document, as editable maps keyed by the builder method names. */
  static final class Amounts {
    final List<Map<String, BigDecimal>> items = new ArrayList<>();
    final Map<String, BigDecimal> totals = new LinkedHashMap<>();
    String currency = "PYG";
    BigDecimal exchangeRate;
    String exchangeCondition;
    int scale = 0;
    String documentType = "1";
    String associatedCdc;
  }

  record Case(
      String name,
      String rules,
      List<Line> lines,
      Consumer<Amounts> setup,
      Consumer<Amounts> mutation) {}

  @Test
  void probesTheArithmeticRules() throws Exception {
    Properties p = issuer();
    KeyMaterial key =
        KeyMaterials.pkcs12(
            Path.of(env("SIFEN_TEST_P12_PATH")), env("SIFEN_TEST_P12_PASSWORD").toCharArray());
    Csc csc = Csc.of(env("SIFEN_TEST_CSC_ID"), env("SIFEN_TEST_CSC").toCharArray());
    SifenClient client =
        SifenClient.builder().environment(Environment.TEST).keyMaterial(key).build();
    SecureRandom random = new SecureRandom();
    long number = (System.currentTimeMillis() / 1000L) % 9_000_000L + 500_000L;

    List<Case> cases = cases();
    String only = System.getenv("SIFEN_TEST_PROBE_ONLY");
    System.out.println("PROBE caso | reglas objetivo | el SDK marca | SIFEN");
    for (Case c : cases) {
      if (only != null && !only.isBlank() && !c.name().matches(only)) {
        continue;
      }
      Amounts a = compute(c.lines(), c.setup());
      if (c.mutation() != null) {
        c.mutation().accept(a);
      }
      ElectronicDocument doc = build(p, a, c.lines(), String.format("%07d", number++), random);
      ValidationResult v =
          SifenDocuments.builder().environment(Environment.TEST).build().validate(doc);
      Set<String> flagged =
          v.issues().stream()
              .map(ValidationIssue::ruleId)
              .collect(Collectors.toCollection(TreeSet::new));
      String sdk = flagged.isEmpty() ? "-" : String.join(",", flagged);
      String outcome;
      try {
        SignedDocument signed =
            SifenDocuments.builder()
                .environment(Environment.TEST)
                .warnings(flagged)
                .build()
                .sign(doc, key, csc);
        ReceptionResult r = client.send(signed);
        outcome =
            r.document().status()
                + " "
                + r.document().messages().stream()
                    .map(ResultMessage::code)
                    .collect(Collectors.joining(","))
                + " "
                + r.document().messages().stream()
                    .map(ResultMessage::message)
                    .collect(Collectors.joining(" | "));
      } catch (RuntimeException e) {
        outcome = "NO ENVIADO: " + e.getClass().getSimpleName() + " " + firstLine(e.getMessage());
      }
      System.out.println("PROBE " + c.name() + " | " + c.rules() + " | " + sdk + " | " + outcome);
      Thread.sleep(1500);
    }
  }

  @Test
  void probesTheCreditNoteLimit() throws Exception {
    String only = System.getenv("SIFEN_TEST_PROBE_ONLY");
    if (only == null || !"nc".matches(only)) {
      return;
    }
    Properties p = issuer();
    KeyMaterial key =
        KeyMaterials.pkcs12(
            Path.of(env("SIFEN_TEST_P12_PATH")), env("SIFEN_TEST_P12_PASSWORD").toCharArray());
    Csc csc = Csc.of(env("SIFEN_TEST_CSC_ID"), env("SIFEN_TEST_CSC").toCharArray());
    SifenClient client =
        SifenClient.builder().environment(Environment.TEST).keyMaterial(key).build();
    SecureRandom random = new SecureRandom();
    long number = (System.currentTimeMillis() / 1000L) % 9_000_000L + 500_000L;
    List<Line> lines = List.of(new Line("1", 10, "100", "1", "11000"));
    List<Line> bigger = List.of(new Line("1", 10, "100", "1", "11001"));
    List<Line> half = List.of(new Line("1", 10, "100", "1", "5500"));

    String[] invoice = send(p, key, csc, client, random, compute(lines, null), lines, number++);
    System.out.println("PROBE nc-factura-origen | - | " + invoice[0]);
    String cdc = invoice[1];
    Consumer<Amounts> nc =
        a -> {
          a.documentType = "5";
          a.associatedCdc = cdc;
        };
    String[] r1 = send(p, key, csc, client, random, compute(half, nc), half, number++);
    System.out.println("PROBE nc-mitad | R-H004-A1 | " + r1[0]);
    Thread.sleep(1500);
    String[] r2 = send(p, key, csc, client, random, compute(half, nc), half, number++);
    System.out.println("PROBE nc-otra-mitad (suma = factura) | R-H004-A1 | " + r2[0]);
    Thread.sleep(1500);
    String[] r3 = send(p, key, csc, client, random, compute(half, nc), half, number++);
    System.out.println("PROBE nc-tercera-mitad (suma > factura) | R-H004-A1 | " + r3[0]);
    Thread.sleep(1500);
    String[] other = send(p, key, csc, client, random, compute(lines, null), lines, number++);
    String cdc2 = other[1];
    Consumer<Amounts> nc2 =
        a -> {
          a.documentType = "5";
          a.associatedCdc = cdc2;
        };
    String[] r4 = send(p, key, csc, client, random, compute(bigger, nc2), bigger, number++);
    System.out.println("PROBE nc-mayor-que-factura+1 | R-H004-A1 | " + r4[0]);
  }

  private static String[] send(
      Properties p,
      KeyMaterial key,
      Csc csc,
      SifenClient client,
      SecureRandom random,
      Amounts a,
      List<Line> lines,
      long number) {
    ElectronicDocument doc = build(p, a, lines, String.format("%07d", number), random);
    ValidationResult v =
        SifenDocuments.builder().environment(Environment.TEST).build().validate(doc);
    Set<String> flagged =
        v.issues().stream()
            .map(ValidationIssue::ruleId)
            .collect(Collectors.toCollection(TreeSet::new));
    try {
      SignedDocument signed =
          SifenDocuments.builder()
              .environment(Environment.TEST)
              .warnings(flagged)
              .build()
              .sign(doc, key, csc);
      ReceptionResult r = client.send(signed);
      return new String[] {
        "SDK "
            + (flagged.isEmpty() ? "-" : String.join(",", flagged))
            + " | "
            + r.document().status()
            + " "
            + r.document().messages().stream()
                .map(m -> m.code() + " " + m.message())
                .collect(Collectors.joining(" | ")),
        signed.cdc()
      };
    } catch (RuntimeException e) {
      return new String[] {
        "NO ENVIADO: " + e.getClass().getSimpleName() + " " + firstLine(e.getMessage()), ""
      };
    }
  }

  // ---------------------------------------------------------------- cases

  private static List<Case> cases() {
    List<Case> out = new ArrayList<>();
    // Prices chosen so that the per-item VAT (Manual) and the VAT on the totals differ.
    java.util.function.Supplier<List<Line>> mixed =
        () ->
            List.of(
                new Line("1", 10, "100", "1", "10004"),
                new Line("1", 10, "100", "1", "20003"),
                new Line("1", 10, "100", "1", "5010"),
                new Line("1", 5, "100", "1", "10001"),
                new Line("1", 5, "100", "1", "20001"),
                new Line("3", 0, "0", "1", "5000"),
                new Line("2", 0, "0", "1", "7000"));
    java.util.function.Supplier<List<Line>> simple =
        () ->
            List.of(new Line("1", 10, "100", "1", "10001"), new Line("1", 5, "100", "1", "10501"));

    out.add(new Case("base-manual", "ninguna", mixed.get(), null, null));
    out.add(
        new Case(
            "vertical-iva-sobre-totales",
            "F015,F016,F017,F018,F019,F020",
            mixed.get(),
            null,
            LiveArithmeticProbeTest::vertical));
    out.add(
        new Case(
            "vertical-solo-iva",
            "F015,F016,F017",
            mixed.get(),
            null,
            a -> {
              BigDecimal i5 =
                  round(
                      a.totals
                          .get("vat5Subtotal")
                          .multiply(new BigDecimal("5"))
                          .divide(new BigDecimal("105"), 10, RoundingMode.HALF_UP),
                      0);
              BigDecimal i10 =
                  round(
                      a.totals
                          .get("vat10Subtotal")
                          .divide(new BigDecimal("11"), 10, RoundingMode.HALF_UP),
                      0);
              a.totals.put("vat5Amount", i5);
              a.totals.put("vat10Amount", i10);
              a.totals.put("vatTotal", i5.add(i10));
            }));

    for (String d : List.of("1", "100")) {
      BigDecimal delta = new BigDecimal(d);
      String s = "+" + d;
      // Item level (first item, 10 %; the exempt item is index 5 of "mixed").
      out.add(item("E727-total-bruto-item" + s, "R-E727-A1", mixed, 0, "grossTotal", delta));
      out.add(item("EA008-total-item" + s, "R-EA008-A1", mixed, 0, "total", delta));
      out.add(item("E735-base-10" + s, "R-E735-A3/A5", mixed, 0, "taxableBase", delta));
      out.add(item("E735-base-5" + s, "R-E735-A2/A4", mixed, 3, "taxableBase", delta));
      out.add(item("E736-iva-item" + s, "R-E736-A2", mixed, 0, "amount", delta));
      out.add(item("E737-base-exenta-gravado" + s, "R-E737-A2", mixed, 0, "exemptBase", delta));
      out.add(item("E735-base-exento" + s, "R-E735-A1", mixed, 5, "taxableBase", delta));
      out.add(item("E736-iva-exento" + s, "R-E736-A1", mixed, 5, "amount", delta));
      // Totals.
      for (String[] t :
          new String[][] {
            {"F002-subtotal-exento", "R-F002-A2", "exemptSubtotal"},
            {"F003-subtotal-exonerado", "R-F003-A1", "exoneratedSubtotal"},
            {"F004-subtotal-5", "R-F004-A2", "vat5Subtotal"},
            {"F005-subtotal-10", "R-F005-A2", "vat10Subtotal"},
            {"F008-total-bruto", "R-F008-A1", "grossTotal"},
            {"F014-total-general", "R-F014-A3", "netTotal"},
            {"F015-iva-5", "R-F015-A1", "vat5Amount"},
            {"F016-iva-10", "R-F016-A1", "vat10Amount"},
            {"F017-iva-total", "R-F017-A1/A2", "vatTotal"},
            {"F018-base-5", "R-F018-A1", "vat5TaxableBaseTotal"},
            {"F019-base-10", "R-F019-A1", "vat10TaxableBaseTotal"},
            {"F020-base-total", "R-F020-A1", "vatTaxableBaseTotal"},
            {"F009-total-desc-item", "R-F009-A2", "itemDiscountTotal"},
            {"F033-total-desc-global", "R-F033-A2", "itemGlobalDiscountTotal"},
            {"F011-total-descuentos", "R-F011-A2", "discountTotal"},
            {"F034-total-anticipo-item", "R-F034-A2", "itemAdvanceTotal"},
            {"F035-total-anticipo-global", "R-F035-A2", "itemGlobalAdvanceTotal"},
            {"F012-total-anticipos", "R-F012-A2", "advanceTotal"},
          }) {
        out.add(total(t[0] + s, t[1], mixed, t[2], delta));
      }
      // Discounts.
      java.util.function.Supplier<List<Line>> disc =
          () ->
              List.of(
                  new Line("1", 10, "100", "2", "10000").discount("1000"),
                  new Line("1", 5, "100", "1", "10500").discount("500"));
      out.add(item("EA003-porcentaje-desc" + s, "R-EA003-A1", disc, 0, "discountRate", delta));
      out.add(item("EA008-total-item-con-desc" + s, "R-EA008-A1", disc, 0, "total", delta));
      out.add(
          total(
              "F009-total-desc-item-con-desc" + s, "R-F009-A2", disc, "itemDiscountTotal", delta));
      out.add(
          total("F011-total-descuentos-con-desc" + s, "R-F011-A2", disc, "discountTotal", delta));
      // Global discount by percentage (EA004 from F010).
      java.util.function.Supplier<List<Line>> global =
          () ->
              List.of(
                  new Line("1", 10, "100", "1", "10000").globalDiscount("1000"),
                  new Line("1", 5, "100", "1", "10500").globalDiscount("1050"));
      Consumer<Amounts> tenPercent = a -> a.totals.put("globalDiscountRate", BigDecimal.TEN);
      out.add(new Case("base-desc-global", "ninguna", global.get(), tenPercent, null));
      out.add(
          new Case(
              "EA004-desc-global-item" + s,
              "R-EA004-A1/A2",
              global.get(),
              tenPercent,
              a -> bumpItem(a, 0, "globalDiscountAmount", delta)));
      out.add(
          new Case(
              "F033-total-desc-global-con-desc" + s,
              "R-F033-A2",
              global.get(),
              tenPercent,
              a -> bump(a, "itemGlobalDiscountTotal", delta)));
      // Partially taxed (NT 13 formula).
      java.util.function.Supplier<List<Line>> partial =
          () ->
              List.of(new Line("4", 10, "30", "1", "10001"), new Line("4", 5, "50", "1", "10501"));
      out.add(new Case("base-gravado-parcial", "ninguna", partial.get(), null, null));
      out.add(item("E735-base-parcial-10" + s, "R-E735-A5", partial, 0, "taxableBase", delta));
      out.add(item("E737-base-exenta-parcial" + s, "R-E737-A1", partial, 0, "exemptBase", delta));
      out.add(
          total("F002-subtotal-exento-parcial" + s, "R-F002-A2", partial, "exemptSubtotal", delta));
      out.add(total("F005-subtotal-10-parcial" + s, "R-F005-A2", partial, "vat10Subtotal", delta));
      // Foreign currency, global exchange rate.
      Consumer<Amounts> usdGlobal =
          a -> {
            a.currency = "USD";
            a.exchangeCondition = "1";
            a.exchangeRate = new BigDecimal("7300");
            a.scale = 2;
          };
      java.util.function.Supplier<List<Line>> usd =
          () ->
              List.of(
                  new Line("1", 10, "100", "1", "100.01"), new Line("1", 5, "100", "3", "33.37"));
      out.add(new Case("base-usd-global", "ninguna", usd.get(), usdGlobal, null));
      out.add(
          new Case(
              "F023-total-gs-global" + s,
              "R-F023-A1",
              usd.get(),
              usdGlobal,
              a -> bump(a, "guaraniTotal", delta)));
      out.add(
          new Case(
              "F016-iva-10-usd+0.01".replace("+0.01", s.equals("+1") ? "+0.01" : "+1.00"),
              "R-F016-A1",
              usd.get(),
              usdGlobal,
              a ->
                  bump(
                      a, "vat10Amount", s.equals("+1") ? new BigDecimal("0.01") : BigDecimal.ONE)));
      // Rounding to multiples of 50 (F013) and its VAT.
      out.add(
          new Case(
              "F013-redondeo-no-multiplo" + s,
              "R-F013-A1",
              simple.get(),
              null,
              a -> {
                a.totals.put("roundingAmount", delta);
                a.totals.put("netTotal", a.totals.get("grossTotal").subtract(delta));
              }));
    }
    // Rounding coherent (F008 = 20502 → 50 Gs. multiple: 20500, rounding 2).
    out.add(
        new Case(
            "F013-redondeo-correcto",
            "ninguna",
            simple.get(),
            null,
            a -> {
              BigDecimal gross = a.totals.get("grossTotal");
              BigDecimal rounded =
                  gross
                      .divide(new BigDecimal("50"), 0, RoundingMode.DOWN)
                      .multiply(new BigDecimal("50"));
              BigDecimal r = gross.subtract(rounded);
              a.totals.put("roundingAmount", r);
              a.totals.put("netTotal", rounded);
            }));
    // Tolerance in a currency with decimals.
    Consumer<Amounts> usdGlobal =
        a -> {
          a.currency = "USD";
          a.exchangeCondition = "1";
          a.exchangeRate = new BigDecimal("7300");
          a.scale = 2;
        };
    java.util.function.Supplier<List<Line>> usd =
        () ->
            List.of(new Line("1", 10, "100", "1", "100.01"), new Line("1", 5, "100", "3", "33.37"));
    for (String x : List.of("0.02", "0.05", "0.10", "0.49", "0.50", "0.51", "0.99")) {
      out.add(
          new Case(
              "tolerancia-usd-F016+" + x,
              "R-F016-A1",
              usd.get(),
              usdGlobal,
              a -> bump(a, "vat10Amount", new BigDecimal(x))));
    }
    for (String x : List.of("0.01", "0.50", "0.51")) {
      out.add(
          new Case(
              "tolerancia-usd-E735-base+" + x,
              "R-E735-A3",
              usd.get(),
              usdGlobal,
              a -> {
                bumpItem(a, 0, "taxableBase", new BigDecimal(x));
                resum(a);
              }));
      out.add(
          new Case(
              "tolerancia-usd-F023+" + x,
              "R-F023-A1",
              usd.get(),
              usdGlobal,
              a -> bump(a, "guaraniTotal", new BigDecimal(x).multiply(HUNDRED))));
    }
    // The other rounding of an item (what an issuer gets when it spreads a difference), with the
    // totals as sums of the items: base 10004 / 1.1 = 9094.545..., VAT 9095 * 0.1 = 909.5.
    out.add(
        new Case(
            "otro-redondeo-base-item",
            "R-E735-A3",
            mixed.get(),
            null,
            a -> {
              bumpItem(a, 0, "taxableBase", BigDecimal.ONE.negate());
              Map<String, BigDecimal> m = a.items.get(0);
              m.put("amount", round(m.get("taxableBase").divide(BigDecimal.TEN), 0));
              resum(a);
            }));
    out.add(
        new Case(
            "otro-redondeo-iva-item-909.5",
            "R-E736-A2",
            mixed.get(),
            null,
            a -> {
              bumpItem(a, 0, "amount", BigDecimal.ONE.negate());
              resum(a);
            }));
    // Per-item exchange rate (D017 = 2): EA009 and F023 as the sum of EA009.
    Consumer<Amounts> usdItem =
        a -> {
          a.currency = "USD";
          a.exchangeCondition = "2";
          a.exchangeRate = new BigDecimal("7300");
          a.scale = 2;
        };
    out.add(new Case("base-usd-por-item", "ninguna", usd.get(), usdItem, null));
    out.add(
        new Case(
            "EA009-total-gs-item+1",
            "R-EA009-A1",
            usd.get(),
            usdItem,
            a -> bumpItem(a, 0, "guaraniTotal", BigDecimal.ONE)));
    out.add(
        new Case(
            "F023-total-gs-por-item+1",
            "R-F023-A2",
            usd.get(),
            usdItem,
            a -> bump(a, "guaraniTotal", BigDecimal.ONE)));
    // VAT of the rounding (F037) and how F017 treats it: 10049 rounds to 10000, 49 / 11 = 4.45.
    java.util.function.Supplier<List<Line>> roundable =
        () -> List.of(new Line("1", 10, "100", "1", "10049"));
    Consumer<Amounts> round49 =
        a -> {
          a.totals.put("roundingAmount", new BigDecimal("49"));
          a.totals.put("netTotal", a.totals.get("grossTotal").subtract(new BigDecimal("49")));
        };
    out.add(new Case("redondeo-sin-iva-del-redondeo", "F013,F017", roundable.get(), null, round49));
    out.add(
        new Case(
            "redondeo-F037-y-F017-resta (tabla)",
            "R-F017-A1,R-F037-A1",
            roundable.get(),
            null,
            round49.andThen(a -> {
              a.totals.put("vat10RoundingAmount", new BigDecimal("4"));
              bump(a, "vatTotal", new BigDecimal("-4"));
            })));
    out.add(
        new Case(
            "redondeo-F037-y-F017-sin-restar (2371)",
            "R-F017-A2,R-F037-A1",
            roundable.get(),
            null,
            round49.andThen(a -> a.totals.put("vat10RoundingAmount", new BigDecimal("4")))));
    out.add(
        new Case(
            "redondeo-F037+1",
            "R-F037-A1",
            roundable.get(),
            null,
            round49.andThen(a -> a.totals.put("vat10RoundingAmount", new BigDecimal("5")))));
    out.add(
        new Case(
            "redondeo-F037+1-coherente",
            "R-F037-A1",
            roundable.get(),
            null,
            round49.andThen(a -> {
              a.totals.put("vat10RoundingAmount", new BigDecimal("5"));
              bump(a, "vatTotal", new BigDecimal("-5"));
            })));
    out.add(
        new Case(
            "redondeo-F037+20-coherente",
            "R-F037-A1",
            roundable.get(),
            null,
            round49.andThen(a -> {
              a.totals.put("vat10RoundingAmount", new BigDecimal("24"));
              bump(a, "vatTotal", new BigDecimal("-24"));
            })));
    // Commission (F025) and its VAT (F026).
    Consumer<Amounts> commission =
        a -> {
          a.totals.put("commissionAmount", new BigDecimal("1000"));
          a.totals.put("commissionVatAmount", new BigDecimal("100"));
          a.totals.put("netTotal", a.totals.get("grossTotal").add(new BigDecimal("1000")));
        };
    out.add(
        new Case(
            "comision-F017-sin-sumar (2371)",
            "R-F017-A2,R-F026-A1",
            simple.get(),
            null,
            commission));
    out.add(
        new Case(
            "comision-F017-suma (tabla)",
            "R-F017-A1,R-F026-A1",
            simple.get(),
            null,
            commission.andThen(a -> bump(a, "vatTotal", new BigDecimal("100")))));
    out.add(
        new Case(
            "comision-F026+1",
            "R-F026-A1",
            simple.get(),
            null,
            commission.andThen(a -> bump(a, "commissionVatAmount", BigDecimal.ONE))));
    out.add(
        new Case(
            "comision-F026+1-coherente",
            "R-F026-A1",
            simple.get(),
            null,
            commission.andThen(a -> {
              bump(a, "commissionVatAmount", BigDecimal.ONE);
              bump(a, "vatTotal", new BigDecimal("101"));
            })));
    // Tolerance of a percentage (EA003): 1000 over 10000 is exactly 10 %.
    for (String x : List.of("0.01", "0.49", "0.50", "0.51", "0.80", "0.81")) {
      out.add(
          new Case(
              "tolerancia-EA003+" + x,
              "R-EA003-A1",
              List.of(new Line("1", 10, "100", "1", "10000").discount("1000")),
              null,
              a -> bumpItem(a, 0, "discountRate", new BigDecimal(x))));
    }
    // The documented variation of 0.8 of EA004, everything else consistent (USD, 2 decimals):
    // F010 = 10 %, price 100.00, so the exact EA004 is 10.00.
    for (String x : List.of("0.00", "0.50", "0.80", "0.81", "1.00")) {
      Consumer<Amounts> usdTen =
          a -> {
            a.currency = "USD";
            a.exchangeCondition = "1";
            a.exchangeRate = new BigDecimal("7300");
            a.scale = 2;
            a.totals.put("globalDiscountRate", BigDecimal.TEN);
          };
      out.add(
          new Case(
              "holgura-EA004+" + x,
              "R-EA004-A1/A2",
              List.of(
                  new Line("1", 10, "100", "1", "100.00")
                      .globalDiscount(
                          new BigDecimal("10.00").add(new BigDecimal(x)).toPlainString())),
              usdTen,
              null));
    }
    // A base case is added in each delta loop: keep the first.
    Map<String, Case> unique = new LinkedHashMap<>();
    for (Case c : out) {
      unique.putIfAbsent(c.name(), c);
    }
    return new ArrayList<>(unique.values());
  }

  private static Case item(
      String name,
      String rules,
      java.util.function.Supplier<List<Line>> lines,
      int index,
      String key,
      BigDecimal delta) {
    return new Case(name, rules, lines.get(), null, a -> bumpItem(a, index, key, delta));
  }

  private static Case total(
      String name,
      String rules,
      java.util.function.Supplier<List<Line>> lines,
      String key,
      BigDecimal delta) {
    return new Case(name, rules, lines.get(), null, a -> bump(a, key, delta));
  }

  private static void bumpItem(Amounts a, int index, String key, BigDecimal delta) {
    Map<String, BigDecimal> item = a.items.get(index);
    item.put(key, item.getOrDefault(key, ZERO).add(delta));
  }

  private static void bump(Amounts a, String key, BigDecimal delta) {
    a.totals.put(key, a.totals.getOrDefault(key, ZERO).add(delta));
  }

  /** Recomputes the VAT totals as sums of the (possibly edited) item values. */
  private static void resum(Amounts a) {
    BigDecimal i5 = ZERO, i10 = ZERO, b5 = ZERO, b10 = ZERO;
    for (int i = 0; i < a.items.size(); i++) {
      Map<String, BigDecimal> m = a.items.get(i);
      if (m.get("taxableBase").signum() == 0) {
        continue;
      }
      // Items at 5 % are recognisable by their VAT being about 1/20 of the base.
      boolean five =
          m.get("amount").multiply(new BigDecimal("15")).compareTo(m.get("taxableBase")) < 0;
      if (five) {
        i5 = i5.add(m.get("amount"));
        b5 = b5.add(m.get("taxableBase"));
      } else {
        i10 = i10.add(m.get("amount"));
        b10 = b10.add(m.get("taxableBase"));
      }
    }
    a.totals.put("vat5Amount", i5);
    a.totals.put("vat10Amount", i10);
    a.totals.put("vatTotal", i5.add(i10));
    a.totals.put("vat5TaxableBaseTotal", b5);
    a.totals.put("vat10TaxableBaseTotal", b10);
    a.totals.put("vatTaxableBaseTotal", b5.add(b10));
  }

  /** Totals computed on the subtotals ("vertical"), keeping the per-item values. */
  private static void vertical(Amounts a) {
    BigDecimal s5 = a.totals.get("vat5Subtotal");
    BigDecimal s10 = a.totals.get("vat10Subtotal");
    BigDecimal i5 =
        round(
            s5.multiply(new BigDecimal("5"))
                .divide(new BigDecimal("105"), 10, RoundingMode.HALF_UP),
            a.scale);
    BigDecimal i10 = round(s10.divide(new BigDecimal("11"), 10, RoundingMode.HALF_UP), a.scale);
    a.totals.put("vat5Amount", i5);
    a.totals.put("vat10Amount", i10);
    a.totals.put("vatTotal", i5.add(i10));
    a.totals.put("vat5TaxableBaseTotal", s5.subtract(i5));
    a.totals.put("vat10TaxableBaseTotal", s10.subtract(i10));
    a.totals.put("vatTaxableBaseTotal", s5.subtract(i5).add(s10).subtract(i10));
  }

  // ---------------------------------------------------------- amounts

  /** The amounts as the Manual describes them: per item first, totals as sums of the items. */
  private static Amounts compute(List<Line> lines, Consumer<Amounts> setup) {
    Amounts a = new Amounts();
    if (setup != null) {
      setup.accept(a);
    }
    int sc = a.scale;
    BigDecimal ex = ZERO, exo = ZERO, s5 = ZERO, s10 = ZERO, i5 = ZERO, i10 = ZERO;
    BigDecimal b5 = ZERO, b10 = ZERO, d = ZERO, gd = ZERO, ad = ZERO, gad = ZERO;
    for (Line l : lines) {
      Map<String, BigDecimal> m = new LinkedHashMap<>();
      BigDecimal gross = round(l.price.multiply(l.qty), sc);
      BigDecimal total =
          round(
              l.price
                  .subtract(l.discount)
                  .subtract(l.globalDiscount)
                  .subtract(l.advance)
                  .subtract(l.globalAdvance)
                  .multiply(l.qty),
              sc);
      m.put("unitPrice", l.price);
      m.put("grossTotal", gross);
      m.put("discountAmount", l.discount);
      if (l.discount.signum() > 0) {
        m.put(
            "discountRate",
            round(l.discount.multiply(HUNDRED).divide(l.price, 10, RoundingMode.HALF_UP), 2));
      }
      m.put("globalDiscountAmount", l.globalDiscount);
      m.put("advanceAmount", l.advance);
      m.put("globalAdvanceAmount", l.globalAdvance);
      m.put("total", total);
      BigDecimal base = ZERO, vat = ZERO, exempt = ZERO;
      if (l.treatment.equals("1") || l.treatment.equals("4")) {
        BigDecimal r = BigDecimal.valueOf(l.rate);
        BigDecimal den = new BigDecimal("10000").add(r.multiply(l.proportion));
        base =
            round(
                HUNDRED
                    .multiply(total)
                    .multiply(l.proportion)
                    .divide(den, 10, RoundingMode.HALF_UP),
                sc);
        vat = round(base.multiply(r).divide(HUNDRED, 10, RoundingMode.HALF_UP), sc);
        if (l.treatment.equals("4")) {
          exempt =
              round(
                  HUNDRED
                      .multiply(total)
                      .multiply(HUNDRED.subtract(l.proportion))
                      .divide(den, 10, RoundingMode.HALF_UP),
                  sc);
        }
      }
      m.put("taxableBase", base);
      m.put("amount", vat);
      m.put("exemptBase", exempt);
      a.items.add(m);
      d = d.add(l.discount.multiply(l.qty));
      gd = gd.add(l.globalDiscount.multiply(l.qty));
      ad = ad.add(l.advance.multiply(l.qty));
      gad = gad.add(l.globalAdvance.multiply(l.qty));
      switch (l.treatment) {
        case "3" -> ex = ex.add(total);
        case "2" -> exo = exo.add(total);
        default -> {
          ex = ex.add(exempt);
          if (l.rate == 5) {
            s5 = s5.add(l.treatment.equals("4") ? base.add(vat) : total);
            i5 = i5.add(vat);
            b5 = b5.add(base);
          } else {
            s10 = s10.add(l.treatment.equals("4") ? base.add(vat) : total);
            i10 = i10.add(vat);
            b10 = b10.add(base);
          }
        }
      }
    }
    BigDecimal grossTotal = ex.add(exo).add(s5).add(s10);
    a.totals.put("exemptSubtotal", ex);
    a.totals.put("exoneratedSubtotal", exo);
    a.totals.put("vat5Subtotal", s5);
    a.totals.put("vat10Subtotal", s10);
    a.totals.put("grossTotal", grossTotal);
    a.totals.put("itemDiscountTotal", round(d, sc));
    a.totals.put("itemGlobalDiscountTotal", round(gd, sc));
    a.totals.put("itemAdvanceTotal", round(ad, sc));
    a.totals.put("itemGlobalAdvanceTotal", round(gad, sc));
    a.totals.putIfAbsent("globalDiscountRate", ZERO);
    a.totals.put("discountTotal", round(d.add(gd), sc));
    a.totals.put("advanceTotal", round(ad.add(gad), sc));
    a.totals.put("roundingAmount", ZERO);
    a.totals.put("netTotal", grossTotal);
    a.totals.put("vat5Amount", i5);
    a.totals.put("vat10Amount", i10);
    a.totals.put("vatTotal", i5.add(i10));
    a.totals.put("vat5TaxableBaseTotal", b5);
    a.totals.put("vat10TaxableBaseTotal", b10);
    a.totals.put("vatTaxableBaseTotal", b5.add(b10));
    if ("2".equals(a.exchangeCondition)) {
      BigDecimal sum = ZERO;
      for (Map<String, BigDecimal> m : a.items) {
        BigDecimal g = round(m.get("total").multiply(a.exchangeRate), 0);
        m.put("guaraniTotal", g);
        m.put("exchangeRate", a.exchangeRate);
        sum = sum.add(g);
      }
      a.totals.put("guaraniTotal", sum);
    } else if (!"PYG".equals(a.currency)) {
      a.totals.put("guaraniTotal", round(grossTotal.multiply(a.exchangeRate), 0));
    }
    return a;
  }

  private static BigDecimal round(BigDecimal v, int scale) {
    return v.setScale(scale, RoundingMode.HALF_UP);
  }

  // ---------------------------------------------------------- document

  private static ElectronicDocument build(
      Properties p, Amounts a, List<Line> lines, String documentNumber, SecureRandom random) {
    String testName = "DE generado en ambiente de prueba - sin valor comercial ni fiscal";
    CommercialOperation.Builder op =
        CommercialOperation.builder().taxTypeCode("1").currencyCode(a.currency);
    if (!"5".equals(a.documentType)) {
      op.transactionTypeCode("1");
    }
    if (!"PYG".equals(a.currency)) {
      op.exchangeRateConditionCode(a.exchangeCondition);
      if ("1".equals(a.exchangeCondition)) {
        op.exchangeRate(a.exchangeRate);
      }
    }
    Payment.Builder payment =
        Payment.builder().typeCode("1").amount(a.totals.get("netTotal")).currencyCode(a.currency);
    if (!"PYG".equals(a.currency)) {
      payment.exchangeRate(a.exchangeRate);
    }
    DocumentTypeData.Builder data = DocumentTypeData.builder();
    if ("5".equals(a.documentType)) {
      data.creditDebitNote(
          io.github.sifensdk.core.model.CreditDebitNote.builder().reasonCode("2").build());
    } else {
      data.invoice(Invoice.builder().presenceIndicatorCode("1").build())
          .operationCondition(
              OperationCondition.builder().conditionCode("1").addPayment(payment.build()).build());
    }
    for (int i = 0; i < lines.size(); i++) {
      Line l = lines.get(i);
      Map<String, BigDecimal> m = a.items.get(i);
      ItemDiscountsAndAdvances.Builder dd =
          ItemDiscountsAndAdvances.builder()
              .discountAmount(m.get("discountAmount"))
              .globalDiscountAmount(m.get("globalDiscountAmount"))
              .advanceAmount(m.get("advanceAmount"))
              .globalAdvanceAmount(m.get("globalAdvanceAmount"))
              .total(m.get("total"));
      if (m.containsKey("discountRate")) {
        dd.discountRate(m.get("discountRate"));
      }
      if (m.containsKey("guaraniTotal")) {
        dd.guaraniTotal(m.get("guaraniTotal"));
      }
      ItemVat.Builder vat = ItemVat.builder().treatmentCode(l.treatment);
      if (!l.treatment.equals("2") && !l.treatment.equals("3")) {
        vat.taxableProportionRate(l.proportion).rate(BigDecimal.valueOf(l.rate));
      } else {
        vat.taxableProportionRate(ZERO).rate(ZERO);
      }
      vat.taxableBase(m.get("taxableBase")).amount(m.get("amount")).exemptBase(m.get("exemptBase"));
      data.addItem(
          Item.builder()
              .internalCode("SDK-" + i)
              .description("Ítem de prueba del SIFEN SDK " + i)
              .unitOfMeasureCode("77")
              .quantity(l.qty)
              .valuation(
                  ItemValues.builder()
                      .exchangeRate(m.get("exchangeRate"))
                      .unitPrice(m.get("unitPrice"))
                      .grossTotal(m.get("grossTotal"))
                      .discountsAndAdvances(dd.build())
                      .build())
              .vat(vat.build())
              .build());
    }
    Map<String, BigDecimal> t = a.totals;
    Totals.Builder totals =
        Totals.builder()
            .exemptSubtotal(t.get("exemptSubtotal"))
            .exoneratedSubtotal(t.get("exoneratedSubtotal"))
            .vat5Subtotal(t.get("vat5Subtotal"))
            .vat10Subtotal(t.get("vat10Subtotal"))
            .grossTotal(t.get("grossTotal"))
            .itemDiscountTotal(t.get("itemDiscountTotal"))
            .itemGlobalDiscountTotal(t.get("itemGlobalDiscountTotal"))
            .itemAdvanceTotal(t.get("itemAdvanceTotal"))
            .itemGlobalAdvanceTotal(t.get("itemGlobalAdvanceTotal"))
            .globalDiscountRate(t.get("globalDiscountRate"))
            .discountTotal(t.get("discountTotal"))
            .advanceTotal(t.get("advanceTotal"))
            .roundingAmount(t.get("roundingAmount"))
            .netTotal(t.get("netTotal"))
            .vat5Amount(t.get("vat5Amount"))
            .vat10Amount(t.get("vat10Amount"))
            .vatTotal(t.get("vatTotal"))
            .vat5TaxableBaseTotal(t.get("vat5TaxableBaseTotal"))
            .vat10TaxableBaseTotal(t.get("vat10TaxableBaseTotal"))
            .vatTaxableBaseTotal(t.get("vatTaxableBaseTotal"));
    if (t.containsKey("guaraniTotal")) {
      totals.guaraniTotal(t.get("guaraniTotal"));
    }
    if (t.containsKey("vat5RoundingAmount")) {
      totals.vat5RoundingAmount(t.get("vat5RoundingAmount"));
    }
    if (t.containsKey("vat10RoundingAmount")) {
      totals.vat10RoundingAmount(t.get("vat10RoundingAmount"));
    }
    if (t.containsKey("commissionAmount")) {
      totals.commissionAmount(t.get("commissionAmount"));
      totals.commissionVatAmount(t.get("commissionVatAmount"));
    }
    ElectronicDocument.Builder doc = ElectronicDocument.builder();
    if (a.associatedCdc != null) {
      doc.addAssociatedDocument(
          io.github.sifensdk.core.model.AssociatedDocument.builder()
              .typeCode("1")
              .cdc(a.associatedCdc)
              .build());
    }
    return doc.operation(
            Operation.builder()
                .emissionTypeCode("1")
                .securityCode(SecurityCode.generate(random, documentNumber))
                .build())
        .stamp(
            Stamp.builder()
                .documentTypeCode(a.documentType)
                .number(p.getProperty("timbrado"))
                .establishment(p.getProperty("establecimiento"))
                .dispatchPoint(p.getProperty("puntoExpedicion"))
                .documentNumber(documentNumber)
                .startDate(LocalDate.parse(p.getProperty("timbradoInicio")))
                .build())
        .generalData(
            GeneralData.builder()
                .issueDateTime(
                    LocalDateTime.now(SifenDocuments.PARAGUAY).truncatedTo(ChronoUnit.SECONDS))
                .commercialOperation(op.build())
                .issuer(
                    Issuer.builder()
                        .ruc(p.getProperty("ruc"))
                        .checkDigit(p.getProperty("dv"))
                        .taxpayerTypeCode(p.getProperty("tipoContribuyente"))
                        .name(testName)
                        .address(p.getProperty("direccion"))
                        .houseNumber(p.getProperty("numeroCasa"))
                        .departmentCode(p.getProperty("departamento"))
                        .districtCode(p.getProperty("distrito"))
                        .cityCode(p.getProperty("ciudad"))
                        .phone(p.getProperty("telefono"))
                        .email(p.getProperty("email"))
                        .addEconomicActivity(
                            EconomicActivity.builder()
                                .code(p.getProperty("actividad"))
                                .description(p.getProperty("actividadDescripcion"))
                                .build())
                        .build())
                .receiver(
                    Receiver.builder()
                        .natureCode("1")
                        .operationTypeCode("1")
                        .countryCode("PRY")
                        .taxpayerTypeCode(p.getProperty("tipoContribuyente"))
                        .ruc(p.getProperty("ruc"))
                        .checkDigit(p.getProperty("dv"))
                        .name(testName)
                        .build())
                .build())
        .documentTypeData(data.build())
        .totals(totals.build())
        .build();
  }

  // ---------------------------------------------------------- helpers

  private static Properties issuer() throws IOException {
    Properties p = new Properties();
    try (Reader r =
        Files.newBufferedReader(Path.of(env("SIFEN_TEST_ISSUER_FILE")), StandardCharsets.UTF_8)) {
      p.load(r);
    }
    return p;
  }

  private static String firstLine(String s) {
    if (s == null) {
      return "";
    }
    int i = s.indexOf('\n');
    return i < 0 ? s : s.substring(0, i);
  }

  private static String env(String name) {
    String value = System.getenv(name);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException("Missing environment variable " + name);
    }
    return value;
  }
}
