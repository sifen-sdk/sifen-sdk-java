/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.rules;

import io.github.sifensdk.core.internal.spec.Spec;
import io.github.sifensdk.core.internal.tree.Node;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import org.jspecify.annotations.Nullable;

/**
 * Builds synthetic document trees by hand (field ids from the specification), without the model or
 * the mapper. Defaults: an invoice in PYG, one item of 110000 taxed at 10%, issued 2025-03-10.
 * Every value is synthetic; the RUC is the Manual's example (44444401-7).
 */
final class SampleDocument {

  static final String ISSUE_DATE_TIME = "2025-03-10T09:00:00";

  /** Returned by an item override to leave the field out of that item. */
  static final String OMIT = "\u0000omit";

  private final Map<String, String> values = new HashMap<>();
  private final Set<String> omitted = new HashSet<>();
  private final Map<String, List<String[]>> extra = new LinkedHashMap<>();
  private int items = 1;
  private @Nullable BiFunction<Integer, String, @Nullable String> itemValues;

  static SampleDocument invoice() {
    return new SampleDocument();
  }

  /** A credit note (C002 = 5) for a returned product, with its associated electronic invoice. */
  static SampleDocument creditNote() {
    return new SampleDocument()
        .set("C002", "5")
        .set("C003", "Nota de crédito electrónica")
        .omit("D011")
        .omit("D012")
        .omit("E010")
        .omit("E600")
        .add("H001", "H002", "iTipDocAso", "1")
        .add("H001", "H003", "dDesTipDocAso", "Electrónico")
        .add("H001", "H004", "dCdCDERef", "01444444017001001000000122025030915873260981");
  }

  SampleDocument set(String fieldId, String value) {
    values.put(fieldId, value);
    return this;
  }

  SampleDocument omit(String fieldId) {
    omitted.add(fieldId);
    return this;
  }

  /** Adds a leaf under a group (created if it does not exist and the group is H001). */
  SampleDocument add(String parentId, String fieldId, String xml, String value) {
    extra.computeIfAbsent(parentId, k -> new ArrayList<>()).add(new String[] {fieldId, xml, value});
    return this;
  }

  SampleDocument items(int count) {
    this.items = count;
    return this;
  }

  /** Per-item override: (0-based item index, field id) to value, or null for the default. */
  SampleDocument itemValues(BiFunction<Integer, String, @Nullable String> overrides) {
    this.itemValues = overrides;
    return this;
  }

