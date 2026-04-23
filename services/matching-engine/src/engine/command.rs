use crate::models::{Order, OrderId, Symbol};

pub enum OrderCommand {
    Place(Order),
    Cancel { symbol: Symbol, order_id: OrderId },
}

impl OrderCommand {
    pub fn symbol(&self) -> &Symbol {
        match self {
            OrderCommand::Place(order) => &order.symbol,
            OrderCommand::Cancel { symbol, .. } => symbol,
        }
    }
}
