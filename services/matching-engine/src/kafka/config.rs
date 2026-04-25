use crate::kafka::error::KafkaConsumerError;
use std::time::Duration;

pub struct KafkaConfig {
    pub bootstrap_servers: String,
    pub group_id: String,
    pub place_topic: String,
    pub cancel_topic: String,
    pub poll_timeout: Duration,
}

impl KafkaConfig {
    pub fn from_env() -> Result<Self, KafkaConsumerError> {
        Ok(Self {
            bootstrap_servers: "localhost:9092".to_string(),
            group_id: "matching-engine".to_string(),
            place_topic: "order.PLACE_ORDER".to_string(),
            cancel_topic: "order.CANCEL_ORDER".to_string(),
            poll_timeout: Duration::from_millis(100),
        })
    }
}
