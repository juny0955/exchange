use std::thread;

use crossbeam::channel::Sender;

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
        let (command_tx, command_rx) = crossbeam::channel::unbounded();

        let worker = SymbolWorker::new(OrderBook::new(), command_rx, self.event_sender.clone());
        thread::Builder::new()
            .name(format!("{}-worker", symbol.ticker()))
            .spawn(move || {
                worker.run();
            })
            .expect("worker 스레드 실행 실패");

        self.router.add_worker(symbol, command_tx);
    }
}
