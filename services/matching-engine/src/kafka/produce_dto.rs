use chrono::{DateTime, Utc};
use rust_decimal::Decimal;
use serde::Serialize;
use uuid::Uuid;

use crate::{
    engine::{CancelReason, EngineError},
    models::Trade,
};

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
pub struct AcceptedMessage {
    pub order_id: Uuid,
    pub account_id: Uuid,
}

#[derive(Serialize)]
pub enum CancelReasonDto {
    UserRequest,
    IocExpired,
    FokExpired,
}
impl From<CancelReason> for CancelReasonDto {
    fn from(reason: CancelReason) -> Self {
        match reason {
            CancelReason::UserRequest => CancelReasonDto::UserRequest,
            CancelReason::IocExpired => CancelReasonDto::IocExpired,
            CancelReason::FokExpired => CancelReasonDto::FokExpired,
        }
    }
}

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
pub struct CanceledMessage {
    pub order_id: Uuid,
    pub account_id: Uuid,
    pub reason: CancelReasonDto,
}

#[derive(Serialize)]
pub enum RejectReasonDto {
    ChannelFull,
    SymbolNotFound,
    InvalidOrderKind,
}
impl From<EngineError> for RejectReasonDto {
    fn from(error: EngineError) -> Self {
        match error {
            EngineError::ChannelFull => RejectReasonDto::ChannelFull,
            EngineError::SymbolNotFound => RejectReasonDto::SymbolNotFound,
            EngineError::InvalidOrderKind => RejectReasonDto::InvalidOrderKind,
        }
    }
}

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
pub struct RejectedMessage {
    pub order_id: Uuid,
    pub account_id: Uuid,
    pub reason: RejectReasonDto,
}

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
pub struct MatchedMessage {
    pub trade_id: Uuid,
    pub buy_account_id: Uuid,
    pub buy_order_id: Uuid,
    pub sell_account_id: Uuid,
    pub sell_order_id: Uuid,
    pub price: Decimal,
    pub quantity: Decimal,
    pub quote_qty: Decimal,
    pub trade_at: DateTime<Utc>,
}
impl From<&Trade> for MatchedMessage {
    fn from(trade: &Trade) -> Self {
        Self {
            trade_id: trade.trade_id.value(),
            buy_account_id: trade.buy_account_id.value(),
            buy_order_id: trade.buy_order_id.value(),
            sell_account_id: trade.sell_account_id.value(),
            sell_order_id: trade.sell_order_id.value(),
            price: trade.price.value(),
            quantity: trade.quantity.value(),
            quote_qty: trade.quote_qty.value(),
            trade_at: trade.trade_at,
        }
    }
}
