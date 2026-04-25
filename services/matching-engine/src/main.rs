use crossbeam::channel;
use matching_engine::kafka::{KafkaConfig, KafkaConsumer};
use matching_engine::{
    engine::{EngineEvent, EngineManager},
    models::Symbol,
};
use std::process::exit;

fn main() {
    let kafka_config = match KafkaConfig::from_env() {
        Ok(c) => c,
        Err(e) => {
            eprintln!("KafkaConfig 로딩 실패: {e}");
            exit(1);
        }
    };

    let (event_sender, event_receiver) = channel::unbounded::<EngineEvent>();
    let mut manager = EngineManager::new(event_sender);
    manager.register_symbol(Symbol {
        base_asset: "BTC".into(),
        quote_asset: "KRW".into(),
    });

    let consumer = match KafkaConsumer::new(kafka_config, manager) {
        Ok(c) => c,
        Err(e) => {
            eprintln!("KafkaConsumer 초기화 실패: {e}");
            exit(1);
        }
    };
    consumer.run();

    // 이벤트 수신
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
}
