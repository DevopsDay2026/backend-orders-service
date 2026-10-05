package com.devopsday.orders.application.port.out;

import java.util.UUID;

public interface IdGenerator {

    UUID newId();
}
