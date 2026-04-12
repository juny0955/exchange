package dev.junyoung.exchange.orderservice.adapter.out.persistence;

import org.jooq.DSLContext;

import dev.junyoung.exchange.orderservice.Tables;
import dev.junyoung.exchange.orderservice.domain.model.entity.Trade;
import dev.junyoung.exchange.orderservice.tables.records.TradesRecord;

public final class JooqTradeMapper {

	public static TradesRecord toRecord(DSLContext dslContext, Trade trade) {
		TradesRecord record = dslContext.newRecord(Tables.TRADES);
		record.setTradeId(trade.tradeId().value());
		record.setOrderId(trade.orderId().value());
		record.setMatchOrderId(trade.matchOrderId().value());
		record.setBaseAsset(trade.symbol().baseAsset());
		record.setQuoteAsset(trade.symbol().quoteAsset());
		record.setSide(trade.side().name());
		record.setPrice(trade.price().value());
		record.setQuantity(trade.quantity().value());
		record.setQuoteQty(trade.quoteQty().value());
		record.setTradeAt(trade.tradeAt());
		record.setCreatedAt(trade.createdAt());
		return record;
	}
}
