/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client.internal;

import io.github.sifensdk.core.cdc.Cdc;
import io.github.sifensdk.core.signing.SignedDocument;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The file of a batch: the XML {@code rLoteDE} with the signed {@code rDE}, compressed as ZIP
 * (MT150 9.2.1, Schema XML 5A; Guía de mejores prácticas, "recibe-lote", steps 1 to 4), and the
 * limits a batch must respect before it is sent.
 *
 * <p>How the file is built, and why:
 *
 * <ul>
 *   <li><b>Structure.</b> {@code <rLoteDE>} with the {@code rDE} as children (LSch01, LSch02). No
 *       XSD defines {@code rLoteDE} (C-706). The root is written without namespace, as in the Guía
 *       ("Crear la estructura del lote {@code <rLoteDE>}"), and each {@code rDE} keeps the
 *       declaration of its own namespace (MT150 7.2.2.2: "cada DE debe contener la declaración de
 *       su namespace individual").
 *   <li><b>Declaration and encoding.</b> One XML declaration for the whole file, UTF-8 (MT150
 *       7.2.1: "para el caso de los envíos de lotes, la estructura completa del archivo debe
 *       contener solo una declaración"). The declaration of each signed document is removed with
 *       {@link Soap#withoutDeclaration(byte[])}.
 *   <li><b>Signed bytes.</b> Each {@code rDE} is copied byte for byte as it was signed, never
 *       parsed or serialized again (ADR-0005). The file is a plain concatenation of bytes.
 *   <li><b>Name of the entry.</b> Neither the Manual nor the Guía names the file inside the ZIP.
 *       {@value #ENTRY_NAME} is the name used by the TIPS-SA {@code
 *       facturacionelectronicapy-setapi} library, whose batches SIFEN accepts in production. To be
 *       confirmed against SIFEN's test environment.
 *   <li><b>Determinism.</b> One entry, DEFLATE, with a fixed modification time, so that the same
 *       documents always give the same bytes and no local time is leaked.
 * </ul>
 */
public final class BatchArchive {

  /** Name of the XML file inside the ZIP (not defined by SIFEN; see the class description). */
  public static final String ENTRY_NAME = "xml_file.xml";

  /**
   * Maximum number of documents in a batch: "hasta 50 DE" (MT150 9.2.1, 9.2.2, LSch02 1-50; Guía de
   * mejores prácticas, "Lote no encolado", c).
   */
  public static final int MAX_DOCUMENTS = 50;

  /**
   * Maximum size of the request message, in bytes. The Manual allows 10.000 KB (MT150 12.3.2.1,
   * BD01, code 0270) and the Guía de mejores prácticas 1000 KB ("Lote no encolado", e, code 0301):
   * contradiction C-707. The stricter limit is applied, because it satisfies both sources, and KB
   * is read as 1000 bytes, the stricter reading too.
   */
  public static final int MAX_MESSAGE_BYTES = 1000 * 1000;

  private static final LocalDateTime ENTRY_TIME = LocalDateTime.of(2000, 1, 1, 0, 0);

  private static final byte[] DECLARATION =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes(StandardCharsets.UTF_8);
  private static final byte[] OPEN = "<rLoteDE>".getBytes(StandardCharsets.UTF_8);
  private static final byte[] CLOSE = "</rLoteDE>".getBytes(StandardCharsets.UTF_8);

  private BatchArchive() {}

  /**
   * Checks the documented limits of a batch, before building it. Messages identify documents by
   * their position (starting at 1) and never show their CDC or RUC.
   *
   * <ul>
   *   <li>1 to {@value #MAX_DOCUMENTS} documents (LSch02 1-50; an empty batch blocks the RUC, Guía,
   *       "Motivos de bloqueo", f);
   *   <li>all of the same document type, C002, taken from the first two digits of the CDC (MT150
   *       9.2.2: "Un lote debe contener solo un mismo tipo de DE"; Guía, "Lote no encolado", b;
   *       C-711);
   *   <li>all of the same issuer, the RUC and check digit of the CDC (Guía, "Lote no encolado", a;
   *       C-711);
   *   <li>no CDC repeated (Guía, "Motivos de bloqueo", g: sending the same CDC several times in a
   *       batch blocks the RUC for 10 to 60 minutes).
   * </ul>
   *
   * <p>The size of the message is checked once it is built ({@link #checkMessageSize(int)}).
   *
   * @param documents the documents
   * @throws IllegalArgumentException if a limit is broken
   */
  public static void validate(List<SignedDocument> documents) {
    Objects.requireNonNull(documents, "documents");
    if (documents.isEmpty()) {
      throw new IllegalArgumentException(
          "A batch needs at least one document (LSch02 1-50; SIFEN blocks the RUC for empty"
              + " batches)");
    }
    if (documents.size() > MAX_DOCUMENTS) {
      throw new IllegalArgumentException(
          "A batch admits up to "
              + MAX_DOCUMENTS
              + " documents (MT150 9.2.1, LSch02), got "
              + documents.size());
    }
    String type = null;
    String issuer = null;
    Set<String> seen = new HashSet<>();
    for (int i = 0; i < documents.size(); i++) {
      SignedDocument document = Objects.requireNonNull(documents.get(i), "documents[" + i + "]");
      Cdc cdc = Cdc.parse(document.cdc());
      String documentIssuer = cdc.issuerRuc() + cdc.issuerRucCheckDigit();
      if (type == null) {
        type = cdc.documentType();
        issuer = documentIssuer;
      } else {
        if (!type.equals(cdc.documentType())) {
          throw new IllegalArgumentException(
              "All documents of a batch must be of the same type (C002, MT150 9.2.2): document "
                  + (i + 1)
                  + " is of type "
                  + cdc.documentType()
                  + " and document 1 of type "
                  + type);
        }
        if (!issuer.equals(documentIssuer)) {
          throw new IllegalArgumentException(
              "All documents of a batch must have the same issuer RUC (Guía de mejores prácticas,"
                  + " lote no encolado a): document "
                  + (i + 1)
                  + " has a different issuer than document 1");
        }
      }
      if (!seen.add(cdc.value())) {
        throw new IllegalArgumentException(
            "Document "
                + (i + 1)
                + " repeats the CDC of an earlier document of the batch; SIFEN blocks the issuer's"
                + " RUC for that (Guía de mejores prácticas, motivos de bloqueo g)");
      }
    }
  }

  /**
   * Checks the size of the complete request message.
   *
   * @param bytes the size of the SOAP envelope
   * @throws IllegalArgumentException if it exceeds {@link #MAX_MESSAGE_BYTES}
   */
  public static void checkMessageSize(int bytes) {
    if (bytes > MAX_MESSAGE_BYTES) {
      throw new IllegalArgumentException(
          "The batch message has "
              + bytes
              + " bytes; SIFEN admits up to 1000 KB (Guía de mejores prácticas, lote no encolado e;"
              + " C-707): send fewer documents per batch");
    }
  }

  /**
   * The XML of the batch: one declaration, {@code <rLoteDE>}, each signed {@code rDE} exactly as
   * signed (without its own declaration), {@code </rLoteDE>}.
   *
   * @param documents the documents
   * @return the UTF-8 bytes of the file
   */
  public static byte[] xml(List<SignedDocument> documents) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.writeBytes(DECLARATION);
    out.writeBytes(OPEN);
    for (SignedDocument document : documents) {
      out.writeBytes(Soap.withoutDeclaration(document.xml()));
    }
    out.writeBytes(CLOSE);
    return out.toByteArray();
  }

  /**
   * The ZIP of the batch, with {@link #xml(List)} as its only entry, {@value #ENTRY_NAME}.
   *
   * @param documents the documents
   * @return the bytes of the ZIP
   */
  public static byte[] zip(List<SignedDocument> documents) {
    byte[] xml = xml(documents);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (ZipOutputStream zip = new ZipOutputStream(out, StandardCharsets.UTF_8)) {
      ZipEntry entry = new ZipEntry(ENTRY_NAME);
      entry.setTimeLocal(ENTRY_TIME);
      entry.setMethod(ZipEntry.DEFLATED);
      zip.putNextEntry(entry);
      zip.write(xml);
      zip.closeEntry();
    } catch (IOException e) {
      throw new UncheckedIOException("Could not compress the batch in memory", e);
    }
    return out.toByteArray();
  }
}
