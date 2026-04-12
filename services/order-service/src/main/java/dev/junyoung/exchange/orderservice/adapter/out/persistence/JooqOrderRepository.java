package dev.junyoung.exchange.orderservice.adapter.out.persistence;

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

@Repository
@RequiredArgsConstructor
public class JooqOrderRepository implements OrderRepository {

    private final DSLContext dslContext;

    @Override
    public void save(Order order) {
        OrdersRecord record = JooqOrderMapper.toRecord(dslContext, order);

        dslContext.insertInto(Tables.ORDERS)
            .set(record)
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
    public void updateAll(List<Order> orders) {
        List<OrdersRecord> records = orders.stream()
            .map(order -> JooqOrderMapper.toRecord(dslContext, order))
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
}
