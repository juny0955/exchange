package dev.junyoung.exchange.orderservice.domain.model.entity;

import dev.junyoung.exchange.core.exception.domain.DomainConflictException;
import dev.junyoung.exchange.orderservice.domain.exception.OrderInvalidException;
import dev.junyoung.exchange.orderservice.domain.exception.OrderStateConflictException;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderStatus;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderType;
import dev.junyoung.exchange.orderservice.domain.model.enums.Side;
import dev.junyoung.exchange.orderservice.domain.model.enums.TimeInForce;
import dev.junyoung.exchange.orderservice.domain.model.value.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Getter
public class Order {

	private final OrderId orderId;
	private final AccountId accountId;
	private final String clientOrderId;
	private final long acceptedSeq;

	private final Symbol symbol;
	private final Side side;
	private final OrderType orderType;
	private final TimeInForce tif;

	private final Price price;
	private final Quantity quantity;		// 수량 기준 (Base Asset)
	private final QuoteQty quoteQty;		// 금액 기준 (Quote Asset) 시장가 매수용

	private Quantity cumBaseQty; 		// 누적 체결 수량 (Base Asset)
	private QuoteQty cumQuoteQty; 		// 누적 체결 금액 (Quote Asset)

	private OrderStatus status;

	private final Instant orderedAt;		// 사용자 주문 시점
	private final Instant createdAt;		// 시스템 접수 시점
	private Instant updatedAt;

	public Order(
		OrderId orderId,
		AccountId accountId,
		String clientOrderId,
		long acceptedSeq,
		Symbol symbol,
		Side side,
		OrderType orderType,
		TimeInForce tif,
		Price price,
		Quantity quantity,
		QuoteQty quoteQty,
		Quantity cumBaseQty,
		QuoteQty cumQuoteQty,
		OrderStatus status,
		Instant orderedAt,
		Instant createdAt,
		Instant updatedAt
	) {
		this.orderId = orderId;
		this.accountId = accountId;
		this.clientOrderId = clientOrderId;
		this.acceptedSeq = acceptedSeq;
		this.symbol = symbol;
		this.side = side;
		this.orderType = orderType;
		this.tif = tif;
		this.price = price;
		this.quantity = quantity;
		this.quoteQty = quoteQty;
		this.cumBaseQty = cumBaseQty;
		this.cumQuoteQty = cumQuoteQty;
		this.status = status;
		this.orderedAt = orderedAt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;

		validateCommonFields();
		switch (orderType) {
			case LIMIT -> validateLimitOrder();
			case MARKET -> validateMarketOrder();
		}
	}

	public static Order create(
		AccountId accountId,
		String clientOrderId,
		long acceptedSeq,
		Symbol symbol,
		Side side,
		OrderType orderType,
		TimeInForce tif,
		Price price,
		Quantity quantity,
		QuoteQty quoteQty,
		Instant orderedAt
	) {
		Instant now = Instant.now();
		return new Order(
			OrderId.newId(),
			accountId,
			clientOrderId,
			acceptedSeq,
			symbol,
			side,
			orderType,
			tif,
			price,
			quantity,
			quoteQty,
			Quantity.zero(),
			QuoteQty.zero(),
			OrderStatus.PENDING,
			orderedAt,
			now,
			now
		);
	}

	public Optional<BigDecimal> getPrice() {
		return Optional.ofNullable(price).map(Price::value);
	}

	public Optional<BigDecimal> getQuantity() {
		return Optional.ofNullable(quantity).map(Quantity::value);
	}

	public Optional<BigDecimal> getQuoteQty() {
		return Optional.ofNullable(quoteQty).map(QuoteQty::value);
	}


	/**
	 * 예약 상태로 변경한다.
	 *
	 * <p>
	 *     주문 상태를 {@link OrderStatus#RESERVED}으로 변경한다.
	 *  </p>
	 *
	 *  @throws DomainConflictException 대기 상태가 아닌 경우 ({@link OrderStatus#PENDING})
	 */
	public void reserved() {
		if (!OrderStatus.PENDING.equals(status))
			throw new OrderStateConflictException("대기 상태 주문이 아닙니다.");

		status = OrderStatus.RESERVED;
		updatedAt = Instant.now();
	}

	/**
	 * 거부 상태로 변경한다.
	 *
	 * <p>
	 *     주문 상태를 {@link OrderStatus#REJECTED}으로 변경한다.
	 * </p>
	 *
	 * @throws DomainConflictException 예약 상태가 아닌 경우 ({@link OrderStatus#RESERVED})
	 */
	public void reject() {
		if (!OrderStatus.RESERVED.equals(status))
			throw new OrderStateConflictException("예약 상태 주문이 아닙니다.");

		status = OrderStatus.REJECTED;
		updatedAt = Instant.now();
	}

