package com.devopsday.orders.application.usecase;

import com.devopsday.orders.application.port.in.GetOrderQuery;
import com.devopsday.orders.application.port.out.OrderRepository;
import com.devopsday.orders.domain.exception.OrderNotFound;
import com.devopsday.orders.domain.model.Order;
import com.devopsday.orders.domain.model.OrderId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class GetOrderService implements GetOrderQuery {

    private final OrderRepository orders;

    GetOrderService(OrderRepository orders) {
        this.orders = orders;
    }

    @Override
    @Transactional
    public Order byId(OrderId id) {
        return orders.findById(id).orElseThrow(() -> new OrderNotFound(id));
    }
}
