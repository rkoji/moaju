package rkoji.moaju.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import rkoji.moaju.global.exception.CustomException;
import rkoji.moaju.global.exception.ErrorCode;
import rkoji.moaju.global.jwt.JwtProvider;
import rkoji.moaju.user.dto.SignUpRequest;
import rkoji.moaju.user.entity.User;
import rkoji.moaju.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;

	@Transactional
	public void signUp(SignUpRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
		}

		User user = User.builder()
			.email(request.email())
			.password(passwordEncoder.encode(request.password()))
			.nickname(request.nickname())
			.build();

		userRepository.save(user);
	}
}
