# Documentos electrónicos

Todos los documentos son un `ElectronicDocument`. El tipo lo da el código del timbrado
(`Stamp.documentTypeCode`, campo C002) y el grupo que cargás en `DocumentTypeData`.

| Tipo | C002 | Grupo en `DocumentTypeData` |
|---|---|---|
| Factura electrónica | `1` | `invoice(Invoice)` |
| Autofactura electrónica | `4` | `selfInvoice(SelfInvoice)` |
| Nota de crédito electrónica | `5` | `creditDebitNote(CreditDebitNote)` |
| Nota de débito electrónica | `6` | `creditDebitNote(CreditDebitNote)` |
| Nota de remisión electrónica | `7` | `deliveryNote(DeliveryNote)` |

Los builders solo exigen lo que Java necesita (los elementos sin los que el objeto no existe). Las reglas de SIFEN
(obligatorio, condicional, prohibido, fórmulas) las aplica `validate`, todas juntas. Así ves todos los problemas en
una sola pasada.

Los ejemplos usan valores inventados. Los nombres de los métodos son en inglés; el campo del Manual Técnico y su
nombre XML están en el Javadoc de cada uno y en cada mensaje de error.

## Lo común a todos

```java
ElectronicDocument document = ElectronicDocument.builder()
    .operation(Operation.builder()
        .emissionType(EmissionType.NORMAL)           // B002 (o .emissionTypeCode("1"))
        .securityCode(securityCode)                  // B004, 9 dígitos
        .build())
    .stamp(Stamp.builder()
        .documentType(DocumentType.INVOICE)          // C002
        .number("12345678")                          // timbrado
        .establishment("001")
        .dispatchPoint("001")
        .documentNumber("0000001")
        .startDate(LocalDate.of(2026, 1, 1))         // inicio de vigencia del timbrado
        .build())
    .generalData(GeneralData.builder()
        .issueDateTime(LocalDateTime.of(2026, 11, 26, 10, 0))
        .commercialOperation(CommercialOperation.builder()
            .transactionType(TransactionType.GOODS_SALE)
            .taxType(TaxType.VAT)
            .currency(Currency.getInstance("PYG"))   // o .currencyCode("PYG")
            .build())
        .issuer(issuer)                              // Issuer: RUC, razón social, dirección, actividades
        .receiver(receiver)                          // Receiver: contribuyente o no, del país o del exterior
        .build())
    .documentTypeData(/* según el tipo, abajo */)
    .totals(totals)                                  // Totals: todos los subtotales y totales, calculados por vos
    .build();
```

- **Código de seguridad:** `SecurityCode.generate(new SecureRandom(), "0000001")` genera uno válido para ese
  número de documento. Es opcional: podés traer el tuyo.
- **Lo que no cargás:** el CDC, su dígito verificador, las descripciones de catálogo (`...Description`), la fecha
  de firma, la firma y el QR. Los completa el SDK ([ADR 0011](../adr/0011-lo-que-completa-el-sdk.md)). Si cargás
  una descripción, se verifica que corresponda al código.
- **Importes:** `BigDecimal`, tal como los calculó tu sistema. El SDK no los redondea ni los corrige.
- **Códigos de catálogo:** cada campo con catálogo acepta una constante (`emissionType(EmissionType.NORMAL)`, del
  paquete `io.github.sifensdk.core.catalog`) o el código como texto (`emissionTypeCode("1")`), que sirve para
  códigos que SIFEN agregue antes de que el SDK los conozca. Ciudad, distrito y país van siempre como texto; la
  moneda acepta `java.util.Currency` ([ADR 0014](../adr/0014-codigos-de-catalogo-tipados.md)).
- **Ítems:** cada `Item` lleva su código, descripción, unidad y cantidad; los valores van en `ItemValues`
  (precio, descuentos y anticipos en `ItemDiscountsAndAdvances`) y el IVA en `ItemVat`.

## Factura electrónica (C002 = 1)

```java
DocumentTypeData.builder()
    .invoice(Invoice.builder()
        .presenceIndicator(PresenceIndicator.IN_PERSON)  // E011
        .build())
    .operationCondition(OperationCondition.builder()
        .condition(PaymentCondition.CASH)            // E601: contado o crédito
        .addPayment(Payment.builder()
            .type(PaymentType.CASH)                  // E606
            .amount(new BigDecimal("131000"))
            .currency(Currency.getInstance("PYG"))
            .build())
        .build())
    .addItem(item)
    .build();
```

