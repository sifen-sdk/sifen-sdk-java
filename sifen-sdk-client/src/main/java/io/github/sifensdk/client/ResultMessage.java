/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client;

/**
 * A code and message returned by SIFEN ({@code dCodRes} and {@code dMsgRes}).
 *
 * @param code the four-digit code
 * @param message the message, as SIFEN wrote it
 */
public record ResultMessage(String code, String message) {}
