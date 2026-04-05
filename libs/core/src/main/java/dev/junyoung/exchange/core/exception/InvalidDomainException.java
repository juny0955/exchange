package dev.junyoung.exchange.core.exception;

/**
 * 도메인 불변식 위반 및 VO 검증 등
 */
public class InvalidDomainException extends RuntimeException {
	public InvalidDomainException(String message) {
		super(message);
	}
}
