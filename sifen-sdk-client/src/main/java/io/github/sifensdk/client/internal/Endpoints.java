/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.internal;

/**
 * Paths of the SIFEN web services (MT150 7.10). The same paths exist in test and production; the
 * environment only changes the host ({@link io.github.sifensdk.core.Environment}).
 *
 * <p>Requests are posted to the {@code .wsdl} address itself, without {@code ?wsdl} (which is only
 * used to download the WSDL). The Manual writes the synchronous reception address in test as {@code
 * recibe.wsd}; that is a typo (contradiction C-723 of the specification).
 */
public final class Endpoints {

  /** Synchronous reception ({@code siRecepDE}). */
  public static final String RECEPTION = "/de/ws/sync/recibe.wsdl";

  /** Batch reception ({@code siRecepLoteDE}). */
  public static final String BATCH = "/de/ws/async/recibe-lote.wsdl";

  /** Batch result query ({@code siResultLoteDE}). */
  public static final String BATCH_RESULT = "/de/ws/consultas/consulta-lote.wsdl";

  /** Document query ({@code siConsDE}). */
  public static final String DOCUMENT_QUERY = "/de/ws/consultas/consulta.wsdl";

  /** Taxpayer query ({@code siConsRUC}). */
  public static final String RUC_QUERY = "/de/ws/consultas/consulta-ruc.wsdl";

  /** Event reception ({@code siRecepEvento}). */
  public static final String EVENTS = "/de/ws/eventos/evento.wsdl";

  private Endpoints() {}
}
