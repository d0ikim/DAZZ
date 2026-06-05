package com.dazz.backend.infrastructure.cache;

import com.dazz.backend.application.port.out.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis(Redisson RBucket)를 사용한 멱등성 키-값 저장소.
 * Key 접두어: "idempotency:{key}"
 */
@Component
@RequiredArgsConstructor
public class IdempotencyRepositoryImpl implements IdempotencyRepository {

    private static final String KEY_PREFIX = "idempotency:";

    private final RedissonClient redissonClient;

    @Override
    public Optional<String> find(String key) {
        RBucket<String> bucket = redissonClient.getBucket(KEY_PREFIX + key);
        return Optional.ofNullable(bucket.get());
    }

    @Override
    public void save(String key, String value, Duration ttl) {
        RBucket<String> bucket = redissonClient.getBucket(KEY_PREFIX + key);
        bucket.set(value, ttl);
    }
}