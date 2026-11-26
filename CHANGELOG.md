# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project follows
[Semantic Versioning](https://semver.org/) (see [docs/versiones-y-soporte.md](docs/versiones-y-soporte.md) for
what that means before 1.0).

## [Unreleased]

### Added

- `SifenDocuments`: validates and signs electronic documents offline, and adds their QR code.
- Document model for the electronic invoice and the credit note (256 fields), immutable, with builders.
- Rule engine over the bundled specification (Manual Técnico v150, Notas Técnicas 1–27): mandatory,
  conditional and prohibited fields, catalogs and their descriptions, and 40 arithmetic checks that never change
  an amount. Rules apply by issue date and environment.
- Validation against the official XSD, offline.
- CDC and security code, XMLDSig signature (NT 16), QR code; key material from PKCS#12, `KeyStore` and PEM.
- Project foundations: multi-module build, continuous integration on Java 17, 21 and 25, community files and
  the initial design decisions.
