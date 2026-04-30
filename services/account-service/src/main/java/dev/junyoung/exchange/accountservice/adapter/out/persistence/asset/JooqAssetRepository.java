package dev.junyoung.exchange.accountservice.adapter.out.persistence.asset;

import dev.junyoung.exchange.accountservice.Tables;
import dev.junyoung.exchange.accountservice.application.port.out.AssetRepository;
import dev.junyoung.exchange.accountservice.domain.model.entity.Asset;
import dev.junyoung.exchange.accountservice.domain.model.value.AssetCode;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JooqAssetRepository implements AssetRepository {

    private final DSLContext dslContext;

    @Override
    public Optional<Asset> findById(AssetCode assetCode) {
        return Optional.ofNullable(
            dslContext.selectFrom(Tables.ASSETS)
                .where(Tables.ASSETS.ASSET_CODE.eq(assetCode.value()))
                .fetchOne(JooqAssetMapper::toDomain)
        );
    }
}
