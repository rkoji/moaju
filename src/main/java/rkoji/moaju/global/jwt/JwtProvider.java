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

	private final SecretKey secretKey;
	private final long expirationMs;

	public JwtProvider(
		@Value("${jwt.secret}") String secret,
		@Value("${jwt.expiration-ms}") long expirationMs
	){
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationMs = expirationMs;
	}

	public String generateToken(Long userId) {
		return Jwts.builder()
			.subject(String.valueOf(userId))
			.issuedAt(new Date())
			.expiration(new Date(System.currentTimeMillis() + expirationMs))
			.signWith(secretKey)
			.compact();
	}

	public Claims parseClaims(String token) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	public boolean validateToken(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	public Long getUserId(String token) {
		return Long.parseLong(parseClaims(token).getSubject());
	}
}
