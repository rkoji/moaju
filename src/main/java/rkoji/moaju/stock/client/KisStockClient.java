package rkoji.moaju.stock.client;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import rkoji.moaju.global.config.KisProperties;
import rkoji.moaju.stock.dto.KisStockPriceResponse;

@Component
@RequiredArgsConstructor
public class KisStockClient {

	private final WebClient kisWebClient;
	private final KisProperties kisProperties;
	private final KisTokenManager kisTokenManager;

	public BigDecimal getCurrentPrice(String ticker) {
		KisStockPriceResponse response = kisWebClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/uapi/domestic-stock/v1/quotations/inquire-price")
				.queryParam("FID_COND_MRKT_DIV_CODE", "J")
				.queryParam("FID_INPUT_ISCD", ticker)
				.build())
			.header("authorization", "Bearer " + kisTokenManager.getAccessToken())
			.header("appkey", kisProperties.appKey())
			.header("appsecret", kisProperties.appSecret())
			.header("tr_id", "FHKST01010100")
			.retrieve()
			.bodyToMono(KisStockPriceResponse.class)
			.block();

		return new BigDecimal(response.output().stckPrpr());
	}

}

