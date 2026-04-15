package dev.junyoung.exchange.core.exception.domain;

import dev.junyoung.exchange.core.exception.CoreException;
import dev.junyoung.exchange.core.exception.ErrorCode;

/**
 * 도메인 레이어 예외의 추상 기반 클래스.
 *
 * <p>도메인 모델(Entity, VO, 도메인 서비스)에서 발생하는 모든 예외의 상위 타입이다.</p>
 *
 * <p>{@link CoreException}과 별도 계층을 형성하며, 도메인 레이어가
 * HTTP 상태코드({@link ErrorCode}) 개념에 의존하지 않도록 설계되었다.
 * 예외 메시지는 도메인 언어로 작성하고, HTTP 매핑은 {@code GlobalExceptionHandler}가 담당한다.</p>
 *
 * <p>직접 사용하지 않고 {@link dev.junyoung.exchange.core.exception.domain.DomainConflictException} 또는
 * {@link dev.junyoung.exchange.core.exception.domain.DomainInvalidException}을 extends한
 * 서비스별 커스텀 예외를 사용한다.</p>
 *
 * @see dev.junyoung.exchange.core.exception.domain.DomainConflictException
 * @see dev.junyoung.exchange.core.exception.domain.DomainInvalidException
 */
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) {
        super(message);
    }
}
