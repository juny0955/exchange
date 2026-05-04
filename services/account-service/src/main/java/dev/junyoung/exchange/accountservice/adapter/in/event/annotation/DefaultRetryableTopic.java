package dev.junyoung.exchange.accountservice.adapter.in.event.annotation;

import dev.junyoung.exchange.accountservice.application.exception.AccountInactiveException;
import dev.junyoung.exchange.accountservice.application.exception.ReservationNotFoundException;
import dev.junyoung.exchange.accountservice.domain.exception.AccountStateConflictException;
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
        ReservationNotFoundException.class,
        AccountStateConflictException.class,
        AccountInactiveException.class,
        IllegalArgumentException.class,
        JacksonException.class
    },
    dltTopicSuffix = ".dlt"
)
public @interface DefaultRetryableTopic {
}
