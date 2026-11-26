/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.testing;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsExchange;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;

/**
 * A simulated SIFEN: an HTTPS server on the loopback interface that answers like the SIFEN web
 * services, to test an integration without network and without a real certificate.
 *
 * <p>Like SIFEN, it requires mutual TLS: connections without a client certificate are refused in
 * the handshake. By default it accepts <b>any</b> client certificate presented, so tests can use a
 * synthetic self-signed one without loading a certification authority; {@link
 * Builder#trustedClientCertificates(KeyStore)} restricts it. Its own certificate is synthetic, for
 * {@code localhost} and {@code 127.0.0.1}; trust it with {@link #serverTrustStore()}.
 *
 * <pre>{@code
 * try (SimulatedSifen sifen = new SimulatedSifen()) {
 *   sifen.start();
 *   SifenClient client = SifenClient.builder()
 *       .environment(Environment.TEST)
 *       .keyMaterial(syntheticKeyMaterial)
 *       .baseUri(sifen.baseUri())
 *       .trustStore(sifen.serverTrustStore())
 *       .build();
 *
 *   sifen.enqueue(SimulatedSifen.RECEPTION, SifenResponses.receptionRejected("1001", "CDC duplicado"));
 *   ReceptionResult result = client.send(signed);
 *
 *   RecordedRequest request = sifen.recordedRequests().get(0);
 * }
 * }</pre>
 *
 * <p>Each request is recorded ({@link #recordedRequests()}) and answered with the next response
 * {@linkplain #enqueue(String, SimulatedResponse) enqueued} for its path, or else with the default
 * response of the service ({@link #defaultResponse(String, SimulatedResponse)} changes it). The
 * defaults behave like a SIFEN that approves everything and remembers it:
 *
 * <ul>
 *   <li>{@link #RECEPTION}: {@link SifenResponses#receptionApproved()}.
 *   <li>{@link #BATCH}: {@link SifenResponses#batchAccepted()}, remembering the CDCs of the batch.
 *   <li>{@link #BATCH_RESULT}: {@link SifenResponses#batchProcessed()} for a batch it accepted,
 *       {@link SifenResponses#batchNotFound()} otherwise.
 *   <li>{@link #DOCUMENT_QUERY}: {@link SifenResponses#documentFound()} for a document it approved,
 *       {@link SifenResponses#documentNotFound()} otherwise.
 *   <li>{@link #RUC_QUERY}: {@link SifenResponses#rucFound(String)} with a synthetic name.
 *   <li>{@link #EVENTS}: {@link SifenResponses#eventRegistered()}.
 *   <li>Any other path: HTTP 404.
 * </ul>
 *
 * <p>It does not validate requests against the XSDs, check signatures or apply SIFEN's business
 * rules: it answers what the test programs. Thread-safe. Every instance is independent (no global
 * state), so tests can run in parallel, each with its own simulated SIFEN.
 */
public final class SimulatedSifen implements AutoCloseable {

  /** Path of the synchronous reception service ({@code siRecepDE}). */
  public static final String RECEPTION = "/de/ws/sync/recibe.wsdl";

  /** Path of the batch reception service ({@code siRecepLoteDE}). */
  public static final String BATCH = "/de/ws/async/recibe-lote.wsdl";

  /** Path of the batch result service ({@code siResultLoteDE}). */
  public static final String BATCH_RESULT = "/de/ws/consultas/consulta-lote.wsdl";

  /** Path of the document query service ({@code siConsDE}). */
  public static final String DOCUMENT_QUERY = "/de/ws/consultas/consulta.wsdl";

  /** Path of the taxpayer query service ({@code siConsRUC}). */
  public static final String RUC_QUERY = "/de/ws/consultas/consulta-ruc.wsdl";

  /** Path of the event reception service ({@code siRecepEvento}). */
  public static final String EVENTS = "/de/ws/eventos/evento.wsdl";

  private static final String SERVER_KEY_STORE = "simulated-sifen-server.p12";
  private static final String SERVER_ALIAS = "simulated-sifen";
  private static final char[] SERVER_PASSWORD = "prueba-sintetica".toCharArray();

