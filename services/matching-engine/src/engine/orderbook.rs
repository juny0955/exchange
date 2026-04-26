use crate::models::{Order, OrderId, OrderKind, Price, Quantity, QuoteQty, Side};
use std::{
    cmp::Reverse,
    collections::{BTreeMap, HashMap, VecDeque},
};

pub struct OrderBook {
    bids: BTreeMap<Reverse<Price>, VecDeque<OrderId>>,
    asks: BTreeMap<Price, VecDeque<OrderId>>,
    index: HashMap<OrderId, Order>,
}

impl OrderBook {
    pub fn new() -> Self {
        Self {
            bids: BTreeMap::new(),
            asks: BTreeMap::new(),
            index: HashMap::new(),
        }
    }

    pub fn add(&mut self, order: Order) {
        let order_id = order.order_id;
        let side = order.side;
        let price = match &order.kind {
            OrderKind::Limit { price, .. } => *price,
            _ => unreachable!("only limit orders can rest on the book"),
        };

        self.index.insert(order_id, order);
        match side {
            Side::Buy => {
                self.bids
                    .entry(Reverse(price))
                    .or_default()
                    .push_back(order_id);
            }
            Side::Sell => {
                self.asks.entry(price).or_default().push_back(order_id);
            }
        }
    }

    pub fn cancel(&mut self, order_id: &OrderId) {
        self.index.remove(order_id);
    }

    pub fn best_ask(&mut self) -> Option<&Order> {
        Self::get_best(&mut self.asks, &self.index)
    }

    pub fn best_bid(&mut self) -> Option<&Order> {
        Self::get_best(&mut self.bids, &self.index)
    }

    pub fn fill(&mut self, order_id: &OrderId, qty: Quantity) {
        if let Some(order) = self.index.get_mut(order_id) {
            let price = match &order.kind {
                OrderKind::Limit { price, .. } => price,
                _ => unreachable!("only limit orders can rest on the book"),
            };
            let quote = *price * qty;
            order.fill(qty, quote);

            if order.is_fully_filled() {
                self.index.remove(order_id);
            }
        }
    }

    pub fn can_fully_fill(&self, side: Side, price_limit: Price, required: Quantity) -> bool {
        let mut acc = Quantity::zero();
        match side {
            Side::Buy => {
                for (price, queue) in &self.asks {
                    if *price > price_limit {
                        break;
                    }
                    if self.has_enough_quantity_in_queue(queue, &mut acc, required) {
                        return true;
                    }
                }
            }
            Side::Sell => {
                for (price, queue) in &self.bids {
                    if price.0 < price_limit {
                        break;
                    }
                    if self.has_enough_quantity_in_queue(queue, &mut acc, required) {
                        return true;
                    }
                }
            }
        }
        false
    }

    pub fn can_fully_fill_quote(&self, required: QuoteQty) -> bool {
        let mut remaining = required;
        for (price, queue) in &self.asks {
            if self.has_enough_quote_in_queue(queue, *price, &mut remaining) {
                return true;
            }
        }
        false
    }

    fn get_best<'a, K>(
        book: &mut BTreeMap<K, VecDeque<OrderId>>,
        index: &'a HashMap<OrderId, Order>,
    ) -> Option<&'a Order>
    where
        K: Ord + Copy,
    {
        loop {
            let (price, order_id) = {
                let (price, queue) = book.iter().next()?;
                (*price, *queue.front()?)
            };

            if let Some(order) = index.get(&order_id) {
                return Some(order);
            }

            let queue = book.get_mut(&price).unwrap();
            queue.pop_front();
            if queue.is_empty() {
                book.remove(&price);
            }
        }
    }

    fn has_enough_quantity_in_queue(
        &self,
        queue: &VecDeque<OrderId>,
        acc: &mut Quantity,
        required: Quantity,
    ) -> bool {
        for order_id in queue {
            if let Some(order) = self.index.get(order_id) {
                *acc += order.remaining_qty();
                if *acc >= required {
                    return true;
                }
            }
        }
        false
    }

    fn has_enough_quote_in_queue(
        &self,
        queue: &VecDeque<OrderId>,
        price: Price,
        remaining: &mut QuoteQty,
    ) -> bool {
        for order_id in queue {
            if let Some(order) = self.index.get(order_id) {
                let affordable_qty = *remaining / price;
                if affordable_qty.is_zero() {
                    return false;
                }

                // TODO: 심볼별 lot_size가 도입되면 실제 체결 로직과 같은 수량 정규화를 공유해야 한다.
                if order.remaining_qty() >= affordable_qty {
                    return true;
                }

                *remaining -= order.remaining_qty() * price;
            }
        }
        false
    }
}

#[cfg(test)]
mod tests {
    use super::OrderBook;
    use crate::models::{
        AccountId, Order, OrderId, OrderKind, Price, Quantity, QuoteQty, Side, Symbol, TimeInForce,
    };
    use rust_decimal::Decimal;
    use uuid::Uuid;

