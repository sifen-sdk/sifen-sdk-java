/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.live;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.sifensdk.client.BatchResult;
import io.github.sifensdk.client.BatchSubmission;
import io.github.sifensdk.client.DocumentQuery;
import io.github.sifensdk.client.DocumentResult;
import io.github.sifensdk.client.ReceptionResult;
import io.github.sifensdk.client.RucQuery;
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
import io.github.sifensdk.core.signing.SignedDocument;
import io.github.sifensdk.core.validation.ValidationResult;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * End to end against the real SIFEN test environment.
 *
 * <p>Runs only when the credentials are present, locally with {@code scripts/prueba-sifen-local.sh}
 * (see {@code docs/pruebas-contra-sifen.md}). It never prints secrets nor document contents: only
 * states, codes and SIFEN's messages.
 */
@EnabledIfEnvironmentVariable(named = "SIFEN_TEST_CSC", matches = ".+")
class LiveSifenTest {

  private static final BigDecimal PRICE = new BigDecimal("11000");

  @Test
  void issuesAnInvoiceInTheTestEnvironment() throws IOException, InterruptedException {
    Properties issuerData = new Properties();
    String issuerFile = System.getenv("SIFEN_TEST_ISSUER_FILE");
    if (issuerFile != null && !issuerFile.isBlank()) {
      try (Reader reader = Files.newBufferedReader(Path.of(issuerFile), StandardCharsets.UTF_8)) {
        issuerData.load(reader);
      }
    } else {
      issuerData.load(new StringReader(env("SIFEN_TEST_ISSUER")));
    }
    KeyMaterial key = LiveKeys.key();
    report("clave", LiveKeys.mode(), List.of());
    Csc csc = Csc.of(env("SIFEN_TEST_CSC_ID"), env("SIFEN_TEST_CSC").toCharArray());

    SifenDocuments documents = SifenDocuments.builder().environment(Environment.TEST).build();
    SifenClient client =
        SifenClient.builder().environment(Environment.TEST).keyMaterial(key).build();

    // 1. The issuer exists and is an electronic issuer.
    RucQuery ruc = client.queryRuc(issuerData.getProperty("ruc"));
    report(
        "consulta RUC",
        ruc.found() + " estado=" + ruc.statusCode() + " fe=" + ruc.electronicIssuer(),
        ruc.messages());
    assertThat(ruc.found()).as("the issuer's RUC exists in SIFEN").isTrue();

    // Document numbers must be new in every run: derived from the current minute.
    long base = (System.currentTimeMillis() / 60_000L) % 9_999_000L + 1;
    SecureRandom random = new SecureRandom();

    // 2. Synchronous reception: not every RUC is enabled for it, so the outcome is only reported.
    SignedDocument first = sign(documents, invoice(issuerData, base, random), key, csc);
    ReceptionResult reception = client.send(first);
    report(
        "recepción síncrona",
        reception.document().status().toString(),
        reception.document().messages());

    // 3. Batch reception: the general service; it must accept the batch.
    SignedDocument second = sign(documents, invoice(issuerData, base + 1, random), key, csc);
    BatchSubmission submission = client.sendBatch(List.of(second));
    report("lote", "aceptado=" + submission.accepted(), submission.messages());
    assertThat(submission.accepted()).as("SIFEN accepted the batch").isTrue();

    // 4. Optionally wait the time the best-practices guide asks for and read the result.
    if ("true".equals(System.getenv("SIFEN_TEST_WAIT"))) {
      Thread.sleep(Duration.ofMinutes(11).toMillis());
      BatchResult result = client.queryBatch(submission.protocolNumber());
      report("consulta de lote", result.state().toString(), result.messages());
      for (DocumentResult d : result.documents()) {
        report("  documento del lote", d.status().toString(), d.messages());
      }
      assertThat(result.state()).isEqualTo(BatchResult.State.PROCESSED);
      DocumentQuery query = client.queryDocument(second.cdc());
      report("consulta de documento", "encontrado=" + query.found(), query.messages());
    }
  }

