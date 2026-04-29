package dev.junyoung.exchange.accountservice.adapter.in.event;

import dev.junyoung.exchange.accountservice.adapter.in.event.annotation.EngineRetryableTopic;
import dev.junyoung.exchange.accountservice.adapter.in.event.message.EngineCanceledMessage;
import dev.junyoung.exchange.accountservice.adapter.in.event.message.EngineRejectedMessage;
import dev.junyoung.exchange.accountservice.application.port.in.ReleaseBalanceUseCase;
import dev.junyoung.exchange.accountservice.application.port.in.command.ReleaseBalanceCommand;
import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import dev.junyoung.exchange.accountservice.domain.model.value.OrderId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class EngineEventConsumer {

    private final ReleaseBalanceUseCase releaseBalanceUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = "${kafka.listeners.engine.topics.canceled}",
        groupId = "${kafka.listeners.engine.group-id}"
    )
    @EngineRetryableTopic
    public void consumeCanceled(ConsumerRecord<String, String> record) {
        EngineCanceledMessage message = objectMapper.readValue(record.value(), EngineCanceledMessage.class);
        releaseBalanceUseCase.release(new ReleaseBalanceCommand(new AccountId(message.accountId()), new OrderId(message.orderId())));
    }

    @KafkaListener(
        topics = "${kafka.listeners.engine.topics.rejected}",
        groupId = "${kafka.listeners.engine.group-id}"
    )
    @EngineRetryableTopic
    public void consumeRejected(ConsumerRecord<String, String> record) {
        EngineRejectedMessage message = objectMapper.readValue(record.value(), EngineRejectedMessage.class);
        releaseBalanceUseCase.release(new ReleaseBalanceCommand(new AccountId(message.accountId()), new OrderId(message.orderId())));
    }

    @KafkaListener(
        topics = "${kafka.listeners.engine.topics.matched}",
        groupId = "${kafka.listeners.engine.group-id}"
    )
    @EngineRetryableTopic
    public void consumeMatched(ConsumerRecord<String, String> record) {
        // TODO: 구현 예정
        log.info("MATCHED 이벤트 수신 (미구현): topic={}, partition={}, offset={}", record.value(), record.partition(), record.offset());
    }

    @DltHandler
    public void handleDlt(ConsumerRecord<String, String> record, @Header(KafkaHeaders.EXCEPTION_MESSAGE) String errorMessage) {
        log.error("엔진 이벤트 최종 처리 실패 topic={}, partition={}, offset={}, error={}, payload={}",
            record.topic(), record.partition(), record.offset(), errorMessage, record.value());
    }
}
