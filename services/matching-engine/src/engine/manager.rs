use crossbeam::channel;
use crossbeam::channel::Sender;
use std::thread::{self, JoinHandle};
use tracing::{info, warn};

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
    worker_handles: Vec<JoinHandle<()>>,
}

impl EngineManager {
    pub fn new(event_sender: Sender<EngineEvent>) -> Self {
        Self {
            router: EngineRouter::new(),
            event_sender,
            worker_handles: Vec::new(),
        }
    }

    pub fn register_symbol(&mut self, symbol: Symbol) {
        let (command_tx, command_rx) = channel::bounded(100_000); // TODO 심볼별 버퍼크기 동적 조절

        let worker = SymbolWorker::new(OrderBook::new(), command_rx, self.event_sender.clone());
        let handle = thread::Builder::new()
            .name(format!("{}-worker", symbol.ticker()))
            .spawn(move || {
                worker.run();
            })
            .expect("worker 스레드 실행 실패");

        self.worker_handles.push(handle);

        let ticker = symbol.ticker();
        self.router.add_worker(symbol, command_tx);
        info!(symbol = %ticker, "심볼 워커 등록");
    }

    pub fn submit(&self, command: OrderCommand) {
        let order_id = command.order_id();
        let account_id = command.account_id();
        if let Err(engine_error) = self.router.dispatch(command) {
            let rejected_event = EngineEvent::Rejected {
                order_id,
                account_id,
                reason: engine_error,
            };

            if let Err(e) = self.event_sender.send(rejected_event) {
                warn!(error = %e, ?order_id, "Rejected 이벤트 전송 실패");
            }
        }

        if let Err(e) = self.event_sender.send(EngineEvent::Accepted {
            order_id,
            account_id,
        }) {
            warn!(error = %e, ?order_id, "Accepted 이벤트 전송 실패");
        }
    }

    pub fn shutdown(mut self) {
        self.router.close();
        drop(self.event_sender);
        for handle in self.worker_handles.drain(..) {
            if let Err(e) = handle.join() {
                warn!(?e, "워커 스레드 panic");
            }
        }
    }
}
