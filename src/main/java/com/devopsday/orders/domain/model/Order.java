package com.devopsday.orders.domain.model;

import com.devopsday.orders.domain.exception.InvalidOrder;
import com.devopsday.orders.domain.exception.InvalidOrderState;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Order {

    private final OrderId id;
    private final String customerId;
    private final List<OrderLine> lines;
    private final OrderStatus status;
    private final String rejectionReason;
    private final Instant createdAt;

    private Order(
            OrderId id,
            String customerId,
            List<OrderLine> lines,
            OrderStatus status,
            String rejectionReason,
            Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        if (customerId == null || customerId.isBlank()) {
            throw new InvalidOrder("customerId is required");
        }
        if (lines == null || lines.isEmpty()) {
            throw new InvalidOrder("an order needs at least one line");
        }
        this.customerId = customerId;
        this.lines = List.copyOf(lines);
        this.rejectionReason = rejectionReason;
    }

    public static Order place(OrderId id, String customerId, List<OrderLine> lines, Instant now) {
        return new Order(id, customerId, lines, OrderStatus.PENDING, null, now);
    }

    public static Order restore(
            OrderId id,
            String customerId,
            List<OrderLine> lines,
            OrderStatus status,
            String rejectionReason,
            Instant createdAt) {
        return new Order(id, customerId, lines, status, rejectionReason, createdAt);
    }

    public Order confirm() {
        requirePending("confirm");
        return new Order(id, customerId, lines, OrderStatus.CONFIRMED, null, createdAt);
    }

    public Order reject(String reason) {
        requirePending("reject");
        return new Order(id, customerId, lines, OrderStatus.REJECTED, reason, createdAt);
    }

    private void requirePending(String action) {
        if (status != OrderStatus.PENDING) {
            throw new InvalidOrderState(
                    "cannot " + action + " order " + id + " in status " + status);
        }
    }

    public OrderId id() {
        return id;
    }

    public String customerId() {
        return customerId;
    }

    public List<OrderLine> lines() {
        return lines;
    }

    public OrderStatus status() {
        return status;
    }

    public Optional<String> rejectionReason() {
        return Optional.ofNullable(rejectionReason);
    }

    public Instant createdAt() {
        return createdAt;
    }
}
