package dev.junyoung.exchange.core.exception;

import java.util.Objects;

/**
 * 애플리케이션 전반에서 사용하는 공통 예외이다.
 *
 * <p>{@link ErrorCode}를 기반으로 예외를 생성하며,
 * 서비스별로 정의한 에러 코드 Enum을 전달받아 사용한다.</p>
 *
 * <p>예외 메시지는 기본적으로 {@link ErrorCode#message()} 값을 사용한다.</p>
 */
public class CoreException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * 에러 코드를 기반으로 예외를 생성한다.
     *
     * <p>예외 메시지는 {@link ErrorCode#message()} 값을 사용한다.</p>
     *
     * @param errorCode 서비스별 에러 코드
     */
    public CoreException(ErrorCode errorCode) {
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
    public CoreException(ErrorCode errorCode, Throwable cause) {
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
