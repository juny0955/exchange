use std::{cmp::Reverse, collections::{BTreeMap, HashMap, VecDeque}};
use rust_decimal::Decimal;
use crate::models::{Order, OrderId, Price, Quantity, Side};

pub struct OrderBook {
    // 매수 호가
    bids: BTreeMap<Reverse<Price>, VecDeque<OrderId>>,
    // 매도 호가
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
        let price = order.price.unwrap();

        self.index.insert(order_id, order);
        match side {
            Side::Buy => {
                self.bids
                    .entry(Reverse(price))
                    .or_default()
                    .push_back(order_id);
            },
            Side::Sell => {
                self.asks
                    .entry(price)
                    .or_default()
                    .push_back(order_id);
            }
        }
    }

    /// 주문을 취소한다
    ///
    /// 즉시 호가에서 삭제하지않고 Index에서만 삭제 수행 (지연삭제)
    pub fn cancel(&mut self, order_id: &OrderId) {
        self.index.remove(order_id);
    }

    /// 최상위 매도 호가를 가져온다
    pub fn best_ask(&mut self) -> Option<&Order> {
        Self::get_best(&mut self.asks, &self.index)
    }

    /// 최상위 매수 호가를 가져온다
    pub fn best_bid(&mut self) -> Option<&Order> {
        Self::get_best(&mut self.bids, &self.index)
    }

    /// Maker 주문을 갱신한다
    ///
    /// 전량 체결시 index에서 제거
    pub fn fill(&mut self, order_id: &OrderId, qty: Quantity) {
        if let Some(order) = self.index.get_mut(order_id) {
            order.fill(qty);

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
                    if price.value() > price_limit { break }
                    if self.has_enough_quantity_in_queue(queue, &mut acc, required) {
                        return true;
                    }
                }
            }
            Side::Sell => {
                for (price, queue) in &self.bids {
                    if price.0.value() < price_limit { break }
                    if self.has_enough_quantity_in_queue(queue, &mut acc, required) {
                        return true;
                    }
                }
            }
        }
        false
    }

    fn get_best<'a, K>(
        book: &mut BTreeMap<K, VecDeque<OrderId>>,
        index: &'a HashMap<OrderId, Order>
    ) -> Option<&'a Order>
    where
        K: Ord + Copy
    {
        loop {
            // 최상위 호가 확인
            let (price, order_id) = {
                let (price, queue) = book.iter().next()?;
                (*price, *queue.front()?)
            };

            if let Some(order) = index.get(&order_id) {
                return Some(order);
            }

            // 호가 정리
            let queue = book.get_mut(&price).unwrap();
            queue.pop_front();
            if queue.is_empty() {
                book.remove(&price);
            }
        }
    }

    fn has_enough_quantity_in_queue(&self, queue: &VecDeque<OrderId>, acc: &mut Decimal, required: Decimal) -> bool {
        for order_id in queue {
            if let Some(order) = self.index.get(order_id) {
                *acc += order.remaining_qty().value();
                if *acc >= required { return true }
            }
        }

        false
    }
}
