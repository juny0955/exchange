package dev.junyoung.exchange.accountservice.adapter.out.persistence.asset;

import dev.junyoung.exchange.accountservice.domain.model.entity.Asset;
import dev.junyoung.exchange.accountservice.domain.model.enums.AssetStatus;
import dev.junyoung.exchange.accountservice.domain.model.value.AssetCode;
import dev.junyoung.exchange.accountservice.tables.records.AssetsRecord;

final class JooqAssetMapper {

    static Asset toDomain(AssetsRecord record) {
        return new Asset(
            new AssetCode(record.getAssetCode()),
            AssetStatus.valueOf(record.getStatus()),
            record.getCreatedAt(),
            record.getUpdatedAt()
        );
    }
}
