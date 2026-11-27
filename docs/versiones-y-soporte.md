# Versiones y soporte

## Versiones de Java

- **Un solo artefacto.** Cada versión del SDK se compila para su Java mínimo y funciona en todas las versiones
  posteriores. No hay ediciones por versión de Java.
- **Mínimo actual: Java 17.** Cada cambio se prueba en las versiones LTS 17, 21 y 25.
- **El mínimo solo sube en una versión mayor** del SDK (por ejemplo, de 1.x a 2.0), nunca en una menor.

## Numeración

El proyecto sigue [Semantic Versioning](https://semver.org/lang/es/).

- **Antes de 1.0 (0.x):** la API puede cambiar entre versiones menores. Cada cambio incompatible se anota en
  el `CHANGELOG.md`. Las versiones 0.x no se publicaron.
- **Beta (`1.0.0-beta.N`) y candidata (`1.0.0-rc.N`):** versiones previas a la 1.0, publicadas en Maven Central para
  usar en producción y encontrar errores. Ver [Beta](#beta) abajo.
- **Desde 1.0:** los cambios incompatibles solo entran en una versión mayor.
- **Notas Técnicas de SIFEN:** incorporar una Nota Técnica es una versión menor o de corrección, salvo que
  obligue a romper la API.

## Beta

La 1.0 se publica cuando el SDK probó que funciona en producción, no antes.

- **Qué esperar.** La beta está completa: valida, firma y envía todos los documentos y eventos, verificados contra el
  ambiente de test de SIFEN. Lo que falta es uso real en producción.
- **API.** Solo cambia para corregir un problema real. Cada cambio incompatible se anota en el `CHANGELOG.md` y la
  integración continua lo detecta comparando con la beta anterior.
- **Correcciones.** Salen en la beta siguiente (`beta.2`, `beta.3`, …). Solo la última beta recibe correcciones.
- **Reportar.** Con la plantilla de errores del repositorio y el intercambio anonimizado
  (`exchange().anonymized()`), sin datos reales. Ver [Pasar a producción](guias/produccion.md).

### Criterios para publicar la 1.0

Se publica una `1.0.0-rc.1` y después la `1.0.0` cuando se cumple todo esto:

1. **Uso en producción:** al menos 4 semanas desde la última beta, con documentos reales aprobados por SIFEN de cada
   tipo y evento que la beta declara soportado, informados por quienes la usan (sin datos, solo el tipo y el
   resultado).
2. **Sin errores graves abiertos:** ningún error que impida emitir, que haga firmar o enviar algo distinto de lo
   pedido, o que exponga datos.
3. **API estable:** la última beta no tuvo cambios incompatibles.
4. **Conformidad:** la batería de casos de `spec` pasa completa.
5. **Integración continua** en verde en Java 17, 21 y 25.

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
| 1.0.0-beta (`main`) | 17 | Beta: en uso en producción para estabilizar la 1.0 |
| 0.x | 17 | No publicadas |
