CREATE TABLE orders (
    order_id        UUID PRIMARY KEY,
    account_id      UUID NOT NULL,
    client_order_id VARCHAR(64) NOT NULL,
    accepted_seq    BIGINT NOT NULL,
    base_asset      VARCHAR(8) NOT NULL,
    quote_asset     VARCHAR(8) NOT NULL,
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
CREATE INDEX idx_order_account_id ON orders (order_id, account_id);

CREATE TABLE trades (
    trade_id        UUID NOT NULL,
    order_id        UUID NOT NULL,
    match_order_id  UUID NOT NULL,
    base_asset      VARCHAR(8) NOT NULL,
    quote_asset     VARCHAR(8) NOT NULL,
    side            VARCHAR(8) NOT NULL,
    price           DECIMAL(32, 16) NOT NULL,
    quantity        DECIMAL(32, 16) NOT NULL,
    quote_qty       DECIMAL(32, 16) NOT NULL,
    trade_at        TIMESTAMPTZ NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,

    PRIMARY KEY (order_id, trade_id)
);

CREATE TABLE order_history (
    order_history_id BIGSERIAL PRIMARY KEY,
    order_id        UUID NOT NULL,
    from_status     VARCHAR(32),
    to_status       VARCHAR(32) NOT NULL,
    reason          VARCHAR(32),
    detail          TEXT,
    created_at      TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_order_id_created_at ON order_history (order_id, created_at);

CREATE TABLE order_outbox (
    outbox_id       UUID PRIMARY KEY,
    order_id        UUID NOT NULL,
    aggregate_type  VARCHAR(32) NOT NULL DEFAULT 'ORDER',
    event_type      VARCHAR(32) NOT NULL,
    payload         JSONB NOT NULL,
    status          VARCHAR(32) NOT NULL,
    retry_count     INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL,
    published_at    TIMESTAMPTZ
);

CREATE TABLE engine_failed_event (
    failed_id       BIGSERIAL PRIMARY KEY,
    topic           VARCHAR(64) NOT NULL,
    partition       INT NOT NULL,
    event_offset    BIGINT NOT NULL,
    payload         JSONB NOT NULL,
    error_message   TEXT,
    status          VARCHAR(32) NOT NULL,
    retry_count     INT NOT NULL DEFAULT 0,
    failed_at       TIMESTAMPTZ NOT NULL,
    resolved_at     TIMESTAMPTZ,

    CONSTRAINT uk_kafka_position UNIQUE (topic, partition, event_offset)
);
