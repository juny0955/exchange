package dev.junyoung.exchange.accountservice.application.exception;

import dev.junyoung.exchange.core.exception.application.ApplicationException;

public class AssetInactiveException extends ApplicationException {
    public AssetInactiveException() {
        super(AccountErrorCode.ASSET_INACTIVE);
    }
}
