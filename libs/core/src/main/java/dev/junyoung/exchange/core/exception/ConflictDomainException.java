package dev.junyoung.exchange.core.exception;

/**
 * 도메인 상태 충돌 예외
 */
public class ConflictDomainException extends RuntimeException {
    public ConflictDomainException(String message) {
        super(message);
    }
}
