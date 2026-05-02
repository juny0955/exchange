package dev.junyoung.exchange.orderservice.application.port.out.command;

import dev.junyoung.exchange.orderservice.domain.model.value.Symbol;

public record OrderSymbolEvent(
	String baseAsset,
	String quoteAsset
) {
	public static OrderSymbolEvent from(Symbol symbol) {
		return new OrderSymbolEvent(symbol.baseAsset(), symbol.quoteAsset());
	}
}
