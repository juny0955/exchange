use crate::engine::EngineError;
use crate::models::{AccountId, OrderId, Symbol, Trade};

#[derive(Debug)]
pub enum EngineEvent {
    Accepted {
        symbol: Symbol,
        order_id: OrderId,
        account_id: AccountId,
    },
    Matched {
        symbol: Symbol,
        trades: Vec<Trade>,
    },
    Canceled {
        symbol: Symbol,
        order_id: OrderId,
        account_id: AccountId,
        reason: CancelReason,
    },
    Rejected {
        symbol: Symbol,
        order_id: OrderId,
        account_id: AccountId,
        reason: EngineError,
    },
}

#[derive(Debug)]
pub enum CancelReason {
    UserRequest,
    IocExpired,
    FokExpired,
}
