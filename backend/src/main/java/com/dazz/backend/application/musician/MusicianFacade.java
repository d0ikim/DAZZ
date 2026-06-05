package com.dazz.backend.application.musician;

import com.dazz.backend.domain.musician.Musician;
import com.dazz.backend.domain.musician.exception.MusicianConcurrentClaimException;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * claim() 의 동시성 보호: 락(외부) > 트랜잭션(내부) 순서를 보장한다.
 * MusicianCommandService 는 @Transactional 을 유지하고,
 * 이 Facade 가 락을 잡은 채로 서비스를 호출한다.
 */
@Component
@RequiredArgsConstructor
public class MusicianFacade {

    private static final long LOCK_WAIT_SECONDS = 2;
    private static final long LOCK_LEASE_SECONDS = 5;

    private final RedissonClient redissonClient;
    private final MusicianCommandService commandService;

    public Musician claim(UUID uuid, Long userId) {
        RLock lock = redissonClient.getLock("musician:claim:" + uuid);
        try {
            if (!lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS)) {
                throw new MusicianConcurrentClaimException(uuid);
            }
            return commandService.claim(uuid, userId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MusicianConcurrentClaimException(uuid);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}