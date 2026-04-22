use crate::engine::command::OrderCommand;
use crate::engine::event::EngineEvent;
use crate::engine::matcher::Matcher;
use crate::engine::orderbook::OrderBook;
use crate::models::Symbol;
use std::sync::mpsc::{Receiver, Sender};

pub struct SymbolWorker {
    symbol: Symbol,
    order_book: OrderBook,
    receiver: Receiver<OrderCommand>,
    event_sender: Sender<EngineEvent>
}

impl SymbolWorker {
    pub fn run(mut self) {
        while let Ok(command) = self.receiver.recv() {
            let result = Matcher::match_order(command, &mut self.order_book);
            let _ = self.event_sender.send(result);
        }
    }
}