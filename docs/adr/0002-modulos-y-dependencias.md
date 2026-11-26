# 0002 — Módulos y dependencias: solo el JDK en `core` y `client`

- **Estado:** Aceptada
- **Fecha:** 2026-11-26

## Contexto

Una librería que arrastra dependencias provoca conflictos de versiones en los sistemas que la usan. Además, no todo
integrador necesita todo: algunos solo generan y firman, otros no quieren PDF.

## Decisión

- `sifen-sdk-core` (modelo, XML, validación, CDC, firma, QR; sin red) y `sifen-sdk-client` (web services) dependen
  solo del JDK: `java.net.http`, `java.xml.crypto`, SunPKCS11 y `System.Logger`. JSpecify se usa solo como
  anotaciones (`requires static`).
- Lo opcional va en módulos aparte: `sifen-sdk-calc`, `sifen-sdk-kude` y `sifen-sdk-testing`.
- Un BOM (`sifen-sdk-bom`) alinea las versiones; lista solo módulos propios.
- Cada módulo es un módulo de Java (JPMS) que exporta solo su API pública.
- Una prueba verifica que `core` no requiera nada fuera del JDK.

## Consecuencias

- Cero conflictos de dependencias para quien use `core` y `client`.
- Algunas piezas se escriben sobre APIs del JDK menos cómodas que una librería externa.

## Alternativas descartadas

- **SLF4J, Apache Santuario o Bouncy Castle como dependencias obligatorias:** el JDK ya cubre lo necesario.
- **Un único artefacto:** obliga a todos a cargar PDF, cálculo y utilidades de prueba.
- **Empaquetar dependencias dentro del jar (shading):** oculta versiones vulnerables y duplica clases.
