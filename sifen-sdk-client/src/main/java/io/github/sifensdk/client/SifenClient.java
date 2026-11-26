/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client;

import io.github.sifensdk.client.internal.Batches;
import io.github.sifensdk.client.internal.Events;
import io.github.sifensdk.client.internal.Queries;
import io.github.sifensdk.client.internal.Reception;
import io.github.sifensdk.client.internal.Transport;
import io.github.sifensdk.core.Environment;
import io.github.sifensdk.core.signing.KeyMaterial;
import io.github.sifensdk.core.signing.SignedDocument;
import java.net.ProxySelector;
import java.net.URI;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Client of the SIFEN web services for one issuer.
 *
 * <p>Build one per issuer and environment and keep it: it is immutable, thread-safe and owns its
 * own TLS connection with the issuer's certificate, so clients of different issuers never share a
 * certificate (ADR-0004). Calls are blocking and suitable for virtual threads.
 *
 * <pre>{@code
 * SifenClient client = SifenClient.builder()
 *     .environment(Environment.TEST)
 *     .keyMaterial(certificate)
 *     .build();
 *
 * BatchSubmission submission = client.sendBatch(List.of(signed));
 * // ...later, no sooner than SIFEN recommends:
 * BatchResult result = client.queryBatch(submission.protocolNumber());
 * }</pre>
 *
 * <p>A rejection is a normal result, never an exception (ADR-0006). Submissions are never retried
 * automatically: if the outcome is unknown, a {@link TransportException} says so, and the document
 * must be queried before resending it (ADR-0007).
 */
public final class SifenClient {

  private final Environment environment;
  private final Transport transport;

  private SifenClient(Builder b) {
    this.environment = Objects.requireNonNull(b.environment, "environment is required");
    KeyMaterial keyMaterial = Objects.requireNonNull(b.keyMaterial, "keyMaterial is required");
    URI base = b.baseUri != null ? b.baseUri : environment.webServiceBaseUri();
    this.transport =
        new Transport(
            base,
            keyMaterial,
            b.trustStore,
            b.connectTimeout,
            b.requestTimeout,
            b.proxy,
            b.clock,
            b.random);
  }

  /**
   * Starts building a client.
   *
   * @return a builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * The environment this client works with.
   *
   * @return the environment
   */
  public Environment environment() {
    return environment;
  }

  /**
   * Sends one signed document to the synchronous reception service ({@code siRecepDE}).
   *
   * <p>Not every taxpayer is enabled for the synchronous service; the batch service is the general
   * one.
   *
   * @param document the signed document, sent exactly as signed
   * @return the outcome
   * @throws TransportException if SIFEN could not be reached
   * @throws ProtocolException if SIFEN's answer is not a valid response
   */
  public ReceptionResult send(SignedDocument document) {
    return Reception.send(transport, Objects.requireNonNull(document, "document"));
  }

  /**
   * Sends up to 50 signed documents, of the same type and issuer, to the batch reception service
   * ({@code siRecepLoteDE}).
   *
   * @param documents the signed documents, sent exactly as signed
   * @return whether the batch was accepted, and its protocol number
   * @throws IllegalArgumentException if the batch breaks a documented limit
   * @throws TransportException if SIFEN could not be reached
   * @throws ProtocolException if SIFEN's answer is not a valid response
   */
  public BatchSubmission sendBatch(List<SignedDocument> documents) {
    return Batches.send(transport, List.copyOf(documents));
  }

  /**
   * Queries the result of a batch ({@code siResultLoteDE}).
   *
   * @param protocolNumber the protocol number received when the batch was accepted
   * @return the state of the batch and of each document
   * @throws TransportException if SIFEN could not be reached
   * @throws ProtocolException if SIFEN's answer is not a valid response
   */
  public BatchResult queryBatch(String protocolNumber) {
    return Batches.result(transport, Objects.requireNonNull(protocolNumber, "protocolNumber"));
  }

