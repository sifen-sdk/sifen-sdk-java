# 0004 — Sin estado global: un cliente por emisor

- **Estado:** Aceptada
- **Fecha:** 2026-11-26

## Contexto

Un mismo proceso puede emitir para muchos contribuyentes, cada uno con su certificado y su ambiente. Guardar
configuración o conexiones TLS en variables estáticas hace que un emisor termine usando el certificado de otro.

## Decisión

- No hay configuración estática ni instancias únicas compartidas. Todo se recibe explícitamente.
- Un cliente se construye para un emisor, un certificado y un ambiente; es inmutable y seguro entre hilos.
- El ambiente (test o producción) siempre lo elige quien llama; no hay valor por defecto.
- El reloj y la zona horaria se pueden inyectar.

## Consecuencias

- Emisores y ambientes distintos conviven sin interferirse.
- Quien integra debe crear y conservar un cliente por emisor.

## Alternativas descartadas

- **Fachada estática con configuración global:** simple para un solo emisor, incorrecta para varios.
