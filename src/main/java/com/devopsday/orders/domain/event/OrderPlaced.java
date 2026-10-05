package com.devopsday.orders.domain.event;

import com.devopsday.orders.domain.model.OrderId;
import com.devopsday.orders.domain.model.OrderLine;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderPlaced(
        UUID eventId, Instant occurredAt, OrderId orderId, String customerId, List<OrderLine> lines)
        implements DomainEvent {

    public OrderPlaced {
        lines = List.copyOf(lines);
    }

    @Override
    public UUID aggregateId() {
        return orderId.value();
    }
}
