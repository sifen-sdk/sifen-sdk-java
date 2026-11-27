# Decisiones de arquitectura (ADR)

Cada decisión de diseño relevante queda registrada acá. Para cambiar una, se propone un ADR nuevo que la reemplace.
Las decisiones comunes a todos los lenguajes están en [`sifen-sdk/spec`](https://github.com/sifen-sdk/spec/tree/main/docs/adr);
estas son las propias del SDK de Java.

| N.º | Decisión | Estado |
|---|---|---|
| [0001](0001-java-17-un-artefacto-y-lineas-por-rama.md) | Java 17 como mínimo, un solo artefacto y líneas mayores por rama | Aceptada |
| [0002](0002-modulos-y-dependencias.md) | Módulos y dependencias: solo el JDK en `core` y `client` | Aceptada |
| [0003](0003-modelo-a-mano-con-nombres-en-ingles.md) | Modelo escrito a mano, inmutable, con nombres legibles en inglés | Aceptada |
| [0004](0004-sin-estado-global.md) | Sin estado global: un cliente por emisor | Aceptada |
| [0005](0005-xml-firmado-inmutable-y-etapas.md) | El XML firmado es inmutable; el flujo se expone por etapas | Aceptada |
| [0006](0006-resultados-y-errores.md) | El rechazo de SIFEN es un resultado; las excepciones son no comprobadas | Aceptada |
| [0007](0007-sin-reenvio-automatico.md) | Nunca reenviar automáticamente una recepción | Aceptada |
| [0008](0008-material-de-claves-por-interfaz.md) | Certificados y claves detrás de una interfaz | Aceptada |
| [0009](0009-build-publicacion-y-licencia.md) | Maven, Maven Central, Apache-2.0 y DCO | Aceptada |
| [0010](0010-semantica-de-la-verificacion-aritmetica.md) | Semántica de la verificación aritmética | Aceptada |
| [0011](0011-lo-que-completa-el-sdk.md) | Lo que completa el SDK y lo que trae el emisor | Aceptada |
| [0012](0012-claves-externas-y-proveedor-de-firma.md) | Claves en token, HSM o firma remota, y el proveedor que firma con ellas | Aceptada |

Plantilla: [0000-plantilla.md](0000-plantilla.md).
