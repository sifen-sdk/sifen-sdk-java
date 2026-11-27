/*
 * Copyright 2026 SIFEN SDK contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.sifensdk.core.internal.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.sifensdk.core.internal.spec.Spec;
import io.github.sifensdk.core.internal.tree.Node;
import io.github.sifensdk.core.internal.xml.SchemaValidator;
import io.github.sifensdk.core.internal.xml.XmlWriter;
import io.github.sifensdk.core.model.Carrier;
import io.github.sifensdk.core.model.DeliveryNote;
import io.github.sifensdk.core.model.ElectronicDocument;
import io.github.sifensdk.core.model.Transport;
import io.github.sifensdk.core.model.TransportDelivery;
import io.github.sifensdk.core.model.TransportDeparture;
import io.github.sifensdk.core.model.TransportVehicle;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Mapping of the delivery note ({@code gCamNRE}, E500) and the transport ({@code gTransp}, E900).
 */
class TransportMapperTest {

  private static final LocalDateTime SIGNED = LocalDateTime.of(2026, 11, 26, 10, 5, 30);

  private final Descriptions descriptions = new Descriptions(Spec.bundled());

  @Test
  void mapsTheSampleDeliveryNote() {
    Node root = DocumentMapper.toTree(TransportSamples.deliveryNote(), SIGNED);

    assertThat(texts(root, "C002")).containsExactly("7");
    assertThat(texts(root, "C003")).containsExactly("Nota de remisión electrónica");
    assertThat(names(root.findAll("E001").get(0)))
        .containsExactly("gCamNRE", "gCamItem", "gTransp");
    assertThat(texts(root, "E502")).containsExactly("Traslado por ventas");
    assertThat(texts(root, "E504")).containsExactly("Emisor de la factura");
    assertThat(texts(root, "E505")).containsExactly("15");
    assertThat(texts(root, "E506")).containsExactly("2026-11-30");
    assertThat(texts(root, "E902")).containsExactly("Propio");
    assertThat(texts(root, "E904")).containsExactly("Terrestre");
    assertThat(texts(root, "E926")).containsExactly("CAPITAL");
    assertThat(texts(root, "E930")).containsExactly("ASUNCION (DISTRITO)");
    assertThat(texts(root, "E986")).containsExactly("Cédula paraguaya");
    // What C002 = 7 leaves out.
    assertThat(root.findAll("D010")).isEmpty();
    assertThat(root.findAll("E600")).isEmpty();
    assertThat(root.findAll("E720")).isEmpty();
    assertThat(root.findAll("E730")).isEmpty();
    assertThat(root.findAll("F001")).isEmpty();
  }

