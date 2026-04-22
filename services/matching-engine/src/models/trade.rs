use std::time::Instant;
use crate::models::{AccountId, OrderId, Price, Quantity, QuoteQty, Symbol, TradeId};

pub struct Trade {
    trade_id: TradeId,
    symbol: Symbol,
    buy_account_id: AccountId,
    buy_order_id: OrderId,
    sell_account_id: AccountId,
    sell_order_id: OrderId,
    price: Price,
    quantity: Quantity,
    quote_qty: QuoteQty,
    trade_at: Instant,
}

impl Trade {
    pub fn new(
        symbol: Symbol,
        buy_account_id: AccountId,
        buy_order_id: OrderId,
        sell_account_id: AccountId,
        sell_order_id: OrderId,
        price: Price,
        quantity: Quantity,
        quote_qty: QuoteQty,
    ) -> Self {
        Self {
            trade_id: TradeId::new(),
            symbol,
            buy_account_id,
            buy_order_id,
            sell_account_id,
            sell_order_id,
            price,
            quantity,
            quote_qty,
            trade_at: Instant::now()
        }
    }
}
