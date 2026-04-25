use crate::engine::EngineManager;
use crate::kafka::config::KafkaConfig;
use crate::kafka::error::KafkaConsumerError;
use rdkafka::ClientConfig;
use rdkafka::consumer::{BaseConsumer, Consumer};

pub struct KafkaConsumer {
    consumer: BaseConsumer,
    config: KafkaConfig,
    manager: EngineManager,
}

impl KafkaConsumer {
    pub fn new(config: KafkaConfig, manager: EngineManager) -> Result<Self, KafkaConsumerError> {
        let consumer: BaseConsumer = ClientConfig::new()
            .set("bootstrap.servers", &config.bootstrap_servers)
            .set("group.id", &config.group_id)
            .set("enable.auto.commit", "false")
            .set("auto.offset.reset", "earliest")
            .set("session.timeout.ms", "10000")
            .create()?;

        consumer.subscribe(&[&config.place_topic, &config.cancel_topic])?;

        Ok(Self {
            consumer,
            config,
            manager,
        })
    }
}
