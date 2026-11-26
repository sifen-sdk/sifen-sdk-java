/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client;

import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * The answer of the taxpayer query service ({@code siConsRUC}).
 *
 * @param found whether the RUC exists
 * @param ruc the RUC queried
 * @param name the taxpayer's name ({@code dRazCons}), if found
 * @param statusCode the taxpayer's status code ({@code dCodEstCons}), if found
 * @param statusDescription the taxpayer's status description ({@code dDesEstCons}), if found
 * @param electronicIssuer whether the taxpayer is an electronic issuer ({@code dRUCFactElec} = S),
 *     null if not informed
 * @param messages codes and messages
 * @param exchange the raw exchange
 */
public record RucQuery(
    boolean found,
    String ruc,
    @Nullable String name,
    @Nullable String statusCode,
    @Nullable String statusDescription,
    @Nullable Boolean electronicIssuer,
    List<ResultMessage> messages,
    Exchange exchange) {

  /**
   * Creates the answer.
   *
   * @param found whether it was found
   * @param ruc the RUC
   * @param name the name, or null
   * @param statusCode the status code, or null
   * @param statusDescription the status description, or null
   * @param electronicIssuer whether it is an electronic issuer, or null
   * @param messages codes and messages
   * @param exchange the raw exchange
   */
  public RucQuery {
    messages = List.copyOf(messages);
  }
}
