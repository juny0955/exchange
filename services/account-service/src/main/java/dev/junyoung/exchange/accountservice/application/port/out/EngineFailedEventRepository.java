package dev.junyoung.exchange.accountservice.application.port.out;

import dev.junyoung.exchange.accountservice.domain.model.entity.EngineFailedEvent;

public interface EngineFailedEventRepository {
    void save(EngineFailedEvent engineFailedEvent);
}
