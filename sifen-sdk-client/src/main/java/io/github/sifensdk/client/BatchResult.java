/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client;

import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * The answer of the batch result service ({@code siResultLoteDE}).
 *
 * <p>A class rather than a record so that fields can be added in a minor version. {@link
 * #toString()} shows only the state, the result codes and the number of documents.
 */
public final class BatchResult {

  private final State state;
  private final List<DocumentResult> documents;
  private final List<ResultMessage> messages;
  private final Exchange exchange;

  private BatchResult(
      State state,
      List<DocumentResult> documents,
      List<ResultMessage> messages,
      Exchange exchange) {
    this.state = Objects.requireNonNull(state, "state");
    this.documents = List.copyOf(documents);
    this.messages = List.copyOf(messages);
    this.exchange = Objects.requireNonNull(exchange, "exchange");
  }

  /**
   * Creates the answer. For the SDK's internal use and tests; not part of the API: it may change in
   * any version.
   *
   * @param state the state
   * @param documents the documents
   * @param messages codes and messages
   * @param exchange the raw exchange
   * @return the answer
   */
  public static BatchResult of(
      State state,
      List<DocumentResult> documents,
      List<ResultMessage> messages,
      Exchange exchange) {
    return new BatchResult(state, documents, messages, exchange);
  }

  /**
   * Returns the state of the batch.
   *
   * @return the state
   */
  public State state() {
    return state;
  }

  /**
   * Returns the outcome of each document, when the batch was processed.
   *
   * @return the documents, unmodifiable; empty unless {@link State#PROCESSED}
   */
  public List<DocumentResult> documents() {
    return documents;
  }

  /**
   * Returns the codes and messages about the batch itself.
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
    if (!(o instanceof BatchResult)) {
      return false;
    }
    BatchResult that = (BatchResult) o;
    return state == that.state
        && documents.equals(that.documents)
        && messages.equals(that.messages)
        && exchange.equals(that.exchange);
  }

  @Override
  public int hashCode() {
    return Objects.hash(state, documents, messages, exchange);
  }

  /** Returns the state, the result codes and the number of documents, without contents. */
  @Override
  public String toString() {
    return "BatchResult[state="
        + state
        + ", codes="
        + ResultMessage.codes(messages)
        + ", documents="
        + documents.size()
        + "]";
  }

  /**
   * The state of a batch.
   *
   * <p>Constants may be added in minor versions: {@code switch} statements need a default branch.
   */
  public enum State {
    /** Processed: see the result of each document (code 0362). */
    PROCESSED,
    /** Still being processed: query again later, no sooner than SIFEN recommends (code 0361). */
    IN_PROCESS,
    /** SIFEN does not know the protocol number (code 0360). */
    NOT_FOUND,
    /** The query window expired (48 hours after sending, code 0364): query each document by CDC. */
    EXPIRED,
    /**
     * SIFEN rejected the query or the batch as a whole (for example code 0340, RUC without
     * permission, or 0363, documents of different types): see the messages. No document of the
     * batch was processed.
     */
    REJECTED
  }
}
