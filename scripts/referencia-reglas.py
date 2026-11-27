#!/usr/bin/env python3
"""Genera la referencia de reglas del sitio de documentación a partir de la especificación.

Lee spec/reglas/*.json y spec/campos/*.json y escribe Markdown en docs/reglas/: un índice y una
página por archivo de reglas. Cada regla tiene un ancla estable con su id en minúsculas
(reglas/de-f-totales/#r-f014-a3) y cada campo con reglas, la suya (#f014).

Solo usa la biblioteca estándar. docs/reglas/ se genera en cada compilación y no se versiona.

Uso: python3 scripts/referencia-reglas.py [--spec spec] [--out docs/reglas]
"""

from __future__ import annotations

import argparse
import html
import json
import re
import shutil
import subprocess
import sys
from pathlib import Path

SPEC_URL = "https://github.com/sifen-sdk/spec/blob/main"

SECCIONES = [
    ("de-", "Documento electrónico"),
    ("ev-", "Eventos"),
    ("ws-", "Web services"),
]

TIPOS = {
    "obligatorio": "Obligatorio",
    "opcional": "Opcional",
    "condicional": "Condicional",
    "prohibido": "Prohibido",
    "formato": "Formato",
    "dependencia": "Dependencia",
    "aritmetica": "Aritmética",
}

OPERADORES = {
    "igual": "=",
    "distinto": "≠",
    "mayor": ">",
    "mayor_igual": "≥",
    "menor": "<",
    "menor_igual": "≤",
}


def leer_json(ruta: Path) -> dict:
    with ruta.open(encoding="utf-8") as f:
        return json.load(f)


def esc(texto: str) -> str:
    """Escapa texto libre para Markdown: HTML y los caracteres que forman énfasis, enlaces o tablas."""
    texto = html.escape(str(texto), quote=False)
    texto = re.sub(r"([\\`*_\[\]|])", r"\\\1", texto)
    return " ".join(texto.split())


def codigo(texto: str) -> str:
    """Texto como código en línea, seguro dentro de una celda de tabla."""
    texto = " ".join(str(texto).split()).replace("|", "\\|")
    return f"`` {texto} ``" if "`" in texto else f"`{texto}`"


class Spec:
    def __init__(self, raiz: Path):
        self.raiz = raiz
        self.campos: dict[str, dict] = {}
        self.titulos: dict[str, str] = {}
        for ruta in sorted((raiz / "campos").glob("*.json")):
            datos = leer_json(ruta)
            self.titulos[ruta.stem] = datos.get("titulo") or datos.get("grupo", ruta.stem)
            for campo in datos.get("campos", []):
                self.campos[campo["id"]] = campo
        self.archivos_reglas: dict[str, dict] = {
            ruta.stem: leer_json(ruta) for ruta in sorted((raiz / "reglas").glob("*.json"))
        }
        # Página en la que aparece cada campo que tiene reglas, para enlazar desde las condiciones.
        self.pagina_de_campo: dict[str, str] = {}
        for pagina, datos in self.archivos_reglas.items():
            for regla in datos.get("reglas", []):
                self.pagina_de_campo.setdefault(regla["campo"], pagina)
        self.contradicciones = self._contradicciones()
        self.evidencias = {
            ruta.name.split("-")[0] + "-" + ruta.name.split("-")[1]: ruta.name
            for ruta in sorted((raiz / "evidencia").glob("E-*.md"))
        }

    def _contradicciones(self) -> dict[str, tuple[str, str]]:
        resultado: dict[str, tuple[str, str]] = {}
        carpeta = self.raiz / "contradicciones.d"
        for ruta in sorted(carpeta.glob("*.md")) if carpeta.is_dir() else []:
            for linea in ruta.read_text(encoding="utf-8").splitlines():
                m = re.match(r"^##\s+(C-\d+)\s+[—-]\s+(.*)$", linea)
                if m:
                    resultado[m.group(1)] = (f"contradicciones.d/{ruta.name}", m.group(2).strip())
        return resultado

    def version(self) -> str | None:
        try:
            salida = subprocess.run(
                ["git", "-C", str(self.raiz), "rev-parse", "--short=12", "HEAD"],
                capture_output=True,
                text=True,
                check=True,
            )
            return salida.stdout.strip() or None
        except (OSError, subprocess.CalledProcessError):
            return None


