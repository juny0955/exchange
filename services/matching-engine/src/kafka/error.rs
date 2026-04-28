#[derive(Debug, thiserror::Error)]
pub enum KafkaConsumerError {
    #[error("rdkafka 오류: {0}")]
    Kafka(#[from] rdkafka::error::KafkaError),

    #[error("잘못된 페이로드: {0}")]
    InvalidPayload(String),

    #[error("JSON 역직렬화 실패: {0}")]
    Decode(#[from] serde_json::Error),
}

#[derive(Debug, thiserror::Error)]
pub enum KafkaProducerError {
    #[error("rdkafka 오류: {0}")]
    Kafka(#[from] rdkafka::error::KafkaError),

    #[error("JSON 직렬화 실패: {0}")]
    Encode(#[from] serde_json::Error),
}
