# SIFEN SDK para Java

[![CI](https://github.com/sifen-sdk/sifen-sdk-java/actions/workflows/ci.yml/badge.svg)](https://github.com/sifen-sdk/sifen-sdk-java/actions/workflows/ci.yml)
[![Licencia](https://img.shields.io/badge/licencia-Apache--2.0-blue.svg)](LICENSE)

SDK comunitario para integrar sistemas Java con **SIFEN**, el Sistema Integrado de Facturación Electrónica
Nacional de Paraguay. [English](README.en.md)

> **Estado: en desarrollo (fase 2), sin versión publicada.** El núcleo ya valida, firma y genera el QR de facturas
> y notas de crédito, sin red. Todavía no envía a SIFEN (fase 3) ni está probado contra el ambiente de test.
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

## Uso

```java
SifenDocuments documents = SifenDocuments.builder()
    .environment(Environment.TEST)
    .build();                                   // inmutable: creá una por ambiente y reutilizala

ElectronicDocument invoice = ElectronicDocument.builder()
    // timbrado, emisor, receptor, ítems y TODOS los importes, tal como los calculó tu sistema
    .build();

ValidationResult result = documents.validate(invoice);
result.errors().forEach(System.out::println);
// ERROR rDE/DE/gTotSub/dTotGralOpe (dTotGralOpe, F014): ... [R-F014-A3, SIFEN 2365, MT150 2365]

KeyMaterial certificate = KeyMaterials.pkcs12(Path.of("certificado.p12"), password);
Csc csc = Csc.of("0001", secret);
SignedDocument signed = documents.sign(invoice, certificate, csc);

store(signed.cdc(), signed.xml());              // guardalo antes de enviarlo; se envían esos mismos bytes
```

- **No toca tus importes.** Si un total no cumple la fórmula de SIFEN, te lo informa con el campo, la regla, el
  código de rechazo y la fuente; no lo corrige.
- **Completa lo mecánico:** el CDC y su dígito verificador, las descripciones de catálogo, la fecha de firma, la
  firma y el QR ([ADR 0011](docs/adr/0011-lo-que-completa-el-sdk.md)).
- **Reglas por fecha y ambiente:** aplica las vigentes en la fecha de emisión del documento, según cada Nota Técnica.

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
| 0 | Fundaciones: repositorios, decisiones, integración continua | Hecha |
| 1 | Especificación como datos (`spec`) | Hecha |
| 2 | Núcleo: factura y nota de crédito → versión 0.1.0 | En curso |
| 3 | Cliente de web services → 0.2.0 | Pendiente |
| 4 | Resto de documentos y todos los eventos → 0.3.0 | Pendiente |
| 5 | Certificados en token, HSM y firma remota → 0.4.0 | Pendiente |
| 6 | Cálculo opcional, KuDE y sitio de documentación → 0.5.0 | Pendiente |
| 7 | Estabilización → 1.0.0 | Pendiente |

## Compilar

```bash
git clone --recurse-submodules https://github.com/sifen-sdk/sifen-sdk-java.git
./mvnw verify
```

Requiere JDK 17 o superior (el formato del código se verifica con JDK 21 o superior). La especificación se incluye
como submódulo (`spec/`); después de actualizarlo, ejecutá `scripts/spec-index.sh`.

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
