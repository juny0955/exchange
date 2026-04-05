CREATE TABLE orders (
    order_id        UUID PRIMARY KEY,
    account_id      UUID NOT NULL,
    client_order_id VARCHAR(64) NOT NULL,
    accepted_seq    BIGINT NOT NULL,
    symbol          VARCHAR(32) NOT NULL,
    side            VARCHAR(8) NOT NULL,
    order_type      VARCHAR(8) NOT NULL,
    tif             VARCHAR(8) NOT NULL,
    price           DECIMAL(32, 16),
    quantity        DECIMAL(32, 16),
    quote_qty       DECIMAL(32, 16),
    cum_base_qty    DECIMAL(32, 16) NOT NULL DEFAULT 0,
    cum_quote_qty   DECIMAL(32, 16) NOT NULL DEFAULT 0,
    status          VARCHAR(32) NOT NULL,
    ordered_at      TIMESTAMPTZ NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_account_client_order UNIQUE (account_id, client_order_id),
    CONSTRAINT uk_accepted_seq UNIQUE (accepted_seq)
);

CREATE TABLE trades (
    trade_id        UUID NOT NULL,
    order_id        UUID NOT NULL,
    match_order_id  UUID NOT NULL,
    symbol          VARCHAR(32) NOT NULL,
    side            VARCHAR(8) NOT NULL,
    price           DECIMAL(32, 16) NOT NULL,
    quantity        DECIMAL(32, 16) NOT NULL,
    quote_qty       DECIMAL(32, 16) NOT NULL,
    trade_at        TIMESTAMPTZ NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,

    PRIMARY KEY (order_id, trade_id)
);
