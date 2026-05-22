package com.dazz.backend.domain.shared;

import java.time.LocalDateTime;

/**
 * 도메인 이벤트 마커. 향후 Kafka 발행 대상이 되는 이벤트는 이 클래스를 상속한다.
 */
public abstract class DomainEvent {

    private final LocalDateTime occurredAt;

    protected DomainEvent() {
        this.occurredAt = LocalDateTime.now();
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
