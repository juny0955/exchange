package dev.junyoung.exchange.core.exception.domain;

/**
 * 도메인 불변식 위반 예외의 추상 기반 클래스.
 *
 * <p>도메인 객체(Entity, VO)의 생성 또는 변경 시 불변식(invariant)이
 * 위반됐을 때 사용한다. null 검증, 양수 범위 검증, 필드 조합 규칙 등이 해당된다.</p>
 *
 * <p>직접 사용하지 않고 각 서비스에서 이 클래스를 extends한
 * 구체 예외를 정의하여 사용한다.</p>
 *
 * <p>사용 예시:</p>
 * <pre>
 * // 서비스별 커스텀 예외 정의
 * public class OrderInvalidException extends DomainInvalidException {
 *     public OrderInvalidException(String message) { super(message); }
 * }
 *
 * // VO 내 사용
 * public Price {
 *     if (value == null) throw new OrderInvalidException("가격은 필수입니다.");
 *     if (value.signum() {@literal <=} 0) throw new OrderInvalidException("가격은 양수여야 합니다.");
 * }
 * </pre>
 *
 * @see DomainException
 */
public abstract class DomainInvalidException extends DomainException {
	protected DomainInvalidException(String message) {
		super(message);
	}
}
