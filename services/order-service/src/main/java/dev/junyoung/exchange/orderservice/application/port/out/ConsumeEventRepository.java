package dev.junyoung.exchange.orderservice.application.port.out;

import dev.junyoung.exchange.orderservice.domain.model.entity.ConsumeFailedEvent;

public interface ConsumeEventRepository {
    void save(ConsumeFailedEvent consumeFailedEvent);
}
