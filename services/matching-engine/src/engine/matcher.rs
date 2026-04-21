use crate::engine::command::OrderCommand;
use crate::engine::orderbook::OrderBook;
use crate::models::{Order, OrderType, Side, TimeInForce};

pub struct Matcher;

impl Matcher {
    pub fn dispatch(command: OrderCommand, book: &mut OrderBook) {
        match command {
            OrderCommand::Place(order) => Self::handle_place(order, book),
            OrderCommand::Cancel { order_id, .. } => {
                book.cancel(&order_id);
            }
        }
    }

    fn handle_place(order: Order, book: &mut OrderBook) {
        match order.order_type {
            OrderType::Market => Self::process_market(order, book),
            OrderType::Limit => Self::process_limit(order, book),
        }
    }

    fn process_market(order: Order, book: &mut OrderBook) {
        match order.side {
            Side::Buy => Self::process_fok_quote(order, book),
            Side::Sell => Self::process_fok(order, book),
        }
    }

    fn process_limit(order: Order, book: &mut OrderBook) {
        match order.tif {
            TimeInForce::Gtc => Self::process_gtc(order, book),
            TimeInForce::Ioc => Self::process_ioc(order, book),
            TimeInForce::Fok => Self::process_fok(order, book),
        }
    }

    fn process_gtc(order: Order, book: &mut OrderBook) {

    }

    fn process_ioc(order: Order, book: &mut OrderBook) {

    }

    fn process_fok(order: Order, book: &mut OrderBook) {

    }

    fn process_fok_quote(order: Order, book: &mut OrderBook) {

    }
}