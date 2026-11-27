# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project follows
[Semantic Versioning](https://semver.org/) (see [docs/versiones-y-soporte.md](docs/versiones-y-soporte.md) for
what that means before 1.0).

## [Unreleased]

Target: 0.4.0 (phase 5). Nothing has been published to Maven Central yet.

### Added (phase 5)

- `KeyMaterials.pkcs11` and `Pkcs11Config`: certificates on a token (type A3) or in an HSM, through the JDK's
  SunPKCS11 provider without installing it. The key never leaves the device.
- `RemoteSigner` and `KeyMaterials.remote`: a remote signing service, cloud KMS or HSM signs; it only receives the
  SHA-256 digest of what is signed, never the document.
- `CertificateChecks`: the signer's certificate is checked before signing. Out of its validity period, or without
  digital signature or non-repudiation usage, `sign` refuses to sign; another RUC than the issuer's is a warning.
- Token and remote keys sign documents and events and authenticate mutual TLS. Verified against SIFEN's test
  environment with the test certificate copied to a SoftHSM2 token and behind an in-process remote signer.
- `scripts/prueba-sifen-token.sh` runs the live tests with the key in a temporary SoftHSM2 token or behind a remote
  signer. Local only.
- Guide: [docs/claves-en-token-hsm-y-firma-remota.md](docs/claves-en-token-hsm-y-firma-remota.md); decision in
  ADR 0012.

### Changed (phase 5)

- `KeyMaterials.keyStore` signs with a non-extractable key through its store's provider when that provider is not
  installed (keys of installed providers are returned as before).
- With a token or remote key, the client installs the SDK's own provider once (it signs only with those keys) and,
  if the key cannot sign with RSA-PSS, offers TLS 1.2 only. SIFEN only offers TLS 1.2.
- `SifenDocuments.sign` and `SifenEvents.sign` throw `SigningException` for an expired, not yet valid or
  non-signing certificate.

### Added (phase 4)

- `SifenEvents`: validates and signs the eight events a taxpayer sends (cancellation, number invalidation,
  nomination, transport update, receipt notification, conformity, disconformity and non-recognition), with an
  event model (`Event`, `EventDetail`) and `SignedEvent`. Verified against SIFEN's test environment.
- `LiveEventsTest`: sends each event to SIFEN's test environment on documents it issues first. Local only.
- Self-invoice (C002 = 4, group E4) and debit note (C002 = 6).
- Delivery note (C002 = 7, group E6) and transport (group E10, also in invoices).
- Sector-specific groups (E9): electricity, insurance and policies, supermarkets, additional commercial data.
- `LiveDocumentsTest`: sends one document of each new type and group, and a transport update event, to SIFEN's
  test environment. Local only. All were approved.
- The specification is updated with evidence E-002: the carrier group is required in every delivery note
  (validation 2300, C-407), and R-F008-A1 does not apply to self-invoices (C-517).

- `DocumentQuery` reads the document's transaction number (`authorizationProtocol`) and the events registered on
  it (`events`, as `QueriedEvent` with an `EventType`), including the ones the SET registers. Verified against
  SIFEN's test environment, which returns the content without the `rContDe` root the XSD describes.

### Changed (phase 4)

- `SifenClient.sendEvents` takes `SignedEvent`s instead of raw bytes.
- The event request declares `xsi:schemaLocation` on `gGroupGesEve`: without it SIFEN answers 0160.
- The rule engine evaluates a profile (the electronic document or the events) instead of assuming `rDE`.

### Changed

- Arithmetic checks follow what SIFEN does, verified against its test environment (evidence E-001 of the
  specification): a value is accepted when it differs from the exact result by at most 0.5 in the unit of the field,
  or by the rule's documented tolerance (0.8 in discount percentages) without adding the 0.5. Before, the SDK
  allowed half a unit of the last reported decimal, stricter than SIFEN in foreign currency, and added the 0.5 to
  the documented tolerance, more lenient than SIFEN in EA004.
- The total VAT (F017) is verified with the field table's formula, as an error; the formula of validation 2371 is
  no longer applied (C-502).
- The per-item exchange rate is no longer required with a global exchange rate (C-303).
- Rules the specification records as `descartada` are never evaluated.

### Added

- `LiveArithmeticProbeTest`: sends one document per arithmetic rule to SIFEN's test environment, each with a single
  amount off by a known delta, and reports SIFEN's answer. Local only, like the live test.

- `SifenClient`: synchronous and batch reception, batch result, document and taxpayer queries, and
  event reception, with mutual TLS per issuer, typed results and the raw exchange. Never resends on its own.
- `BatchSchedule`: when to query a batch without triggering SIFEN's temporary blocks.
- `sifen-sdk-testing`: a simulated SIFEN with mutual TLS and XSD-valid responses for integration tests.
- A test against the real SIFEN test environment, run locally with
  `scripts/prueba-sifen-local.sh`; it is not part of CI.
- `SifenDocuments`: validates and signs electronic documents offline, and adds their QR code.
- Document model for the electronic invoice and the credit note (256 fields), immutable, with builders.
- Rule engine over the bundled specification (Manual Técnico v150, Notas Técnicas 1–27): mandatory,
  conditional and prohibited fields, catalogs and their descriptions, and 40 arithmetic checks that never change
  an amount. Rules apply by issue date and environment.
- Validation against the official XSD, offline.
- CDC and security code, XMLDSig signature (NT 16), QR code; key material from PKCS#12, `KeyStore` and PEM.
- Project foundations: multi-module build, continuous integration on Java 17, 21 and 25, community files and
  the initial design decisions.
