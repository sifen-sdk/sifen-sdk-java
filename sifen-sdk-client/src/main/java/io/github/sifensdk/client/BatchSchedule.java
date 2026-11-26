/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * When to query the result of a batch, so as not to query too early or too often.
 *
 * <p>The SDK never waits or polls by itself: this class only computes instants, and the application
 * decides how to schedule its queries (ADR-0004, ADR-0007). It has no state and no clock: every
 * method takes the instants it needs. Immutable and thread-safe.
 *
 * <p>{@link #recommended()} uses the figures of SIFEN's Guía de mejores prácticas (October 2024):
 *
 * <ul>
 *   <li><b>First query: 10 minutes after sending.</b> "se recomienda comenzar a realizar la
 *       consulta pasados los 10 minutos de la recepción" (Recomendaciones, 4); "Se sugiere comenzar
 *       a consultar un lote enviado pasado 10 minutos desde el envío" ("recibe-lote", Response, 1).
 *   <li><b>Interval between queries: at least 10 minutes.</b> "y luego a intervalos regulares no
 *       menores a 10 minutos" (Recomendaciones, 4); "se sugiere consultar a intervalos mínimos de
 *       10 minutos" ("consulta-lote", Response, 2). Under heavy load processing can take from 1 to
 *       24 hours (same place).
 *   <li><b>Query window: 48 hours after sending.</b> "La consulta del lote contempla un plazo de
 *       hasta 48 horas posteriores al envío del mismo. Una vez superado el tiempo, deberá consultar
 *       cada CDC del lote mediante la WS Consulta DE" ("consulta-lote", Response, 3; code 0364,
 *       contradiction C-807: the Manual does not mention it).
 * </ul>
 *
 * <p>The processing time SIFEN informs when it accepts a batch ({@link
 * BatchSubmission#processingTime()}) is not used: its unit is ambiguous (C-710) and it is an
 * average per document, not a waiting time.
 *
 * <p>Use the instant the batch was sent, for example {@code submission.exchange().sentAt()}.
 */
public final class BatchSchedule {

  private static final BatchSchedule RECOMMENDED =
      new BatchSchedule(Duration.ofMinutes(10), Duration.ofMinutes(10), Duration.ofHours(48));

  private final Duration firstQueryDelay;
  private final Duration minimumInterval;
  private final Duration queryWindow;

  private BatchSchedule(Duration firstQueryDelay, Duration minimumInterval, Duration queryWindow) {
    this.firstQueryDelay = firstQueryDelay;
    this.minimumInterval = minimumInterval;
    this.queryWindow = queryWindow;
  }

  /**
   * The schedule of SIFEN's Guía de mejores prácticas: first query 10 minutes after sending, then
   * every 10 minutes or more, for up to 48 hours.
   *
   * @return the recommended schedule
   */
  public static BatchSchedule recommended() {
    return RECOMMENDED;
  }

  /**
   * A custom schedule, for example a longer interval at peak hours. Shorter figures than the
   * recommended ones are allowed, but SIFEN advises against them.
   *
   * @param firstQueryDelay time from sending to the first query, at least zero
   * @param minimumInterval minimum time between two queries, positive
   * @param queryWindow time after sending during which SIFEN answers batch queries, positive
   * @return the schedule
   * @throws IllegalArgumentException if a duration is out of range
   */
  public static BatchSchedule of(
      Duration firstQueryDelay, Duration minimumInterval, Duration queryWindow) {
    Objects.requireNonNull(firstQueryDelay, "firstQueryDelay");
    Objects.requireNonNull(minimumInterval, "minimumInterval");
    Objects.requireNonNull(queryWindow, "queryWindow");
    if (firstQueryDelay.isNegative()) {
      throw new IllegalArgumentException("firstQueryDelay must not be negative");
    }
    if (minimumInterval.isNegative() || minimumInterval.isZero()) {
      throw new IllegalArgumentException("minimumInterval must be positive");
    }
    if (queryWindow.isNegative() || queryWindow.isZero()) {
      throw new IllegalArgumentException("queryWindow must be positive");
    }
    return new BatchSchedule(firstQueryDelay, minimumInterval, queryWindow);
  }

  /**
   * Time from sending to the first query.
   *
   * @return the delay
   */
  public Duration firstQueryDelay() {
    return firstQueryDelay;
  }

  /**
   * Minimum time between two queries of the same batch.
   *
   * @return the interval
   */
  public Duration minimumInterval() {
    return minimumInterval;
  }

  /**
   * Time after sending during which the batch can be queried.
   *
   * @return the window
   */
  public Duration queryWindow() {
    return queryWindow;
  }

  /**
   * The earliest instant for the first query of a batch.
   *
   * @param sentAt when the batch was sent
   * @return {@code sentAt} plus {@link #firstQueryDelay()}
   */
  public Instant firstQueryAt(Instant sentAt) {
    return Objects.requireNonNull(sentAt, "sentAt").plus(firstQueryDelay);
  }

  /**
   * The earliest instant for the next query, after one that answered that the batch is still being
   * processed (0361).
   *
   * @param lastQueryAt when the last query was made
   * @return {@code lastQueryAt} plus {@link #minimumInterval()}
   */
  public Instant nextQueryAt(Instant lastQueryAt) {
    return Objects.requireNonNull(lastQueryAt, "lastQueryAt").plus(minimumInterval);
  }

  /**
   * The last instant at which SIFEN still answers queries about the batch.
   *
   * @param sentAt when the batch was sent
   * @return {@code sentAt} plus {@link #queryWindow()}
   */
  public Instant windowEndsAt(Instant sentAt) {
    return Objects.requireNonNull(sentAt, "sentAt").plus(queryWindow);
  }

  /**
   * Whether the query window has passed: from then on, query each document by CDC ({@link
   * SifenClient#queryDocument(String)}) instead of the batch.
   *
   * @param sentAt when the batch was sent
   * @param now the current instant
   * @return true if {@code now} is after {@link #windowEndsAt(Instant)}
   */
  public boolean windowExpired(Instant sentAt, Instant now) {
    return Objects.requireNonNull(now, "now").isAfter(windowEndsAt(sentAt));
  }

  @Override
  public boolean equals(@Nullable Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof BatchSchedule)) {
      return false;
    }
    BatchSchedule that = (BatchSchedule) other;
    return firstQueryDelay.equals(that.firstQueryDelay)
        && minimumInterval.equals(that.minimumInterval)
        && queryWindow.equals(that.queryWindow);
  }

  @Override
  public int hashCode() {
    return Objects.hash(firstQueryDelay, minimumInterval, queryWindow);
  }

  @Override
  public String toString() {
    return "BatchSchedule{firstQueryDelay="
        + firstQueryDelay
        + ", minimumInterval="
        + minimumInterval
        + ", queryWindow="
        + queryWindow
        + "}";
  }
}
