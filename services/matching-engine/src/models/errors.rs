use rust_decimal::Decimal;
use thiserror::Error;

#[derive(Error, Debug)]
pub enum ValidationError {
    #[error("가격은 0보다 커야합니다. input: {0}")]
    InvalidPrice(Decimal),

    #[error("수량은 0보다 커야합니다. input: {0}")]
    InvalidQuantity(Decimal),

    #[error("금액은 0보다 커야합니다. input: {0}")]
    InvalidQuoteQty(Decimal),
}
