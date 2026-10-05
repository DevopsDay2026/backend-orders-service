package com.devopsday.orders.testsupport;

import com.devopsday.orders.application.port.out.OrderRepository;
import com.devopsday.orders.domain.model.Order;
import com.devopsday.orders.domain.model.OrderId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class InMemoryOrderRepository implements OrderRepository {

    private final Map<OrderId, Order> store = new HashMap<>();

    @Override
    public Optional<Order> findById(OrderId id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void save(Order order) {
        store.put(order.id(), order);
    }

    public int size() {
        return store.size();
    }
}
