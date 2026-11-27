/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.conformance;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.github.sifensdk.client.DocumentResult;
import io.github.sifensdk.client.EventReceptionResult;
import io.github.sifensdk.client.EventResult;
import io.github.sifensdk.client.Exchange;
import io.github.sifensdk.client.ReceptionResult;
import io.github.sifensdk.client.ResultMessage;
import io.github.sifensdk.client.Status;
import io.github.sifensdk.client.internal.Events;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Runs the {@code respuestas} cases of the specification's conformance battery ({@code
 * spec/conformidad/respuestas}) on the client: one dynamic test per case.
 *
 * <p>Each case is a SIFEN answer (the SOAP envelope, next to its JSON) and the expected reading:
 * per document or event, its id, state ({@code aprobado}, {@code aprobado_con_observacion}, {@code
 * rechazado}), protocol and result codes in order. The answer is read by the same code the client
 * uses for a real call: the reception answer by {@code Reception.parse} (package-private, reached
 * by reflection inside the module) and the event answer by {@link Events#parse}.
 *
 * <p>The cases are read from the directory in the system property {@code sifen.conformance.dir}, by
 * default the {@code conformidad} folder of the specification submodule ({@code
 * ../spec/conformidad} from this module); without {@code respuestas} cases the test is skipped.
 */
class ResponseConformanceTest {

  static Path directory() {
    return Path.of(System.getProperty("sifen.conformance.dir", "../spec/conformidad"));
  }

  @TestFactory
  Stream<DynamicTest> responseCases() throws IOException {
    Path folder = directory().resolve("respuestas");
    List<Path> cases = new ArrayList<>();
    if (Files.isDirectory(folder)) {
      try (Stream<Path> files = Files.list(folder)) {
        files.filter(p -> p.toString().endsWith(".json")).sorted().forEach(cases::add);
      }
    }
    assumeTrue(!cases.isEmpty(), "No response cases in " + folder.toAbsolutePath());
    return cases.stream()
        .map(p -> DynamicTest.dynamicTest("respuestas/" + p.getFileName(), () -> run(p)));
  }

  private static void run(Path casePath) throws Exception {
    Map<String, Object> c =
        CaseJson.object(
            CaseJson.parse(new String(Files.readAllBytes(casePath), StandardCharsets.UTF_8)));
    Map<String, Object> input = CaseJson.object(c.get("entrada"));
    byte[] xml = Files.readAllBytes(casePath.resolveSibling((String) c.get("xml")));
    List<Object> expected = CaseJson.list(CaseJson.object(c.get("esperado")).get("resultados"));

    List<Read> actual = new ArrayList<>();
    if ("recepcion".equals(input.get("servicio"))) {
      DocumentResult r = reception(exchange(xml), (String) input.get("cdc")).document();
      actual.add(new Read(r.cdc(), r.status(), r.authorizationProtocol(), r.messages()));
    } else {
      EventReceptionResult r = Events.parse(exchange(xml));
      for (EventResult e : r.events()) {
        actual.add(new Read(e.id(), e.status(), e.authorizationProtocol(), e.messages()));
      }
    }

    List<String> problems = new ArrayList<>();
    if (actual.size() != expected.size()) {
      problems.add("expected " + expected.size() + " results, got " + actual.size());
    }
    for (int i = 0; i < Math.min(actual.size(), expected.size()); i++) {
      Map<String, Object> e = CaseJson.object(expected.get(i));
      Read a = actual.get(i);
      String where = "result " + e.get("id") + ": ";
      if (!a.id.equals(e.get("id"))) {
        problems.add(where + "id " + a.id);
      }
      if (!state(a.status).equals(e.get("estado"))) {
        problems.add(where + "state " + state(a.status) + ", expected " + e.get("estado"));
      }
      String protocol = a.protocol == null || a.protocol.isEmpty() ? null : a.protocol;
      Object expectedProtocol = e.get("protocolo");
      if (protocol == null ? expectedProtocol != null : !protocol.equals(expectedProtocol)) {
        problems.add(where + "protocol " + protocol + ", expected " + expectedProtocol);
      }
      List<String> codes =
          a.messages.stream().map(ResultMessage::code).collect(Collectors.toList());
      List<Object> expectedCodes =
          CaseJson.list(e.get("mensajes")).stream()
              .map(m -> CaseJson.object(m).get("codigo"))
              .collect(Collectors.toList());
      if (!codes.equals(expectedCodes)) {
        problems.add(where + "codes " + codes + ", expected " + expectedCodes);
      }
    }
    if (!problems.isEmpty()) {
      fail(c.get("descripcion") + "\n  " + String.join("\n  ", problems));
    }
  }

  /** The state as the specification names it. */
  static String state(Status status) {
    switch (status) {
      case APPROVED:
        return "aprobado";
      case APPROVED_WITH_OBSERVATION:
        return "aprobado_con_observacion";
      case REJECTED:
        return "rechazado";
      default:
        return "en_proceso";
    }
  }

  private static Exchange exchange(byte[] response) {
    return new Exchange(
        "1",
        URI.create("https://sifen-test.set.gov.py/de/ws/conformidad"),
        new byte[0],
        200,
        response,
        Instant.EPOCH,
        Duration.ZERO);
  }

  private static ReceptionResult reception(Exchange exchange, String cdc) throws Exception {
    Class<?> reception = Class.forName("io.github.sifensdk.client.internal.Reception");
    Method parse = reception.getDeclaredMethod("parse", Exchange.class, String.class);
    parse.setAccessible(true);
    try {
      return (ReceptionResult) parse.invoke(null, exchange, cdc);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      }
      throw new UncheckedIOException(new IOException(cause));
    }
  }

  /** What the client read for one document or event. */
  private static final class Read {
    final String id;
    final Status status;
    final @Nullable String protocol;
    final List<ResultMessage> messages;

    Read(String id, Status status, @Nullable String protocol, List<ResultMessage> messages) {
      this.id = id;
      this.status = status;
      this.protocol = protocol;
      this.messages = messages;
    }
  }
}
