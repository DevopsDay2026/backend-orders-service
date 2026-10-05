package com.devopsday.orders.adapter.out.messaging;

import com.devopsday.orders.adapter.config.OrdersConfig;
import com.devopsday.orders.adapter.out.persistence.OutboxEntity;
import com.devopsday.orders.adapter.out.persistence.OutboxRepository;
import com.devopsday.orders.application.port.out.OrderEventPublisher;
import com.devopsday.orders.domain.event.DomainEvent;
import com.devopsday.orders.domain.event.OrderPlaced;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OutboxOrderEventPublisher implements OrderEventPublisher {

    private final OutboxRepository outbox;
    private final ObjectMapper json;
    private final OrdersConfig config;

    OutboxOrderEventPublisher(OutboxRepository outbox, ObjectMapper json, OrdersConfig config) {
        this.outbox = outbox;
        this.json = json;
        this.config = config;
    }

    @Override
    public void publish(DomainEvent event) {
        var row =
                switch (event) {
                    case OrderPlaced placed ->
                            OutboxEntity.pending(
                                    placed.eventId(),
                                    placed.aggregateId(),
                                    OrderPlacedV1.TYPE,
                                    config.topics().orderPlaced(),
                                    toJson(OrderPlacedV1.from(placed)),
                                    placed.occurredAt());
                };
        outbox.persist(row);
    }

    private String toJson(Object payload) {
        try {
            return json.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "cannot serialize " + payload.getClass().getSimpleName(), e);
        }
    }
}
