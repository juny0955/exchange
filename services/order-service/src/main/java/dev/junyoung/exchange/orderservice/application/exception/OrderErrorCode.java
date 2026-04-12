package dev.junyoung.exchange.orderservice.application.exception;

import dev.junyoung.exchange.core.exception.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum OrderErrorCode implements ErrorCode {
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER-NOT-FOUND", "해당 주문을 찾을 수 없습니다."),
    DUPLICATE_PLACE_ORDER(HttpStatus.CONFLICT, "ORDER-DUP-PLACE", "이미 처리된 주문입니다."),
    ORDER_ALREADY_FINAL(HttpStatus.UNPROCESSABLE_CONTENT, "ORDER-ALREADY-FINAL", "이미 종료된 주문입니다."),
    ORDER_ALREADY_REQ_CANCEL(HttpStatus.CONFLICT, "ORDER-ALREADY-REQ-CANCEL", "이미 취소 요청된 주문입니다."),

    ACCOUNT_RESERVE_FAILED(HttpStatus.BAD_REQUEST, "ORDER-ACCOUNT-RESERVE-FAILED", "계좌 검증, 홀드 실패") // TODO 세분화 필요
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

    @Override
    public int status() {
        return status.value();
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
