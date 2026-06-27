package rkoji.moaju.portfolio.dto;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioResponse(
	List<HoldingResponse> holdings,
	BigDecimal totalPurchaseAmount,  // 총 매수금액 (평균매수가 * 보유수량 합계)
	BigDecimal totalEvaluationAmount, // 총 평가금액 (현재가 * 보유수량 합계)
	BigDecimal totalProfitLoss,       // 총 수익금
	BigDecimal totalProfitLossRate    // 총 수익률 %
) {
}
