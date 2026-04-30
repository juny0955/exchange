package dev.junyoung.exchange.accountservice.application.service;

import dev.junyoung.exchange.accountservice.application.exception.BalanceNotFoundException;
import dev.junyoung.exchange.accountservice.application.exception.ReservationNotFoundException;
import dev.junyoung.exchange.accountservice.application.port.in.SettleBalanceUseCase;
import dev.junyoung.exchange.accountservice.application.port.in.command.SettleBalanceCommand;
import dev.junyoung.exchange.accountservice.application.port.out.*;
import dev.junyoung.exchange.accountservice.domain.model.LedgerEntryFactory;
import dev.junyoung.exchange.accountservice.domain.model.entity.Balance;
import dev.junyoung.exchange.accountservice.domain.model.entity.LedgerEntry;
import dev.junyoung.exchange.accountservice.domain.model.entity.Reservation;
import dev.junyoung.exchange.accountservice.domain.model.enums.ReferenceType;
import dev.junyoung.exchange.accountservice.domain.model.value.AssetCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class SettleBalanceService implements SettleBalanceUseCase {

    private final BalanceRepository balanceRepository;
    private final ReservationRepository reservationRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    @Override
    public void settle(List<SettleBalanceCommand> commands) {
        if (commands.isEmpty()) return;

        if (ledgerEntryRepository.existsByReferenceTypeAndReferenceId(ReferenceType.TRADE, commands.getFirst().tradeId().value()))
            return;

        Map<ReservationLockKey, Reservation> reservations = loadReservations(commands);
        Map<BalanceLockKey, Balance> balances = loadBalances(commands, reservations);

        List<LedgerEntry> ledgerEntries = new ArrayList<>();
        for (SettleBalanceCommand command : commands)
            applySettlement(command, reservations, balances, ledgerEntries);

        reservationRepository.updateAll(new ArrayList<>(reservations.values()));
        balanceRepository.upsertAll(new ArrayList<>(balances.values()));
        ledgerEntryRepository.saveAll(ledgerEntries);
    }

    private Map<ReservationLockKey, Reservation> loadReservations(List<SettleBalanceCommand> commands) {
        List<ReservationLockKey> keys = commands.stream()
            .flatMap(c -> Stream.of(
                new ReservationLockKey(c.buyOrderId(), c.buyAccountId()),
                new ReservationLockKey(c.sellOrderId(), c.sellAccountId())
            ))
            .distinct()
            .toList();

        List<Reservation> reservations = reservationRepository.findAllByLockKeyForUpdate(keys);
        Map<ReservationLockKey, Reservation> map = reservations.stream()
            .collect(Collectors.toMap(
                r -> new ReservationLockKey(r.getOrderId(), r.getAccountId()),
                r -> r
            ));

        if (map.size() != keys.size())
            throw new ReservationNotFoundException();

        return map;
    }

    private Map<BalanceLockKey, Balance> loadBalances(List<SettleBalanceCommand> commands, Map<ReservationLockKey, Reservation> reservations) {
        List<BalanceLockKey> keys = commands.stream()
            .flatMap(c -> {
                AssetCode quote = reservations.get(new ReservationLockKey(c.buyOrderId(), c.buyAccountId())).getAssetCode();
                AssetCode base = reservations.get(new ReservationLockKey(c.sellOrderId(), c.sellAccountId())).getAssetCode();
                return Stream.of(
                    new BalanceLockKey(c.buyAccountId(), quote),    // 매수자 quote: held 차감 → 반드시 존재
                    new BalanceLockKey(c.buyAccountId(), base),     // 매수자 base : 신규 가능
                    new BalanceLockKey(c.sellAccountId(), base),    // 매도자 base: held 차감 → 반드시 존재
                    new BalanceLockKey(c.sellAccountId(), quote)    // 매도자 quote: 신규 가능
                );
            })
            .distinct()
            .toList();

        return balanceRepository.findAllByLockKeyForUpdate(keys).stream()
            .collect(Collectors.toMap(
                b -> new BalanceLockKey(b.getAccountId(), b.getAssetCode()),
                b -> b
            ));
    }

    private void applySettlement(
        SettleBalanceCommand command,
        Map<ReservationLockKey, Reservation> reservations,
        Map<BalanceLockKey, Balance> balances,
        List<LedgerEntry> ledgerEntries
    ) {
        Reservation buyerReservation = reservations.get(new ReservationLockKey(command.buyOrderId(), command.buyAccountId()));
        Reservation sellerReservation = reservations.get(new ReservationLockKey(command.sellOrderId(), command.sellAccountId()));

        buyerReservation.release(command.quoteQty());
        sellerReservation.release(command.quantity());

        AssetCode quoteAsset = buyerReservation.getAssetCode();
        AssetCode baseAsset = sellerReservation.getAssetCode();

        Balance buyerQuote = requireExisting(balances, new BalanceLockKey(command.buyAccountId(), quoteAsset));
        Balance buyerBase = balances.computeIfAbsent(
            new BalanceLockKey(command.buyAccountId(), baseAsset),
            k -> Balance.createZero(k.accountId(), k.assetCode())
        );
        Balance sellerBase = requireExisting(balances, new BalanceLockKey(command.sellAccountId(), baseAsset));
        Balance sellerQuote = balances.computeIfAbsent(
            new BalanceLockKey(command.sellAccountId(), quoteAsset),
            k -> Balance.createZero(k.accountId(), k.assetCode())
        );

        buyerQuote.settleHeld(command.quoteQty());          // 매수자: quote held 차감
        buyerBase.settleAvailable(command.quantity());      // 매수자: base available 증가
        sellerBase.settleHeld(command.quantity());          // 매도자: base held 차감
        sellerQuote.settleAvailable(command.quoteQty());    // 매도자: quote available 증가

        ledgerEntries.addAll(LedgerEntryFactory.createForSettle(
            command.buyAccountId(), command.sellAccountId(),
            quoteAsset, baseAsset,
            command.quoteQty(), command.quantity(),
            command.tradeId()
        ));
    }

    private Balance requireExisting(Map<BalanceLockKey, Balance> balances, BalanceLockKey key) {
        Balance balance = balances.get(key);
        if (balance == null) throw new BalanceNotFoundException();
        return balance;
    }
}
