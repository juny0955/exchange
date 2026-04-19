package dev.junyoung.exchange.orderservice.adapter.in.event;

import java.nio.charset.StandardCharsets;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

import dev.junyoung.exchange.orderservice.adapter.in.event.exception.EventHeaderMissingException;
import dev.junyoung.exchange.orderservice.application.exception.OrderOutboxNotFoundException;
import dev.junyoung.exchange.orderservice.application.port.in.OrderOutboxPublishedUseCase;
import dev.junyoung.exchange.orderservice.domain.model.value.OutboxId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderOutboxEventConsumer {

    /**
     * TODO props 분리 예정
     */
    private static final String OUTBOX_ID_HEADER_NAME = "id";
    private static final String GROUP_ID = "order-outbox-status-updater";
    private static final String PLACE_ORDER_TOPIC = "order.PLACE_ORDER";
    private static final String CANCEL_ORDER_TOPIC = "order.CANCEL_ORDER";

    private final OrderOutboxPublishedUseCase orderOutboxPublishedUseCase;

    /**
     * 상태 변경 실패시 재시도 수행 후 {@link #handleDlt(ConsumerRecord)}에서 로깅만 수행 (별도 처리 X)
     * 추후 엔진에서 후속 이벤트 수신 시 상태 변경 시도하는 스케줄러를 추가하여 해결
     */
    @RetryableTopic(
        attempts = "3",
        backOff = @BackOff(delay = 1000, multiplier = 2),
        exclude = {
            OrderOutboxNotFoundException.class, // outbox 테이블 커밋 wal읽어서 처리하기때문에 발생가능성 거의없음
            IllegalArgumentException.class,     // 메시지 포멧 파싱 오류로 재시도 무의미
        },
        dltTopicSuffix = ".dlt"
    )
    @KafkaListener(
        topics = {PLACE_ORDER_TOPIC, CANCEL_ORDER_TOPIC},
        groupId = GROUP_ID
    )
    public void orderOutboxStatusUpdater(ConsumerRecord<String, String> record) {
        Header header = record.headers().lastHeader(OUTBOX_ID_HEADER_NAME);
        if (header == null)
            throw new EventHeaderMissingException(GROUP_ID, record.topic(), record.partition(), record.offset());

        OutboxId outboxId = OutboxId.from(header.value());
        orderOutboxPublishedUseCase.orderOutboxPublished(outboxId);
    }

    @DltHandler
    public void handleDlt(ConsumerRecord<String, String> record) {
        String outboxIdStr = extractOutboxIdSafely(record);
        log.error("[{}] Outbox 상태 변경 실패 outboxId={} topic={} partition={} offset={}", GROUP_ID, outboxIdStr, record.topic(), record.partition(), record.offset());
    }

    private String extractOutboxIdSafely(ConsumerRecord<String, String> record) {
        Header header = record.headers().lastHeader(OUTBOX_ID_HEADER_NAME);
        if (header == null) return "MISSING_HEADER";
        try {
            return OutboxId.from(header.value()).value().toString();
        } catch (Exception e) {
            return "INVALID:" + new String(header.value(), StandardCharsets.UTF_8);
        }
    }
}