- **Crédito:** `OperationCondition.builder().credit(Credit)`, con plazo (`term`) o cuotas (`addInstallment`).
- **Tarjeta o cheque:** `Payment.builder().card(CardPayment)` o `.check(CheckPayment)`.
- **Compras públicas:** `Invoice.builder().publicProcurement(PublicProcurement)`.
- **Envío futuro:** `Invoice.builder().futureShipmentDate(fecha)`.

## Autofactura electrónica (C002 = 4)

La emite quien compra a un vendedor que no es contribuyente. Los datos del vendedor y del lugar de la transacción
van en `SelfInvoice`:

```java
DocumentTypeData.builder()
    .selfInvoice(SelfInvoice.builder()
        .sellerNature(SellerNature.NON_TAXPAYER)     // E301
        .sellerIdDocumentType(IdDocumentType.PARAGUAYAN_ID)  // E304
        .sellerIdDocumentNumber("1234567")
        .sellerName("Vendedor de Ejemplo")
        .sellerAddress("Calle del Vendedor")
        .sellerHouseNumber("0")
        .sellerDepartment(Department.CAPITAL)
        .sellerCityCode("1")
        .transactionAddress("Calle de la Transacción")
        .transactionDepartment(Department.CAPITAL)
        .transactionCityCode("1")
        .build())
    .operationCondition(/* contado */)
    .addItem(item)                                   // sin ItemVat
    .build();
```

Lo que cambia respecto de la factura, según las reglas de SIFEN:

- El receptor es el propio emisor, la moneda es guaraníes y la operación es al contado.
- Los ítems no llevan IVA, y los totales no llevan subtotales por tasa ni IVA.
- Lleva un documento asociado: la constancia electrónica (`AssociatedDocument` con `type(AssociatedDocumentType.ELECTRONIC_CERTIFICATE)`).

## Nota de crédito y nota de débito (C002 = 5 y 6)

Las dos tienen la misma estructura: un motivo y el documento que corrigen.

```java
ElectronicDocument creditNote = ElectronicDocument.builder()
    // operation, stamp con documentType(DocumentType.CREDIT_NOTE) (o DEBIT_NOTE), generalData
    .documentTypeData(DocumentTypeData.builder()
        .creditDebitNote(CreditDebitNote.builder()
            .reason(CreditDebitNoteReason.RETURN)    // E401
            .build())
        .addItem(item)
        .build())
    .totals(totals)
    .addAssociatedDocument(AssociatedDocument.builder()
        .type(AssociatedDocumentType.ELECTRONIC)     // H002
        .cdc(cdcDeLaFactura)
        .build())
    .build();
```

Si el documento asociado es impreso, en lugar del CDC van el timbrado, establecimiento, punto de expedición, número
y fecha (`printedStamp`, `establishment`, `dispatchPoint`, `documentNumber`, `printedIssueDate`).

## Nota de remisión electrónica (C002 = 7)

Documenta el traslado de mercaderías. No lleva importes: ni condición de operación, ni valores por ítem, ni totales.
Lleva el transporte, que en la remisión es obligatorio.

```java
DocumentTypeData.builder()
    .deliveryNote(DeliveryNote.builder()
        .reason(DeliveryNoteReason.SALE)             // E501
        .responsible(DeliveryNoteIssuer.INVOICE_ISSUER)  // E503
        .estimatedKilometers(15)
        .futureInvoiceDate(LocalDate.of(2026, 11, 30))
        .build())
    .addItem(Item.builder()
        .internalCode("A-001")
        .description("Producto de ejemplo")
        .unitOfMeasure(UnitOfMeasure.UNIT)           // E709: 77
        .quantity(new BigDecimal("10"))
        .goodsRelevance(GoodsRelevance.BREAKAGE_TOLERANCE)
        .breakageOrShrinkageQuantity(BigDecimal.ZERO)
        .breakageOrShrinkageRate(BigDecimal.ZERO)
        .build())
    .transport(transport)                            // ver abajo
    .build();
```

En la remisión, `Operation` lleva además `taxAuthorityInformation` (B006): el texto que exige la Resolución
General 41/2014. Lo escribe el emisor.

## Transporte

El grupo de transporte (E900) es obligatorio en la nota de remisión y opcional en la factura.

