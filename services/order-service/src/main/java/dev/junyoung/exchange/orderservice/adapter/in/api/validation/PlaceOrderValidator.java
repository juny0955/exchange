package dev.junyoung.exchange.orderservice.adapter.in.api.validation;

import dev.junyoung.exchange.orderservice.adapter.in.api.requests.PlaceOrderRequest;
import dev.junyoung.exchange.orderservice.adapter.in.api.validation.annotation.ValidPlaceOrder;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderType;
import dev.junyoung.exchange.orderservice.domain.model.enums.Side;
import dev.junyoung.exchange.orderservice.domain.model.enums.TimeInForce;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PlaceOrderValidator implements ConstraintValidator<ValidPlaceOrder, PlaceOrderRequest> {

	@Override
	public boolean isValid(PlaceOrderRequest request, ConstraintValidatorContext context) {
		boolean isMarket = OrderType.isMarket(request.orderType());
		boolean isBuy = Side.isBuy(request.side());

		if (!isMarket)
			return validateLimitOrder(request, context);

		if (isBuy)
			return validateMarketBuy(request, context);

		return validateMarketSell(request, context);
	}

	private boolean validateLimitOrder(PlaceOrderRequest request, ConstraintValidatorContext context) {
		if (request.price() == null)
			return fail(context, "price", "지정가 주문은 주문 가격이 필수입니다.");

		if (request.quantity() == null)
			return fail(context, "quantity", "지정가 주문은 주문 수량이 필수입니다.");

		if (request.quoteQty() != null)
			return fail(context, "quoteQty", "지정가 주문은 주문 금액을 사용할 수 없습니다.");

		return true;
	}

	private boolean validateMarketBuy(PlaceOrderRequest request, ConstraintValidatorContext context) {
		if (!TimeInForce.IOC.name().equalsIgnoreCase(request.tif()))
			return fail(context, "tif","시장가 주문은 IOC만 지원합니다.");

		if (request.quoteQty() == null)
			return fail(context, "quoteQty","시장가 매수 주문은 주문 금액이 필수입니다.");

		if (request.quantity() != null)
			return fail(context, "quantity", "시장가 매수 주문은 주문 수량을 사용할 수 없습니다.");

		if (request.price() != null)
			return fail(context, "price", "시장가 매수 주문은 주문 가격을 사용할 수 없습니다.");

		return true;
	}

	private boolean validateMarketSell(PlaceOrderRequest request, ConstraintValidatorContext context) {
		if (!TimeInForce.IOC.name().equalsIgnoreCase(request.tif()))
			return fail(context, "tif", "시장가 주문은 IOC만 지원합니다.");

		if (request.quantity() == null)
			return fail(context, "quantity", "시장가 매도 주문은 주문 수량이 필수입니다.");

		if (request.quoteQty() != null)
			return fail(context, "quoteQty", "시장가 매도 주문은 주문 금액을 사용할 수 없습니다.");

		if (request.price() != null)
			return fail(context, "price", "시장가 매도 주문은 주문 가격을 사용할 수 없습니다.");

		return true;
	}

	private boolean fail(ConstraintValidatorContext context, String field, String message) {
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(message)
			.addPropertyNode(field)
			.addConstraintViolation();
		return false;
	}
}
