#!/usr/bin/env sh
# Regenerates the list of bundled specification files after updating the spec submodule.
# The core cannot list a directory inside a jar, so it reads these index files instead.
set -eu
root=$(cd "$(dirname "$0")/.." && pwd)
out="$root/sifen-sdk-core/src/main/resources/io/github/sifensdk/core/spec/index"
mkdir -p "$out"
for folder in campos reglas catalogos respuestas; do
  (cd "$root/spec/$folder" && ls -1 -- *.json | LC_ALL=C sort) > "$out/$folder.txt"
done
(cd "$root/spec/xsd/oficial" && ls -1 -- *.xsd | LC_ALL=C sort) > "$out/xsd.txt"
echo "Index written to $out"
