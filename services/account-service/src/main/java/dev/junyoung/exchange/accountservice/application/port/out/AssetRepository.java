package dev.junyoung.exchange.accountservice.application.port.out;

import dev.junyoung.exchange.accountservice.domain.model.entity.Asset;
import dev.junyoung.exchange.accountservice.domain.model.value.AssetCode;

import java.util.Optional;

public interface AssetRepository {
    Optional<Asset> findById(AssetCode assetCode);
}
