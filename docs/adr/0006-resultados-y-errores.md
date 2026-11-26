# 0006 — El rechazo de SIFEN es un resultado; las excepciones son no comprobadas

- **Estado:** Aceptada
- **Fecha:** 2026-11-26

## Contexto

Que SIFEN rechace un documento es un desenlace normal del negocio, no una falla del programa. Sí son fallas un
corte de red, una respuesta ilegible o un certificado inválido.

## Decisión

- Toda respuesta de SIFEN es un valor tipado: aprobado, aprobado con observación, rechazado o en proceso, con sus
  códigos y mensajes.
- Las excepciones son no comprobadas y distinguen validación local, firma, transporte y protocolo.
- Una respuesta que no es de SIFEN (por ejemplo, una página HTML del balanceador) es un error de protocolo, nunca
  un resultado vacío.
- Ni los resultados ni las excepciones incluyen secretos; el contenido de los documentos no se escribe en logs.

## Consecuencias

- Quien integra está obligado a tratar el rechazo como un caso más.
- Los códigos de respuesta se mantienen como datos en `spec`.

## Alternativas descartadas

- **Excepción por cada rechazo:** usa excepciones para el flujo normal.
- **Una única excepción comprobada para todo:** no permite distinguir un corte de red de un error de datos.
