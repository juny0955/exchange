use crate::models::{OrderId, Trade};

pub enum EngineEvent {
    Matched(Vec<Trade>),
    Canceled {
        order_id: OrderId,
        reason: CancelReason,
    },
}

#[derive(Debug)]
pub enum CancelReason {
    UserRequest,
    IocExpired,
    FokExpired,
}
