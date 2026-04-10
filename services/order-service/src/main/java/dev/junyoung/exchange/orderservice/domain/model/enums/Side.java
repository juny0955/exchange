package dev.junyoung.exchange.orderservice.domain.model.enums;

public enum Side {
	BUY, SELL;

	public static boolean isBuy(String side) {
		return BUY.name().equalsIgnoreCase(side);
	}
}
