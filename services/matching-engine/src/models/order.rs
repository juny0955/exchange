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

impl Order {
    pub fn fill(&mut self, fill_qty: Quantity, fill_quote: QuoteQty) {
        self.filled_qty = self.filled_qty.add(fill_qty);
        self.filled_quote_qty = self.filled_quote_qty.add(fill_quote);
    }

    pub fn remaining_qty(&self) -> Quantity {
        self.quantity.unwrap().sub(self.filled_qty)
    }

    pub fn is_fully_filled(&self) -> bool {
        self.quantity.unwrap().value().eq(&self.filled_qty.value())
    }
}
