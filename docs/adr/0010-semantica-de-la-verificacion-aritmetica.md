# 0010 — Semántica de la verificación aritmética

- **Estado:** Aceptada, revisada el 2026-11-27 con pruebas contra el ambiente de test
- **Fecha:** 2026-11-26

## Contexto

SIFEN recalcula los importes de cada documento y lo rechaza si no coinciden con sus fórmulas. El SDK no calcula
(ADR 0001 de `spec`), pero debe detectar antes de enviar un importe que SIFEN va a rechazar. Muchas fórmulas
tienen divisiones con resultado periódico (por ejemplo, la base gravada del IVA), así que "igual al resultado de la
fórmula" necesita una definición precisa.

## Decisión

- **Nunca se modifica un valor.** La verificación compara y, si no coincide, informa el campo, la regla, su fuente
  y el código con que respondería SIFEN.
- Un valor informado es correcto si su diferencia con el **resultado exacto** de la fórmula es **≤ 0,5 en la unidad
  del campo** (guaraníes, dólares, puntos porcentuales), sin importar cuántos decimales informó el emisor. Si la
  regla documenta una **holgura** (la variación de 0,8 en los porcentajes de descuento), la tolerancia es esa holgura,
  sin sumarle el 0,5.
- Las **sumatorias** usan los valores informados de cada ítem, no valores recalculados.
- Una regla que la especificación marca como `descartada` (SIFEN no la aplica, según una evidencia de `spec`) no se
  verifica nunca.
- Cada regla puede configurarse como advertencia en lugar de error, por cliente y sin estado global.

## Revisión del 2026-11-27

Pruebas contra el ambiente de test (evidencia E-001 de `spec`) mostraron que la primera versión de esta decisión no
coincidía con SIFEN en tres puntos:

- **Tolerancia:** se aceptaba media unidad del último decimal informado (0,005 con 2 decimales). SIFEN acepta 0,50 y
  rechaza 0,51, también en dólares; en guaraníes el resultado era el mismo.
- **Holgura:** se sumaba al medio decimal. SIFEN acepta 0,80 y rechaza 0,81 en EA003 y EA004, y rechaza 1 guaraní de
  diferencia en EA004, que antes el SDK dejaba pasar.
- **Fórmulas en conflicto:** se verificaba la de la validación numerada (2371 para F017). SIFEN verifica la de la
  tabla de campos y rechaza con 2371 (C-502).

SIFEN verifica casi todas las fórmulas con código de rechazo incluso con 1 guaraní de diferencia, así que la
verificación sigue siendo un error por defecto: avisar antes de firmar evita rechazos.

## Consecuencias

- El emisor conserva su lógica y su redondeo; el SDK solo le avisa cuando SIFEN no los aceptaría.
- Si el motor de SIFEN resulta más tolerante o más estricto que esta definición, se ajusta con evidencia de pruebas
  contra el ambiente de test y se registra en `spec` (como en la revisión del 2026-11-27).

## Alternativas descartadas

- **Comparar exacto:** rechazaría valores correctamente redondeados.
- **Tolerancia fija de 1 guaraní:** SIFEN rechaza 1 guaraní de diferencia respecto del exacto cuando supera 0,5.
- **Media unidad del último decimal informado** (primera versión): más estricta que SIFEN en moneda extranjera.
- **Redondear el valor del emisor:** viola la regla de no modificar importes.
