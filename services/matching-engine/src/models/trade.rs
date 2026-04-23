use crate::models::{AccountId, OrderId, Price, Quantity, QuoteQty, Side, Symbol, TradeId};
use std::time::Instant;

pub struct TradeParticipant {
    pub account_id: AccountId,
    pub order_id: OrderId,
}

pub struct Trade {
    pub trade_id: TradeId,
    pub symbol: Symbol,
    pub buy_account_id: AccountId,
    pub buy_order_id: OrderId,
    pub sell_account_id: AccountId,
    pub sell_order_id: OrderId,
    pub price: Price,
    pub quantity: Quantity,
    pub quote_qty: QuoteQty,
    pub trade_at: Instant,
}

impl Trade {
    pub fn new(
        symbol: Symbol,
        taker_side: Side,
        taker: TradeParticipant,
        maker: TradeParticipant,
        price: Price,
        quantity: Quantity,
        quote_qty: QuoteQty,
    ) -> Self {
        let (buy, sell) = match taker_side {
            Side::Buy => (taker, maker),
            Side::Sell => (maker, taker),
        };

        Self {
            trade_id: TradeId::new(),
            symbol,
            buy_account_id: buy.account_id,
            buy_order_id: buy.order_id,
            sell_account_id: sell.account_id,
            sell_order_id: sell.order_id,
            price,
            quantity,
            quote_qty,
            trade_at: Instant::now(),
        }
    }
}
