package rkoji.moaju.stock.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KisTokenResponse(

	@JsonProperty("access_token")
	String accessToken,

	@JsonProperty("expires_in")
	long expiresIn
) {
}
