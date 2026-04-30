package dev.junyoung.exchange.accountservice.domain.model.entity;

import dev.junyoung.exchange.accountservice.domain.exception.AccountStateConflictException;
import dev.junyoung.exchange.accountservice.domain.model.enums.AssetStatus;
import dev.junyoung.exchange.accountservice.domain.model.value.AssetCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class Asset {
    private final AssetCode assetCode;
    private AssetStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public static Asset create(AssetCode assetCode) {
        Instant now = Instant.now();
        return new Asset(assetCode, AssetStatus.ACTIVE, now, now);
    }

    public void deactivate() {
        if (status == AssetStatus.INACTIVE) throw new AccountStateConflictException("이미 비활성화된 자산입니다.");
        status = AssetStatus.INACTIVE;
        updatedAt = Instant.now();
    }

    public void activate() {
        if (status == AssetStatus.ACTIVE) throw new AccountStateConflictException("이미 활성화된 자산입니다.");
        status = AssetStatus.ACTIVE;
        updatedAt = Instant.now();
    }

    public boolean isActive() {
        return status == AssetStatus.ACTIVE;
    }
}
