/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * Testing support for SIFEN SDK users: a simulated SIFEN, local and without network.
 *
 * <p>{@link io.github.sifensdk.testing.SimulatedSifen} is an HTTPS server with mutual TLS that
 * answers on the same paths as SIFEN; {@link io.github.sifensdk.testing.SifenResponses} has its
 * responses, valid according to the official XSDs, including the failures seen in practice (the
 * load balancer's HTML page, SOAP faults, HTTP errors and slow answers); and {@link
 * io.github.sifensdk.testing.RecordedRequest} shows exactly what the integration sent. It does not
 * depend on any test framework.
 */
@NullMarked
package io.github.sifensdk.testing;

import org.jspecify.annotations.NullMarked;
