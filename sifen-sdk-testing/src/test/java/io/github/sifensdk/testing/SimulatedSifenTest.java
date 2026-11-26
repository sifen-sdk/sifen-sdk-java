/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.testing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SimulatedSifenTest {

  private SimulatedSifen sifen;

  @AfterEach
  void close() {
    if (sifen != null) {
      sifen.close();
    }
  }

  @Test
  void refusesConnectionsWithoutClientCertificate() throws Exception {
    sifen = new SimulatedSifen().start();
    SSLContext tls = SyntheticClient.tls(sifen, false);

    assertThatThrownBy(
            () -> {
              try (SSLSocket socket =
                  (SSLSocket) tls.getSocketFactory().createSocket("127.0.0.1", sifen.port())) {
                socket.startHandshake();
                OutputStream out = socket.getOutputStream();
                out.write(
                    ("POST "
                            + SimulatedSifen.RECEPTION
                            + " HTTP/1.1\r\nHost: 127.0.0.1\r\n"
                            + "Content-Length: 0\r\n\r\n")
                        .getBytes(StandardCharsets.US_ASCII));
                out.flush();
                // With TLS 1.3 the server's refusal arrives as an alert after the client finishes.
                if (socket.getInputStream().read() == -1) {
                  throw new IOException("closed by the server");
                }
              }
            })
        .isInstanceOf(IOException.class);
    assertThat(sifen.recordedRequests()).isEmpty();
  }

  @Test
  void recordsTheRequestAndAnswersWithTheDefaultResponse() throws Exception {
    sifen = new SimulatedSifen().start();
    byte[] body = Requests.reception("12345", Requests.CDC);

    Response response = post(SimulatedSifen.RECEPTION, body, SyntheticClient.tls(sifen, true), 0);

    assertThat(response.status).isEqualTo(200);
    assertThat(response.contentType).startsWith("application/soap+xml");
    assertThat(response.body).contains("rRetEnviDe").contains(Requests.CDC).contains("Aprobado");
    List<RecordedRequest> recorded = sifen.recordedRequests();
    assertThat(recorded).hasSize(1);
    RecordedRequest request = recorded.get(0);
    assertThat(request.method()).isEqualTo("POST");
    assertThat(request.path()).isEqualTo(SimulatedSifen.RECEPTION);
    assertThat(request.body()).isEqualTo(body);
    assertThat(request.requestId()).isEqualTo("12345");
    assertThat(request.clientCertificate()).isEqualTo(SyntheticClient.certificate());
    assertThat(request.receivedAt()).isBeforeOrEqualTo(Instant.now());
    assertThat(request.toString()).doesNotContain(Requests.CDC);
    assertThat(sifen.recordedRequests(SimulatedSifen.RECEPTION)).hasSize(1);
    assertThat(sifen.recordedRequests(SimulatedSifen.EVENTS)).isEmpty();
  }

  @Test
  void enqueuedResponsesAreUsedOnceInOrder() throws Exception {
    sifen = new SimulatedSifen().start();
    sifen.enqueue(SimulatedSifen.RECEPTION, SifenResponses.balancerHtmlPage());
    sifen.enqueue(
        SimulatedSifen.RECEPTION, SifenResponses.receptionRejected("1001", "CDC duplicado"));
    SSLContext tls = SyntheticClient.tls(sifen, true);
    byte[] body = Requests.reception("1", Requests.CDC);

    Response html = post(SimulatedSifen.RECEPTION, body, tls, 0);
    Response rejected = post(SimulatedSifen.RECEPTION, body, tls, 0);
    Response fallback = post(SimulatedSifen.RECEPTION, body, tls, 0);

    assertThat(html.contentType).startsWith("text/html");
    assertThat(rejected.body).contains("Rechazado").contains("1001");
    assertThat(fallback.body).contains(">Aprobado<");
  }

  @Test
  void defaultResponseCanBeReplacedAndUnknownPathsAre404() throws Exception {
    sifen = new SimulatedSifen().start();
    sifen.defaultResponse(SimulatedSifen.RUC_QUERY, SifenResponses.rucNotFound());
    SSLContext tls = SyntheticClient.tls(sifen, true);

    assertThat(post(SimulatedSifen.RUC_QUERY, Requests.rucQuery("1", "44444401"), tls, 0).body)
        .contains("0500");
    assertThat(post("/de/ws/otro.wsdl", new byte[0], tls, 0).status).isEqualTo(404);
    assertThat(sifen.recordedRequests()).hasSize(2);
  }

  @Test
  void delayedResponsesLetTheClientTimeOutAndCloseDoesNotWait() throws Exception {
    sifen = new SimulatedSifen().start();
    sifen.enqueue(
        SimulatedSifen.RECEPTION,
        SifenResponses.receptionApproved().delayed(Duration.ofSeconds(30)));
    SSLContext tls = SyntheticClient.tls(sifen, true);

    assertThatThrownBy(
            () -> post(SimulatedSifen.RECEPTION, Requests.reception("1", Requests.CDC), tls, 300))
        .isInstanceOf(SocketTimeoutException.class);

    long start = System.nanoTime();
    sifen.close();
    assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(Duration.ofSeconds(5));
  }

  @Test
  void canRestrictTheTrustedClientCertificates() throws Exception {
    KeyStore onlySynthetic = KeyStore.getInstance("PKCS12");
    onlySynthetic.load(null, null);
    onlySynthetic.setCertificateEntry("cliente", SyntheticClient.certificate());
    sifen = SimulatedSifen.builder().trustedClientCertificates(onlySynthetic).build().start();
    assertThat(
            post(
                    SimulatedSifen.RUC_QUERY,
                    Requests.rucQuery("1", "44444401"),
                    SyntheticClient.tls(sifen, true),
                    0)
                .status)
        .isEqualTo(200);
    sifen.close();

    KeyStore someoneElse = new SimulatedSifen().serverTrustStore();
    sifen = SimulatedSifen.builder().trustedClientCertificates(someoneElse).build().start();
    SSLContext tls = SyntheticClient.tls(sifen, true);
    assertThatThrownBy(
            () -> post(SimulatedSifen.RUC_QUERY, Requests.rucQuery("1", "44444401"), tls, 0))
        .isInstanceOf(IOException.class);
    assertThat(sifen.recordedRequests()).isEmpty();
  }

  @Test
  void lifecycle() {
    SimulatedSifen notStarted = new SimulatedSifen();
    assertThatThrownBy(notStarted::baseUri).isInstanceOf(IllegalStateException.class);
    notStarted.start();
    assertThat(notStarted.baseUri().toString()).startsWith("https://127.0.0.1:");
    assertThatThrownBy(notStarted::start).isInstanceOf(IllegalStateException.class);
    notStarted.close();
    notStarted.close();
    assertThatThrownBy(notStarted::baseUri).isInstanceOf(IllegalStateException.class);
  }

  // --- helpers ---------------------------------------------------------------------------------

  private Response post(String path, byte[] body, SSLContext tls, int readTimeoutMillis)
      throws IOException {
    HttpsURLConnection connection =
        (HttpsURLConnection) sifen.baseUri().resolve(path).toURL().openConnection();
    connection.setSSLSocketFactory(tls.getSocketFactory());
    connection.setRequestMethod("POST");
    connection.setDoOutput(true);
    connection.setConnectTimeout(5000);
    connection.setReadTimeout(readTimeoutMillis);
    connection.setRequestProperty("Content-Type", "application/soap+xml; charset=utf-8");
    try (OutputStream out = connection.getOutputStream()) {
      out.write(body);
    }
    int status = connection.getResponseCode();
    InputStream in = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
    String text = in == null ? "" : new String(in.readAllBytes(), StandardCharsets.UTF_8);
    String contentType = connection.getContentType();
    connection.disconnect();
    return new Response(status, contentType == null ? "" : contentType, text);
  }

  private static final class Response {
    final int status;
    final String contentType;
    final String body;

    Response(int status, String contentType, String body) {
      this.status = status;
      this.contentType = contentType;
      this.body = body;
    }
  }
}