  @Test
  void writesEveryFieldInTheOrderOfTheXsd() {
    Node note = new DeliveryNoteMapper(descriptions).deliveryNote(fullDeliveryNote());
    assertThat(names(note))
        .containsExactly(
            "iMotEmiNR",
            "dDesMotEmiNR",
            "iRespEmiNR",
            "dDesRespEmiNR",
            "dKmR",
            "dFecEm",
            "cPreFle");

    Node transport = new TransportMapper(descriptions).transport(fullTransport());
    assertThat(names(transport))
        .containsExactly(
            "iTipTrans",
            "dDesTipTrans",
            "iModTrans",
            "dDesModTrans",
            "iRespFlete",
            "cCondNeg",
            "dNuManif",
            "dNuDespImp",
            "dIniTras",
            "dFinTras",
            "cPaisDest",
            "dDesPaisDest",
            "gCamSal",
            "gCamEnt",
            "gCamEnt",
            "gVehTras",
            "gVehTras",
            "gCamTrans");
    assertThat(names(transport.findAll("E920").get(0)))
        .containsExactly(
            "dDirLocSal",
            "dNumCasSal",
            "dComp1Sal",
            "dComp2Sal",
            "cDepSal",
            "dDesDepSal",
            "cDisSal",
            "dDesDisSal",
            "cCiuSal",
            "dDesCiuSal",
            "dTelSal");
    assertThat(names(transport.findAll("E940").get(1)))
        .containsExactly(
            "dDirLocEnt",
            "dNumCasEnt",
            "dComp1Ent",
            "dComp2Ent",
            "cDepEnt",
            "dDesDepEnt",
            "cDisEnt",
            "dDesDisEnt",
            "cCiuEnt",
            "dDesCiuEnt",
            "dTelEnt");
    assertThat(names(transport.findAll("E960").get(0)))
        .containsExactly(
            "dTiVehTras",
            "dMarVeh",
            "dTipIdenVeh",
            "dNroIDVeh",
            "dAdicVeh",
            "dNroMatVeh",
            "dNroVuelo");
    assertThat(names(transport.findAll("E980").get(0)))
        .containsExactly(
            "iNatTrans",
            "dNomTrans",
            "dRucTrans",
            "dDVTrans",
            "iTipIDTrans",
            "dDTipIDTrans",
            "dNumIDTrans",
            "cNacTrans",
            "dDesNacTrans",
            "dNumIDChof",
            "dNomChof",
            "dDomFisc",
            "dDirChof",
            "dNombAg",
            "dRucAg",
            "dDVAg",
            "dDirAge");
    assertThat(texts(transport, "E912")).containsExactly("Brasil");
    assertThat(texts(transport, "E989")).containsExactly("Paraguay");
    assertThat(texts(transport, "E941")).containsExactly("Entrega 1", "Entrega 2");
    assertThat(transport.findAll("E961").get(1).path()).isEqualTo("gTransp/gVehTras[2]/dTiVehTras");
  }

  @Test
  void aDocumentWithEveryFieldIsValidAgainstTheXsd() {
    ElectronicDocument note = TransportSamples.deliveryNote();
    ElectronicDocument full =
        note.toBuilder()
            .documentTypeData(
                note.documentTypeData().toBuilder()
                    .deliveryNote(fullDeliveryNote())
                    .transport(fullTransport())
                    .build())
            .build();
    byte[] xml = XmlWriter.write(DocumentMapper.toTree(full, SIGNED));
    assertThat(SchemaValidator.validateDocument(xml)).isEmpty();
  }

  @Test
  void keepsTheFreightAmountExactlyAsGiven() {
    DeliveryNote note =
        fullDeliveryNote().toBuilder().freightAmount(new BigDecimal("1E+5")).build();
    assertThat(texts(new DeliveryNoteMapper(descriptions).deliveryNote(note), "E507"))
        .containsExactly("100000");
    note = note.toBuilder().freightAmount(new BigDecimal("150000.50")).build();
    assertThat(texts(new DeliveryNoteMapper(descriptions).deliveryNote(note), "E507"))
        .containsExactly("150000.50");
  }

  @Test
  void writesTheXsdTextOfTheReasonForRepairs() {
    // C-206: the XSD says "para reparación" where the Manual says "por reparación".
    DeliveryNote note = fullDeliveryNote().toBuilder().reasonCode("9").build();
    assertThat(texts(new DeliveryNoteMapper(descriptions).deliveryNote(note), "E502"))
        .containsExactly("Traslado de bienes para reparación");
  }

  @Test
  void anOtherReasonKeepsItsFreeDescription() {
    DeliveryNote note =
        fullDeliveryNote().toBuilder().reasonCode("99").reasonDescription("Motivo propio").build();
    assertThat(texts(new DeliveryNoteMapper(descriptions).deliveryNote(note), "E502"))
        .containsExactly("Motivo propio");
  }

  @Test
  void leavesOutWhatIsNotInformed() {
    Node root = DocumentMapper.toTree(TransportSamples.invoiceWithTransport(), SIGNED);
    assertThat(root.findAll("E500")).isEmpty();
    assertThat(root.findAll("E901")).isEmpty();
    assertThat(root.findAll("E902")).isEmpty();
    assertThat(root.findAll("E920")).isEmpty();
    assertThat(root.findAll("E980")).isEmpty();
    assertThat(texts(root, "E965")).containsExactly("ABC123");
    assertThat(names(root.findAll("E001").get(0)))
        .containsExactly("gCamFE", "gCamCond", "gCamItem", "gCamItem", "gTransp");
  }

