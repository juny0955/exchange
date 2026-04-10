package dev.junyoung.exchange.orderservice.application.service;

import dev.junyoung.exchange.orderservice.application.port.in.CancelOrderUseCase;
import dev.junyoung.exchange.orderservice.application.port.in.command.CancelOrderCommand;
import dev.junyoung.exchange.orderservice.application.port.out.EngineExecutionPort;
import dev.junyoung.exchange.orderservice.application.port.out.command.EngineCancelCommand;
import dev.junyoung.exchange.orderservice.application.service.tx.CancelOrderTx;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <h1>주문 취소 서비스</h1>
 *
 * <p>
 *     Order 상태 업데이트(CANCEL_PENDING) -> 엔진 취소 전달 <br>
 *     엔진 취소 후 이벤트 전달받아 (CANCEL) 상태변경, Account 자산 Release
 * </p>
 */
@Service
@RequiredArgsConstructor
public class CancelOrderService implements CancelOrderUseCase {

    private final CancelOrderTx cancelOrderTx;
    private final EngineExecutionPort engineExecutionPort;

    @Override
    public void cancelOrder(CancelOrderCommand command) {
        Order order = cancelOrderTx.cancelOrderTx(command);
        processEngineCancel(order);
    }

    private void processEngineCancel(Order order) {
        try {
            engineExecutionPort.cancel(new EngineCancelCommand(order.getOrderId().value(), order.getAccountId().value()));
        } catch (Exception e) {
            // TODO 주문 취소 실패시 어떻게 처리할지 결정 해야함
            throw e;
        }
    }
}
