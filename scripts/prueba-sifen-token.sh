#!/usr/bin/env bash
# Runs the live tests against SIFEN's TEST environment with the key held in a PKCS#11 token
# (SoftHSM2) or by a remote signer, instead of a file.
#
# For the token, the test certificate is copied to a temporary SoftHSM2 token in a private
# directory (in RAM when /dev/shm exists), with a random PIN, and the directory is deleted when the
# script ends. Nothing leaves this machine except the requests to SIFEN's test environment. See
# docs/pruebas-contra-sifen.md.
#
# Usage: scripts/prueba-sifen-token.sh [pkcs11|remote] [TestClass ...]
#   default: pkcs11 LiveSifenTest
#   env file: SIFEN_TEST_ENV_FILE (default ~/.config/sifen-sdk/test.env)
set -euo pipefail

root=$(cd "$(dirname "$0")/.." && pwd)
mode=${1:-pkcs11}
[ $# -gt 0 ] && shift
classes=${*:-LiveSifenTest}
env_file=${SIFEN_TEST_ENV_FILE:-"$HOME/.config/sifen-sdk/test.env"}

fail() { echo "ERROR: $*" >&2; exit 1; }

case "$mode" in pkcs11 | remote) ;; *) fail "el modo es pkcs11 o remote" ;; esac
[ -f "$env_file" ] || fail "no existe $env_file (ver docs/pruebas-contra-sifen.md)"
env_real=$(realpath "$env_file")
case "$env_real" in "$root"/*) fail "el archivo de credenciales no puede estar dentro del repositorio" ;; esac

private_only() {
  local f=$1 m
  m=$(stat -c '%a' "$f")
  [ "${m: -2}" = "00" ] || fail "$f tiene permisos $m; ejecutá: chmod 600 '$f'"
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
  real=$(realpath "$f")
  case "$real" in "$root"/*) fail "$f no puede estar dentro del repositorio" ;; esac
  private_only "$real"
done
unset SIFEN_TEST_P12_BASE64 SIFEN_TEST_ISSUER

maven_extra=()
if [ "$mode" = pkcs11 ]; then
  command -v softhsm2-util >/dev/null || fail "falta SoftHSM2 (sudo apt install softhsm2)"
  library=${SOFTHSM2_LIB:-}
  for candidate in /usr/lib/softhsm/libsofthsm2.so /usr/local/lib/softhsm/libsofthsm2.so; do
    [ -z "$library" ] && [ -f "$candidate" ] && library=$candidate
  done
  [ -n "$library" ] || fail "no encuentro libsofthsm2.so; indicala con SOFTHSM2_LIB"

  base=/dev/shm
  [ -d "$base" ] || base=${TMPDIR:-/tmp}
  umask 077
  work=$(mktemp -d "$base/sifen-token.XXXXXX")
  trap 'rm -rf "$work"' EXIT
  trap 'exit 130' INT TERM
  mkdir "$work/tokens"
  printf 'directories.tokendir = %s/tokens\nobjectstore.backend = file\nlog.level = ERROR\n' \
    "$work" >"$work/softhsm2.conf"
  export SOFTHSM2_CONF="$work/softhsm2.conf"

  SIFEN_TEST_PKCS11_PIN=$(od -An -N8 -tu8 /dev/urandom | tr -d ' \n')
  so_pin=$(od -An -N8 -tu8 /dev/urandom | tr -d ' \n')
  out=$(softhsm2-util --init-token --free --label sifen-prueba \
    --pin "$SIFEN_TEST_PKCS11_PIN" --so-pin "$so_pin")
  slot=$(sed -n 's/.*reassigned to slot \([0-9]*\).*/\1/p' <<<"$out")
  [ -n "$slot" ] || fail "softhsm2-util no informó la ranura"
  printf 'name = SifenPrueba\nlibrary = "%s"\nslot = %s\n' "$library" "$slot" >"$work/pkcs11.cfg"

  # keytool reads the passwords from the environment. softhsm2-util only takes them as arguments:
  # they are random, of this run only, and the token lives in a private directory.
  export SIFEN_TEST_PKCS11_PIN
  keytool -importkeystore -noprompt \
    -srckeystore "$SIFEN_TEST_P12_PATH" -srcstoretype PKCS12 -srcstorepass:env SIFEN_TEST_P12_PASSWORD \
    -destkeystore NONE -deststoretype PKCS11 -deststorepass:env SIFEN_TEST_PKCS11_PIN \
    -providerClass sun.security.pkcs11.SunPKCS11 -providerArg "$work/pkcs11.cfg" >/dev/null
  echo "Certificado de prueba copiado a un token SoftHSM2 temporal (se borra al terminar)."

  export SIFEN_TEST_PKCS11_LIBRARY=$library SIFEN_TEST_PKCS11_SLOT=$slot
  maven_extra+=("-Dsofthsm2.conf=$SOFTHSM2_CONF")
fi
export SIFEN_TEST_KEY=$mode

cd "$root"
status=0
for cls in $classes; do
  echo "== $cls con la clave en: $mode"
  # -o: offline, only already downloaded build tools run while the credentials are loaded.
  ./mvnw -o -q -B -ntp -pl sifen-sdk-client -am verify ${maven_extra[@]+"${maven_extra[@]}"} \
    -Dtest="$cls" -Dsurefire.failIfNoSpecifiedTests=false \
    -Dspotless.check.skip=true -DtrimStackTrace=true -Dsurefire.useFile=false || status=$?
done
exit $status
