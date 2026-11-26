/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client;

import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * The answer of the document query service ({@code siConsDE}).
 *
 * @param found whether SIFEN has a document with that CDC
 * @param documentXml the document container SIFEN returns ({@code xContenDE}), as received; null if
 *     not found
 * @param messages codes and messages
 * @param exchange the raw exchange
 */
public record DocumentQuery(
    boolean found, @Nullable String documentXml, List<ResultMessage> messages, Exchange exchange) {

  /**
   * Creates the answer.
   *
   * @param found whether it was found
   * @param documentXml the container, or null
   * @param messages codes and messages
   * @param exchange the raw exchange
   */
  public DocumentQuery {
    messages = List.copyOf(messages);
  }
}
