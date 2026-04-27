package dev.junyoung.exchange.orderservice.application.port.in.engine;

import dev.junyoung.exchange.orderservice.application.port.in.command.EngineMatchedCommand;

import java.util.List;

public interface HandleEngineMatchedEventUseCase {
	void handle(List<EngineMatchedCommand> commands);
}
