package dev.junyoung.exchange.core.exception.domain;

/**
 * 도메인 상태 충돌 예외의 추상 기반 클래스.
 *
 * <p>도메인 객체의 현재 상태가 요청한 작업을 허용하지 않을 때 사용한다.
 * 상태 머신의 전이 규칙 위반이 대표적인 사례다.</p>
 *
 * <p>직접 사용하지 않고 각 서비스에서 이 클래스를 extends한
 * 구체 예외를 정의하여 사용한다.</p>
 *
 * <p>사용 예시:</p>
 * <pre>
 * // 서비스별 커스텀 예외 정의
 * public class OrderCancelConflictException extends DomainConflictException {
 *     public OrderCancelConflictException(String message) { super(message); }
 * }
 *
 * // 도메인 내 사용
 * public void requestCancel() {
 *     if (isFinal()) throw new OrderCancelConflictException("이미 종료된 주문입니다.");
 * }
 * </pre>
 *
 * @see DomainException
 */
public abstract class DomainConflictException extends DomainException {
    protected DomainConflictException(String message) {
        super(message);
    }
}
