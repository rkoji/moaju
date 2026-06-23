package rkoji.moaju.global.jwt;

public record TokenResponse(
	String accessToken,
	String refreshToken
) {
}
