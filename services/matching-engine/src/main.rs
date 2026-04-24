use crossbeam::channel;
use matching_engine::{
    engine::{EngineEvent, EngineManager},
    models::Symbol,
};

fn main() {
    let (event_sender, event_receiver) = channel::unbounded::<EngineEvent>();

    let mut manager = EngineManager::new(event_sender);
    manager.register_symbol(Symbol {
        base_asset: "BTC".into(),
        quote_asset: "KRW".into(),
    });

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
