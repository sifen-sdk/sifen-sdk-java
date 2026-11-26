/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client;

import java.time.OffsetDateTime;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * The outcome of one electronic document in SIFEN: received synchronously, or inside a batch.
 *
 * @param cdc the CDC of the document
 * @param status its state
 * @param authorizationProtocol the authorization protocol ({@code dProtAut}), when approved
 * @param processedAt when SIFEN processed it, if informed
 * @param messages codes and messages, in the order SIFEN gave them
 */
public record DocumentResult(
    String cdc,
    Status status,
    @Nullable String authorizationProtocol,
    @Nullable OffsetDateTime processedAt,
    List<ResultMessage> messages) {

  /**
   * Creates the result.
   *
   * @param cdc the CDC
   * @param status the state
   * @param authorizationProtocol the authorization protocol, or null
   * @param processedAt when it was processed, or null
   * @param messages codes and messages
   */
  public DocumentResult {
    messages = List.copyOf(messages);
  }
}
