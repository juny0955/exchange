use crate::models::Side::{Buy, Sell};
use derive_more::{Add, AddAssign, Div, Sub, SubAssign};
use rust_decimal::Decimal;
use std::ops::{Div, Mul};
use uuid::Uuid;

#[derive(Debug, Clone, Copy)]
pub enum Side {
    Buy,
    Sell,
}
impl Side {
    pub fn opposite(&self) -> Side {
        match self {
            Buy => Sell,
            Sell => Buy,
        }
    }
}

#[derive(Debug, Clone, Copy, PartialEq)]
pub enum TimeInForce {
    Gtc,
    Ioc,
    Fok,
}

#[derive(Debug, Clone, Copy, PartialEq)]
pub enum OrderKind {
    Limit {
        price: Price,
        quantity: Quantity,
        tif: TimeInForce,
    },
    MarketBuy {
        quote_qty: QuoteQty,
    },
    MarketSell {
        quantity: Quantity,
    },
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

#[derive(Debug, Clone, Copy)]
pub struct TradeId(Uuid);
impl TradeId {
    pub fn value(&self) -> Uuid {
        self.0
    }
}

impl Default for TradeId {
    fn default() -> Self {
        Self(Uuid::new_v4())
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, PartialOrd, Ord)]
pub struct Price(Decimal);
impl Price {
    pub fn new(val: Decimal) -> Self {
        Self(val)
    }

    pub fn zero() -> Self {
        Self(Decimal::ZERO)
    }

    pub fn value(&self) -> Decimal {
        self.0
    }
}
impl Mul<Quantity> for Price {
    type Output = QuoteQty;
    fn mul(self, other: Quantity) -> Self::Output {
        QuoteQty(self.0 * other.0)
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, PartialOrd, Ord, Add, AddAssign, Sub, SubAssign)]
pub struct Quantity(Decimal);
impl Quantity {
    pub fn new(val: Decimal) -> Self {
        Self(val)
    }

    pub fn zero() -> Self {
        Self(Decimal::ZERO)
    }

    pub fn value(&self) -> Decimal {
        self.0
    }

    pub fn is_zero(&self) -> bool {
        self.0.is_zero()
    }
}
impl Mul<Price> for Quantity {
    type Output = QuoteQty;
    fn mul(self, other: Price) -> Self::Output {
        QuoteQty(self.0 * other.0)
    }
}

#[derive(
    Debug, Clone, Copy, PartialEq, Eq, PartialOrd, Ord, Add, AddAssign, Sub, SubAssign, Div,
)]
pub struct QuoteQty(Decimal);
impl QuoteQty {
    pub fn new(val: Decimal) -> Self {
        Self(val)
    }

    pub fn zero() -> Self {
        Self(Decimal::ZERO)
    }

    pub fn value(&self) -> Decimal {
        self.0
    }
}
impl Div<Price> for QuoteQty {
    type Output = Quantity;
    fn div(self, other: Price) -> Self::Output {
        Quantity(self.0 / other.0)
    }
}