  private final @Nullable KeyStore trustedClientCertificates;
  private final State state;
  private final Map<String, Deque<SimulatedResponse>> queues = new ConcurrentHashMap<>();
  private final Map<String, SimulatedResponse> defaults = new ConcurrentHashMap<>();
  private final List<RecordedRequest> recorded = new CopyOnWriteArrayList<>();
  private final Object lifecycle = new Object();
  private @Nullable HttpsServer server;
  private @Nullable ExecutorService executor;
  private boolean closed;

  /**
   * Creates a simulated SIFEN that accepts any client certificate and uses the system clock. Call
   * {@link #start()} before using it.
   */
  public SimulatedSifen() {
    this(builder());
  }

  private SimulatedSifen(Builder b) {
    this.trustedClientCertificates = b.trustedClientCertificates;
    this.state = new State(b.clock);
    defaults.put(RECEPTION, SifenResponses.receptionApproved());
    defaults.put(BATCH, SifenResponses.batchAccepted());
    defaults.put(BATCH_RESULT, SifenResponses.batchResultFromState());
    defaults.put(DOCUMENT_QUERY, SifenResponses.documentFromState());
    defaults.put(RUC_QUERY, SifenResponses.rucFound(SifenResponses.SYNTHETIC_NAME));
    defaults.put(EVENTS, SifenResponses.eventRegistered());
  }

  /**
   * Starts building a simulated SIFEN with non-default settings.
   *
   * @return a builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Starts the server on {@code 127.0.0.1} and an ephemeral port.
   *
   * @return this simulated SIFEN
   * @throws IllegalStateException if it was already started or closed
   * @throws UncheckedIOException if the port cannot be opened
   */
  public SimulatedSifen start() {
    synchronized (lifecycle) {
      if (closed || server != null) {
        throw new IllegalStateException("The simulated SIFEN was already started or closed");
      }
      SSLContext context = sslContext();
      try {
        InetAddress loopback = InetAddress.getByAddress("localhost", new byte[] {127, 0, 0, 1});
        HttpsServer s = HttpsServer.create(new InetSocketAddress(loopback, 0), 0);
        s.setHttpsConfigurator(
            new HttpsConfigurator(context) {
              @Override
              public void configure(HttpsParameters params) {
                SSLParameters parameters = getSSLContext().getDefaultSSLParameters();
                parameters.setProtocols(new String[] {"TLSv1.3", "TLSv1.2"});
                parameters.setNeedClientAuth(true);
                params.setSSLParameters(parameters);
              }
            });
        s.createContext("/", this::handle);
        AtomicInteger threads = new AtomicInteger();
        ExecutorService e =
            Executors.newCachedThreadPool(r -> {
              Thread t = new Thread(r, "simulated-sifen-" + threads.incrementAndGet());
              t.setDaemon(true);
              return t;
            });
        s.setExecutor(e);
        s.start();
        this.server = s;
        this.executor = e;
      } catch (UnknownHostException e) {
        throw new IllegalStateException(e);
      } catch (IOException e) {
        throw new UncheckedIOException("Could not start the simulated SIFEN", e);
      }
      return this;
    }
  }

  /**
   * The base address to give the client ({@code SifenClient.Builder#baseUri}), for example {@code
   * https://127.0.0.1:50123}.
   *
   * @return the base address
   * @throws IllegalStateException if it is not running
   */
  public URI baseUri() {
    return URI.create("https://127.0.0.1:" + port());
  }

  /**
   * The port the server listens on.
   *
   * @return the port
   * @throws IllegalStateException if it is not running
   */
  public int port() {
    synchronized (lifecycle) {
      if (server == null || closed) {
        throw new IllegalStateException("The simulated SIFEN is not running; call start()");
      }
      return server.getAddress().getPort();
    }
  }

