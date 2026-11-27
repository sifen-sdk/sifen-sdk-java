#!/usr/bin/env bash
# Deletes the local SIFEN test credentials used by scripts/prueba-sifen-local.sh.
# Usage: scripts/borrar-credenciales-sifen.sh [folder]   (default: ~/.config/sifen-sdk)
set -euo pipefail
dir=${1:-"$HOME/.config/sifen-sdk"}
root=$(cd "$(dirname "$0")/.." && pwd)
[ -d "$dir" ] || { echo "No hay nada que borrar en $dir"; exit 0; }
# Overwrite before deleting.
find "$dir" -type f -exec shred -u -z -n 3 {} +
rmdir "$dir" 2>/dev/null || rm -rf "$dir"
# Test reports may hold SIFEN's messages for the issuer: remove them too.
rm -rf "$root/sifen-sdk-client/target/surefire-reports"
echo "Credenciales borradas de $dir y reportes de prueba eliminados."
