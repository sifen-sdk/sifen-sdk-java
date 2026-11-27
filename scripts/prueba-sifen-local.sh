#!/usr/bin/env bash
# Runs LiveSifenTest against SIFEN's TEST environment with local credentials.
#
# The credentials stay in files outside the repository, readable only by you. Nothing is
# uploaded anywhere except to SIFEN's test environment; the test prints only states, codes
# and SIFEN's messages. See docs/pruebas-contra-sifen.md.
#
# Usage: scripts/prueba-sifen-local.sh [env-file]   (default: ~/.config/sifen-sdk/test.env)
set -euo pipefail

root=$(cd "$(dirname "$0")/.." && pwd)
env_file=${1:-"$HOME/.config/sifen-sdk/test.env"}

fail() { echo "ERROR: $*" >&2; exit 1; }

[ -f "$env_file" ] || fail "no existe $env_file (ver docs/pruebas-contra-sifen.md)"
env_real=$(realpath "$env_file")
case "$env_real" in "$root"/*) fail "el archivo de credenciales no puede estar dentro del repositorio" ;; esac

private_only() {
  local f=$1 mode
  mode=$(stat -c '%a' "$f")
  [ "${mode: -2}" = "00" ] || fail "$f tiene permisos $mode; ejecutá: chmod 600 '$f'"
  [ "$(stat -c '%U' "$f")" = "$(id -un)" ] || fail "$f no es tuyo"
}
private_only "$env_real"

set -a
# shellcheck disable=SC1090
. "$env_real"
set +a

for v in SIFEN_TEST_P12_PATH SIFEN_TEST_P12_PASSWORD SIFEN_TEST_CSC_ID SIFEN_TEST_CSC SIFEN_TEST_ISSUER_FILE; do
  [ -n "${!v:-}" ] || fail "falta $v en $env_file"
done
for f in "$SIFEN_TEST_P12_PATH" "$SIFEN_TEST_ISSUER_FILE"; do
  [ -f "$f" ] || fail "no existe $f"
  real=$(realpath "$f")
  case "$real" in "$root"/*) fail "$f no puede estar dentro del repositorio" ;; esac
  private_only "$real"
done
unset SIFEN_TEST_P12_BASE64 SIFEN_TEST_ISSUER

cd "$root"
echo "Emitiendo en el ambiente de TEST de SIFEN (no se imprimen credenciales ni documentos)..."
# -o: offline, only already downloaded build tools run while the credentials are loaded.
./mvnw -o -q -B -ntp -pl sifen-sdk-client -am verify \
  -Dtest=LiveSifenTest -Dsurefire.failIfNoSpecifiedTests=false \
  -Dspotless.check.skip=true -DtrimStackTrace=true -Dsurefire.useFile=false
status=$?
# The test reports may contain SIFEN's messages; they stay in target/, which git ignores.
exit $status
