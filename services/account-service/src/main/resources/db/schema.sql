CREATE TABLE accounts (
    account_id      UUID PRIMARY KEY,
    status          VARCHAR(32) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL
);

CREATE TABLE assets (
    asset_code      VARCHAR(8) PRIMARY KEY,
    status          VARCHAR(32) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL
);

CREATE TABLE balances (
    account_id      UUID NOT NULL,
    asset_code      VARCHAR(8) NOT NULL,
    available       DECIMAL(32, 16) NOT NULL DEFAULT 0,
    held            DECIMAL(32, 16) NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (account_id, asset_code),

    CONSTRAINT fk_balances_account
        FOREIGN KEY (account_id) REFERENCES accounts (account_id),
    CONSTRAINT fk_balances_asset
        FOREIGN KEY (asset_code) REFERENCES assets (asset_code),

    CONSTRAINT chk_balances_available_non_negative CHECK (available >= 0),
    CONSTRAINT chk_balances_held_non_negative CHECK (held >= 0)
);

CREATE TABLE reservations (
    reservation_id  BIGSERIAL PRIMARY KEY,
    order_id        UUID NOT NULL,
    account_id      UUID NOT NULL,
    asset_code      VARCHAR(8) NOT NULL,
    amount          DECIMAL(32, 16) NOT NULL,
    released_amount DECIMAL(32, 16) NOT NULL DEFAULT 0,
    status          VARCHAR(32) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_balance_reservations_order UNIQUE (order_id),

    CONSTRAINT fk_balance_reservations_account_asset
        FOREIGN KEY (account_id, asset_code) REFERENCES balances (account_id, asset_code),

    CONSTRAINT chk_balance_reservations_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_balance_reservations_released_valid CHECK (released_amount > 0 AND released_amount <= amount)
);

CREATE TABLE ledger_entries (
    ledger_entry_id BIGSERIAL PRIMARY KEY,
    account_id      UUID NOT NULL,
    asset_code      VARCHAR(8) NOT NULL,
    amount          DECIMAL(32, 16) NOT NULL,
    balance_type    VARCHAR(16) NOT NULL,
    entry_type      VARCHAR(16) NOT NULL,
    reference_type  VARCHAR(32) NOT NULL,
    reference_id    UUID NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_ledger_entries_account_asset
        FOREIGN KEY (account_id, asset_code) REFERENCES balances (account_id, asset_code),

    CONSTRAINT chk_ledger_entries_amount_non_negative CHECK (amount >= 0)
);
