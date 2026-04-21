use std::{cmp::Reverse, collections::{BTreeMap, HashMap, VecDeque}};

use crate::models::{Order, OrderId, Price, Side};

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
}
