# KuDE

El KuDE es la representación gráfica de un documento electrónico: lo que se imprime o se manda en PDF al cliente.
No reemplaza al XML; el documento válido es el XML firmado y aprobado por SIFEN.

## El SDK no lo dibuja

El diseño del KuDE es de cada empresa: su logo, su tipografía, su formato de papel, su sistema de reportes. Por eso
el SDK no genera PDF ni imágenes ([ADR 0002](../adr/0002-modulos-y-dependencias.md)):

- Una plantilla fija no le sirve a nadie tal cual, y una configurable termina siendo un generador de reportes.
- Generar PDF exige dependencias externas, que `core` y `client` no tienen.
- Tu sistema ya tiene cómo generar documentos (JasperReports, una plantilla HTML, la impresora fiscal de tu punto
  de venta). Usalo.

Lo que el SDK te da es lo que no podés calcular vos: el CDC, la URL del QR y el XML firmado.

```java
SignedDocument signed = documents.sign(invoice, certificate, csc);

signed.cdc();                // 44 dígitos
signed.qrUrl();              // la URL que va dentro del QR, completa
signed.signatureDateTime();  // fecha y hora de firma
signed.xml();                // el XML firmado, para adjuntarlo o leerlo
```

El resto de los datos ya los tenés: son los del `ElectronicDocument` que armaste.

## Qué lleva un KuDE

Según el Manual Técnico de SIFEN, el KuDE muestra los datos del documento. Lo habitual:

- **Emisor:** razón social, RUC con dígito verificador, dirección, teléfono, correo y actividad económica.
- **Timbrado:** número y fecha de inicio de vigencia.
- **Documento:** tipo (factura electrónica, nota de crédito electrónica, …), número con establecimiento y punto de
  expedición (`001-001-0000001`), fecha y hora de emisión.
- **Receptor:** nombre o razón social y RUC, o documento de identidad.
- **Operación:** condición (contado o crédito), moneda y tipo de cambio si no es guaraníes.
- **Ítems:** código, descripción, unidad, cantidad, precio unitario, descuento y valor de venta separado por tasa
  (exentas, 5 %, 10 %).
- **Totales:** subtotales por tasa, total de la operación, total en guaraníes si la moneda es otra, y liquidación
  del IVA (5 %, 10 % y total).
- **QR**, con la URL de `signed.qrUrl()`.
- **CDC**, en grupos de cuatro caracteres (sección 10.1 del Manual).
- **Leyendas:** que se puede consultar la validez del documento con su CDC en el portal de e-Kuatia, y que es la
  representación gráfica de un documento electrónico (XML).

Y, cuando corresponde:

- **Información de interés del fisco** (`taxAuthorityInformation`, B006): si el documento la lleva, el Manual pide
  imprimirla.
- **Información adicional del emisor** (J003): aclaraciones para tus clientes. Puede ir en el KuDE, pero no forma
  parte del documento que guarda SIFEN.
- **Nota de remisión:** datos del traslado (motivo, responsable, transporte, vehículo y transportista).
- **Notas de crédito y débito:** el documento al que se refieren.

El formato exacto (qué datos son obligatorios en el KuDE, leyendas textuales, formatos de fecha) está en el Manual
Técnico y sus Notas Técnicas. Por ejemplo, la Nota Técnica 10 cambia el formato de la fecha de inicio del timbrado
en el KuDE a `DD-MM-AAAA`. Revisalos al diseñar el tuyo.

## El CDC en grupos de cuatro

```java
String cdcForPrinting = signed.cdc().replaceAll("(.{4})(?!$)", "$1 ");
// 0144 4444 0170 0100 1001 4528 2201 7012 5158 7326 0988
```

## El QR

`qrUrl()` es la URL completa, con los parámetros y el hash que exige SIFEN. Codificala tal cual en el QR con la
biblioteca que prefieras (por ejemplo, ZXing). No la armes ni la modifiques: el hash depende de cada parámetro.

## Documentos firmados antes

Si guardaste solo el XML firmado, el CDC y la URL del QR están adentro: el CDC es el atributo `Id` de `DE` y la URL
está en `dCarQR`, dentro de `gCamFuFD`.
