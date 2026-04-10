package dev.junyoung.exchange.orderservice.application.port.in;

import dev.junyoung.exchange.orderservice.application.port.in.command.CancelOrderCommand;

/**
 * <h1>주문 취소 서비스</h1>
 *
 * <p>
 *     Order 상태 업데이트(CANCEL_PENDING) -> 엔진 취소 전달 <br>
 *     엔진 취소 후 이벤트 전달받아 (CANCEL) 상태변경, Account 자산 Release
 * </p>
 */
public interface CancelOrderUseCase {
    void cancelOrder(CancelOrderCommand command);
}
