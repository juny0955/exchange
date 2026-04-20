package dev.junyoung.exchange.orderservice.adapter.in.event;

import dev.junyoung.exchange.orderservice.adapter.in.event.annotation.EngineRetryableTopic;
import dev.junyoung.exchange.orderservice.adapter.in.event.message.EngineAcceptedMessage;
import dev.junyoung.exchange.orderservice.adapter.in.event.message.EngineCanceledMessage;
import dev.junyoung.exchange.orderservice.adapter.in.event.message.EngineMatchedMessage;
import dev.junyoung.exchange.orderservice.adapter.in.event.message.EngineRejectedMessage;
import dev.junyoung.exchange.orderservice.application.port.in.engine.HandleEngineAcceptedEventUseCase;
import dev.junyoung.exchange.orderservice.application.port.in.engine.HandleEngineCanceledEventUseCase;
import dev.junyoung.exchange.orderservice.application.port.in.engine.HandleEngineMatchedEventUseCase;
import dev.junyoung.exchange.orderservice.application.port.in.engine.HandleEngineRejectedEventUseCase;
import dev.junyoung.exchange.orderservice.domain.model.value.AccountId;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class EngineEventConsumer {

    private static final String GROUP_ID = "engine-event-handler";
    private static final String ACCEPTED_TOPIC = "engine.ACCEPTED";
    private static final String MATCHED_TOPIC = "engine.MATCHED";
    private static final String CANCELED_TOPIC = "engine.CANCELED";
    private static final String REJECTED_TOPIC = "engine.REJECTED";

    private final HandleEngineAcceptedEventUseCase acceptedEventUseCase;
    private final HandleEngineMatchedEventUseCase matchedEventUseCase;
    private final HandleEngineRejectedEventUseCase rejectedEventUseCase;
    private final HandleEngineCanceledEventUseCase canceledEventUseCase;
    private final ObjectMapper objectMapper;

    @EngineRetryableTopic
    @KafkaListener(topics = ACCEPTED_TOPIC, groupId = GROUP_ID)
    public void consumeAccepted(ConsumerRecord<String, String> record) {
        EngineAcceptedMessage message = objectMapper.readValue(record.value(), EngineAcceptedMessage.class);
        acceptedEventUseCase.handle(new OrderId(message.orderId()), new AccountId(message.accountId()));
    }

    @EngineRetryableTopic
    @KafkaListener(topics = MATCHED_TOPIC, groupId = GROUP_ID)
    public void consumeMatched(ConsumerRecord<String, String> record) {
        EngineMatchedMessage message = objectMapper.readValue(record.value(), EngineMatchedMessage.class);
        matchedEventUseCase.handle(message.toCommand());
    }

    @EngineRetryableTopic
    @KafkaListener(topics = CANCELED_TOPIC, groupId = GROUP_ID)
    public void consumeCanceled(ConsumerRecord<String, String> record) {
        EngineCanceledMessage message = objectMapper.readValue(record.value(), EngineCanceledMessage.class);
        canceledEventUseCase.handle(new OrderId(message.orderId()), new AccountId(message.accountId()));
    }

    @EngineRetryableTopic
    @KafkaListener(topics = REJECTED_TOPIC, groupId = GROUP_ID)
    public void consumeRejected(ConsumerRecord<String, String> record) {
        EngineRejectedMessage message = objectMapper.readValue(record.value(), EngineRejectedMessage.class);
        rejectedEventUseCase.handle(new OrderId(message.orderId()), new AccountId(message.accountId()));
    }
}
