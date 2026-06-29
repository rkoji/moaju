package rkoji.moaju.trade.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import rkoji.moaju.account.entity.BrokerageAccount;
import rkoji.moaju.account.repository.BrokerageAccountRepository;
import rkoji.moaju.global.exception.CustomException;
import rkoji.moaju.global.exception.ErrorCode;
import rkoji.moaju.stock.entity.Currency;
import rkoji.moaju.stock.entity.Market;
import rkoji.moaju.stock.entity.Stock;
import rkoji.moaju.stock.repository.StockRepository;
import rkoji.moaju.trade.dto.CreateTradeRequest;
import rkoji.moaju.trade.dto.TradeResponse;
import rkoji.moaju.trade.entity.Trade;
import rkoji.moaju.trade.entity.TradeType;
import rkoji.moaju.trade.repository.TradeRepository;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

	@Mock private TradeRepository tradeRepository;
	@Mock private StockRepository stockRepository;
	@Mock private BrokerageAccountRepository accountRepository;

	@InjectMocks
	private TradeService tradeService;

	private static final Long USER_ID = 1L;
	private static final Long ACCOUNT_ID = 10L;
	private static final Long STOCK_ID = 100L;

	private BrokerageAccount account() {
		return BrokerageAccount.builder()
			.userId(USER_ID)
			.brokerName("삼성증권")
			.nickname("메인계좌")
			.build();
	}

	private Stock stock() {
		return Stock.builder()
			.ticker("005930")
			.name("삼성전자")
			.market(Market.KOSPI)
			.currency(Currency.KRW)
			.build();
	}

	@Nested
	class 거래_등록 {

		@Test
		void 성공() {
			// given
			CreateTradeRequest request = new CreateTradeRequest(
				STOCK_ID, TradeType.BUY, new BigDecimal("10"), new BigDecimal("70000"), null
			);
			given(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).willReturn(Optional.of(account()));
			given(stockRepository.findById(STOCK_ID)).willReturn(Optional.of(stock()));
			given(tradeRepository.save(any(Trade.class))).willAnswer(inv -> inv.getArgument(0));

			// when
			TradeResponse response = tradeService.create(USER_ID, ACCOUNT_ID, request);

			// then
			assertThat(response.type()).isEqualTo(TradeType.BUY);
			assertThat(response.quantity()).isEqualByComparingTo(new BigDecimal("10"));
			assertThat(response.price()).isEqualByComparingTo(new BigDecimal("70000"));
		}

		@Test
		void 계좌_없으면_예외() {
			// given
			CreateTradeRequest request = new CreateTradeRequest(
				STOCK_ID, TradeType.BUY, new BigDecimal("10"), new BigDecimal("70000"), null
			);
			given(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> tradeService.create(USER_ID, ACCOUNT_ID, request))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
		}

		@Test
		void 종목_없으면_예외() {
			// given
			CreateTradeRequest request = new CreateTradeRequest(
				STOCK_ID, TradeType.BUY, new BigDecimal("10"), new BigDecimal("70000"), null
			);
			given(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).willReturn(Optional.of(account()));
			given(stockRepository.findById(STOCK_ID)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> tradeService.create(USER_ID, ACCOUNT_ID, request))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.STOCK_NOT_FOUND);
		}
	}

	@Nested
	class 거래_목록_조회 {

		@Test
		void 성공() {
			// given
			Trade trade = Trade.builder()
				.accountId(ACCOUNT_ID).stockId(STOCK_ID)
				.type(TradeType.BUY).quantity(new BigDecimal("5")).price(new BigDecimal("70000"))
				.build();
			given(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).willReturn(Optional.of(account()));
			given(tradeRepository.findALlByAccountId(ACCOUNT_ID)).willReturn(List.of(trade));
			given(stockRepository.findById(STOCK_ID)).willReturn(Optional.of(stock()));

			// when
			List<TradeResponse> responses = tradeService.getTrades(USER_ID, ACCOUNT_ID);

			// then
			assertThat(responses).hasSize(1);
			assertThat(responses.get(0).ticker()).isEqualTo("005930");
		}

		@Test
		void 계좌_없으면_예외() {
			// given
			given(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> tradeService.getTrades(USER_ID, ACCOUNT_ID))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
		}
	}

	@Nested
	class 거래_삭제 {

		@Test
		void 성공() {
			// given
			Long tradeId = 1L;
			Trade trade = Trade.builder()
				.accountId(ACCOUNT_ID).stockId(STOCK_ID)
				.type(TradeType.BUY).quantity(new BigDecimal("10")).price(new BigDecimal("70000"))
				.build();
			given(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).willReturn(Optional.of(account()));
			given(tradeRepository.findById(tradeId)).willReturn(Optional.of(trade));

			// when
			tradeService.deleteTrade(USER_ID, ACCOUNT_ID, tradeId);

			// then
			then(tradeRepository).should().delete(trade);
		}

		@Test
		void 계좌_없으면_예외() {
			// given
			given(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> tradeService.deleteTrade(USER_ID, ACCOUNT_ID, 1L))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
		}

		@Test
		void 거래_없으면_예외() {
			// given
			Long tradeId = 999L;
			given(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).willReturn(Optional.of(account()));
			given(tradeRepository.findById(tradeId)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> tradeService.deleteTrade(USER_ID, ACCOUNT_ID, tradeId))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.TRADE_NOT_FOUND);
		}
	}
}
