package rkoji.moaju.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

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

}