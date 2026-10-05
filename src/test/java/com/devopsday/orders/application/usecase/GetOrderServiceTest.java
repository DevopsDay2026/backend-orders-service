package com.devopsday.orders.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devopsday.orders.domain.exception.OrderNotFound;
import com.devopsday.orders.domain.model.OrderId;
import com.devopsday.orders.testsupport.InMemoryOrderRepository;
import com.devopsday.orders.testsupport.OrderFixtures;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GetOrderServiceTest {

    private final InMemoryOrderRepository orders = new InMemoryOrderRepository();
    private final GetOrderService service = new GetOrderService(orders);

    @Test
    void returnsExistingOrder() {
        var order = OrderFixtures.pendingOrder();
        orders.save(order);

        assertThat(service.byId(order.id())).isSameAs(order);
    }

    @Test
    void failsWhenOrderDoesNotExist() {
        var id = new OrderId(UUID.randomUUID());

        assertThatThrownBy(() -> service.byId(id))
                .isInstanceOf(OrderNotFound.class)
                .hasMessageContaining(id.toString())
                .extracting("code")
                .isEqualTo("order-not-found");
    }
}
