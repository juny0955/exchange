package dev.junyoung.exchange.orderservice.application.port.in.engine;

import dev.junyoung.exchange.orderservice.application.port.in.command.EngineMatchedCommand;

public interface HandleEngineMatchedEventUseCase {
	void handle(EngineMatchedCommand command);
}
