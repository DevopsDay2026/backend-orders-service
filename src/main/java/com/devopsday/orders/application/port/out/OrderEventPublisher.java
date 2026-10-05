package com.devopsday.orders.application.port.out;

import com.devopsday.orders.domain.event.DomainEvent;

public interface OrderEventPublisher {

    void publish(DomainEvent event);
}
