# 0011 — Lo que completa el SDK y lo que trae el emisor

- **Estado:** Aceptada
- **Fecha:** 2026-11-26

## Contexto

"No calcular" se refiere a los importes. Hay otros campos que se derivan de forma mecánica de datos que ya están
en el documento o de la especificación, y pedírselos al emisor solo agrega formas de equivocarse.

## Decisión

El SDK completa, sin intervención del emisor:

- **Estructurales:** la versión del formato (`dVerFor`), el CDC (`Id`) y su dígito verificador (`dDVId`), a partir
  de los campos que lo componen; `dSisFact` mientras el XSD lo exija (contradicción C-103 de `spec`).
- **Descripciones de catálogo** (`dDes…`): la que corresponde al código, tal como la exige el XSD. Si el código
  admite una descripción libre (por ejemplo "Otro"), la trae el emisor.
- **Firma:** la fecha y hora de firma (`dFecFirma`), la firma digital y el QR (`gCamFuFD`).

El emisor trae todo lo demás, en particular **todos los importes, subtotales e impuestos** y el código de
seguridad (`dCodSeg`), para el que el SDK ofrece un generador opcional.

Los elementos que la especificación documenta pero el XSD vigente no admite no se emiten (manda el XSD en
estructura).

## Consecuencias

- Menos campos para el emisor y ninguna discrepancia posible entre un código y su descripción.
- El CDC siempre coincide con los datos del documento.

## Alternativas descartadas

- **Que el emisor cargue también descripciones y CDC:** más trabajo y más rechazos, sin beneficio.
