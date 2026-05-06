package dev.junyoung.exchange.accountservice.adapter.in.event;

import dev.junyoung.exchange.accountservice.adapter.in.event.exception.EventHeaderMissingException;
import dev.junyoung.exchange.accountservice.application.exception.AccountOutboxNotFoundException;
import dev.junyoung.exchange.accountservice.application.port.in.CompleteAccountOutboxUseCase;
import dev.junyoung.exchange.accountservice.domain.exception.AccountInvalidException;
import dev.junyoung.exchange.accountservice.domain.model.value.OutboxId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountOutboxEventConsumer {

    private static final String OUTBOX_ID_HEADER_NAME = "id";

    @Value("${kafka.listeners.account-outbox.group-id}")
    private String groupId;

    private final CompleteAccountOutboxUseCase completeAccountOutboxUseCase;

    @RetryableTopic(
        attempts = "3",
        backOff = @BackOff(delay = 1000, multiplier = 2),
        exclude = {
            AccountOutboxNotFoundException.class,  // 존재하지 않거나 이미 SUCCESS
            EventHeaderMissingException.class,     // 헤더 없으면 재시도 불필요
            AccountInvalidException.class,         // OutboxId 파싱 실패 (UUID 형식 오류)
        },
        dltTopicSuffix = ".dlt"
    )
    @KafkaListener(
        topics = {
            "${kafka.listeners.account-outbox.topics.reserved}",
            "${kafka.listeners.account-outbox.topics.rejected}"
        },
        groupId = "${kafka.listeners.account-outbox.group-id}"
    )
    public void updateOutboxStatus(ConsumerRecord<String, String> record) {
        Header header = record.headers().lastHeader(OUTBOX_ID_HEADER_NAME);
        if (header == null)
            throw new EventHeaderMissingException(groupId, record.topic(), record.partition(), record.offset());

        OutboxId outboxId = OutboxId.from(header.value());  // 실패 시 AccountInvalidException
        Instant publishedAt = Instant.ofEpochMilli(record.timestamp());
        completeAccountOutboxUseCase.complete(outboxId, publishedAt);
    }

    @DltHandler
    public void handleDlt(ConsumerRecord<String, String> record) {
        String outboxIdStr = extractOutboxIdSafely(record);
        log.error("[{}] Outbox 상태 변경 실패 outboxId={} topic={} partition={} offset={}",
            groupId, outboxIdStr, record.topic(), record.partition(), record.offset());
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
