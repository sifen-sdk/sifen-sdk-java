/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.internal;

import io.github.sifensdk.client.Exchange;
import io.github.sifensdk.client.TransportException;
import io.github.sifensdk.core.signing.KeyMaterial;
import java.io.IOException;
import java.net.ConnectException;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;
import org.jspecify.annotations.Nullable;

/**
 * HTTPS transport to SIFEN with mutual TLS for one issuer.
 *
 * <p>Each instance owns its own {@link HttpClient} and {@link SSLContext} built from the issuer's
 * key material, so several issuers can work in the same process without sharing a certificate
 * (ADR-0004). The private key is used through a key manager and never exported, so keys held in a
 * token or HSM work too (ADR-0008). Immutable and thread-safe.
 */
public final class Transport {

  /** Media type of SOAP 1.2 (MT150 7.4: SOAP version 1.2). */
  public static final String SOAP_MEDIA_TYPE = "application/soap+xml; charset=utf-8";

  private static final String ALIAS = "sifen";

  private final URI baseUri;
  private final HttpClient http;
  private final Duration requestTimeout;
  private final Clock clock;
  private final SecureRandom random;

  /**
   * Creates the transport.
   *
   * @param baseUri base address of the web services, for example {@code
   *     https://sifen-test.set.gov.py}
   * @param keyMaterial the issuer's certificate and private key, presented in the TLS handshake
   * @param trustStore trusted certificate authorities for SIFEN's server certificate, or null for
   *     the JDK's default trust store
   * @param connectTimeout connection timeout
   * @param requestTimeout timeout of a whole request
   * @param proxy proxy selector, or null for none
   * @param clock clock used to time exchanges
   * @param random source of request ids
   */
  public Transport(
      URI baseUri,
      KeyMaterial keyMaterial,
      @Nullable KeyStore trustStore,
      Duration connectTimeout,
      Duration requestTimeout,
      @Nullable ProxySelector proxy,
      Clock clock,
      SecureRandom random) {
    this.baseUri = Objects.requireNonNull(baseUri, "baseUri");
    this.requestTimeout = Objects.requireNonNull(requestTimeout, "requestTimeout");
    this.clock = Objects.requireNonNull(clock, "clock");
    this.random = Objects.requireNonNull(random, "random");
    SSLParameters parameters = new SSLParameters();
    parameters.setProtocols(new String[] {"TLSv1.3", "TLSv1.2"});
    HttpClient.Builder builder =
        HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Objects.requireNonNull(connectTimeout, "connectTimeout"))
            .followRedirects(HttpClient.Redirect.NEVER)
            .sslContext(sslContext(Objects.requireNonNull(keyMaterial, "keyMaterial"), trustStore))
            .sslParameters(parameters);
    if (proxy != null) {
      builder.proxy(proxy);
    }
    this.http = builder.build();
  }

  /**
   * A new request id ({@code dId}): 15 random digits, the maximum length the Manual allows.
   *
   * @return the request id
   */
  public String newRequestId() {
    StringBuilder sb = new StringBuilder(15);
    sb.append(1 + random.nextInt(9));
    for (int i = 1; i < 15; i++) {
      sb.append(random.nextInt(10));
    }
    return sb.toString();
  }

  /**
   * Sends a SOAP request and returns the raw exchange, whatever the HTTP status.
   *
   * @param path path of the service, for example {@code /de/ws/sync/recibe.wsdl}
   * @param requestId the request id included in the body
   * @param body the complete SOAP envelope
   * @return the exchange
   * @throws TransportException if no response was received
   */
  public Exchange post(String path, String requestId, byte[] body) {
    URI endpoint = baseUri.resolve(path);
    HttpRequest request =
        HttpRequest.newBuilder(endpoint)
            .timeout(requestTimeout)
            .header("Content-Type", SOAP_MEDIA_TYPE)
            .POST(HttpRequest.BodyPublishers.ofByteArray(body))
            .build();
    Instant start = clock.instant();
    try {
      HttpResponse<byte[]> response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
      return new Exchange(
          requestId,
          endpoint,
          body,
          response.statusCode(),
          response.body(),
          start,
          Duration.between(start, clock.instant()));
    } catch (HttpConnectTimeoutException | ConnectException | SSLHandshakeException e) {
      // Nothing reached SIFEN: safe to retry later.
      throw new TransportException(
          "Could not connect to SIFEN at " + endpoint + ": " + e.getClass().getSimpleName(),
          false,
          e);
    } catch (IOException e) {
      throw new TransportException(
          "The connection to SIFEN failed after sending the request to "
              + endpoint
              + " ("
              + e.getClass().getSimpleName()
              + "); its outcome is unknown, query before resending",
          true,
          e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new TransportException(
          "Interrupted while waiting for SIFEN; the outcome is unknown", true, e);
    }
  }

  private static SSLContext sslContext(KeyMaterial keyMaterial, @Nullable KeyStore trustStore) {
    try {
      TrustManagerFactory tmf =
          TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      tmf.init(trustStore);
      SSLContext context = SSLContext.getInstance("TLS");
      context.init(
          new javax.net.ssl.KeyManager[] {new IssuerKeyManager(keyMaterial)},
          tmf.getTrustManagers(),
          null);
      return context;
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Could not set up TLS for SIFEN", e);
    }
  }

  /** Always presents the issuer's certificate; the key never leaves its provider. */
  private static final class IssuerKeyManager extends X509ExtendedKeyManager {

    private final KeyMaterial keyMaterial;

    IssuerKeyManager(KeyMaterial keyMaterial) {
      this.keyMaterial = keyMaterial;
    }

    @Override
    public String[] getClientAliases(String keyType, Principal @Nullable [] issuers) {
      return new String[] {ALIAS};
    }

    @Override
    public String chooseClientAlias(
        String[] keyType, Principal @Nullable [] issuers, @Nullable Socket socket) {
      return ALIAS;
    }

    @Override
    public String chooseEngineClientAlias(
        String[] keyType, Principal @Nullable [] issuers, @Nullable SSLEngine engine) {
      return ALIAS;
    }

    @Override
    public String @Nullable [] getServerAliases(String keyType, Principal @Nullable [] issuers) {
      return null;
    }

    @Override
    public @Nullable String chooseServerAlias(
        String keyType, Principal @Nullable [] issuers, @Nullable Socket socket) {
      return null;
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) {
      List<X509Certificate> chain = keyMaterial.certificateChain();
      return chain.toArray(new X509Certificate[0]);
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
      return keyMaterial.privateKey();
    }
  }
}
