# Synthetic server certificate of the simulated SIFEN

**Everything in this folder is synthetic.** `simulated-sifen-server.p12` holds a self-signed certificate and
its private key, generated with the JDK's `keytool` only so that `SimulatedSifen` can serve HTTPS on the
loopback interface during tests. No certification authority issued it, it identifies nobody, and it is
public: anyone can read the key from this jar. Never trust it outside tests, and never add real key material
here.

| | |
|---|---|
| File | `simulated-sifen-server.p12` (PKCS#12, one alias: `simulated-sifen`) |
| Test password (store and key) | `prueba-sintetica` |
| Subject | `CN=localhost, O=SIFEN SDK SIMULATED SERVER (SYNTHETIC - TESTS ONLY), C=PY` |
| Subject alternative names | `DNS:localhost`, `IP:127.0.0.1` |
| Key | RSA 2048, SHA256withRSA; key usage digitalSignature + keyEncipherment; extended key usage serverAuth |
| Validity | 2026-01-01 to 2045-12-27 |

Clients trust it through `SimulatedSifen.serverTrustStore()`; there is no need to read this file directly.

How it was generated:

```bash
keytool -genkeypair -alias simulated-sifen -keyalg RSA -keysize 2048 -sigalg SHA256withRSA \
  -dname "CN=localhost, O=SIFEN SDK SIMULATED SERVER (SYNTHETIC - TESTS ONLY), C=PY" \
  -ext san=dns:localhost,ip:127.0.0.1 -ext KeyUsage:critical=digitalSignature,keyEncipherment \
  -ext ExtendedKeyUsage=serverAuth -validity 7300 -startdate 2026/01/01 \
  -storetype PKCS12 -keystore simulated-sifen-server.p12 -storepass prueba-sintetica -keypass prueba-sintetica
```
