package dev.junyoung.exchange.orderservice.adapter.out.persistence.order;

import dev.junyoung.exchange.orderservice.Tables;
import dev.junyoung.exchange.orderservice.application.port.out.OrderRepository;
import dev.junyoung.exchange.orderservice.domain.model.entity.Order;
import dev.junyoung.exchange.orderservice.domain.model.value.AccountId;
import dev.junyoung.exchange.orderservice.domain.model.value.OrderId;
import dev.junyoung.exchange.orderservice.tables.records.OrdersRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JooqOrderRepository implements OrderRepository {

    private final DSLContext dslContext;

    @Override
    public void save(Order order) {
        dslContext.insertInto(Tables.ORDERS)
            .set(JooqOrderMapper.toRecord(dslContext, order))
            .execute();
    }

    @Override
    public void updateStatus(Order order) {
        dslContext.update(Tables.ORDERS)
            .set(Tables.ORDERS.STATUS, order.getStatus().name())
            .set(Tables.ORDERS.UPDATED_AT, order.getUpdatedAt())
            .where(Tables.ORDERS.ORDER_ID.eq(order.getOrderId().value()))
            .execute();
    }

    @Override
    public void updateFill(List<Order> orders) {
        List<OrdersRecord> records = orders.stream()
            .map(order -> {
                OrdersRecord record = JooqOrderMapper.toRecord(dslContext, order);
                record.changed(false);
                record.changed(Tables.ORDERS.CUM_BASE_QTY, true);
                record.changed(Tables.ORDERS.CUM_QUOTE_QTY, true);
                record.changed(Tables.ORDERS.STATUS, true);
                record.changed(Tables.ORDERS.UPDATED_AT, true);
                return record;
            })
            .toList();

        dslContext.batchUpdate(records).execute();
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

    @Override
    public Optional<Order> findByIdAndAccountIdForUpdate(OrderId orderId, AccountId accountId) {
        return Optional.ofNullable(
            dslContext.selectFrom(Tables.ORDERS)
                .where(Tables.ORDERS.ORDER_ID.eq(orderId.value()))
                .and(Tables.ORDERS.ACCOUNT_ID.eq(accountId.value()))
                .forUpdate()
                .fetchOne(JooqOrderMapper::toDomain)
        );
    }

    @Override
    public List<Order> findAllByIdForUpdate(List<OrderId> orderIds) {
        List<UUID> ids = orderIds.stream()
            .map(OrderId::value)
            .toList();

        return dslContext.selectFrom(Tables.ORDERS)
            .where(Tables.ORDERS.ORDER_ID.in(ids))
            .orderBy(Tables.ORDERS.ORDER_ID.asc())
            .forUpdate()
            .fetch(JooqOrderMapper::toDomain);
    }
}
