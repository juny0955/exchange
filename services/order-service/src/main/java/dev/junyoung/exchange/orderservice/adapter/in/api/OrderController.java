package dev.junyoung.exchange.orderservice.adapter.in.api;

import dev.junyoung.exchange.orderservice.application.port.in.CancelOrderUseCase;
import dev.junyoung.exchange.orderservice.application.port.in.command.CancelOrderCommand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.junyoung.exchange.orderservice.adapter.in.api.requests.PlaceOrderRequest;
import dev.junyoung.exchange.orderservice.adapter.in.api.response.PlaceOrderResponse;
import dev.junyoung.exchange.orderservice.application.port.in.PlaceOrderUseCase;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

	private final PlaceOrderUseCase placeOrderUseCase;
	private final CancelOrderUseCase cancelOrderUseCase;

	@PostMapping
	public ResponseEntity<PlaceOrderResponse> placeOrder(
		@RequestBody @Valid PlaceOrderRequest request
	) {
		OrderId orderId = placeOrderUseCase.placeOrder(request.toCommand());
		return ResponseEntity.accepted()
			.body(new PlaceOrderResponse(orderId.value()));
	}

	@DeleteMapping("/{orderId}")
	public ResponseEntity<Void> cancelOrder(
		@PathVariable UUID orderId,
		@RequestParam UUID accountId
	) {
		cancelOrderUseCase.cancelOrder(new CancelOrderCommand(orderId, accountId));
		return ResponseEntity.accepted()
			.build();
	}
}
