/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.spec;

import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * A field of the specification ({@code campos/}).
 *
 * @param id Manual id, for example {@code E727}
 * @param xml XML element name, for example {@code dTotBruOpeItem}
 * @param name readable English name shared by every SDK
 * @param parent parent field id, or null for a root
 * @param type grupo, alfanumerico, numerico, fecha, fecha_hora, binario or xml
 * @param length length as written in the Manual, or null
 * @param minOccurs minimum occurrences
 * @param maxOccurs maximum occurrences, {@code Integer.MAX_VALUE} for N
 * @param description official description
 * @param catalog catalog id, or null
 * @param state vigente, futuro or eliminado
 * @param sources official sources
 * @param validity validity
 * @param contradictions related contradiction ids
 * @param group the group code of the file it comes from (for example {@code E8.1})
 */
public record Field(
    String id,
    String xml,
    String name,
    @Nullable String parent,
    String type,
    @Nullable String length,
    int minOccurs,
    int maxOccurs,
    String description,
    @Nullable String catalog,
    String state,
    List<Source> sources,
    Validity validity,
    List<String> contradictions,
    String group) {}
