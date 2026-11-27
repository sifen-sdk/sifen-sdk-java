/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.signing;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A signed event, ready to be stored and sent: the exact bytes of the signed {@code rGesEve}, its
 * identifier and its signature date and time.
 *
 * <p>ADR-0005: the bytes are final; they are sent exactly as they are. Immutable and thread-safe:
 * {@link #xml()} returns a copy. {@link #toString()} shows no content of the event.
 */
public final class SignedEvent {

  private final byte[] xml;
  private final String id;
  private final LocalDateTime signatureDateTime;

  private SignedEvent(byte[] xml, String id, LocalDateTime signatureDateTime) {
    this.xml = xml;
    this.id = id;
    this.signatureDateTime = signatureDateTime;
  }

  /**
   * Creates a signed event from its parts. The SDK creates it when signing; this factory also lets
   * an event signed elsewhere be sent the same way.
   *
   * @param xml the UTF-8 bytes of the signed {@code rGesEve}; copied
   * @param id the event identifier (attribute {@code Id} of {@code rEve}, GDE003)
   * @param signatureDateTime the signature date and time ({@code dFecFirma}, GDE004)
   * @return the signed event
   * @throws IllegalArgumentException if the XML is empty or the identifier is not 1 to 10 digits
   */
  public static SignedEvent of(byte[] xml, String id, LocalDateTime signatureDateTime) {
    Objects.requireNonNull(xml, "xml");
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(signatureDateTime, "signatureDateTime");
    if (xml.length == 0) {
      throw new IllegalArgumentException("The signed XML is empty");
    }
    if (!id.matches("[0-9]{1,10}")) {
      throw new IllegalArgumentException(
          "The event identifier (GDE003) has 1 to 10 digits (Evento_v150.xsd tdIdEve)");
    }
    return new SignedEvent(xml.clone(), id, signatureDateTime);
  }

  /**
   * Returns a copy of the exact bytes of the signed {@code rGesEve} (UTF-8).
   *
   * @return a new array on every call
   */
  public byte[] xml() {
    return xml.clone();
  }

  /**
   * Returns the event identifier (GDE003), by which SIFEN's answer refers to it.
   *
   * @return the identifier
   */
  public String id() {
    return id;
  }

  /**
   * Returns the signature date and time ({@code dFecFirma}, GDE004).
   *
   * @return local date and time, without zone, in the zone configured with {@code
   *     SifenEvents.Builder.zone} (Paraguay by default)
   */
  public LocalDateTime signatureDateTime() {
    return signatureDateTime;
  }

  /** Returns a description without any content of the event. */
  @Override
  public String toString() {
    return "SignedEvent[" + xml.length + " bytes]";
  }
}
