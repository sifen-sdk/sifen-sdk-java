/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.dsig;

import io.github.sifensdk.core.SigningException;
import io.github.sifensdk.core.signing.KeyMaterial;
import io.github.sifensdk.core.signing.SignedEvent;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import javax.xml.crypto.dsig.XMLSignature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Signs an event: an enveloped XML Signature over {@code rEve} (GDE002), referenced by its {@code
 * Id}, placed as the next sibling of {@code rEve} inside {@code rGesEve} ({@code Evento_v150.xsd}
 * trGesEve), with the same algorithms as the document (Manual Técnico v150, 7.6 and 7.7; NT 16).
 * The event is serialized once and the signature verified before returning.
 */
public final class EventAssembler {

  private EventAssembler() {}

  /**
   * Signs the event and serializes it.
   *
   * @param unsignedEvent the UTF-8 bytes of an {@code rGesEve} with {@code rEve} and without {@code
   *     Signature}
   * @param keyMaterial the signer's key and certificate (RSA)
   * @return the signed event
   * @throws SigningException if the input is not an unsigned {@code rGesEve} or signing fails;
   *     messages carry no event content
   */
  public static SignedEvent sign(byte[] unsignedEvent, KeyMaterial keyMaterial) {
    if (unsignedEvent == null || keyMaterial == null) {
      throw new NullPointerException("unsignedEvent and keyMaterial are required");
    }
    X509Certificate certificate = keyMaterial.certificate();
    if (!"RSA".equals(certificate.getPublicKey().getAlgorithm())) {
      throw new SigningException(
          "SIFEN signatures use RSA (Manual Técnico v150, 7.7; NT 16, XS06); the certificate key is "
              + certificate.getPublicKey().getAlgorithm());
    }
    Document document = DocumentAssembler.parse(unsignedEvent);
    Element root = document.getDocumentElement();
    if (!DocumentAssembler.isSifen(root, "rGesEve")) {
      throw new SigningException("The event root is not rGesEve in the SIFEN namespace");
    }
    if (DocumentAssembler.firstChild(root, XMLSignature.XMLNS, "Signature") != null) {
      throw new SigningException("The rGesEve already has a Signature");
    }
    Element rEve = DocumentAssembler.requireChild(root, "rEve");
    String id = rEve.getAttributeNS(null, "Id");
    if (!id.matches("[0-9]{1,10}")) {
      throw new SigningException("The Id attribute of rEve (GDE003) is not 1 to 10 digits");
    }
    LocalDateTime signatureDateTime;
    try {
      signatureDateTime =
          LocalDateTime.parse(DocumentAssembler.requireChild(rEve, "dFecFirma").getTextContent());
    } catch (DateTimeParseException e) {
      throw new SigningException(
          "dFecFirma (GDE004) is not a date and time yyyy-MM-ddTHH:mm:ss", e);
    }
    rEve.setIdAttributeNS(null, "Id", true);
    Element signature = DocumentAssembler.sign(root, rEve, id, keyMaterial);
    DocumentAssembler.removeWhitespace(signature, "SignatureValue");
    DocumentAssembler.removeWhitespace(signature, "X509Certificate");
    byte[] signed = DocumentAssembler.serialize(document);
    verify(signed, certificate);
    return SignedEvent.of(signed, id, signatureDateTime);
  }

  private static void verify(byte[] signed, X509Certificate certificate) {
    Document document = DocumentAssembler.parse(signed);
    Element root = document.getDocumentElement();
    Element rEve = DocumentAssembler.requireChild(root, "rEve");
    rEve.setIdAttributeNS(null, "Id", true);
    DocumentAssembler.verifySignatureIn(root, certificate.getPublicKey());
  }
}
