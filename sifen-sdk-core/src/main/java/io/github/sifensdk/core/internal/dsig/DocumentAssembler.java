/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.dsig;

import io.github.sifensdk.core.Environment;
import io.github.sifensdk.core.SigningException;
import io.github.sifensdk.core.cdc.Cdc;
import io.github.sifensdk.core.qr.QrCode;
import io.github.sifensdk.core.qr.QrParameters;
import io.github.sifensdk.core.signing.Csc;
import io.github.sifensdk.core.signing.KeyMaterial;
import io.github.sifensdk.core.signing.SignedDocument;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Signs an unsigned {@code rDE}, adds its QR and serializes it once.
 *
 * <p>Signature (Manual Técnico v150, 7.6 and 7.7, as modified by Nota Técnica 16, 1.1):
 *
 * <ul>
 *   <li>XML Signature, enveloped, over the element {@code DE} (A001), referenced as {@code
 *       URI="#<CDC>"}; {@code Signature} without prefix, as a child of {@code rDE} right after
 *       {@code DE} (I001; rule R-I001-F2).
 *   <li>{@code CanonicalizationMethod} (XS04): exclusive canonicalization {@code
 *       http://www.w3.org/2001/10/xml-exc-c14n#}. NT 16 lists it among the valid values (written
 *       without the final {@code #}; contradiction C-514 keeps the standard W3C identifier). The
 *       Manual's example uses the inclusive one; the exclusive one is chosen because it does not
 *       depend on the namespaces of the elements around {@code rDE} (the SOAP envelope it travels
 *       in).
 *   <li>{@code SignatureMethod} (XS06): {@code http://www.w3.org/2001/04/xmldsig-more#rsa-sha256}
 *       (Manual and NT 16; RSA, 7.7).
 *   <li>{@code Transforms} (XS12, XS13): a single transform, {@code
 *       http://www.w3.org/2000/09/xmldsig#enveloped-signature}, the only valid value since NT 16
 *       (occurrence 1-1; contradiction C-513: the Manual had two).
 *   <li>{@code DigestMethod} (XS16): {@code http://www.w3.org/2001/04/xmlenc#sha256} (Manual
 *       example and NT 16).
 *   <li>{@code KeyInfo/X509Data/X509Certificate} with the signer's certificate only, and none of
 *       {@code X509SubjectName}, {@code X509IssuerSerial}, {@code X509IssuerName}, {@code X509SKI}
 *       or {@code KeyValue} (7.6).
 * </ul>
 *
 * <p>Then the QR is computed with the {@code DigestValue} of the signature and {@code
 * gCamFuFD/dCarQR} (J001, J002) is added after {@code Signature}, in the SIFEN namespace. The
 * document is serialized a single time, to UTF-8 without added whitespace, and the signature of the
 * resulting bytes is verified (with secure validation) before returning them. The signature date
 * ({@code dFecFirma}, A004) is taken from the input as is.
 */
public final class DocumentAssembler {

  static final String SIFEN_NAMESPACE = "http://ekuatia.set.gov.py/sifen/xsd";
  static final String RSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";
  private static final String SECURE_VALIDATION = "org.jcp.xml.dsig.secureValidation";
  private static final String DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

  private DocumentAssembler() {}

  /**
   * Signs the document, adds its QR and serializes it.
   *
   * @param unsignedRde the UTF-8 bytes of an {@code rDE} with {@code dVerFor} and {@code DE}, and
   *     without {@code Signature} nor {@code gCamFuFD}
   * @param keyMaterial the signer's key and certificate (RSA)
   * @param csc the taxpayer's security code for the QR
   * @param environment the environment of the document
   * @return the signed document
   * @throws SigningException if the input is not an unsigned {@code rDE}, a published test CSC is
   *     used in production, or signing or verifying fails; messages carry no document content
   */
  public static SignedDocument signAndFinish(
      byte[] unsignedRde, KeyMaterial keyMaterial, Csc csc, Environment environment) {
    if (unsignedRde == null || keyMaterial == null || csc == null || environment == null) {
      throw new NullPointerException("unsignedRde, keyMaterial, csc and environment are required");
    }
    if (environment == Environment.PRODUCTION && csc.isPublishedTestCode()) {
      throw new SigningException(
          "The CSC is one of the test codes published in the Manual Técnico (13.8.4); it cannot be"
              + " used in production");
    }
    X509Certificate certificate = keyMaterial.certificate();
    if (!"RSA".equals(certificate.getPublicKey().getAlgorithm())) {
      throw new SigningException(
          "SIFEN signatures use RSA (Manual Técnico v150, 7.7; NT 16, XS06); the certificate key is "
              + certificate.getPublicKey().getAlgorithm());
    }

    Document document = parse(unsignedRde);
    Element rde = document.getDocumentElement();
    if (!isSifen(rde, "rDE")) {
      throw new SigningException("The document root is not rDE in the SIFEN namespace");
    }
    if (firstChild(rde, XMLSignature.XMLNS, "Signature") != null
        || firstChild(rde, SIFEN_NAMESPACE, "gCamFuFD") != null) {
      throw new SigningException("The rDE already has Signature or gCamFuFD");
    }
    Element de = requireChild(rde, "DE");
    String cdc = cdcOf(de);
    LocalDateTime signatureDateTime = signatureDateTimeOf(de);
    de.setIdAttributeNS(null, "Id", true);

    Element signature = sign(rde, de, cdc, keyMaterial);
    removeWhitespace(signature, "SignatureValue");
    removeWhitespace(signature, "X509Certificate");

    QrCode qr =
        QrCode.generate(qrParameters(rde, de, cdc, digestValue(signature)), csc, environment);
    Element unsigned = document.createElementNS(SIFEN_NAMESPACE, "gCamFuFD");
    Element qrElement = document.createElementNS(SIFEN_NAMESPACE, "dCarQR");
    qrElement.setTextContent(qr.url());
    unsigned.appendChild(qrElement);
    rde.insertBefore(unsigned, signature.getNextSibling());

    byte[] signed = serialize(document);
    verify(signed, certificate.getPublicKey());
    return SignedDocument.of(signed, cdc, qr.url(), signatureDateTime);
  }

  /**
   * Verifies the signature of a signed {@code rDE} with the given public key, with secure
   * validation.
   *
   * @param signedRde the bytes of the signed {@code rDE}
   * @param publicKey the key of the signer's certificate
   * @throws SigningException if the signature is missing or not valid
   */
  static void verify(byte[] signedRde, PublicKey publicKey) {
    Document document = parse(signedRde);
    Element rde = document.getDocumentElement();
    Element de = isSifen(rde, "rDE") ? firstChild(rde, SIFEN_NAMESPACE, "DE") : null;
    Element signature =
        isSifen(rde, "rDE") ? firstChild(rde, XMLSignature.XMLNS, "Signature") : null;
    if (de == null || signature == null) {
      throw new SigningException("The signed document has no DE or no Signature in rDE");
    }
    de.setIdAttributeNS(null, "Id", true);
    DOMValidateContext context =
        new DOMValidateContext(KeySelector.singletonKeySelector(publicKey), signature);
    context.setProperty(SECURE_VALIDATION, Boolean.TRUE);
    boolean valid;
    try {
      XMLSignature xmlSignature =
          XMLSignatureFactory.getInstance("DOM").unmarshalXMLSignature(context);
      valid = xmlSignature.validate(context);
    } catch (MarshalException | XMLSignatureException e) {
      throw new SigningException("Could not verify the signature of the signed document", e);
    }
    if (!valid) {
      throw new SigningException("The signature of the signed document is not valid");
    }
  }

  private static Element sign(Element rde, Element de, String cdc, KeyMaterial keyMaterial) {
    XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
    try {
      Reference reference =
          factory.newReference(
              "#" + cdc,
              factory.newDigestMethod(DigestMethod.SHA256, null),
              List.of(factory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)),
              null,
              null);
      SignedInfo signedInfo =
          factory.newSignedInfo(
              factory.newCanonicalizationMethod(
                  CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null),
              factory.newSignatureMethod(RSA_SHA256, null),
              List.of(reference));
      KeyInfoFactory keyInfoFactory = factory.getKeyInfoFactory();
      KeyInfo keyInfo =
          keyInfoFactory.newKeyInfo(
              List.of(keyInfoFactory.newX509Data(List.of(keyMaterial.certificate()))));
      Node next = de.getNextSibling();
      DOMSignContext context =
          next == null
              ? new DOMSignContext(keyMaterial.privateKey(), rde)
              : new DOMSignContext(keyMaterial.privateKey(), rde, next);
      context.setDefaultNamespacePrefix("");
      context.setProperty(SECURE_VALIDATION, Boolean.TRUE);
      factory.newXMLSignature(signedInfo, keyInfo).sign(context);
    } catch (GeneralSecurityException | MarshalException | XMLSignatureException e) {
      throw new SigningException("Could not sign the document", e);
    }
    Element signature = firstChild(rde, XMLSignature.XMLNS, "Signature");
    if (signature == null) {
      throw new SigningException("Could not sign the document: no Signature was produced");
    }
    return signature;
  }

  private static String cdcOf(Element de) {
    if (!de.hasAttributeNS(null, "Id")) {
      throw new SigningException("DE has no Id attribute (A002)");
    }
    try {
      return Cdc.parse(de.getAttributeNS(null, "Id")).value();
    } catch (IllegalArgumentException e) {
      throw new SigningException("The Id attribute of DE (A002) is not a valid CDC", e);
    }
  }

  private static LocalDateTime signatureDateTimeOf(Element de) {
    Element element = requireChild(de, "dFecFirma");
    try {
      return LocalDateTime.parse(element.getTextContent());
    } catch (DateTimeParseException e) {
      throw new SigningException("dFecFirma (A004) is not a date and time yyyy-MM-ddTHH:mm:ss", e);
    }
  }

  private static String digestValue(Element signature) {
    Element signedInfo = requireDsigChild(signature, "SignedInfo");
    Element reference = requireDsigChild(signedInfo, "Reference");
    return requireDsigChild(reference, "DigestValue").getTextContent();
  }

  /** Reads the values of the QR from the document (Manual Técnico v150, 13.8.2; C-512). */
  private static QrParameters qrParameters(Element rde, Element de, String cdc, String digest) {
    String version = requireChild(rde, "dVerFor").getTextContent();
    Element general = requireChild(de, "gDatGralOpe");
    String issueDateTime = requireChild(general, "dFeEmiDE").getTextContent();
    Element receiver = firstChild(general, SIFEN_NAMESPACE, "gDatRec");
    String receiverRuc =
        receiver == null ? null : text(firstChild(receiver, SIFEN_NAMESPACE, "dRucRec"));
    String receiverId =
        receiver == null || receiverRuc != null
            ? null
            : text(firstChild(receiver, SIFEN_NAMESPACE, "dNumIDRec"));
    Element totals = firstChild(de, SIFEN_NAMESPACE, "gTotSub");
    String total = totals == null ? null : text(firstChild(totals, SIFEN_NAMESPACE, "dTotGralOpe"));
    String vat = totals == null ? null : text(firstChild(totals, SIFEN_NAMESPACE, "dTotIVA"));
    int items = 0;
    Element typeFields = firstChild(de, SIFEN_NAMESPACE, "gDtipDE");
    if (typeFields != null) {
      for (Node n = typeFields.getFirstChild(); n != null; n = n.getNextSibling()) {
        if (n instanceof Element && isSifen((Element) n, "gCamItem")) {
          items++;
        }
      }
    }
    return new QrParameters(
        version, cdc, issueDateTime, receiverRuc, receiverId, total, vat, items, digest);
  }

  private static @Nullable String text(@Nullable Element element) {
    return element == null ? null : element.getTextContent();
  }

  private static void removeWhitespace(Element signature, String localName) {
    var nodes = signature.getElementsByTagNameNS(XMLSignature.XMLNS, localName);
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      node.setTextContent(node.getTextContent().replaceAll("\\s", ""));
    }
  }

  private static byte[] serialize(Document document) {
    try {
      TransformerFactory factory = TransformerFactory.newDefaultInstance();
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
      Transformer transformer = factory.newTransformer();
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty(OutputKeys.INDENT, "no");
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
      out.write(DECLARATION.getBytes(StandardCharsets.UTF_8));
      transformer.transform(new DOMSource(document), new StreamResult(out));
      return out.toByteArray();
    } catch (TransformerException | IOException e) {
      throw new SigningException("Could not serialize the signed document", e);
    }
  }

  /** Parses XML with DTDs, external entities and XInclude disabled. */
  static Document parse(byte[] xml) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
      factory.setNamespaceAware(true);
      factory.setValidating(false);
      factory.setXIncludeAware(false);
      factory.setExpandEntityReferences(false);
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
      DocumentBuilder builder = factory.newDocumentBuilder();
      builder.setErrorHandler(SilentErrorHandler.INSTANCE);
      return builder.parse(new ByteArrayInputStream(xml));
    } catch (ParserConfigurationException e) {
      throw new SigningException("Could not configure the XML parser", e);
    } catch (SAXException | IOException e) {
      throw new SigningException("The document is not well-formed XML or contains a DTD");
    }
  }

  private static boolean isSifen(Element element, String localName) {
    return SIFEN_NAMESPACE.equals(element.getNamespaceURI())
        && localName.equals(element.getLocalName());
  }

  private static @Nullable Element firstChild(Element parent, String namespace, String localName) {
    for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (n instanceof Element
          && namespace.equals(n.getNamespaceURI())
          && localName.equals(n.getLocalName())) {
        return (Element) n;
      }
    }
    return null;
  }

  private static Element requireChild(Element parent, String localName) {
    Element child = firstChild(parent, SIFEN_NAMESPACE, localName);
    if (child == null) {
      throw new SigningException(
          "The element " + parent.getLocalName() + " has no child " + localName);
    }
    return child;
  }

  private static Element requireDsigChild(Element parent, String localName) {
    Element child = firstChild(parent, XMLSignature.XMLNS, localName);
    if (child == null) {
      throw new SigningException("The signature has no " + localName);
    }
    return child;
  }

  /** Turns parser warnings and errors into exceptions instead of printing them. */
  private enum SilentErrorHandler implements ErrorHandler {
    INSTANCE;

    @Override
    public void warning(SAXParseException exception) {
      // ignored: warnings do not stop parsing and must not be printed
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
      throw exception;
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
      throw exception;
    }
  }
}
