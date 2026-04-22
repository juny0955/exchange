use crate::engine::event::EngineEvent;
use crate::engine::orderbook::OrderBook;
use crate::models::{AccountId, Order, OrderId, OrderType, Price, Quantity, QuoteQty, Side, TimeInForce, Trade};

struct MakerInfo {
    order_id: OrderId,
    account_id: AccountId,
    price: Price,
    remaining_qty: Quantity,
}

pub struct Matcher;

impl Matcher {
    pub fn match_order(order: Order, book: &mut OrderBook) -> EngineEvent {
        if order.order_type == OrderType::Market {
            match order.side {
                Side::Buy => Self::process_fok_quote(order, book),
                Side::Sell => Self::process_fok(order, book),
            }
        } else {
            match order.tif {
                TimeInForce::Gtc => Self::process_match(order, book, true),
                TimeInForce::Ioc => Self::process_match(order, book, false),
                TimeInForce::Fok => Self::process_fok(order, book),
            }
        }
    }

    fn process_match(order: Order, book: &mut OrderBook, resting: bool) -> EngineEvent {
        let mut taker = order;
        let mut trades = Vec::new();

        while let Some(maker) = Self::get_maker_info(taker.side.opposite(), book) {
            if !Self::can_match(&taker, maker.price) {
                break;
            }

            let trade = Self::execute_fill(&mut taker, maker, book);
            trades.push(trade);

            if taker.is_fully_filled() { return EngineEvent::Matched(trades) }
        }

        if resting && !taker.is_fully_filled(){
            book.add(taker);
        }

        EngineEvent::Matched(trades)
    }

    /// FOK 전용 Processor
    fn process_fok(order: Order, book: &mut OrderBook) -> EngineEvent {
        let required = order.quantity.unwrap();
        if !book.can_fully_fill(order.side.opposite(), order.price.unwrap(), required) {
            return EngineEvent::Canceled(order.order_id);
        }
        Self::process_match(order, book, false)
    }

    /// TODO FOK + Quote 전용 Processor
    fn process_fok_quote(order: Order, book: &mut OrderBook) -> EngineEvent {
        EngineEvent::Canceled(order.order_id)
    }

    fn get_maker_info(side: Side, book: &mut OrderBook) -> Option<MakerInfo> {
        match side {
            Side::Buy => book.best_bid(),
            Side::Sell => book.best_ask(),
        }.map(|m| MakerInfo {
                order_id: m.order_id,
                account_id: m.account_id,
                price: m.price.unwrap(),
                remaining_qty: m.remaining_qty(),
            }
        )
    }

    fn can_match(taker: &Order, maker_price: Price) -> bool {
        match taker.price {
            Some(taker_price) => match taker.side {
                Side::Buy => taker_price >= maker_price,
                Side::Sell => taker_price <= maker_price,
            }
            None => true
        }
    }

    fn execute_fill(taker: &mut Order, maker: MakerInfo, book: &mut OrderBook) -> Trade {
        let fill_qty = maker.remaining_qty.min(taker.remaining_qty());
        let quote_qty = QuoteQty::new(maker.price.value() * fill_qty.value());

        book.fill(&maker.order_id, fill_qty);
        taker.fill(fill_qty);

        let (buy_account_id, buy_order_id, sell_account_id, sell_order_id) = match taker.side {
            Side::Buy => (taker.account_id, taker.order_id, maker.account_id, maker.order_id),
            Side::Sell => (maker.account_id, maker.order_id, taker.account_id, taker.order_id),
        };

        Trade::new(taker.symbol.clone(), buy_account_id, buy_order_id, sell_account_id, sell_order_id, maker.price, fill_qty, quote_qty)
    }
}