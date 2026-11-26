# 0008 — Certificados y claves detrás de una interfaz

- **Estado:** Aceptada
- **Fecha:** 2026-11-26

## Contexto

Los certificados habilitados en Paraguay pueden estar en un archivo, en un token o HSM, o custodiados por el
prestador (firma remota). La misma clave firma el documento y autentica la conexión TLS.

## Decisión

- La firma y la autenticación TLS se piden a una interfaz de material de claves, no a un archivo.
- Implementaciones previstas: PKCS#12 (ruta, bytes o flujo), `KeyStore` con alias, PEM, PKCS#11 y firmante remoto
  (experimental).
- La clave privada nunca se exporta ni se copia; las contraseñas no se guardan en `String` de larga vida ni
  aparecen en `toString()`.
- Antes de usar un certificado se puede verificar su vigencia, su uso permitido y que su RUC sea el del emisor.

## Consecuencias

- Los mismos pasos sirven para archivo, token y HSM.
- La firma remota depende de cada prestador; se ofrece la interfaz y las implementaciones llegan por contribución.

## Alternativas descartadas

- **Solo un archivo `.p12` por ruta:** deja afuera tokens, HSM y firma remota, y obliga a escribir el certificado
  en disco.
