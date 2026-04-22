use crate::models::{OrderId, Trade};

pub enum EngineEvent {
    Matched(Vec<Trade>),
    Canceled(OrderId),
}