package com.devopsday.orders.application.port.out;

import java.time.Instant;

public interface Clock {

    Instant now();
}
