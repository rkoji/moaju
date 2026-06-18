package rkoji.moaju.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.assertj.core.api.Assertions;
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
import rkoji.moaju.user.dto.LoginRequest;
import rkoji.moaju.user.dto.SignUpRequest;
import rkoji.moaju.user.entity.User;
import rkoji.moaju.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;
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
			given(jwtProvider.generateToken(any())).willReturn("token");

			// when
			String token = userService.login(request);

			// then
			assertThat(token).isEqualTo("token");

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

}