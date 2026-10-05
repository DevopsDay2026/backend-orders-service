package com.devopsday.orders.application.port.in;

import com.devopsday.orders.domain.model.Order;
import com.devopsday.orders.domain.model.OrderId;

public interface GetOrderQuery {

    Order byId(OrderId id);
}
