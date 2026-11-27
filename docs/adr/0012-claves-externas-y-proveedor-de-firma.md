# 0012 — Claves en token, HSM o firma remota, y el proveedor que firma con ellas

- **Estado:** Aceptada
- **Fecha:** 2026-11-27

## Contexto

Muchos certificados habilitados viven en un token o en un HSM, o los custodia el prestador o un servicio de firma
(ADR 0008). La clave no sale de ahí: el SDK no puede leerla, solo pedirle que firme. La misma clave firma el
documento (XMLDSig) y autentica la conexión TLS mutua con SIFEN.

Una prueba técnica (JDK 21, SoftHSM2 y un firmante en proceso) mostró:

- **XMLDSig** acepta el proveedor de la firma como propiedad del contexto
  (`org.jcp.xml.dsig.internal.dom.SignatureProvider`), así que firma con una clave de token o remota **sin
  instalar nada** en `Security`.
- **JSSE** (el TLS del JDK) busca la firma del handshake solo entre los proveedores **instalados**. Con una clave
  de un proveedor no instalado falla ("No supported CertificateVerify signature algorithm").
- **SIFEN test acepta solo TLS 1.2** y pide la firma del cliente con RSA y SHA-256 (PKCS#1 v1.5). TLS 1.3 exige
  RSA-PSS para el certificado del cliente.
- Si el proveedor rechaza un algoritmo al **iniciar** la firma, JSSE prueba el siguiente esquema. Si lo rechaza
  recién al firmar, el handshake falla.

## Decisión

- Una clave que no se puede leer se representa con una clave interna (`ExternalKey`) que lleva consigo **qué la
  firma**: el proveedor de su token (una instancia de SunPKCS11 que no se instala) o un `RemoteSigner`.
- Un único proveedor del SDK (`SifenSdkKeys-…`, con un nombre propio por cada copia del SDK cargada en la JVM)
  firma con esas claves y **solo** con ellas (`SupportedKeyClasses`). No guarda estado: todo lo que necesita viaja
  en la clave, así que un solo proveedor sirve a todos los emisores del proceso sin mezclarlos (ADR 0004).
- El proveedor abre la firma del token o del firmante al **iniciarla**, de modo que una clave que no puede firmar
  con un algoritmo se rechaza ahí y JSSE prueba otro esquema.
- **XMLDSig:** el SDK le pasa ese proveedor por la propiedad del contexto. No se instala nada.
- **TLS:** el cliente instala el proveedor una vez, al final de la lista, la primera vez que se conecta con una
  clave de este tipo. Es la única modificación global del SDK: aditiva, idempotente y sin datos de ningún emisor.
  Como solo acepta sus propias claves, no cambia cómo se firma con ninguna otra.
- **PKCS#11:** `KeyMaterials.pkcs11(Pkcs11Config, pin, alias)` configura SunPKCS11 para la biblioteca y la ranura
  del token sin instalarlo. Una clave no extraíble de un `KeyStore` cuyo proveedor no está instalado firma igual,
  con ese proveedor. Las claves de proveedores instalados (por ejemplo `Windows-MY`) se usan tal cual.
- **Firma remota:** `RemoteSigner` recibe solo el resumen SHA-256 de lo que se firma (32 bytes), nunca el
  documento. Cada firma se verifica con la clave pública del certificado antes de usarla. Firma con
  RSASSA-PKCS1-v1_5, lo que usan SIFEN y la mayoría de los servicios de firma (KMS, HSM en
  la nube). Con una clave que no firma RSA-PSS (un firmante remoto, o un token sin ese mecanismo), el cliente ofrece
  solo TLS 1.2.
- **Antes de firmar** se verifica el certificado: fuera de vigencia o sin uso de firma digital ni no repudio es
  error; un RUC distinto del emisor es solo advertencia (`CertificateChecks`), porque SIFEN acepta el certificado de
  una persona física autorizada por el emisor, que no lleva el RUC del emisor (evidencia E-001 y E-002 de `spec`).

## Consecuencias

- Documentos, eventos y TLS funcionan igual con la clave en un archivo, en un token, en un HSM o en un servicio
  remoto. RSASSA-PKCS1-v1_5 es determinista: la misma clave produce los mismos bytes por cualquier camino, y las
  pruebas lo comparan.
- Verificado contra SIFEN test el 2026-11-27 con el certificado de prueba copiado a un token SoftHSM2 y detrás de
  un firmante remoto en proceso: los mismos documentos y eventos, con los mismos resultados que con el archivo.
- El inicio de sesión de PKCS#11 es de todo el proceso: el token queda abierto mientras el proceso corre, y abrirlo
  otra vez en el mismo proceso no vuelve a pedir el PIN.
- El firmante remoto aplica su propio tiempo de espera; el SDK espera a que responda.
- El proveedor queda instalado mientras la JVM corre y mantiene cargada la copia del SDK que lo instaló (por
  ejemplo, al redesplegar una aplicación en un servidor).
- Cada llamada a `KeyMaterials.pkcs11` abre el token de nuevo: el material de claves se crea una vez y se reutiliza.
- Las pruebas de token usan SoftHSM2; la integración continua lo instala en Ubuntu. En Windows esas pruebas se
  omiten.

## Alternativas descartadas

- **Instalar el proveedor SunPKCS11 de cada token:** un proveedor global por token, con nombres que chocan y que
  puede quedar elegido para otras claves.
- **Un proveedor propio por emisor o por clave:** estado global que crece con cada emisor.
- **Exigir un certificado para TLS y otro para firmar:** SIFEN usa el mismo.
- **Mandar al firmante remoto los datos en lugar del resumen:** expone el contenido y muchos servicios (Azure Key
  Vault, por ejemplo) solo firman resúmenes.
- **Soportar RSA-PSS en el firmante remoto desde ya:** SIFEN no lo usa; se puede agregar después sin romper la
  interfaz.
