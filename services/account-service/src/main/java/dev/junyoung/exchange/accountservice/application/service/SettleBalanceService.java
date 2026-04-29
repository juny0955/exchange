package dev.junyoung.exchange.accountservice.application.service;

import dev.junyoung.exchange.accountservice.application.exception.BalanceNotFoundException;
import dev.junyoung.exchange.accountservice.application.exception.ReservationNotFoundException;
import dev.junyoung.exchange.accountservice.application.port.in.SettleBalanceUseCase;
import dev.junyoung.exchange.accountservice.application.port.in.command.SettleBalanceCommand;
import dev.junyoung.exchange.accountservice.application.port.out.BalanceRepository;
import dev.junyoung.exchange.accountservice.application.port.out.BalanceReservationRepository;
import dev.junyoung.exchange.accountservice.application.port.out.LedgerEntryRepository;
import dev.junyoung.exchange.accountservice.domain.model.LedgerEntryFactory;
import dev.junyoung.exchange.accountservice.domain.model.entity.Balance;
import dev.junyoung.exchange.accountservice.domain.model.entity.BalanceReservation;
import dev.junyoung.exchange.accountservice.domain.model.value.AssetCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SettleBalanceService implements SettleBalanceUseCase {

    private final BalanceRepository balanceRepository;
    private final BalanceReservationRepository balanceReservationRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    @Override
    public void settle(SettleBalanceCommand command) {
        BalanceReservation buyerReservation = balanceReservationRepository.findByOrderIdAndAccountIdForUpdate(command.buyOrderId(), command.buyAccountId())
            .orElseThrow(ReservationNotFoundException::new);
        BalanceReservation sellerReservation = balanceReservationRepository.findByOrderIdAndAccountIdForUpdate(command.sellOrderId(), command.sellAccountId())
            .orElseThrow(ReservationNotFoundException::new);

        buyerReservation.release(command.quoteQty());
        sellerReservation.release(command.quantity());

        AssetCode quoteAsset = buyerReservation.getAssetCode();
        AssetCode baseAsset = sellerReservation.getAssetCode();

        balanceReservationRepository.updateAll(List.of(buyerReservation, sellerReservation));

        settleBalance(command, quoteAsset, baseAsset);

        ledgerEntryRepository.saveAll(
            LedgerEntryFactory.createForSettle(
                command.buyAccountId(), command.sellAccountId(),
                quoteAsset, baseAsset,
                command.quoteQty(), command.quantity(),
                command.tradeId()
            )
        );
    }

    private void settleBalance(SettleBalanceCommand command, AssetCode quoteAsset, AssetCode baseAsset) {
        // 매수자 잔고 조회
        Balance buyerQuote = balanceRepository.findByAccountIdAndAssetCodeForUpdate(command.buyAccountId(), quoteAsset)
            .orElseThrow(BalanceNotFoundException::new);
        Balance buyerBase = balanceRepository.findByAccountIdAndAssetCodeForUpdate(command.buyAccountId(), baseAsset)
            .orElseGet(() -> Balance.createZero(command.buyAccountId(), baseAsset));

        // 매도자 잔고 조회
        Balance sellerBase = balanceRepository.findByAccountIdAndAssetCodeForUpdate(command.sellAccountId(), baseAsset)
            .orElseThrow(BalanceNotFoundException::new);
        Balance sellerQuote = balanceRepository.findByAccountIdAndAssetCodeForUpdate(command.sellAccountId(), quoteAsset)
            .orElseGet(() -> Balance.createZero(command.sellAccountId(), quoteAsset));

        buyerQuote.settleHeld(command.quoteQty());          // 매수자: KRW held 차감
        buyerBase.settleAvailable(command.quantity());      // 매수자: BTC available 추가

        sellerBase.settleHeld(command.quantity());          // 매도자: BTC held 차감
        sellerQuote.settleAvailable(command.quoteQty());    // 매도자: KRW available 추가

        balanceRepository.upsertAll(List.of(buyerQuote, buyerBase, sellerBase, sellerQuote));
    }
}
