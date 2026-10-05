package com.devopsday.orders.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "order_line")
public class OrderLineEntity {

    @Id UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    OrderEntity order;

    @Column(name = "line_no", nullable = false)
    int lineNo;

    @Column(nullable = false)
    String sku;

    @Column(nullable = false)
    int quantity;
}
