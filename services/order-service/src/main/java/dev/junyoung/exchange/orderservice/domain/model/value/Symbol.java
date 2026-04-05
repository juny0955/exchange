package dev.junyoung.exchange.orderservice.domain.model.value;

public record Symbol(
	String baseAsset,
	String quoteAsset
) {
	public Symbol {
		if (baseAsset == null || baseAsset.isBlank())
			throw new IllegalArgumentException("기초 자산을 필수 입니다.");

		if (quoteAsset == null || quoteAsset.isBlank())
			throw new IllegalArgumentException("결제 자산은 필수 입니다.");

		baseAsset = baseAsset.toUpperCase();
		quoteAsset = quoteAsset.toUpperCase();

		if (baseAsset.equals(quoteAsset))
			throw new IllegalArgumentException("기초 자산과 결제 자산은 같을 수 없습니다.");
	}

	public String getTicker() {
		return baseAsset + "-" + quoteAsset;
	}
}
