package dev.junyoung.exchange.orderservice.adapter.out.persistence.history;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import dev.junyoung.exchange.orderservice.Tables;
import dev.junyoung.exchange.orderservice.application.port.out.OrderHistoryRepository;
import dev.junyoung.exchange.orderservice.domain.model.entity.OrderHistory;
import dev.junyoung.exchange.orderservice.tables.records.OrderHistoryRecord;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JooqOrderHistoryRepository implements OrderHistoryRepository {

	private final DSLContext dslContext;

	@Override
	public void save(OrderHistory orderHistory) {
		OrderHistoryRecord record = JooqOrderHistoryMapper.toRecord(dslContext, orderHistory);

		dslContext.insertInto(Tables.ORDER_HISTORY)
			.set(record)
			.execute();
	}

	@Override
	public void saveAll(List<OrderHistory> orderHistories) {
		List<OrderHistoryRecord> records = orderHistories.stream()
			.map(orderHistory -> JooqOrderHistoryMapper.toRecord(dslContext, orderHistory))
			.toList();

		dslContext.batchInsert(records).execute();
	}
}
