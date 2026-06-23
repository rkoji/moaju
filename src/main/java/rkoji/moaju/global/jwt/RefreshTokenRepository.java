package rkoji.moaju.global.jwt;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

	private static final String KEY_PREFIX = "refresh:";

	private final StringRedisTemplate redisTemplate;

	public void save(Long userId, String refreshToken, long expirationMs) {
		redisTemplate.opsForValue()
			.set(key(userId), refreshToken, Duration.ofMillis(expirationMs));
	}

	public Optional<String> findByUserId(Long userId) {
		return Optional.ofNullable(redisTemplate.opsForValue().get(key(userId)));
	}

	public void deleteByUserId(Long userId) {
		redisTemplate.delete(key(userId));
	}

	private String key(Long userId) {
		return KEY_PREFIX + userId;
	}
}
