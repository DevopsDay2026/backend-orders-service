# ADR-0002: Outbox transaccional y consumidores idempotentes

- Estado: Aceptado
- Fecha: 2026-10-03

## Contexto

Guardar en PostgreSQL y publicar en Kafka son dos sistemas distintos: si se publica directo desde el
caso de uso, un fallo entre ambos pierde el evento o publica algo que nunca se guardó.

## Decisión

- **Publicación**: el caso de uso llama al puerto de publicación, cuyo adaptador inserta una fila
  en la tabla `outbox` dentro de la misma transacción. `OutboxRelay` (`@Scheduled`) lee las
  pendientes con `FOR UPDATE SKIP LOCKED`, las envía por SmallRye Reactive Messaging y las marca
  como enviadas solo cuando el broker confirma. Varias réplicas pueden correr el relay a la vez.
- **Consumo**: `@Incoming` + `@Blocking`. El caso de uso registra el `eventId` en `processed_event`
  (`INSERT ... ON CONFLICT DO NOTHING`) en la misma transacción del cambio; un evento repetido no
  hace nada. Los fallos van a `<tópico>.dlq` (`failure-strategy=dead-letter-queue`).
- **Contrato**: records versionados (`OrderPlacedV1`) con `eventId`, `occurredAt` y clave igual al
  id del agregado, documentados en `docs/events.md` en cada repositorio. No hay librería compartida.
- **Deserialización**: el canal entrega `String` y el adaptador lo convierte con Jackson. Un JSON
  inválido se convierte en una excepción del consumidor y termina en la DLQ, en vez de detener el
  canal como haría un fallo dentro de un `Deserializer` de Kafka.

## Dependencias que justifica

`quarkus-messaging-kafka`, `quarkus-scheduler`, `quarkus-smallrye-fault-tolerance` (reintentos de
los consumidores ante fallos transitorios de persistencia antes de ir a la DLQ) y, en tests,
`smallrye-reactive-messaging-in-memory` y `awaitility`.

## Consecuencias

- Entrega *at-least-once*: un evento puede publicarse dos veces; la idempotencia lo absorbe.
- Hay un retardo de hasta `OUTBOX_POLL_INTERVAL` entre el commit y la publicación.
- La tabla `outbox` crece; las filas enviadas se pueden purgar con un job externo.
