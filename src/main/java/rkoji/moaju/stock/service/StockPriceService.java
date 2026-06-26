package rkoji.moaju.stock.service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import rkoji.moaju.stock.client.KisStockClient;

@Service
@RequiredArgsConstructor
public class StockPriceService {

	private final KisStockClient kisStockClient;
	private final StringRedisTemplate redisTemplate;

	private static final long CACHE_TTL_MINUTES = 5;

	public BigDecimal getCurrentPrice(String ticker){
		String cacheKey = "stock:price:" + ticker;

		String cached = redisTemplate.opsForValue().get(cacheKey);
		if (cached != null) {
			return new BigDecimal(cached);
		}

		BigDecimal price = kisStockClient.getCurrentPrice(ticker);
		redisTemplate.opsForValue().set(cacheKey, price.toPlainString(), CACHE_TTL_MINUTES, TimeUnit.MINUTES);
		return price;
	}
}
