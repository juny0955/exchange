use rust_decimal::Decimal;

use crate::engine::event::{CancelReason, EngineEvent};
use crate::engine::orderbook::OrderBook;
use crate::models::{
    AccountId, Order, OrderId, OrderType, Price, Quantity, QuoteQty, Side, TimeInForce, Trade,
};

struct MakerInfo {
    order_id: OrderId,
    account_id: AccountId,
    price: Price,
    remaining_qty: Quantity,
}
impl MakerInfo {
    fn new(order: &Order) -> Self {
        Self {
            order_id: order.order_id,
            account_id: order.account_id,
            price: order.price.unwrap(),
            remaining_qty: order.remaining_qty(),
        }
    }
}

pub struct Matcher;
impl Matcher {
    pub fn match_order(order: Order, book: &mut OrderBook) -> Vec<EngineEvent> {
        if order.order_type == OrderType::Market {
            match order.side {
                Side::Buy => Self::process_market_buy(order, book),
                Side::Sell => Self::process_market_sell(order, book),
            }
        } else {
            match order.tif {
                TimeInForce::Gtc => Self::process_match(order, book, true),
                TimeInForce::Ioc => Self::process_match(order, book, false),
                TimeInForce::Fok => Self::process_limit_fok(order, book),
            }
        }
    }

    /// GTC/IOC 공통 매칭 루프
    ///
    /// resting=true 일시 미체결 잔량 호가 등록
    fn process_match(order: Order, book: &mut OrderBook, resting: bool) -> Vec<EngineEvent> {
        let mut taker = order;
        let mut trades = Vec::new();

        while let Some(maker) = Self::get_maker_info(taker.side.opposite(), book) {
            if !Self::can_match(&taker, maker.price) {
                break;
            }

            let trade = Self::execute_fill(&mut taker, maker, book);
            trades.push(trade);

            if taker.is_fully_filled() {
                return vec![EngineEvent::Matched(trades)];
            }
        }

        let mut events = Vec::new();
        if !trades.is_empty() {
            events.push(EngineEvent::Matched(trades));
        }

        if resting {
            book.add(taker);
        } else {
            events.push(EngineEvent::Canceled {
                order_id: taker.order_id,
                reason: CancelReason::IocExpired,
            });
        }

        events
    }

    /// LIMIT 주문 FOK 전용
    fn process_limit_fok(order: Order, book: &mut OrderBook) -> Vec<EngineEvent> {
        let price = order.price.unwrap().value();
        let required = order.quantity.unwrap().value();

        if !book.can_fully_fill(order.side, price, required) {
            return vec![EngineEvent::Canceled {
                order_id: order.order_id,
                reason: CancelReason::FokExpired,
            }];
        }
        // 사전 검사 통과로 전량 체결 보장
        Self::process_match(order, book, false)
    }

    /// 시장가 매수(FOK) 전용
    ///
    /// 금액 기준 전량 체결 검증 후 처리
    fn process_market_buy(order: Order, book: &mut OrderBook) -> Vec<EngineEvent> {
        let required_quote = order.quote_qty.unwrap();

        if !book.can_fully_fill_quote(required_quote.value()) {
            return vec![EngineEvent::Canceled {
                order_id: order.order_id,
                reason: CancelReason::FokExpired,
            }];
        }

        let mut taker = order;
        let mut trades = Vec::new();
        let mut remaining_quote = required_quote;

        while remaining_quote > QuoteQty::zero() {
            let Some(maker) = Self::get_maker_info(Side::Sell, book) else {
                break;
            };

            let max_qty = Quantity::new(remaining_quote.value() / maker.price.value());
            let fill_qty = maker.remaining_qty.min(max_qty);
            let fill_quote = QuoteQty::new(maker.price.value() * fill_qty.value());

            book.fill(&maker.order_id, fill_qty);
            taker.fill(fill_qty, fill_quote);
            remaining_quote = remaining_quote.sub(fill_quote);

            trades.push(Trade::new(
                taker.symbol.clone(),
                taker.account_id,
                taker.order_id,
                maker.account_id,
                maker.order_id,
                maker.price,
                fill_qty,
                fill_quote,
            ));
        }

        vec![EngineEvent::Matched(trades)]
    }

    /// 시장가 매도(FOK) 전용
    fn process_market_sell(order: Order, book: &mut OrderBook) -> Vec<EngineEvent> {
        let required = order.quantity.unwrap().value();

        if !book.can_fully_fill(order.side, Decimal::ZERO, required) {
            return vec![EngineEvent::Canceled {
                order_id: order.order_id,
                reason: CancelReason::FokExpired,
            }];
        }
        // 사전 검사 통과로 전량 체결 보장
        Self::process_match(order, book, false)
    }

    // 반대편 최우선 호가 MakerInfo로 변환
    fn get_maker_info(side: Side, book: &mut OrderBook) -> Option<MakerInfo> {
        match side {
            Side::Buy => book.best_bid(),
            Side::Sell => book.best_ask(),
        }
        .map(MakerInfo::new)
    }

    // 체결 가능 여부 확인 - 시장가는 항상 true
    fn can_match(taker: &Order, maker_price: Price) -> bool {
        match taker.price {
            Some(taker_price) => match taker.side {
                Side::Buy => taker_price >= maker_price,
                Side::Sell => taker_price <= maker_price,
            },
            None => true,
        }
    }

    // 단일 체결 실행 - taker/maker 수량 차감 및 Trade 생성
    fn execute_fill(taker: &mut Order, maker: MakerInfo, book: &mut OrderBook) -> Trade {
        let fill_qty = maker.remaining_qty.min(taker.remaining_qty());
        let quote_qty = QuoteQty::new(maker.price.value() * fill_qty.value());

        book.fill(&maker.order_id, fill_qty);
        taker.fill(fill_qty, quote_qty);

        let (buy_account_id, buy_order_id, sell_account_id, sell_order_id) = match taker.side {
            Side::Buy => (
                taker.account_id,
                taker.order_id,
                maker.account_id,
                maker.order_id,
            ),
            Side::Sell => (
                maker.account_id,
                maker.order_id,
                taker.account_id,
                taker.order_id,
            ),
        };

        Trade::new(
            taker.symbol.clone(),
            buy_account_id,
            buy_order_id,
            sell_account_id,
            sell_order_id,
            maker.price,
            fill_qty,
            quote_qty,
        )
    }
}
