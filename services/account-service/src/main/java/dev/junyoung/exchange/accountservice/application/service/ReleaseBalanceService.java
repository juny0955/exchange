package dev.junyoung.exchange.accountservice.application.service;

import dev.junyoung.exchange.accountservice.application.exception.BalanceNotFoundException;
import dev.junyoung.exchange.accountservice.application.exception.ReservationNotFoundException;
import dev.junyoung.exchange.accountservice.application.port.in.ReleaseBalanceUseCase;
import dev.junyoung.exchange.accountservice.application.port.in.command.ReleaseBalanceCommand;
import dev.junyoung.exchange.accountservice.application.port.out.*;
import dev.junyoung.exchange.accountservice.domain.model.LedgerEntryFactory;
import dev.junyoung.exchange.accountservice.domain.model.entity.Balance;
import dev.junyoung.exchange.accountservice.domain.model.entity.BalanceReservation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class ReleaseBalanceService implements ReleaseBalanceUseCase {

    private final BalanceRepository balanceRepository;
    private final BalanceReservationRepository balanceReservationRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    @Override
    public void release(ReleaseBalanceCommand command) {
        BalanceReservation reservation = balanceReservationRepository.findByOrderIdAndAccountIdForUpdate(command.orderId(), command.accountId())
            .orElseThrow(ReservationNotFoundException::new);

        BigDecimal releaseAmount = reservation.getRemainingHeld();

        Balance balance = balanceRepository.findByAccountIdAndAssetCodeForUpdate(reservation.getAccountId(), reservation.getAssetCode())
            .orElseThrow(BalanceNotFoundException::new);

        reservation.release(releaseAmount);
        balance.release(releaseAmount);

        balanceReservationRepository.update(reservation);
        balanceRepository.update(balance);
        ledgerEntryRepository.saveAll(
            LedgerEntryFactory.createForRelease(
                reservation.getAccountId(),
                reservation.getAssetCode(),
                releaseAmount,
                command.orderId()
            )
        );
    }
}
