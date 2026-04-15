package dev.junyoung.exchange.core.exception;

import dev.junyoung.exchange.core.exception.application.ApplicationException;
import dev.junyoung.exchange.core.exception.infrastructure.InfrastructureException;

import java.util.Objects;

/**
 * 모든 커스텀 예외의 최상위 추상 클래스.
 *
 * <p>{@link ErrorCode}를 기반으로 HTTP 상태 코드와 에러 코드를 관리하며,
 * 예외 메시지는 기본적으로 {@link ErrorCode#message()} 값을 사용한다.</p>
 *
 * <p>전체 예외 계층 구조:</p>
 * <pre>
 * CoreException (abstract)                 — HTTP 에러코드 기반
 * ├── ApplicationException (abstract)      — 애플리케이션 레이어 예외
 * └── InfrastructureException (abstract)   — 인프라 레이어 예외
 *
 * DomainException (abstract, 별도 트리)    — 도메인 순수 예외 (HTTP 무관)
 * ├── DomainConflictException (abstract)   — 도메인 상태 충돌
 * └── DomainInvalidException (abstract)    — 도메인 불변식 위반
 * </pre>
 *
 * <p>직접 사용하지 않고 {@link ApplicationException} 또는
 * {@link InfrastructureException}을 extends한 서비스별 커스텀 예외를 사용한다.</p>
 */
public abstract class CoreException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * 에러 코드를 기반으로 예외를 생성한다.
     *
     * <p>예외 메시지는 {@link ErrorCode#message()} 값을 사용한다.</p>
     *
     * @param errorCode 서비스별 에러 코드
     */
    protected CoreException(ErrorCode errorCode) {
        super(requireErrorCode(errorCode).message());
        this.errorCode = errorCode;
    }

    /**
     * 에러 코드를 기반으로 예외를 생성한다.
     *
     * <p>예외 메시지는 {@link ErrorCode#message()} 값을 사용한다.</p>
     *
     * @param errorCode 서비스별 에러 코드
     */
    protected CoreException(ErrorCode errorCode, Throwable cause) {
        super(requireErrorCode(errorCode).message(), cause);
        this.errorCode = errorCode;
    }

    /**
     * 해당 예외에 연결된 에러 코드를 반환한다.
     *
     * @return 에러 코드
     */
    public ErrorCode errorCode() {
        return errorCode;
    }

    private static ErrorCode requireErrorCode(ErrorCode errorCode) {
        return Objects.requireNonNull(errorCode, "errorCode는 null일 수 없습니다.");
    }
}
