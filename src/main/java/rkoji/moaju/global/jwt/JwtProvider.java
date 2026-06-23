package rkoji.moaju.global.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtProvider {

	private final SecretKey accessKey;
	private final SecretKey refreshKey;
	private final long accessExpirationMs;
	private final long refreshExpirationMs;


	public JwtProvider(
		@Value("${jwt.access-secret}") String accessSecret,
		@Value("${jwt.access-expiration-ms}") long accessExpirationMs,
		@Value("${jwt.refresh-secret}") String refreshSecret,
		@Value("${jwt.refresh-expiration-ms}") long refreshExpirationMs
	) {
		this.accessKey = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
		this.refreshKey = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
		this.accessExpirationMs = accessExpirationMs;
		this.refreshExpirationMs = refreshExpirationMs;
	}

	public String generateAccessToken(Long userId) {
		return generateToken(userId, accessKey, accessExpirationMs);
	}

	public String generateRefreshToken(Long userId) {
		return generateToken(userId, refreshKey, refreshExpirationMs);
	}

	public long getRefreshExpirationMs() {
		return refreshExpirationMs;
	}

	private String generateToken(Long userId, SecretKey key, long expirationMs) {
		return Jwts.builder()
			.subject(String.valueOf(userId))
			.issuedAt(new Date())
			.expiration(new Date(System.currentTimeMillis() + expirationMs))
			.signWith(key)
			.compact();
	}

	public boolean validateAccessToken(String token) {
		return validate(token, accessKey);
	}

	public boolean validateRefreshToken(String token) {
		return validate(token, refreshKey);
	}

	private boolean validate(String token, SecretKey key) {
		try {
			parseClaims(token, key);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	public Long getUserIdFromAccessToken(String token) {
		return Long.parseLong(parseClaims(token, accessKey).getSubject());
	}

	public Long getUserIdFromRefreshToken(String token) {
		return Long.parseLong(parseClaims(token, refreshKey).getSubject());
	}

	private Claims parseClaims(String token, SecretKey key) {
		return Jwts.parser()
			.verifyWith(key)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}
}
