package rkoji.moaju.account.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import rkoji.moaju.account.dto.AccountResponse;
import rkoji.moaju.account.dto.CreateAccountRequest;
import rkoji.moaju.account.dto.UpdateAccountRequest;
import rkoji.moaju.account.entity.BrokerageAccount;
import rkoji.moaju.account.repository.BrokerageAccountRepository;
import rkoji.moaju.global.exception.CustomException;
import rkoji.moaju.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

	@Mock
	private BrokerageAccountRepository accountRepository;

	@InjectMocks
	private AccountService accountService;

	private static final Long USER_ID = 1L;

	@Nested
	class 계좌_등록 {

		@Test
		void 성공() {
			// given
			CreateAccountRequest request = new CreateAccountRequest("삼성증권", "메인계좌");
			given(accountRepository.save(any(BrokerageAccount.class)))
				.willAnswer(invocation -> invocation.getArgument(0));

			// when
			AccountResponse response = accountService.create(USER_ID, request);

			// then
			assertThat(response.brokerName()).isEqualTo("삼성증권");
			assertThat(response.nickname()).isEqualTo("메인계좌");
		}
	}

	@Nested
	class 계좌_목록_조회 {

		@Test
		void 성공() {
			// given
			BrokerageAccount account = BrokerageAccount.builder()
				.userId(USER_ID)
				.brokerName("삼성증권")
				.nickname("메인계좌")
				.build();
			given(accountRepository.findAllByUserId(USER_ID)).willReturn(List.of(account));

			// when
			List<AccountResponse> responses = accountService.getAccounts(USER_ID);

			// then
			assertThat(responses).hasSize(1);
			assertThat(responses.get(0).nickname()).isEqualTo("메인계좌");
		}
	}

	@Nested
	class 계좌_단건_조회 {

		@Test
		void 본인_계좌가_아니면_예외() {
			// given
			Long accountId = 1L;
			given(accountRepository.findByIdAndUserId(accountId, USER_ID)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> accountService.getAccount(USER_ID, accountId))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
		}
	}

	@Nested
	class 계좌_수정 {

		@Test
		void 성공() {
			// given
			Long accountId = 1L;
			BrokerageAccount account = BrokerageAccount.builder()
				.userId(USER_ID)
				.brokerName("삼성증권")
				.nickname("메인계좌")
				.build();
			UpdateAccountRequest request = new UpdateAccountRequest("새이름");

			given(accountRepository.findByIdAndUserId(accountId, USER_ID)).willReturn(Optional.of(account));

			// when
			AccountResponse response = accountService.update(USER_ID, accountId, request);

			// then
			assertThat(response.nickname()).isEqualTo("새이름");
		}
	}

	@Nested
	class 계좌_삭제 {

		@Test
		void 성공() {
			// given
			Long accountId = 1L;
			BrokerageAccount account = BrokerageAccount.builder()
				.userId(USER_ID)
				.brokerName("삼성증권")
				.nickname("메인계좌")
				.build();

			given(accountRepository.findByIdAndUserId(accountId, USER_ID)).willReturn(Optional.of(account));

			// when
			accountService.delete(USER_ID, accountId);

			// then
			then(accountRepository).should().delete(account);
		}
	}
}
