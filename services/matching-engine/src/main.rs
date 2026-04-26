use crossbeam::channel::{self, Sender};
use matching_engine::kafka::{KafkaConfig, KafkaConsumer};
use matching_engine::{
    engine::{EngineEvent, EngineManager},
    models::Symbol,
};
use std::process::exit;
use std::thread;

fn main() {
    let kafka_config = init_kafka_config();

    let (event_sender, event_receiver) = channel::unbounded::<EngineEvent>();
    let manager = init_engine_manager(event_sender);

    let consumer = init_kafka_consumer(kafka_config, manager);

    thread::scope(|scope| {
        scope.spawn(|| {
            consumer.run();
        });

        // 이벤트 수신 TODO producer 교체
        for event in event_receiver {
            match event {
                EngineEvent::Matched(trades) => {
                    println!("체결: {:?}", trades)
                }
                EngineEvent::Canceled {
                    order_id,
                    account_id,
                    reason,
                } => {
                    println!(
                        "취소: order_id={:?}, account_id={:?}, reason={:?}",
                        order_id, account_id, reason
                    )
                }
                EngineEvent::Rejected {
                    order_id,
                    account_id,
                    reason,
                } => {
                    println!(
                        "거부: order_id={:?}, account_id={:?}, reason={:?}",
                        order_id, account_id, reason
                    )
                }
            }
        }
    });
}

fn init_kafka_config() -> KafkaConfig {
    match KafkaConfig::from_env() {
        Ok(c) => c,
        Err(e) => {
            eprintln!("KafkaConfig 로딩 실패: {e}");
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

fn init_kafka_consumer(kafka_config: KafkaConfig, manager: EngineManager) -> KafkaConsumer {
    match KafkaConsumer::new(kafka_config, manager) {
        Ok(c) => c,
        Err(e) => {
            eprintln!("KafkaConsumer 초기화 실패: {e}");
            exit(1);
        }
    }
}
