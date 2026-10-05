package com.devopsday.orders.testsupport;

import com.devopsday.orders.domain.model.Order;
import com.devopsday.orders.domain.model.OrderId;
import com.devopsday.orders.domain.model.OrderLine;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class OrderFixtures {

    public static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private OrderFixtures() {}

    public static Order pendingOrder() {
        return Order.place(
                new OrderId(UUID.randomUUID()),
                "customer-1",
                List.of(new OrderLine("SKU-1", 2), new OrderLine("SKU-2", 1)),
                NOW);
    }
}
