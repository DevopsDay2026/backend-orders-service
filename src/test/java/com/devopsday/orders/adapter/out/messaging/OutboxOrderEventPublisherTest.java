package com.devopsday.orders.adapter.out.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import com.devopsday.orders.adapter.out.persistence.OutboxRepository;
import com.devopsday.orders.domain.event.OrderPlaced;
import com.devopsday.orders.testsupport.InMemoryMessagingProfile;
import com.devopsday.orders.testsupport.OrderFixtures;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(InMemoryMessagingProfile.class)
class OutboxOrderEventPublisherTest {

    @Inject OutboxOrderEventPublisher publisher;
    @Inject OutboxRepository outbox;
    @Inject ObjectMapper json;

    @Test
    @TestTransaction
    void storesPendingRowWithVersionedPayloadKeyedByOrderId() throws Exception {
        var order = OrderFixtures.pendingOrder();
        var event =
                new OrderPlaced(
                        UUID.randomUUID(),
                        OrderFixtures.NOW,
                        order.id(),
                        order.customerId(),
                        order.lines());

        publisher.publish(event);
        outbox.flush();
        outbox.getEntityManager().clear();

        var row = outbox.findById(event.eventId());
        assertThat(row.eventType()).isEqualTo("OrderPlacedV1");
        assertThat(row.topic()).isEqualTo("orders.placed");
        assertThat(row.messageKey()).isEqualTo(order.id().toString());
        assertThat(row.sentAt()).isNull();

        var payload = json.readTree(row.payload());
        assertThat(payload.get("eventId").asText()).isEqualTo(event.eventId().toString());
        assertThat(payload.get("occurredAt").asText()).isEqualTo("2026-01-01T10:00:00Z");
        assertThat(payload.get("orderId").asText()).isEqualTo(order.id().toString());
        assertThat(payload.get("customerId").asText()).isEqualTo("customer-1");
        assertThat(payload.get("lines")).hasSize(2);
        assertThat(payload.get("lines").get(0).get("sku").asText()).isEqualTo("SKU-1");
        assertThat(payload.get("lines").get(0).get("quantity").asInt()).isEqualTo(2);
    }
}
