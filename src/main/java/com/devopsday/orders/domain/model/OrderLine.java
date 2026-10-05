package com.devopsday.orders.domain.model;

import com.devopsday.orders.domain.exception.InvalidOrder;

public record OrderLine(String sku, int quantity) {

    public OrderLine {
        if (sku == null || sku.isBlank()) {
            throw new InvalidOrder("sku is required");
        }
        if (quantity <= 0) {
            throw new InvalidOrder("quantity must be positive for sku " + sku);
        }
    }
}
