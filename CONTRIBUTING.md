# Contributing

Thank you for helping. Issues and pull requests are welcome in Spanish or English.

## Before you start

- For anything larger than a small fix, open an issue first so we can agree on the approach.
- Read the design decisions in [docs/adr](docs/adr/README.md). A change that contradicts one needs a new ADR.

## Rules that are not negotiable

1. **Cite the source.** Every rule, field or code you add or change must reference the official SIFEN document
   that defines it: Manual Técnico (field or validation number), Nota Técnica (number) or XSD. A pull request
   without a source for a fiscal rule will not be merged. Rules and code tables themselves belong in
   [`sifen-sdk/spec`](https://github.com/sifen-sdk/spec).
2. **Never change an amount.** The SDK transports the amounts it receives. It may verify them and report a
   difference; it must never round, recompute or overwrite them.
3. **No real data, ever.** No real certificates, keys, passwords, security codes (CSC), taxpayer numbers,
   people's names or production documents — not in code, tests, issues, logs or screenshots. Use synthetic data.
4. **No invented rules or defaults.** If SIFEN does not require it, the SDK does not require it. If SIFEN does
   not define a default, the SDK does not make one up.

## Development

```bash
./mvnw verify            # build, tests and format check
./mvnw spotless:apply    # fix formatting
```

JDK 17 or later is required; the format check runs on JDK 21 or later. Code targets Java 17.

- Every change in behaviour comes with a test.
- Public API has Javadoc. Packages are `@NullMarked` (JSpecify).
- The `core` and `client` modules depend on the JDK only. A new dependency anywhere must be justified in the
  pull request.

## Commits and pull requests

- [Conventional Commits](https://www.conventionalcommits.org/) (`feat(core): …`, `fix(client): …`).
- Sign off every commit (`git commit -s`). By doing so you certify the
  [Developer Certificate of Origin](https://developercertificate.org/). There is no CLA.
- Keep pull requests focused. Fill in the template, including the source of any fiscal rule.
- If you used an AI assistant, you are still responsible for every line: review it, test it and make sure it
  cites real sources.

## Conduct

This project follows the [Code of Conduct](CODE_OF_CONDUCT.md).
