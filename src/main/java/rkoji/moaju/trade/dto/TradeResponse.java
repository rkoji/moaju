package rkoji.moaju.trade.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import rkoji.moaju.stock.entity.Stock;
import rkoji.moaju.trade.entity.Trade;
import rkoji.moaju.trade.entity.TradeType;

public record TradeResponse(
	Long id,
	Long stockId,
	String stockName,
	String ticker,
	TradeType type,
	BigDecimal quantity,
	BigDecimal price,
	LocalDateTime tradedAt
) {
	public static TradeResponse of(Trade trade, Stock stock) {
		return new TradeResponse(
			trade.getId(),
			stock.getId(),
			stock.getName(),
			stock.getTicker(),
			trade.getType(),
			trade.getQuantity(),
			trade.getPrice(),
			trade.getTradedAt()
		);
	}

}
