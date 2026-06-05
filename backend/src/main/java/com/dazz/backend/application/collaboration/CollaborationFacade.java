package com.dazz.backend.application.collaboration;

import com.dazz.backend.application.collaboration.command.CollaborationLinkCommand;
import com.dazz.backend.application.port.out.IdempotencyRepository;
import com.dazz.backend.domain.musician.exception.CollaborationConcurrentException;
import com.dazz.backend.domain.musician.exception.IdempotencyConflictException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 협업 가중치 업데이트의 동시성 + 멱등성 보호 계층.
 *
 * 실행 순서:
 * 1. Idempotency-Key 캐시 확인 (Redis HIT → 즉시 반환)
 * 2. Redisson 분산락 획득 (lock > transaction 범위 보장)
 * 3. @Transactional 서비스 호출 → 커밋
 * 4. 락 해제
 * 5. 결과를 Redis에 캐시 (TTL 24h)
 */
@Component
@RequiredArgsConstructor
public class CollaborationFacade {

    private static final long LOCK_WAIT_SECONDS = 2;
    private static final long LOCK_LEASE_SECONDS = 5;
    private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);

    private final RedissonClient redissonClient;
    private final CollaborationCommandService commandService;
    private final IdempotencyRepository idempotencyRepository;
    private final ObjectMapper objectMapper;

    public CollaborationResult linkOrIncrement(String idempotencyKey, CollaborationLinkCommand command) {
        String payloadHash = buildPayloadHash(command);

        Optional<String> cached = idempotencyRepository.find(idempotencyKey);
        if (cached.isPresent()) {
            return deserializeCached(idempotencyKey, payloadHash, cached.get());
        }

        String lockKey = buildLockKey(command.fromMusicianId(), command.toMusicianId());
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS)) {
                throw new CollaborationConcurrentException(command.fromMusicianId(), command.toMusicianId());
            }
            CollaborationResult result = commandService.linkOrIncrement(command);
            cacheResult(idempotencyKey, payloadHash, result);
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CollaborationConcurrentException(command.fromMusicianId(), command.toMusicianId());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private CollaborationResult deserializeCached(String key, String currentPayloadHash, String json) {
        try {
            CachedEntry entry = objectMapper.readValue(json, CachedEntry.class);
            if (!entry.payloadHash().equals(currentPayloadHash)) {
                throw new IdempotencyConflictException(key);
            }
            return entry.result();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("캐시 역직렬화 실패: " + key, e);
        }
    }

    private void cacheResult(String key, String payloadHash, CollaborationResult result) {
        try {
            String json = objectMapper.writeValueAsString(new CachedEntry(payloadHash, result));
            idempotencyRepository.save(key, json, IDEMPOTENCY_TTL);
        } catch (JsonProcessingException e) {
            // 캐시 실패는 비즈니스 로직에 영향 없음 — 로깅만
            throw new IllegalStateException("캐시 직렬화 실패: " + key, e);
        }
    }

    private String buildPayloadHash(CollaborationLinkCommand command) {
        return command.fromMusicianId() + ":" + command.toMusicianId() + ":" + command.relationType();
    }

    private String buildLockKey(Long aId, Long bId) {
        long min = Math.min(aId, bId);
        long max = Math.max(aId, bId);
        return String.format("collab:lock:%d:%d", min, max);
    }

    private record CachedEntry(String payloadHash, CollaborationResult result) {}
}