package rkoji.moaju.trade.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import rkoji.moaju.trade.entity.TradeType;

public record CreateTradeRequest(
	@NotNull(message = "종목을 선택해 주세요")
	Long stockId,

	@NotNull(message = "거래 유형을 선택해 주세요")
	TradeType type,

	@NotNull(message = "수량을 입력해 주세요")
	@DecimalMin(value = "0.000001", message = "수량은 0보다 커야 합니다")
	BigDecimal quantity,

	@NotNull(message = "가격을 입력해 주세요")
	@DecimalMin(value = "0", message = "가격은 0 이상이어야 합니다")
	BigDecimal price,

	LocalDateTime tradedAt
) {
}
