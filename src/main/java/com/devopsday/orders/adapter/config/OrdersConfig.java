package com.devopsday.orders.adapter.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "orders")
public interface OrdersConfig {

    Topics topics();

    Outbox outbox();

    interface Topics {

        String orderPlaced();
    }

    interface Outbox {

        @WithDefault("1s")
        String pollInterval();

        @WithDefault("100")
        int batchSize();
    }
}
