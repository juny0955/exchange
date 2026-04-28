use crossbeam::channel::{self, Sender};
use matching_engine::kafka::{KafkaConfig, KafkaConsumer, KafkaProducer};
use matching_engine::{
    engine::{EngineEvent, EngineManager},
    models::Symbol,
};
use std::io::{IsTerminal, stdout};
use std::process::exit;
use std::sync::Arc;
use std::sync::atomic::{AtomicBool, Ordering};
use std::thread;
use tracing::error;
use tracing_subscriber::EnvFilter;

fn main() {
    init_tracing();
    let kafka_config = init_kafka_config();
    let (event_sender, event_receiver) = channel::unbounded::<EngineEvent>();
    let manager = init_engine_manager(event_sender);

    let shutdown = Arc::new(AtomicBool::new(false));
    let shutdown_signal = shutdown.clone();
    ctrlc::set_handler(move || {
        shutdown_signal.store(true, Ordering::Relaxed);
    })
    .expect("Ctrl-C 시그널 핸들러 등록 실패");

    let consumer = init_kafka_consumer(kafka_config.clone(), manager, shutdown.clone());
    let producer = init_kafka_producer(kafka_config);

    thread::scope(|scope| {
        let manager_handle = scope.spawn(|| {
            let manager = consumer.run();
            manager.shutdown();
        });

        producer.run(event_receiver);

        manager_handle.join().expect("스레드 panic!");
    });
}

fn init_tracing() {
    let filter = EnvFilter::try_from_default_env().unwrap_or_else(|_| EnvFilter::new("info"));

    let json = std::env::var("LOG_FORMAT")
        .map(|v| v.eq_ignore_ascii_case("json"))
        .unwrap_or(false);

    if json {
        tracing_subscriber::fmt()
            .json()
            .with_env_filter(filter)
            .init();
    } else {
        tracing_subscriber::fmt()
            .with_env_filter(filter)
            .with_ansi(IsTerminal::is_terminal(&stdout()))
            .init();
    }
}

fn init_kafka_config() -> KafkaConfig {
    match KafkaConfig::from_env() {
        Ok(c) => c,
        Err(e) => {
            error!(error = %e, "KafkaConfig 로딩 실패");
            exit(1);
        }
    }
}

fn init_engine_manager(event_sender: Sender<EngineEvent>) -> EngineManager {
    let mut engine_manager = EngineManager::new(event_sender);
    engine_manager.register_symbol(Symbol {
        base_asset: "BTC".into(),
        quote_asset: "KRW".into(),
    });

    engine_manager
}

fn init_kafka_consumer(
    kafka_config: KafkaConfig,
    manager: EngineManager,
    shutdown: Arc<AtomicBool>,
) -> KafkaConsumer {
    match KafkaConsumer::new(kafka_config, manager, shutdown) {
        Ok(c) => c,
        Err(e) => {
            error!(error = %e, "KafkaConsumer 초기화 실패");
            exit(1);
        }
    }
}

fn init_kafka_producer(kafka_config: KafkaConfig) -> KafkaProducer {
    match KafkaProducer::new(kafka_config) {
        Ok(p) => p,
        Err(e) => {
            error!(error = %e, "KafkaProducer 초기화 실패");
            exit(1);
        }
    }
}
