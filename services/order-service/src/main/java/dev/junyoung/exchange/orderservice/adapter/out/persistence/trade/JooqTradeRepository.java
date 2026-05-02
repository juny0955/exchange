package dev.junyoung.exchange.orderservice.adapter.out.persistence.trade;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import dev.junyoung.exchange.orderservice.Tables;
import dev.junyoung.exchange.orderservice.application.port.out.TradeRepository;
import dev.junyoung.exchange.orderservice.domain.model.entity.Trade;
import dev.junyoung.exchange.orderservice.domain.model.value.TradeId;
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

	@Override
	public Set<TradeId> findExistingTradeIds(List<TradeId> tradeIds) {
		if (tradeIds.isEmpty()) return Set.of();

		List<UUID> ids = tradeIds.stream()
			.map(TradeId::value)
			.toList();

		return dslContext.selectDistinct(Tables.TRADES.TRADE_ID)
			.from(Tables.TRADES)
			.where(Tables.TRADES.TRADE_ID.in(ids))
			.fetch()
			.stream()
			.map(r -> new TradeId(r.value1()))
			.collect(Collectors.toUnmodifiableSet());
	}
}
