/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client;

import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * The outcome of one event in SIFEN, inside an {@link EventReceptionResult}.
 *
 * <p>A class rather than a record so that fields can be added in a minor version. {@link
 * #toString()} shows only the state and the result codes.
 */
public final class EventResult {

  private final String id;
  private final Status status;
  private final @Nullable String authorizationProtocol;
  private final List<ResultMessage> messages;

  private EventResult(
      String id,
      Status status,
      @Nullable String authorizationProtocol,
      List<ResultMessage> messages) {
    this.id = Objects.requireNonNull(id, "id");
    this.status = Objects.requireNonNull(status, "status");
    this.authorizationProtocol = authorizationProtocol;
    this.messages = List.copyOf(messages);
  }

  /**
   * Creates the outcome. For the SDK's internal use and tests; not part of the API: it may change
   * in any version.
   *
   * @param id the id
   * @param status the state
   * @param authorizationProtocol the protocol, or null if none
   * @param messages codes and messages
   * @return the outcome
   */
  public static EventResult of(
      String id,
      Status status,
      @Nullable String authorizationProtocol,
      List<ResultMessage> messages) {
    return new EventResult(id, status, authorizationProtocol, messages);
  }

  /**
   * Returns the event id ({@code id}).
   *
   * @return the id
   */
  public String id() {
    return id;
  }

  /**
   * Returns the state of the event.
   *
   * @return the state
   */
  public Status status() {
    return status;
  }

  /**
   * Returns the event's authorization protocol ({@code dProtAut}).
   *
   * @return the protocol, or null when SIFEN gave none (for example, when rejected)
   */
  public @Nullable String authorizationProtocol() {
    return authorizationProtocol;
  }

  /**
   * Returns SIFEN's codes and messages for the event.
   *
   * @return the codes and messages, unmodifiable
   */
  public List<ResultMessage> messages() {
    return messages;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EventResult)) {
      return false;
    }
    EventResult that = (EventResult) o;
    return id.equals(that.id)
        && status == that.status
        && Objects.equals(authorizationProtocol, that.authorizationProtocol)
        && messages.equals(that.messages);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, status, authorizationProtocol, messages);
  }

  /** Returns the state and the result codes, without message texts. */
  @Override
  public String toString() {
    return "EventResult[status=" + status + ", codes=" + ResultMessage.codes(messages) + "]";
  }
}
