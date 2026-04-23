use crate::models::{
    AccountId, OrderId, OrderKind, Quantity, QuoteQty, Side, Symbol, TradeParticipant,
};

#[derive(Debug, Clone)]
pub struct Order {
    pub order_id: OrderId,
    pub account_id: AccountId,
    pub symbol: Symbol,

    pub side: Side,
    pub kind: OrderKind,

    pub filled_qty: Quantity,
    pub filled_quote_qty: QuoteQty,
}

impl Order {
    pub fn fill(&mut self, fill_qty: Quantity, fill_quote: QuoteQty) {
        self.filled_qty = self.filled_qty.add(fill_qty);
        self.filled_quote_qty = self.filled_quote_qty.add(fill_quote);
    }

    /// Limit / MarketSell 전용
    pub fn remaining_qty(&self) -> Quantity {
        match self.kind {
            OrderKind::Limit { quantity, .. } | OrderKind::MarketSell { quantity } => {
                quantity.sub(self.filled_qty)
            }
            OrderKind::MarketBuy { .. } => unreachable!("시장가 매수 주문은 수량이 없습니다."),
        }
    }

    /// Limit / MarketSell 전용
    pub fn is_fully_filled(&self) -> bool {
        match &self.kind {
            OrderKind::Limit { quantity, .. } | OrderKind::MarketSell { quantity } => {
                *quantity == self.filled_qty
            }
            OrderKind::MarketBuy { .. } => unreachable!("시장가 매수 주문은 수량이 없습니다."),
        }
    }

    pub fn as_participant(&self) -> TradeParticipant {
        TradeParticipant {
            account_id: self.account_id,
            order_id: self.order_id,
        }
    }
}
