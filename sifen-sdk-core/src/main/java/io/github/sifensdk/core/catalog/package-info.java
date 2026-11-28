/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * SIFEN catalogs as enums (ADR 0014): tipo de emisión, tipo de documento, afectación del IVA,
 * departamento, unidad de medida and the other small catalogs. Generated from {@code
 * spec/catalogos} by {@code scripts/generar-catalogos.py}.
 *
 * <p>Large or geographic catalogs (city, district, country, currency) stay as {@code String} codes.
 */
@NullMarked
package io.github.sifensdk.core.catalog;

import org.jspecify.annotations.NullMarked;
