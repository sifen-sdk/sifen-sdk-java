# 0005 — El XML firmado es inmutable; el flujo se expone por etapas

- **Estado:** Aceptada
- **Fecha:** 2026-11-26

## Contexto

Volver a serializar un XML firmado invalida la firma. Además, quien emite necesita guardar el documento firmado
antes de enviarlo, para no perderlo si falla la red.

## Decisión

- Firmar devuelve los bytes exactos del documento, junto con su CDC y la dirección del QR. Esos bytes nunca se
  vuelven a serializar.
- Las etapas se pueden usar por separado: construir, validar, firmar, enviar, consultar.
- Se puede firmar sin enviar y enviar un XML ya firmado por fuera del SDK.
- Cada resultado expone el intercambio completo con SIFEN para auditoría.

## Consecuencias

- Quien integra puede persistir antes de enviar y reenviar exactamente lo mismo.
- La API tiene más pasos que un único "emitir".

## Alternativas descartadas

- **Una sola operación que arma, firma y envía:** impide guardar antes de enviar y reenviar lo ya firmado.
