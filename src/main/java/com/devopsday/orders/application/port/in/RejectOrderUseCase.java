package com.devopsday.orders.application.port.in;

import com.devopsday.orders.domain.model.OrderId;
import java.util.UUID;

public interface RejectOrderUseCase {

    void reject(Command command);

    record Command(UUID eventId, OrderId orderId, String reason) {}
}
