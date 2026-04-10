package dev.junyoung.exchange.orderservice.domain.model.enums;

public enum OrderType {
	LIMIT, MARKET;

	public static boolean isMarket(String type) {
		return MARKET.name().equalsIgnoreCase(type);
	}
}
