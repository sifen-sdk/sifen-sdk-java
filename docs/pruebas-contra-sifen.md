# Pruebas contra el ambiente de test de SIFEN

La prueba `LiveSifenTest` emite documentos en el **ambiente de test** de SIFEN: consulta el RUC del emisor, envía
una factura por el servicio síncrono, otra por lote y, si se pide, espera y consulta el resultado del lote y el
documento. Confirma que el SDK funciona contra el SIFEN real (firma, TLS mutuo, formatos).

No forma parte de la integración continua: necesita un certificado de firma, un timbrado y un CSC de test, que nunca
van al repositorio. Se corre de forma local con tus propias credenciales de test.

- La prueba solo apunta al ambiente de test (`Environment.TEST` está fijo en el código) y solo imprime estados,
  códigos y mensajes de SIFEN.
- Nunca pegues el certificado, su contraseña ni el CSC en un issue, un commit ni un registro.

## 1. Configurar

Guardá el certificado y dos archivos de configuración fuera del repositorio, por ejemplo en `~/.config/sifen-sdk/`.

`~/.config/sifen-sdk/test.env`:

```bash
SIFEN_TEST_P12_PATH=$HOME/.config/sifen-sdk/certificado.p12
SIFEN_TEST_P12_PASSWORD='la contraseña del .p12'
SIFEN_TEST_CSC_ID=0001
SIFEN_TEST_CSC='el CSC de test'
SIFEN_TEST_ISSUER_FILE=$HOME/.config/sifen-sdk/emisor.properties
SIFEN_TEST_WAIT=true
```

`~/.config/sifen-sdk/emisor.properties`, con los datos tal como figuran en el RUC y el timbrado de test:

```properties
ruc=80000000
dv=0
tipoContribuyente=2
timbrado=12345678
timbradoInicio=2026-01-01
establecimiento=001
puntoExpedicion=001
direccion=Calle 1
numeroCasa=0
departamento=1
distrito=1
ciudad=1
telefono=021000000
email=facturacion@example.com
actividad=62010
actividadDescripcion=Descripción de la actividad tal como figura en el RUC
```

Después: `chmod 600 ~/.config/sifen-sdk/*`. El script se niega a correr si algún archivo lo pueden leer otros
usuarios o si está dentro del repositorio.

`tipoContribuyente`: 1 persona física, 2 persona jurídica. Departamento, distrito y ciudad son los códigos de la
tabla geográfica de SIFEN. El nombre del emisor no se configura: en test SIFEN exige un literal fijo, que pone la
prueba.

## 2. Correr la prueba

```bash
scripts/prueba-sifen-local.sh
```

Con `SIFEN_TEST_WAIT=true` tarda unos 12 minutos (espera lo que pide la Guía de mejores prácticas antes de consultar
el lote). Maven corre sin conexión (`-o`), así que mientras las credenciales están cargadas solo se ejecutan
herramientas ya descargadas.

Los números de documento se derivan del minuto actual, así que cada corrida usa números nuevos del timbrado de test.

## 3. Probar las fórmulas de importes (opcional)

`LiveArithmeticProbeTest` envía al ambiente de test un documento por cada regla aritmética, cada uno con un solo
importe desplazado en una cantidad conocida, e imprime qué respondió SIFEN. Sirve para comprobar que el SDK exige lo
mismo que SIFEN; el último resultado está en la evidencia E-001 de `spec`. Envía más de cien documentos de a uno.

```bash
set -a; . ~/.config/sifen-sdk/test.env; set +a
SIFEN_TEST_PROBE=true ./mvnw -o -B -ntp -pl sifen-sdk-client -am verify \
  -Dtest=LiveArithmeticProbeTest -Dsurefire.failIfNoSpecifiedTests=false -Dsurefire.useFile=false
```

Con `SIFEN_TEST_PROBE_ONLY='<expresión regular>'` corre solo los casos cuyo nombre coincide (`nc` prueba el límite de
las notas de crédito).

Los eventos se prueban igual con `LiveEventsTest` y `SIFEN_TEST_EVENTS=true`: emite unas facturas nuevas y sobre
ellas envía cada evento (cancelación, inutilización de dos números, nominación de una factura innominada y los cuatro
del receptor).

Los importes del cálculo opcional se prueban con `LiveCalcTest` y `SIFEN_TEST_CALC=true`: envía facturas cuyos
importes completó `sifen-sdk-calc`, en guaraníes (con descuentos, descuento global, anticipos, todas las afectaciones
de IVA y el redondeo a 50) y en dólares (con un tipo de cambio global y con uno por ítem). El 2026-11-27 SIFEN aprobó
las cuatro.

Dos comportamientos intermitentes del ambiente de test, observados el 2026-11-27: la consulta de RUC respondió
alguna vez 0160 ("XML mal formado") a un pedido correcto, y un evento sobre una factura aprobada segundos antes fue
rechazado con 4152 ("CDC del DTE es inexistente"), aunque otro evento sobre esa misma factura se había registrado.
En ambos casos, repetir más tarde funcionó. El SDK nunca reintenta solo.

## 4. Con la clave en un token o detrás de un firmante remoto (opcional)

```bash
scripts/prueba-sifen-token.sh pkcs11 LiveSifenTest       # el certificado, copiado a un token SoftHSM2 temporal
scripts/prueba-sifen-token.sh remote LiveEventsTest      # el certificado detrás de un firmante remoto en proceso
```

Con `pkcs11` el script copia el certificado de prueba a un token SoftHSM2 en un directorio privado (en memoria si
existe `/dev/shm`), con un PIN aleatorio, y lo borra al terminar. Hace falta SoftHSM2 (`sudo apt install softhsm2`).
Las variables de cada prueba (`SIFEN_TEST_EVENTS=true`, `SIFEN_TEST_DOCUMENTS=true`, …) se pasan igual. El
2026-11-27, la suite de documentos y la de eventos dieron por los dos caminos los mismos resultados que con el
archivo. Una cancelación respondió una vez 0100 ("Error Inesperado") y pasó al repetirla.

SIFEN test acepta solo TLS 1.2 y pide la firma del cliente con RSA y SHA-256.

## 5. Limpiar (opcional)

```bash
scripts/borrar-credenciales-sifen.sh
```

Sobrescribe y borra los archivos de `~/.config/sifen-sdk/` y los reportes de la prueba.
