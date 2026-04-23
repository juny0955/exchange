use std::collections::HashMap;

use crossbeam::channel::Sender;

use crate::{engine::command::OrderCommand, models::Symbol};

pub struct EngineRouter {
    workers: HashMap<Symbol, Sender<OrderCommand>>,
}

impl EngineRouter {
    pub fn new() -> Self {
        Self {
            workers: HashMap::new(),
        }
    }

    pub fn add_worker(&mut self, symbol: Symbol, worker: Sender<OrderCommand>) {
        self.workers.entry(symbol).or_insert(worker);
    }

    pub fn dispatch(&self, command: OrderCommand) {
        if let Some(worker) = self.workers.get(command.symbol()) {
            worker.send(command).ok();
        }
    }
}
