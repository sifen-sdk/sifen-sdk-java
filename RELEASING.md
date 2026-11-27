# Publicar una versión

Las versiones se publican en Maven Central con el flujo [`publicar.yml`](.github/workflows/publicar.yml): compila,
corre todas las pruebas, firma con GPG, genera el inventario de dependencias (SBOM, `bom.json`) y la atestación de
procedencia, y sube a Central Portal. Corre en el entorno protegido `release` de GitHub: **no avanza hasta que una
persona con permiso lo aprueba**, y solo ese entorno tiene los secretos.

## Preparación única

Lo hace una persona con acceso de administración a la organización `sifen-sdk`. Ningún secreto pasa por el
repositorio, por issues ni por chats.

1. **Espacio de nombres.** En [central.sonatype.com](https://central.sonatype.com/) iniciar sesión con GitHub, ir a
   *Namespaces* → *Add namespace* → `io.github.sifen-sdk`. El portal pide crear un repositorio público con un nombre
   que indica (por ejemplo `OSSRH-123456`) en la organización `sifen-sdk`; crearlo vacío, pulsar *Verify* y, ya
   verificado, borrarlo.
2. **Token.** En el portal, *Account* → *Generate User Token*. Da un usuario y una contraseña de token.
3. **Clave GPG del proyecto** (en una máquina de confianza):

   ```bash
   gpg --quick-gen-key "SIFEN SDK releases <releases@sifen-sdk.invalid>" ed25519 sign 3y
   gpg --list-secret-keys --keyid-format long          # anotar el id de la clave (después de "ed25519/")
   gpg --keyserver keys.openpgp.org --send-keys <ID>
   gpg --keyserver keyserver.ubuntu.com --send-keys <ID>
   gpg --armor --export-secret-keys <ID> > clave-privada.asc   # solo para cargarla en el paso 4; borrarla después
   ```

   Guardar la frase de paso y una copia de la clave privada fuera de línea.
4. **Entorno `release`.** En GitHub, *Settings* → *Environments* → *New environment* → `release`:
   - *Required reviewers*: quienes aprueban las publicaciones. Activar *Prevent self-review* si son dos o más.
   - *Deployment branches and tags*: solo etiquetas `v*` y la rama `main`.
   - *Environment secrets*:

     | Secreto | Valor |
     |---|---|
     | `MAVEN_CENTRAL_USERNAME` | El usuario del token (paso 2) |
     | `MAVEN_CENTRAL_TOKEN` | La contraseña del token (paso 2) |
     | `GPG_PRIVATE_KEY` | El contenido de `clave-privada.asc` |
     | `GPG_PASSPHRASE` | La frase de paso de la clave |

   Después, borrar `clave-privada.asc` del disco.
5. **Etiqueta `api-incompatible`** en el repositorio (*Issues* → *Labels*), para los PR que cambian la API a
   propósito (ver abajo).

## Ensayo (sin publicar)

Antes de la primera versión, y cada vez que cambie algo del proceso:

1. `main` tiene la versión a publicar, sin `-SNAPSHOT`.
2. *Actions* → *Publicar* → *Run workflow* en `main`, con *Ensayo* marcado. Aprobarlo.
3. En el portal, *Deployments*: el despliegue debe quedar **VALIDATED** (firmas, POM, fuentes y Javadoc correctos).
   Revisarlo y pulsar **Drop**. Nada se publicó.

## Publicar una versión

1. La integración continua está en verde en `main`.
2. Un PR de versión:
   - pone la versión en todos los `pom.xml` (`1.0.0-beta.N`, `1.0.0-rc.N` o `1.0.0`);
   - actualiza `project.build.outputTimestamp` a la fecha y hora de la versión;
   - en `CHANGELOG.md` pasa `[Unreleased]` a `[versión] - fecha`, con los cambios incompatibles marcados;
   - fusionado en `main`.
3. Etiquetar ese commit y empujar la etiqueta: `git tag -s v1.0.0-beta.N -m "1.0.0-beta.N"` y
   `git push origin v1.0.0-beta.N`.
4. Aprobar el despliegue en *Actions*. El flujo verifica que la etiqueta coincida con la versión, publica y crea el
   release en GitHub (marcado como pre-release si la versión lleva `-`), con los jar y el SBOM.
5. Verificar en [Maven Central](https://central.sonatype.com/namespace/io.github.sifen-sdk) que aparecen los
   artefactos (puede tardar hasta 30 minutos) y que un proyecto limpio los descarga y compila.
6. Un PR posterior:
   - vuelve a `-SNAPSHOT` (por ejemplo `1.0.0-beta.(N+1)-SNAPSHOT`);
   - fija `api.baseline` en la versión recién publicada;
   - la primera vez, pone `japicmp.skip` en `false` para que la CI compare la API desde entonces.

Una publicación en Maven Central **no se puede borrar ni reemplazar**. Si salió mal, se publica una versión nueva.

## Cambios de API

Desde la `1.0.0-beta.1`, la CI compara la API pública de cada módulo con la última versión publicada (japicmp). Un
cambio incompatible hace fallar el PR. Si es intencional:

- se anota en `CHANGELOG.md`, en la sección del cambio, como incompatible;
- el PR lleva la etiqueta `api-incompatible`.

Durante la beta solo se aceptan para corregir un problema real; desde la `1.0.0`, solo en una versión mayor.

## Probar el empaquetado localmente

```bash
./mvnw -Prelease -Dgpg.skip verify
```

Genera, para cada módulo, el jar, las fuentes, el Javadoc y el SBOM tal como los exige Maven Central, sin firmar ni
subir.
