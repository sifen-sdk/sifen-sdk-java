# SIFEN SDK para Java

[![CI](https://github.com/sifen-sdk/sifen-sdk-java/actions/workflows/ci.yml/badge.svg)](https://github.com/sifen-sdk/sifen-sdk-java/actions/workflows/ci.yml)
[![Licencia](https://img.shields.io/badge/licencia-Apache--2.0-blue.svg)](LICENSE)

SDK comunitario para integrar sistemas Java con **SIFEN**, el Sistema Integrado de Facturación Electrónica
Nacional de Paraguay. [English](README.en.md)

> **Estado: en construcción (fase 0).** Todavía no hay ninguna versión publicada ni funcionalidad utilizable.
> Este repositorio contiene la estructura del proyecto, sus decisiones de diseño y su integración continua.
> El avance se sigue en la [hoja de ruta](#hoja-de-ruta).

> Proyecto comunitario e independiente. **No está afiliado a la DNIT ni cuenta con su aval.** "SIFEN" y
> "e-Kuatia" se mencionan solo para describir con qué sistema se integra.

## Qué va a ser

Un SDK de **integración**, no un sistema de facturación:

- **Transporta, no calcula.** Envía los importes que tu sistema ya calculó, sin modificarlos ni redondearlos.
  Antes de enviar verifica que cumplan las fórmulas que SIFEN documenta y, si no, falla indicando el campo, la
  regla y dónde está documentada. El cálculo existe solo como módulo opcional que vos decidís invocar.
- **Exige exactamente lo que SIFEN documenta.** Un campo obligatorio se exige siempre; uno opcional, nunca; uno
  condicional, solo bajo la condición documentada. Cada regla cita su origen: Manual Técnico, Nota Técnica o
  esquema XSD.
- **Sin estado global.** Varios emisores, certificados y ambientes conviven en un mismo proceso.
- **El XML firmado no se toca.** Se firma una vez, lo recibís para guardarlo antes de enviar, y se envía tal cual.
- **Sin dependencias obligatorias** más allá del JDK en los módulos principales.

Las reglas, catálogos y casos de prueba viven como datos en
[`sifen-sdk/spec`](https://github.com/sifen-sdk/spec), para que los SDK de otros lenguajes se comporten igual.

## Módulos

| Módulo | Para qué |
|---|---|
| `sifen-sdk-core` | Modelo de documentos, XML, validación, CDC, firma y QR. Funciona sin red. |
| `sifen-sdk-client` | Web services de SIFEN: ambientes, TLS mutuo, resultados tipados. |
| `sifen-sdk-calc` | Cálculo de referencia, opcional. El núcleo nunca lo invoca. |
| `sifen-sdk-kude` | Representación gráfica (KuDE), opcional. |
| `sifen-sdk-testing` | SIFEN simulado y certificados sintéticos para tus pruebas. |
| `sifen-sdk-bom` | Alinea las versiones de todos los módulos. |

## Compatibilidad

| | |
|---|---|
| Java | 17 como mínimo. Un solo artefacto, probado en 17, 21 y 25. |
| SIFEN | Manual Técnico v150 y Notas Técnicas 1 a 27 (objetivo; ver hoja de ruta). |

La política de versiones y de soporte está en [docs/versiones-y-soporte.md](docs/versiones-y-soporte.md).

## Hoja de ruta

| Fase | Entrega | Estado |
|---|---|---|
| 0 | Fundaciones: repositorios, decisiones, integración continua | En curso |
| 1 | Especificación como datos (`spec`) | Pendiente |
| 2 | Núcleo: factura y nota de crédito → versión 0.1.0 | Pendiente |
| 3 | Cliente de web services → 0.2.0 | Pendiente |
| 4 | Resto de documentos y todos los eventos → 0.3.0 | Pendiente |
| 5 | Certificados en token, HSM y firma remota → 0.4.0 | Pendiente |
| 6 | Cálculo opcional, KuDE y sitio de documentación → 0.5.0 | Pendiente |
| 7 | Estabilización → 1.0.0 | Pendiente |

## Compilar

```bash
./mvnw verify
```

Requiere JDK 17 o superior (el formato del código se verifica con JDK 21 o superior).

## Participar

- Cómo contribuir: [CONTRIBUTING.md](CONTRIBUTING.md)
- Decisiones de diseño: [docs/adr](docs/adr/README.md)
- Reportar una vulnerabilidad: [SECURITY.md](SECURITY.md)
- Dudas y ayuda: [SUPPORT.md](SUPPORT.md)

## Aviso

Este software se entrega "tal cual", sin garantías. La responsabilidad por el contenido fiscal de cada documento
es de quien lo emite. Nunca subas a este repositorio, ni a sus issues, certificados, claves, códigos de seguridad
ni documentos reales.

## Licencia

[Apache-2.0](LICENSE). Creado y mantenido por Web Studio junto con la comunidad.