	/**
	 * 활성 상태로 변경한다
	 *
	 * <p>
	 *     주문 상태를 {@link OrderStatus#NEW}으로 변경한다.
	 * </p>
	 * @throws DomainConflictException 예약 상태 주문이 아닌 경우 ({@link OrderStatus#RESERVED})
	 */
	public void accepted() {
		if (!OrderStatus.RESERVED.equals(status))
			throw new OrderStateConflictException("예약 상태 주문이 아닙니다.");

		status = OrderStatus.NEW;
		updatedAt = Instant.now();
	}

	/**
	 * 주문 취소를 요청한다.
	 *
	 * 현재 상태에 따라 주문 상태를 변경한다.
	 * <ul>
	 *     <li>{@link OrderStatus#NEW} -> {@link OrderStatus#CANCEL_PENDING}</li>
	 *     <li>{@link OrderStatus#PARTIALLY_FILLED} -> {@link OrderStatus#PARTIALLY_FILLED_CANCEL_PENDING}</li>
	 * </ul>
	 * 실제 취소 확정은 매칭 엔진의 응답 이후 이루어진다.
	 * </p>
	 *
	 * @throws DomainConflictException 취소 요청 가능한 상태가 아닐 경우 ({@link OrderStatus#PENDING})
	 * @throws DomainConflictException 이미 취소 요청된 주문인 경우 ({@link OrderStatus#CANCEL_PENDING}, {@link OrderStatus#PARTIALLY_FILLED_CANCEL_PENDING})
	 * @throws DomainConflictException 이미 종료된 주문인 경우 ({@link OrderStatus#FILLED}, {@link OrderStatus#CANCELED}, {@link OrderStatus#REJECTED})
	 */
	public void requestCancel() {
		if (OrderStatus.PENDING.equals(status))
			throw new OrderStateConflictException("취소 요청 가능한 상태가 아닙니다.");

		if (isCancelPendingStatus())
			throw new OrderStateConflictException("이미 취소 요청된 주문입니다.");

		if (isFinal())
			throw new OrderStateConflictException("이미 종료된 주문입니다.");

		status = OrderStatus.PARTIALLY_FILLED.equals(status) ? OrderStatus.PARTIALLY_FILLED_CANCEL_PENDING : OrderStatus.CANCEL_PENDING;
		updatedAt = Instant.now();
	}

	/**
	 * 취소 상태로 변경한다
	 *
	 * <p>
	 *     주문 상태를 {@link OrderStatus#CANCELED}으로 변경한다.
	 * </p>
	 *
	 * @throws DomainConflictException 취소 대기 주문이 아닌 경우 ({@link OrderStatus#CANCEL_PENDING}), ({@link OrderStatus#PARTIALLY_FILLED_CANCEL_PENDING})
	 */
	public void cancel() {
		if (!isCancelPendingStatus())
			throw new OrderStateConflictException("취소 대기 주문이 아닙니다.");

		status = OrderStatus.CANCELED;
		updatedAt = Instant.now();
	}

	/**
	 * 체결 결과를 반영한다
	 *
	 * <p>
	 *     누적 체결 수량 및 금액을 업데이트하고 완전 체결 여부에 따른 상태 전이 수행
	 * </p>
	 *
	 * <ul>
	 *     <li>전량체결 -> {@link OrderStatus#FILLED}</li>
	 *     <li>부분체결:
	 *     		<ul>
	 *        		<li>취소대기상태 -> {@link OrderStatus#PARTIALLY_FILLED_CANCEL_PENDING}</li>
	 *         		<li>일반 -> {@link OrderStatus#PARTIALLY_FILLED}</li>
	 *     		</ul>
	 *     </li>
	 * </ul>
	 * @param baseQty 체결 수량
	 * @param quoteQty 체결 금액
	 * @throws DomainConflictException 체결 가능 상태가 아닌 경우 ({@link OrderStatus#PENDING})
	 * @throws DomainConflictException 이미 종료된 주문인 경우 ({@link OrderStatus#FILLED}, {@link OrderStatus#CANCELED}, {@link OrderStatus#REJECTED})
	 */
	public void fill(Quantity baseQty, QuoteQty quoteQty) {
		if (OrderStatus.PENDING.equals(status))
			throw new OrderStateConflictException("체결 가능한 주문 상태가 아닙니다.");
		if (isFinal())
			throw new OrderStateConflictException("이미 종료된 주문입니다.");

		cumBaseQty = cumBaseQty.add(baseQty);
		cumQuoteQty = cumQuoteQty.add(quoteQty);
		updatedAt = Instant.now();

		if (isFullyFilled()) {
			status = OrderStatus.FILLED;
			return;
		}

		if (isCancelPendingStatus()) {
			status = OrderStatus.PARTIALLY_FILLED_CANCEL_PENDING;
			return;
		}

		status = OrderStatus.PARTIALLY_FILLED;
	}

