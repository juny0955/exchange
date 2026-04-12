package dev.junyoung.exchange.orderservice.application.port.in;

import dev.junyoung.exchange.orderservice.application.port.in.command.EngineMatchedCommand;

public interface HandleEngineMatchedEventUseCase {
	void handle(EngineMatchedCommand command);
}
