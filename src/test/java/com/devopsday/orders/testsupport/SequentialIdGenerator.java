package com.devopsday.orders.testsupport;

import com.devopsday.orders.application.port.out.IdGenerator;
import java.util.UUID;

public final class SequentialIdGenerator implements IdGenerator {

    private long next = 1;

    @Override
    public UUID newId() {
        return new UUID(0, next++);
    }
}
