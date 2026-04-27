use crate::engine::EngineError;
use crate::models::{AccountId, OrderId, Trade};

#[derive(Debug)]
pub enum EngineEvent {
    Accepted {
        order_id: OrderId,
        account_id: AccountId,
    },
    Matched(Vec<Trade>),
    Canceled {
        order_id: OrderId,
        account_id: AccountId,
        reason: CancelReason,
    },
    Rejected {
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
