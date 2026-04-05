package dev.junyoung.exchange.orderservice.adapter.in.api.response;

import java.util.UUID;

public record PlaceOrderResponse(
	UUID orderId
) {
}
