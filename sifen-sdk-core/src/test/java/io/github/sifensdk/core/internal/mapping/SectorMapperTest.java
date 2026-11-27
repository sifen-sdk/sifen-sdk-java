/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.sifensdk.core.internal.tree.Node;
import io.github.sifensdk.core.model.ElectronicDocument;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class SectorMapperTest {

  private static final LocalDateTime SIGNED = LocalDateTime.of(2026, 11, 26, 10, 5, 30);

  @Test
  void placesTheSectorFieldsAfterTheItemsInsideGDtipDE() {
    Node root = DocumentMapper.toTree(SectorSamples.electricityInvoice(), SIGNED);
    Node typeData = root.findAll("E001").get(0);
    assertThat(names(typeData))
        .containsExactly("gCamFE", "gCamCond", "gCamItem", "gCamItem", "gCamEsp");
    assertThat(root.findAll("E790").get(0).path()).isEqualTo("rDE/DE/gDtipDE/gCamEsp");
  }

  @Test
  void writesEveryGroupInXsdOrderWithTheValuesAsGiven() {
    Node esp =
        DocumentMapper.toTree(SectorSamples.allSectorsInvoice(), SIGNED).findAll("E790").get(0);
    assertThat(names(esp))
        .containsExactly("gGrupEner", "gGrupEner", "gGrupSeg", "gGrupSup", "gGrupAdi");

    Node energy = esp.children().get(0);
    assertThat(names(energy))
        .containsExactly("dNroMed", "dActiv", "dCateg", "dLecAnt", "dLecAct", "dConKwh");
    assertThat(texts(esp, "E795")).containsExactly("12400.00", "0");
    assertThat(texts(esp, "E796")).containsExactly("12750.50", "80.25");
    assertThat(texts(esp, "E797")).containsExactly("350.50", "80.25");

    Node insurance = esp.children().get(2);
    assertThat(names(insurance)).containsExactly("dCodEmpSeg", "gGrupPolSeg", "gGrupPolSeg");
    assertThat(names(insurance.children().get(1)))
        .containsExactly(
            "dPoliza",
            "dUnidVig",
            "dVigencia",
            "dNumPoliza",
            "dFecIniVig",
            "dFecFinVig",
            "dCodInt");
    assertThat(names(insurance.children().get(2)))
        .containsExactly("dPoliza", "dUnidVig", "dVigencia", "dNumPoliza");
    assertThat(texts(esp, "EA793")).containsExactly("1", "6.5");
    assertThat(texts(esp, "EA795")).containsExactly("2026-11-26T00:00:00");
    assertThat(texts(esp, "EA796")).containsExactly("2027-11-25T23:59:59");

    assertThat(names(esp.children().get(3)))
        .containsExactly("dNomCaj", "dEfectivo", "dVuelto", "dDonac", "dDesDonac");
    assertThat(texts(esp, "E812")).containsExactly("150000");

    assertThat(names(esp.children().get(4)))
        .containsExactly(
            "dCiclo",
            "dFecIniC",
            "dFecFinC",
            "dVencPag",
            "dVencPag",
            "dVencPag",
            "dContrato",
            "dSalAnt",
            "dCodConDncp");
    assertThat(texts(esp, "E822")).containsExactly("2026-11-01");
    assertThat(texts(esp, "E824")).containsExactly("2026-12-10", "2026-12-20", "2026-12-30");
    assertThat(texts(esp, "E826")).containsExactly("15000.5000");
  }

  @Test
  void leavesOutTheGroupsAndFieldsThatAreNotInformed() {
    assertThat(DocumentMapper.toTree(Samples.invoice(), SIGNED).findAll("E790")).isEmpty();

    Node esp =
        DocumentMapper.toTree(SectorSamples.insuranceInvoice(), SIGNED).findAll("E790").get(0);
    assertThat(names(esp)).containsExactly("gGrupSeg");
    assertThat(names(esp.children().get(0))).containsExactly("gGrupPolSeg");
  }

  @Test
  void theModelToStringShowsNoValues() {
    ElectronicDocument all = SectorSamples.allSectorsInvoice();
    var sector = all.documentTypeData().sectorSpecificFields();
    assertThat(sector).isNotNull();
    assertThat(sector.toString()).isEqualTo("SectorSpecificFields[electricity=2]");
    assertThat(sector.insurance().toString()).isEqualTo("Insurance[policies=2]");
    assertThat(sector.electricity().get(0).toString()).isEqualTo("Electricity");
    assertThat(sector.additionalCommercialData().toString())
        .isEqualTo("AdditionalCommercialData[paymentDueDates=3]");
    assertThat(sector.toBuilder().build()).isEqualTo(sector).hasSameHashCodeAs(sector);
  }

  private static List<String> names(Node node) {
    return node.children().stream().map(Node::name).collect(Collectors.toList());
  }

  private static List<String> texts(Node root, String id) {
    return root.findAll(id).stream().map(Node::text).collect(Collectors.toList());
  }
}
