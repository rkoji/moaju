package rkoji.moaju.portfolio.service;

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
import rkoji.moaju.portfolio.dto.PortfolioResponse;
import rkoji.moaju.stock.entity.Currency;
import rkoji.moaju.stock.entity.Market;
import rkoji.moaju.stock.entity.Stock;
import rkoji.moaju.stock.repository.StockRepository;
import rkoji.moaju.stock.service.StockPriceService;
import rkoji.moaju.trade.entity.Trade;
import rkoji.moaju.trade.entity.TradeType;
import rkoji.moaju.trade.repository.TradeRepository;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

	@Mock private BrokerageAccountRepository accountRepository;
	@Mock private TradeRepository tradeRepository;
	@Mock private StockRepository stockRepository;
	@Mock private StockPriceService stockPriceService;

	@InjectMocks
	private PortfolioService portfolioService;

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

	private Trade buyTrade(BigDecimal quantity, BigDecimal price) {
		return Trade.builder()
			.accountId(ACCOUNT_ID).stockId(STOCK_ID)
			.type(TradeType.BUY).quantity(quantity).price(price)
			.build();
	}

	private Trade sellTrade(BigDecimal quantity, BigDecimal price) {
		return Trade.builder()
			.accountId(ACCOUNT_ID).stockId(STOCK_ID)
			.type(TradeType.SELL).quantity(quantity).price(price)
			.build();
	}

	@Nested
	class 포트폴리오_조회 {

		@Test
		void 계좌_없으면_예외() {
			// given
			given(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> portfolioService.getPortfolio(USER_ID, ACCOUNT_ID))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
		}

		@Test
		void 거래_없으면_빈_포트폴리오() {
			// given
			given(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).willReturn(Optional.of(account()));
			given(tradeRepository.findALlByAccountId(ACCOUNT_ID)).willReturn(List.of());

			// when
			PortfolioResponse response = portfolioService.getPortfolio(USER_ID, ACCOUNT_ID);

			// then
			assertThat(response.holdings()).isEmpty();
			assertThat(response.totalPurchaseAmount()).isEqualByComparingTo(BigDecimal.ZERO);
			assertThat(response.totalEvaluationAmount()).isEqualByComparingTo(BigDecimal.ZERO);
		}

		@Test
		void 매수_거래의_보유수량과_평균매수가_계산() {
			// given
			// 10주 @ 60,000 + 10주 @ 80,000 → 평균 70,000
			List<Trade> trades = List.of(
				buyTrade(new BigDecimal("10"), new BigDecimal("60000")),
				buyTrade(new BigDecimal("10"), new BigDecimal("80000"))
			);
			given(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).willReturn(Optional.of(account()));
			given(tradeRepository.findALlByAccountId(ACCOUNT_ID)).willReturn(trades);
			given(stockRepository.findById(STOCK_ID)).willReturn(Optional.of(stock()));
			given(stockPriceService.getCurrentPrice("005930")).willReturn(new BigDecimal("75000"));

			// when
			PortfolioResponse response = portfolioService.getPortfolio(USER_ID, ACCOUNT_ID);

			// then
			assertThat(response.holdings()).hasSize(1);
			assertThat(response.holdings().get(0).quantity()).isEqualByComparingTo(new BigDecimal("20"));
			assertThat(response.holdings().get(0).averagePrice()).isEqualByComparingTo(new BigDecimal("70000"));
		}

		@Test
		void 매수_후_일부_매도시_보유수량_차감() {
			// given
			// 매수 10주, 매도 3주 → 보유 7주
			List<Trade> trades = List.of(
				buyTrade(new BigDecimal("10"), new BigDecimal("70000")),
				sellTrade(new BigDecimal("3"), new BigDecimal("75000"))
			);
			given(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).willReturn(Optional.of(account()));
			given(tradeRepository.findALlByAccountId(ACCOUNT_ID)).willReturn(trades);
			given(stockRepository.findById(STOCK_ID)).willReturn(Optional.of(stock()));
			given(stockPriceService.getCurrentPrice("005930")).willReturn(new BigDecimal("75000"));

			// when
			PortfolioResponse response = portfolioService.getPortfolio(USER_ID, ACCOUNT_ID);

			// then
			assertThat(response.holdings().get(0).quantity()).isEqualByComparingTo(new BigDecimal("7"));
		}

		@Test
		void 전량_매도시_holdings_에서_제외() {
			// given
			List<Trade> trades = List.of(
				buyTrade(new BigDecimal("10"), new BigDecimal("70000")),
				sellTrade(new BigDecimal("10"), new BigDecimal("75000"))
			);
			given(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).willReturn(Optional.of(account()));
			given(tradeRepository.findALlByAccountId(ACCOUNT_ID)).willReturn(trades);
			given(stockRepository.findById(STOCK_ID)).willReturn(Optional.of(stock()));

			// when
			PortfolioResponse response = portfolioService.getPortfolio(USER_ID, ACCOUNT_ID);

			// then
			assertThat(response.holdings()).isEmpty();
		}

		@Test
		void 현재가_조회_실패시_수익률은_null() {
			// given
			List<Trade> trades = List.of(
				buyTrade(new BigDecimal("10"), new BigDecimal("70000"))
			);
			given(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).willReturn(Optional.of(account()));
			given(tradeRepository.findALlByAccountId(ACCOUNT_ID)).willReturn(trades);
			given(stockRepository.findById(STOCK_ID)).willReturn(Optional.of(stock()));
			given(stockPriceService.getCurrentPrice("005930")).willThrow(new RuntimeException("KIS API 오류"));

			// when
			PortfolioResponse response = portfolioService.getPortfolio(USER_ID, ACCOUNT_ID);

			// then
			assertThat(response.holdings()).hasSize(1);
			assertThat(response.holdings().get(0).currentPrice()).isNull();
			assertThat(response.holdings().get(0).profitLossRate()).isNull();
		}
	}
}
