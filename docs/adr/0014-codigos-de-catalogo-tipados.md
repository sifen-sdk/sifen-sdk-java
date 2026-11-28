# 0014 — Códigos de catálogo tipados

- **Estado:** Aceptada
- **Fecha:** 2026-11-28

## Contexto

Muchos campos de SIFEN toman su valor de un catálogo: tipo de emisión, tipo de documento, afectación del IVA,
departamento, unidad de medida… Hasta la 1.0.0-beta.1 el modelo los recibía solo como `String`
(`emissionTypeCode("1")`). Un código equivocado lo detectaba la validación, antes de firmar, pero el compilador y el
IDE no ayudaban y `"1"` no dice nada a quien lee el código.

Un `enum` resuelve eso, pero SIFEN agrega códigos por Nota Técnica: con solo `enum`, cada código nuevo exigiría una
versión del SDK, leer una respuesta con un código desconocido no tendría representación y agregar una constante
rompería los `switch` exhaustivos de quien usa Java 21. Algunos catálogos, además, son grandes y geográficos (6766
ciudades).

## Decisión

- Cada catálogo chico de `spec` declara `nombre` en inglés, y cada entrada el suyo (`emission_type`,
  `contingency`). Los catálogos grandes o geográficos (ciudad, distrito, país, moneda) y los que el emisor no
  carga no lo llevan.
- `scripts/generar-catalogos.py` genera un `enum` por catálogo con nombre en `io.github.sifensdk.core.catalog`
  (`EmissionType.CONTINGENCY`), con `code()`, `description()` (la descripción oficial que escribe el SDK) y
  `fromCode(String)` → `Optional`. Todos implementan `CatalogCode`. Los archivos generados se versionan y
  `CatalogEnumsTest` falla si dejan de coincidir con `spec`.
- Cada campo del modelo con catálogo tipado suma, **sin quitar nada**, un setter y un getter con el nombre del
  campo sin `Code`: `emissionType(EmissionType)` y `Optional<EmissionType> emissionType()`. El getter queda vacío
  si el código no está o el SDK no lo conoce. Los campos de moneda suman lo mismo con `java.util.Currency`.
  `TypedCatalogAccessorsTest` lo exige para todo campo.
- El `String` sigue siendo la vía para códigos que SIFEN agregue antes de que el SDK los conozca y para quien ya
  guarda los códigos en su base de datos.
- Las constantes de códigos que SIFEN dejó de aceptar quedan `@Deprecated`; las definidas pero aún no vigentes lo
  dicen en el Javadoc.

## Consecuencias

- Autocompletado, Javadoc con la descripción oficial y errores de tipeo detectados al compilar.
- Ningún cambio incompatible: los métodos `String` siguen iguales.
- Un código nuevo de SIFEN se puede usar enseguida con el `String`; cuando `spec` lo incorpora, el test obliga a
  regenerar y la constante llega en la versión siguiente.
- Los nombres de las constantes son API: desde la 1.0 no cambian.

## Alternativas descartadas

- **Solo `enum`, sin `String`:** cada código nuevo de SIFEN bloquearía a los integradores hasta una versión del
  SDK, y quitar los `String` rompería a quien ya los usa.
- **Clases con constantes y `of(String)` ("open enum"):** admiten códigos desconocidos, pero no sirven en `switch`
  y son menos naturales en Java que un `enum` con `fromCode` → `Optional`.
- **Nombres solo en el repositorio de Java:** un SDK en otro lenguaje tendría que inventar los suyos.
- **Generar en cada build:** un plugin más y código que no se ve en el repositorio.
