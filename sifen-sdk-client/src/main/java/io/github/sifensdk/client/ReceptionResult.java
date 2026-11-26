/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client;

/**
 * The answer of the synchronous reception service ({@code siRecepDE}).
 *
 * @param document the outcome of the document
 * @param exchange the raw exchange
 */
public record ReceptionResult(DocumentResult document, Exchange exchange) {}