```java
Transport transport = Transport.builder()
    .type(TransportType.OWN)                         // E901
    .mode(TransportMode.LAND)                        // E903
    .freightPayer(FreightPayer.INVOICE_ISSUER)       // E905
    .estimatedStartDate(LocalDate.of(2026, 11, 26))
    .estimatedEndDate(LocalDate.of(2026, 11, 27))
    .departureLocation(TransportDeparture.builder()
        .address("Calle de Ejemplo").houseNumber("123")
        .department(Department.CAPITAL).districtCode("1").cityCode("1")
        .build())
    .addDeliveryLocation(TransportDelivery.builder()
        .address("Avenida de Ejemplo").houseNumber("456")
        /* departamento, distrito y ciudad */
        .build())
    .addVehicle(TransportVehicle.builder()
        .type("Camión").brand("Marca")
        .identificationType(VehicleIdentificationType.VEHICLE_IDENTIFICATION_NUMBER)  // E967
        .identificationNumber("VIN0000000000001")
        .build())
    .carrier(Carrier.builder()
        .nature(CarrierNature.NON_TAXPAYER)
        .name("Transportista de Ejemplo")
        .idDocumentType(IdDocumentType.PARAGUAYAN_ID).idDocumentNumber("1234567")
        .driverName("Chofer de Ejemplo").driverIdDocumentNumber("1234567")
        .taxAddress("Calle del Transportista 456")
        .driverAddress("Calle del Chofer 789")
        .build())
    .build();
```

Qué grupos son obligatorios según el tipo de documento y la modalidad está en la
[referencia de reglas del transporte](../reglas/de-e10-transporte.md). Si los datos cambian durante el traslado,
se informa con el evento de [actualización de datos del transporte](eventos.md#actualizacion-de-datos-del-transporte).

## Grupos sectoriales

Algunos sectores informan datos propios. Van en `DocumentTypeData.sectorSpecificFields(...)` o en cada ítem:

| Sector | Dónde | Clase |
|---|---|---|
| Energía eléctrica (E791) | `SectorSpecificFields.addElectricity` | `Electricity` |
| Seguros (E800) | `SectorSpecificFields.insurance` | `Insurance`, con `InsurancePolicy` |
| Supermercados (E810) | `SectorSpecificFields.supermarket` | `Supermarket` |
| Datos adicionales de uso comercial (E820) | `SectorSpecificFields.additionalCommercialData` | `AdditionalCommercialData` |
| Rastreo de la mercadería (E750) | `Item.goodsTracking` | `GoodsTracking` |
| Vehículos nuevos (E770) | `Item.newVehicle` | `NewVehicle` |

Los datos complementarios de uso general (G001: órdenes de compra y de venta, asiento contable, carga) van en
`ElectronicDocument.complementaryData(ComplementaryData)`.

## Leer los errores de validación

`validate` devuelve un `ValidationResult` con todos los problemas:

```java
ValidationResult result = documents.validate(document);
if (!result.isValid()) {
  for (ValidationIssue issue : result.errors()) {
    issue.ruleId();       // R-F014-A3, o XSD si es un error de esquema
    issue.fieldId();      // F014
    issue.xmlName();      // dTotGralOpe
    issue.path();         // rDE/DE/gTotSub/dTotGralOpe
    issue.message();      // qué falló, en una frase
    issue.sifenCode();    // 2365: el código con que SIFEN lo rechazaría
    issue.sources();      // MT150 2365: dónde está documentada la regla
  }
}
```

- **`ruleId()`** es el id de la regla en la especificación. Buscalo en la
  [referencia de reglas](../reglas/index.md): cada regla tiene un ancla con su id en minúsculas.
- **`errors()`** son los que impiden firmar. **`issues()`** incluye además las advertencias (`Severity.WARNING`).
- **`uncheckedRules()`** son reglas que aplican pero el SDK no puede verificar sin conexión (por ejemplo, un
  plazo que depende del reloj de SIFEN). Revisalas vos.
- **Una regla como advertencia:** `SifenDocuments.builder().warningRules(Set.of("R-…"))` la informa sin bloquear la
  firma. Sirve mientras se confirma una contradicción de la especificación.
- **Fórmulas:** un importe cumple si difiere del resultado exacto en 0,5 o menos en la unidad del campo
  ([ADR 0010](../adr/0010-semantica-de-la-verificacion-aritmetica.md)). Las sumas usan los valores de cada ítem tal
  como los informaste.

`sign` valida de nuevo y, si hay errores, lanza `ValidationException`; `exception.result()` trae el mismo
`ValidationResult`.
