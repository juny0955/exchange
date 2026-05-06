package dev.junyoung.exchange.accountservice.application.port.out;

import dev.junyoung.exchange.accountservice.domain.model.entity.ConsumerFailedEvent;

public interface ConsumerFailedEventRepository {
    void save(ConsumerFailedEvent consumerFailedEvent);
}
