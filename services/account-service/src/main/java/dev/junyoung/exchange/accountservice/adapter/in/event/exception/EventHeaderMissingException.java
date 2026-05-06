package dev.junyoung.exchange.accountservice.adapter.in.event.exception;

/**
 * 이벤트 메시지 헤더 누락 시 발생.
 *
 * <p>
 *     Consumer 내부에서만 처리되어 DLT로 라우팅되므로
 *     HTTP 응답 매핑이 불필요하다. 따라서 표준 {@link IllegalArgumentException}을 상속한다.
 * </p>
 */
public class EventHeaderMissingException extends IllegalArgumentException {
	public EventHeaderMissingException(String groupId, String topic, int partition, long offset) {
		super("[%s] 이벤트 헤더 누락 topic=%s partition=%d offset=%d".formatted(groupId, topic, partition, offset));
	}
}
