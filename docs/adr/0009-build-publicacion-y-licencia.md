# 0009 — Maven, Maven Central, Apache-2.0 y DCO

- **Estado:** Aceptada
- **Fecha:** 2026-11-26

## Contexto

El proyecto necesita una forma de construir y publicar que cualquiera pueda reproducir, y una licencia que permita
el uso comercial sin fricción.

## Decisión

- Maven con `mvnw`; compilaciones reproducibles (marca de tiempo fija) y formato verificado.
- Publicación en Maven Central por el Central Portal, bajo `io.github.sifen-sdk`, desde un entorno protegido.
- Licencia Apache-2.0. Los aportes se certifican con DCO (`git commit -s`); no hay CLA.
- Acciones de GitHub fijadas por hash, permisos mínimos y análisis de seguridad desde el primer día.
- Advertencias del compilador y de Javadoc tratadas como errores.

## Consecuencias

- El identificador de Maven queda atado al nombre de la organización en GitHub.
- Quien contribuye debe firmar sus commits.

## Alternativas descartadas

- **Gradle:** igual de capaz, pero el plugin oficial de publicación de Sonatype es para Maven.
- **MIT:** no incluye concesión de patentes. **AGPL/GPL:** impide el uso en sistemas comerciales cerrados.
- **Dominio propio para el identificador:** más portable, pero con costo.
