# SIFEN SDK para Java

SDK comunitario para integrar sistemas Java con **SIFEN**, el Sistema Integrado de Facturación Electrónica Nacional
de Paraguay.

Valida, firma y envía a SIFEN los documentos electrónicos (factura, autofactura, notas de crédito y de débito, nota
de remisión) y los 8 eventos que envía un contribuyente. Consulta lotes, documentos y RUC.

!!! note "Estado"
    En desarrollo, sin versión publicada en Maven Central. La API puede cambiar antes de la 1.0
    ([versiones y soporte](versiones-y-soporte.md)).

!!! info "Proyecto comunitario"
    Proyecto independiente. **No está afiliado a la DNIT ni cuenta con su aval.** "SIFEN" y "e-Kuatia" se
    mencionan solo para describir con qué sistema se integra.

## Integración, no facturación

El SDK lleva a SIFEN lo que tu sistema ya decidió. No es un sistema de facturación ni una calculadora:

- **Transporta, no calcula.** Envía los importes tal como los calculó tu sistema, sin redondearlos ni
  corregirlos. Antes de firmar verifica que cumplan las fórmulas que SIFEN documenta y, si no, te dice el campo, la
  regla, el código de rechazo y dónde está documentada.
- **Exige exactamente lo que SIFEN documenta.** Un campo obligatorio se exige siempre; uno opcional, nunca; uno
  condicional, solo bajo su condición. Cada regla cita su fuente: Manual Técnico, Nota Técnica o esquema XSD
  ([referencia de reglas](reglas/index.md)).
- **Sin estado global.** Varios emisores, certificados y ambientes conviven en un mismo proceso
  ([ADR 0004](adr/0004-sin-estado-global.md)).
- **El XML firmado no se toca.** Se firma una vez, lo guardás y se envían esos mismos bytes
  ([ADR 0005](adr/0005-xml-firmado-inmutable-y-etapas.md)).
- **Nunca reenvía solo.** Si la red falla después de enviar, te lo dice y vos consultás antes de reenviar
  ([ADR 0007](adr/0007-sin-reenvio-automatico.md)).
- **Solo el JDK.** `core` y `client` no tienen dependencias externas
  ([ADR 0002](adr/0002-modulos-y-dependencias.md)).

Lo mecánico sí lo completa: el CDC y su dígito verificador, las descripciones de catálogo, la fecha de firma, la
firma y el QR ([ADR 0011](adr/0011-lo-que-completa-el-sdk.md)).

## Módulos

| Módulo | Para qué |
|---|---|
| `sifen-sdk-core` | Modelo de documentos y eventos, XML, validación, CDC, firma y QR. Funciona sin red. |
| `sifen-sdk-client` | Web services de SIFEN: ambientes, TLS mutuo, resultados tipados. |
| `sifen-sdk-calc` | Cálculo de referencia opcional. El núcleo nunca lo invoca ([guía](guias/calculo.md)). |
| `sifen-sdk-testing` | SIFEN simulado y certificados sintéticos para tus pruebas. |
| `sifen-sdk-bom` | Alinea las versiones de todos los módulos. |

La representación gráfica (KuDE) no es parte del SDK: su diseño es de cada empresa. El SDK te da lo que necesitás
para armarla ([guía](guias/kude.md)).

## Por dónde seguir

- [Primeros pasos](primeros-pasos.md): dependencia, validar, firmar y enviar.
- [Documentos](guias/documentos.md), [eventos](guias/eventos.md) y [envío y consultas](guias/envio-y-consultas.md).
- [Referencia de reglas](reglas/index.md): qué exige SIFEN de cada campo, con su fuente.
- [Decisiones](adr/README.md): por qué el SDK funciona así.
- [API](https://sifen-sdk.github.io/sifen-sdk-java/api/): el Javadoc de todos los módulos.

Las reglas, catálogos y casos de prueba viven como datos en [`sifen-sdk/spec`](https://github.com/sifen-sdk/spec),
para que los SDK de otros lenguajes se comporten igual.
