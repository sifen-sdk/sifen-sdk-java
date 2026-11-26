# Versiones y soporte

## Versiones de Java

- **Un solo artefacto.** Cada versión del SDK se compila para su Java mínimo y funciona en todas las versiones
  posteriores. No hay ediciones por versión de Java.
- **Mínimo actual: Java 17.** Cada cambio se prueba en las versiones LTS 17, 21 y 25.
- **El mínimo solo sube en una versión mayor** del SDK (por ejemplo, de 1.x a 2.0), nunca en una menor.

## Numeración

El proyecto sigue [Semantic Versioning](https://semver.org/lang/es/).

- **Antes de 1.0 (0.x):** la API puede cambiar entre versiones menores. Cada cambio incompatible se anota en
  el `CHANGELOG.md`.
- **Desde 1.0:** los cambios incompatibles solo entran en una versión mayor.
- **Notas Técnicas de SIFEN:** incorporar una Nota Técnica es una versión menor o de corrección, salvo que
  obligue a romper la API.

## Líneas de versión

Todo vive en este mismo repositorio.

- `main` es la línea actual.
- Cuando empiece una versión mayor nueva, la anterior pasa a una rama de mantenimiento (`1.x`, `2.x`, …).
- Las correcciones entran primero en la rama soportada más antigua y se fusionan hacia adelante.

## Soporte

- La línea actual recibe funciones nuevas, Notas Técnicas y correcciones.
- La línea mayor anterior recibe **Notas Técnicas y correcciones de seguridad durante 12 meses** desde la
  publicación de la nueva.
- Las líneas más antiguas no reciben cambios.

| Línea | Java mínimo | Estado |
|---|---|---|
| 0.x (`main`) | 17 | En desarrollo; sin versiones publicadas |
