package com.devopsday.orders.application.port.in;

import com.devopsday.orders.domain.model.Order;
import java.util.List;

public interface PlaceOrderUseCase {

    Order place(Command command);

    record Command(String customerId, List<Line> lines) {

        public Command {
            lines = lines == null ? List.of() : List.copyOf(lines);
        }

        public record Line(String sku, int quantity) {}
    }
}
