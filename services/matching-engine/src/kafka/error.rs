#[derive(Debug, thiserror::Error)]
pub enum KafkaConsumerError {
    #[error("rdkafka 오류: {0}")]
    Kafka(#[from] rdkafka::error::KafkaError),

    #[error("잘못된 페이로드: {0}")]
    InvalidPayload(String),
}
