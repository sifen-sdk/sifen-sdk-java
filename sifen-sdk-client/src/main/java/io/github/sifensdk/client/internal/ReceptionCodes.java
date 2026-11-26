/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.internal;

import io.github.sifensdk.client.ResultMessage;
import java.util.List;
import java.util.Set;

/**
 * Response codes that the synchronous reception and the queries classify without reading the state
 * text, taken from {@code spec/respuestas}.
 *
 * <p>Only codes with an explicit class in the specification are listed. A code outside these sets
 * is not guessed: the caller reports it as a protocol problem (ADR-0006).
 */
final class ReceptionCodes {

  /**
   * Rejections that any web service may return before processing its data area, all of class {@code
   * rechazo} in {@code spec/respuestas/certificado-y-firma.json}: schema 0100-0107 (MT150 12.2.3,
   * kept although the change log removes them, C-801), certificate 0120-0126 (12.2.4), signature
   * 0140-0143 (12.2.5, NT 16), generic input 0160-0163 (12.2.6) and call control 0180/0183
   * (12.2.7). The codes 0001-0007, removed in 2023, are not included.
   */
  private static final Set<String> GENERIC_REJECTIONS =
      Set.of(
          "0100", "0101", "0102", "0104", "0105", "0106", "0107", "0120", "0121", "0122", "0123",
          "0124", "0125", "0126", "0140", "0141", "0142", "0143", "0160", "0161", "0162", "0163",
          "0180", "0183");

  /** Authorization of the DE (MT150 12.3.1.3, BC01): class {@code aprobacion}. */
  static final String RECEPTION_APPROVED = "0260";

  /** Reception message over 1000 KB (MT150 12.3.1.1, BA01): class {@code rechazo}. */
  static final String RECEPTION_TOO_LARGE = "0200";

  /** Document query message over 1000 KB (MT150 12.3.4.1, BJ01): class {@code rechazo}. */
  static final String DOCUMENT_QUERY_TOO_LARGE = "0380";

  /** Taxpayer query message over 1000 KB (MT150 12.3.5.1, BM01, C-720): class {@code rechazo}. */
  static final String RUC_QUERY_TOO_LARGE = "0460";

  private ReceptionCodes() {}

  /**
   * Whether a code is a rejection of the service call itself: a generic rejection or the size limit
   * of that service.
   *
   * @param code the code ({@code dCodRes})
   * @param serviceTooLarge the size-limit code of the service
   * @return true if the call was rejected
   */
  static boolean isServiceRejection(String code, String serviceTooLarge) {
    return GENERIC_REJECTIONS.contains(code) || serviceTooLarge.equals(code);
  }

  /**
   * Whether any of the messages is a rejection of the service call itself.
   *
   * @param messages the messages
   * @param serviceTooLarge the size-limit code of the service
   * @return true if one of them is
   */
  static boolean anyServiceRejection(List<ResultMessage> messages, String serviceTooLarge) {
    for (ResultMessage m : messages) {
      if (isServiceRejection(m.code(), serviceTooLarge)) {
        return true;
      }
    }
    return false;
  }
}