  /**
   * A trust store with the synthetic certificate of this server, to give the client ({@code
   * SifenClient.Builder#trustStore}). A new instance on every call.
   *
   * @return the trust store
   */
  public KeyStore serverTrustStore() {
    try {
      Certificate certificate = serverKeyStore().getCertificate(SERVER_ALIAS);
      KeyStore trust = KeyStore.getInstance("PKCS12");
      trust.load(null, null);
      trust.setCertificateEntry(SERVER_ALIAS, certificate);
      return trust;
    } catch (GeneralSecurityException | IOException e) {
      throw new IllegalStateException("Could not build the trust store", e);
    }
  }

  /**
   * Enqueues a response for the next request to a path. Responses enqueued for the same path are
   * used in order, once each; when none is left, the path's default response is used.
   *
   * @param path the service path, for example {@link #RECEPTION}
   * @param response the response
   * @return this simulated SIFEN
   */
  public SimulatedSifen enqueue(String path, SimulatedResponse response) {
    Objects.requireNonNull(response, "response");
    queues.computeIfAbsent(normalize(path), p -> new ConcurrentLinkedDeque<>()).addLast(response);
    return this;
  }

  /**
   * Replaces the response used for a path when nothing is enqueued for it.
   *
   * @param path the service path, for example {@link #RUC_QUERY}
   * @param response the response
   * @return this simulated SIFEN
   */
  public SimulatedSifen defaultResponse(String path, SimulatedResponse response) {
    defaults.put(normalize(path), Objects.requireNonNull(response, "response"));
    return this;
  }

  /**
   * The requests received so far, in order of arrival (including those to unknown paths).
   *
   * @return an immutable copy
   */
  public List<RecordedRequest> recordedRequests() {
    return List.copyOf(recorded);
  }

  /**
   * The requests received so far for one path, in order of arrival.
   *
   * @param path the service path, for example {@link #BATCH}
   * @return an immutable list
   */
  public List<RecordedRequest> recordedRequests(String path) {
    String p = normalize(path);
    List<RecordedRequest> out = new ArrayList<>();
    for (RecordedRequest r : recorded) {
      if (r.path().equals(p)) {
        out.add(r);
      }
    }
    return List.copyOf(out);
  }

  /**
   * Stops the server, closing open connections and interrupting delayed responses. Calling it again
   * has no effect.
   */
  @Override
  public void close() {
    HttpsServer s;
    ExecutorService e;
    synchronized (lifecycle) {
      if (closed) {
        return;
      }
      closed = true;
      s = server;
      e = executor;
    }
    if (e != null) {
      e.shutdownNow();
    }
    if (s != null) {
      s.stop(0);
    }
  }

  @Override
  public String toString() {
    synchronized (lifecycle) {
      return "SimulatedSifen{"
          + (server == null
              ? "not started"
              : closed ? "closed" : "port=" + server.getAddress().getPort())
          + "}";
    }
  }

  // --- request handling ------------------------------------------------------------------------

