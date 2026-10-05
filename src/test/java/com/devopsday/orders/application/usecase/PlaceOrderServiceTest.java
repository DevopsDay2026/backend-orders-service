package com.devopsday.orders.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devopsday.orders.application.port.in.PlaceOrderUseCase.Command;
import com.devopsday.orders.application.port.in.PlaceOrderUseCase.Command.Line;
import com.devopsday.orders.domain.event.OrderPlaced;
import com.devopsday.orders.domain.exception.InvalidOrder;
import com.devopsday.orders.domain.model.OrderLine;
import com.devopsday.orders.domain.model.OrderStatus;
import com.devopsday.orders.testsupport.InMemoryOrderRepository;
import com.devopsday.orders.testsupport.OrderFixtures;
import com.devopsday.orders.testsupport.RecordingEventPublisher;
import com.devopsday.orders.testsupport.SequentialIdGenerator;
import java.util.List;
import org.junit.jupiter.api.Test;

class PlaceOrderServiceTest {

    private final InMemoryOrderRepository orders = new InMemoryOrderRepository();
    private final RecordingEventPublisher events = new RecordingEventPublisher();
    private final PlaceOrderService service =
            new PlaceOrderService(
                    orders, events, () -> OrderFixtures.NOW, new SequentialIdGenerator());

    @Test
    void persistsPendingOrder() {
        var order = service.place(new Command("customer-1", List.of(new Line("SKU-1", 2))));

        assertThat(orders.findById(order.id()))
                .hasValueSatisfying(
                        saved -> {
                            assertThat(saved.status()).isEqualTo(OrderStatus.PENDING);
                            assertThat(saved.customerId()).isEqualTo("customer-1");
                            assertThat(saved.lines()).containsExactly(new OrderLine("SKU-1", 2));
                            assertThat(saved.createdAt()).isEqualTo(OrderFixtures.NOW);
                        });
    }

    @Test
    void publishesOrderPlacedKeyedByOrderId() {
        var order = service.place(new Command("customer-1", List.of(new Line("SKU-1", 2))));

        assertThat(events.published())
                .singleElement()
                .isInstanceOfSatisfying(
                        OrderPlaced.class,
                        event -> {
                            assertThat(event.orderId()).isEqualTo(order.id());
                            assertThat(event.aggregateId()).isEqualTo(order.id().value());
                            assertThat(event.eventId()).isNotEqualTo(order.id().value());
                            assertThat(event.occurredAt()).isEqualTo(OrderFixtures.NOW);
                            assertThat(event.customerId()).isEqualTo("customer-1");
                            assertThat(event.lines()).containsExactly(new OrderLine("SKU-1", 2));
                        });
    }

    @Test
    void doesNotPersistNorPublishWhenDomainRejectsCommand() {
        assertThatThrownBy(() -> service.place(new Command("customer-1", List.of())))
                .isInstanceOf(InvalidOrder.class);

        assertThat(orders.size()).isZero();
        assertThat(events.published()).isEmpty();
    }

    @Test
    void commandToleratesNullLines() {
        assertThat(new Command("customer-1", null).lines()).isEmpty();
    }
}
