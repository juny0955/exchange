package dev.junyoung.exchange.core.exception.application;

import dev.junyoung.exchange.core.exception.CoreException;
import dev.junyoung.exchange.core.exception.ErrorCode;

/**
 * 애플리케이션 레이어 예외의 추상 기반 클래스.
 *
 * <p>유스케이스, 서비스, 이벤트 핸들러 등 애플리케이션 레이어에서
 * 발생하는 비즈니스 오류를 표현한다.</p>
 *
 * <p>직접 사용하지 않고 각 서비스(모듈)에서 이 클래스를 extends한
 * 커스텀 예외를 정의하여 사용한다.</p>
 *
 * <p>예시: {@code OrderApplicationException extends ApplicationException}</p>
 *
 * @see CoreException
 * @see ErrorCode
 */
public abstract class ApplicationException extends CoreException {

    protected ApplicationException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected ApplicationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