class Pagina:
    def __init__(self, spec: Spec, nombre: str):
        self.spec = spec
        self.nombre = nombre
        self.campos_propios = {r["campo"] for r in spec.archivos_reglas[nombre].get("reglas", [])}

    def enlace_campo(self, campo_id: str) -> str:
        if campo_id in self.campos_propios:
            return f"[{campo_id}](#{campo_id.lower()})"
        pagina = self.spec.pagina_de_campo.get(campo_id)
        if pagina is None:
            return campo_id
        return f"[{campo_id}]({pagina}.md#{campo_id.lower()})"

    def condicion(self, c: dict) -> str:
        if "todas" in c:
            return "(" + " y ".join(self.condicion(x) for x in c["todas"]) + ")"
        if "alguna" in c:
            return "(" + " o ".join(self.condicion(x) for x in c["alguna"]) + ")"
        if "no" in c:
            return "no " + self.condicion(c["no"])
        campo = self.enlace_campo(c["campo"])
        op = c["op"]
        if op in OPERADORES:
            texto = f"{campo} {OPERADORES[op]} {codigo(c['valor'])}"
        elif op in ("en", "no_en"):
            valores = ", ".join(codigo(v) for v in c["valores"])
            texto = f"{campo} {'en' if op == 'en' else 'no en'} {{{valores}}}"
        elif op == "presente":
            texto = f"{campo} presente"
        elif op == "ausente":
            texto = f"{campo} ausente"
        else:
            raise ValueError(f"Operador desconocido en una condición: {op}")
        if c.get("cuantificador") == "todos":
            texto += " (en todas las repeticiones)"
        return texto

    def contradiccion(self, cid: str) -> str:
        info = self.spec.contradicciones.get(cid)
        if info is None:
            return f"[{cid}]({SPEC_URL}/contradicciones.md)"
        archivo, titulo = info
        return f"[{cid}]({SPEC_URL}/{archivo}) — {esc(titulo)}"

    def evidencia(self, eid: str) -> str:
        archivo = self.spec.evidencias.get(eid)
        destino = f"{SPEC_URL}/evidencia/{archivo}" if archivo else f"{SPEC_URL}/evidencia"
        return f"[{eid}]({destino})"


def fuentes(lista: list[dict]) -> str:
    partes = []
    for f in lista:
        texto = f"{esc(f['doc'])} {esc(f['ref'])}"
        if "pagina" in f:
            texto += f", p. {f['pagina']}"
        if f.get("cita"):
            texto += f": «{esc(f['cita'])}»"
        partes.append(texto)
    return "<br>".join(partes)


def vigencia(v: dict | None) -> str:
    if not v:
        return "Desde el Manual Técnico v150, en ambos ambientes"

    def fechas(d: dict) -> str:
        return ", ".join(
            f"{amb} {d.get(clave) or 'sin fecha publicada'}"
            for clave, amb in (("test", "test"), ("produccion", "producción"))
            if clave in d
        )

    partes = []
    if v.get("desde"):
        partes.append("desde " + fechas(v["desde"]))
    if v.get("hasta"):
        partes.append("hasta " + fechas(v["hasta"]))
    return "; ".join(partes).capitalize()


def lista_ids(valor) -> list[str]:
    if valor is None:
        return []
    return valor if isinstance(valor, list) else [valor]


def pagina_reglas(spec: Spec, nombre: str, datos: dict) -> str:
    p = Pagina(spec, nombre)
    reglas = datos.get("reglas", [])
    titulo = spec.titulos.get(nombre, datos.get("grupo", nombre))
    lineas = [
        f"# {esc(titulo)}",
        "",
        f"Grupo `{datos.get('grupo', '')}` · {len(reglas)} reglas · "
        f"[`reglas/{nombre}.json`]({SPEC_URL}/reglas/{nombre}.json)",
        "",
        "Generado a partir de la especificación. No se edita a mano.",
        "",
    ]
    por_campo: dict[str, list[dict]] = {}
    for regla in reglas:
        por_campo.setdefault(regla["campo"], []).append(regla)

    for campo_id, de_campo in por_campo.items():
        campo = spec.campos.get(campo_id, {})
        encabezado = campo_id
        if campo.get("xml"):
            encabezado += f" · {campo['xml']}"
        lineas += [f"## {esc(encabezado)} {{ #{campo_id.lower()} }}", ""]
        if campo.get("descripcion"):
            lineas += [esc(campo["descripcion"]), ""]
        for regla in de_campo:
            lineas += regla_md(p, regla)
    return "\n".join(lineas).rstrip() + "\n"


