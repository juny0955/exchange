package dev.junyoung.exchange.orderservice.domain.service;

import java.math.BigDecimal;

import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.value.Symbol;
import dev.junyoung.exchange.orderservice.domain.service.dto.AssetReserveResult;

/**
 * 주문 유형과 방향에 따라 잠글 자산과 금액을 계산하는 도메인 서비스.
 *
 * <pre>
 * | 주문 유형 | 방향 | 잠글 자산   | 잠글 금액         |
 * |-----------|------|-------------|-------------------|
 * | LIMIT     | BUY  | quoteAsset  | price × quantity  |
 * | LIMIT     | SELL | baseAsset   | quantity          |
 * | MARKET    | BUY  | quoteAsset  | quoteQty          |
 * | MARKET    | SELL | baseAsset   | quantity          |
 * </pre>
 *
 * @deprecated 매칭엔진 인메모리 잔고 검증으로 사용 하지않음 제거예정
 */
@Deprecated
public final class AssetReserveCalculator {

	/**
	 * 주문에 대한 자산 잠금 정보를 계산한다.
	 *
	 * @param order 잠금 계산 대상 주문
	 * @return 잠글 자산과 금액
	 */
	public static AssetReserveResult calculate(Order order) {
		return switch (order.getOrderType()) {
			case LIMIT -> calcLimitOrder(order);
			case MARKET -> calcMarketOrder(order);
		};
	}

	/**
	 * 지정가 주문의 자산 잠금 정보를 계산한다.
	 *
	 * <p>
	 *     매수 시 결제 자산(quoteAsset)을 {@code price × quantity} 만큼 잠근다.<br>
	 *     매도 시 기초 자산(baseAsset)을 {@code quantity} 만큼 잠근다.
	 * </p>
	 */
	private static AssetReserveResult calcLimitOrder(Order order) {
		Symbol symbol = order.getSymbol();
		BigDecimal price = order.getPrice().get();
		BigDecimal quantity = order.getQuantity().get();

		return order.isBuy() ?
			AssetReserveResult.of(symbol.quoteAsset(), price.multiply(quantity)) :
			AssetReserveResult.of(symbol.baseAsset(), quantity);
	}

	/**
	 * 시장가 주문의 자산 잠금 정보를 계산한다.
	 *
	 * <p>
	 *     매수 시 결제 자산(quoteAsset)을 {@code quoteQty} 만큼 잠근다.<br>
	 *     매도 시 기초 자산(baseAsset)을 {@code quantity} 만큼 잠근다.
	 * </p>
	 */
	private static AssetReserveResult calcMarketOrder(Order order) {
		Symbol symbol = order.getSymbol();

		return order.isBuy() ?
			AssetReserveResult.of(symbol.quoteAsset(), order.getQuoteQty().get()) :
			AssetReserveResult.of(symbol.baseAsset(), order.getQuantity().get());
	}
}
