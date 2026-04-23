use crossbeam::channel::{Receiver, Sender};

use crate::engine::command::OrderCommand;
use crate::engine::event::{CancelReason, EngineEvent};
use crate::engine::matcher::Matcher;
use crate::engine::orderbook::OrderBook;

pub struct SymbolWorker {
    order_book: OrderBook,
    command_receiver: Receiver<OrderCommand>,
    event_sender: Sender<EngineEvent>,
}

impl SymbolWorker {
    pub fn new(
        order_book: OrderBook,
        command_receiver: Receiver<OrderCommand>,
        event_sender: Sender<EngineEvent>,
    ) -> Self {
        Self {
            order_book,
            command_receiver,
            event_sender,
        }
    }

    pub fn run(mut self) {
        for command in &self.command_receiver {
            let results = match command {
                OrderCommand::Place(order) => Matcher::match_order(order, &mut self.order_book),
                OrderCommand::Cancel { order_id, .. } => {
                    self.order_book.cancel(&order_id);

                    vec![EngineEvent::Canceled {
                        order_id,
                        reason: CancelReason::UserRequest,
                    }]
                }
            };

            for event in results {
                let _ = self.event_sender.send(event);
            }
        }
    }
}
