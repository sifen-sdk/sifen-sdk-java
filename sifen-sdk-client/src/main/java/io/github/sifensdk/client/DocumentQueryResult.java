/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client;

import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * The answer of the document query service ({@code siConsDE}).
 *
 * <p>A class rather than a record so that fields can be added in a minor version. {@link
 * #toString()} shows only whether the document was found, the result codes and the number of
 * events, never the document.
 */
public final class DocumentQueryResult {

  private final boolean found;
  private final @Nullable String documentXml;
  private final @Nullable String authorizationProtocol;
  private final List<QueriedEvent> events;
  private final List<ResultMessage> messages;
  private final Exchange exchange;

  private DocumentQueryResult(
      boolean found,
      @Nullable String documentXml,
      @Nullable String authorizationProtocol,
      List<QueriedEvent> events,
      List<ResultMessage> messages,
      Exchange exchange) {
    this.found = found;
    this.documentXml = documentXml;
    this.authorizationProtocol = authorizationProtocol;
    this.events = List.copyOf(events);
    this.messages = List.copyOf(messages);
    this.exchange = Objects.requireNonNull(exchange, "exchange");
  }

  /**
   * Creates the answer. For the SDK's internal use and tests; not part of the API: it may change in
   * any version.
   *
   * @param found whether it was found
   * @param documentXml the container, or null
   * @param authorizationProtocol the transaction number, or null
   * @param events the registered events
   * @param messages codes and messages
   * @param exchange the raw exchange
   * @return the answer
   */
  public static DocumentQueryResult of(
      boolean found,
      @Nullable String documentXml,
      @Nullable String authorizationProtocol,
      List<QueriedEvent> events,
      List<ResultMessage> messages,
      Exchange exchange) {
    return new DocumentQueryResult(
        found, documentXml, authorizationProtocol, events, messages, exchange);
  }

  /**
   * Returns whether SIFEN has a document with that CDC.
   *
   * @return true if found
   */
  public boolean found() {
    return found;
  }

  /**
   * Returns the document container SIFEN returns ({@code xContenDE}), as received.
   *
   * @return the container, or null if not found
   */
  public @Nullable String documentXml() {
    return documentXml;
  }

  /**
   * Returns the document's transaction number ({@code dProtAut}).
   *
   * @return the transaction number, or null if not found or absent
   */
  public @Nullable String authorizationProtocol() {
    return authorizationProtocol;
  }

  /**
   * Returns the events registered on the document, in the order SIFEN lists them.
   *
   * @return the events, unmodifiable
   */
  public List<QueriedEvent> events() {
    return events;
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
    if (!(o instanceof DocumentQueryResult)) {
      return false;
    }
    DocumentQueryResult that = (DocumentQueryResult) o;
    return found == that.found
        && Objects.equals(documentXml, that.documentXml)
        && Objects.equals(authorizationProtocol, that.authorizationProtocol)
        && events.equals(that.events)
        && messages.equals(that.messages)
        && exchange.equals(that.exchange);
  }

  @Override
  public int hashCode() {
    return Objects.hash(found, documentXml, authorizationProtocol, events, messages, exchange);
  }

  /** Returns whether it was found, the result codes and the number of events, without contents. */
  @Override
  public String toString() {
    return "DocumentQueryResult[found="
        + found
        + ", codes="
        + ResultMessage.codes(messages)
        + ", events="
        + events.size()
        + "]";
  }
}
