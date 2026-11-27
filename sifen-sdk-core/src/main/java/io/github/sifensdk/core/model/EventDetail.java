/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

/**
 * The detail of an event: exactly one of the eight events an issuer or a receiver can send ({@code
 * gGroupTiEvt}, GDE007, a choice in {@code Evento_v150.xsd}).
 *
 * <p>Issuer events: {@link Cancellation}, {@link NumberInvalidation}, {@link Nomination} and {@link
 * TransportUpdate}. Receiver events: {@link ReceiptNotification}, {@link Conformity}, {@link
 * Disconformity} and {@link NonRecognition}. The events SIFEN adds by itself are only read, never
 * built.
 *
 * <p>Permitted types may be added in minor versions: {@code switch} statements need a default
 * branch.
 */
public sealed interface EventDetail
    permits Cancellation,
        NumberInvalidation,
        ReceiptNotification,
        Conformity,
        Disconformity,
        NonRecognition,
        Nomination,
        TransportUpdate {}
