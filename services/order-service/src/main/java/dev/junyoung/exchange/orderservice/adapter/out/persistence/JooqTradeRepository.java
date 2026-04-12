package dev.junyoung.exchange.orderservice.adapter.out.persistence;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import dev.junyoung.exchange.orderservice.application.port.out.TradeRepository;
import dev.junyoung.exchange.orderservice.domain.model.entity.Trade;
import dev.junyoung.exchange.orderservice.tables.records.TradesRecord;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JooqTradeRepository implements TradeRepository {

	private final DSLContext dslContext;

	@Override
	public void saveAll(List<Trade> trades) {
		List<TradesRecord> records = trades.stream()
			.map(trade -> JooqTradeMapper.toRecord(dslContext, trade))
			.toList();

		dslContext.batchInsert(records).execute();
	}
}
