use crossbeam::channel;
use crossbeam::channel::Sender;
use std::thread;

use crate::engine::OrderCommand;
use crate::{
    engine::{
        event::EngineEvent, orderbook::OrderBook, router::EngineRouter, worker::SymbolWorker,
    },
    models::Symbol,
};

pub struct EngineManager {
    router: EngineRouter,
    event_sender: Sender<EngineEvent>,
}

impl EngineManager {
    pub fn new(event_sender: Sender<EngineEvent>) -> Self {
        Self {
            router: EngineRouter::new(),
            event_sender,
        }
    }

    pub fn register_symbol(&mut self, symbol: Symbol) {
        let (command_tx, command_rx) = channel::bounded(100_000); // TODO 심볼별 버퍼크기 동적 조절

        let worker = SymbolWorker::new(OrderBook::new(), command_rx, self.event_sender.clone());
        thread::Builder::new()
            .name(format!("{}-worker", symbol.ticker()))
            .spawn(move || {
                worker.run();
            })
            .expect("worker 스레드 실행 실패");

        self.router.add_worker(symbol, command_tx);
    }

    pub fn submit(&self, command: OrderCommand) {
        let order_id = command.order_id();
        let account_id = command.account_id();
        if let Err(engine_error) = self.router.dispatch(command) {
            let _ = self.event_sender.send(EngineEvent::Rejected {
                order_id,
                account_id,
                reason: engine_error,
            });
        }
    }
}
