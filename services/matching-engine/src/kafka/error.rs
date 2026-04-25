#[derive(Debug, thiserror::Error)]
pub enum KafkaConsumerError {
    #[error("rdkafka 오류: {0}")]
    Kafka(#[from] rdkafka::error::KafkaError),
}
