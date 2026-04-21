use crate::models::{
    AccountId, OrderId, OrderType, Price, Quantity, QuoteQty, Side, Symbol, TimeInForce,
};

#[derive(Debug, Clone)]
pub struct Order {
    pub order_id: OrderId,
    pub account_id: AccountId,
    pub symbol: Symbol,

    pub side: Side,
    pub order_type: OrderType,
    pub tif: TimeInForce,

    pub price: Option<Price>,
    pub quantity: Option<Quantity>,
    pub quote_qty: Option<QuoteQty>,

    pub filled_qty: Quantity,
    pub filled_quote_qty: QuoteQty,
}
