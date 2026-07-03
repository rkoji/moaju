package rkoji.moaju.portfolio.service;

import static rkoji.moaju.global.exception.ErrorCode.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import rkoji.moaju.account.repository.BrokerageAccountRepository;
import rkoji.moaju.global.exception.CustomException;
import rkoji.moaju.portfolio.dto.HoldingResponse;
import rkoji.moaju.portfolio.dto.PortfolioResponse;
import rkoji.moaju.stock.entity.Stock;
import rkoji.moaju.stock.repository.StockRepository;
import rkoji.moaju.stock.service.StockPriceService;
import rkoji.moaju.trade.entity.Trade;
import rkoji.moaju.trade.entity.TradeType;
import rkoji.moaju.trade.repository.TradeRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioService {

	private final BrokerageAccountRepository accountRepository;
	private final TradeRepository tradeRepository;
	private final StockRepository stockRepository;
	private final StockPriceService stockPriceService;

	public PortfolioResponse getPortfolio(Long userId, Long accountId) {
		accountRepository.findByIdAndUserId(accountId, userId).orElseThrow(
			() -> new CustomException(ACCOUNT_NOT_FOUND)
		);

		List<Trade> trades = tradeRepository.findALlByAccountId(accountId);

		Map<Long, List<Trade>> tradesByStock = trades.stream()
			.collect(Collectors.groupingBy(Trade::getStockId));

		// 전량 매도한 종목도 실현손익 집계엔 포함해야 해서, 필터링 전 전체 목록을 따로 갖고 있는다.
		List<HoldingResponse> allHoldings = tradesByStock.entrySet().stream()
			.map(entry -> toHolding(entry.getKey(), entry.getValue()))
			.toList();

		List<HoldingResponse> holdings = allHoldings.stream()
			.filter(h -> h.quantity().compareTo(BigDecimal.ZERO) > 0)
			.toList();

		BigDecimal totalPurchase = holdings.stream()
			.map(h -> h.averagePrice().multiply(h.quantity()))
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal totalEvaluation = holdings.stream()
			.filter(h -> h.currentPrice() != null)
			.map(h -> h.currentPrice().multiply(h.quantity()))
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal totalRealizedProfitLoss = allHoldings.stream()
			.map(HoldingResponse::realizedProfitLoss)
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal totalBuyAmountEver = allHoldings.stream()
			.map(HoldingResponse::totalBuyAmount)
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal totalEvaluationProfitLoss = totalEvaluation.subtract(totalPurchase);
		BigDecimal totalProfitLoss = totalRealizedProfitLoss.add(totalEvaluationProfitLoss);

		BigDecimal totalProfitLossRate = totalBuyAmountEver.compareTo(BigDecimal.ZERO) == 0
			? BigDecimal.ZERO
			: totalProfitLoss.divide(totalBuyAmountEver, 6, RoundingMode.HALF_UP)
			.multiply(BigDecimal.valueOf(100));

		return new PortfolioResponse(
			holdings, totalPurchase, totalEvaluation,
			totalRealizedProfitLoss, totalProfitLoss, totalProfitLossRate
		);
	}

	public BigDecimal calculateProfitRate(Long accountId) {
		List<Trade> trades = tradeRepository.findALlByAccountId(accountId);

		Map<Long, List<Trade>> tradesByStock = trades.stream()
			.collect(Collectors.groupingBy(Trade::getStockId));

		BigDecimal totalBuyAmount = BigDecimal.ZERO;
		BigDecimal totalProfitLoss = BigDecimal.ZERO;

		for (Map.Entry<Long, List<Trade>> entry : tradesByStock.entrySet()) {
			HoldingResponse holding = toHolding(entry.getKey(), entry.getValue());

			// 전량 매도한 종목은 현재가와 무관하게 이미 확정된 실현손익만 더하면 된다.
			BigDecimal profitLoss = holding.quantity().compareTo(BigDecimal.ZERO) > 0
				? holding.profitLoss()
				: holding.realizedProfitLoss();

			if (profitLoss == null) continue; // 보유 중인데 현재가 조회에 실패한 경우

			totalBuyAmount = totalBuyAmount.add(holding.totalBuyAmount());
			totalProfitLoss = totalProfitLoss.add(profitLoss);
		}
		if (totalBuyAmount.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}

		return totalProfitLoss.divide(totalBuyAmount, 6, RoundingMode.HALF_UP)
			.multiply(BigDecimal.valueOf(100));
	}

	private HoldingResponse toHolding(Long stockId, List<Trade> trades) {
		Stock stock = stockRepository.findById(stockId).orElseThrow(
			() -> new CustomException(STOCK_NOT_FOUND)
		);

		BigDecimal totalBuyQuantity = BigDecimal.ZERO;
		BigDecimal totalBuyAmount = BigDecimal.ZERO;
		BigDecimal totalSellQuantity = BigDecimal.ZERO;
		BigDecimal totalSellAmount = BigDecimal.ZERO;

		for (Trade trade : trades) {
			if (trade.getType() == TradeType.BUY) {
				totalBuyQuantity = totalBuyQuantity.add(trade.getQuantity());
				totalBuyAmount = totalBuyAmount.add(trade.getQuantity().multiply(trade.getPrice()));
			} else {
				totalSellQuantity = totalSellQuantity.add(trade.getQuantity());
				totalSellAmount = totalSellAmount.add(trade.getQuantity().multiply(trade.getPrice()));
			}
		}

		BigDecimal holdingQuantity = totalBuyQuantity.subtract(totalSellQuantity);
		BigDecimal averagePrice = totalBuyAmount.divide(totalBuyQuantity, 6, RoundingMode.HALF_UP);

		// 실현손익: 매도로 확정된 손익. 현재가 조회와 무관하게 항상 계산 가능하다.
		BigDecimal realizedProfitLoss = totalSellAmount.subtract(averagePrice.multiply(totalSellQuantity));

		BigDecimal currentPrice = null;
		BigDecimal evaluationProfitLoss = null;
		BigDecimal profitLoss = null;
		BigDecimal profitLossRate = null;

		try {
			currentPrice = stockPriceService.getCurrentPrice(stock.getTicker());
			evaluationProfitLoss = currentPrice.subtract(averagePrice).multiply(holdingQuantity);
			profitLoss = realizedProfitLoss.add(evaluationProfitLoss);
			profitLossRate = profitLoss.divide(totalBuyAmount, 6, RoundingMode.HALF_UP)
				.multiply(BigDecimal.valueOf(100));
		} catch (Exception e) {
			log.warn("현재가 조회 실패 - ticker: {}", stock.getTicker());
		}

		return new HoldingResponse(
			stockId, stock.getTicker(), stock.getName(),
			stock.getMarket(), stock.getCurrency(),
			holdingQuantity, averagePrice, currentPrice, totalBuyAmount,
			realizedProfitLoss, evaluationProfitLoss, profitLoss, profitLossRate
		);

	}
}
