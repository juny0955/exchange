use std::time::Duration;

use crossbeam::channel::Receiver;
use rdkafka::{
    ClientConfig,
    producer::{BaseRecord, DefaultProducerContext, Producer, ThreadedProducer},
};
use tracing::{error, warn};

use crate::{
    engine::EngineEvent,
    kafka::{
        KafkaConfig,
        error::KafkaProducerError,
        produce_dto::{AcceptedMessage, CanceledMessage, MatchedMessage, RejectedMessage},
    },
};

pub struct KafkaProducer {
    producer: ThreadedProducer<DefaultProducerContext>,
    config: KafkaConfig,
}

impl KafkaProducer {
    pub fn new(config: KafkaConfig) -> Result<Self, KafkaProducerError> {
        let producer = ClientConfig::new()
            .set("bootstrap.servers", &config.bootstrap_servers)
            .set("acks", "all")
            .set("enable.idempotence", "true")
            .set("compression.type", "lz4")
            .create()?;

        Ok(Self { producer, config })
    }

    pub fn run(self, receiver: Receiver<EngineEvent>) {
        for event in receiver {
            if let Err(e) = self.publish(event) {
                warn!(error = %e, "EngineEvent publish 실패");
            }
        }

        if let Err(e) = self.producer.flush(Duration::from_secs(5)) {
            error!(error = %e, "producer flush 실패");
        }
    }

    fn publish(&self, event: EngineEvent) -> Result<(), KafkaProducerError> {
        match event {
            EngineEvent::Accepted {
                symbol,
                order_id,
                account_id,
            } => {
                let message = AcceptedMessage {
                    order_id: order_id.value(),
                    account_id: account_id.value(),
                };
                let payload = serde_json::to_vec(&message)?;
                self.send(&self.config.accepted_topic, &symbol.ticker(), &payload)
            }
            EngineEvent::Canceled {
                symbol,
                order_id,
                account_id,
                reason,
            } => {
                let message = CanceledMessage {
                    order_id: order_id.value(),
                    account_id: account_id.value(),
                    reason: reason.into(),
                };
                let payload = serde_json::to_vec(&message)?;
                self.send(&self.config.cancelled_topic, &symbol.ticker(), &payload)
            }
            EngineEvent::Rejected {
                symbol,
                order_id,
                account_id,
                reason,
            } => {
                let message = RejectedMessage {
                    order_id: order_id.value(),
                    account_id: account_id.value(),
                    reason: reason.into(),
                };
                let payload = serde_json::to_vec(&message)?;
                self.send(&self.config.rejected_topic, &symbol.ticker(), &payload)
            }
            EngineEvent::Matched { symbol, trades } => {
                let messages: Vec<MatchedMessage> =
                    trades.iter().map(MatchedMessage::from).collect();
                let payload = serde_json::to_vec(&messages)?;
                self.send(&self.config.matched_topic, &symbol.ticker(), &payload)
            }
        }
    }

    fn send(&self, topic: &str, key: &str, payload: &[u8]) -> Result<(), KafkaProducerError> {
        self.producer
            .send(BaseRecord::to(topic).key(key).payload(payload))
            .map_err(|(e, _)| KafkaProducerError::Kafka(e))
    }
}