def regla_md(p: Pagina, r: dict) -> list[str]:
    rid = r["id"]
    tipo = TIPOS.get(r["tipo"], r["tipo"])
    marca = " · descartada" if "descartada" in r else ""
    lineas = [f"### {rid}{marca} {{ #{rid.lower()} }}", ""]
    descartada = r.get("descartada")
    if descartada:
        lineas += [
            '!!! warning "Descartada: SIFEN no la aplica"',
            "",
            "    La fuente la escribe, pero las pruebas contra SIFEN muestran que no la aplica. "
            "El SDK no la verifica.",
            f"    Contradicción {p.contradiccion(descartada['contradiccion'])}. "
            f"Evidencia {p.evidencia(descartada['evidencia'])}.",
            "",
        ]
    lineas += [esc(r["texto"]), "", "| | |", "|---|---|", f"| Tipo | {tipo} |"]
    lineas.append(f"| Campo | {p.enlace_campo(r['campo'])} |")
    if "condicion" in r:
        cond = p.condicion(r["condicion"])
        if cond.startswith("(") and cond.endswith(")"):
            cond = cond[1:-1]
        lineas.append(f"| Condición | {cond} |")
    if r.get("no_declarativa"):
        lineas.append("| Condición | No se puede expresar como datos: la describe el texto |")
    if "formula" in r:
        lineas.append(f"| Fórmula | {codigo(r['formula'])} |")
    if r.get("operandos"):
        lineas.append("| Operandos | " + ", ".join(p.enlace_campo(o) for o in r["operandos"]) + " |")
    if "holgura" in r:
        lineas.append(f"| Holgura | {esc(r['holgura'])} |")
    if r.get("codigo_sifen"):
        lineas.append(f"| Código SIFEN | {esc(r['codigo_sifen'])} |")
    lineas.append(f"| Fuente | {fuentes(r.get('fuentes', []))} |")
    lineas.append(f"| Vigencia | {vigencia(r.get('vigencia'))} |")
    contradicciones = lista_ids(r.get("contradiccion"))
    if contradicciones:
        lineas.append("| Contradicción | " + "<br>".join(p.contradiccion(c) for c in contradicciones) + " |")
    lineas.append("")
    return lineas


def indice(spec: Spec) -> str:
    total = sum(len(d.get("reglas", [])) for d in spec.archivos_reglas.values())
    version = spec.version()
    origen = f"commit `{version}` de " if version else ""
    lineas = [
        "# Referencia de reglas",
        "",
        "Lo que SIFEN exige de cada campo, tal como lo aplica el SDK antes de firmar. Son las "
        f"{total} reglas de la especificación ({origen}[`sifen-sdk/spec`](https://github.com/sifen-sdk/spec)), "
        "generadas en cada publicación del sitio.",
        "",
        "- **Cada regla tiene su ancla:** el id en minúsculas, por ejemplo `de-f-totales/#r-f014-a3`. "
        "Los errores de validación traen ese id (`ValidationIssue.ruleId()`).",
        "- **Fuente:** `MT150` es el Manual Técnico v150; `NTnnn`, la Nota Técnica n; `XSD`, los esquemas "
        "oficiales; `GUIA-MP`, la Guía de mejores prácticas. El formato completo está en "
        f"[`FORMATO.md`]({SPEC_URL}/FORMATO.md).",
        "- **Descartada:** la fuente la escribe, pero SIFEN no la aplica (lo demuestra una evidencia). "
        "El SDK no la verifica.",
        "- **Aritméticas:** el SDK compara, nunca corrige. Un valor cumple si difiere del resultado exacto "
        "en 0,5 o menos en la unidad del campo, o en la holgura de la regla si la tiene "
        "([ADR 0010](../adr/0010-semantica-de-la-verificacion-aritmetica.md)).",
        "",
    ]
    for prefijo, titulo in SECCIONES + [("", "Otras")]:
        nombres = [
            n
            for n in spec.archivos_reglas
            if n.startswith(prefijo) and (prefijo or not any(n.startswith(s) for s, _ in SECCIONES))
        ]
        if not nombres:
            continue
        lineas += [f"## {titulo}", "", "| Grupo | Reglas | Descartadas |", "|---|---|---|"]
        for n in nombres:
            reglas = spec.archivos_reglas[n].get("reglas", [])
            descartadas = sum(1 for r in reglas if "descartada" in r)
            lineas.append(
                f"| [{esc(spec.titulos.get(n, n))}]({n}.md) | {len(reglas)} | {descartadas or ''} |"
            )
        lineas.append("")
    return "\n".join(lineas).rstrip() + "\n"


def main() -> int:
    raiz = Path(__file__).resolve().parent.parent
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--spec", type=Path, default=raiz / "spec")
    parser.add_argument("--out", type=Path, default=raiz / "docs" / "reglas")
    args = parser.parse_args()

    if not (args.spec / "reglas").is_dir():
        print(f"No encuentro {args.spec / 'reglas'}: ¿falta el submódulo spec?", file=sys.stderr)
        return 1
    spec = Spec(args.spec)
    if args.out.exists():
        shutil.rmtree(args.out)
    args.out.mkdir(parents=True)
    (args.out / "index.md").write_text(indice(spec), encoding="utf-8")
    for nombre, datos in spec.archivos_reglas.items():
        (args.out / f"{nombre}.md").write_text(pagina_reglas(spec, nombre, datos), encoding="utf-8")
    total = sum(len(d.get("reglas", [])) for d in spec.archivos_reglas.values())
    print(f"{total} reglas en {len(spec.archivos_reglas)} páginas → {args.out}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
