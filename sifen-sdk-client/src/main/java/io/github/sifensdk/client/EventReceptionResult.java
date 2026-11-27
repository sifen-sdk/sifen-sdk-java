/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client;

import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * The answer of the event reception service ({@code siRecepEvento}): the outcome of each event
 * sent.
 *
 * <p>A class rather than a record so that fields can be added in a minor version. {@link
 * #toString()} shows only the number of events.
 */
public final class EventReceptionResult {

  private final List<EventResult> events;
  private final Exchange exchange;

  private EventReceptionResult(List<EventResult> events, Exchange exchange) {
    this.events = List.copyOf(events);
    this.exchange = Objects.requireNonNull(exchange, "exchange");
  }

  /**
   * Creates the answer. For the SDK's internal use and tests; not part of the API: it may change in
   * any version.
   *
   * @param events the events
   * @param exchange the raw exchange
   * @return the answer
   */
  public static EventReceptionResult of(List<EventResult> events, Exchange exchange) {
    return new EventReceptionResult(events, exchange);
  }

  /**
   * Returns the outcome of each event, in the order SIFEN gave them.
   *
   * @return the events, unmodifiable
   */
  public List<EventResult> events() {
    return events;
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
    if (!(o instanceof EventReceptionResult)) {
      return false;
    }
    EventReceptionResult that = (EventReceptionResult) o;
    return events.equals(that.events) && exchange.equals(that.exchange);
  }

  @Override
  public int hashCode() {
    return Objects.hash(events, exchange);
  }

  /** Returns the number of events, without contents. */
  @Override
  public String toString() {
    return "EventReceptionResult[events=" + events.size() + "]";
  }
}
