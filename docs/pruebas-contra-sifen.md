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

## 3. Limpiar (opcional)

```bash
scripts/borrar-credenciales-sifen.sh
```

Sobrescribe y borra los archivos de `~/.config/sifen-sdk/` y los reportes de la prueba.
