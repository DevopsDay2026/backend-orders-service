package com.devopsday.orders.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devopsday.orders.application.port.in.RejectOrderUseCase.Command;
import com.devopsday.orders.domain.exception.OrderNotFound;
import com.devopsday.orders.domain.model.OrderId;
import com.devopsday.orders.domain.model.OrderStatus;
import com.devopsday.orders.testsupport.InMemoryOrderRepository;
import com.devopsday.orders.testsupport.InMemoryProcessedEventStore;
import com.devopsday.orders.testsupport.OrderFixtures;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RejectOrderServiceTest {

    private final InMemoryOrderRepository orders = new InMemoryOrderRepository();
    private final RejectOrderService service =
            new RejectOrderService(orders, new InMemoryProcessedEventStore());

    @Test
    void rejectsPendingOrderWithReason() {
        var order = OrderFixtures.pendingOrder();
        orders.save(order);

        service.reject(new Command(UUID.randomUUID(), order.id(), "insufficient stock"));

        assertThat(orders.findById(order.id()))
                .hasValueSatisfying(
                        saved -> {
                            assertThat(saved.status()).isEqualTo(OrderStatus.REJECTED);
                            assertThat(saved.rejectionReason()).contains("insufficient stock");
                        });
    }

    @Test
    void ignoresRedeliveredEvent() {
        var order = OrderFixtures.pendingOrder();
        orders.save(order);
        var eventId = UUID.randomUUID();

        service.reject(new Command(eventId, order.id(), "first"));
        service.reject(new Command(eventId, order.id(), "second"));

        assertThat(orders.findById(order.id()))
                .hasValueSatisfying(saved -> assertThat(saved.rejectionReason()).contains("first"));
    }

    @Test
    void failsWhenOrderDoesNotExist() {
        var command = new Command(UUID.randomUUID(), new OrderId(UUID.randomUUID()), "no stock");

        assertThatThrownBy(() -> service.reject(command)).isInstanceOf(OrderNotFound.class);
    }
}
