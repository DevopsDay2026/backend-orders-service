package com.devopsday.orders.adapter.in.messaging;

import com.devopsday.orders.application.port.in.RejectOrderUseCase;
import com.devopsday.orders.domain.model.OrderId;
import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.PersistenceException;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class InventoryRejectedConsumer {

    private final RejectOrderUseCase rejectOrder;
    private final EventJsonReader reader;

    InventoryRejectedConsumer(RejectOrderUseCase rejectOrder, EventJsonReader reader) {
        this.rejectOrder = rejectOrder;
        this.reader = reader;
    }

    @Incoming("inventory-rejected")
    @Blocking
    @Retry(maxRetries = 3, delay = 200, retryOn = PersistenceException.class)
    public void on(String payload) {
        var event = reader.read(payload, InventoryRejectedV1.class);
        rejectOrder.reject(
                new RejectOrderUseCase.Command(
                        event.eventId(), new OrderId(event.orderId()), event.reason()));
    }
}
