/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core;

import io.github.sifensdk.core.internal.dsig.EventAssembler;
import io.github.sifensdk.core.internal.mapping.EventMapper;
import io.github.sifensdk.core.internal.rules.RuleEngine;
import io.github.sifensdk.core.internal.tree.Node;
import io.github.sifensdk.core.internal.xml.SchemaValidator;
import io.github.sifensdk.core.internal.xml.XmlWriter;
import io.github.sifensdk.core.model.Event;
import io.github.sifensdk.core.signing.KeyMaterial;
import io.github.sifensdk.core.signing.SignedEvent;
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
 * Validates and signs SIFEN events (cancellation, number invalidation, nomination, transport update
 * and the receiver's events), without network access.
 *
 * <p>Build one instance per environment and keep it; it is immutable and thread-safe.
 *
 * <pre>{@code
 * SifenEvents events = SifenEvents.builder().environment(Environment.TEST).build();
 *
 * Event cancel = Event.builder()
 *     .id("1")
 *     .detail(Cancellation.builder().cdc(cdc).reason("Error en los datos del receptor").build())
 *     .build();
 * SignedEvent signed = events.sign(cancel, keyMaterial);
 * EventResult result = client.sendEvents(List.of(signed));
 * }</pre>
 *
 * <p>Deadlines that depend on SIFEN's clock (for example, 48 hours to cancel an invoice) cannot be
 * checked offline: they are listed in {@link ValidationResult#uncheckedRules()}.
 */
public final class SifenEvents {

  private final Environment environment;
  private final Clock clock;
  private final ZoneId zone;
  private final Set<String> warningRuleIds;

  private SifenEvents(Builder builder) {
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
   * Validates an event against what SIFEN documents: the rules of the events in force on the
   * signature date in this environment, and the official XSD.
   *
   * @param event the event
   * @return the issues found and the rules that could not be checked offline
   */
  public ValidationResult validate(Event event) {
    Objects.requireNonNull(event, "event");
    LocalDateTime now = now();
    return validate(EventMapper.toTree(event, now), now);
  }

  /**
   * Validates and signs an event. The returned XML is final: send exactly those bytes (ADR-0005).
   *
   * @param event the event
   * @param keyMaterial the certificate and private key of who sends it (the issuer, or the receiver
   *     for the receiver's events)
   * @return the signed event
   * @throws ValidationException if the event has validation errors; nothing is signed
   * @throws SigningException if signing fails
   */
  public SignedEvent sign(Event event, KeyMaterial keyMaterial) {
    Objects.requireNonNull(event, "event");
    Objects.requireNonNull(keyMaterial, "keyMaterial");
    LocalDateTime now = now();
    Node tree = EventMapper.toTree(event, now);
    ValidationResult result = validate(tree, now);
    if (!result.isValid()) {
      throw new ValidationException(result);
    }
    SignedEvent signed = EventAssembler.sign(XmlWriter.write(tree), keyMaterial);
    List<ValidationIssue> schema = SchemaValidator.validateSignedEvent(signed.xml());
    if (!schema.isEmpty()) {
      // The unsigned event was valid, so this is a defect of the SDK, not of the caller's data.
      throw new SigningException(
          "The signed event does not match the official XSD (" + schema.get(0) + ")");
    }
    return signed;
  }

  private ValidationResult validate(Node tree, LocalDateTime signatureDateTime) {
    ValidationResult rules =
        RuleEngine.evaluateEvent(
            tree, environment, signatureDateTime.toLocalDate(), warningRuleIds);
    List<ValidationIssue> issues = new ArrayList<>(rules.issues());
    issues.addAll(SchemaValidator.validateEvent(XmlWriter.write(tree)));
    return new ValidationResult(issues, rules.uncheckedRules());
  }

  private LocalDateTime now() {
    return LocalDateTime.now(clock.withZone(zone)).truncatedTo(ChronoUnit.SECONDS);
  }

  /** Builder of {@link SifenEvents}. */
  public static final class Builder {

    private Environment environment;
    private Clock clock = Clock.systemUTC();
    private ZoneId zone = SifenDocuments.PARAGUAY;
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
     * The time zone of the signature date and time (defaults to {@link SifenDocuments#PARAGUAY}).
     *
     * @param zone the zone
     * @return this builder
     */
    public Builder zone(ZoneId zone) {
      this.zone = Objects.requireNonNull(zone, "zone");
      return this;
    }

    /**
     * Rules whose issues are reported as warnings instead of errors.
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
    public SifenEvents build() {
      return new SifenEvents(this);
    }
  }
}
