# Primeros pasos

## Requisitos

- **Java 17 o superior.** Un solo artefacto, probado en 17, 21 y 25.
- Un **certificado de firma** habilitado para el emisor, un **timbrado** y un **CSC** (código de seguridad del
  contribuyente) del ambiente que vas a usar. Para empezar, el ambiente de test.

## Dependencia

!!! warning "Todavía no está en Maven Central"
    El SDK aún no tiene versión publicada. Para probarlo, compilalo e instalalo en tu repositorio local de Maven:

    ```bash
    git clone --recurse-submodules https://github.com/sifen-sdk/sifen-sdk-java.git
    cd sifen-sdk-java
    ./mvnw install -DskipTests
    ```

    Usá la versión que figura en el `pom.xml` raíz (termina en `-SNAPSHOT`).

Importá el BOM, que alinea las versiones de todos los módulos, y agregá los que uses:

```xml
<properties>
  <sifen-sdk.version><!-- versión del SDK --></sifen-sdk.version>
</properties>

<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.github.sifen-sdk</groupId>
      <artifactId>sifen-sdk-bom</artifactId>
      <version>${sifen-sdk.version}</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>io.github.sifen-sdk</groupId>
    <artifactId>sifen-sdk-core</artifactId>   <!-- validar y firmar, sin red -->
  </dependency>
  <dependency>
    <groupId>io.github.sifen-sdk</groupId>
    <artifactId>sifen-sdk-client</artifactId> <!-- enviar y consultar; incluye core -->
  </dependency>
</dependencies>
```

Con Gradle:

```kotlin
dependencies {
    implementation(platform("io.github.sifen-sdk:sifen-sdk-bom:$sifenSdkVersion"))
    implementation("io.github.sifen-sdk:sifen-sdk-client")
}
```

Si tu aplicación usa módulos de Java (JPMS), los nombres son `io.github.sifensdk.core` e
`io.github.sifensdk.client`.

## Validar, firmar y enviar

```java
import io.github.sifensdk.client.BatchResult;
import io.github.sifensdk.client.BatchSubmission;
import io.github.sifensdk.client.SifenClient;
import io.github.sifensdk.core.Environment;
import io.github.sifensdk.core.SifenDocuments;
import io.github.sifensdk.core.model.ElectronicDocument;
import io.github.sifensdk.core.signing.Csc;
import io.github.sifensdk.core.signing.KeyMaterial;
import io.github.sifensdk.core.signing.KeyMaterials;
import io.github.sifensdk.core.signing.SignedDocument;
import io.github.sifensdk.core.validation.ValidationResult;

SifenDocuments documents = SifenDocuments.builder()
    .environment(Environment.TEST)
    .build();                                   // inmutable: creá una por ambiente y reutilizala

ElectronicDocument invoice = ElectronicDocument.builder()
    // timbrado, emisor, receptor, ítems y TODOS los importes, tal como los calculó tu sistema
    .build();

ValidationResult result = documents.validate(invoice);
result.errors().forEach(System.out::println);
// ERROR rDE/DE/gTotSub/dTotGralOpe (dTotGralOpe, F014): ... [R-F014-A3, SIFEN 2365, MT150 2365]

KeyMaterial certificate = KeyMaterials.pkcs12(Path.of("certificado.p12"), password);
Csc csc = Csc.of("0001", secret);
SignedDocument signed = documents.sign(invoice, certificate, csc);

store(signed.cdc(), signed.xml());              // guardalo antes de enviarlo; se envían esos mismos bytes

SifenClient client = SifenClient.builder()
    .environment(Environment.TEST)
    .keyMaterial(certificate)                   // TLS mutuo con el certificado de ESTE emisor
    .build();                                   // uno por emisor; inmutable y seguro entre hilos

BatchSubmission submission = client.sendBatch(List.of(signed));
// Más tarde (BatchSchedule dice cuándo, para no provocar bloqueos):
BatchResult batch = client.queryBatch(submission.protocolNumber());
```

Paso a paso:

1. **`SifenDocuments`** valida y firma. No usa la red. El ambiente lo elegís vos: no hay valor por defecto.
2. **`ElectronicDocument`** es el documento, inmutable y armado con builders. Cómo se arma cada tipo está en
   [Documentos](guias/documentos.md).
3. **`validate`** aplica las reglas de SIFEN vigentes en la fecha de emisión y el esquema XSD. Devuelve todos los
   problemas juntos, no solo el primero.
4. **`sign`** valida de nuevo, verifica el certificado, completa el CDC, la firma y el QR, y devuelve un
   `SignedDocument`: los bytes exactos del XML, el CDC y la URL del QR. Si hay errores, lanza
   `ValidationException` con el mismo `ValidationResult`.
5. **Guardá** `signed.xml()` antes de enviarlo. Si la red falla, reenviás esos mismos bytes.
6. **`SifenClient`** envía y consulta, uno por emisor. Ver [Envío y consultas](guias/envio-y-consultas.md).

`password` y `secret` son `char[]`: borralos con `Arrays.fill` cuando ya no los necesites. Si el certificado está en
un token, un HSM o un servicio de firma remota, solo cambia cómo se crea el `KeyMaterial`
([guía](claves-en-token-hsm-y-firma-remota.md)).

## Resultados y errores

- **Un rechazo de SIFEN es un resultado**, no una excepción: `Status.REJECTED` con sus códigos y mensajes.
- **Las excepciones son no comprobadas** y dicen qué falló: `ValidationException` (datos),
  `SigningException` (certificado o firma), `TransportException` (red) y `ProtocolException` (una respuesta que no
  es de SIFEN).
- **Todo intercambio queda disponible** en `exchange()`: los bytes enviados y recibidos, para auditoría.

## Para tus pruebas

`sifen-sdk-testing` trae un SIFEN simulado, local, con TLS mutuo y respuestas válidas según los esquemas oficiales.
Agregalo con `<scope>test</scope>`. Para probar contra el ambiente de test real, ver
[Pruebas contra SIFEN](pruebas-contra-sifen.md).
