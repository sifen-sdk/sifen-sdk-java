/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client;

import java.util.List;

/**
 * The answer of the batch result service ({@code siResultLoteDE}).
 *
 * @param state the state of the batch
 * @param documents the outcome of each document, when the batch was processed
 * @param messages codes and messages about the batch itself
 * @param exchange the raw exchange
 */
public record BatchResult(
    State state, List<DocumentResult> documents, List<ResultMessage> messages, Exchange exchange) {

  /**
   * Creates the answer.
   *
   * @param state the state
   * @param documents the documents
   * @param messages codes and messages
   * @param exchange the raw exchange
   */
  public BatchResult {
    documents = List.copyOf(documents);
    messages = List.copyOf(messages);
  }

  /** The state of a batch. */
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
