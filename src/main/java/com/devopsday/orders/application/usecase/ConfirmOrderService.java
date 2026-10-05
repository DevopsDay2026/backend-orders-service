package com.devopsday.orders.application.usecase;

import com.devopsday.orders.application.port.in.ConfirmOrderUseCase;
import com.devopsday.orders.application.port.out.OrderRepository;
import com.devopsday.orders.application.port.out.ProcessedEventStore;
import com.devopsday.orders.domain.exception.OrderNotFound;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ConfirmOrderService implements ConfirmOrderUseCase {

    private final OrderRepository orders;
    private final ProcessedEventStore processedEvents;

    ConfirmOrderService(OrderRepository orders, ProcessedEventStore processedEvents) {
        this.orders = orders;
        this.processedEvents = processedEvents;
    }

    @Override
    @Transactional
    public void confirm(Command command) {
        if (!processedEvents.markIfNew(command.eventId())) {
            return;
        }
        var order =
                orders.findById(command.orderId())
                        .orElseThrow(() -> new OrderNotFound(command.orderId()));
        orders.save(order.confirm());
    }
}
