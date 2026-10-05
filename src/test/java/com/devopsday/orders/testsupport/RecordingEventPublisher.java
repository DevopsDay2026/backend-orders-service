package com.devopsday.orders.testsupport;

import com.devopsday.orders.application.port.out.OrderEventPublisher;
import com.devopsday.orders.domain.event.DomainEvent;
import java.util.ArrayList;
import java.util.List;

public final class RecordingEventPublisher implements OrderEventPublisher {

    private final List<DomainEvent> published = new ArrayList<>();

    @Override
    public void publish(DomainEvent event) {
        published.add(event);
    }

    public List<DomainEvent> published() {
        return List.copyOf(published);
    }
}
