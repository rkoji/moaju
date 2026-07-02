package rkoji.moaju.alert.dto;

import java.math.BigDecimal;

public record UpdateAlertRequest(
	BigDecimal targetProfitRate,
	BigDecimal alertThreshold,
	boolean alertEnabled
) {
}
