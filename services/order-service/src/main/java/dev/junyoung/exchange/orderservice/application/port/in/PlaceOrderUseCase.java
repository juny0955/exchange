package dev.junyoung.exchange.orderservice.application.port.in;

import dev.junyoung.exchange.orderservice.application.port.in.command.PlaceOrderCommand;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;

/**
 * <h1>주문 접수 서비스</h1>
 *
 * <p>
 *     Order 선저장 (PENDING) -> Account 잔고 검증 / 홀드 -> Matching Engine 주문 접수 순서로 동작
 * </p>
 */
public interface PlaceOrderUseCase {
	OrderId placeOrder(PlaceOrderCommand command);
}