  private void handle(HttpExchange exchange) {
    try {
      byte[] body;
      try (InputStream in = exchange.getRequestBody()) {
        body = in.readAllBytes();
      }
      String path = normalize(exchange.getRequestURI().getPath());
      Element payload = Interaction.payloadOf(body);
      RecordedRequest request =
          new RecordedRequest(
              exchange.getRequestMethod(),
              path,
              exchange.getRequestHeaders().getFirst("Content-Type"),
              body,
              clientChain(exchange),
              payload == null ? null : Xml.text(payload, "dId"),
              state.clock().instant());
      recorded.add(request);

      SimulatedResponse response = next(path);
      if (!response.delay().isZero()) {
        Thread.sleep(response.delay().toMillis());
      }
      SimulatedResponse.Rendered rendered;
      try {
        rendered = response.render(new Interaction(request, payload, state));
      } catch (RuntimeException e) {
        rendered =
            new SimulatedResponse.Rendered(
                500,
                "text/plain; charset=utf-8",
                ("Simulated SIFEN could not render " + response + ": " + e.getMessage())
                    .getBytes(StandardCharsets.UTF_8));
      }
      exchange.getResponseHeaders().set("Content-Type", rendered.contentType);
      exchange.sendResponseHeaders(
          rendered.httpStatus, rendered.body.length == 0 ? -1 : rendered.body.length);
      try (OutputStream out = exchange.getResponseBody()) {
        out.write(rendered.body);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      // The client went away (for example, after its timeout): nothing to answer.
    } finally {
      exchange.close();
    }
  }

  private SimulatedResponse next(String path) {
    Deque<SimulatedResponse> queue = queues.get(path);
    SimulatedResponse enqueued = queue == null ? null : queue.pollFirst();
    if (enqueued != null) {
      return enqueued;
    }
    SimulatedResponse fallback = defaults.get(path);
    return fallback != null ? fallback : SifenResponses.httpError(404);
  }

  private static List<X509Certificate> clientChain(HttpExchange exchange) {
    List<X509Certificate> chain = new ArrayList<>();
    if (exchange instanceof HttpsExchange) {
      try {
        for (Certificate c : ((HttpsExchange) exchange).getSSLSession().getPeerCertificates()) {
          if (c instanceof X509Certificate) {
            chain.add((X509Certificate) c);
          }
        }
      } catch (SSLPeerUnverifiedException e) {
        throw new IllegalStateException("A request arrived without client certificate", e);
      }
    }
    return chain;
  }

  private static String normalize(String path) {
    Objects.requireNonNull(path, "path");
    return path.startsWith("/") ? path : "/" + path;
  }

  // --- TLS ---------------------------------------------------------------------------------------

  private SSLContext sslContext() {
    try {
      KeyManagerFactory kmf =
          KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      kmf.init(serverKeyStore(), SERVER_PASSWORD);
      TrustManager[] trustManagers;
      if (trustedClientCertificates != null) {
        TrustManagerFactory tmf =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustedClientCertificates);
        trustManagers = tmf.getTrustManagers();
      } else {
        trustManagers = new TrustManager[] {new AnyClientCertificate()};
      }
      SSLContext context = SSLContext.getInstance("TLS");
      context.init(kmf.getKeyManagers(), trustManagers, null);
      return context;
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Could not set up TLS for the simulated SIFEN", e);
    }
  }

  private static KeyStore serverKeyStore() {
    try (InputStream in = SimulatedSifen.class.getResourceAsStream(SERVER_KEY_STORE)) {
      if (in == null) {
        throw new IllegalStateException("Missing resource " + SERVER_KEY_STORE);
      }
      KeyStore store = KeyStore.getInstance("PKCS12");
      store.load(in, SERVER_PASSWORD);
      return store;
    } catch (GeneralSecurityException | IOException e) {
      throw new IllegalStateException("Could not load the synthetic server certificate", e);
    }
  }

  /**
   * Accepts any client certificate that is presented (the handshake still fails without one, as
   * {@code needClientAuth} is set). Only for this simulated server, never for real connections.
   */
  private static final class AnyClientCertificate implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {
      if (chain == null || chain.length == 0) {
        throw new CertificateException("No client certificate");
      }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {
      throw new CertificateException("Not used for server certificates");
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
    }
  }

  /** Builder of {@link SimulatedSifen}. */
  public static final class Builder {

    private @Nullable KeyStore trustedClientCertificates;
    private Clock clock = Clock.systemUTC();

    private Builder() {}

    /**
     * Accepts only client certificates issued by (or equal to) the certificates of this trust
     * store, validated with the JDK's PKIX rules (including validity dates). Without it, any client
     * certificate presented is accepted.
     *
     * @param trustStore the trusted certificates
     * @return this builder
     */
    public Builder trustedClientCertificates(KeyStore trustStore) {
      this.trustedClientCertificates = Objects.requireNonNull(trustStore, "trustStore");
      return this;
    }

    /**
     * The clock for processing dates ({@code dFecProc}) and {@link RecordedRequest#receivedAt()}
     * (defaults to the system clock).
     *
     * @param clock the clock
     * @return this builder
     */
    public Builder clock(Clock clock) {
      this.clock = Objects.requireNonNull(clock, "clock");
      return this;
    }

    /**
     * Builds the simulated SIFEN, not yet started.
     *
     * @return the simulated SIFEN
     */
    public SimulatedSifen build() {
      return new SimulatedSifen(this);
    }
  }
}
