# Material de claves sintético para pruebas

**Todo lo de esta carpeta es sintético.** Son certificados autofirmados generados con `keytool` del JDK solo para
las pruebas del SDK. No los emitió ningún prestador, no identifican a nadie y SIFEN los rechaza. Nunca los uses
fuera de las pruebas ni agregues acá material de claves real.

| Archivo | Contenido |
|---|---|
| `prueba.p12` | PKCS#12 con un alias (`firma`): clave RSA 2048 y certificado autofirmado. |
| `dos-alias.p12` | PKCS#12 con dos alias con clave (`firma` y `otra`), para probar el error de alias ambiguo. |
| `prueba-certificado.pem` | El certificado de `prueba.p12` en PEM. |
| `prueba-clave.pem` | La clave privada de `prueba.p12` en PEM, PKCS#8 sin cifrar. |

- **Contraseña de prueba** (del almacén y de las claves): `prueba-sintetica`
- **Sujeto:** `CN=CONTRIBUYENTE SINTETICO DE PRUEBA, SERIALNUMBER=RUC44444401-7, O=SIFEN SDK PRUEBAS, C=PY`
  (el RUC 44444401-7 es el del ejemplo del Manual Técnico v150).
- **Uso de clave:** firma digital y no repudio (crítico). **Vigencia:** 2026-01-01 a 2045-12-27.

Cómo se generaron:

```bash
keytool -genkeypair -alias firma -keyalg RSA -keysize 2048 -sigalg SHA256withRSA \
  -dname "CN=CONTRIBUYENTE SINTETICO DE PRUEBA, SERIALNUMBER=RUC44444401-7, O=SIFEN SDK PRUEBAS, C=PY" \
  -ext KeyUsage:critical=digitalSignature,nonRepudiation -validity 7300 -startdate 2026/01/01 \
  -storetype PKCS12 -keystore prueba.p12 -storepass prueba-sintetica -keypass prueba-sintetica
# dos-alias.p12: copia de prueba.p12 más un segundo -genkeypair con -alias otra
#   y -dname "CN=SEGUNDA CLAVE SINTETICA, SERIALNUMBER=RUC44444401-7, O=SIFEN SDK PRUEBAS, C=PY"
openssl pkcs12 -in prueba.p12 -passin pass:prueba-sintetica -nokeys -clcerts | openssl x509 -out prueba-certificado.pem
openssl pkcs12 -in prueba.p12 -passin pass:prueba-sintetica -nocerts -nodes | openssl pkey -out prueba-clave.pem
```

`rde-sin-firmar.xml` es un `rDE` sintético mínimo (no completo según el XSD) con los datos del ejemplo del Manual,
para probar la firma y el QR.
