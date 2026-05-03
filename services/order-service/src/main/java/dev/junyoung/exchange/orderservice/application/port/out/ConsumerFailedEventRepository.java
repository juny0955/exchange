package dev.junyoung.exchange.orderservice.application.port.out;

import dev.junyoung.exchange.orderservice.domain.model.entity.ConsumerFailedEvent;

public interface ConsumerFailedEventRepository {
    void save(ConsumerFailedEvent consumerFailedEvent);
}
