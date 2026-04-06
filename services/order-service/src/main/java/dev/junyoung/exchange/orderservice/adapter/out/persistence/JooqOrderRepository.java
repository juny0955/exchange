package dev.junyoung.exchange.orderservice.adapter.out.persistence;

import dev.junyoung.exchange.orderservice.Tables;
import dev.junyoung.exchange.orderservice.application.port.out.OrderRepository;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.value.AccountId;
import dev.junyoung.exchange.orderservice.tables.records.OrdersRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JooqOrderRepository implements OrderRepository {

    private final DSLContext dslContext;

    @Override
    public void save(Order order) {
        OrdersRecord record = JooqOrderMapper.toRecord(dslContext, order);

        dslContext.insertInto(Tables.ORDERS)
            .set(record)
            .onConflict(Tables.ORDERS.ORDER_ID)
            .doUpdate()
            .set(Tables.ORDERS.STATUS, record.getStatus())
            .set(Tables.ORDERS.CUM_BASE_QTY, record.getCumBaseQty())
            .set(Tables.ORDERS.CUM_QUOTE_QTY, record.getCumQuoteQty())
            .set(Tables.ORDERS.UPDATED_AT, record.getUpdatedAt())
            .execute();
    }

    @Override
    public boolean existsByAccountIdAndClientOrderId(AccountId accountId, String clientOrderId) {
        return dslContext.fetchExists(
            dslContext.selectOne()
            .from(Tables.ORDERS)
            .where(Tables.ORDERS.ACCOUNT_ID.eq(accountId.value()))
            .and(Tables.ORDERS.CLIENT_ORDER_ID.eq(clientOrderId))
        );
    }
}
