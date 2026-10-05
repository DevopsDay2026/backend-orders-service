package com.devopsday.orders.application.port.out;

import com.devopsday.orders.domain.model.Order;
import com.devopsday.orders.domain.model.OrderId;
import java.util.Optional;

public interface OrderRepository {

    Optional<Order> findById(OrderId id);

    void save(Order order);
}
