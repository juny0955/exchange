package dev.junyoung.exchange.orderservice.adapter.in.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.junyoung.exchange.orderservice.adapter.in.api.requests.PlaceOrderRequest;
import dev.junyoung.exchange.orderservice.adapter.in.api.response.PlaceOrderResponse;
import dev.junyoung.exchange.orderservice.application.port.in.PlaceOrderUseCase;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

	private final PlaceOrderUseCase placeOrderUseCase;

	@PostMapping
	public ResponseEntity<PlaceOrderResponse> placeOrder(
		@RequestBody @Valid PlaceOrderRequest request
	) {
		OrderId orderId = placeOrderUseCase.placeOrder(request.toCommand());
		return ResponseEntity.accepted()
			.body(new PlaceOrderResponse(orderId.value()));
	}
}
