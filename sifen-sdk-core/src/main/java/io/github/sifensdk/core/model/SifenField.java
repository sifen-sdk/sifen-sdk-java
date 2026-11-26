/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Links a model type or accessor to its field in the SIFEN Manual Técnico and its XML element.
 *
 * <p>Placed on every group type (for example {@code @SifenField(id = "C001", xml = "gTimb")} on
 * {@link Stamp}) and on every accessor that holds a field. Available at run time, so tools and
 * error messages can show the official id and XML name next to the readable English name.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface SifenField {

  /**
   * The field id in the Manual Técnico, for example {@code E727}.
   *
   * @return the field id
   */
  String id();

  /**
   * The XML element name, for example {@code dTotBruOpeItem}.
   *
   * @return the XML name
   */
  String xml();
}
