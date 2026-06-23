package rkoji.moaju.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import rkoji.moaju.global.exception.CustomException;
import rkoji.moaju.global.exception.ErrorCode;
import rkoji.moaju.global.jwt.JwtProvider;
import rkoji.moaju.global.jwt.RefreshTokenRepository;
import rkoji.moaju.global.jwt.TokenResponse;
import rkoji.moaju.user.dto.LoginRequest;
import rkoji.moaju.user.dto.SignUpRequest;
import rkoji.moaju.user.entity.User;
import rkoji.moaju.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private RefreshTokenRepository refreshTokenRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private JwtProvider jwtProvider;

	@InjectMocks
	private UserService userService;

	@Nested
	class 회원가입 {

		@Test
		void 성공() {
			// given
			SignUpRequest request = new SignUpRequest("test123@test.com", "password123", "테스터");

			given(userRepository.existsByEmail(request.email())).willReturn(false);
			given(passwordEncoder.encode(request.password())).willReturn("encodedPassword");

			// when
			userService.signUp(request);

			// then
			then(userRepository).should().save(any(User.class));
		}


		@Test
		void 이메일_중복_예외(){
			// given
			SignUpRequest request = new SignUpRequest("test123@test.com", "password123", "테스트");

			given(userRepository.existsByEmail(request.email())).willReturn(true);

			// when & then
			assertThatThrownBy(() -> userService.signUp(request))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);
		}
	}

	@Nested
	class 로그인{

		@Test
		void 성공(){
			// given
			LoginRequest request = new LoginRequest("test123@test.com", "password123");
			User user = User.builder()
				.email(request.email())
				.password("encodedPassword")
				.nickname("테스터")
				.build();

			given(userRepository.findByEmail(request.email())).willReturn(Optional.of(user));
			given(passwordEncoder.matches(request.password(),
				user.getPassword())).willReturn(true);
			given(jwtProvider.generateAccessToken(any())).willReturn("accessToken");
			given(jwtProvider.generateRefreshToken(any())).willReturn("refreshToken");
			given(jwtProvider.getRefreshExpirationMs()).willReturn(1209600000L);

			// when
			TokenResponse tokens = userService.login(request);

			// then
			assertThat(tokens.accessToken()).isEqualTo("accessToken");
			assertThat(tokens.refreshToken()).isEqualTo("refreshToken");
			then(refreshTokenRepository).should().save(any(), eq("refreshToken"), eq(1209600000L));
		}

		@Test
		void 존재하지않는_이메일_예외(){
			// given
			LoginRequest request = new LoginRequest("test@test.com", "password123");

			given(userRepository.findByEmail(request.email())).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> userService.login(request))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.USER_NOT_FOUND);
		}

		@Test
		void 비밀번호_불일치_예외(){
			// given
			LoginRequest request = new LoginRequest("test@test.com", "wrongPassword");
			User user = User.builder()
				.email(request.email())
				.password("encodedPassword")
				.nickname("테스터")
				.build();

			given(userRepository.findByEmail(request.email())).willReturn(Optional.of(user));
			given(passwordEncoder.matches(request.password(),
				user.getPassword())).willReturn(false);

			// when & then
			assertThatThrownBy(() -> userService.login(request))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.INVALID_PASSWORD);
		}

	}

	@Nested
	class 토큰_재발급 {

		@Test
		void 성공() {
			// given
			Long userId = 1L;
			String refreshToken = "validRefreshToken";

			given(jwtProvider.validateRefreshToken(refreshToken)).willReturn(true);
			given(jwtProvider.getUserIdFromRefreshToken(refreshToken)).willReturn(userId);
			given(refreshTokenRepository.findByUserId(userId)).willReturn(Optional.of(refreshToken));
			given(jwtProvider.generateAccessToken(userId)).willReturn("newAccessToken");
			given(jwtProvider.generateRefreshToken(userId)).willReturn("newRefreshToken");
			given(jwtProvider.getRefreshExpirationMs()).willReturn(1209600000L);

			// when
			TokenResponse tokens = userService.reissue(refreshToken);

			// then
			assertThat(tokens.accessToken()).isEqualTo("newAccessToken");
			assertThat(tokens.refreshToken()).isEqualTo("newRefreshToken");
			then(refreshTokenRepository).should().save(userId, "newRefreshToken", 1209600000L);
		}

		@Test
		void 토큰이_없으면_예외() {
			// when & then
			assertThatThrownBy(() -> userService.reissue(null))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
		}

		@Test
		void 유효하지_않은_토큰_예외() {
			// given
			String refreshToken = "invalidRefreshToken";
			given(jwtProvider.validateRefreshToken(refreshToken)).willReturn(false);

			// when & then
			assertThatThrownBy(() -> userService.reissue(refreshToken))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
		}

		@Test
		void 저장된_토큰이_없으면_예외() {
			// given
			Long userId = 1L;
			String refreshToken = "validRefreshToken";

			given(jwtProvider.validateRefreshToken(refreshToken)).willReturn(true);
			given(jwtProvider.getUserIdFromRefreshToken(refreshToken)).willReturn(userId);
			given(refreshTokenRepository.findByUserId(userId)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() -> userService.reissue(refreshToken))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
		}

		@Test
		void 저장된_토큰과_불일치하면_예외() {
			// given
			Long userId = 1L;
			String refreshToken = "validRefreshToken";

			given(jwtProvider.validateRefreshToken(refreshToken)).willReturn(true);
			given(jwtProvider.getUserIdFromRefreshToken(refreshToken)).willReturn(userId);
			given(refreshTokenRepository.findByUserId(userId)).willReturn(Optional.of("differentRefreshToken"));

			// when & then
			assertThatThrownBy(() -> userService.reissue(refreshToken))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
		}
	}

	@Nested
	class 로그아웃 {

		@Test
		void 성공() {
			// given
			Long userId = 1L;

			// when
			userService.logout(userId);

			// then
			then(refreshTokenRepository).should().deleteByUserId(userId);
		}
	}

}
