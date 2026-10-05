package com.devopsday.orders.adapter.out.messaging;

import com.devopsday.orders.domain.event.OrderPlaced;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RegisterForReflection
public record OrderPlacedV1(
        UUID eventId, Instant occurredAt, UUID orderId, String customerId, List<Line> lines) {

    public static final String TYPE = "OrderPlacedV1";

    public OrderPlacedV1 {
        lines = List.copyOf(lines);
    }

    public static OrderPlacedV1 from(OrderPlaced event) {
        return new OrderPlacedV1(
                event.eventId(),
                event.occurredAt(),
                event.orderId().value(),
                event.customerId(),
                event.lines().stream().map(line -> new Line(line.sku(), line.quantity())).toList());
    }

    @RegisterForReflection
    public record Line(String sku, int quantity) {}
}
