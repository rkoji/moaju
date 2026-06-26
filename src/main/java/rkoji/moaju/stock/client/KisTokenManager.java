package rkoji.moaju.stock.client;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import rkoji.moaju.global.config.KisProperties;
import rkoji.moaju.stock.client.dto.KisTokenResponse;

@Component
@RequiredArgsConstructor
public class KisTokenManager {

	private final WebClient kisWebClient;
	private final KisProperties kisProperties;

	private String accessToken;
	private LocalDateTime expiredAt;

	public String getAccessToken() {
		if (accessToken == null || LocalDateTime.now().isAfter(expiredAt.minusMinutes(10))) {
			issueToken();
		}
		return accessToken;
	}

	private synchronized void issueToken() {
		KisTokenResponse response = kisWebClient.post()
			.uri("/oauth2/tokenP")
			.bodyValue(Map.of(
				"grant_type", "client_credentials",
				"appkey", kisProperties.appKey(),
				"appsecret", kisProperties.appSecret()
			))
			.retrieve()
			.bodyToMono(KisTokenResponse.class)
			.block();

		this.accessToken = response.accessToken();
		this.expiredAt = LocalDateTime.now().plusSeconds(response.expiresIn());
	}
}
