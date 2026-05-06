package dev.junyoung.exchange.accountservice.adapter.in.event;

import dev.junyoung.exchange.accountservice.adapter.in.event.annotation.DefaultRetryableTopic;
import dev.junyoung.exchange.accountservice.adapter.in.event.message.OrderReserveMessage;
import dev.junyoung.exchange.accountservice.application.exception.*;
import dev.junyoung.exchange.accountservice.application.port.in.ReserveBalanceUseCase;
import dev.junyoung.exchange.accountservice.application.port.in.SaveConsumerFailedEventUseCase;
import dev.junyoung.exchange.accountservice.application.port.in.SaveRejectedOutboxUseCase;
import dev.junyoung.exchange.accountservice.application.port.in.command.SaveConsumerFailedEventCommand;
import dev.junyoung.exchange.accountservice.domain.exception.AccountInvalidException;
import dev.junyoung.exchange.accountservice.domain.exception.InsufficientBalanceException;
import dev.junyoung.exchange.accountservice.domain.model.enums.RejectedReason;
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
public class OrderEventConsumer {

	private final ReserveBalanceUseCase reserveBalanceUseCase;
	private final SaveRejectedOutboxUseCase saveRejectedOutboxUseCase;
	private final ObjectMapper objectMapper;
	private final SaveConsumerFailedEventUseCase saveConsumerFailedEventUseCase;

	@DefaultRetryableTopic
	@KafkaListener(
		topics = "${kafka.listeners.order.topics.reserve}",
		groupId = "${kafka.listeners.order.group-id}"
	)
	public void consumeReserve(ConsumerRecord<String, String> record) {
		OrderReserveMessage message = objectMapper.readValue(record.value(), OrderReserveMessage.class);

		try {
			reserveBalanceUseCase.reserve(message.toCommand());
		} catch (Exception e) {
			RejectedReason reason = toRejectedReason(e);
			if (reason == null) throw e;
			saveRejectedOutboxUseCase.save(message.toRejectedCommand(reason));
		}
	}

	@DltHandler
	public void handleDlt(
		ConsumerRecord<String, String> record,
		@Header(KafkaHeaders.EXCEPTION_MESSAGE) String errorMessage
	) {
		log.error("[ORDER-EVENT-DLT] 최종 처리 실패 topic={}, partition={}, offset={}, Error={}, Payload={}", record.topic(), record.partition(), record.offset(), errorMessage, record.value());

		try {
			SaveConsumerFailedEventCommand command = new SaveConsumerFailedEventCommand(record.topic(), record.partition(), record.offset(), record.value(), errorMessage);
			saveConsumerFailedEventUseCase.save(command);
		} catch (Exception e) {
			log.error("[ORDER-EVENT-DLT] 영속 실패", e);
		}
	}

	private RejectedReason toRejectedReason(Exception e) {
		return switch (e) {
			case AccountNotFoundException ignored -> RejectedReason.ACCOUNT_NOT_FOUND;
			case AccountInactiveException ignored -> RejectedReason.ACCOUNT_INACTIVE;
			case AssetNotFoundException ignored -> RejectedReason.ASSET_NOT_FOUND;
			case AssetInactiveException ignored -> RejectedReason.ASSET_INACTIVE;
			case BalanceNotFoundException ignored -> RejectedReason.BALANCE_NOT_FOUND;
			case InsufficientBalanceException ignored -> RejectedReason.INSUFFICIENT_BALANCE;
			case AccountInvalidException ignored -> RejectedReason.INVALID_AMOUNT;
			default -> null;
		};
	}
}
