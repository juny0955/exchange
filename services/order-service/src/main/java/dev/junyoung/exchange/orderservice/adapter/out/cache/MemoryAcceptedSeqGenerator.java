package dev.junyoung.exchange.orderservice.adapter.out.cache;

import dev.junyoung.exchange.orderservice.application.port.out.AcceptedSeqGenerator;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

// TODO 분산환경 대응하여 구현체 변경 필요
@Component
public class MemoryAcceptedSeqGenerator implements AcceptedSeqGenerator {

    private final AtomicLong sequence = new AtomicLong(0L);

    @Override
    public long next() {
        return sequence.incrementAndGet();
    }
}
