package com.devopsday.orders.adapter.out.persistence;

import com.devopsday.orders.domain.model.Order;
import com.devopsday.orders.domain.model.OrderId;
import com.devopsday.orders.domain.model.OrderLine;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class OrderEntityMapper {

    public Order toDomain(OrderEntity entity) {
        var lines =
                entity.lines.stream().map(line -> new OrderLine(line.sku, line.quantity)).toList();
        return Order.restore(
                new OrderId(entity.id),
                entity.customerId,
                lines,
                entity.status,
                entity.rejectionReason,
                entity.createdAt);
    }

    public OrderEntity toNewEntity(Order order) {
        var entity = new OrderEntity();
        entity.id = order.id().value();
        entity.customerId = order.customerId();
        entity.createdAt = order.createdAt();
        applyState(order, entity);
        var lineNo = 0;
        for (var line : order.lines()) {
            var lineEntity = new OrderLineEntity();
            lineEntity.id = UUID.randomUUID();
            lineEntity.order = entity;
            lineEntity.lineNo = lineNo++;
            lineEntity.sku = line.sku();
            lineEntity.quantity = line.quantity();
            entity.lines.add(lineEntity);
        }
        return entity;
    }

    public void applyState(Order order, OrderEntity entity) {
        entity.status = order.status();
        entity.rejectionReason = order.rejectionReason().orElse(null);
    }
}
