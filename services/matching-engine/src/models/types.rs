use crate::models::Side::{Buy, Sell};
use rust_decimal::Decimal;
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

#[derive(Debug, Clone, Copy)]
pub enum TimeInForce {
    Gtc,
    Ioc,
    Fok,
}

#[derive(Debug, Clone, Copy, PartialEq)]
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

#[derive(Debug, Clone, Copy)]
pub struct TradeId(Uuid);
impl TradeId {
    pub fn new() -> Self {
        Self(Uuid::new_v4())
    }
    pub fn value(&self) -> Uuid {
        self.0
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, PartialOrd, Ord)]
pub struct Price(Decimal);
impl Price {
    pub fn new(val: Decimal) -> Self {
        Self(val)
    }

    pub fn value(&self) -> Decimal {
        self.0
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, PartialOrd, Ord)]
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

    pub fn sub(&self, val: Quantity) -> Quantity {
        Quantity::new(self.0 - val.value())
    }

    pub fn add(&self, val: Quantity) -> Quantity {
        Quantity::new(self.0 + val.value())
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, PartialOrd, Ord)]
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

    pub fn sub(&self, val: QuoteQty) -> QuoteQty {
        QuoteQty::new(self.0 - val.value())
    }

    pub fn add(&self, val: QuoteQty) -> QuoteQty {
        QuoteQty::new(self.0 + val.value())
    }
}
