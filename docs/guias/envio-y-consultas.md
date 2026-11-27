# Envío y consultas

`SifenClient` habla con los web services de SIFEN. Se crea **uno por emisor**: lleva el certificado de ese emisor
para el TLS mutuo. Es inmutable y seguro entre hilos; crealo una vez y reutilizalo.

```java
SifenClient client = SifenClient.builder()
    .environment(Environment.TEST)                   // obligatorio, sin valor por defecto
    .keyMaterial(certificate)                        // el certificado de ESTE emisor
    .connectTimeout(Duration.ofSeconds(10))          // opcional
    .requestTimeout(Duration.ofSeconds(60))          // opcional
    .build();
```

También acepta `proxy(ProxySelector)`, `trustStore(KeyStore)` (si tu entorno necesita otras autoridades de
certificación), `baseUri(URI)` (por ejemplo, para apuntar al SIFEN simulado de `sifen-sdk-testing`) y
`clock(Clock)`.

## Enviar un documento

### Síncrono

```java
ReceptionResult reception = client.send(signed);
DocumentResult result = reception.document();
switch (result.status()) {
  case APPROVED, APPROVED_WITH_OBSERVATION -> guardarProtocolo(result.authorizationProtocol());
  case REJECTED -> corregir(result.messages());     // códigos y mensajes de SIFEN
  case IN_PROCESS -> consultarMasTarde(result.cdc());
  default -> revisar(result.messages());            // estados que agregue una versión menor
}
```

No todos los contribuyentes están habilitados para el servicio síncrono. El servicio general es el de lotes.

### Por lote

Hasta 50 documentos firmados, del mismo tipo y del mismo emisor.

```java
BatchSubmission submission = client.sendBatch(List.of(signed1, signed2));
if (submission.accepted()) {
  guardar(submission.protocolNumber(), submission.exchange().sentAt());
} else {
  revisar(submission.messages());
}
```

Aceptar el lote no aprueba los documentos: el resultado de cada uno se conoce después, consultando el lote.

## Cuándo consultar un lote

SIFEN bloquea al que consulta demasiado seguido. `BatchSchedule` calcula los instantes según la Guía de mejores
prácticas de SIFEN: la primera consulta a los 10 minutos del envío, después cada 10 minutos como mínimo, y hasta
48 horas después del envío.

```java
BatchSchedule schedule = BatchSchedule.recommended();
Instant sentAt = submission.exchange().sentAt();

Instant first = schedule.firstQueryAt(sentAt);        // no consultes antes
Instant next = schedule.nextQueryAt(lastQueryAt);     // entre consulta y consulta
boolean late = schedule.windowExpired(sentAt, Instant.now());
```

El SDK no espera ni consulta por su cuenta: `BatchSchedule` solo calcula instantes y tu aplicación agenda las
consultas como prefiera (una tarea programada, una cola). Si necesitás otros plazos,
`BatchSchedule.of(primeraConsulta, intervaloMínimo, ventana)`.

```java
BatchResult batch = client.queryBatch(protocolNumber);
switch (batch.state()) {
  case PROCESSED -> batch.documents().forEach(this::registrar);   // un DocumentResult por documento
  case IN_PROCESS -> reagendar(schedule.nextQueryAt(Instant.now()));
  case EXPIRED -> consultarCadaDocumentoPorCdc();                 // pasaron las 48 horas
  case NOT_FOUND, REJECTED -> revisar(batch.messages());
  default -> revisar(batch.messages());                           // estados que agregue una versión menor
}
```

`REJECTED` significa que SIFEN rechazó el lote entero (por ejemplo, documentos de tipos distintos): ningún documento
se procesó.

## Consultar un documento

```java
DocumentQueryResult query = client.queryDocument(cdc);
if (query.found()) {
  query.authorizationProtocol();   // número de transacción
  query.documentXml();             // el documento tal como lo devuelve SIFEN
  query.events();                  // eventos registrados: cancelación, conformidad, …
}
```

Es la forma de saber qué pasó con un documento cuando no sabés si llegó, o cuando venció la ventana del lote.

## Consultar un RUC

```java
RucQueryResult ruc = client.queryRuc("80000000");          // sin dígito verificador
if (ruc.found()) {
  ruc.name();
  ruc.statusDescription();
  ruc.electronicIssuer();          // true si es facturador electrónico; null si SIFEN no lo informa
}
```

## Nunca reenvía solo

SIFEN bloquea por un tiempo al contribuyente que reenvía un documento que todavía está en proceso. Por eso el SDK
**no reintenta ningún envío** de documentos, lotes ni eventos ([ADR 0007](../adr/0007-sin-reenvio-automatico.md)).

Si la red falla, `TransportException.outcomeUnknown()` te dice si el pedido pudo haber llegado:

```java
try {
  client.send(signed);
} catch (TransportException e) {
  if (e.outcomeUnknown()) {
    // Pudo haber llegado. Antes de reenviar, consultá por CDC.
    DocumentQueryResult query = client.queryDocument(signed.cdc());
  } else {
    // No llegó (por ejemplo, no se pudo conectar). Podés reenviar los mismos bytes.
  }
}
```

Para reenviar, usá el mismo `SignedDocument` que guardaste: se envían exactamente esos bytes. No firmes de nuevo:
cambiaría la fecha de firma y el QR.

## Resultados, no excepciones

- Un rechazo es `Status.REJECTED` con sus `ResultMessage` (código y mensaje), nunca una excepción
  ([ADR 0006](../adr/0006-resultados-y-errores.md)).
- `TransportException`: no se pudo hablar con SIFEN (red, TLS, tiempo de espera).
- `ProtocolException`: la respuesta no es de SIFEN o no se puede leer (por ejemplo, una página HTML de un
  balanceador). Trae el intercambio en `exchange()`, si lo hubo.
- Cada resultado tiene `exchange()`: los bytes enviados y recibidos, el endpoint, el estado HTTP, cuándo se envió y
  cuánto tardó. Guardalo si necesitás auditoría.
