package dev.junyoung.exchange.orderservice.domain.service.dto;

import java.math.BigDecimal;

public record AssetReserveResult(
	String asset,
	BigDecimal amount
) {
	public static AssetReserveResult of(String asset, BigDecimal amount) {
		return new AssetReserveResult(asset, amount);
	}
}
