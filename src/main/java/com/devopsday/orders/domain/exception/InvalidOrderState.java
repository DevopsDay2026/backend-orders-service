package com.devopsday.orders.domain.exception;

public final class InvalidOrderState extends DomainException {

    public InvalidOrderState(String message) {
        super("invalid-order-state", message);
    }
}
