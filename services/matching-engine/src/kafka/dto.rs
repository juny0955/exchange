use crate::engine::OrderCommand;
use crate::kafka::error::KafkaConsumerError;
use crate::models::{
    AccountId, Order, OrderId, OrderKind, Price, Quantity, QuoteQty, Side, Symbol, TimeInForce,
};
use rust_decimal::Decimal;
use serde::Deserialize;
use uuid::Uuid;

#[derive(Debug, Deserialize)]
#[serde(rename_all = "UPPERCASE")]
pub enum SideDto {
    Buy,
    Sell,
}
impl From<SideDto> for Side {
    fn from(value: SideDto) -> Self {
        match value {
            SideDto::Buy => Side::Buy,
            SideDto::Sell => Side::Sell,
        }
    }
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "UPPERCASE")]
pub enum OrderTypeDto {
    Limit,
    Market,
}

#[derive(Debug, Deserialize)]
#[serde(rename_all = "UPPERCASE")]
pub enum TifDto {
    Gtc,
    Ioc,
    Fok,
}
impl From<TifDto> for TimeInForce {
    fn from(value: TifDto) -> Self {
        match value {
            TifDto::Gtc => TimeInForce::Gtc,
            TifDto::Ioc => TimeInForce::Ioc,
            TifDto::Fok => TimeInForce::Fok,
        }
    }
}

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct PlaceOrderDto {
    pub order_id: Uuid,
    pub account_id: Uuid,
    pub symbol: Symbol,
    pub side: SideDto,
    pub order_type: OrderTypeDto,
    pub tif: Option<TifDto>,
    pub price: Option<Decimal>,
    pub quantity: Option<Decimal>,
    pub quote_qty: Option<Decimal>,
}

impl PlaceOrderDto {
    pub fn try_into_command(self) -> Result<OrderCommand, KafkaConsumerError> {
        let kind = match (self.order_type, &self.side) {
            (OrderTypeDto::Limit, _) => {
                let price = self.price.ok_or_else(|| invalid("LIMIT: price 누락"))?;
                let quantity = self
                    .quantity
                    .ok_or_else(|| invalid("LIMIT: quantity 누락"))?;
                let tif = self.tif.ok_or_else(|| invalid("LIMIT: tif 누락"))?;
                OrderKind::Limit {
                    price: Price::new(price),
                    quantity: Quantity::new(quantity),
                    tif: tif.into(),
                }
            }
            (OrderTypeDto::Market, SideDto::Buy) => {
                let quote_qty = self
                    .quote_qty
                    .ok_or_else(|| invalid("MARKET BUY: quote_qty 누락"))?;
                OrderKind::MarketBuy {
                    quote_qty: QuoteQty::new(quote_qty),
                }
            }
            (OrderTypeDto::Market, SideDto::Sell) => {
                let quantity = self
                    .quantity
                    .ok_or_else(|| invalid("MARKET SELL: quantity 누락"))?;
                OrderKind::MarketSell {
                    quantity: Quantity::new(quantity),
                }
            }
        };

        let order = Order {
            order_id: OrderId::new(self.order_id),
            account_id: AccountId::new(self.account_id),
            symbol: self.symbol,
            side: self.side.into(),
            kind,
            filled_qty: Quantity::zero(),
            filled_quote_qty: QuoteQty::zero(),
        };

        Ok(OrderCommand::Place(order))
    }
}

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CancelOrderDto {
    pub order_id: Uuid,
    pub account_id: Uuid,
    pub symbol: Symbol,
}

impl CancelOrderDto {
    pub fn into_command(self) -> OrderCommand {
        OrderCommand::Cancel {
            symbol: self.symbol,
            order_id: OrderId::new(self.order_id),
            account_id: AccountId::new(self.account_id),
        }
    }
}

fn invalid<S: Into<String>>(msg: S) -> KafkaConsumerError {
    KafkaConsumerError::InvalidPayload(msg.into())
}
