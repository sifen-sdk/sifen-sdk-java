# Eventos

Un evento informa a SIFEN algo que pasó con un documento ya aprobado, o con números de timbrado que no se van a
usar. El SDK arma, valida y firma los 8 eventos que envía un contribuyente: 4 del emisor y 4 del receptor.

Se usan igual que los documentos: `SifenEvents` valida y firma, sin red; `SifenClient` envía.

```java
SifenEvents events = SifenEvents.builder()
    .environment(Environment.TEST)
    .build();                                        // inmutable: una por ambiente

Event cancel = Event.builder()
    .id("1")                                         // GDE003: identificador del evento
    .detail(Cancellation.builder()
        .cdc(signed.cdc())
        .reason("Error en los datos del receptor")
        .build())
    .build();

ValidationResult result = events.validate(cancel);   // opcional: sign también valida
SignedEvent signedEvent = events.sign(cancel, certificate);

EventReceptionResult registered = client.sendEvents(List.of(signedEvent));
for (EventResult e : registered.events()) {
  e.id();                       // el id del evento
  e.status();                   // APPROVED, APPROVED_WITH_OBSERVATION, REJECTED o IN_PROCESS
  e.authorizationProtocol();    // el número de transacción, si se registró
  e.messages();                 // códigos y mensajes de SIFEN
}
```

- **Hasta 15 eventos por envío.** `sendEvents` lanza `IllegalArgumentException` si pasás más.
- **El id** identifica el evento dentro del envío: el resultado de cada uno vuelve con su id.
- **`sign` valida antes de firmar** y lanza `ValidationException` si hay errores, igual que con los documentos.
- **Plazos:** los que dependen del reloj de SIFEN (por ejemplo, el plazo para cancelar una factura) no se pueden
  verificar sin conexión. Aparecen en `ValidationResult.uncheckedRules()`.
- **Un evento firmado por fuera del SDK** se envía igual: `SignedEvent.of(xml, id, fechaDeFirma)`.
- Como con los documentos, el SDK **nunca reenvía solo** un evento ([envío y consultas](envio-y-consultas.md)).

## Eventos del emisor

### Cancelación

Anula un documento aprobado. Lleva el CDC y el motivo.

```java
Cancellation.builder().cdc(cdc).reason("Error en los datos del receptor").build()
```

### Inutilización de números

Informa un rango de números del timbrado que no se va a usar.

```java
NumberInvalidation.builder()
    .stampNumber("12345678")
    .establishment("001")
    .dispatchPoint("001")
    .rangeStartNumber("0000010")
    .rangeEndNumber("0000012")
    .documentTypeCode("1")                           // C002 de los documentos que no se emitirán
    .reason("Números salteados por error del sistema")
    .build()
```

`documentSeries` es opcional, para timbrados con serie.

### Nominación

Identifica al receptor de una factura emitida sin nombre (innominada). Lleva el CDC, el motivo y los datos del
receptor, con los mismos campos que el receptor de un documento (`receiverNatureCode`, `operationTypeCode`,
`receiverCountryCode`, `receiverName`, `receiverRuc` o el documento de identidad, …).

```java
Nomination.builder()
    .cdc(cdc)
    .reason("Nominación de factura innominada")
    .receiverNatureCode("1")
    .operationTypeCode("1")
    .receiverCountryCode("PRY")
    .receiverTaxpayerTypeCode("2")
    .receiverRuc("44444402")
    .receiverCheckDigit("5")
    .receiverName("Cliente de Ejemplo S.A.")
    .build()
```

### Actualización de datos del transporte

Informa un cambio durante el traslado: lugar de entrega, chofer, transportista o vehículo (`reasonCode` 1 a 4).
Lleva el CDC del documento con transporte y solo los datos del grupo que cambia.

```java
TransportUpdate.builder()
    .cdc(cdc)
    .reasonCode("2")                                 // cambio del chofer
    .driverName("Chofer de Ejemplo")
    .driverIdDocumentNumber("1234567")
    .build()
```

## Eventos del receptor

Los envía quien recibió el documento, con su propio certificado.

### Notificación de recepción

Avisa que el documento llegó. Lleva el CDC, la fecha de emisión, la de recepción, los datos del receptor y el total
en guaraníes.

```java
ReceiptNotification.builder()
    .cdc(cdc)
    .issueDateTime(LocalDateTime.of(2026, 11, 26, 10, 0))
    .receptionDateTime(LocalDateTime.of(2026, 11, 26, 10, 5))
    .receiverTypeCode("1")                           // 1 contribuyente, 2 no contribuyente
    .receiverRuc("44444402")
    .receiverCheckDigit("5")
    .receiverName("Cliente de Ejemplo S.A.")
    .guaraniTotal(new BigDecimal("131000"))
    .build()
```

### Conformidad

Total o parcial (`typeCode` 1 o 2). En la parcial se informa la fecha estimada de recepción.

```java
Conformity.builder().cdc(cdc).typeCode("1").build()
```

### Disconformidad

```java
Disconformity.builder().cdc(cdc).reason("Los ítems no coinciden con el pedido").build()
```

### Desconocimiento

El receptor no reconoce la operación. Lleva los mismos datos que la notificación de recepción, sin el total, y el
motivo.

```java
NonRecognition.builder()
    .cdc(cdc)
    .issueDateTime(LocalDateTime.of(2026, 11, 26, 10, 0))
    .receptionDateTime(LocalDateTime.of(2026, 11, 26, 10, 5))
    .receiverTypeCode("1")
    .receiverRuc("44444402")
    .receiverCheckDigit("5")
    .receiverName("Cliente de Ejemplo S.A.")
    .reason("No reconocemos esta operación")
    .build()
```

## Ver los eventos de un documento

`client.queryDocument(cdc)` devuelve los eventos registrados en `events()`, como `QueriedEvent`: su tipo
(`EventType`), su estado y los valores de su detalle. Incluye los que registran SIFEN u otros (por ejemplo, de la
SET), que no se envían desde el SDK ([envío y consultas](envio-y-consultas.md#consultar-un-documento)).

Las reglas de cada evento están en la referencia: [comunes](../reglas/ev-comun.md),
[cancelación](../reglas/ev-cancelacion.md), [inutilización](../reglas/ev-inutilizacion.md),
[nominación](../reglas/ev-nominacion.md), [transporte](../reglas/ev-transporte.md) y
[receptor](../reglas/ev-receptor.md).
