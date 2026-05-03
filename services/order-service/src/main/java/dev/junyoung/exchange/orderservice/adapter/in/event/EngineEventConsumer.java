package dev.junyoung.exchange.orderservice.adapter.in.event;

import dev.junyoung.exchange.orderservice.adapter.in.event.annotation.DefaultRetryableTopic;
import dev.junyoung.exchange.orderservice.adapter.in.event.message.EngineAcceptedMessage;
import dev.junyoung.exchange.orderservice.adapter.in.event.message.EngineCanceledMessage;
import dev.junyoung.exchange.orderservice.adapter.in.event.message.EngineMatchedMessage;
import dev.junyoung.exchange.orderservice.adapter.in.event.message.EngineRejectedMessage;
import dev.junyoung.exchange.orderservice.application.port.in.SaveConsumeFailedEventUseCase;
import dev.junyoung.exchange.orderservice.application.port.in.command.EngineMatchedCommand;
import dev.junyoung.exchange.orderservice.application.port.in.command.SaveConsumeFailedEventCommand;
import dev.junyoung.exchange.orderservice.application.port.in.engine.HandleEngineAcceptedEventUseCase;
import dev.junyoung.exchange.orderservice.application.port.in.engine.HandleEngineCanceledEventUseCase;
import dev.junyoung.exchange.orderservice.application.port.in.engine.HandleEngineMatchedEventUseCase;
import dev.junyoung.exchange.orderservice.application.port.in.engine.HandleEngineRejectedEventUseCase;
import dev.junyoung.exchange.orderservice.domain.model.value.AccountId;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EngineEventConsumer {

    private final HandleEngineAcceptedEventUseCase acceptedEventUseCase;
    private final HandleEngineMatchedEventUseCase matchedEventUseCase;
    private final HandleEngineRejectedEventUseCase rejectedEventUseCase;
    private final HandleEngineCanceledEventUseCase canceledEventUseCase;
    private final ObjectMapper objectMapper;

    private final SaveConsumeFailedEventUseCase saveConsumeFailedEventUseCase;

    @DefaultRetryableTopic
    @KafkaListener(
        topics = "${kafka.listeners.engine.topics.accepted}",
        groupId = "${kafka.listeners.engine.group-id}"
    )
    public void consumeAccepted(ConsumerRecord<String, String> record) {
        EngineAcceptedMessage message = objectMapper.readValue(record.value(), EngineAcceptedMessage.class);
        acceptedEventUseCase.handle(new OrderId(message.orderId()), new AccountId(message.accountId()));
    }

    @DefaultRetryableTopic
    @KafkaListener(
        topics = "${kafka.listeners.engine.topics.matched}",
        groupId = "${kafka.listeners.engine.group-id}"
    )
    public void consumeMatched(ConsumerRecord<String, String> record) {
        List<EngineMatchedMessage> messages = objectMapper.readValue(
            record.value(),
            objectMapper.getTypeFactory().constructCollectionType(List.class, EngineMatchedMessage.class)
        );

        List<EngineMatchedCommand> commands = messages.stream()
            .map(EngineMatchedMessage::toCommand)
            .toList();

        matchedEventUseCase.handle(commands);
    }

    @DefaultRetryableTopic
    @KafkaListener(
        topics = "${kafka.listeners.engine.topics.canceled}",
        groupId = "${kafka.listeners.engine.group-id}"
    )
    public void consumeCanceled(ConsumerRecord<String, String> record) {
        EngineCanceledMessage message = objectMapper.readValue(record.value(), EngineCanceledMessage.class);
        canceledEventUseCase.handle(new OrderId(message.orderId()), new AccountId(message.accountId()), message.reason());
    }

    @DefaultRetryableTopic
    @KafkaListener(
        topics = "${kafka.listeners.engine.topics.rejected}",
        groupId = "${kafka.listeners.engine.group-id}"
    )
    public void consumeRejected(ConsumerRecord<String, String> record) {
        EngineRejectedMessage message = objectMapper.readValue(record.value(), EngineRejectedMessage.class);
        rejectedEventUseCase.handle(new OrderId(message.orderId()), new AccountId(message.accountId()), message.reason());
    }

    @DltHandler
    public void handleDlt(
        ConsumerRecord<String, String> record,
        @Header(KafkaHeaders.EXCEPTION_MESSAGE) String errorMessage
    ) {
        log.error("[ENGINE-EVENT-DLT] 최종 처리 실패 topic={}, partition={}, offset={}, Error={}, Payload={}", record.topic(), record.partition(), record.offset(), errorMessage, record.value());

        try {
            SaveConsumeFailedEventCommand command = new SaveConsumeFailedEventCommand(record.topic(), record.partition(), record.offset(), record.value(), errorMessage);
            saveConsumeFailedEventUseCase.save(command);
        } catch (Exception e) {
            log.error("[ENGINE-EVENT-DLT] 영속 실패", e);
        }
    }
}
