package dev.junyoung.exchange.accountservice.application.service;

import dev.junyoung.exchange.accountservice.application.exception.AccountInactiveException;
import dev.junyoung.exchange.accountservice.application.exception.AccountNotFoundException;
import dev.junyoung.exchange.accountservice.application.exception.AssetInactiveException;
import dev.junyoung.exchange.accountservice.application.exception.AssetNotFoundException;
import dev.junyoung.exchange.accountservice.application.exception.BalanceNotFoundException;
import dev.junyoung.exchange.accountservice.application.port.in.command.ReserveBalanceCommand;
import dev.junyoung.exchange.accountservice.application.port.out.AccountRepository;
import dev.junyoung.exchange.accountservice.application.port.out.AssetRepository;
import dev.junyoung.exchange.accountservice.application.port.out.BalanceRepository;
import dev.junyoung.exchange.accountservice.application.port.out.LedgerEntryRepository;
import dev.junyoung.exchange.accountservice.application.port.out.ReservationRepository;
import dev.junyoung.exchange.accountservice.domain.exception.AccountStateConflictException;
import dev.junyoung.exchange.accountservice.domain.model.entity.Account;
import dev.junyoung.exchange.accountservice.domain.model.entity.Asset;
import dev.junyoung.exchange.accountservice.domain.model.entity.Balance;
import dev.junyoung.exchange.accountservice.domain.model.value.AccountId;
import dev.junyoung.exchange.accountservice.domain.model.value.AssetCode;
import dev.junyoung.exchange.accountservice.domain.model.value.OrderId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ReserveBalanceServiceTest {

	@Mock AccountRepository accountRepository;
	@Mock AssetRepository assetRepository;
	@Mock BalanceRepository balanceRepository;
	@Mock ReservationRepository reservationRepository;
	@Mock LedgerEntryRepository ledgerEntryRepository;

	@InjectMocks ReserveBalanceService service;

	private OrderId orderId;
	private AccountId accountId;
	private AssetCode assetCode;
	private ReserveBalanceCommand command;
	private static final BigDecimal AMOUNT = new BigDecimal("100.0");

	@BeforeEach
	void setUp() {
		orderId = new OrderId(UUID.randomUUID());
		accountId = new AccountId(UUID.randomUUID());
		assetCode = new AssetCode("BTC");
		command = new ReserveBalanceCommand(orderId, accountId, assetCode, AMOUNT);
	}

	// ── 멱등성 ────────────────────────────────────────────────

	@Test
	void 이미_처리된_예약은_노옵으로_반환한다() {
		given(reservationRepository.existsOrderIdAndAccountId(orderId, accountId)).willReturn(true);

		service.reserve(command);

		then(accountRepository).should(never()).findById(any());
		then(balanceRepository).should(never()).findByAccountIdAndAssetCodeForUpdate(any(), any());
		then(reservationRepository).should(never()).save(any());
		then(ledgerEntryRepository).should(never()).saveAll(any());
	}

	@Test
	void 동일_계좌의_다른_주문은_정상_처리한다() {
		OrderId otherId = new OrderId(UUID.randomUUID());
		ReserveBalanceCommand otherCommand = new ReserveBalanceCommand(otherId, accountId, assetCode, AMOUNT);
		Balance balance = Balance.createZero(accountId, assetCode);
		balance.deposit(AMOUNT);

		given(reservationRepository.existsOrderIdAndAccountId(otherId, accountId)).willReturn(false);
		given(accountRepository.findById(accountId)).willReturn(Optional.of(Account.create()));
		given(assetRepository.findById(assetCode)).willReturn(Optional.of(Asset.create(assetCode)));
		given(balanceRepository.findByAccountIdAndAssetCodeForUpdate(accountId, assetCode)).willReturn(Optional.of(balance));

		assertThatNoException().isThrownBy(() -> service.reserve(otherCommand));
		then(reservationRepository).should().save(any());
	}

	// ── 정상 흐름 ─────────────────────────────────────────────

	@Test
	void 신규_예약은_잔고_차감_예약_저장_원장_2건을_수행한다() {
		Balance balance = Balance.createZero(accountId, assetCode);
		balance.deposit(AMOUNT);

		given(reservationRepository.existsOrderIdAndAccountId(orderId, accountId)).willReturn(false);
		given(accountRepository.findById(accountId)).willReturn(Optional.of(Account.create()));
		given(assetRepository.findById(assetCode)).willReturn(Optional.of(Asset.create(assetCode)));
		given(balanceRepository.findByAccountIdAndAssetCodeForUpdate(accountId, assetCode)).willReturn(Optional.of(balance));

		service.reserve(command);

		then(balanceRepository).should().update(balance);
		then(reservationRepository).should().save(any());
		then(ledgerEntryRepository).should().saveAll(argThat(entries -> entries.size() == 2));
	}

	// ── 예외 흐름 ─────────────────────────────────────────────

	@Test
	void 계좌가_없으면_AccountNotFoundException을_던진다() {
		given(reservationRepository.existsOrderIdAndAccountId(orderId, accountId)).willReturn(false);
		given(accountRepository.findById(accountId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> service.reserve(command))
			.isInstanceOf(AccountNotFoundException.class);
	}

	@Test
	void 계좌가_비활성이면_AccountInactiveException을_던진다() {
		Account inactive = Account.create();
		inactive.suspend();

		given(reservationRepository.existsOrderIdAndAccountId(orderId, accountId)).willReturn(false);
		given(accountRepository.findById(accountId)).willReturn(Optional.of(inactive));

		assertThatThrownBy(() -> service.reserve(command))
			.isInstanceOf(AccountInactiveException.class);
	}

	@Test
	void 자산이_없으면_AssetNotFoundException을_던진다() {
		given(reservationRepository.existsOrderIdAndAccountId(orderId, accountId)).willReturn(false);
		given(accountRepository.findById(accountId)).willReturn(Optional.of(Account.create()));
		given(assetRepository.findById(assetCode)).willReturn(Optional.empty());

		assertThatThrownBy(() -> service.reserve(command))
			.isInstanceOf(AssetNotFoundException.class);
	}

	@Test
	void 자산이_비활성이면_AssetInactiveException을_던진다() {
		Asset inactive = Asset.create(assetCode);
		inactive.deactivate();

		given(reservationRepository.existsOrderIdAndAccountId(orderId, accountId)).willReturn(false);
		given(accountRepository.findById(accountId)).willReturn(Optional.of(Account.create()));
		given(assetRepository.findById(assetCode)).willReturn(Optional.of(inactive));

		assertThatThrownBy(() -> service.reserve(command))
			.isInstanceOf(AssetInactiveException.class);
	}

	@Test
	void 잔고가_없으면_BalanceNotFoundException을_던진다() {
		given(reservationRepository.existsOrderIdAndAccountId(orderId, accountId)).willReturn(false);
		given(accountRepository.findById(accountId)).willReturn(Optional.of(Account.create()));
		given(assetRepository.findById(assetCode)).willReturn(Optional.of(Asset.create(assetCode)));
		given(balanceRepository.findByAccountIdAndAssetCodeForUpdate(accountId, assetCode)).willReturn(Optional.empty());

		assertThatThrownBy(() -> service.reserve(command))
			.isInstanceOf(BalanceNotFoundException.class);
	}

	@Test
	void 가용잔고_부족이면_AccountStateConflictException을_던진다() {
		Balance zeroBalance = Balance.createZero(accountId, assetCode);

		given(reservationRepository.existsOrderIdAndAccountId(orderId, accountId)).willReturn(false);
		given(accountRepository.findById(accountId)).willReturn(Optional.of(Account.create()));
		given(assetRepository.findById(assetCode)).willReturn(Optional.of(Asset.create(assetCode)));
		given(balanceRepository.findByAccountIdAndAssetCodeForUpdate(accountId, assetCode)).willReturn(Optional.of(zeroBalance));

		assertThatThrownBy(() -> service.reserve(command))
			.isInstanceOf(AccountStateConflictException.class);
	}
}
