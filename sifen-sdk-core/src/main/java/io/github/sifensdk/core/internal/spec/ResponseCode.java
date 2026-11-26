/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.spec;

import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * A SIFEN response code ({@code respuestas/}).
 *
 * @param code four-digit code
 * @param service service that returns it
 * @param message official message
 * @param kind aprobacion, aprobacion_con_observacion, rechazo, en_proceso, transitorio, bloqueo or
 *     informativo
 * @param action suggested action, or null
 * @param field related field id, or null
 * @param sources official sources
 * @param validity validity
 */
public record ResponseCode(
    String code,
    String service,
    String message,
    String kind,
    @Nullable String action,
    @Nullable String field,
    List<Source> sources,
    Validity validity) {}
