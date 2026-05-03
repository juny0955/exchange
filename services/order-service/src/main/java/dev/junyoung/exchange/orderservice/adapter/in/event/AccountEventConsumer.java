package dev.junyoung.exchange.orderservice.adapter.in.event;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import dev.junyoung.exchange.orderservice.adapter.in.event.annotation.DefaultRetryableTopic;
import dev.junyoung.exchange.orderservice.adapter.in.event.message.AccountReservedMessage;
import dev.junyoung.exchange.orderservice.application.port.in.SaveConsumeFailedEventUseCase;
import dev.junyoung.exchange.orderservice.application.port.in.account.HandleAccountReservedEvent;
import dev.junyoung.exchange.orderservice.application.port.in.command.SaveConsumeFailedEventCommand;
import dev.junyoung.exchange.orderservice.domain.model.value.AccountId;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountEventConsumer {

	private final HandleAccountReservedEvent handleAccountReservedEvent;
	private final SaveConsumeFailedEventUseCase saveConsumeFailedEventUseCase;
	private final ObjectMapper objectMapper;

	@DefaultRetryableTopic
	@KafkaListener(
		topics = "${kafka.listeners.account.topics.reserved}",
		groupId = "${kafka.listeners.account.group-id}"
	)
	public void consumeReserved(ConsumerRecord<String, String> record) {
		AccountReservedMessage message = objectMapper.readValue(record.value(), AccountReservedMessage.class);
		handleAccountReservedEvent.handle(new OrderId(message.orderId()), new AccountId(message.accountId()));
	}

	@DltHandler
	public void handleDlt(
		ConsumerRecord<String, String> record,
		@Header(KafkaHeaders.EXCEPTION_MESSAGE) String errorMessage
	) {
		log.error("[ACCOUNT-EVENT-DLT] 최종 처리 실패 topic={}, partition={}, offset={}, Error={}, Payload={}", record.topic(), record.partition(), record.offset(), errorMessage, record.value());

		try {
			SaveConsumeFailedEventCommand command = new SaveConsumeFailedEventCommand(record.topic(), record.partition(), record.offset(), record.value(), errorMessage);
			saveConsumeFailedEventUseCase.save(command);
		} catch (Exception e) {
			log.error("[ACCOUNT-EVENT-DLT] 영속 실패", e);
		}
	}
}