    fn make_order(id: Uuid, price: i64, qty: i64, side: Side) -> Order {
        Order {
            order_id: OrderId::new(id),
            account_id: AccountId::new(Uuid::new_v4()),
            symbol: Symbol {
                base_asset: "BTC".into(),
                quote_asset: "USDT".into(),
            },
            side,
            kind: OrderKind::Limit {
                price: Price::new(Decimal::from(price)),
                quantity: Quantity::new(Decimal::from(qty)),
                tif: TimeInForce::Gtc,
            },
            filled_qty: Quantity::zero(),
            filled_quote_qty: QuoteQty::zero(),
        }
    }

    fn limit_buy(price: i64, qty: i64) -> Order {
        make_order(Uuid::new_v4(), price, qty, Side::Buy)
    }

    fn limit_sell(price: i64, qty: i64) -> Order {
        make_order(Uuid::new_v4(), price, qty, Side::Sell)
    }

    // 매수 주문은 최우선 매수 호가에 등록된다
    #[test]
    fn add_buy_order_appears_in_best_bid() {
        let mut book = OrderBook::new();
        let order = limit_buy(100, 1);
        let id = order.order_id;
        book.add(order);
        assert_eq!(book.best_bid().unwrap().order_id, id);
    }

    // 매도 주문은 최우선 매도 호가에 등록된다
    #[test]
    fn add_sell_order_appears_in_best_ask() {
        let mut book = OrderBook::new();
        let order = limit_sell(100, 1);
        let id = order.order_id;
        book.add(order);
        assert_eq!(book.best_ask().unwrap().order_id, id);
    }

    // 더 높은 매수 가격이 우선된다
    #[test]
    fn add_multiple_bids_returns_highest_price() {
        let mut book = OrderBook::new();
        let low = limit_buy(90, 1);
        let high = limit_buy(110, 1);
        let high_id = high.order_id;
        book.add(low);
        book.add(high);
        assert_eq!(book.best_bid().unwrap().order_id, high_id);
    }

    // 더 낮은 매도 가격이 우선된다
    #[test]
    fn add_multiple_asks_returns_lowest_price() {
        let mut book = OrderBook::new();
        let high = limit_sell(200, 1);
        let low = limit_sell(100, 1);
        let low_id = low.order_id;
        book.add(high);
        book.add(low);
        assert_eq!(book.best_ask().unwrap().order_id, low_id);
    }

    // 동일 매수 가격은 FIFO를 유지한다
    #[test]
    fn add_same_price_fifo_order() {
        let mut book = OrderBook::new();
        let first = limit_buy(100, 1);
        let second = limit_buy(100, 2);
        let first_id = first.order_id;
        book.add(first);
        book.add(second);
        assert_eq!(book.best_bid().unwrap().order_id, first_id);
    }

    // 취소된 매수 주문은 조회에서 제외된다
    #[test]
    fn cancel_removes_from_index_best_bid_skips_it() {
        let mut book = OrderBook::new();
        let order = limit_buy(100, 1);
        let id = order.order_id;
        book.add(order);
        book.cancel(&id);
        assert!(book.best_bid().is_none());
    }

    // 같은 가격대 다음 주문이 최우선이 된다
    #[test]
    fn cancel_then_next_order_returned() {
        let mut book = OrderBook::new();
        let first = limit_buy(100, 1);
        let second = limit_buy(100, 1);
        let first_id = first.order_id;
        let second_id = second.order_id;
        book.add(first);
        book.add(second);
        book.cancel(&first_id);
        assert_eq!(book.best_bid().unwrap().order_id, second_id);
    }

    // 없는 주문 취소는 실패하지 않는다
    #[test]
    fn cancel_nonexistent_no_panic() {
        let mut book = OrderBook::new();
        let fake_id = OrderId::new(Uuid::new_v4());
        book.cancel(&fake_id);
    }

    // 부분 체결된 주문은 호가에 남는다
    #[test]
    fn partial_fill_remains_in_book() {
        let mut book = OrderBook::new();
        let order = limit_buy(100, 10);
        let id = order.order_id;
        book.add(order);
        book.fill(&id, Quantity::new(Decimal::from(5)));
        assert_eq!(book.best_bid().unwrap().order_id, id);
    }

    // 전량 체결된 주문은 호가에서 제거된다
    #[test]
    fn full_fill_removes_from_book() {
        let mut book = OrderBook::new();
        let order = limit_buy(100, 10);
        let id = order.order_id;
        book.add(order);
        book.fill(&id, Quantity::new(Decimal::from(10)));
        assert!(book.best_bid().is_none());
    }

    // 없는 주문 체결은 영향이 없다
    #[test]
    fn fill_nonexistent_no_effect() {
        let mut book = OrderBook::new();
        let fake_id = OrderId::new(Uuid::new_v4());
        book.fill(&fake_id, Quantity::new(Decimal::from(5)));
    }

    // 빈 호가창은 매수 호가가 없다
    #[test]
    fn empty_book_best_bid_returns_none() {
        let mut book = OrderBook::new();
        assert!(book.best_bid().is_none());
    }

    // 빈 호가창은 매도 호가가 없다
    #[test]
    fn empty_book_best_ask_returns_none() {
        let mut book = OrderBook::new();
        assert!(book.best_ask().is_none());
    }

