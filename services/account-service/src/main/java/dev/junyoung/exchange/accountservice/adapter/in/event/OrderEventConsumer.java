package dev.junyoung.exchange.accountservice.adapter.in.event;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import dev.junyoung.exchange.accountservice.adapter.in.event.annotation.DefaultRetryableTopic;
import dev.junyoung.exchange.accountservice.adapter.in.event.message.OrderReserveMessage;
import dev.junyoung.exchange.accountservice.application.port.in.ReserveBalanceUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

	private final ReserveBalanceUseCase reserveBalanceUseCase;
	private final ObjectMapper objectMapper;

	@DefaultRetryableTopic
	@KafkaListener(
		topics = "${kafka.listeners.order.topics.reserve}",
		groupId = "${kafka.listeners.order.group-id}"
	)
	public void consumeReserve(ConsumerRecord<String, String> record) {
		OrderReserveMessage message = objectMapper.readValue(record.value(), OrderReserveMessage.class);
		reserveBalanceUseCase.reserve(message.toCommand());
	}
}
