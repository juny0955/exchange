use std::time::Duration;

use crossbeam::channel;
use rust_decimal::Decimal;
use uuid::Uuid;

use matching_engine::engine::{CancelReason, EngineEvent, EngineManager, OrderCommand};
use matching_engine::models::{
    AccountId, Order, OrderId, OrderKind, Price, Quantity, QuoteQty, Side, Symbol, TimeInForce,
};

fn btc_krw() -> Symbol {
    Symbol {
        base_asset: "BTC".into(),
        quote_asset: "KRW".into(),
    }
}

fn limit_order(symbol: Symbol, side: Side, price: i64, qty: i64, tif: TimeInForce) -> Order {
    Order {
        order_id: OrderId::new(Uuid::new_v4()),
        account_id: AccountId::new(Uuid::new_v4()),
        symbol,
        side,
        kind: OrderKind::Limit {
            price: Price::new(Decimal::from(price)),
            quantity: Quantity::new(Decimal::from(qty)),
            tif,
        },
        filled_qty: Quantity::zero(),
        filled_quote_qty: QuoteQty::zero(),
    }
}

fn setup() -> (EngineManager, crossbeam::channel::Receiver<EngineEvent>) {
    let (event_tx, event_rx) = channel::unbounded::<EngineEvent>();
    let mut manager = EngineManager::new(event_tx);
    manager.register_symbol(btc_krw());
    (manager, event_rx)
}

#[test]
fn test_limit_buy_sell_fully_matched() {
    let (manager, event_rx) = setup();

    manager.submit(OrderCommand::Place(limit_order(
        btc_krw(),
        Side::Sell,
        50_000_000,
        1,
        TimeInForce::Gtc,
    ))).unwrap();
    manager.submit(OrderCommand::Place(limit_order(
        btc_krw(),
        Side::Buy,
        50_000_000,
        1,
        TimeInForce::Gtc,
    ))).unwrap();

    let event = event_rx
        .recv_timeout(Duration::from_millis(200))
        .expect("체결 이벤트가 오지 않음");

    match event {
        EngineEvent::Matched(trades) => {
            assert_eq!(trades.len(), 1);
            let trade = &trades[0];
            assert_eq!(trade.price.value(), Decimal::from(50_000_000));
            assert_eq!(trade.quantity.value(), Decimal::from(1));
        }
        other => panic!("예상치 못한 이벤트: {:?}", other),
    }
}

#[test]
fn test_ioc_no_liquidity_canceled() {
    let (manager, event_rx) = setup();

    manager.submit(OrderCommand::Place(limit_order(
        btc_krw(),
        Side::Buy,
        50_000_000,
        1,
        TimeInForce::Ioc,
    ))).unwrap();

    let event = event_rx
        .recv_timeout(Duration::from_millis(200))
        .expect("취소 이벤트가 오지 않음");

    match event {
        EngineEvent::Canceled { reason, .. } => {
            assert!(matches!(reason, CancelReason::IocExpired));
        }
        other => panic!("예상치 못한 이벤트: {:?}", other),
    }
}

#[test]
fn test_partial_fill_then_rest_matched() {
    let (manager, event_rx) = setup();

    // 매도 qty=1
    manager.submit(OrderCommand::Place(limit_order(
        btc_krw(),
        Side::Sell,
        50_000_000,
        1,
        TimeInForce::Gtc,
    ))).unwrap();
    // 매수 qty=2 — 1체결, 나머지 1 book 대기
    manager.submit(OrderCommand::Place(limit_order(
        btc_krw(),
        Side::Buy,
        50_000_000,
        2,
        TimeInForce::Gtc,
    ))).unwrap();

    let first = event_rx
        .recv_timeout(Duration::from_millis(200))
        .expect("첫 번째 체결 이벤트가 오지 않음");

    match first {
        EngineEvent::Matched(trades) => {
            assert_eq!(trades.len(), 1);
            assert_eq!(trades[0].quantity.value(), Decimal::from(1));
        }
        other => panic!("예상치 못한 이벤트: {:?}", other),
    }

    // 추가 매도로 나머지 qty=1 체결
    manager.submit(OrderCommand::Place(limit_order(
        btc_krw(),
        Side::Sell,
        50_000_000,
        1,
        TimeInForce::Gtc,
    ))).unwrap();

    let second = event_rx
        .recv_timeout(Duration::from_millis(200))
        .expect("두 번째 체결 이벤트가 오지 않음");

    match second {
        EngineEvent::Matched(trades) => {
            assert_eq!(trades.len(), 1);
            assert_eq!(trades[0].quantity.value(), Decimal::from(1));
        }
        other => panic!("예상치 못한 이벤트: {:?}", other),
    }
}
