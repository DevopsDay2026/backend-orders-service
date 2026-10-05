package com.devopsday.orders.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devopsday.orders.domain.exception.InvalidOrder;
import com.devopsday.orders.domain.exception.InvalidOrderState;
import com.devopsday.orders.testsupport.OrderFixtures;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class OrderTest {

    private static final OrderId ID = new OrderId(UUID.randomUUID());
    private static final List<OrderLine> LINES = List.of(new OrderLine("SKU-1", 2));

    @Test
    void placeCreatesPendingOrder() {
        var order = Order.place(ID, "customer-1", LINES, OrderFixtures.NOW);

        assertThat(order.id()).isEqualTo(ID);
        assertThat(order.customerId()).isEqualTo("customer-1");
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.lines()).containsExactly(new OrderLine("SKU-1", 2));
        assertThat(order.createdAt()).isEqualTo(OrderFixtures.NOW);
        assertThat(order.rejectionReason()).isEmpty();
    }

    @Test
    void rejectsOrderWithoutLines() {
        assertThatThrownBy(() -> Order.place(ID, "customer-1", List.of(), OrderFixtures.NOW))
                .isInstanceOf(InvalidOrder.class)
                .hasMessageContaining("at least one line");
    }

    @Test
    void rejectsOrderWithNullLines() {
        assertThatThrownBy(() -> Order.place(ID, "customer-1", null, OrderFixtures.NOW))
                .isInstanceOf(InvalidOrder.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "  ")
    void rejectsBlankCustomer(String customerId) {
        assertThatThrownBy(() -> Order.place(ID, customerId, LINES, OrderFixtures.NOW))
                .isInstanceOf(InvalidOrder.class)
                .hasMessageContaining("customerId");
    }

    @Test
    void linesAreDefensivelyCopied() {
        var mutable = new ArrayList<>(LINES);

        var order = Order.place(ID, "customer-1", mutable, OrderFixtures.NOW);
        mutable.clear();

        assertThat(order.lines()).hasSize(1);
        assertThatThrownBy(() -> order.lines().add(new OrderLine("SKU-9", 1)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void confirmMovesPendingToConfirmed() {
        var confirmed = OrderFixtures.pendingOrder().confirm();

        assertThat(confirmed.status()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(confirmed.rejectionReason()).isEmpty();
    }

    @Test
    void rejectMovesPendingToRejectedWithReason() {
        var rejected = OrderFixtures.pendingOrder().reject("insufficient stock for SKU-1");

        assertThat(rejected.status()).isEqualTo(OrderStatus.REJECTED);
        assertThat(rejected.rejectionReason()).contains("insufficient stock for SKU-1");
    }

    @Test
    void cannotConfirmTwice() {
        var confirmed = OrderFixtures.pendingOrder().confirm();

        assertThatThrownBy(confirmed::confirm)
                .isInstanceOf(InvalidOrderState.class)
                .hasMessageContaining("CONFIRMED");
    }

    @Test
    void cannotRejectConfirmedOrder() {
        var confirmed = OrderFixtures.pendingOrder().confirm();

        assertThatThrownBy(() -> confirmed.reject("late")).isInstanceOf(InvalidOrderState.class);
    }

    @Test
    void cannotConfirmRejectedOrder() {
        var rejected = OrderFixtures.pendingOrder().reject("no stock");

        assertThatThrownBy(rejected::confirm).isInstanceOf(InvalidOrderState.class);
    }

    @Test
    void restoreKeepsPersistedState() {
        var restored =
                Order.restore(
                        ID,
                        "customer-1",
                        LINES,
                        OrderStatus.REJECTED,
                        "no stock",
                        OrderFixtures.NOW);

        assertThat(restored.status()).isEqualTo(OrderStatus.REJECTED);
        assertThat(restored.rejectionReason()).contains("no stock");
    }
}
