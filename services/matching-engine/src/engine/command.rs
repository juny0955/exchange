use crate::models::{Order, OrderId, Symbol};

pub enum OrderCommand {
    Place(Order),
    Cancel { symbol: Symbol, order_id: OrderId },
}
