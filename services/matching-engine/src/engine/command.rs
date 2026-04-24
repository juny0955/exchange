use crate::models::{AccountId, Order, OrderId, Symbol};

pub enum OrderCommand {
    Place(Order),
    Cancel { symbol: Symbol, order_id: OrderId, account_id: AccountId },
}

impl OrderCommand {
    pub fn symbol(&self) -> &Symbol {
        match self {
            OrderCommand::Place(order) => &order.symbol,
            OrderCommand::Cancel { symbol, .. } => symbol,
        }
    }

    pub fn order_id(&self) -> OrderId {
        match self {
            OrderCommand::Place(order) => order.order_id,
            OrderCommand::Cancel { order_id, .. } => *order_id,
        }
    }

    pub fn account_id(&self) -> AccountId {
        match self {
            OrderCommand::Place(order) => order.account_id,
            OrderCommand::Cancel { account_id, .. } => *account_id,
        }
    }
}
