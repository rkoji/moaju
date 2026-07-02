package rkoji.moaju.stock.service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import rkoji.moaju.stock.client.KisStockClient;

@Slf4j
@Service
public class StockPriceService {

	private final KisStockClient kisStockClient;
	private final StringRedisTemplate redisTemplate;
	private final RedissonClient redissonClient;
	private final Counter cacheHitCounter;
	private final Counter cacheMissCounter;

	private static final long CACHE_TTL_MINUTES = 5;
	private static final String LOCK_PREFIX = "lock:stock:price:";

	public StockPriceService(KisStockClient kisStockClient, StringRedisTemplate redisTemplate,
		RedissonClient redissonClient, MeterRegistry meterRegistry) {
		this.kisStockClient = kisStockClient;
		this.redisTemplate = redisTemplate;
		this.redissonClient = redissonClient;
		this.cacheHitCounter = meterRegistry.counter("stock.cache.hit");
		this.cacheMissCounter = meterRegistry.counter("stock.cache.miss");
	}

	public BigDecimal getCurrentPrice(String ticker) {
		String cacheKey = "stock:price:" + ticker;

		String cached = redisTemplate.opsForValue().get(cacheKey);
		if (cached != null) {
			log.info("[Cache HIT] ticker={}", ticker);
			cacheHitCounter.increment();
			return new BigDecimal(cached);
		}

		RLock lock = redissonClient.getLock(LOCK_PREFIX + ticker);
		try {
			boolean acquired = lock.tryLock(3, TimeUnit.SECONDS);
			if (!acquired) {
				log.warn("[Lock 획득 실패] ticker={}", ticker);
				String fallback = redisTemplate.opsForValue().get(cacheKey);
				if (fallback != null) {
					return new BigDecimal(fallback);
				}
				BigDecimal price = kisStockClient.getCurrentPrice(ticker);
				redisTemplate.opsForValue().set(cacheKey, price.toPlainString(), CACHE_TTL_MINUTES, TimeUnit.MINUTES);
				return price;
			}

			cached = redisTemplate.opsForValue().get(cacheKey);
			if (cached != null) {
				log.info("[Cache HIT after lock] ticker={}", ticker);
				cacheHitCounter.increment();
				return new BigDecimal(cached);
			}

			log.info("[Cache MISS] ticker={} → KIS API 호출", ticker);
			cacheMissCounter.increment();
			BigDecimal price = kisStockClient.getCurrentPrice(ticker);
			redisTemplate.opsForValue().set(cacheKey, price.toPlainString(), CACHE_TTL_MINUTES, TimeUnit.MINUTES);
			return price;

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("현재가 조회 중 인터럽트 발생", e);
		} finally {
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}
}
