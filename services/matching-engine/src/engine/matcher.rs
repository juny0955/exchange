use crate::engine::EngineError;
use crate::engine::event::{CancelReason, EngineEvent};
use crate::engine::orderbook::OrderBook;
use crate::models::{
    AccountId, Order, OrderId, OrderKind, Price, Quantity, QuoteQty, Side, TimeInForce, Trade,
    TradeParticipant,
};

struct MakerInfo {
    order_id: OrderId,
    account_id: AccountId,
    price: Price,
    remaining_qty: Quantity,
}
impl MakerInfo {
    fn new(order: &Order) -> Option<Self> {
        let price = match &order.kind {
            OrderKind::Limit { price, .. } => *price,
            _ => return None,
        };

        Some(Self {
            order_id: order.order_id,
            account_id: order.account_id,
            price,
            remaining_qty: order.remaining_qty(),
        })
    }

    fn as_participant(&self) -> TradeParticipant {
        TradeParticipant {
            account_id: self.account_id,
            order_id: self.order_id,
        }
    }
}

pub struct Matcher;
impl Matcher {
    pub fn match_order(order: Order, book: &mut OrderBook) -> Vec<EngineEvent> {
        match order.kind {
            OrderKind::MarketBuy { .. } => Self::process_market_buy(order, book),
            OrderKind::MarketSell { .. } => Self::process_market_sell(order, book),
            OrderKind::Limit { tif, .. } => match tif {
                TimeInForce::Gtc => Self::process_match(order, book, true),
                TimeInForce::Ioc => Self::process_match(order, book, false),
                TimeInForce::Fok => Self::process_limit_fok(order, book),
            },
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
                account_id: taker.account_id,
                reason: CancelReason::IocExpired,
            });
        }

        events
    }

    /// LIMIT 주문 FOK 전용
    fn process_limit_fok(order: Order, book: &mut OrderBook) -> Vec<EngineEvent> {
        let (price, required) = match &order.kind {
            OrderKind::Limit {
                price, quantity, ..
            } => (*price, *quantity),
            _ => return Self::invalid_order_kind(&order),
        };

        if !book.can_fully_fill(order.side, price, required) {
            return vec![EngineEvent::Canceled {
                order_id: order.order_id,
                account_id: order.account_id,
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
        let required_quote = match &order.kind {
            OrderKind::MarketBuy { quote_qty } => *quote_qty,
            _ => return Self::invalid_order_kind(&order),
        };

        if !book.can_fully_fill_quote(required_quote) {
            return vec![EngineEvent::Canceled {
                order_id: order.order_id,
                account_id: order.account_id,
                reason: CancelReason::FokExpired,
            }];
        }

        let mut taker = order;
        let mut trades = Vec::new();

        while !taker.is_fully_filled() {
            let Some(maker) = Self::get_maker_info(Side::Sell, book) else {
                break;
            };

            let Some((fill_qty, fill_quote)) =
                Self::market_buy_fill(required_quote - taker.filled_quote_qty, &maker)
            else {
                break;
            };

            book.fill(&maker.order_id, fill_qty);
            taker.fill(fill_qty, fill_quote);

            trades.push(Self::make_trade(&taker, maker, fill_qty, fill_quote));
        }

        vec![EngineEvent::Matched(trades)]
    }

    /// 시장가 매도(FOK) 전용
    fn process_market_sell(order: Order, book: &mut OrderBook) -> Vec<EngineEvent> {
        let required = match &order.kind {
            OrderKind::MarketSell { quantity } => *quantity,
            _ => return Self::invalid_order_kind(&order),
        };

        if !book.can_fully_fill(order.side, Price::zero(), required) {
            return vec![EngineEvent::Canceled {
                order_id: order.order_id,
                account_id: order.account_id,
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
        .and_then(MakerInfo::new)
    }

    // 체결 가능 여부 확인 - 시장가는 항상 true
    fn can_match(taker: &Order, maker_price: Price) -> bool {
        match taker.kind {
            OrderKind::Limit {
                price: taker_price, ..
            } => match taker.side {
                Side::Buy => taker_price >= maker_price,
                Side::Sell => taker_price <= maker_price,
            },
            _ => true,
        }
    }

    // 단일 체결 실행 - taker/maker 수량 차감 및 Trade 생성
    fn market_buy_fill(
        remaining_quote: QuoteQty,
        maker: &MakerInfo,
    ) -> Option<(Quantity, QuoteQty)> {
        let affordable_qty = (remaining_quote / maker.price).floor();
        if affordable_qty.is_zero() {
            return None;
        }

        let fill_qty = maker.remaining_qty.min(affordable_qty);
        let fill_quote = maker.price * fill_qty;
        Some((fill_qty, fill_quote))
    }

    fn execute_fill(taker: &mut Order, maker: MakerInfo, book: &mut OrderBook) -> Trade {
        let fill_qty = maker.remaining_qty.min(taker.remaining_qty());
        let fill_quote = maker.price * fill_qty;

        book.fill(&maker.order_id, fill_qty);
        taker.fill(fill_qty, fill_quote);

        Self::make_trade(taker, maker, fill_qty, fill_quote)
    }

    fn make_trade(
        taker: &Order,
        maker: MakerInfo,
        fill_qty: Quantity,
        fill_quote: QuoteQty,
    ) -> Trade {
        Trade::new(
            taker.symbol.clone(),
            taker.side,
            taker.as_participant(),
            maker.as_participant(),
            maker.price,
            fill_qty,
            fill_quote,
        )
    }

    fn invalid_order_kind(order: &Order) -> Vec<EngineEvent> {
        vec![EngineEvent::Rejected {
            order_id: order.order_id,
            account_id: order.account_id,
            reason: EngineError::InvlidOrderKind,
        }]
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::engine::orderbook::OrderBook;
    use crate::models::{AccountId, OrderKind, Price, Quantity, QuoteQty, Symbol, TimeInForce};
    use rust_decimal::Decimal;
    use uuid::Uuid;

    fn btc_usdt() -> Symbol {
        Symbol {
            base_asset: "BTC".into(),
            quote_asset: "USDT".into(),
        }
    }

    fn limit_order(side: Side, price: i64, qty: i64, tif: TimeInForce) -> Order {
        Order {
            order_id: OrderId::new(Uuid::new_v4()),
            account_id: AccountId::new(Uuid::new_v4()),
            symbol: btc_usdt(),
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

    fn market_buy_order(quote: i64) -> Order {
        Order {
            order_id: OrderId::new(Uuid::new_v4()),
            account_id: AccountId::new(Uuid::new_v4()),
            symbol: btc_usdt(),
            side: Side::Buy,
            kind: OrderKind::MarketBuy {
                quote_qty: QuoteQty::new(Decimal::from(quote)),
            },
            filled_qty: Quantity::zero(),
            filled_quote_qty: QuoteQty::zero(),
        }
    }

    fn market_sell_order(qty: i64) -> Order {
        Order {
            order_id: OrderId::new(Uuid::new_v4()),
            account_id: AccountId::new(Uuid::new_v4()),
            symbol: btc_usdt(),
            side: Side::Sell,
            kind: OrderKind::MarketSell {
                quantity: Quantity::new(Decimal::from(qty)),
            },
            filled_qty: Quantity::zero(),
            filled_quote_qty: QuoteQty::zero(),
        }
    }

    fn matched_trades(events: Vec<EngineEvent>) -> Vec<Trade> {
        events
            .into_iter()
            .find_map(|e| match e {
                EngineEvent::Matched(trades) => Some(trades),
                _ => None,
            })
            .unwrap_or_default()
    }

    fn cancel_reason(events: &[EngineEvent]) -> Option<&CancelReason> {
        events.iter().find_map(|e| match e {
            EngineEvent::Canceled { reason, .. } => Some(reason),
            _ => None,
        })
    }

    // ── GTC ──────────────────────────────────────────────────────────────────

    #[test]
    fn gtc_buy_fully_matches_resting_ask() {
        let mut book = OrderBook::new();
        book.add(limit_order(Side::Sell, 100, 5, TimeInForce::Gtc));

        let events =
            Matcher::match_order(limit_order(Side::Buy, 100, 5, TimeInForce::Gtc), &mut book);

        let trades = matched_trades(events);
        assert_eq!(trades.len(), 1);
        assert_eq!(trades[0].price, Price::new(Decimal::from(100)));
        assert_eq!(trades[0].quantity, Quantity::new(Decimal::from(5)));
        assert!(book.best_ask().is_none());
    }

    #[test]
    fn gtc_buy_partial_fill_rests_remainder_in_book() {
        let mut book = OrderBook::new();
        book.add(limit_order(Side::Sell, 100, 3, TimeInForce::Gtc));

        let events =
            Matcher::match_order(limit_order(Side::Buy, 100, 5, TimeInForce::Gtc), &mut book);

        let trades = matched_trades(events);
        assert_eq!(trades[0].quantity, Quantity::new(Decimal::from(3)));
        assert!(book.best_bid().is_some());
        assert!(book.best_ask().is_none());
    }

    #[test]
    fn gtc_buy_price_miss_rests_in_book_no_trade() {
        let mut book = OrderBook::new();
        book.add(limit_order(Side::Sell, 200, 5, TimeInForce::Gtc));

        let events =
            Matcher::match_order(limit_order(Side::Buy, 100, 5, TimeInForce::Gtc), &mut book);

        assert!(matched_trades(events).is_empty());
        assert!(book.best_bid().is_some());
    }

    // ── IOC ──────────────────────────────────────────────────────────────────

    #[test]
    fn ioc_buy_fully_matches() {
        let mut book = OrderBook::new();
        book.add(limit_order(Side::Sell, 100, 5, TimeInForce::Gtc));

        let events =
            Matcher::match_order(limit_order(Side::Buy, 100, 5, TimeInForce::Ioc), &mut book);

        assert_eq!(matched_trades(events).len(), 1);
    }

    #[test]
    fn ioc_buy_partial_fill_cancels_remainder() {
        let mut book = OrderBook::new();
        book.add(limit_order(Side::Sell, 100, 3, TimeInForce::Gtc));

        let events =
            Matcher::match_order(limit_order(Side::Buy, 100, 5, TimeInForce::Ioc), &mut book);

        assert!(events.iter().any(|e| matches!(e, EngineEvent::Matched(_))));
        assert!(matches!(
            cancel_reason(&events),
            Some(CancelReason::IocExpired)
        ));
        assert!(book.best_bid().is_none());
    }

    #[test]
    fn ioc_buy_no_match_immediately_canceled() {
        let mut book = OrderBook::new();
        book.add(limit_order(Side::Sell, 200, 5, TimeInForce::Gtc));

        let events =
            Matcher::match_order(limit_order(Side::Buy, 100, 5, TimeInForce::Ioc), &mut book);

        assert!(matches!(
            cancel_reason(&events),
            Some(CancelReason::IocExpired)
        ));
        assert!(book.best_bid().is_none());
    }

    // ── Limit FOK ────────────────────────────────────────────────────────────

    #[test]
    fn limit_fok_buy_fully_fills() {
        let mut book = OrderBook::new();
        book.add(limit_order(Side::Sell, 100, 5, TimeInForce::Gtc));

        let events =
            Matcher::match_order(limit_order(Side::Buy, 100, 5, TimeInForce::Fok), &mut book);

        let trades = matched_trades(events);
        assert_eq!(trades.len(), 1);
        assert_eq!(trades[0].quantity, Quantity::new(Decimal::from(5)));
    }

    #[test]
    fn limit_fok_buy_insufficient_qty_canceled() {
        let mut book = OrderBook::new();
        book.add(limit_order(Side::Sell, 100, 3, TimeInForce::Gtc));

        let events =
            Matcher::match_order(limit_order(Side::Buy, 100, 5, TimeInForce::Fok), &mut book);

        assert!(matches!(
            cancel_reason(&events),
            Some(CancelReason::FokExpired)
        ));
    }

    #[test]
    fn limit_fok_buy_price_out_of_range_canceled() {
        let mut book = OrderBook::new();
        book.add(limit_order(Side::Sell, 200, 5, TimeInForce::Gtc));

        let events =
            Matcher::match_order(limit_order(Side::Buy, 100, 5, TimeInForce::Fok), &mut book);

        assert!(matches!(
            cancel_reason(&events),
            Some(CancelReason::FokExpired)
        ));
    }

    // ── Market Buy ───────────────────────────────────────────────────────────

    #[test]
    fn market_buy_fills_with_quote_amount() {
        let mut book = OrderBook::new();
        book.add(limit_order(Side::Sell, 100, 10, TimeInForce::Gtc));

        let events = Matcher::match_order(market_buy_order(500), &mut book);

        let trades = matched_trades(events);
        assert_eq!(trades.len(), 1);
        assert_eq!(trades[0].quote_qty, QuoteQty::new(Decimal::from(500)));
        assert_eq!(trades[0].quantity, Quantity::new(Decimal::from(5)));
    }

    #[test]
    fn market_buy_insufficient_quote_canceled() {
        let mut book = OrderBook::new();
        book.add(limit_order(Side::Sell, 100, 2, TimeInForce::Gtc));

        let events = Matcher::match_order(market_buy_order(500), &mut book);

        assert!(matches!(
            cancel_reason(&events),
            Some(CancelReason::FokExpired)
        ));
    }

    // ── Market Sell ──────────────────────────────────────────────────────────

    #[test]
    fn market_buy_cancels_when_remaining_quote_cannot_buy_next_level() {
        let mut book = OrderBook::new();
        book.add(limit_order(Side::Sell, 100, 5, TimeInForce::Gtc));
        book.add(limit_order(Side::Sell, 200, 10, TimeInForce::Gtc));

        let events = Matcher::match_order(market_buy_order(550), &mut book);

        assert!(!events.iter().any(|e| matches!(e, EngineEvent::Matched(_))));
        assert!(matches!(
            cancel_reason(&events),
            Some(CancelReason::FokExpired)
        ));
    }

    #[test]
    fn market_buy_trades_keep_quote_equal_to_price_times_quantity() {
        let mut book = OrderBook::new();
        book.add(limit_order(Side::Sell, 100, 10, TimeInForce::Gtc));

        let events = Matcher::match_order(market_buy_order(500), &mut book);

        let trades = matched_trades(events);
        assert_eq!(trades.len(), 1);
        assert_eq!(trades[0].quote_qty, trades[0].price * trades[0].quantity,);
    }

    #[test]
    fn market_sell_fully_fills() {
        let mut book = OrderBook::new();
        book.add(limit_order(Side::Buy, 100, 10, TimeInForce::Gtc));

        let events = Matcher::match_order(market_sell_order(5), &mut book);

        let trades = matched_trades(events);
        assert_eq!(trades.len(), 1);
        assert_eq!(trades[0].quantity, Quantity::new(Decimal::from(5)));
        assert_eq!(trades[0].price, Price::new(Decimal::from(100)));
    }

    #[test]
    fn market_sell_insufficient_qty_canceled() {
        let mut book = OrderBook::new();
        book.add(limit_order(Side::Buy, 100, 3, TimeInForce::Gtc));

        let events = Matcher::match_order(market_sell_order(5), &mut book);

        assert!(matches!(
            cancel_reason(&events),
            Some(CancelReason::FokExpired)
        ));
    }

    // ── Trade 계정 매핑 ──────────────────────────────────────────────────────

    #[test]
    fn buy_taker_is_assigned_as_buyer_in_trade() {
        let mut book = OrderBook::new();
        let maker = limit_order(Side::Sell, 100, 5, TimeInForce::Gtc);
        let maker_account = maker.account_id;
        book.add(maker);

        let taker = limit_order(Side::Buy, 100, 5, TimeInForce::Gtc);
        let taker_account = taker.account_id;
        let events = Matcher::match_order(taker, &mut book);

        let trades = matched_trades(events);
        assert_eq!(trades[0].buy_account_id, taker_account);
        assert_eq!(trades[0].sell_account_id, maker_account);
    }

    #[test]
    fn sell_taker_is_assigned_as_seller_in_trade() {
        let mut book = OrderBook::new();
        let maker = limit_order(Side::Buy, 100, 5, TimeInForce::Gtc);
        let maker_account = maker.account_id;
        book.add(maker);

        let taker = limit_order(Side::Sell, 100, 5, TimeInForce::Gtc);
        let taker_account = taker.account_id;
        let events = Matcher::match_order(taker, &mut book);

        let trades = matched_trades(events);
        assert_eq!(trades[0].sell_account_id, taker_account);
        assert_eq!(trades[0].buy_account_id, maker_account);
    }
}
