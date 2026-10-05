package com.devopsday.orders.adapter.out.persistence;

import com.devopsday.orders.application.port.out.OrderRepository;
import com.devopsday.orders.domain.model.Order;
import com.devopsday.orders.domain.model.OrderId;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderPanacheRepository repository;
    private final OrderEntityMapper mapper;

    OrderRepositoryAdapter(OrderPanacheRepository repository, OrderEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return repository.findWithLines(id.value()).map(mapper::toDomain);
    }

    @Override
    public void save(Order order) {
        repository
                .findByIdOptional(order.id().value())
                .ifPresentOrElse(
                        entity -> mapper.applyState(order, entity),
                        () -> repository.persist(mapper.toNewEntity(order)));
    }
}
