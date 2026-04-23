use crate::engine::command::OrderCommand;
use crate::engine::event::{CancelReason, EngineEvent};
use crate::engine::matcher::Matcher;
use crate::engine::orderbook::OrderBook;
use crate::models::Symbol;
use std::sync::mpsc::{Receiver, Sender};

pub struct SymbolWorker {
    symbol: Symbol,
    order_book: OrderBook,
    receiver: Receiver<OrderCommand>,
    event_sender: Sender<EngineEvent>,
}

impl SymbolWorker {
    pub fn run(mut self) {
        while let Ok(command) = self.receiver.recv() {
            let result = match command {
                OrderCommand::Place(order) => Matcher::match_order(order, &mut self.order_book),
                OrderCommand::Cancel { order_id, .. } => {
                    self.order_book.cancel(&order_id);

                    EngineEvent::Canceled {
                        order_id,
                        reason: CancelReason::UserRequest,
                    }
                }
            };

            let _ = self.event_sender.send(result);
        }
    }
}