  private static SignedDocument sign(
      SifenDocuments documents, ElectronicDocument document, KeyMaterial key, Csc csc) {
    ValidationResult validation = documents.validate(document);
    validation.issues().forEach(i -> System.out.println("validación: " + i));
    assertThat(validation.isValid()).as("the document is valid before sending").isTrue();
    return documents.sign(document, key, csc);
  }

  /**
   * A cash invoice of one item at 10 % VAT, to the issuer itself, so no third-party data is used.
   */
  static ElectronicDocument invoice(Properties p, long number, SecureRandom random) {
    String documentNumber = String.format("%07d", number);
    BigDecimal base = new BigDecimal("10000");
    BigDecimal vat = new BigDecimal("1000");
    BigDecimal zero = BigDecimal.ZERO;
    return ElectronicDocument.builder()
        .operation(
            Operation.builder()
                .emissionTypeCode("1")
                .securityCode(SecurityCode.generate(random, documentNumber))
                .build())
        .stamp(
            Stamp.builder()
                .documentTypeCode("1")
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
                .commercialOperation(
                    CommercialOperation.builder()
                        .transactionTypeCode("1")
                        .taxTypeCode("1")
                        .currencyCode("PYG")
                        .build())
                .issuer(
                    Issuer.builder()
                        .ruc(p.getProperty("ruc"))
                        .checkDigit(p.getProperty("dv"))
                        .taxpayerTypeCode(p.getProperty("tipoContribuyente"))
                        .name("DE generado en ambiente de prueba - sin valor comercial ni fiscal")
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
                        .name("DE generado en ambiente de prueba - sin valor comercial ni fiscal")
                        .build())
                .build())
        .documentTypeData(
            DocumentTypeData.builder()
                .invoice(Invoice.builder().presenceIndicatorCode("1").build())
                .operationCondition(
                    OperationCondition.builder()
                        .conditionCode("1")
                        .addPayment(
                            Payment.builder()
                                .typeCode("1")
                                .amount(PRICE)
                                .currencyCode("PYG")
                                .build())
                        .build())
                .addItem(
                    Item.builder()
                        .internalCode("SDK-PRUEBA")
                        .description("Ítem de prueba del SIFEN SDK")
                        .unitOfMeasureCode("77")
                        .quantity(BigDecimal.ONE)
                        .valuation(
                            ItemValues.builder()
                                .unitPrice(PRICE)
                                .grossTotal(PRICE)
                                .discountsAndAdvances(
                                    ItemDiscountsAndAdvances.builder()
                                        .discountAmount(zero)
                                        .globalDiscountAmount(zero)
                                        .advanceAmount(zero)
                                        .globalAdvanceAmount(zero)
                                        .total(PRICE)
                                        .build())
                                .build())
                        .vat(
                            ItemVat.builder()
                                .treatmentCode("1")
                                .taxableProportionRate(new BigDecimal("100"))
                                .rate(BigDecimal.TEN)
                                .taxableBase(base)
                                .amount(vat)
                                .exemptBase(zero)
                                .build())
                        .build())
                .build())
        .totals(
            Totals.builder()
                .grossTotal(PRICE)
                .itemDiscountTotal(zero)
                .itemGlobalDiscountTotal(zero)
                .itemAdvanceTotal(zero)
                .itemGlobalAdvanceTotal(zero)
                .globalDiscountRate(zero)
                .discountTotal(zero)
                .advanceTotal(zero)
                .roundingAmount(zero)
                .netTotal(PRICE)
                .vat10Subtotal(PRICE)
                .vat10Amount(vat)
                .vatTotal(vat)
                .vat10TaxableBaseTotal(base)
                .vatTaxableBaseTotal(base)
                .build())
        .build();
  }

  private static void report(
      String step, String outcome, List<io.github.sifensdk.client.ResultMessage> messages) {
    System.out.println(
        step
            + ": "
            + outcome
            + " "
            + messages.stream()
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
