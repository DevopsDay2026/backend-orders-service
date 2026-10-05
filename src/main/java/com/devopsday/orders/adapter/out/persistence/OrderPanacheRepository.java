package com.devopsday.orders.adapter.out.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class OrderPanacheRepository implements PanacheRepositoryBase<OrderEntity, UUID> {

    public Optional<OrderEntity> findWithLines(UUID id) {
        return find("from OrderEntity o left join fetch o.lines where o.id = ?1", id)
                .singleResultOptional();
    }
}
