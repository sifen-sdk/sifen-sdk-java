/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.internal;

import io.github.sifensdk.client.Exchange;
import io.github.sifensdk.client.ProtocolException;
import io.github.sifensdk.client.Status;
import java.text.Normalizer;
import java.util.Locale;
import org.jspecify.annotations.Nullable;

/**
 * Reads the state SIFEN writes in {@code dEstRes} for a document of a batch (CRSch051) or for an
 * event (GRSch030).
 *
 * <p>The documented values are "Aprobado", "Aprobado con observación" and "Rechazado" (MT150
 * CRSch051 and GRSch030). They are compared ignoring case, accents and repeated spaces, so that
 * "Aprobado con observacion" (without accent) is read too. A value that is not one of them is never
 * guessed: it is a {@link ProtocolException}.
 */
public final class BatchStatus {

  private BatchStatus() {}

  /**
   * The state of a document of a batch.
   *
   * @param value the text of {@code dEstRes}
   * @param exchange the exchange, for the exception
   * @return the state
   * @throws ProtocolException if the value is not a documented state
   */
  public static Status ofDocument(String value, Exchange exchange) {
    Status status = ofText(value);
    if (status == null) {
      throw unknown(value, exchange);
    }
    return status;
  }

  /**
   * The state written as one of the three documented texts, or null.
   *
   * @param value the text
   * @return the state, or null if it is not a documented text
   */
  static @Nullable Status ofText(String value) {
    switch (normalize(value)) {
      case "aprobado":
        return Status.APPROVED;
      case "aprobado con observacion":
        return Status.APPROVED_WITH_OBSERVATION;
      case "rechazado":
        return Status.REJECTED;
      default:
        return null;
    }
  }

  /**
   * The exception for a state that cannot be classified. The state is a short code word of SIFEN,
   * not document content, so it is included (shortened) to help diagnosis.
   *
   * @param value the text
   * @param exchange the exchange
   * @return the exception
   */
  static ProtocolException unknown(String value, Exchange exchange) {
    String shown = value.length() > 40 ? value.substring(0, 40) + "..." : value;
    return new ProtocolException(
        "SIFEN answered with a state (dEstRes) that is not documented: \"" + shown + "\"",
        exchange);
  }

  /**
   * Lower case, without accents, with single spaces.
   *
   * @param value the text
   * @return the normalized text
   */
  static String normalize(String value) {
    String decomposed = Normalizer.normalize(value.trim(), Normalizer.Form.NFD);
    return decomposed.replaceAll("\\p{M}", "").replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
  }
}
