# 0001 — Java 17 como mínimo, un solo artefacto y líneas mayores por rama

- **Estado:** Aceptada
- **Fecha:** 2026-11-26

## Contexto

Los sistemas que emiten por SIFEN corren en versiones de Java muy distintas. Las librerías de referencia del
ecosistema (Spring, Jackson, JUnit) publican un solo artefacto compilado para un mínimo, que funciona en todas las
versiones posteriores, y solo suben ese mínimo en una versión mayor.

## Decisión

- El SDK se compila con `--release 17` y se prueba en las LTS 17, 21 y 25.
- No hay ediciones por versión de Java ni jars multi-versión.
- El mínimo solo sube en una versión mayor del SDK.
- Las líneas mayores conviven en este repositorio: `main` es la actual y la anterior pasa a una rama `N.x`. Las
  correcciones entran por la rama soportada más antigua y se fusionan hacia adelante.
- La línea anterior recibe Notas Técnicas y correcciones de seguridad durante 12 meses.

## Consecuencias

- Más sistemas pueden adoptar el SDK.
- El código no puede usar funciones del lenguaje ni APIs posteriores a Java 17.
- Mantener dos líneas cuesta poco mientras las reglas vivan como datos en `spec`, que ambas consumen.

## Alternativas descartadas

- **Mínimo 21 o 25:** código algo más moderno, pero deja afuera a muchos sistemas.
- **Una rama o edición por versión de Java:** cada corrección y cada Nota Técnica habría que repetirla en todas.
