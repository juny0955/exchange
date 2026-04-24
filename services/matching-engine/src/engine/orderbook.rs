use crate::models::{Order, OrderId, OrderKind, Price, Quantity, QuoteQty, Side};
use rust_decimal::Decimal;
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
            let quote = QuoteQty::new(price.value() * qty.value());
            order.fill(qty, quote);

            if order.is_fully_filled() {
                self.index.remove(order_id);
            }
        }
    }

    pub fn can_fully_fill(&self, side: Side, price_limit: Decimal, required: Decimal) -> bool {
        let mut acc = Decimal::ZERO;
        match side {
            Side::Buy => {
                for (price, queue) in &self.asks {
                    if price.value() > price_limit {
                        break;
                    }
                    if self.has_enough_quantity_in_queue(queue, &mut acc, required) {
                        return true;
                    }
                }
            }
            Side::Sell => {
                for (price, queue) in &self.bids {
                    if price.0.value() < price_limit {
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

    pub fn can_fully_fill_quote(&self, required: Decimal) -> bool {
        let mut remaining = required;
        for (price, queue) in &self.asks {
            if self.has_enough_quote_in_queue(queue, price.value(), &mut remaining) {
                return true;
            }
            if remaining < price.value() {
                return false;
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
        acc: &mut Decimal,
        required: Decimal,
    ) -> bool {
        for order_id in queue {
            if let Some(order) = self.index.get(order_id) {
                *acc += order.remaining_qty().value();
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
        price: Decimal,
        remaining: &mut Decimal,
    ) -> bool {
        for order_id in queue {
            if let Some(order) = self.index.get(order_id) {
                let affordable_qty = (*remaining / price).floor();
                if affordable_qty.is_zero() {
                    return false;
                }

                let fill_qty = order.remaining_qty().value().min(affordable_qty);
                *remaining -= fill_qty * price;
                if *remaining <= Decimal::ZERO {
                    return true;
                }
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

    #[test]
    fn add_buy_order_appears_in_best_bid() {
        let mut book = OrderBook::new();
        let order = limit_buy(100, 1);
        let id = order.order_id;
        book.add(order);
        assert_eq!(book.best_bid().unwrap().order_id, id);
    }

    #[test]
    fn add_sell_order_appears_in_best_ask() {
        let mut book = OrderBook::new();
        let order = limit_sell(100, 1);
        let id = order.order_id;
        book.add(order);
        assert_eq!(book.best_ask().unwrap().order_id, id);
    }

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

    #[test]
    fn cancel_removes_from_index_best_bid_skips_it() {
        let mut book = OrderBook::new();
        let order = limit_buy(100, 1);
        let id = order.order_id;
        book.add(order);
        book.cancel(&id);
        assert!(book.best_bid().is_none());
    }

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

    #[test]
    fn cancel_nonexistent_no_panic() {
        let mut book = OrderBook::new();
        let fake_id = OrderId::new(Uuid::new_v4());
        book.cancel(&fake_id);
    }

    #[test]
    fn partial_fill_remains_in_book() {
        let mut book = OrderBook::new();
        let order = limit_buy(100, 10);
        let id = order.order_id;
        book.add(order);
        book.fill(&id, Quantity::new(Decimal::from(5)));
        assert_eq!(book.best_bid().unwrap().order_id, id);
    }

    #[test]
    fn full_fill_removes_from_book() {
        let mut book = OrderBook::new();
        let order = limit_buy(100, 10);
        let id = order.order_id;
        book.add(order);
        book.fill(&id, Quantity::new(Decimal::from(10)));
        assert!(book.best_bid().is_none());
    }

    #[test]
    fn fill_nonexistent_no_effect() {
        let mut book = OrderBook::new();
        let fake_id = OrderId::new(Uuid::new_v4());
        book.fill(&fake_id, Quantity::new(Decimal::from(5)));
    }

    #[test]
    fn empty_book_best_bid_returns_none() {
        let mut book = OrderBook::new();
        assert!(book.best_bid().is_none());
    }

    #[test]
    fn empty_book_best_ask_returns_none() {
        let mut book = OrderBook::new();
        assert!(book.best_ask().is_none());
    }

    #[test]
    fn after_all_canceled_best_bid_returns_none() {
        let mut book = OrderBook::new();
        let o = limit_buy(100, 1);
        let id = o.order_id;
        book.add(o);
        book.cancel(&id);
        assert!(book.best_bid().is_none());
    }

    #[test]
    fn fok_buy_enough_qty_single_order() {
        let mut book = OrderBook::new();
        book.add(limit_sell(100, 10));
        assert!(book.can_fully_fill(Side::Buy, Decimal::from(100), Decimal::from(10)));
    }

    #[test]
    fn fok_buy_enough_qty_multiple_orders() {
        let mut book = OrderBook::new();
        book.add(limit_sell(100, 5));
        book.add(limit_sell(100, 5));
        assert!(book.can_fully_fill(Side::Buy, Decimal::from(100), Decimal::from(10)));
    }

    #[test]
    fn fok_buy_not_enough_qty() {
        let mut book = OrderBook::new();
        book.add(limit_sell(100, 3));
        assert!(!book.can_fully_fill(Side::Buy, Decimal::from(100), Decimal::from(10)));
    }

    #[test]
    fn fok_buy_price_limit_exceeded() {
        let mut book = OrderBook::new();
        book.add(limit_sell(200, 10));
        assert!(!book.can_fully_fill(Side::Buy, Decimal::from(100), Decimal::from(10)));
    }

    #[test]
    fn fok_sell_enough_qty() {
        let mut book = OrderBook::new();
        book.add(limit_buy(100, 10));
        assert!(book.can_fully_fill(Side::Sell, Decimal::from(100), Decimal::from(10)));
    }

    #[test]
    fn fok_sell_price_limit_not_met() {
        let mut book = OrderBook::new();
        book.add(limit_buy(50, 10));
        assert!(!book.can_fully_fill(Side::Sell, Decimal::from(100), Decimal::from(10)));
    }

    #[test]
    fn fok_quote_enough() {
        let mut book = OrderBook::new();
        book.add(limit_sell(100, 10));
        assert!(book.can_fully_fill_quote(Decimal::from(1000)));
    }

    #[test]
    fn fok_quote_not_enough() {
        let mut book = OrderBook::new();
        book.add(limit_sell(100, 5));
        assert!(!book.can_fully_fill_quote(Decimal::from(1000)));
    }

    #[test]
    fn fok_quote_across_multiple_orders() {
        let mut book = OrderBook::new();
        book.add(limit_sell(100, 5));
        book.add(limit_sell(100, 5));
        assert!(book.can_fully_fill_quote(Decimal::from(1000)));
    }

    #[test]
    fn fok_quote_fails_when_single_order_lacks_quantity() {
        let mut book = OrderBook::new();
        book.add(limit_sell(100, 5));
        assert!(!book.can_fully_fill_quote(Decimal::from(1000)));
    }

    #[test]
    fn fok_quote_fails_when_remaining_quote_cannot_buy_next_level() {
        let mut book = OrderBook::new();
        book.add(limit_sell(100, 5));
        book.add(limit_sell(200, 10));
        assert!(!book.can_fully_fill_quote(Decimal::from(550)));
    }
}
