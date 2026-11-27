/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * Internal: keys that never leave where they live (a PKCS#11 token or HSM, or a remote signer) and
 * the provider that signs with them (ADR-0012). Not part of the public API; exported only to the
 * SDK client, which needs it for mutual TLS.
 */
@NullMarked
package io.github.sifensdk.core.internal.keys;

import org.jspecify.annotations.NullMarked;
