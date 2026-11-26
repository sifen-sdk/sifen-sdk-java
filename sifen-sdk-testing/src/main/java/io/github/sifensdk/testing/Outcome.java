/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.testing;

import io.github.sifensdk.client.ResultMessage;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * The outcome the simulated SIFEN reports for one document or one event: approved, approved with
 * observation, or rejected, with its result messages ({@code dCodRes}/{@code dMsgRes}).
 *
 * <p>Used by {@link SifenResponses#reception(Outcome)}, {@link SifenResponses#batchProcessed(
 * Outcome...)} and {@link SifenResponses#events(Outcome...)}. By default an outcome applies to the
 * document or event found in the request (by position, for batches and events); {@link
 * #forId(String)} pins it to a specific CDC or event id instead.
 *
 * <p>Immutable.
 */
public final class Outcome {

  enum Kind {
    APPROVED("Aprobado"),
    APPROVED_WITH_OBSERVATION("Aprobado con observación"),
    REJECTED("Rechazado");

    /** Text of {@code dEstRes} (MT150 PP050, CRSch051, GRSch030). */
    final String estRes;

    Kind(String estRes) {
      this.estRes = estRes;
    }
  }

  private final Kind kind;
  private final List<ResultMessage> messages;
  private final @Nullable String id;

  private Outcome(Kind kind, List<ResultMessage> messages, @Nullable String id) {
    this.kind = kind;
    this.messages = List.copyOf(messages);
    this.id = id;
  }

  /**
   * Approved ({@code dEstRes} "Aprobado"), with the service's success message: 0260 for documents,
   * 0600 for events.
   *
   * @return the outcome
   */
  public static Outcome approved() {
    return new Outcome(Kind.APPROVED, List.of(), null);
  }

  /**
   * Approved with observation ({@code dEstRes} "Aprobado con observación").
   *
   * @param code the observation code, for example {@code 1005} (late transmission of the document)
   *     or {@code 4103} (late event)
   * @param message the observation message
   * @return the outcome
   * @throws IllegalArgumentException if the code is not four digits or the message is empty or
   *     longer than 255 characters (the limits of the XSDs)
   */
  public static Outcome approvedWithObservation(String code, String message) {
    return new Outcome(Kind.APPROVED_WITH_OBSERVATION, List.of(message(code, message)), null);
  }

  /**
   * Rejected ({@code dEstRes} "Rechazado"). SIFEN informs only the first rejection reason.
   *
   * @param code the rejection code, for example {@code 1001} (duplicated CDC) or {@code 4002}
   *     (event for a CDC that does not exist)
   * @param message the rejection message
   * @return the outcome
   * @throws IllegalArgumentException if the code is not four digits or the message is empty or
   *     longer than 255 characters (the limits of the XSDs)
   */
  public static Outcome rejected(String code, String message) {
    return new Outcome(Kind.REJECTED, List.of(message(code, message)), null);
  }

  /**
   * The same outcome, applied to a specific document (CDC) or event id instead of the one found in
   * the request at the same position.
   *
   * @param id the CDC of the document, or the id of the event
   * @return a new outcome
   */
  public Outcome forId(String id) {
    return new Outcome(kind, messages, Objects.requireNonNull(id, "id"));
  }

  Kind kind() {
    return kind;
  }

  boolean approvedOrObserved() {
    return kind != Kind.REJECTED;
  }

  @Nullable String id() {
    return id;
  }

  /** The messages, or the given success message when an approval has none of its own. */
  List<ResultMessage> messages(ResultMessage success) {
    return messages.isEmpty() ? List.of(success) : messages;
  }

  /** Checks the limits every response XSD sets on result messages: four digits, 1 to 255 chars. */
  static ResultMessage message(String code, String message) {
    Objects.requireNonNull(code, "code");
    Objects.requireNonNull(message, "message");
    if (!code.matches("[0-9]{4}")) {
      throw new IllegalArgumentException("A SIFEN result code has four digits: " + code);
    }
    if (message.isEmpty() || message.length() > 255) {
      throw new IllegalArgumentException("A SIFEN result message has 1 to 255 characters");
    }
    return new ResultMessage(code, message);
  }

  @Override
  public String toString() {
    return "Outcome{" + kind + ", " + messages + (id == null ? "" : ", id=" + id) + "}";
  }
}
