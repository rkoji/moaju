package rkoji.moaju.portfolio.dto;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioResponse(
	List<HoldingResponse> holdings,
	BigDecimal totalPurchaseAmount,      // 총 매수금액 (현재 보유 중인 종목 기준, 평균매수가 * 보유수량 합계)
	BigDecimal totalEvaluationAmount,    // 총 평가금액 (현재 보유 중인 종목 기준, 현재가 * 보유수량 합계)
	BigDecimal totalRealizedProfitLoss,  // 총 실현손익 (전량 매도로 보유 종목 목록에서 빠진 종목 포함 전체 합산)
	BigDecimal totalProfitLoss,          // 총 손익 = 총 실현손익 + 총 평가손익(현재 보유 종목의 미실현 손익)
	BigDecimal totalProfitLossRate       // 총 수익률 % = 총 손익 / 총 매수원금(과거 매도분 포함 전체) * 100
) {
}
