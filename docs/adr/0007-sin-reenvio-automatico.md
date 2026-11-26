# 0007 — Nunca reenviar automáticamente una recepción

- **Estado:** Aceptada
- **Fecha:** 2026-11-26

## Contexto

SIFEN bloquea temporalmente al contribuyente que reenvía un documento que todavía está en proceso o un lote
duplicado. Si la red falla durante un envío, no se sabe si SIFEN lo recibió.

## Decisión

- El SDK no reintenta por su cuenta el envío de documentos, lotes ni eventos.
- Ante una falla de red durante un envío, informa "estado desconocido" e indica consultar por CDC o por lote.
- Solo las consultas, que no cambian nada en SIFEN, admiten reintentos configurables.
- El ayudante de lotes respeta los intervalos y plazos de consulta que SIFEN documenta.

## Consecuencias

- Menos riesgo de bloqueos.
- Quien integra decide cuándo reenviar, con la información que el SDK le da.

## Alternativas descartadas

- **Reintentos genéricos en todas las operaciones:** cómodos, pero provocan bloqueos.
