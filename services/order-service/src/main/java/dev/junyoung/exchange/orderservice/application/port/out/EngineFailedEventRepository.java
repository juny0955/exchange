package dev.junyoung.exchange.orderservice.application.port.out;

import dev.junyoung.exchange.orderservice.domain.model.entity.EngineFailedEvent;

public interface EngineFailedEventRepository {
    void save(EngineFailedEvent engineFailedEvent);
}
