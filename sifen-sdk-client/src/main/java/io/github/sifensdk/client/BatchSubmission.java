/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client;

import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * The answer of the batch reception service ({@code siRecepLoteDE}).
 *
 * <p>When accepted, SIFEN gives a protocol number to query the result later with {@link
 * SifenClient#queryBatch(String)}. Each document's own result is only known through that query.
 *
 * <p>A class rather than a record so that fields can be added in a minor version. {@link
 * #toString()} shows only whether it was accepted and the result codes.
 */
public final class BatchSubmission {

  private final boolean accepted;
  private final @Nullable String protocolNumber;
  private final @Nullable String processingTime;
  private final List<ResultMessage> messages;
  private final Exchange exchange;

  private BatchSubmission(
      boolean accepted,
      @Nullable String protocolNumber,
      @Nullable String processingTime,
      List<ResultMessage> messages,
      Exchange exchange) {
    this.accepted = accepted;
    this.protocolNumber = protocolNumber;
    this.processingTime = processingTime;
    this.messages = List.copyOf(messages);
    this.exchange = Objects.requireNonNull(exchange, "exchange");
  }

  /**
   * Creates the answer. For the SDK's internal use and tests; not part of the API: it may change in
   * any version.
   *
   * @param accepted whether it was accepted
   * @param protocolNumber the protocol number, or null
   * @param processingTime the processing time as SIFEN sent it, or null
   * @param messages codes and messages
   * @param exchange the raw exchange
   * @return the answer
   */
  public static BatchSubmission of(
      boolean accepted,
      @Nullable String protocolNumber,
      @Nullable String processingTime,
      List<ResultMessage> messages,
      Exchange exchange) {
    return new BatchSubmission(accepted, protocolNumber, processingTime, messages, exchange);
  }

  /**
   * Returns whether SIFEN accepted the batch for processing (code 0300).
   *
   * @return true if accepted
   */
  public boolean accepted() {
    return accepted;
  }

  /**
   * Returns the batch protocol number ({@code dProtConsLote}).
   *
   * @return the protocol number, or null when not accepted
   */
  public @Nullable String protocolNumber() {
    return protocolNumber;
  }

  /**
   * Returns the processing time SIFEN informs ({@code dTpoProces}, BRSch06), as SIFEN sent it.
   *
   * <p>Its unit is not verified: BRSch06 and MT150 8.2.1 say seconds, 8.2.2 says milliseconds
   * (contradiction C-710). It is informative only; do not use it as a waiting time (see {@link
   * BatchSchedule}).
   *
   * @return the text of {@code dTpoProces}, trimmed, or null if absent or empty
   */
  public @Nullable String processingTime() {
    return processingTime;
  }

  /**
   * Returns SIFEN's codes and messages.
   *
   * @return the codes and messages, unmodifiable
   */
  public List<ResultMessage> messages() {
    return messages;
  }

  /**
   * Returns the raw exchange.
   *
   * @return the exchange
   */
  public Exchange exchange() {
    return exchange;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BatchSubmission)) {
      return false;
    }
    BatchSubmission that = (BatchSubmission) o;
    return accepted == that.accepted
        && Objects.equals(protocolNumber, that.protocolNumber)
        && Objects.equals(processingTime, that.processingTime)
        && messages.equals(that.messages)
        && exchange.equals(that.exchange);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accepted, protocolNumber, processingTime, messages, exchange);
  }

  /** Returns whether it was accepted and the result codes, without contents. */
  @Override
  public String toString() {
    return "BatchSubmission[accepted="
        + accepted
        + ", codes="
        + ResultMessage.codes(messages)
        + "]";
  }
}
