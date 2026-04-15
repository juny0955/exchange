package dev.junyoung.exchange.orderservice.application.port.in;

import dev.junyoung.exchange.orderservice.application.port.in.command.PlaceOrderCommand;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;

/**
 * <h1>주문 접수 서비스</h1>
 *
 * <p>
 *     Order 및 Outbox 저장 -> 잔고 점증 및
 * </p>
 */
public interface PlaceOrderUseCase {
	OrderId placeOrder(PlaceOrderCommand command);
}
