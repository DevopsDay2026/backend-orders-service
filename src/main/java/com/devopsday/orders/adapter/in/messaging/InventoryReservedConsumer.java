package com.devopsday.orders.adapter.in.messaging;

import com.devopsday.orders.application.port.in.ConfirmOrderUseCase;
import com.devopsday.orders.domain.model.OrderId;
import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.PersistenceException;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class InventoryReservedConsumer {

    private final ConfirmOrderUseCase confirmOrder;
    private final EventJsonReader reader;

    InventoryReservedConsumer(ConfirmOrderUseCase confirmOrder, EventJsonReader reader) {
        this.confirmOrder = confirmOrder;
        this.reader = reader;
    }

    @Incoming("inventory-reserved")
    @Blocking
    @Retry(maxRetries = 3, delay = 200, retryOn = PersistenceException.class)
    public void on(String payload) {
        var event = reader.read(payload, InventoryReservedV1.class);
        confirmOrder.confirm(
                new ConfirmOrderUseCase.Command(event.eventId(), new OrderId(event.orderId())));
    }
}
