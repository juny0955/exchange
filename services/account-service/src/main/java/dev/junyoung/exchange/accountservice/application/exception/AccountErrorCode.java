package dev.junyoung.exchange.accountservice.application.exception;

import dev.junyoung.exchange.core.exception.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum AccountErrorCode implements ErrorCode {
    ACCOUNT_NOT_FOUND       (HttpStatus.NOT_FOUND,  "ACCOUNT-NOT-FOUND",    "해당 계좌를 찾을 수 없습니다."),
    ACCOUNT_INACTIVE        (HttpStatus.CONFLICT,   "ACCOUNT-INACTIVE",     "계좌가 활성 상태가 아닙니다."),
    ASSET_NOT_FOUND         (HttpStatus.NOT_FOUND,  "ASSET-NOT-FOUND",      "해당 자산을 찾을 수 없습니다."),
    ASSET_INACTIVE          (HttpStatus.CONFLICT,   "ASSET-INACTIVE",       "거래 불가능한 자산입니다."),
    BALANCE_NOT_FOUND       (HttpStatus.NOT_FOUND,  "BALANCE-NOT-FOUND",    "해당 잔고를 찾을 수 없습니다."),
    RESERVATION_NOT_FOUND   (HttpStatus.NOT_FOUND,  "RESERVATION-NOT-FOUND","해당 예약을 찾을 수 없습니다."),
    OUTBOX_NOT_FOUND        (HttpStatus.NOT_FOUND, "OUTBOX-NOT-FOUND",      "해당 Outbox를 찾을 수 없습니다.");

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