  Node build() {
    Node de = group("DE", "A001");
    Node rde = group("rDE", "AA001");
    leaf(rde, "dVerFor", "AA002", "150");
    if (de == null) {
      return rde;
    }
    rde.add(de);
    String cdc = cdc();
    if (!omitted.contains("A002")) {
      de.attribute("Id", values.getOrDefault("A002", cdc));
    }
    leaf(de, "dDVId", "A003", cdc.substring(43));
    leaf(de, "dFecFirma", "A004", "2025-03-10T10:00:00");
    leaf(de, "dSisFact", "A005", "1");

    Node ope = child(de, "gOpeDE", "B001");
    leaf(ope, "iTipEmi", "B002", "1");
    leaf(ope, "dDesTipEmi", "B003", "Normal");
    leaf(ope, "dCodSeg", "B004", "587326098");

    Node timb = child(de, "gTimb", "C001");
    leaf(timb, "iTiDE", "C002", "1");
    leaf(timb, "dDesTiDE", "C003", "Factura electrónica");
    leaf(timb, "dNumTim", "C004", "12345678");
    leaf(timb, "dEst", "C005", "001");
    leaf(timb, "dPunExp", "C006", "001");
    leaf(timb, "dNumDoc", "C007", "0000001");
    leaf(timb, "dFeIniT", "C008", "2025-01-01");

    Node gral = child(de, "gDatGralOpe", "D001");
    leaf(gral, "dFeEmiDE", "D002", ISSUE_DATE_TIME);
    Node com = child(gral, "gOpeCom", "D010");
    leaf(com, "iTipTra", "D011", "1");
    leaf(com, "dDesTipTra", "D012", "Venta de mercadería");
    leaf(com, "iTImp", "D013", "1");
    leaf(com, "dDesTImp", "D014", "IVA");
    leaf(com, "cMoneOpe", "D015", "PYG");
    leaf(com, "dDesMoneOpe", "D016", "Guarani");
    leaf(com, "dCondTiCam", "D017", null);
    leaf(com, "dTiCam", "D018", null);
    Node emis = child(gral, "gEmis", "D100");
    leaf(emis, "dRucEm", "D101", "44444401");
    leaf(emis, "dDVEmi", "D102", "7");
    leaf(emis, "iTipCont", "D103", "2");
    leaf(emis, "dNomEmi", "D105", ValueChecks.TEST_ISSUER_NAME);
    leaf(emis, "dDirEmi", "D107", "Calle sintetica");
    leaf(emis, "dNumCas", "D108", "0");
    leaf(emis, "cDepEmi", "D111", "1");
    leaf(emis, "dDesDepEmi", "D112", "CAPITAL");
    leaf(emis, "cDisEmi", "D113", null);
    leaf(emis, "cCiuEmi", "D115", "1");
    leaf(emis, "dDesCiuEmi", "D116", "ASUNCION (DISTRITO)");
    leaf(emis, "dTelEmi", "D117", "021000000");
    leaf(emis, "dEmailE", "D118", "correo@example.com");
    Node act = child(emis, "gActEco", "D130");
    leaf(act, "cActEco", "D131", "00000");
    leaf(act, "dDesActEco", "D132", "Actividad sintetica");
    Node rec = child(gral, "gDatRec", "D200");
    leaf(rec, "iNatRec", "D201", "2");
    leaf(rec, "iTiOpe", "D202", "2");
    leaf(rec, "cPaisRec", "D203", "PRY");
    leaf(rec, "dDesPaisRe", "D204", "Paraguay");
    leaf(rec, "iTipIDRec", "D208", "1");
    leaf(rec, "dDTipIDRec", "D209", "Cédula paraguaya");
    leaf(rec, "dNumIDRec", "D210", "1234567");
    leaf(rec, "dNomRec", "D211", "Receptor sintetico");

    Node tip = child(de, "gDtipDE", "E001");
    Node fe = child(tip, "gCamFE", "E010");
    leaf(fe, "iIndPres", "E011", "1");
    leaf(fe, "dDesIndPres", "E012", "Operación presencial");
    Node nc = "5".equals(values.get("C002")) ? child(tip, "gCamNCDE", "E400") : null;
    if (nc != null) {
      leaf(nc, "iMotEmi", "E401", "2");
      leaf(nc, "dDesMotEmi", "E402", "Devolución");
    }
    Node cond = child(tip, "gCamCond", "E600");
    leaf(cond, "iCondOpe", "E601", "1");
    leaf(cond, "dDCondOpe", "E602", "Contado");
    Node pay = child(cond, "gPaConEIni", "E605");
    leaf(pay, "iTiPago", "E606", "1");
    leaf(pay, "dDesTiPag", "E607", "Efectivo");
    leaf(pay, "dMonTiPag", "E608", String.valueOf(110000L * items));
    leaf(pay, "cMoneTiPag", "E609", "PYG");
    leaf(pay, "dDMoneTiPag", "E610", "Guarani");
    for (int i = 0; i < items; i++) {
      item(tip, i);
    }

    Node tot = child(de, "gTotSub", "F001");
    String total = String.valueOf(110000L * items);
    leaf(tot, "dSubExe", "F002", null);
    leaf(tot, "dSub5", "F004", null);
    leaf(tot, "dSub10", "F005", total);
    leaf(tot, "dTotOpe", "F008", total);
    leaf(tot, "dTotDesc", "F009", "0");
    leaf(tot, "dTotDescGlotem", "F033", "0");
    leaf(tot, "dTotAntItem", "F034", "0");
    leaf(tot, "dTotAnt", "F035", "0");
    leaf(tot, "dPorcDescTotal", "F010", "0");
    leaf(tot, "dDescTotal", "F011", "0");
    leaf(tot, "dAnticipo", "F012", "0");
    leaf(tot, "dRedon", "F013", "0");
    leaf(tot, "dTotGralOpe", "F014", total);
    leaf(tot, "dIVA5", "F015", null);
    leaf(tot, "dIVA10", "F016", String.valueOf(10000L * items));
    leaf(tot, "dTotIVA", "F017", String.valueOf(10000L * items));
    leaf(tot, "dBaseGrav5", "F018", null);
    leaf(tot, "dBaseGrav10", "F019", String.valueOf(100000L * items));
    leaf(tot, "dTBasGraIVA", "F020", String.valueOf(100000L * items));
    leaf(tot, "dTotalGs", "F023", null);

    List<String[]> associated = extra.get("H001");
    if (associated != null && !omitted.contains("H001")) {
      Node aso = group("gCamDEAsoc", "H001");
      for (String[] l : associated) {
        aso.add(Node.leaf(l[1], l[0], l[2]));
      }
      de.add(aso);
    }

    return rde;
  }

