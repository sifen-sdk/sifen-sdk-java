/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.xml;

import io.github.sifensdk.core.internal.tree.Node;

/**
 * A minimal synthetic invoice (FE, cash, one item with 10% VAT), built by hand as a tree. Data are
 * made up; the RUC is the Manual's example 44444401-7.
 */
final class MinimalDocument {

  static final String CDC = "01444444017001001000000122025010110000000019";

  private MinimalDocument() {}

  private static Node leaf(String name, String text) {
    return Node.leaf(name, null, text);
  }

  private static Node group(String name) {
    return Node.group(name, null);
  }

  static Node rde() {
    return group("rDE").add(leaf("dVerFor", "150")).add(de());
  }

  static Node de() {
    Node de = group("DE").attribute("Id", CDC);
    de.add(leaf("dDVId", "9"))
        .add(leaf("dFecFirma", "2025-01-01T10:00:00"))
        .add(leaf("dSisFact", "1"));
    de.add(
        group("gOpeDE")
            .add(leaf("iTipEmi", "1"))
            .add(leaf("dDesTipEmi", "Normal"))
            .add(leaf("dCodSeg", "000000001")));
    de.add(
        group("gTimb")
            .add(leaf("iTiDE", "1"))
            .add(leaf("dDesTiDE", "Factura electrónica"))
            .add(leaf("dNumTim", "12345678"))
            .add(leaf("dEst", "001"))
            .add(leaf("dPunExp", "001"))
            .add(leaf("dNumDoc", "0000001"))
            .add(leaf("dFeIniT", "2024-01-01")));
    Node emis =
        group("gEmis")
            .add(leaf("dRucEm", "44444401"))
            .add(leaf("dDVEmi", "7"))
            .add(leaf("iTipCont", "2"))
            .add(leaf("dNomEmi", "DE generado en ambiente de prueba"))
            .add(leaf("dDirEmi", "Calle de prueba"))
            .add(leaf("dNumCas", "0"))
            .add(leaf("cDepEmi", "1"))
            .add(leaf("dDesDepEmi", "CAPITAL"))
            .add(leaf("cCiuEmi", "1"))
            .add(leaf("dDesCiuEmi", "ASUNCION (DISTRITO)"))
            .add(leaf("dTelEmi", "021000000"))
            .add(leaf("dEmailE", "prueba@example.com"))
            .add(
                group("gActEco")
                    .add(leaf("cActEco", "62010"))
                    .add(leaf("dDesActEco", "Actividad de prueba")));
    Node rec =
        group("gDatRec")
            .add(leaf("iNatRec", "2"))
            .add(leaf("iTiOpe", "2"))
            .add(leaf("cPaisRec", "PRY"))
            .add(leaf("dDesPaisRe", "Paraguay"))
            .add(leaf("iTipIDRec", "1"))
            .add(leaf("dDTipIDRec", "Cédula paraguaya"))
            .add(leaf("dNumIDRec", "1234567"))
            .add(leaf("dNomRec", "Receptor de prueba"));
    de.add(
        group("gDatGralOpe")
            .add(leaf("dFeEmiDE", "2025-01-01T10:00:00"))
            .add(
                group("gOpeCom")
                    .add(leaf("iTipTra", "1"))
                    .add(leaf("dDesTipTra", "Venta de mercadería"))
                    .add(leaf("iTImp", "1"))
                    .add(leaf("dDesTImp", "IVA"))
                    .add(leaf("cMoneOpe", "PYG"))
                    .add(leaf("dDesMoneOpe", "Guarani")))
            .add(emis)
            .add(rec));
    Node item =
        group("gCamItem")
            .add(leaf("dCodInt", "A-1"))
            .add(leaf("dDesProSer", "Producto de prueba"))
            .add(leaf("cUniMed", "77"))
            .add(leaf("dDesUniMed", "UNI"))
            .add(leaf("dCantProSer", "1"))
            .add(
                group("gValorItem")
                    .add(leaf("dPUniProSer", "11000"))
                    .add(leaf("dTotBruOpeItem", "11000"))
                    .add(
                        group("gValorRestaItem")
                            .add(leaf("dDescItem", "0"))
                            .add(leaf("dPorcDesIt", "0"))
                            .add(leaf("dDescGloItem", "0"))
                            .add(leaf("dTotOpeItem", "11000"))))
            .add(
                group("gCamIVA")
                    .add(leaf("iAfecIVA", "1"))
                    .add(leaf("dDesAfecIVA", "Gravado IVA"))
                    .add(leaf("dPropIVA", "100"))
                    .add(leaf("dTasaIVA", "10"))
                    .add(leaf("dBasGravIVA", "10000"))
                    .add(leaf("dLiqIVAItem", "1000"))
                    .add(leaf("dBasExe", "0")));
    de.add(
        group("gDtipDE")
            .add(
                group("gCamFE")
                    .add(leaf("iIndPres", "1"))
                    .add(leaf("dDesIndPres", "Operación presencial")))
            .add(
                group("gCamCond")
                    .add(leaf("iCondOpe", "1"))
                    .add(leaf("dDCondOpe", "Contado"))
                    .add(
                        group("gPaConEIni")
                            .add(leaf("iTiPago", "1"))
                            .add(leaf("dDesTiPag", "Efectivo"))
                            .add(leaf("dMonTiPag", "11000"))
                            .add(leaf("cMoneTiPag", "PYG"))
                            .add(leaf("dDMoneTiPag", "Guarani"))))
            .add(item));
    return de;
  }
}
