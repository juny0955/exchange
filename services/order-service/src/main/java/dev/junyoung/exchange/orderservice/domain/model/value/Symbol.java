package dev.junyoung.exchange.orderservice.domain.model.value;

import dev.junyoung.exchange.orderservice.domain.exception.OrderInvalidException;

public record Symbol(
	String baseAsset,
	String quoteAsset
) {
	public Symbol {
		if (baseAsset == null || baseAsset.isBlank())
			throw new OrderInvalidException("기초 자산은 필수입니다.");

		if (quoteAsset == null || quoteAsset.isBlank())
			throw new OrderInvalidException("결제 자산은 필수입니다.");

		baseAsset = baseAsset.toUpperCase();
		quoteAsset = quoteAsset.toUpperCase();

		if (baseAsset.equals(quoteAsset))
			throw new OrderInvalidException("기초 자산과 결제 자산은 같을 수 없습니다.");
	}

	public String getTicker() {
		return baseAsset + "-" + quoteAsset;
	}
}
