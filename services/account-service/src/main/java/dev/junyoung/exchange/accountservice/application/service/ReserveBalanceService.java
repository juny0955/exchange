package dev.junyoung.exchange.accountservice.application.service;

import dev.junyoung.exchange.accountservice.application.exception.*;
import dev.junyoung.exchange.accountservice.application.port.in.ReserveBalanceUseCase;
import dev.junyoung.exchange.accountservice.application.port.in.command.ReserveBalanceCommand;
import dev.junyoung.exchange.accountservice.application.port.out.*;
import dev.junyoung.exchange.accountservice.domain.exception.AccountStateConflictException;
import dev.junyoung.exchange.accountservice.domain.model.LedgerEntryFactory;
import dev.junyoung.exchange.accountservice.domain.model.entity.*;
import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import dev.junyoung.exchange.accountservice.domain.model.value.AssetCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ReserveBalanceService implements ReserveBalanceUseCase {

    private final AccountRepository accountRepository;
    private final AssetRepository assetRepository;
    private final BalanceRepository balanceRepository;
    private final BalanceReservationRepository balanceReservationRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    @Override
    public void reserve(ReserveBalanceCommand command) {
        validateAccount(command.accountId());
        validateAsset(command.assetCode());

        reserveBalance(command.accountId(), command.assetCode(), command.amount());
        saveBalanceReservation(command);
        saveLedgerEntries(command);
    }

    /**
     * 계좌 검증 진행
     * @param accountId 계좌 ID
     * @throws AccountNotFoundException 계좌를 찾을 수 없는 경우
     * @throws AccountInactiveException 계좌가 비활성 상태인 경우
     */
    private void validateAccount(AccountId accountId) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(AccountNotFoundException::new);

        if (!account.isActive())
            throw new AccountInactiveException();
    }

    /**
     * Asset 검증 진행
     * @param assetCode AssetCode
     * @throws AssetNotFoundException 자산을 찾을 수 없는 경우
     * @throws AssetInactiveException 자산이 비활성 상태인 경우
     */
    private void validateAsset(AssetCode assetCode) {
        Asset asset = assetRepository.findById(assetCode)
            .orElseThrow(AssetNotFoundException::new);

        if (!asset.isActive())
            throw new AssetInactiveException();
    }

    /**
     * 잔고 예약 진행
     * @param accountId 계좌 ID
     * @param assetCode AssetCode
     * @param amount 예약 금액
     * @throws BalanceNotFoundException 잔고를 찾을 수 없는 경우
     * @throws AccountStateConflictException 가용 잔고 부족 시
     */
    private void reserveBalance(AccountId accountId, AssetCode assetCode, BigDecimal amount) {
        Balance balance = balanceRepository.findByAccountIdAndAssetCode(accountId, assetCode)
            .orElseThrow(BalanceNotFoundException::new);

        balance.reserve(amount);
        balanceRepository.update(balance);
    }

    /**
     * 잔고 예약 기록 생성 및 저장
     * @param command 예약 커맨드
     */
    private void saveBalanceReservation(ReserveBalanceCommand command) {
        BalanceReservation balanceReservation = BalanceReservation.create(
            command.orderId(),
            command.accountId(),
            command.assetCode(),
            command.amount()
        );

        balanceReservationRepository.save(balanceReservation);
    }

    /**
     * 예약으로 인한 잔고 변동을 원장에 기록
     * AVAILABLE DEBIT + HELD CREDIT 2건을 원자적으로 저장
     * @param command 예약 커맨드
     */
    private void saveLedgerEntries(ReserveBalanceCommand command) {
        List<LedgerEntry> ledgerEntries = LedgerEntryFactory.createForOrder(
            command.accountId(),
            command.assetCode(),
            command.amount(),
            command.orderId()
        );

        ledgerEntryRepository.saveAll(ledgerEntries);
    }
}
