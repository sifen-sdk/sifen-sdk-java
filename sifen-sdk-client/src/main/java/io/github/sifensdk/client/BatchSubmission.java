/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client;

import java.time.Duration;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * The answer of the batch reception service ({@code siRecepLoteDE}).
 *
 * <p>When accepted, SIFEN gives a protocol number to query the result later with {@link
 * SifenClient#queryBatch(String)}. Each document's own result is only known through that query.
 *
 * @param accepted whether SIFEN accepted the batch for processing (code 0300)
 * @param protocolNumber the batch protocol number ({@code dProtConsLote}), when accepted
 * @param processingTime the processing time SIFEN informs ({@code dTpoProces}), if any
 * @param messages codes and messages
 * @param exchange the raw exchange
 */
public record BatchSubmission(
    boolean accepted,
    @Nullable String protocolNumber,
    @Nullable Duration processingTime,
    List<ResultMessage> messages,
    Exchange exchange) {

  /**
   * Creates the answer.
   *
   * @param accepted whether it was accepted
   * @param protocolNumber the protocol number, or null
   * @param processingTime the processing time, or null
   * @param messages codes and messages
   * @param exchange the raw exchange
   */
  public BatchSubmission {
    messages = List.copyOf(messages);
  }
}
