package rkoji.moaju.trade.service;

import static rkoji.moaju.global.exception.ErrorCode.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import rkoji.moaju.account.repository.BrokerageAccountRepository;
import rkoji.moaju.global.exception.CustomException;
import rkoji.moaju.global.exception.ErrorCode;
import rkoji.moaju.stock.entity.Stock;
import rkoji.moaju.stock.repository.StockRepository;
import rkoji.moaju.trade.dto.CreateTradeRequest;
import rkoji.moaju.trade.dto.TradeResponse;
import rkoji.moaju.trade.entity.Trade;
import rkoji.moaju.trade.repository.TradeRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeService {

	private final TradeRepository tradeRepository;
	private final StockRepository stockRepository;
	private final BrokerageAccountRepository accountRepository;

	@Transactional
	public TradeResponse create(Long userId, Long accountId, CreateTradeRequest request) {
		accountRepository.findByIdAndUserId(accountId, userId).orElseThrow(
			() -> new CustomException(ACCOUNT_NOT_FOUND)
		);

		Stock stock = stockRepository.findById(request.stockId()).orElseThrow(
			() -> new CustomException(STOCK_NOT_FOUND)
		);

		Trade trade = Trade.builder()
			.accountId(accountId)
			.stockId(request.stockId())
			.type(request.type())
			.quantity(request.quantity())
			.price(request.price())
			.tradedAt(request.tradedAt() != null ? request.tradedAt() : LocalDateTime.now())
			.build();

		return TradeResponse.of(tradeRepository.save(trade), stock);
	}

	public List<TradeResponse> getTrades(Long userId, Long accountId) {
		accountRepository.findByIdAndUserId(accountId, userId).orElseThrow(
			() -> new CustomException(ACCOUNT_NOT_FOUND)
		);

		return tradeRepository.findALlByAccountId(accountId).stream()
			.map(trade -> {
				Stock stock = stockRepository.findById(trade.getStockId()).orElseThrow(
					() -> new CustomException(STOCK_NOT_FOUND)
				);
				return TradeResponse.of(trade, stock);
			})
			.toList();
	}

	public List<TradeResponse> getTradeByStock(Long userId, Long accountId, Long stockId) {
		accountRepository.findByIdAndUserId(accountId, userId).orElseThrow(
			() -> new CustomException(ACCOUNT_NOT_FOUND)
		);

		return tradeRepository.findAllByAccountIdAndStockId(accountId, stockId).stream()
			.map(trade -> {
				Stock stock = stockRepository.findById(trade.getStockId()).orElseThrow(
					() -> new CustomException(STOCK_NOT_FOUND)
				);
				return TradeResponse.of(trade, stock);
			})
			.toList();
	}

	@Transactional
	public void deleteTrade(Long userId, Long accountId, Long tradeId) {
		accountRepository.findByIdAndUserId(accountId, userId).orElseThrow(
			() -> new CustomException(ACCOUNT_NOT_FOUND)
		);

		Trade trade = tradeRepository.findById(tradeId).orElseThrow(
			() -> new CustomException(TRADE_NOT_FOUND)
		);

		tradeRepository.delete(trade);
	}

	@Transactional
	public void deleteAllByStock(Long userId, Long accountId, Long stockId) {
		accountRepository.findByIdAndUserId(accountId, userId).orElseThrow(
			() -> new CustomException(ACCOUNT_NOT_FOUND)
		);

		List<Trade> trades = tradeRepository.findAllByAccountIdAndStockId(accountId, stockId);
		tradeRepository.deleteAll(trades);
	}
}
