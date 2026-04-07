package dev.junyoung.exchange.orderservice.adapter.out.engine;

import dev.junyoung.exchange.orderservice.application.port.out.EngineExecutionPort;
import dev.junyoung.exchange.orderservice.application.port.out.command.EngineCancelCommand;
import dev.junyoung.exchange.orderservice.application.port.out.command.EnginePlaceCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// TODO gRPC 구현체 변경 필요
@Component
@Slf4j
public class MockEngineExecutionAdapter implements EngineExecutionPort {

    @Override
    public void place(EnginePlaceCommand command) {
        log.info("Order placed: {}", command);
    }

    @Override
    public void cancel(EngineCancelCommand command) {
        log.info("Order cancelled: {}", command);
    }
}