  @Test
  void toStringShowsOnlyCounts() {
    ElectronicDocument note = TransportSamples.deliveryNote();
    assertThat(note.documentTypeData().transport().toString())
        .isEqualTo("Transport[deliveryLocations=1, vehicles=1]");
    assertThat(fullTransport().carrier().toString()).isEqualTo("Carrier");
    assertThat(fullDeliveryNote().toString()).isEqualTo("DeliveryNote");
  }

  @Test
  void equalObjectsHaveEqualHashCodes() {
    assertThat(fullTransport()).isEqualTo(fullTransport()).hasSameHashCodeAs(fullTransport());
    assertThat(fullTransport().toBuilder().build()).isEqualTo(fullTransport());
    assertThat(fullDeliveryNote().toBuilder().build()).isEqualTo(fullDeliveryNote());
    assertThat(fullTransport().toBuilder().modeCode("2").build()).isNotEqualTo(fullTransport());
  }

  // ---------------------------------------------------------------------------------------------

  private static DeliveryNote fullDeliveryNote() {
    return DeliveryNote.builder()
        .reasonCode("1")
        .responsibleCode("1")
        .estimatedKilometers(120)
        .futureInvoiceDate(LocalDate.of(2026, 11, 30))
        .freightAmount(new BigDecimal("150000"))
        .build();
  }

  private static Transport fullTransport() {
    return Transport.builder()
        .typeCode("2")
        .modeCode("1")
        .freightPayerCode("3")
        .negotiationConditionCode("FOB")
        .manifestNumber("MAN-1")
        .importDispatchNumber("0000000000000001")
        .estimatedStartDate(LocalDate.of(2026, 11, 26))
        .estimatedEndDate(LocalDate.of(2026, 11, 28))
        .destinationCountryCode("BRA")
        .departureLocation(
            TransportDeparture.builder()
                .address("Salida")
                .houseNumber("1")
                .addressComplement1("Complemento 1")
                .addressComplement2("Complemento 2")
                .departmentCode("1")
                .districtCode("1")
                .cityCode("1")
                .phone("021000000")
                .build())
        .addDeliveryLocation(delivery("Entrega 1"))
        .addDeliveryLocation(delivery("Entrega 2"))
        .addVehicle(vehicle())
        .addVehicle(vehicle())
        .carrier(
            Carrier.builder()
                .natureCode("2")
                .name("Transportista de Ejemplo")
                .ruc("44444402")
                .checkDigit("5")
                .idDocumentTypeCode("1")
                .idDocumentNumber("1234567")
                .nationalityCode("PRY")
                .driverIdDocumentNumber("1234567")
                .driverName("Chofer de Ejemplo")
                .taxAddress("Domicilio fiscal")
                .driverAddress("Dirección del chofer")
                .agentName("Agente de Ejemplo")
                .agentRuc("44444401")
                .agentCheckDigit("7")
                .agentAddress("Dirección del agente")
                .build())
        .build();
  }

  private static TransportDelivery delivery(String address) {
    return TransportDelivery.builder()
        .address(address)
        .houseNumber("2")
        .addressComplement1("Complemento 1")
        .addressComplement2("Complemento 2")
        .departmentCode("1")
        .districtCode("1")
        .cityCode("1")
        .phone("021000001")
        .build();
  }

  private static TransportVehicle vehicle() {
    return TransportVehicle.builder()
        .type("Camión")
        .brand("Marca")
        .identificationTypeCode("1")
        .identificationNumber("VIN0000000000001")
        .additionalData("Dato adicional")
        .plateNumber("ABC123")
        .flightNumber("PZ1234")
        .build();
  }

  private static List<String> names(Node node) {
    return node.children().stream().map(Node::name).collect(Collectors.toList());
  }

  private static List<String> texts(Node root, String id) {
    return root.findAll(id).stream().map(Node::text).collect(Collectors.toList());
  }
}
