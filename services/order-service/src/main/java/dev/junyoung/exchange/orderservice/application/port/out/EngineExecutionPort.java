package dev.junyoung.exchange.orderservice.application.port.out;

import dev.junyoung.exchange.orderservice.application.port.out.command.EngineCancelCommand;
import dev.junyoung.exchange.orderservice.application.port.out.command.EnginePlaceCommand;

public interface EngineExecutionPort {
    void place(EnginePlaceCommand command);
    void cancel(EngineCancelCommand command);
}
