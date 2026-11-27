/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client;

import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * The answer of the taxpayer query service ({@code siConsRUC}).
 *
 * <p>A class rather than a record so that fields can be added in a minor version. {@link
 * #toString()} shows only whether the RUC was found and the result codes, never the RUC or the
 * taxpayer's name.
 */
public final class RucQueryResult {

  private final boolean found;
  private final String ruc;
  private final @Nullable String name;
  private final @Nullable String statusCode;
  private final @Nullable String statusDescription;
  private final @Nullable Boolean electronicIssuer;
  private final List<ResultMessage> messages;
  private final Exchange exchange;

  private RucQueryResult(
      boolean found,
      String ruc,
      @Nullable String name,
      @Nullable String statusCode,
      @Nullable String statusDescription,
      @Nullable Boolean electronicIssuer,
      List<ResultMessage> messages,
      Exchange exchange) {
    this.found = found;
    this.ruc = Objects.requireNonNull(ruc, "ruc");
    this.name = name;
    this.statusCode = statusCode;
    this.statusDescription = statusDescription;
    this.electronicIssuer = electronicIssuer;
    this.messages = List.copyOf(messages);
    this.exchange = Objects.requireNonNull(exchange, "exchange");
  }

  /**
   * Creates the answer. For the SDK's internal use and tests; not part of the API: it may change in
   * any version.
   *
   * @param found whether it was found
   * @param ruc the RUC
   * @param name the name, or null
   * @param statusCode the status code, or null
   * @param statusDescription the status description, or null
   * @param electronicIssuer whether it is an electronic issuer, or null
   * @param messages codes and messages
   * @param exchange the raw exchange
   * @return the answer
   */
  public static RucQueryResult of(
      boolean found,
      String ruc,
      @Nullable String name,
      @Nullable String statusCode,
      @Nullable String statusDescription,
      @Nullable Boolean electronicIssuer,
      List<ResultMessage> messages,
      Exchange exchange) {
    return new RucQueryResult(
        found, ruc, name, statusCode, statusDescription, electronicIssuer, messages, exchange);
  }

  /**
   * Returns whether the RUC exists.
   *
   * @return true if found
   */
  public boolean found() {
    return found;
  }

  /**
   * Returns the RUC queried.
   *
   * @return the RUC, without check digit
   */
  public String ruc() {
    return ruc;
  }

  /**
   * Returns the taxpayer's name ({@code dRazCons}).
   *
   * @return the name, or null if not found or not informed
   */
  public @Nullable String name() {
    return name;
  }

  /**
   * Returns the taxpayer's status code ({@code dCodEstCons}).
   *
   * @return the code, or null if not found or not informed
   */
  public @Nullable String statusCode() {
    return statusCode;
  }

  /**
   * Returns the taxpayer's status description ({@code dDesEstCons}).
   *
   * @return the description, or null if not found or not informed
   */
  public @Nullable String statusDescription() {
    return statusDescription;
  }

  /**
   * Returns whether the taxpayer is an electronic issuer ({@code dRUCFactElec} = S).
   *
   * @return true or false, or null if not informed
   */
  public @Nullable Boolean electronicIssuer() {
    return electronicIssuer;
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
    if (!(o instanceof RucQueryResult)) {
      return false;
    }
    RucQueryResult that = (RucQueryResult) o;
    return found == that.found
        && ruc.equals(that.ruc)
        && Objects.equals(name, that.name)
        && Objects.equals(statusCode, that.statusCode)
        && Objects.equals(statusDescription, that.statusDescription)
        && Objects.equals(electronicIssuer, that.electronicIssuer)
        && messages.equals(that.messages)
        && exchange.equals(that.exchange);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        found, ruc, name, statusCode, statusDescription, electronicIssuer, messages, exchange);
  }

  /** Returns whether it was found and the result codes, without the RUC or the name. */
  @Override
  public String toString() {
    return "RucQueryResult[found=" + found + ", codes=" + ResultMessage.codes(messages) + "]";
  }
}
