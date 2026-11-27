/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.model;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * An event to send to SIFEN: its identifier and its detail.
 *
 * <p>SIFEN group {@code GDE002} ({@code rEve}). The SDK completes the signature date and time
 * ({@code dFecFirma}, GDE004) and the format version ({@code dVerFor}, GDE005); the event type
 * ({@code dTiGDE}, GDE006) is not in the current XSD and is not written. Immutable and thread-safe;
 * create it with {@link #builder()}.
 */
@SifenField(id = "GDE002", xml = "rEve")
public final class Event {

  private final String id;
  private final EventDetail detail;

  private Event(Builder b) {
    this.id = Checks.required(b.id, "id", "GDE003", "Id");
    this.detail = Checks.required(b.detail, "detail", "GDE007", "gGroupTiEvt");
  }

  /**
   * Starts a new builder.
   *
   * @return an empty builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Starts a builder with a copy of this object's values.
   *
   * @return a builder with these values
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  /**
   * Identificador del evento, asignado por quien lo genera (1 a 10 dígitos).
   *
   * <p>SIFEN field {@code GDE003} (attribute {@code Id} of {@code rEve}). SIFEN's answer refers to
   * the event by this identifier.
   *
   * @return the value
   */
  @SifenField(id = "GDE003", xml = "Id")
  public String id() {
    return id;
  }

  /**
   * El evento: cancelación, inutilización, nominación, actualización de transporte o uno de los
   * eventos del receptor.
   *
   * <p>SIFEN field {@code GDE007} ({@code gGroupTiEvt}).
   *
   * @return the value
   */
  @SifenField(id = "GDE007", xml = "gGroupTiEvt")
  public EventDetail detail() {
    return detail;
  }

  /**
   * Compares all values.
   *
   * @param other the object to compare with
   * @return whether both hold the same values
   */
  @Override
  public boolean equals(@Nullable Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Event)) {
      return false;
    }
    Event that = (Event) other;
    return id.equals(that.id) && detail.equals(that.detail);
  }

  /**
   * A hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, detail);
  }

  /**
   * The type of the detail. Never shows document values.
   *
   * @return a description without document contents
   */
  @Override
  public String toString() {
    return "Event[" + detail + "]";
  }

  /** Builder for {@link Event}. Not thread-safe; {@link #build()} returns an immutable copy. */
  public static final class Builder {

    private @Nullable String id;
    private @Nullable EventDetail detail;

    private Builder() {}

    private Builder(Event source) {
      this.id = source.id;
      this.detail = source.detail;
    }

    /**
     * Sets {@code id} (GDE003, attribute {@code Id} of {@code rEve}).
     *
     * @param id the value
     * @return this builder
     */
    public Builder id(String id) {
      this.id = id;
      return this;
    }

    /**
     * Sets {@code detail} (GDE007, {@code gGroupTiEvt}).
     *
     * @param detail the event
     * @return this builder
     */
    public Builder detail(EventDetail detail) {
      this.detail = detail;
      return this;
    }

    /**
     * Builds the immutable object.
     *
     * @return the object
     * @throws NullPointerException if the id or the detail is missing
     */
    public Event build() {
      return new Event(this);
    }
  }
}