	/**
	 * 매수 주문 여부 확인
	 *
	 * @return 매수 주문 여부
	 */
	public boolean isBuy() {
		return Side.BUY.equals(side);
	}

	/**
	 * 취소 대기 상태 여부 판단
	 *
	 * 해당 상태인 경우 True
	 * <ul>
	 *     <li>{@link OrderStatus#CANCEL_PENDING}</li>
	 *     <li>{@link OrderStatus#PARTIALLY_FILLED_CANCEL_PENDING}</li>
	 * </ul>
	 * @return 취소 대기 여부
	 */
	public boolean isCancelPendingStatus() {
		return switch (status) {
			case CANCEL_PENDING, PARTIALLY_FILLED_CANCEL_PENDING -> true;
			default -> false;
		};
	}

	/**
	 * 최종 상태 확인
	 *
	 * <p>
	 *     해당 주문이
	 *     {@link OrderStatus#FILLED} or {@link OrderStatus#CANCELED} or {@link OrderStatus#REJECTED}
	 *     상태인지 체크한다.
	 * </p>
	 * @return 최종 상태 여부
	 */
	private boolean isFinal() {
		return switch (status) {
			case FILLED, CANCELED, REJECTED -> true;
			default -> false;
		};
	}

	/**
	 * 전량 체결 여부 판단
	 *
	 * <ul>
	 *     <li>{@link OrderType#LIMIT} 주문: {@link #cumBaseQty}가 주문 수량 이상일시 </li>
	 *     <li>{@link OrderType#MARKET} 주문:
	 *     		<ul>
	 *     		 	<li>{@link Side#BUY} 주문: {@link #cumQuoteQty}가 주문 금액 이상일시</li>
	 *     		 	<li>{@link Side#SELL} 주문: {@link #cumBaseQty}가 주문 수량 이상일시</li>
	 *     		</ul>
	 *     </li>
	 * </ul>
	 * @return 완전 체결 여부
	 */
	private boolean isFullyFilled() {
		return switch (orderType) {
			case LIMIT -> cumBaseQty.value().compareTo(quantity.value()) >= 0;
			case MARKET -> switch (side) {
				case BUY -> cumQuoteQty.value().compareTo(quoteQty.value()) >= 0;
				case SELL -> cumBaseQty.value().compareTo(quantity.value()) >= 0;
			};
		};
	}

	private void validateCommonFields() {
		if (orderId == null) throw new OrderInvalidException("주문 ID는 필수입니다.");
		if (accountId == null) throw new OrderInvalidException("계좌 ID는 필수입니다.");
		if (clientOrderId == null || clientOrderId.isBlank()) throw new OrderInvalidException("멱등 주문 ID는 필수입니다.");
		if (acceptedSeq <= 0) throw new OrderInvalidException("접수 순번은 0보다 커야합니다.");
		if (symbol == null) throw new OrderInvalidException("거래 심볼은 필수입니다.");
		if (side == null) throw new OrderInvalidException("주문 방향(매수/매도)은 필수입니다.");
		if (orderType == null) throw new OrderInvalidException("주문 유형은 필수입니다.");
		if (tif == null) throw new OrderInvalidException("주문 조건은 필수입니다.");
		if (orderedAt == null) throw new OrderInvalidException("주문 시점은 필수입니다.");
	}

	private void validateLimitOrder() {
		if (quoteQty != null) throw new OrderInvalidException("지정가 주문에는 금액을 지정할 수 없습니다.");
		if (price == null) throw new OrderInvalidException("지정가 주문에는 가격이 필수입니다.");
		if (quantity == null) throw new OrderInvalidException("지정가 주문에는 수량이 필수입니다.");
		if (quantity.isZero()) throw new OrderInvalidException("주문 수량은 0보다 커야합니다.");
	}

	private void validateMarketOrder() {
		switch (side) {
			case BUY -> {
				if (price != null) throw new OrderInvalidException("시장가 매수 주문에는 가격을 지정할 수 없습니다.");
				if (quantity != null) throw new OrderInvalidException("시장가 매수 주문에는 수량을 지정할 수 없습니다.");
				if (quoteQty == null) throw new OrderInvalidException("시장가 매수 주문에는 금액이 필수입니다.");
				if (quoteQty.isZero()) throw new OrderInvalidException("주문 금액은 0보다 커야합니다.");
			}
			case SELL -> {
				if (price != null) throw new OrderInvalidException("시장가 매도 주문에는 가격을 지정할 수 없습니다.");
				if (quoteQty != null) throw new OrderInvalidException("시장가 매도 주문에는 금액을 지정할 수 없습니다.");
				if (quantity == null) throw new OrderInvalidException("시장가 매도 주문에는 수량이 필수입니다.");
				if (quantity.isZero()) throw new OrderInvalidException("주문 수량은 0보다 커야합니다.");
			}
		}
	}
}