  private void item(Node tip, int index) {
    Node it = group("gCamItem", "E700");
    if (it == null) {
      return;
    }
    tip.add(it);
    itemLeaf(it, index, "dCodInt", "E701", "A" + index);
    itemLeaf(it, index, "dDesProSer", "E708", "Producto sintetico");
    itemLeaf(it, index, "cUniMed", "E709", "77");
    itemLeaf(it, index, "dDesUniMed", "E710", "UNI");
    itemLeaf(it, index, "dCantProSer", "E711", "1");
    Node val = group("gValorItem", "E720");
    if (val != null) {
      it.add(val);
      itemLeaf(val, index, "dPUniProSer", "E721", "110000");
      itemLeaf(val, index, "dTiCamIt", "E725", null);
      itemLeaf(val, index, "dTotBruOpeItem", "E727", "110000");
      Node resta = group("gValorRestaItem", "EA001");
      if (resta != null) {
        val.add(resta);
        itemLeaf(resta, index, "dDescItem", "EA002", "0");
        itemLeaf(resta, index, "dPorcDesIt", "EA003", null);
        itemLeaf(resta, index, "dDescGloItem", "EA004", "0");
        itemLeaf(resta, index, "dAntPreUniIt", "EA006", "0");
        itemLeaf(resta, index, "dAntGloPreUniIt", "EA007", "0");
        itemLeaf(resta, index, "dTotOpeItem", "EA008", "110000");
        itemLeaf(resta, index, "dTotOpeGs", "EA009", null);
      }
    }
    Node iva = group("gCamIVA", "E730");
    if (iva != null) {
      it.add(iva);
      itemLeaf(iva, index, "iAfecIVA", "E731", "1");
      itemLeaf(iva, index, "dDesAfecIVA", "E732", "Gravado IVA");
      itemLeaf(iva, index, "dPropIVA", "E733", "100");
      itemLeaf(iva, index, "dTasaIVA", "E734", "10");
      itemLeaf(iva, index, "dBasGravIVA", "E735", "100000");
      itemLeaf(iva, index, "dLiqIVAItem", "E736", "10000");
      itemLeaf(iva, index, "dBasExe", "E737", "0");
    }
  }

  private void itemLeaf(Node parent, int index, String xml, String id, @Nullable String fallback) {
    String v = itemValues == null ? null : itemValues.apply(index, id);
    if (OMIT.equals(v)) {
      return;
    }
    leaf(parent, xml, id, v != null ? v : fallback);
  }

  private @Nullable Node group(String xml, String id) {
    return omitted.contains(id) ? null : Node.group(xml, id);
  }

  private @Nullable Node child(@Nullable Node parent, String xml, String id) {
    Node g = parent == null ? null : group(xml, id);
    if (parent != null && g != null) {
      parent.add(g);
      for (String[] l : extra.getOrDefault(id, List.of())) {
        g.add(Node.leaf(l[1], l[0], l[2]));
      }
    }
    return g;
  }

  private void leaf(@Nullable Node parent, String xml, String id, @Nullable String fallback) {
    if (parent == null || omitted.contains(id)) {
      return;
    }
    String v = values.containsKey(id) ? values.get(id) : fallback;
    if (v != null) {
      parent.add(Node.leaf(xml, id, v));
    }
  }

  /** The CDC from the values the tree will carry (MT150 10.1). */
  private String cdc() {
    String date = val("D002", ISSUE_DATE_TIME);
    String base =
        pad(val("C002", "1"), 2)
            + pad(val("D101", "44444401"), 8)
            + val("D102", "7")
            + pad(val("C005", "001"), 3)
            + pad(val("C006", "001"), 3)
            + pad(val("C007", "0000001"), 7)
            + val("D103", "2")
            + date.substring(0, 4)
            + date.substring(5, 7)
            + date.substring(8, 10)
            + val("B002", "1")
            + pad(val("B004", "587326098"), 9);
    Integer dv = CheckDigit.modulo11(base);
    return base + (dv == null ? "0" : dv.toString());
  }

  private String val(String id, String fallback) {
    return values.getOrDefault(id, fallback);
  }

  private static String pad(String s, int n) {
    StringBuilder sb = new StringBuilder();
    for (int i = s.length(); i < n; i++) {
      sb.append('0');
    }
    return sb.append(s).toString();
  }

  /** Catalog description to emit, for tests that need it. */
  static String description(String catalogId, String code) {
    return Spec.bundled()
        .catalogs()
        .get(catalogId)
        .find(code, null)
        .orElseThrow()
        .descriptionToEmit();
  }
}
