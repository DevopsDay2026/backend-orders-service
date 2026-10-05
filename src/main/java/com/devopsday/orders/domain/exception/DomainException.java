package com.devopsday.orders.domain.exception;

public abstract sealed class DomainException extends RuntimeException
        permits InvalidOrder, InvalidOrderState, OrderNotFound {

    private final String code;

    protected DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
