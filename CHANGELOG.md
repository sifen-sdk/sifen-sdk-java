# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project follows
[Semantic Versioning](https://semver.org/) (see [docs/versiones-y-soporte.md](docs/versiones-y-soporte.md) for
what that means before 1.0).

## [Unreleased]

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
