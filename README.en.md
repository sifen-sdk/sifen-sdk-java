# SIFEN SDK for Java

Community SDK for integrating Java systems with **SIFEN**, Paraguay's national electronic invoicing system.
[Español](README.md) (primary documentation).

> **Status: under construction (phase 0).** Nothing is published or usable yet. This repository currently holds
> the project structure, its design decisions and its continuous integration.

> Independent community project. **Not affiliated with or endorsed by DNIT.**

## What it will be

An **integration** SDK, not an invoicing system:

- **It transports, it does not calculate.** Amounts computed by your system are sent unchanged, never rounded.
  Before sending, the SDK verifies them against the formulas SIFEN documents and fails with the field, the rule
  and its source. Calculation exists only as an optional module you call explicitly.
- **It requires exactly what SIFEN documents.** Mandatory, optional and conditional fields follow the official
  documentation, and every rule cites its source (Manual Técnico, Nota Técnica or XSD).
- **No global state.** Many issuers, certificates and environments can coexist in one process.
- **Signed XML is immutable.** Signed once, returned to you to store before sending, sent as is.
- **No mandatory dependencies** beyond the JDK in the main modules.

Rules, code tables and conformance cases live as data in [`sifen-sdk/spec`](https://github.com/sifen-sdk/spec)
so that SDKs for other languages behave the same way.

## Compatibility

Java 17 minimum. A single artifact, tested on 17, 21 and 25.

## Build

```bash
./mvnw verify
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) and [SECURITY.md](SECURITY.md). Design decisions are recorded in
[docs/adr](docs/adr/README.md) (in Spanish).

## License

[Apache-2.0](LICENSE).
