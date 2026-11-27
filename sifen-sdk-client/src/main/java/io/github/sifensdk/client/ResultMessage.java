/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client;

import java.util.ArrayList;
import java.util.List;

/**
 * A code and message returned by SIFEN ({@code dCodRes} and {@code dMsgRes}).
 *
 * <p>{@link #toString()} shows only the code: SIFEN's texts may quote the document's data.
 *
 * @param code the four-digit code
 * @param message the message, as SIFEN wrote it; empty (never null) when SIFEN sent none
 */
public record ResultMessage(String code, String message) {

  /** Returns the code, without the message text. */
  @Override
  public String toString() {
    return "ResultMessage[code=" + code + "]";
  }

  /** The codes of the messages, in order, for the {@code toString} of the results. */
  static List<String> codes(List<ResultMessage> messages) {
    List<String> codes = new ArrayList<>(messages.size());
    for (ResultMessage m : messages) {
      codes.add(m.code());
    }
    return codes;
  }
}
