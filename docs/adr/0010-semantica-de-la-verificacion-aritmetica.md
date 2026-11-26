# 0010 — Semántica de la verificación aritmética

- **Estado:** Aceptada
- **Fecha:** 2026-11-26

## Contexto

SIFEN recalcula los importes de cada documento y lo rechaza si no coinciden con sus fórmulas. El SDK no calcula
(ADR 0001 de `spec`), pero debe detectar antes de enviar un importe que SIFEN va a rechazar. Muchas fórmulas
tienen divisiones con resultado periódico (por ejemplo, la base gravada del IVA), así que "igual al resultado de la
fórmula" necesita una definición precisa.

## Decisión

- **Nunca se modifica un valor.** La verificación compara y, si no coincide, informa el campo, la regla, su fuente
  y el código con que respondería SIFEN.
- Un valor informado es correcto si es un **redondeo válido del resultado exacto** de la fórmula **a la escala con
  que el emisor lo informó**: |informado − exacto| ≤ media unidad del último decimal informado. Con 0 decimales,
  110000 ⇒ base exacta 100000; con 2 decimales, 33.333… acepta 33.33.
- Las **holguras** solo se suman cuando la fuente las escribe (por ejemplo, la variación de 0,8 en los porcentajes
  de descuento). El redondeo de 50 céntimos que menciona el Manual en la sección de totales se aplica solo donde la
  especificación lo carga como holgura.
- Las **sumatorias** usan los valores informados de cada ítem, no valores recalculados.
- Cuando una fórmula tiene una contradicción abierta y otra versión, se verifica contra la **validación numerada**
  (la que tiene código de rechazo), que es la que aplica SIFEN.
- Cada regla puede configurarse como advertencia en lugar de error, por cliente y sin estado global.

## Consecuencias

- El emisor conserva su lógica y su redondeo; el SDK solo le avisa cuando SIFEN no los aceptaría.
- Si el motor de SIFEN resulta más tolerante o más estricto que esta definición, se ajusta con evidencia de pruebas
  contra el ambiente de test (fase 3) y se registra en `spec`.

## Alternativas descartadas

- **Comparar exacto:** rechazaría valores correctamente redondeados.
- **Tolerancia fija (por ejemplo, 1 guaraní):** no está documentada y oculta errores reales en moneda extranjera.
- **Redondear el valor del emisor:** viola la regla de no modificar importes.
