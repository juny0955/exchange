#[derive(Debug, thiserror::Error)]
pub enum EngineError {
    #[error("채널이 가득 찼습니다")]
    ChannelFull,

    #[error("등록되지 않은 심볼입니다")]
    SymbolNotFound,

    #[error("잘못된 주문 종류입니다")]
    InvalidOrderKind,
}
