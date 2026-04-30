package dev.junyoung.exchange.accountservice.application.port.in.command;

public record SaveEngineFailedEventCommand(
    String topic,
    int partition,
    long offset,
    String payload,
    String errorMessage
) {
}
