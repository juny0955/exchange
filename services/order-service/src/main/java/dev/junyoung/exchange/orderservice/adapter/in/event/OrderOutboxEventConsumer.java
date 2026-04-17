package dev.junyoung.exchange.orderservice.adapter.in.event;

import dev.junyoung.exchange.orderservice.application.port.in.OrderOutboxPublishedUseCase;
import dev.junyoung.exchange.orderservice.domain.model.value.OutboxId;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderOutboxEventConsumer {

    private final OrderOutboxPublishedUseCase orderOutboxPublishedUseCase;

    @KafkaListener(
        topics = {"order.PLACE_ORDER", "order.CANCEL_ORDER"},
        groupId = "order-outbox-status-updater"
    )
    public void orderOutboxStatusUpdater(ConsumerRecord<String, String> record) {
        Header header = record.headers().lastHeader("id");
        if (header == null) return;

        OutboxId outboxId = OutboxId.from(header.value());
        orderOutboxPublishedUseCase.orderOutboxPublished(outboxId); // TODO 실패시 어떻게 조치?
    }
}
