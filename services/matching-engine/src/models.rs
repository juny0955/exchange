use rust_decimal::Decimal;
use uuid::Uuid;

#[derive(Debug, Clone, Copy)]
pub enum Side {
    Buy,
    Sell,
}

#[derive(Debug, Clone, Copy)]
pub enum TimeInForce {
    Gtc,
    Ioc,
    Fok,
}

#[derive(Debug, Clone, Copy)]
pub enum OrderType {
    Limit,
    Market,
}

#[derive(Debug, Clone)]
pub struct Symbol {
    pub base_asset: String,
    pub quote_asset: String,
}

pub struct Order {
    pub order_id: Uuid,
    pub account_id: Uuid,
    pub symbol: Symbol,

    pub side: Side,
    pub order_type: OrderType,
    pub tif: TimeInForce,

    price: Option<Decimal>,
    quantity: Option<Decimal>,
    quote_qty: Option<Decimal>,

    pub filled_qty: Decimal,
    pub filled_quote_qty: Decimal,

    pub seq: u64,
}
