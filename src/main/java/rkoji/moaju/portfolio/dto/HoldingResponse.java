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
	BigDecimal quantity,               // 보유 수량
	BigDecimal averagePrice,           // 평균 매수가
	BigDecimal currentPrice,           // 현재가
	BigDecimal totalBuyAmount,         // 총 매수원금 (평균매수가 * 총매수수량, 매도 이력과 무관하게 지금까지 실제로 투입한 원금)
	BigDecimal realizedProfitLoss,     // 실현손익 = 매도금액 - (평균매수가 * 매도수량). 현재가 조회와 무관하게 항상 계산됨
	BigDecimal evaluationProfitLoss,   // 평가손익(미실현) = (현재가 - 평균매수가) * 보유수량. 현재가 조회 실패 시 null
	BigDecimal profitLoss,             // 총손익 = 실현손익 + 평가손익. 현재가 조회 실패 시 null
	BigDecimal profitLossRate          // 총수익률 % = 총손익 / 총매수원금 * 100. 현재가 조회 실패 시 null
) {
}
