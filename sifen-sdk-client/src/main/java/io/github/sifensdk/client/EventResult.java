/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client;

import java.util.List;

/**
 * The answer of the event reception service ({@code siRecepEvento}).
 *
 * @param events the outcome of each event, in the order SIFEN gave them
 * @param exchange the raw exchange
 */
public record EventResult(List<Event> events, Exchange exchange) {

  /**
   * Creates the answer.
   *
   * @param events the events
   * @param exchange the raw exchange
   */
  public EventResult {
    events = List.copyOf(events);
  }

  /**
   * The outcome of one event.
   *
   * @param id the event id
   * @param status its state
   * @param authorizationProtocol the protocol, when registered
   * @param messages codes and messages
   */
  public record Event(
      String id, Status status, String authorizationProtocol, List<ResultMessage> messages) {

    /**
     * Creates the outcome.
     *
     * @param id the id
     * @param status the state
     * @param authorizationProtocol the protocol, empty if none
     * @param messages codes and messages
     */
    public Event {
      messages = List.copyOf(messages);
    }
  }
}
