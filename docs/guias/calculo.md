# Cálculo de referencia

El SDK no calcula: transporta los importes que calculó tu sistema y verifica que cumplan las fórmulas de SIFEN
([ADR 0010](../adr/0010-semantica-de-la-verificacion-aritmetica.md)). Si todavía no tenés ese cálculo, o querés
compararlo con uno de referencia, el módulo opcional `sifen-sdk-calc` lo hace por vos.

- **Opcional.** Si no agregás la dependencia, no existe. El núcleo nunca lo invoca: ni `validate` ni `sign`
  calculan ni corrigen nada.
- **Comprobado.** Las pruebas completan miles de documentos al azar con el calculador y el núcleo no encuentra
  ningún error; SIFEN test aprobó las facturas completadas con él, en guaraníes y en dólares.

## Uso

```java
Calculator calc = Calculator.defaults();          // guaraníes sin decimales, otras monedas con 2, HALF_UP

ItemAmounts a = calc.item(ItemPrice.builder()
    .currencyCode("PYG")
    .quantity(new BigDecimal("3"))
    .unitPrice(new BigDecimal("12345"))
    .discountPerUnit(new BigDecimal("345"))        // opcional: descuento por unidad (EA002)
    .taxed(BigDecimal.TEN)                         // obligatorio: o .exempt(), .exonerated(), .partiallyTaxed(10, 30), .selfInvoice()
    .build());

Item item = Item.builder()
    .description("Producto")
    .unitOfMeasureCode("77")
    .quantity(new BigDecimal("3"))
    .values(a.values())                            // E720: valores, descuentos y total del ítem
    .vat(a.vat())                                  // E730: base gravada, IVA y base exenta
    .build();

// Con todos los ítems cargados en el documento:
ElectronicDocument complete = calc.withTotals(document);   // devuelve una copia con el grupo F; no toca el original
```

- **Descuento global:** pasá la misma tasa a cada ítem (`globalDiscountRate`, F010) y a
  `withTotals(document, tasa)`.
- **Moneda extranjera:** con un tipo de cambio por ítem (D017 = 2), cargalo en cada ítem (`exchangeRate`); el
  total en guaraníes (F023) se calcula solo. Con un tipo de cambio global (D017 = 1), se toma del documento.
- **Afectación de IVA obligatoria:** siempre elegís una (`taxed`, `exempt`, `exonerated`, `partiallyTaxed` o
  `selfInvoice`); si no llamás a ninguna, `build()` lanza `IllegalStateException`.
- **Autofactura:** `selfInvoice()` marca un ítem de autofactura: sin afectación de IVA (E731), sin grupo de IVA y sin
  descuentos ni anticipos.

## Cómo calcula

- **Por ítem.** Cada importe del ítem se calcula exacto con las fórmulas del Manual Técnico y se redondea una vez a
  los decimales de su moneda.
- **Los totales son sumas de los ítems ya redondeados.** No se recalcula nada sobre el total, que es lo que exige
  SIFEN: rechaza el cálculo "vertical" (evidencia E-001 de `spec`).
- **Redondeo explícito:** `Calculator.builder().decimals("USD", 2).roundingMode(RoundingMode.HALF_EVEN)`. Solo se
  aceptan los modos "a la mitad" (`HALF_UP`, `HALF_DOWN`, `HALF_EVEN`): truncar puede errar casi una unidad y SIFEN
  acepta hasta 0,5.
- **Redondeo a múltiplos de 50 (F013):** apagado por defecto; `roundToFifty(true)` lo activa en guaraníes.

## Lo que no calcula

- El impuesto selectivo al consumo (D013 = 2): sus totales no están definidos (contradicción C-501 de `spec`).
- La comisión (F025, F026) y el IVA del redondeo (F036, F037): SIFEN no los verifica.
- La nota de remisión no tiene importes.

Si tu sistema tiene otras reglas comerciales (cómo prorratear un descuento, cómo redondear un precio), usá las tuyas:
el núcleo verifica cualquier importe igual, venga de donde venga.
