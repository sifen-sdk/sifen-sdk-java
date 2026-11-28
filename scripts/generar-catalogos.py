#!/usr/bin/env python3
# Copyright 2026 SIFEN SDK contributors
# SPDX-License-Identifier: Apache-2.0
"""Genera un enum de Java por cada catálogo de spec que declara 'nombre' (ADR 0014).

Uso: scripts/generar-catalogos.py   (y después ./mvnw spotless:apply)

Escribe sifen-sdk-core/src/main/java/io/github/sifensdk/core/catalog/<Tipo>.java. Los archivos generados se
versionan; CatalogEnumsTest falla si dejan de coincidir con spec (códigos, nombres o descripciones).
"""
import glob
import json
import os
import sys

RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SPEC = os.path.join(RAIZ, "spec")
DESTINO = os.path.join(RAIZ, "sifen-sdk-core", "src", "main", "java", "io", "github", "sifensdk", "core", "catalog")
NO_GENERADOS = {"CatalogCode.java", "package-info.java"}

CABECERA = """/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
"""


def pascal(snake):
    return "".join(p[:1].upper() + p[1:] for p in snake.split("_"))


def java_str(s):
    return '"' + s.replace("\\", "\\\\").replace('"', '\\"') + '"'


def html(s):
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("@", "&#64;").replace("*/", "*&#47;")


def campos_codigo(d):
    descripciones = set((d.get("descripciones") or {}).values())
    return [c for c in d.get("campos", []) if c not in descripciones]


def generar(d):
    tipo = pascal(d["nombre"])
    campos = ", ".join(f"{{@code {c}}}" for c in campos_codigo(d))
    out = [CABECERA.format(id=d["id"]), "package io.github.sifensdk.core.catalog;", "", "import java.util.Optional;", ""]
    out += [
        "/**",
        f" * {html(d['titulo'])}.",
        " *",
        f" * <p>SIFEN catalog {{@code {d['id']}}}" + (f", used by fields {campos}." if campos else "."),
        " * Each constant carries the code that travels in the XML and the official description the SDK",
        " * writes next to it. Builders also accept the code as a {@code String}, for codes SIFEN adds before",
        " * this enum knows them.",
        " *",
        f" * <p>Generated from {{@code spec/catalogos/{d['id']}.json}} by {{@code scripts/generar-catalogos.py}}; do not",
        " * edit by hand.",
        " */",
        f"public enum {tipo} implements CatalogCode {{",
    ]
    entradas = d["entradas"]
    for i, e in enumerate(entradas):
        descripcion = e.get("descripcion_xsd", e["descripcion"])
        doc = [f"  /** {{@code {e['codigo']}}}: {html(e['descripcion'])}."]
        if e.get("significado"):
            doc[0] = f"  /** {{@code {e['codigo']}}}: {html(e['descripcion'])} ({html(e['significado'])})."
        notas = []
        if e.get("estado") == "futuro":
            notas.append("Defined by SIFEN but not in force yet.")
        hasta = (e.get("vigencia") or {}).get("hasta")
        if hasta:
            notas.append(f"No longer valid since {hasta.get('produccion') or hasta.get('test')}.")
        if notas:
            doc = [doc[0], "   *", "   * <p>" + " ".join(notas)]
            if hasta:
                doc += ["   *", "   * @deprecated SIFEN no longer accepts this code"]
            doc.append("   */")
        else:
            doc[0] += " */"
        out += doc
        if hasta:
            out.append("  @Deprecated")
        fin = ";" if i == len(entradas) - 1 else ","
        out.append(f"  {e['nombre'].upper()}({java_str(e['codigo'])}, {java_str(descripcion)}){fin}")
        out.append("")
    out += [
        "  private final String code;",
        "  private final String description;",
        "",
        f"  {tipo}(String code, String description) {{",
        "    this.code = code;",
        "    this.description = description;",
        "  }",
        "",
        "  @Override",
        "  public String code() {",
        "    return code;",
        "  }",
        "",
        "  @Override",
        "  public String description() {",
        "    return description;",
        "  }",
        "",
        "  /**",
        "   * Returns the constant for a code.",
        "   *",
        "   * @param code the code as it travels in the XML",
        "   * @return the constant, or empty if this version of the SDK does not know the code",
        "   */",
        f"  public static Optional<{tipo}> fromCode(String code) {{",
        f"    for ({tipo} value : values()) {{",
        "      if (value.code.equals(code)) {",
        "        return Optional.of(value);",
        "      }",
        "    }",
        "    return Optional.empty();",
        "  }",
        "}",
        "",
    ]
    return tipo, "\n".join(out)


def main():
    os.makedirs(DESTINO, exist_ok=True)
    generados = set()
    for ruta in sorted(glob.glob(os.path.join(SPEC, "catalogos", "*.json"))):
        d = json.load(open(ruta, encoding="utf-8"))
        if "nombre" not in d:
            continue
        tipo, codigo = generar(d)
        archivo = tipo + ".java"
        if archivo in generados:
            sys.exit(f"dos catálogos generan {archivo}")
        generados.add(archivo)
        with open(os.path.join(DESTINO, archivo), "w", encoding="utf-8") as f:
            f.write(codigo)
    for archivo in os.listdir(DESTINO):
        if archivo.endswith(".java") and archivo not in generados and archivo not in NO_GENERADOS:
            os.remove(os.path.join(DESTINO, archivo))
    print(f"{len(generados)} enums en {os.path.relpath(DESTINO, RAIZ)}")


if __name__ == "__main__":
    main()
