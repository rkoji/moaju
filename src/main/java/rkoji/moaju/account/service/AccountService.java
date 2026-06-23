package rkoji.moaju.account.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import rkoji.moaju.account.dto.AccountResponse;
import rkoji.moaju.account.dto.CreateAccountRequest;
import rkoji.moaju.account.dto.UpdateAccountRequest;
import rkoji.moaju.account.entity.BrokerageAccount;
import rkoji.moaju.account.repository.BrokerageAccountRepository;
import rkoji.moaju.global.exception.CustomException;
import rkoji.moaju.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class AccountService {

	private final BrokerageAccountRepository accountRepository;

	@Transactional
	public AccountResponse create(Long userId, CreateAccountRequest request) {
		BrokerageAccount account = BrokerageAccount.builder()
			.userId(userId)
			.brokerName(request.brokerName())
			.nickname(request.nickname())
			.build();

		return AccountResponse.from(accountRepository.save(account));
	}

	public List<AccountResponse> getAccounts(Long userId) {
		return accountRepository.findAllByUserId(userId).stream()
			.map(AccountResponse::from)
			.toList();
	}

	public AccountResponse getAccount(Long userId, Long accountId) {
		return AccountResponse.from(findOwnedAccount(userId, accountId));
	}

	@Transactional
	public AccountResponse update(Long userId, Long accountId, UpdateAccountRequest request) {
		BrokerageAccount account = findOwnedAccount(userId, accountId);
		account.updateNickname(request.nickname());
		return AccountResponse.from(account);
	}

	@Transactional
	public void delete(Long userId, Long accountId) {
		BrokerageAccount account = findOwnedAccount(userId, accountId);
		accountRepository.delete(account);
	}

	private BrokerageAccount findOwnedAccount(Long userId, Long accountId) {
		return accountRepository.findByIdAndUserId(accountId, userId)
			.orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
	}
}
