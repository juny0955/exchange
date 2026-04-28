use crossbeam::channel::{Receiver, Sender};
use tracing::warn;

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
            match command {
                OrderCommand::Place(order) => {
                    let order_id = order.order_id;
                    let account_id = order.account_id;
                    if let Err(e) = self.event_sender.send(EngineEvent::Accepted {
                        symbol: order.symbol.clone(),
                        order_id,
                        account_id,
                    }) {
                        warn!(error = %e, ?order_id, "Accepted 이벤트 전송 실패");
                    }

                    for event in Matcher::match_order(order, &mut self.order_book) {
                        if let Err(e) = self.event_sender.send(event) {
                            warn!(error = %e, "EngineEvent 전송 실패");
                        }
                    }
                }
                OrderCommand::Cancel {
                    symbol,
                    order_id,
                    account_id,
                } => {
                    self.order_book.cancel(&order_id);
                    if let Err(e) = self.event_sender.send(EngineEvent::Canceled {
                        symbol,
                        order_id,
                        account_id,
                        reason: CancelReason::UserRequest,
                    }) {
                        warn!(error = %e, "EngineEvent 전송 실패");
                    }
                }
            };
        }
    }
}
