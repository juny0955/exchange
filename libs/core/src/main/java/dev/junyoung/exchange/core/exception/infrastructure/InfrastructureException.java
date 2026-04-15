package dev.junyoung.exchange.core.exception.infrastructure;

import dev.junyoung.exchange.core.exception.CoreException;
import dev.junyoung.exchange.core.exception.ErrorCode;

/**
 * 인프라스트럭처 레이어 예외의 추상 기반 클래스.
 *
 * <p>외부 시스템 연동(gRPC, REST, 메시지 브로커, DB 등) 중 발생하는
 * 인프라 오류를 표현한다.</p>
 *
 * <p>직접 사용하지 않고 각 서비스(모듈)에서 이 클래스를 extends한
 * 커스텀 예외를 정의하여 사용한다.</p>
 *
 * <p>예시: {@code AccountGrpcException extends InfrastructureException}</p>
 *
 * @see CoreException
 * @see ErrorCode
 */
public abstract class InfrastructureException extends CoreException {

    protected InfrastructureException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected InfrastructureException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
