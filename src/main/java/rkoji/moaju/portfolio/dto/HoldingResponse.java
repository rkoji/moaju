package rkoji.moaju.portfolio.dto;

import java.math.BigDecimal;

import rkoji.moaju.stock.entity.Currency;
import rkoji.moaju.stock.entity.Market;

public record HoldingResponse(
	Long stockId,
	String ticker,
	String name,
	Market market,
	Currency currency,
	BigDecimal quantity,        // 보유 수량
	BigDecimal averagePrice,    // 평균 매수가
	BigDecimal currentPrice,    // 현재가
	BigDecimal profitLoss,      // 수익금 = (현재가 - 평균매수가) * 보유수량
	BigDecimal profitLossRate   // 수익률 % = (현재가 - 평균매수가) / 평균매수가 * 100
) {
}
