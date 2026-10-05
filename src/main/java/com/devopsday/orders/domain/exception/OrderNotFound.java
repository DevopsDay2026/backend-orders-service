package com.devopsday.orders.domain.exception;

import com.devopsday.orders.domain.model.OrderId;

public final class OrderNotFound extends DomainException {

    public OrderNotFound(OrderId id) {
        super("order-not-found", "order " + id + " does not exist");
    }
}
