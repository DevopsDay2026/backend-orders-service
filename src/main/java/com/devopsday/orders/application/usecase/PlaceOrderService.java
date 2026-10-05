package com.devopsday.orders.application.usecase;

import com.devopsday.orders.application.port.in.PlaceOrderUseCase;
import com.devopsday.orders.application.port.out.Clock;
import com.devopsday.orders.application.port.out.IdGenerator;
import com.devopsday.orders.application.port.out.OrderEventPublisher;
import com.devopsday.orders.application.port.out.OrderRepository;
import com.devopsday.orders.domain.event.OrderPlaced;
import com.devopsday.orders.domain.model.Order;
import com.devopsday.orders.domain.model.OrderId;
import com.devopsday.orders.domain.model.OrderLine;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PlaceOrderService implements PlaceOrderUseCase {

    private final OrderRepository orders;
    private final OrderEventPublisher events;
    private final Clock clock;
    private final IdGenerator ids;

    PlaceOrderService(
            OrderRepository orders, OrderEventPublisher events, Clock clock, IdGenerator ids) {
        this.orders = orders;
        this.events = events;
        this.clock = clock;
        this.ids = ids;
    }

    @Override
    @Transactional
    public Order place(Command command) {
        var lines =
                command.lines().stream()
                        .map(line -> new OrderLine(line.sku(), line.quantity()))
                        .toList();
        var now = clock.now();
        var order = Order.place(new OrderId(ids.newId()), command.customerId(), lines, now);
        orders.save(order);
        events.publish(
                new OrderPlaced(ids.newId(), now, order.id(), order.customerId(), order.lines()));
        return order;
    }
}
