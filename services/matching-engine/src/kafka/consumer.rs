use crate::engine::EngineManager;
use crate::kafka::config::KafkaConfig;
use crate::kafka::dto::{CancelOrderDto, PlaceOrderDto};
use crate::kafka::error::KafkaConsumerError;
use rdkafka::consumer::{BaseConsumer, CommitMode, Consumer};
use rdkafka::{ClientConfig, Message};

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

    pub fn run(&self) {
        loop {
            match self.consumer.poll(self.config.poll_timeout) {
                None => continue,
                Some(Err(e)) => {
                    eprintln!("kafka poll 오류: {e}");
                    continue;
                }
                Some(Ok(message)) => {
                    let topic = message.topic().to_string();
                    let payload = match message.payload() {
                        Some(p) => p,
                        None => {
                            let _ = self.consumer.commit_message(&message, CommitMode::Async);
                            continue;
                        }
                    };

                    let result = if topic == self.config.place_topic {
                        self.handle_place(payload)
                    } else if topic == self.config.cancel_topic {
                        self.handle_cancel(payload)
                    } else {
                        Ok(())
                    };

                    if let Err(e) = result {
                        eprintln!("Message 처리 실패: {e}");
                    }

                    let _ = self.consumer.commit_message(&message, CommitMode::Async);
                }
            }
        }
    }

    fn handle_place(&self, payload: &[u8]) -> Result<(), KafkaConsumerError> {
        let dto: PlaceOrderDto = serde_json::from_slice(payload)?;
        let cmd = dto.try_into_command()?;
        self.manager.submit(cmd);
        Ok(())
    }

    fn handle_cancel(&self, payload: &[u8]) -> Result<(), KafkaConsumerError> {
        let dto: CancelOrderDto = serde_json::from_slice(payload)?;
        self.manager.submit(dto.into_command());
        Ok(())
    }
}
