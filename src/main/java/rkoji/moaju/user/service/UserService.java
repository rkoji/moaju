package rkoji.moaju.user.service;

import static rkoji.moaju.global.exception.ErrorCode.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import rkoji.moaju.global.exception.CustomException;
import rkoji.moaju.global.jwt.JwtProvider;
import rkoji.moaju.global.jwt.RefreshTokenRepository;
import rkoji.moaju.global.jwt.TokenResponse;
import rkoji.moaju.user.dto.LoginRequest;
import rkoji.moaju.user.dto.SignUpRequest;
import rkoji.moaju.user.entity.User;
import rkoji.moaju.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;

	@Transactional
	public void signUp(SignUpRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new CustomException(EMAIL_ALREADY_EXISTS);
		}

		User user = User.builder()
			.email(request.email())
			.password(passwordEncoder.encode(request.password()))
			.nickname(request.nickname())
			.build();

		userRepository.save(user);
	}

	@Transactional
	public TokenResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email()).orElseThrow(
			() -> new CustomException(USER_NOT_FOUND)
		);

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new CustomException(INVALID_PASSWORD);
		}

		String accessToken = jwtProvider.generateAccessToken(user.getId());
		String refreshToken = jwtProvider.generateRefreshToken(user.getId());

		refreshTokenRepository.save(user.getId(), refreshToken, jwtProvider.getRefreshExpirationMs());

		return new TokenResponse(accessToken, refreshToken);
	}

	@Transactional
	public TokenResponse reissue(String refreshToken) {
		if (refreshToken == null || !jwtProvider.validateRefreshToken(refreshToken)) {
			throw new CustomException(INVALID_REFRESH_TOKEN);
		}

		Long userId = jwtProvider.getUserIdFromRefreshToken(refreshToken);

		String savedRefreshToken = refreshTokenRepository.findByUserId(userId)
			.orElseThrow(() -> new CustomException(INVALID_REFRESH_TOKEN));

		if (!savedRefreshToken.equals(refreshToken)) {
			throw new CustomException(INVALID_REFRESH_TOKEN);
		}

		String newAccessToken = jwtProvider.generateAccessToken(userId);
		String newRefreshToken = jwtProvider.generateRefreshToken(userId);

		refreshTokenRepository.save(userId, newRefreshToken, jwtProvider.getRefreshExpirationMs());

		return new TokenResponse(newAccessToken, newRefreshToken);
	}

	public void logout(Long userId) {
		refreshTokenRepository.deleteByUserId(userId);
	}
}
