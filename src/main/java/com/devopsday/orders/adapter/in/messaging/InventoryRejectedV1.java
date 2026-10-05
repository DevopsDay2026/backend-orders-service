package com.devopsday.orders.adapter.in.messaging;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@RegisterForReflection
public record InventoryRejectedV1(UUID eventId, Instant occurredAt, UUID orderId, String reason) {

    public InventoryRejectedV1 {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(orderId, "orderId is required");
    }
}