  /**
   * Queries a document by CDC ({@code siConsDE}).
   *
   * @param cdc the CDC
   * @return whether SIFEN has it, and its container
   * @throws TransportException if SIFEN could not be reached
   * @throws ProtocolException if SIFEN's answer is not a valid response
   */
  public DocumentQuery queryDocument(String cdc) {
    return Queries.document(transport, Objects.requireNonNull(cdc, "cdc"));
  }

  /**
   * Queries a taxpayer by RUC ({@code siConsRUC}).
   *
   * @param ruc the RUC, without check digit
   * @return the taxpayer's data, if found
   * @throws TransportException if SIFEN could not be reached
   * @throws ProtocolException if SIFEN's answer is not a valid response
   */
  public RucQuery queryRuc(String ruc) {
    return Queries.ruc(transport, Objects.requireNonNull(ruc, "ruc"));
  }

  /**
   * Sends up to 15 signed events ({@code siRecepEvento}).
   *
   * @param signedEvents each signed event exactly as signed: the {@code rGesEve} element, with its
   *     {@code rEve} and its {@code Signature}
   * @return the outcome of each event
   * @throws IllegalArgumentException if more events than SIFEN admits are given
   * @throws TransportException if SIFEN could not be reached
   * @throws ProtocolException if SIFEN's answer is not a valid response
   */
  public EventResult sendEvents(List<byte[]> signedEvents) {
    return Events.send(transport, List.copyOf(signedEvents));
  }

  /** Builder of {@link SifenClient}. */
  public static final class Builder {

    private @Nullable Environment environment;
    private @Nullable KeyMaterial keyMaterial;
    private @Nullable KeyStore trustStore;
    private @Nullable URI baseUri;
    private Duration connectTimeout = Duration.ofSeconds(15);
    private Duration requestTimeout = Duration.ofSeconds(90);
    private @Nullable ProxySelector proxy;
    private Clock clock = Clock.systemUTC();
    private SecureRandom random = new SecureRandom();

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
     * The issuer's certificate and private key, used for mutual TLS (required).
     *
     * @param keyMaterial the key material
     * @return this builder
     */
    public Builder keyMaterial(KeyMaterial keyMaterial) {
      this.keyMaterial = Objects.requireNonNull(keyMaterial, "keyMaterial");
      return this;
    }

    /**
     * Certificate authorities trusted for SIFEN's server certificate (defaults to the JDK's).
     *
     * @param trustStore the trust store
     * @return this builder
     */
    public Builder trustStore(KeyStore trustStore) {
      this.trustStore = Objects.requireNonNull(trustStore, "trustStore");
      return this;
    }

    /**
     * Overrides the base address of the web services, for example to use a simulated SIFEN in
     * tests. Not needed for the real environments.
     *
     * @param baseUri the base address
     * @return this builder
     */
    public Builder baseUri(URI baseUri) {
      this.baseUri = Objects.requireNonNull(baseUri, "baseUri");
      return this;
    }

    /**
     * Connection timeout (default 15 seconds).
     *
     * @param timeout the timeout
     * @return this builder
     */
    public Builder connectTimeout(Duration timeout) {
      this.connectTimeout = Objects.requireNonNull(timeout, "timeout");
      return this;
    }

    /**
     * Timeout of a whole request (default 90 seconds).
     *
     * @param timeout the timeout
     * @return this builder
     */
    public Builder requestTimeout(Duration timeout) {
      this.requestTimeout = Objects.requireNonNull(timeout, "timeout");
      return this;
    }

    /**
     * Proxy to reach SIFEN.
     *
     * @param proxy the proxy selector
     * @return this builder
     */
    public Builder proxy(ProxySelector proxy) {
      this.proxy = Objects.requireNonNull(proxy, "proxy");
      return this;
    }

    /**
     * Clock used to time exchanges (defaults to the system clock).
     *
     * @param clock the clock
     * @return this builder
     */
    public Builder clock(Clock clock) {
      this.clock = Objects.requireNonNull(clock, "clock");
      return this;
    }

    /**
     * Builds the client.
     *
     * @return the client
     * @throws NullPointerException if the environment or the key material is missing
     */
    public SifenClient build() {
      return new SifenClient(this);
    }
  }
}
