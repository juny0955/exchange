package dev.junyoung.exchange.orderservice.adapter.in.event.annotation;

import dev.junyoung.exchange.orderservice.application.exception.OrderNotFoundException;
import dev.junyoung.exchange.orderservice.domain.exception.OrderInvalidException;
import dev.junyoung.exchange.orderservice.domain.exception.OrderStateConflictException;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.RetryableTopic;
import tools.jackson.core.JacksonException;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RetryableTopic(
    attempts = "3",
    backOff = @BackOff(delay = 1000, multiplier = 2),
    exclude = {
        OrderNotFoundException.class,
        OrderInvalidException.class,
        OrderStateConflictException.class,
        IllegalArgumentException.class,
        JacksonException.class
    },
    dltTopicSuffix = ".dlt"
)
public @interface DefaultRetryableTopic {
}
