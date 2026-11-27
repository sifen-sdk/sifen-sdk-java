/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.client;

/**
 * The type of an event found in a document query, by the element of its detail ({@code
 * gGroupTiEvt}, GDE007, in {@code Evento_v150.xsd}). The first eight are the events a taxpayer
 * sends; the others are registered by SIFEN or by other parties and are only read.
 */
public enum EventType {
  /** Cancelación ({@code rGeVeCan}). */
  CANCELLATION("rGeVeCan"),
  /** Inutilización de números ({@code rGeVeInu}). */
  NUMBER_INVALIDATION("rGeVeInu"),
  /** Notificación de recepción ({@code rGeVeNotRec}). */
  RECEIPT_NOTIFICATION("rGeVeNotRec"),
  /** Conformidad ({@code rGeVeConf}). */
  CONFORMITY("rGeVeConf"),
  /** Disconformidad ({@code rGeVeDisconf}). */
  DISCONFORMITY("rGeVeDisconf"),
  /** Desconocimiento ({@code rGeVeDescon}). */
  NON_RECOGNITION("rGeVeDescon"),
  /** Nominación ({@code rGEveNom}). */
  NOMINATION("rGEveNom"),
  /** Actualización de datos del transporte ({@code rGeVeTr}). */
  TRANSPORT_UPDATE("rGeVeTr"),
  /** Endoso ({@code rGeVeEnd}), not yet in force. */
  ENDORSEMENT("rGeVeEnd"),
  /** Retención ({@code rGeVeRetAce}). */
  WITHHOLDING("rGeVeRetAce"),
  /** Anulación de retención ({@code rGeVeRetAnu}). */
  WITHHOLDING_CANCELLATION("rGeVeRetAnu"),
  /** Cesión de créditos fiscales ({@code rGeVeCCFF}). */
  TAX_CREDIT_TRANSFER("rGeVeCCFF"),
  /** Devolución de créditos fiscales, cuestionado ({@code rGeDevCCFFCue}). */
  TAX_CREDIT_REFUND_QUESTIONED("rGeDevCCFFCue"),
  /** Devolución de créditos fiscales, devuelto ({@code rGeDevCCFFDev}). */
  TAX_CREDIT_REFUND_RETURNED("rGeDevCCFFDev"),
  /** Anticipo ({@code rGeVeAnt}). */
  ADVANCE("rGeVeAnt"),
  /** Remisión ({@code rGeVeRem}). */
  DELIVERY_NOTE("rGeVeRem"),
  /** Objeto de análisis, de la SET ({@code rGeVeOA}). */
  ANALYSIS_OBJECT("rGeVeOA"),
  /** Proceso de control, de la SET ({@code rGeVePC}). */
  CONTROL_PROCESS("rGeVePC"),
  /** Impugnación, de la SET ({@code rGeVeImp}). */
  CHALLENGE("rGeVeImp"),
  /** Determinación, de la SET ({@code rGeVeDet}). */
  DETERMINATION("rGeVeDet"),
  /** An event element this version does not know. */
  OTHER("");

  private final String element;

  EventType(String element) {
    this.element = element;
  }

  /**
   * The XML element of the event's detail, for example {@code rGeVeCan}; empty for {@link #OTHER}.
   *
   * @return the element name
   */
  public String element() {
    return element;
  }

  /**
   * The type of a detail element.
   *
   * @param element the local name of the element under {@code gGroupTiEvt}
   * @return its type, or {@link #OTHER} when unknown
   */
  public static EventType ofElement(String element) {
    for (EventType t : values()) {
      if (!t.element.isEmpty() && t.element.equals(element)) {
        return t;
      }
    }
    return OTHER;
  }
}
