use rust_decimal::Decimal;
use uuid::Uuid;

use crate::models::ValidationError;

#[derive(Debug, Clone, Copy)]
pub enum Side {
    Buy,
    Sell,
}

#[derive(Debug, Clone, Copy)]
pub enum TimeInForce {
    Gtc,
    Ioc,
    Fok,
}

#[derive(Debug, Clone, Copy)]
pub enum OrderType {
    Limit,
    Market,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash)]
pub struct OrderId(Uuid);
impl OrderId {
    pub fn new(val: Uuid) -> Self {
        Self(val)
    }

    pub fn value(&self) -> Uuid {
        self.0
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash)]
pub struct AccountId(Uuid);
impl AccountId {
    pub fn new(val: Uuid) -> Self {
        Self(val)
    }

    pub fn value(&self) -> Uuid {
        self.0
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, PartialOrd, Ord)]
pub struct Price(Decimal);
impl Price {
    pub fn new(val: Decimal) -> Result<Self, ValidationError> {
        if val.is_sign_negative() || val.is_zero() {
            return Err(ValidationError::InvalidPrice(val));
        }
        Ok(Self(val))
    }

    pub fn value(&self) -> Decimal {
        self.0
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, PartialOrd, Ord)]
pub struct Quantity(Decimal);
impl Quantity {
    pub fn new(val: Decimal) -> Result<Self, ValidationError> {
        if val.is_sign_negative() || val.is_zero() {
            return Err(ValidationError::InvalidQuantity(val));
        }
        Ok(Self(val))
    }

    /// 누적 체결용
    ///
    /// 검증을 진행하지않는다
    pub fn zero() -> Self {
        Self(Decimal::ZERO)
    }

    pub fn value(&self) -> Decimal {
        self.0
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, PartialOrd, Ord)]
pub struct QuoteQty(Decimal);
impl QuoteQty {
    pub fn new(val: Decimal) -> Result<Self, ValidationError> {
        if val.is_sign_negative() || val.is_zero() {
            return Err(ValidationError::InvalidQuoteQty(val));
        }
        Ok(Self(val))
    }

    /// 누적 체결용
    ///
    /// 검증을 진행하지않는다
    pub fn zero() -> Self {
        Self(Decimal::ZERO)
    }

    pub fn value(&self) -> Decimal {
        self.0
    }
}
