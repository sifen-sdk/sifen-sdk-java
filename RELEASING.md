# Publicar una versión

> Todavía no se publicó ninguna versión. Este documento describe el proceso previsto y lo que falta preparar.

## Preparación única (pendiente)

1. Verificar el espacio de nombres `io.github.sifen-sdk` en el [Central Portal](https://central.sonatype.com/)
   de Sonatype (pide crear un repositorio público temporal en la organización).
2. Generar un token de publicación del Central Portal.
3. Crear una clave GPG del proyecto y publicar su parte pública en un servidor de claves.
4. Crear en GitHub el entorno protegido `release` y cargar ahí, como secretos, el token y la clave GPG.
   Nunca en el repositorio.
5. Agregar el flujo de publicación (release-please + `central-publishing-maven-plugin`) que use ese entorno.

## Probar el empaquetado sin publicar

```bash
./mvnw -Prelease -Dgpg.skip verify
```

Genera, para cada módulo, el jar, las fuentes y el Javadoc tal como los exige Maven Central, sin firmar ni subir.

## En cada versión (cuando exista el flujo)

1. La integración continua está en verde en `main`.
2. `CHANGELOG.md` describe los cambios y, si hay, las Notas Técnicas incorporadas.
3. Se actualiza `project.build.outputTimestamp` en el `pom.xml`.
4. Se fusiona el pull request de versión; el flujo etiqueta, firma, publica y crea el release en GitHub.
5. Se verifica que los artefactos aparezcan en Maven Central y se anuncia.
