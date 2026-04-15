package dev.junyoung.exchange.orderservice.adapter.in.api.requests;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import dev.junyoung.exchange.coreweb.validation.annotation.ValidEnum;
import dev.junyoung.exchange.orderservice.adapter.in.api.validation.annotation.ValidPlaceOrder;
import dev.junyoung.exchange.orderservice.application.port.in.command.PlaceOrderCommand;
import dev.junyoung.exchange.orderservice.domain.model.enums.OrderType;
import dev.junyoung.exchange.orderservice.domain.model.enums.Side;
import dev.junyoung.exchange.orderservice.domain.model.enums.TimeInForce;
import jakarta.validation.constraints.*;

@ValidPlaceOrder
public record PlaceOrderRequest (

	@NotNull
	UUID accountId,

	@NotBlank
	@Size(max = 64)
	String clientOrderId,

	@NotBlank
	@Size(max = 8)
	String baseAsset,

	@NotBlank
	@Size(max = 8)
	String quoteAsset,

	@NotBlank
	@ValidEnum(enumClass = Side.class)
	String side,

	@NotBlank
	@ValidEnum(enumClass = OrderType.class)
	String orderType,

	@NotBlank
	@ValidEnum(enumClass = TimeInForce.class)
	String tif,

	@DecimalMin(value = "0", inclusive = false)
	BigDecimal price,

	@DecimalMin(value = "0", inclusive = false)
	BigDecimal quantity,

	@DecimalMin(value = "0", inclusive = false)
	BigDecimal quoteQty,

	@NotNull
	@PastOrPresent
	Instant orderedAt
) {
	public PlaceOrderCommand toCommand() {
		return new PlaceOrderCommand(
			accountId,
			clientOrderId,
			baseAsset,
			quoteAsset,
			side,
			orderType,
			tif,
			price,
			quantity,
			quoteQty,
			orderedAt
		);
	}
}
