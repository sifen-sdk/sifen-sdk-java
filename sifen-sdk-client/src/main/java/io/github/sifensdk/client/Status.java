/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client;

/**
 * The state SIFEN gives to a document or event ({@code dEstRes}, and the codes of each service).
 *
 * <p>A rejection is a normal result, never an exception (ADR-0006).
 *
 * <p>Constants may be added in minor versions: {@code switch} statements need a default branch.
 */
public enum Status {
  /** Approved ("Aprobado"). */
  APPROVED,
  /** Approved with observations ("Aprobado con observación"). */
  APPROVED_WITH_OBSERVATION,
  /** Rejected ("Rechazado"): fix the data and send again. */
  REJECTED,
  /** Still being processed by SIFEN: query again later. */
  IN_PROCESS
}
