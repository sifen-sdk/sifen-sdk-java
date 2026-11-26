# Pruebas contra el ambiente de test de SIFEN

La prueba `LiveSifenTest` emite documentos reales en el **ambiente de test** de SIFEN: consulta el RUC del emisor,
envía una factura por el servicio síncrono, otra por lote y, si se pide, espera y consulta el resultado del lote y
el documento. Es lo único que confirma que el SDK funciona contra el SIFEN real (firma, TLS mutuo, formatos) y
sirve para resolver las contradicciones abiertas de la especificación.

## Seguridad

- Solo corre **a mano o una vez por semana, sobre `main`**, dentro del entorno protegido `sifen-test` de GitHub.
  Nunca corre en pull requests, así que nadie puede leer los secretos desde un fork.
- Los registros de Actions de un repositorio público **son públicos**. La prueba solo imprime estados, códigos y
  mensajes de SIFEN; nunca certificados, claves, CSC ni el contenido de los documentos. No agregues impresiones.
- Usá exclusivamente credenciales del **ambiente de test**. Nunca de producción.

## Preparación (una vez)

1. En GitHub: **Settings → Environments → New environment** → `sifen-test`. Recomendado: en "Deployment
   branches" elegí `main` solamente y agregate como revisor obligatorio.
2. En ese entorno, cargá estos **secretos**:

| Secreto | Contenido |
|---|---|
| `SIFEN_TEST_P12_BASE64` | El certificado `.p12` del emisor en Base64 (`base64 -w0 certificado.p12`) |
| `SIFEN_TEST_P12_PASSWORD` | Su contraseña |
| `SIFEN_TEST_CSC_ID` | El id del CSC de test (por ejemplo `0001`) |
| `SIFEN_TEST_CSC` | El CSC de test |
| `SIFEN_TEST_ISSUER` | Los datos del emisor en formato `clave=valor`, uno por línea (ver abajo) |

`SIFEN_TEST_ISSUER`, con los datos tal como figuran en el RUC y el timbrado de test:

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

`tipoContribuyente`: 1 persona física, 2 persona jurídica. Departamento, distrito y ciudad son los códigos de la
tabla geográfica de SIFEN. El nombre del emisor no se configura: en test SIFEN exige un literal fijo, que el SDK
pone.

## Correrla

- **Desde GitHub:** Actions → *SIFEN test environment* → *Run workflow* (con "Wait" marcado, tarda unos 12
  minutos).
- **En tu máquina:** exportá las mismas variables de entorno y ejecutá
  `./mvnw -pl sifen-sdk-client -am verify -Dtest=LiveSifenTest -Dsurefire.failIfNoSpecifiedTests=false`.

Los números de documento se derivan del minuto actual, así que cada corrida usa números nuevos del timbrado de test.
