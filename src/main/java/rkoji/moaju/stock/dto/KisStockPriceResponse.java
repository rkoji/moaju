package rkoji.moaju.stock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KisStockPriceResponse(
	@JsonProperty("output")
	Output output
	) {

	public record Output(
		@JsonProperty("stck_prpr")
		String stckPrpr
	) {
	}
}
