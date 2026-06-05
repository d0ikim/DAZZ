package com.dazz.backend.application.port.out;

import java.time.Duration;
import java.util.Optional;

public interface IdempotencyRepository {

    Optional<String> find(String key);

    void save(String key, String value, Duration ttl);
}