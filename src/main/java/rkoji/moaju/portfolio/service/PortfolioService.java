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

		List<HoldingResponse> holdings = tradesByStock.entrySet().stream()
			.map(entry -> toHolding(entry.getKey(), entry.getValue()))
			.filter(h -> h.quantity().compareTo(BigDecimal.ZERO) > 0)
			.toList();

		BigDecimal totalPurchase = holdings.stream()
			.map(h -> h.averagePrice().multiply(h.quantity()))
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal totalEvaluation = holdings.stream()
			.filter(h -> h.currentPrice() != null)
			.map(h -> h.currentPrice().multiply(h.quantity()))
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal totalProfitLoss = totalEvaluation.subtract(totalPurchase);

		BigDecimal totalProfitLossRate = totalPurchase.compareTo(BigDecimal.ZERO) == 0
			? BigDecimal.ZERO
			: totalProfitLoss.divide(totalPurchase, 6, RoundingMode.HALF_UP)
			.multiply(BigDecimal.valueOf(100));

		return new PortfolioResponse(holdings, totalPurchase, totalEvaluation, totalProfitLoss, totalProfitLossRate);

	}

	public BigDecimal calculateProfitRate(Long accountId) {
		List<Trade> trades = tradeRepository.findALlByAccountId(accountId);

		Map<Long, List<Trade>> tradesByStock = trades.stream()
			.collect(Collectors.groupingBy(Trade::getStockId));

		BigDecimal totalPurchase = BigDecimal.ZERO;
		BigDecimal totalEvaluation = BigDecimal.ZERO;

		for (Map.Entry<Long, List<Trade>> entry : tradesByStock.entrySet()) {
			HoldingResponse holding = toHolding(entry.getKey(), entry.getValue());
			if (holding.quantity().compareTo(BigDecimal.ZERO) <= 0) continue;
			if (holding.currentPrice() == null) continue; // 현재가 조회 실패 종목은 아예 제외

			totalPurchase = totalPurchase.add(holding.averagePrice().multiply(holding.quantity()));
			totalEvaluation = totalEvaluation.add(holding.currentPrice().multiply(holding.quantity()));
		}
		if (totalPurchase.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}

		return totalEvaluation.subtract(totalPurchase)
			.divide(totalPurchase, 6, RoundingMode.HALF_UP)
			.multiply(BigDecimal.valueOf(100));
	}

	private HoldingResponse toHolding(Long stockId, List<Trade> trades) {
		Stock stock = stockRepository.findById(stockId).orElseThrow(
			() -> new CustomException(STOCK_NOT_FOUND)
		);

		BigDecimal totalBuyQuantity = BigDecimal.ZERO;
		BigDecimal totalBuyAmount = BigDecimal.ZERO;
		BigDecimal totalSellQuantity = BigDecimal.ZERO;

		for (Trade trade : trades) {
			if (trade.getType() == TradeType.BUY) {
				totalBuyQuantity = totalBuyQuantity.add(trade.getQuantity());
				totalBuyAmount = totalBuyAmount.add(trade.getQuantity().multiply(trade.getPrice()));
			} else {
				totalSellQuantity = totalSellQuantity.add(trade.getQuantity());
			}
		}

		BigDecimal holdingQuantity = totalBuyQuantity.subtract(totalSellQuantity);
		BigDecimal averagePrice = totalBuyAmount.divide(totalBuyQuantity, 6, RoundingMode.HALF_UP);

		BigDecimal currentPrice = null;
		BigDecimal profitLoss = null;
		BigDecimal profitLossRate = null;

		try {
			currentPrice = stockPriceService.getCurrentPrice(stock.getTicker());
			profitLoss = currentPrice.subtract(averagePrice).multiply(holdingQuantity);
			profitLossRate = currentPrice.subtract(averagePrice)
				.divide(averagePrice, 6, RoundingMode.HALF_UP)
				.multiply(BigDecimal.valueOf(100));
		} catch (Exception e) {
			log.warn("현재가 조회 실패 - ticker: {}", stock.getTicker());
		}

		return new HoldingResponse(
			stockId, stock.getTicker(), stock.getName(),
			stock.getMarket(), stock.getCurrency(),
			holdingQuantity, averagePrice, currentPrice,
			profitLoss, profitLossRate
		);

	}
}
