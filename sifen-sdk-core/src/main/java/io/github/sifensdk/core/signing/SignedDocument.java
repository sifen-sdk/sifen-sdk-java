/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.signing;

import io.github.sifensdk.core.cdc.Cdc;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A signed document, ready to be stored and sent: the exact bytes of the signed {@code rDE}, its
 * CDC and the address of its QR.
 *
 * <p>ADR-0005: the bytes are final. They must be stored and sent exactly as they are; serializing
 * them again (for example through a DOM or an XML binding) can break the signature.
 *
 * <p>Immutable and thread-safe: {@link #xml()} returns a copy. {@link #toString()} shows no content
 * of the document.
 */
public final class SignedDocument {

  private final byte[] xml;
  private final String cdc;
  private final String qrUrl;
  private final LocalDateTime signatureDateTime;

  private SignedDocument(byte[] xml, String cdc, String qrUrl, LocalDateTime signatureDateTime) {
    this.xml = xml;
    this.cdc = cdc;
    this.qrUrl = qrUrl;
    this.signatureDateTime = signatureDateTime;
  }

  /**
   * Creates a signed document from its parts. The SDK creates it when signing; this factory also
   * lets a document signed elsewhere be handled the same way.
   *
   * @param xml the UTF-8 bytes of the signed {@code rDE}; copied
   * @param cdc the CDC of the document (attribute {@code Id} of {@code DE}, A002)
   * @param qrUrl the QR address (the text of {@code dCarQR}, J002, without XML escaping)
   * @param signatureDateTime the signature date and time ({@code dFecFirma}, A004), local time in
   *     the zone of {@code SifenDocuments.Builder.zone} (Paraguay by default)
   * @return the signed document
   * @throws IllegalArgumentException if the XML is empty or the CDC is not valid
   */
  public static SignedDocument of(
      byte[] xml, String cdc, String qrUrl, LocalDateTime signatureDateTime) {
    Objects.requireNonNull(xml, "xml");
    Objects.requireNonNull(qrUrl, "qrUrl");
    Objects.requireNonNull(signatureDateTime, "signatureDateTime");
    if (xml.length == 0) {
      throw new IllegalArgumentException("The signed XML is empty");
    }
    String value = Cdc.parse(Objects.requireNonNull(cdc, "cdc")).value();
    return new SignedDocument(xml.clone(), value, qrUrl, signatureDateTime);
  }

  /**
   * Returns a copy of the exact bytes of the signed {@code rDE} (UTF-8).
   *
   * @return a new array on every call
   */
  public byte[] xml() {
    return xml.clone();
  }

  /**
   * Returns the CDC of the document (A002).
   *
   * @return the 44 characters of the CDC
   */
  public String cdc() {
    return cdc;
  }

  /**
   * Returns the QR address ({@code dCarQR}, J002), as it must be encoded in the QR image.
   *
   * @return the address, with plain {@code &} separators
   */
  public String qrUrl() {
    return qrUrl;
  }

  /**
   * Returns the signature date and time ({@code dFecFirma}, A004).
   *
   * @return local date and time, without zone, in the zone configured with {@code
   *     SifenDocuments.Builder.zone} (Paraguay by default)
   */
  public LocalDateTime signatureDateTime() {
    return signatureDateTime;
  }

  /** Returns a description without any content of the document. */
  @Override
  public String toString() {
    return "SignedDocument[" + xml.length + " bytes]";
  }
}
