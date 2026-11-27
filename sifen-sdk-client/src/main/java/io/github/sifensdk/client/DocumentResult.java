/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * The outcome of one electronic document in SIFEN: received synchronously, or inside a batch.
 *
 * <p>A class rather than a record so that fields can be added in a minor version. {@link
 * #toString()} shows only the state and the result codes, never the CDC (it carries the issuer's
 * RUC) nor SIFEN's message texts.
 */
public final class DocumentResult {

  private final String cdc;
  private final Status status;
  private final @Nullable String authorizationProtocol;
  private final @Nullable OffsetDateTime processedAt;
  private final List<ResultMessage> messages;

  private DocumentResult(
      String cdc,
      Status status,
      @Nullable String authorizationProtocol,
      @Nullable OffsetDateTime processedAt,
      List<ResultMessage> messages) {
    this.cdc = Objects.requireNonNull(cdc, "cdc");
    this.status = Objects.requireNonNull(status, "status");
    this.authorizationProtocol = authorizationProtocol;
    this.processedAt = processedAt;
    this.messages = List.copyOf(messages);
  }

  /**
   * Creates the result. For the SDK's internal use and tests; not part of the API: it may change in
   * any version.
   *
   * @param cdc the CDC
   * @param status the state
   * @param authorizationProtocol the authorization protocol, or null
   * @param processedAt when it was processed, or null
   * @param messages codes and messages
   * @return the result
   */
  public static DocumentResult of(
      String cdc,
      Status status,
      @Nullable String authorizationProtocol,
      @Nullable OffsetDateTime processedAt,
      List<ResultMessage> messages) {
    return new DocumentResult(cdc, status, authorizationProtocol, processedAt, messages);
  }

  /**
   * Returns the CDC of the document.
   *
   * @return the CDC
   */
  public String cdc() {
    return cdc;
  }

  /**
   * Returns the state of the document.
   *
   * @return the state
   */
  public Status status() {
    return status;
  }

  /**
   * Returns the authorization protocol ({@code dProtAut}).
   *
   * @return the protocol, or null when SIFEN gave none (for example, when rejected)
   */
  public @Nullable String authorizationProtocol() {
    return authorizationProtocol;
  }

  /**
   * Returns when SIFEN processed the document ({@code dFecProc}).
   *
   * @return the date and time, or null if not informed
   */
  public @Nullable OffsetDateTime processedAt() {
    return processedAt;
  }

  /**
   * Returns SIFEN's codes and messages, in the order SIFEN gave them.
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
    if (!(o instanceof DocumentResult)) {
      return false;
    }
    DocumentResult that = (DocumentResult) o;
    return cdc.equals(that.cdc)
        && status == that.status
        && Objects.equals(authorizationProtocol, that.authorizationProtocol)
        && Objects.equals(processedAt, that.processedAt)
        && messages.equals(that.messages);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cdc, status, authorizationProtocol, processedAt, messages);
  }

  /** Returns the state and the result codes, without the CDC or message texts. */
  @Override
  public String toString() {
    return "DocumentResult[status=" + status + ", codes=" + ResultMessage.codes(messages) + "]";
  }
}
