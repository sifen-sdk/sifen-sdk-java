# 0013 — Sitio de documentación con MkDocs Material en GitHub Pages

- **Estado:** Aceptada
- **Fecha:** 2026-11-27

## Contexto

La documentación vive en `docs/` como Markdown: guías, decisiones y la política de versiones. Quien integra necesita
además dos cosas que hoy no están a la vista: qué exige SIFEN de cada campo (más de mil reglas en `spec/reglas`) y
la API pública (Javadoc). El SDK cita reglas por id en cada error de validación, así que cada regla necesita una
dirección estable a la que enlazar.

## Decisión

- **MkDocs con el tema Material**, compilado desde `docs/` (`mkdocs.yml` en la raíz), en español, con búsqueda y
  modo claro y oscuro. Se publica en **GitHub Pages** con las acciones oficiales de Pages.
- **La referencia de reglas se genera** en cada compilación desde `spec/` con `scripts/referencia-reglas.py`
  (Python, solo la biblioteca estándar): una página por archivo de reglas y un ancla por regla con su id en
  minúsculas (`reglas/de-f-totales/#r-f014-a3`). Las reglas descartadas se marcan con su contradicción y su
  evidencia. `docs/reglas/` no se versiona.
- **El Javadoc de todos los módulos** se genera con `javadoc:aggregate` y se publica bajo `/api/`.
- **El flujo `sitio.yml`** compila con `mkdocs build --strict` en cada pull request que toca la documentación (un
  enlace roto falla el PR) y publica solo desde `main`. Las acciones van fijadas por hash y las dependencias de
  Python, por hash en `docs/requirements.txt`.

## Consecuencias

- La referencia de reglas siempre coincide con la versión de `spec` del submódulo; no hay copia a mano que
  desactualizar.
- Las páginas de reglas se enlazan desde el índice generado, no desde el nav de `mkdocs.yml`: un archivo de reglas
  nuevo en `spec` aparece solo, sin tocar la configuración.
- Compilar el sitio requiere Python y JDK, solo en el flujo del sitio; el build de Maven no cambia.
- El sitio muestra la documentación de `main`, no la de cada versión publicada. Versionarla (por ejemplo, con
  `mike`) queda para cuando haya versiones publicadas.
- Material for MkDocs anunció el fin de su mantenimiento para mayo de 2027. Su sucesor, Zensical, del mismo equipo,
  lee el mismo `mkdocs.yml`; la migración se evalúa antes de esa fecha.

## Alternativas descartadas

- **Jekyll, el generador por defecto de GitHub Pages:** sin búsqueda ni navegación útiles sin plugins, y con
  Ruby como dependencia del proyecto.
- **Un generador en Java (maven-site-plugin u otro):** pensado para reportes del build, no para guías; la
  referencia de reglas igual necesitaría un generador propio.
- **Escribir la referencia de reglas a mano:** se desactualiza con cada Nota Técnica.
