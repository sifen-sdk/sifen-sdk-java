# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project follows
[Semantic Versioning](https://semver.org/) (see [docs/versiones-y-soporte.md](docs/versiones-y-soporte.md) for
what that means before 1.0).

## [Unreleased]

## [1.0.0-beta.1] - 2026-11-28

The first version published to Maven Central, for production use while 1.0 stabilizes
(criteria in [docs/versiones-y-soporte.md](docs/versiones-y-soporte.md#beta)). Earlier 0.x versions were never
published.

### Added (phase 7)

- `Exchange.anonymized()`: a copy of an exchange with SIFEN without the documents' data (values masked keeping
  their shape; signatures and certificates removed; SIFEN's codes and messages kept), to attach to bug reports.
- Guide "Pasar a producción": what changes from test, what to do with each result, how to report a beta bug.
- Publishing workflow: tag-driven, with approval in a protected environment, GPG signatures, SBOM and build
  provenance, and a dry run that uploads to the Central Portal without publishing. API compatibility check
  (japicmp) against the last published version from 1.0.0-beta.1 on.
- Conformance battery from the specification run by the SDK's tests.
- The specification's evidence E-003: F036 behaves like F037, a per-item exchange rate with a global one is
  rejected (1855), and SIFEN accepts TLS 1.2 only.

### Changed (phase 7) — incompatible

API settled for 1.0.0-beta.1. If you built an earlier version from source:

- Client results renamed: `DocumentQuery` → `DocumentQueryResult`, `RucQuery` → `RucQueryResult`, `EventResult`
  (the answer to `sendEvents`) → `EventReceptionResult`, and its nested `EventResult.Event` → `EventResult`.
  `queryDocument`, `queryRuc` and `sendEvents` keep their names.
- `DocumentResult`, `BatchResult`, `BatchSubmission`, `DocumentQueryResult`, `RucQueryResult`,
  `EventReceptionResult`, `EventResult` and `QueriedEvent` are final classes instead of records, so fields can be
  added in a minor version. Accessors, `equals` and `hashCode` are the same; they are created with `of(...)`,
  which is for the SDK and tests only and not part of the API.
- `toString()` of every client result shows only states, codes and counts (for example
  `DocumentQueryResult[found=true, codes=[0422], events=2]`): never the CDC, RUC, names, XML or SIFEN's message
  texts. `ResultMessage.toString()` shows the code only.
- The key material's `toString()` shows the issuer's common name, the serial number and the end of validity,
  no longer the subject (a natural person's certificate carries their name and identity document).
- The authorization protocol of an event (`EventResult.authorizationProtocol()`,
  `QueriedEvent.authorizationProtocol()`) is `null` when SIFEN gives none, instead of `""`.
- `BatchSubmission.processingTime()` is the text of `dTpoProces` as SIFEN sent it (`String`, nullable) instead of
  a `Duration` in seconds: its unit is not verified (C-710).
- Identity documents are named `idDocumentTypeCode` / `idDocumentTypeDescription` / `idDocumentNumber`
  everywhere: `Receiver` and `GenerationResponsible` (were `idTypeCode`, `idTypeDescription`, `idNumber`),
  `SelfInvoice.sellerIdDocumentTypeCode` / `sellerIdDocumentTypeDescription` / `sellerIdDocumentNumber` (were
  `sellerIdTypeCode`, `sellerIdTypeDescription`, `sellerIdNumber`), `QrParameters.receiverIdDocumentNumber`
  (was `receiverIdNumber`).
- `ReceiptNotification.operationTotalPyg()` → `guaraniTotal()` (`dTotalGs`, like `Totals.guaraniTotal()`).
- `Cdc`: `expeditionPoint()` → `dispatchPoint()`, `documentType()` → `documentTypeCode()`, `taxpayerType()` →
  `taxpayerTypeCode()`, `emissionType()` → `emissionTypeCode()` (and the parameters of `compose`);
  `issueDate()` returns a `LocalDate`, and `parse` rejects a CDC whose date is not a valid `yyyyMMdd`.
- `Item.valuation()` and `Item.Builder.valuation(ItemValues)` → `values()` and `values(ItemValues)`.
- `CertificateChecks.Issue(boolean error, …)` → `Issue(Severity severity, …)`, with `isError()`.
- `SifenDocuments.Builder.warnings(Set)` and `SifenEvents.Builder.warnings(Set)` → `warningRules(Set)`.
- `ValidationException.result()` is `@Nullable`: the result is not serialized, so a deserialized exception has
  none. Its message no longer says "document", since it is also thrown for events.
- `sifen-sdk-calc`: `ItemPrice.Builder.selfInvoice()` marks an item of a self-invoice; `build()` throws
  `IllegalStateException` if no VAT treatment was chosen (it used to mean a self-invoice item silently).
- `sifen-sdk-testing`: the service paths are the enum `SimulatedSifen.Service` (`RECEPTION`, `BATCH`,
  `BATCH_RESULT`, `DOCUMENT_QUERY`, `RUC_QUERY`, `EVENTS`, with `path()`); `enqueue`, `defaultResponse` and
  `recordedRequests` take a `Service`. The `String` constants were removed.


### Added (phase 6)

- `sifen-sdk-calc`, optional: `Calculator` derives the amounts of each item (`ItemPrice` → `ItemAmounts`: E727,
  EA002–EA009, E735–E737) and the totals of the document (group F) with the Manual's formulas, computed per item
  and summed as SIFEN verifies them. Rounding is explicit (decimals per currency, half-way modes only) and the
  rounding to multiples of 50 is optional. The core never invokes it.
- A property test completes thousands of random documents with the calculator and checks that the core finds no
  error; another test fails if an arithmetic rule of the specification is neither computed nor left out on purpose.
- `LiveCalcTest`: invoices completed by the calculator, in guaraníes and in dollars, sent to SIFEN's test
  environment. Local only. All were approved.
- Documentation site (MkDocs Material on GitHub Pages) with guides, the API reference and a reference of every rule
  generated from the specification. ADR 0013.

### Removed (phase 6)

- The empty `sifen-sdk-kude` module: the graphic representation (KuDE) is each company's own design. The SDK gives
  what it needs (CDC, QR address and signed XML); the site explains what a KuDE carries.

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
