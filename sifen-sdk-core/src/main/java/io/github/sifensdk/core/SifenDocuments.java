/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core;

import io.github.sifensdk.core.internal.dsig.DocumentAssembler;
import io.github.sifensdk.core.internal.mapping.DocumentMapper;
import io.github.sifensdk.core.internal.rules.RuleEngine;
import io.github.sifensdk.core.internal.tree.Node;
import io.github.sifensdk.core.internal.xml.SchemaValidator;
import io.github.sifensdk.core.internal.xml.XmlWriter;
import io.github.sifensdk.core.model.ElectronicDocument;
import io.github.sifensdk.core.signing.Csc;
import io.github.sifensdk.core.signing.KeyMaterial;
import io.github.sifensdk.core.signing.SignedDocument;
import io.github.sifensdk.core.validation.ValidationException;
import io.github.sifensdk.core.validation.ValidationIssue;
import io.github.sifensdk.core.validation.ValidationResult;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Validates and signs SIFEN electronic documents, without network access.
 *
 * <p>Build one instance per environment (and keep it; it is immutable and thread-safe). Nothing is
 * global: instances for different environments can coexist in the same process.
 *
 * <pre>{@code
 * SifenDocuments documents = SifenDocuments.builder().environment(Environment.TEST).build();
 *
 * ValidationResult result = documents.validate(invoice);
 * SignedDocument signed = documents.sign(invoice, keyMaterial, csc);
 * store(signed.cdc(), signed.xml());   // keep it before sending it
 * }</pre>
 *
 * <p>The SDK never computes, rounds or changes an amount (ADR-0010): validation reports the amounts
 * that SIFEN would reject, with the field, the rule and its official source.
 */
public final class SifenDocuments {

  /** Paraguay's time zone, used for the signature date and time when no other is configured. */
  public static final ZoneId PARAGUAY = ZoneId.of("America/Asuncion");

  private final Environment environment;
  private final Clock clock;
  private final ZoneId zone;
  private final Set<String> warningRuleIds;

  private SifenDocuments(Builder builder) {
    this.environment = Objects.requireNonNull(builder.environment, "environment is required");
    this.clock = builder.clock;
    this.zone = builder.zone;
    this.warningRuleIds = Set.copyOf(builder.warningRuleIds);
  }

  /**
   * Starts building an instance.
   *
   * @return a builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * The environment this instance works for.
   *
   * @return the environment
   */
  public Environment environment() {
    return environment;
  }

  /**
   * Validates a document against what SIFEN documents: the rules of the specification in force on
   * the document's issue date in this environment, the arithmetic of its amounts, and the official
   * XSD.
   *
   * @param document the document
   * @return the issues found and the rules that could not be checked offline
   */
  public ValidationResult validate(ElectronicDocument document) {
    Objects.requireNonNull(document, "document");
    return validate(DocumentMapper.toTree(document, now()), document);
  }

  /**
   * Validates and signs a document, and adds its QR code.
   *
   * <p>The returned XML is final: store it as is before sending it, and send exactly those bytes.
   * It is never serialized again (ADR-0005).
   *
   * @param document the document
   * @param keyMaterial the issuer's certificate and private key
   * @param csc the issuer's security code for the QR, given by SIFEN
   * @return the signed document, its CDC and its QR address
   * @throws ValidationException if the document has validation errors; nothing is signed
   * @throws SigningException if signing fails
   */
  public SignedDocument sign(ElectronicDocument document, KeyMaterial keyMaterial, Csc csc) {
    Objects.requireNonNull(document, "document");
    Objects.requireNonNull(keyMaterial, "keyMaterial");
    Objects.requireNonNull(csc, "csc");
    Node tree = DocumentMapper.toTree(document, now());
    ValidationResult result = validate(tree, document);
    if (!result.isValid()) {
      throw new ValidationException(result);
    }
    SignedDocument signed =
        DocumentAssembler.signAndFinish(XmlWriter.write(tree), keyMaterial, csc, environment);
    List<ValidationIssue> schema = SchemaValidator.validateSigned(signed.xml());
    if (!schema.isEmpty()) {
      // The unsigned document was valid, so this is a defect of the SDK, not of the caller's data.
      throw new SigningException(
          "The signed document does not match the official XSD (" + schema.get(0) + ")");
    }
    return signed;
  }

  private ValidationResult validate(Node tree, ElectronicDocument document) {
    ValidationResult rules =
        RuleEngine.evaluate(
            tree,
            environment,
            document.generalData().issueDateTime().toLocalDate(),
            warningRuleIds);
    List<ValidationIssue> issues = new ArrayList<>(rules.issues());
    issues.addAll(SchemaValidator.validateDocument(XmlWriter.write(tree)));
    return new ValidationResult(issues, rules.uncheckedRules());
  }

  private LocalDateTime now() {
    return LocalDateTime.now(clock.withZone(zone)).truncatedTo(ChronoUnit.SECONDS);
  }

  /** Builder of {@link SifenDocuments}. */
  public static final class Builder {

    private Environment environment;
    private Clock clock = Clock.systemUTC();
    private ZoneId zone = PARAGUAY;
    private Set<String> warningRuleIds = Set.of();

    private Builder() {}

    /**
     * The environment (required; there is no default).
     *
     * @param environment test or production
     * @return this builder
     */
    public Builder environment(Environment environment) {
      this.environment = Objects.requireNonNull(environment, "environment");
      return this;
    }

    /**
     * The clock used for the signature date and time (defaults to the system clock).
     *
     * @param clock the clock
     * @return this builder
     */
    public Builder clock(Clock clock) {
      this.clock = Objects.requireNonNull(clock, "clock");
      return this;
    }

    /**
     * The time zone of the signature date and time (defaults to {@link #PARAGUAY}).
     *
     * @param zone the zone
     * @return this builder
     */
    public Builder zone(ZoneId zone) {
      this.zone = Objects.requireNonNull(zone, "zone");
      return this;
    }

    /**
     * Rules whose issues are reported as warnings instead of errors (for example, while an open
     * contradiction of the specification is being verified).
     *
     * @param ruleIds specification rule ids
     * @return this builder
     */
    public Builder warnings(Set<String> ruleIds) {
      this.warningRuleIds = Set.copyOf(ruleIds);
      return this;
    }

    /**
     * Builds the instance.
     *
     * @return the instance
     * @throws NullPointerException if no environment was set
     */
    public SifenDocuments build() {
      return new SifenDocuments(this);
    }
  }
}
