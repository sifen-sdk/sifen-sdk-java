# 0003 — Modelo escrito a mano, inmutable, con nombres legibles en inglés

- **Estado:** Aceptada
- **Fecha:** 2026-11-26

## Contexto

Un documento electrónico tiene cientos de campos. Los nombres del XML (`dTotOpe`) son crípticos, pero son la
referencia que cada integrador busca en el Manual Técnico.

## Decisión

- El modelo público se escribe a mano: clases inmutables con constructores fluidos (builders).
- Los nombres son legibles en inglés (`operationTotal`). El nombre XML y el código del Manual (`dTotOpe`, F014)
  figuran en una anotación, en el Javadoc y en cada mensaje de error.
- Una prueba compara el modelo con la tabla de campos de `spec` y falla si falta o sobra un campo.
- Los importes son `BigDecimal` y se conservan tal como llegan.
- Ninguna clase generada a partir del XSD aparece en la API pública.

## Consecuencias

- API cuidada y estable frente a cambios del XSD.
- Agregar un campo exige tocar el modelo y la especificación; la prueba impide que se desincronicen.

## Alternativas descartadas

- **Clases generadas con JAXB en la API pública:** mutables, atadas al XSD y con sus nombres crípticos.
- **Generador propio desde la especificación:** descartado; mantener el modelo a mano es viable y da mejor API.
- **Nombres XML crudos o nombres en español:** se eligió inglés por ser lo convencional en un SDK.
