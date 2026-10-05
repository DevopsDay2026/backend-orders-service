package com.devopsday.orders.adapter.in.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.devopsday.orders.application.port.in.ConfirmOrderUseCase;
import com.devopsday.orders.application.port.in.RejectOrderUseCase;
import com.devopsday.orders.domain.exception.OrderNotFound;
import com.devopsday.orders.domain.model.OrderId;
import com.devopsday.orders.testsupport.InMemoryMessagingProfile;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(InMemoryMessagingProfile.class)
class InventoryConsumersTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    @Inject @Any InMemoryConnector connector;
    @InjectMock ConfirmOrderUseCase confirmOrder;
    @InjectMock RejectOrderUseCase rejectOrder;

    @Test
    void inventoryReservedConfirmsTheOrder() {
        var eventId = UUID.randomUUID();
        var orderId = UUID.randomUUID();

        connector
                .source("inventory-reserved")
                .send(
                        """
            {"eventId":"%s","occurredAt":"2026-01-01T10:00:00Z","orderId":"%s"}
            """
                                .formatted(eventId, orderId));

        await().atMost(TIMEOUT)
                .untilAsserted(
                        () ->
                                verify(confirmOrder)
                                        .confirm(
                                                new ConfirmOrderUseCase.Command(
                                                        eventId, new OrderId(orderId))));
    }

    @Test
    void inventoryRejectedRejectsTheOrderWithReason() {
        var eventId = UUID.randomUUID();
        var orderId = UUID.randomUUID();

        connector
                .source("inventory-rejected")
                .send(
                        """
            {"eventId":"%s","occurredAt":"2026-01-01T10:00:00Z","orderId":"%s","reason":"insufficient stock for SKU-1"}
            """
                                .formatted(eventId, orderId));

        await().atMost(TIMEOUT)
                .untilAsserted(
                        () ->
                                verify(rejectOrder)
                                        .reject(
                                                new RejectOrderUseCase.Command(
                                                        eventId,
                                                        new OrderId(orderId),
                                                        "insufficient stock for SKU-1")));
    }

    @Test
    void unknownFieldsAreIgnoredForForwardCompatibility() {
        var eventId = UUID.randomUUID();
        var orderId = UUID.randomUUID();

        connector
                .source("inventory-reserved")
                .send(
                        """
            {"eventId":"%s","occurredAt":"2026-01-01T10:00:00Z","orderId":"%s","addedLater":true}
            """
                                .formatted(eventId, orderId));

        await().atMost(TIMEOUT)
                .untilAsserted(
                        () ->
                                verify(confirmOrder)
                                        .confirm(
                                                new ConfirmOrderUseCase.Command(
                                                        eventId, new OrderId(orderId))));
    }

    @Test
    void malformedPayloadIsNackedSoItGoesToTheDeadLetterQueue() {
        var failure = sendAndCaptureNack("inventory-reserved", "{not-json");

        await().atMost(TIMEOUT).untilAsserted(() -> assertThat(failure.get()).isNotNull());
        assertThat(failure.get()).isInstanceOf(MalformedEventException.class);
        verifyNoInteractions(confirmOrder);
    }

    @Test
    void payloadWithoutOrderIdIsNacked() {
        var failure =
                sendAndCaptureNack(
                        "inventory-rejected", "{\"eventId\":\"" + UUID.randomUUID() + "\"}");

        await().atMost(TIMEOUT).untilAsserted(() -> assertThat(failure.get()).isNotNull());
        assertThat(failure.get()).isInstanceOf(MalformedEventException.class);
        verifyNoInteractions(rejectOrder);
    }

    @Test
    void useCaseFailureIsNackedSoItGoesToTheDeadLetterQueue() {
        var orderId = new OrderId(UUID.randomUUID());
        doThrow(new OrderNotFound(orderId)).when(confirmOrder).confirm(any());

        var failure =
                sendAndCaptureNack(
                        "inventory-reserved",
                        "{\"eventId\":\"%s\",\"orderId\":\"%s\"}"
                                .formatted(UUID.randomUUID(), orderId));

        await().atMost(TIMEOUT).untilAsserted(() -> assertThat(failure.get()).isNotNull());
        assertThat(failure.get()).isInstanceOf(OrderNotFound.class);
    }

    private AtomicReference<Throwable> sendAndCaptureNack(String channel, String payload) {
        var failure = new AtomicReference<Throwable>();
        connector
                .<Message<String>>source(channel)
                .send(
                        Message.of(
                                payload,
                                () -> CompletableFuture.completedFuture(null),
                                reason -> {
                                    failure.set(reason);
                                    return CompletableFuture.completedFuture(null);
                                }));
        return failure;
    }
}
