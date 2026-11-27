# Pasar a producción

Lo que cambia entre el ambiente de test y el de producción, y cómo actuar ante cada resultado. Durante la beta,
cualquier comportamiento distinto del que describe esta página es un error: reportalo (ver al final).

## Lo que cambia

| | Test | Producción |
|---|---|---|
| Ambiente en el SDK | `Environment.TEST` | `Environment.PRODUCTION` |
| Servicios | `sifen-test.set.gov.py` | `sifen.set.gov.py` |
| QR | `ekuatia.set.gov.py/consultas-test/qr` | `ekuatia.set.gov.py/consultas/qr` |
| CSC | El genérico de test que publica el Manual | El que te asignó la SET; el SDK rechaza los de test en producción |
| Timbrado | El de test | El de producción |
| Certificado | El del emisor | El mismo tipo; debe estar vigente y permitir firma |

- El ambiente no tiene valor por defecto: lo elegís vos en `SifenDocuments`, `SifenEvents` y `SifenClient`. Usá el
  mismo en los tres; un documento firmado para test lleva el QR de test.
- Los dos ambientes aceptan solo TLS 1.2 y piden la firma del cliente con RSA y SHA-256 (comprobado el 2026-11-27).
- Antes del primer envío, `SifenClient.queryRuc` con tu RUC confirma que el certificado, la conexión y el permiso
  de facturador electrónico funcionan.

## Antes de enviar

1. `validate` sin errores. Las advertencias son reglas que configuraste como tales.
2. `sign` y **guardá** `SignedDocument.xml()` antes de enviar: ese es el documento, con su CDC.
3. Enviá esos mismos bytes; nunca vuelvas a generar el XML de un documento ya firmado.

## Ante cada resultado

| Resultado | Qué significa | Qué hacer |
|---|---|---|
| `Status.APPROVED` | SIFEN lo aprobó | Guardá el protocolo (`authorizationProtocol`) |
| `Status.APPROVED_WITH_OBSERVATION` | Aprobado con observaciones | Guardalo y revisá los mensajes |
| `Status.REJECTED` | SIFEN lo rechazó; el documento no existe para SIFEN | Corregí el documento según el código y el mensaje, y volvé a firmarlo y enviarlo |
| `TransportException` con `outcomeUnknown() == false` | No llegó a SIFEN | Reintentá más tarde |
| `TransportException` con `outcomeUnknown() == true` | No se sabe si llegó | **Consultá por CDC** (`queryDocument`) antes de reenviar: reenviar un documento en proceso provoca bloqueos |
| `ProtocolException` | SIFEN respondió algo que no corresponde a lo documentado | Guardá el intercambio y reportalo |

- El SDK **nunca reenvía solo** ([ADR 0007](../adr/0007-sin-reenvio-automatico.md)).
- En lotes, consultá el resultado cuando lo indique `BatchSchedule`, no antes.
- Guardá cada intercambio (`exchange()`): es la prueba de qué se envió y qué respondió SIFEN.

## Reportar un error de la beta

1. Tomá el intercambio y anonimizalo: `result.exchange().anonymized()`. Conserva la estructura, los códigos y el
   mensaje de SIFEN, y reemplaza los datos por `9` y `x` manteniendo su forma; quita firmas y certificados.
2. Revisalo antes de compartirlo.
3. Abrí un issue con la plantilla **Error / Bug**: versión del SDK, ambiente, código y mensaje de SIFEN, regla del SDK
   si la hay, y el intercambio anonimizado.

Nunca adjuntes certificados, claves, CSC, documentos reales ni intercambios sin anonimizar.
