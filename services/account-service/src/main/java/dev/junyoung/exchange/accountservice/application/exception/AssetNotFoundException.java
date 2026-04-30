package dev.junyoung.exchange.accountservice.application.exception;

import dev.junyoung.exchange.core.exception.application.ApplicationException;

public class AssetNotFoundException extends ApplicationException {
    public AssetNotFoundException() {
        super(AccountErrorCode.ASSET_NOT_FOUND);
    }
}
