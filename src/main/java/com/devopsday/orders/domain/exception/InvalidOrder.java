package com.devopsday.orders.domain.exception;

public final class InvalidOrder extends DomainException {

    public InvalidOrder(String message) {
        super("invalid-order", message);
    }
}