    // 모든 주문 취소 후 매수 호가가 비워진다
    #[test]
    fn after_all_canceled_best_bid_returns_none() {
        let mut book = OrderBook::new();
        let o = limit_buy(100, 1);
        let id = o.order_id;
        book.add(o);
        book.cancel(&id);
        assert!(book.best_bid().is_none());
    }

    // 단일 매도 주문으로 전량 체결 가능하다
    #[test]
    fn fok_buy_enough_qty_single_order() {
        let mut book = OrderBook::new();
        book.add(limit_sell(100, 10));
        assert!(book.can_fully_fill(
            Side::Buy,
            Price::new(Decimal::from(100)),
            Quantity::new(Decimal::from(10))
        ));
    }

    // 여러 매도 주문으로 전량 체결 가능하다
    #[test]
    fn fok_buy_enough_qty_multiple_orders() {
        let mut book = OrderBook::new();
        book.add(limit_sell(100, 5));
        book.add(limit_sell(100, 5));
        assert!(book.can_fully_fill(
            Side::Buy,
            Price::new(Decimal::from(100)),
            Quantity::new(Decimal::from(10))
        ));
    }

    // 매도 수량이 부족하면 전량 체결할 수 없다
    #[test]
    fn fok_buy_not_enough_qty() {
        let mut book = OrderBook::new();
        book.add(limit_sell(100, 3));
        assert!(!book.can_fully_fill(
            Side::Buy,
            Price::new(Decimal::from(100)),
            Quantity::new(Decimal::from(100))
        ));
    }

    // 상한 가격을 넘는 매도 호가는 제외된다
    #[test]
    fn fok_buy_price_limit_exceeded() {
        let mut book = OrderBook::new();
        book.add(limit_sell(200, 10));
        assert!(!book.can_fully_fill(
            Side::Buy,
            Price::new(Decimal::from(100)),
            Quantity::new(Decimal::from(100))
        ));
    }

    // 단일 매수 주문으로 전량 체결 가능하다
    #[test]
    fn fok_sell_enough_qty() {
        let mut book = OrderBook::new();
        book.add(limit_buy(100, 10));
        assert!(book.can_fully_fill(
            Side::Sell,
            Price::new(Decimal::from(100)),
            Quantity::new(Decimal::from(10))
        ));
    }

    // 하한 가격 미달 매수 호가는 제외된다
    #[test]
    fn fok_sell_price_limit_not_met() {
        let mut book = OrderBook::new();
        book.add(limit_buy(50, 10));
        assert!(!book.can_fully_fill(
            Side::Sell,
            Price::new(Decimal::from(100)),
            Quantity::new(Decimal::from(100))
        ));
    }

    // 주문 금액으로 전량 매수할 수 있다
    #[test]
    fn fok_quote_enough() {
        let mut book = OrderBook::new();
        book.add(limit_sell(100, 10));
        assert!(book.can_fully_fill_quote(QuoteQty::new(Decimal::from(1000))));
    }

    // 주문 금액이 부족하면 전량 매수할 수 없다
    #[test]
    fn fok_quote_not_enough() {
        let mut book = OrderBook::new();
        book.add(limit_sell(100, 5));
        assert!(!book.can_fully_fill_quote(QuoteQty::new(Decimal::from(1000))));
    }

    // 여러 매도 주문으로 전량 매수할 수 있다
    #[test]
    fn fok_quote_across_multiple_orders() {
        let mut book = OrderBook::new();
        book.add(limit_sell(100, 5));
        book.add(limit_sell(100, 5));
        assert!(book.can_fully_fill_quote(QuoteQty::new(Decimal::from(1000))));
    }

    // 단일 매도 주문 수량 부족시 실패한다
    #[test]
    fn fok_quote_fails_when_single_order_lacks_quantity() {
        let mut book = OrderBook::new();
        book.add(limit_sell(100, 5));
        assert!(!book.can_fully_fill_quote(QuoteQty::new(Decimal::from(1000))));
    }

    // 남은 금액으로 다음 호가의 소수 수량을 살 수 있다
    #[test]
    fn fok_quote_allows_fractional_quantity_at_next_level() {
        let mut book = OrderBook::new();
        book.add(limit_sell(100, 5));
        book.add(limit_sell(200, 10));
        assert!(book.can_fully_fill_quote(QuoteQty::new(Decimal::from(550))));
    }

    // 단일 호가에서도 주문 금액만큼 소수 수량 체결 가능하다
    #[test]
    fn fok_quote_allows_fractional_quantity_single_level() {
        let mut book = OrderBook::new();
        book.add(limit_sell(200, 10));
        assert!(book.can_fully_fill_quote(QuoteQty::new(Decimal::from(50))));
    }

    // 나눗셈이 딱 떨어지지 않아도 주문 금액 전액 매수 가능하다
    #[test]
    fn fok_quote_allows_repeating_decimal_quantity() {
        let mut book = OrderBook::new();
        book.add(limit_sell(3, 100));
        assert!(book.can_fully_fill_quote(QuoteQty::new(Decimal::from(100))));
    }
}
