package com.devopsday.orders.domain.event;

import java.time.Instant;
import java.util.UUID;

public sealed interface DomainEvent permits OrderPlaced {

    UUID eventId();

    Instant occurredAt();

    UUID aggregateId();
}
