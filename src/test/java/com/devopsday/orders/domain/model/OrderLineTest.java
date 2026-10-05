package com.devopsday.orders.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devopsday.orders.domain.exception.InvalidOrder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class OrderLineTest {

    @Test
    void keepsSkuAndQuantity() {
        var line = new OrderLine("SKU-1", 3);

        assertThat(line.sku()).isEqualTo("SKU-1");
        assertThat(line.quantity()).isEqualTo(3);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void rejectsNonPositiveQuantity(int quantity) {
        assertThatThrownBy(() -> new OrderLine("SKU-1", quantity))
                .isInstanceOf(InvalidOrder.class)
                .hasMessageContaining("quantity");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void rejectsBlankSku(String sku) {
        assertThatThrownBy(() -> new OrderLine(sku, 1))
                .isInstanceOf(InvalidOrder.class)
                .hasMessageContaining("sku");
    }

    @Test
    void orderIdRequiresValue() {
        assertThatThrownBy(() -> new OrderId(null)).isInstanceOf(NullPointerException.class);
    }
}
