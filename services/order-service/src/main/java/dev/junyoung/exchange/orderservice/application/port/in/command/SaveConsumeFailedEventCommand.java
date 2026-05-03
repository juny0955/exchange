package dev.junyoung.exchange.orderservice.application.port.in.command;

public record SaveConsumeFailedEventCommand(
    String topic,
    int partition,
    long offset,
    String payload,
    String errorMessage
) {
}
