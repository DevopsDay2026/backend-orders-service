package com.devopsday.orders.adapter.in.rest;

import com.devopsday.orders.domain.model.Order;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderResponse(
        UUID id,
        String customerId,
        String status,
        String rejectionReason,
        Instant createdAt,
        List<Line> lines) {

    public record Line(String sku, int quantity) {}

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.id().value(),
                order.customerId(),
                order.status().name(),
                order.rejectionReason().orElse(null),
                order.createdAt(),
                order.lines().stream().map(line -> new Line(line.sku(), line.quantity())).toList());
    }
}
