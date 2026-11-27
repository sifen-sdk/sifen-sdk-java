# Certificados en token, HSM y firma remota

El SDK firma documentos y eventos, y autentica el TLS mutuo con SIFEN, con un `KeyMaterial`. Este puede tener la
clave en un archivo o en un lugar del que nunca sale: un token, un HSM o un servicio de firma remota. Los pasos son
los mismos en todos los casos (ADR [0008](adr/0008-material-de-claves-por-interfaz.md) y
[0012](adr/0012-claves-externas-y-proveedor-de-firma.md)).

```java
KeyMaterial certificate = /* cualquiera de las opciones de abajo */;
SignedDocument signed = documents.sign(invoice, certificate, csc);
SifenClient client = SifenClient.builder().environment(Environment.TEST).keyMaterial(certificate).build();
```

## Token o HSM (PKCS#11)

Los certificados en token (tipo A3) y los HSM se usan con la biblioteca PKCS#11 del fabricante:

```java
KeyMaterial certificate = KeyMaterials.pkcs11(
    Pkcs11Config.library(Path.of("/usr/lib/libeToken.so")),  // en Windows, por ejemplo C:\Windows\System32\eTPKCS11.dll
    pin);                                                     // char[]; borralo después con Arrays.fill
```

- **Ranura:** si la biblioteca tiene varias, elegila con `Pkcs11Config.slot(biblioteca, idDeRanura)` o
  `Pkcs11Config.slotIndex(biblioteca, posición)`. `pkcs11-tool -L` (OpenSC) las lista.
- **Varias claves en el token:** pasá el alias, `KeyMaterials.pkcs11(config, pin, "alias")`. Si no lo pasás y hay
  más de una, el error lista los alias disponibles.
- **Configuración avanzada:** `Pkcs11Config.configFile(ruta)` acepta un archivo de configuración de SunPKCS11.
- **El PIN** se usa solo para iniciar sesión y no se guarda. El inicio de sesión de PKCS#11 es de todo el proceso:
  el token queda abierto mientras el proceso corre.
- **Errores claros:** biblioteca inexistente, ranura sin token, PIN incorrecto o bloqueado, alias inexistente. Los
  mensajes nunca incluyen el PIN.

El SDK usa el proveedor SunPKCS11 del JDK sin instalarlo. Si ya tenés un `KeyStore` de PKCS#11 (o el almacén de
Windows, `Windows-MY`), `KeyMaterials.keyStore(almacén, alias, null)` también sirve.

## Firma remota

Si la clave la custodia un servicio (un KMS o HSM en la nube, o el prestador de certificación), implementá
`RemoteSigner`:

```java
KeyMaterial certificate = KeyMaterials.remote(new RemoteSigner() {
  @Override public List<X509Certificate> certificateChain() {
    return cadena;                                   // el certificado del firmante primero
  }
  @Override public byte[] signSha256Digest(byte[] sha256Digest) {
    return servicio.firmar(sha256Digest);            // RSASSA-PKCS1-v1_5 sobre el resumen SHA-256
  }
});
```

- **El servicio solo recibe un resumen SHA-256 de 32 bytes**, nunca el documento ni sus datos.
- La firma es RSASSA-PKCS1-v1_5 con SHA-256. En AWS KMS es `RSASSA_PKCS1_V1_5_SHA_256` con `MessageType=DIGEST`; en
  Azure Key Vault, `RS256`.
- La implementación debe ser segura entre hilos y aplicar su propio tiempo de espera.
- Si el servicio falla, `sign` lanza `SigningException` con la excepción del servicio como causa. Toda firma se
  verifica antes de devolverla.
- La conexión con SIFEN usa TLS 1.2 (SIFEN no ofrece TLS 1.3).

## Verificación del certificado

Antes de firmar, `SifenDocuments.sign` y `SifenEvents.sign` rechazan un certificado vencido, todavía no vigente o
sin uso de firma digital ni de no repudio. `CertificateChecks.check(certificado, instante, rucDelEmisor)` informa
además, como advertencia, si el certificado tiene un RUC distinto del emisor. No es un error: SIFEN acepta el
certificado de una persona autorizada por el emisor, que lleva su propia identidad y no el RUC del emisor.

## Probar sin hardware

[SoftHSM2](https://github.com/softhsm/SoftHSMv2) es un token PKCS#11 por software. Las pruebas del SDK lo usan, y
`scripts/prueba-sifen-token.sh` corre las pruebas en vivo contra SIFEN test con el certificado de prueba copiado a
un token temporal (ver [pruebas contra SIFEN](pruebas-contra-sifen.md)).
