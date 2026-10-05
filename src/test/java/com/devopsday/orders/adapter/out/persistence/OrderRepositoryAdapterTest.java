package com.devopsday.orders.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.devopsday.orders.domain.model.OrderId;
import com.devopsday.orders.domain.model.OrderLine;
import com.devopsday.orders.domain.model.OrderStatus;
import com.devopsday.orders.testsupport.InMemoryMessagingProfile;
import com.devopsday.orders.testsupport.OrderFixtures;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(InMemoryMessagingProfile.class)
class OrderRepositoryAdapterTest {

    @Inject OrderRepositoryAdapter adapter;
    @Inject OrderPanacheRepository panache;

    @Test
    @TestTransaction
    void savesAndReloadsOrderWithLinesInOrder() {
        var order = OrderFixtures.pendingOrder();

        adapter.save(order);
        flushAndClear();

        assertThat(adapter.findById(order.id()))
                .hasValueSatisfying(
                        loaded -> {
                            assertThat(loaded.customerId()).isEqualTo("customer-1");
                            assertThat(loaded.status()).isEqualTo(OrderStatus.PENDING);
                            assertThat(loaded.createdAt()).isEqualTo(OrderFixtures.NOW);
                            assertThat(loaded.rejectionReason()).isEmpty();
                            assertThat(loaded.lines())
                                    .containsExactly(
                                            new OrderLine("SKU-1", 2), new OrderLine("SKU-2", 1));
                        });
    }

    @Test
    @TestTransaction
    void updatesStatusOfExistingOrderAndBumpsVersion() {
        var order = OrderFixtures.pendingOrder();
        adapter.save(order);
        flushAndClear();
        var initialVersion = panache.findById(order.id().value()).version;

        adapter.save(order.reject("insufficient stock for SKU-1"));
        flushAndClear();

        assertThat(adapter.findById(order.id()))
                .hasValueSatisfying(
                        loaded -> {
                            assertThat(loaded.status()).isEqualTo(OrderStatus.REJECTED);
                            assertThat(loaded.rejectionReason())
                                    .contains("insufficient stock for SKU-1");
                            assertThat(loaded.lines()).hasSize(2);
                        });
        assertThat(panache.findById(order.id().value()).version).isGreaterThan(initialVersion);
    }

    @Test
    @TestTransaction
    void returnsEmptyForUnknownOrder() {
        assertThat(adapter.findById(new OrderId(UUID.randomUUID()))).isEmpty();
    }

    private void flushAndClear() {
        panache.flush();
        panache.getEntityManager().clear();
    }
}
