use crate::models::{OrderId, Trade};

pub enum EngineEvent {
    Matched(Vec<Trade>),
    Canceled{
        order_id: OrderId,
        reason: CancelReason
    },
}

pub enum CancelReason {
    UserRequest,
    FokExpired,
}