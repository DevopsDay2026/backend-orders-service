package com.devopsday.orders.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devopsday.orders.application.port.in.ConfirmOrderUseCase.Command;
import com.devopsday.orders.domain.exception.InvalidOrderState;
import com.devopsday.orders.domain.exception.OrderNotFound;
import com.devopsday.orders.domain.model.OrderId;
import com.devopsday.orders.domain.model.OrderStatus;
import com.devopsday.orders.testsupport.InMemoryOrderRepository;
import com.devopsday.orders.testsupport.InMemoryProcessedEventStore;
import com.devopsday.orders.testsupport.OrderFixtures;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ConfirmOrderServiceTest {

    private final InMemoryOrderRepository orders = new InMemoryOrderRepository();
    private final ConfirmOrderService service =
            new ConfirmOrderService(orders, new InMemoryProcessedEventStore());

    @Test
    void confirmsPendingOrder() {
        var order = OrderFixtures.pendingOrder();
        orders.save(order);

        service.confirm(new Command(UUID.randomUUID(), order.id()));

        assertThat(orders.findById(order.id()))
                .hasValueSatisfying(
                        saved -> assertThat(saved.status()).isEqualTo(OrderStatus.CONFIRMED));
    }

    @Test
    void ignoresRedeliveredEvent() {
        var order = OrderFixtures.pendingOrder();
        orders.save(order);
        var command = new Command(UUID.randomUUID(), order.id());

        service.confirm(command);
        service.confirm(command);

        assertThat(orders.findById(order.id()))
                .hasValueSatisfying(
                        saved -> assertThat(saved.status()).isEqualTo(OrderStatus.CONFIRMED));
    }

    @Test
    void failsOnDifferentEventForAlreadyConfirmedOrder() {
        var order = OrderFixtures.pendingOrder();
        orders.save(order);
        service.confirm(new Command(UUID.randomUUID(), order.id()));

        assertThatThrownBy(() -> service.confirm(new Command(UUID.randomUUID(), order.id())))
                .isInstanceOf(InvalidOrderState.class);
    }

    @Test
    void failsWhenOrderDoesNotExist() {
        var command = new Command(UUID.randomUUID(), new OrderId(UUID.randomUUID()));

        assertThatThrownBy(() -> service.confirm(command)).isInstanceOf(OrderNotFound.class);
    }
}
