use std::time::Duration;

use crossbeam::channel;
use rust_decimal::Decimal;
use uuid::Uuid;

use matching_engine::engine::{
    CancelReason, EngineError, EngineEvent, EngineManager, OrderCommand,
};
use matching_engine::models::{
    AccountId, Order, OrderId, OrderKind, Price, Quantity, QuoteQty, Side, Symbol, TimeInForce,
};

fn btc_krw() -> Symbol {
    Symbol {
        base_asset: "BTC".into(),
        quote_asset: "KRW".into(),
    }
}

fn eth_krw() -> Symbol {
    Symbol {
        base_asset: "ETH".into(),
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

fn recv_event(event_rx: &crossbeam::channel::Receiver<EngineEvent>) -> EngineEvent {
    event_rx
        .recv_timeout(Duration::from_millis(200))
        .expect("expected engine event")
}

fn assert_no_event(event_rx: &crossbeam::channel::Receiver<EngineEvent>) {
    match event_rx.recv_timeout(Duration::from_millis(200)) {
        Ok(event) => panic!("unexpected event: {:?}", event),
        Err(crossbeam::channel::RecvTimeoutError::Timeout) => {}
        Err(err) => panic!("unexpected receive error: {:?}", err),
    }
}

// 동일가 주문이 정상 체결된다
#[test]
fn test_limit_buy_sell_fully_matched() {
    let (manager, event_rx) = setup();

    manager.submit(OrderCommand::Place(limit_order(
        btc_krw(),
        Side::Sell,
        50_000_000,
        1,
        TimeInForce::Gtc,
    )));
    manager.submit(OrderCommand::Place(limit_order(
        btc_krw(),
        Side::Buy,
        50_000_000,
        1,
        TimeInForce::Gtc,
    )));

    match recv_event(&event_rx) {
        EngineEvent::Matched(trades) => {
            assert_eq!(trades.len(), 1);
            let trade = &trades[0];
            assert_eq!(trade.price.value(), Decimal::from(50_000_000));
            assert_eq!(trade.quantity.value(), Decimal::from(1));
        }
        other => panic!("unexpected event: {:?}", other),
    }
}

// 유동성이 없으면 IOC 주문이 취소된다
#[test]
fn test_ioc_no_liquidity_canceled() {
    let (manager, event_rx) = setup();

    manager.submit(OrderCommand::Place(limit_order(
        btc_krw(),
        Side::Buy,
        50_000_000,
        1,
        TimeInForce::Ioc,
    )));

    match recv_event(&event_rx) {
        EngineEvent::Canceled { reason, .. } => {
            assert!(matches!(reason, CancelReason::IocExpired));
        }
        other => panic!("unexpected event: {:?}", other),
    }
}

// 부분 체결 후 남은 수량이 다음 주문과 체결된다
#[test]
fn test_partial_fill_then_rest_matched() {
    let (manager, event_rx) = setup();

    manager.submit(OrderCommand::Place(limit_order(
        btc_krw(),
        Side::Sell,
        50_000_000,
        1,
        TimeInForce::Gtc,
    )));
    manager.submit(OrderCommand::Place(limit_order(
        btc_krw(),
        Side::Buy,
        50_000_000,
        2,
        TimeInForce::Gtc,
    )));

    match recv_event(&event_rx) {
        EngineEvent::Matched(trades) => {
            assert_eq!(trades.len(), 1);
            assert_eq!(trades[0].quantity.value(), Decimal::from(1));
        }
        other => panic!("unexpected event: {:?}", other),
    }

    manager.submit(OrderCommand::Place(limit_order(
        btc_krw(),
        Side::Sell,
        50_000_000,
        1,
        TimeInForce::Gtc,
    )));

    match recv_event(&event_rx) {
        EngineEvent::Matched(trades) => {
            assert_eq!(trades.len(), 1);
            assert_eq!(trades[0].quantity.value(), Decimal::from(1));
        }
        other => panic!("unexpected event: {:?}", other),
    }
}

// 미등록 심볼 주문은 거절된다
#[test]
fn test_unregistered_symbol_rejected() {
    let (manager, event_rx) = setup();

    manager.submit(OrderCommand::Place(limit_order(
        eth_krw(),
        Side::Buy,
        4_000_000,
        1,
        TimeInForce::Gtc,
    )));

    match recv_event(&event_rx) {
        EngineEvent::Rejected { reason, .. } => {
            assert!(matches!(reason, EngineError::SymbolNotFound));
        }
        other => panic!("unexpected event: {:?}", other),
    }
}

// 취소된 주문은 이후 매칭되지 않는다
#[test]
fn test_cancel_existing_order_prevents_later_match() {
    let (manager, event_rx) = setup();
    let resting_buy = limit_order(btc_krw(), Side::Buy, 50_000_000, 1, TimeInForce::Gtc);

    manager.submit(OrderCommand::Place(resting_buy.clone()));
    assert_no_event(&event_rx);

    manager.submit(OrderCommand::Cancel {
        symbol: btc_krw(),
        order_id: resting_buy.order_id,
        account_id: resting_buy.account_id,
    });

    match recv_event(&event_rx) {
        EngineEvent::Canceled {
            order_id, reason, ..
        } => {
            assert_eq!(order_id, resting_buy.order_id);
            assert!(matches!(reason, CancelReason::UserRequest));
        }
        other => panic!("unexpected event: {:?}", other),
    }

    manager.submit(OrderCommand::Place(limit_order(
        btc_krw(),
        Side::Sell,
        50_000_000,
        1,
        TimeInForce::Gtc,
    )));
    assert_no_event(&event_rx);
}

// 없는 주문 취소도 사용자 취소로 처리된다
#[test]
fn test_cancel_unknown_order_still_emits_user_request() {
    let (manager, event_rx) = setup();

    manager.submit(OrderCommand::Cancel {
        symbol: btc_krw(),
        order_id: OrderId::new(Uuid::new_v4()),
        account_id: AccountId::new(Uuid::new_v4()),
    });

    match recv_event(&event_rx) {
        EngineEvent::Canceled { reason, .. } => {
            assert!(matches!(reason, CancelReason::UserRequest));
        }
        other => panic!("unexpected event: {:?}", other),
    }
}

// 체결 완료 주문 취소도 사용자 취소로 처리된다
#[test]
fn test_cancel_filled_order_still_emits_user_request() {
    let (manager, event_rx) = setup();
    let resting_sell = limit_order(btc_krw(), Side::Sell, 50_000_000, 1, TimeInForce::Gtc);
    let aggressive_buy = limit_order(btc_krw(), Side::Buy, 50_000_000, 1, TimeInForce::Gtc);

    manager.submit(OrderCommand::Place(resting_sell.clone()));
    manager.submit(OrderCommand::Place(aggressive_buy));

    match recv_event(&event_rx) {
        EngineEvent::Matched(trades) => assert_eq!(trades.len(), 1),
        other => panic!("unexpected event: {:?}", other),
    }

    manager.submit(OrderCommand::Cancel {
        symbol: btc_krw(),
        order_id: resting_sell.order_id,
        account_id: resting_sell.account_id,
    });

    match recv_event(&event_rx) {
        EngineEvent::Canceled {
            order_id, reason, ..
        } => {
            assert_eq!(order_id, resting_sell.order_id);
            assert!(matches!(reason, CancelReason::UserRequest));
        }
        other => panic!("unexpected event: {:?}", other),
    }
}

// 심볼별 주문장은 서로 독립적으로 동작한다
#[test]
fn test_symbols_route_to_independent_books() {
    let (event_tx, event_rx) = channel::unbounded::<EngineEvent>();
    let mut manager = EngineManager::new(event_tx);
    manager.register_symbol(btc_krw());
    manager.register_symbol(eth_krw());

    manager.submit(OrderCommand::Place(limit_order(
        btc_krw(),
        Side::Sell,
        50_000_000,
        1,
        TimeInForce::Gtc,
    )));
    manager.submit(OrderCommand::Place(limit_order(
        eth_krw(),
        Side::Buy,
        4_000_000,
        1,
        TimeInForce::Gtc,
    )));
    assert_no_event(&event_rx);

    manager.submit(OrderCommand::Place(limit_order(
        btc_krw(),
        Side::Buy,
        50_000_000,
        1,
        TimeInForce::Gtc,
    )));

    match recv_event(&event_rx) {
        EngineEvent::Matched(trades) => {
            assert_eq!(trades.len(), 1);
            assert_eq!(trades[0].symbol.base_asset, "BTC");
        }
        other => panic!("unexpected event: {:?}", other),
    }

    manager.submit(OrderCommand::Place(limit_order(
        eth_krw(),
        Side::Sell,
        4_000_000,
        1,
        TimeInForce::Gtc,
    )));

    match recv_event(&event_rx) {
        EngineEvent::Matched(trades) => {
            assert_eq!(trades.len(), 1);
            assert_eq!(trades[0].symbol.base_asset, "ETH");
        }
        other => panic!("unexpected event: {:?}", other),
    }
}
