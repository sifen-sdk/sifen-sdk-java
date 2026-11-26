/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.testing;

import java.time.Clock;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.jspecify.annotations.Nullable;

/**
 * What one simulated SIFEN remembers between requests: the batches it accepted (protocol number and
 * the CDCs inside) and the documents it approved (to answer document queries). Thread-safe; one
 * instance per {@link SimulatedSifen}, never shared.
 */
final class State {

  /** SIFEN informs dates in Paraguay's time zone, with its offset (C-703). */
  static final ZoneId PARAGUAY = ZoneId.of("America/Asuncion");

  private final Clock clock;
  private final Map<String, List<String>> batches = new ConcurrentHashMap<>();
  private final Map<String, String> received = new ConcurrentHashMap<>();
  private final Map<String, Approved> approved = new ConcurrentHashMap<>();

  State(Clock clock) {
    this.clock = Objects.requireNonNull(clock, "clock");
  }

  Clock clock() {
    return clock;
  }

  /** A new batch protocol number: 17 digits, like the examples of the best practices guide. */
  String newProtocolNumber() {
    return digits(17);
  }

  /** A new authorization (transaction) number: 10 digits (MT150 PP051, CRSch052, GRSch031). */
  String newAuthorization() {
    return digits(10);
  }

  void rememberBatch(String protocolNumber, List<String> cdcs) {
    batches.put(protocolNumber, List.copyOf(cdcs));
  }

  /**
   * The CDCs of an accepted batch.
   *
   * @return the CDCs, or null if no batch with that number was accepted
   */
  @Nullable List<String> batch(String protocolNumber) {
    return batches.get(protocolNumber);
  }

  /** The protocol number of the batch that contained a CDC, or null. */
  @Nullable String batchContaining(String cdc) {
    for (Map.Entry<String, List<String>> e : batches.entrySet()) {
      if (e.getValue().contains(cdc)) {
        return e.getKey();
      }
    }
    return null;
  }

  void rememberReceived(String cdc, String documentXml) {
    received.put(cdc, documentXml);
  }

  /** Records the approval of a received document; unknown CDCs are ignored. */
  void approve(String cdc, String authorization) {
    String xml = received.get(cdc);
    if (xml != null) {
      approved.put(cdc, new Approved(xml, authorization));
    }
  }

  /** An approved document, or null if this simulated SIFEN did not approve that CDC. */
  @Nullable Approved approvedDocument(String cdc) {
    return approved.get(cdc);
  }

  /** A document this simulated SIFEN received and approved. */
  static final class Approved {
    final String xml;
    final String authorization;

    Approved(String xml, String authorization) {
      this.xml = xml;
      this.authorization = authorization;
    }
  }

  private static String digits(int count) {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    StringBuilder sb = new StringBuilder(count);
    sb.append(1 + random.nextInt(9));
    for (int i = 1; i < count; i++) {
      sb.append(random.nextInt(10));
    }
    return sb.toString();
  }
}
