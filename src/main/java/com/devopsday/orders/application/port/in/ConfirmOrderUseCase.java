package com.devopsday.orders.application.port.in;

import com.devopsday.orders.domain.model.OrderId;
import java.util.UUID;

public interface ConfirmOrderUseCase {

    void confirm(Command command);

    record Command(UUID eventId, OrderId orderId) {}
}
