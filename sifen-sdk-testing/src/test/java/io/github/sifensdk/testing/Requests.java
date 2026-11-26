/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.testing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Synthetic SOAP requests like the ones the SDK client sends, with the data of the Manual. */
final class Requests {

  /** CDC of the example of the Technical Manual v150 (synthetic). */
  static final String CDC = "01444444017001001001452822017012515873260988";

  /** A second synthetic CDC (only used where nothing validates its check digit). */
  static final String OTHER_CDC = "01444444017001001001452922017012515873260981";

  static final String DIGEST = "dGhpcyBpcyBhIHN5bnRoZXRpYyBkaWdlc3QgdmFsdWU=";

  private static final String NS = "http://ekuatia.set.gov.py/sifen/xsd";

  private Requests() {}

  static String rde(String cdc) {
    return "<rDE xmlns=\""
        + NS
        + "\"><dVerFor>150</dVerFor><DE Id=\""
        + cdc
        + "\"><dDVId>8</dDVId></DE>"
        + "<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\"><SignedInfo><Reference URI=\"#"
        + cdc
        + "\"><DigestValue>"
        + DIGEST
        + "</DigestValue></Reference></SignedInfo></Signature></rDE>";
  }

  static byte[] envelope(String payload) {
    return ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">"
            + "<env:Header/><env:Body>"
            + payload
            + "</env:Body></env:Envelope>")
        .getBytes(StandardCharsets.UTF_8);
  }

  static byte[] reception(String dId, String cdc) {
    return envelope(
        "<rEnviDe xmlns=\""
            + NS
            + "\"><dId>"
            + dId
            + "</dId><xDE>"
            + rde(cdc)
            + "</xDE></rEnviDe>");
  }

  static byte[] batch(String dId, String... cdcs) {
    StringBuilder lote = new StringBuilder("<rLoteDE>");
    for (String cdc : cdcs) {
      lote.append(rde(cdc));
    }
    lote.append("</rLoteDE>");
    ByteArrayOutputStream zipped = new ByteArrayOutputStream();
    try (ZipOutputStream zip = new ZipOutputStream(zipped)) {
      zip.putNextEntry(new ZipEntry("lote.xml"));
      zip.write(lote.toString().getBytes(StandardCharsets.UTF_8));
      zip.closeEntry();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return envelope(
        "<rEnvioLote xmlns=\""
            + NS
            + "\"><dId>"
            + dId
            + "</dId><xDE>"
            + Base64.getEncoder().encodeToString(zipped.toByteArray())
            + "</xDE></rEnvioLote>");
  }

  static byte[] batchResult(String dId, String protocolNumber) {
    return envelope(
        "<rEnviConsLoteDe xmlns=\""
            + NS
            + "\"><dId>"
            + dId
            + "</dId><dProtConsLote>"
            + protocolNumber
            + "</dProtConsLote></rEnviConsLoteDe>");
  }

  static byte[] documentQuery(String dId, String cdc) {
    return envelope(
        "<rEnviConsDeRequest xmlns=\""
            + NS
            + "\"><dId>"
            + dId
            + "</dId><dCDC>"
            + cdc
            + "</dCDC></rEnviConsDeRequest>");
  }

  static byte[] rucQuery(String dId, String ruc) {
    return envelope(
        "<rEnviConsRUC xmlns=\""
            + NS
            + "\"><dId>"
            + dId
            + "</dId><dRUCCons>"
            + ruc
            + "</dRUCCons></rEnviConsRUC>");
  }

  static byte[] events(String dId, String... eventIds) {
    StringBuilder sb = new StringBuilder();
    sb.append("<rEnviEventoDe xmlns=\"")
        .append(NS)
        .append("\"><dId>")
        .append(dId)
        .append("</dId><dEvReg><gGroupGesEve>");
    for (String id : eventIds) {
      sb.append("<rGesEve><rEve Id=\"")
          .append(id)
          .append("\"><dFecFirma>2026-11-26T10:00:00</dFecFirma><dVerFor>150</dVerFor>")
          .append("</rEve></rGesEve>");
    }
    sb.append("</gGroupGesEve></dEvReg></rEnviEventoDe>");
    return envelope(sb.toString());
  }
}
