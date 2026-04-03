package dev.junyoung.exchange.core.exception;

/**
 * 서비스별 에러코드를 정의하기 위한 공통 인터페이스이다.
 *
 * <p>각 서비스는 도메인에 맞는 에러코드를 Enum으로 정의하고
 * 해당 인터페이스를 구현하여 사용한다.
 * </p>
 *
 * <ul>
 *     <li>{@link CoreException}에서 참조한다.</li>
 * </ul>
 */
public interface ErrorCode {
    /**
     * HTTP 응답 상태 코드를 반환한다.
     *
     * <p>예: 400, 404, 500</p>
     *
     * @return HTTP Status code
     */
    int status();

    /**
     * 서비스 내부에서 관리하는 고유 에러코드를 반환한다.
     *
     * <p>{@code CORE-001}, {@code CORE-002}</p>
     *
     * @return 고유 에러코드 문자열
     */
    String code();

    /**
     * 클라이언트에 전달할 기본 에러 메시지를 반환한다.
     *
     * <p>예: {@code 해당 리소스를 찾을 수 없습니다.}</p>
     *
     * @return 기본 에러 메시지
     */
    String message();
}
