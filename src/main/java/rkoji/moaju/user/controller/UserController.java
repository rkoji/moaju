package rkoji.moaju.user.controller;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import rkoji.moaju.global.jwt.JwtProvider;
import rkoji.moaju.global.jwt.TokenResponse;
import rkoji.moaju.global.response.ApiResponse;
import rkoji.moaju.user.dto.LoginRequest;
import rkoji.moaju.user.dto.SignUpRequest;
import rkoji.moaju.user.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final JwtProvider jwtProvider;

	@Value("${cookie.secure}")
	private boolean cookieSecure;

	@PostMapping("/signup")
	public ResponseEntity<ApiResponse<Void>> signUp(@RequestBody @Valid SignUpRequest request) {
		userService.signUp(request);
		return ResponseEntity.ok(ApiResponse.ok());
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<String>> login(@RequestBody @Valid LoginRequest request) {
		TokenResponse tokens = userService.login(request);

		ResponseCookie cookie = buildRefreshTokenCookie(tokens.refreshToken());

		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, cookie.toString())
			.body(ApiResponse.ok(tokens.accessToken()));

	}

	@PostMapping("/reissue")
	public ResponseEntity<ApiResponse<String>> reissue(
		@CookieValue(name = "refreshToken",required = false) String refreshToken
	){
		TokenResponse tokens = userService.reissue(refreshToken);

		ResponseCookie cookie = buildRefreshTokenCookie(tokens.refreshToken());

		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE,cookie.toString())
			.body(ApiResponse.ok(tokens.accessToken()));
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal Long userId) {
		userService.logout(userId);

		ResponseCookie expiredCookie = ResponseCookie.from("refreshToken", "")
			.httpOnly(true)
			.secure(cookieSecure)
			.sameSite("Strict")
			.path("/api/users")
			.maxAge(0)
			.build();

		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE,expiredCookie.toString())
			.body(ApiResponse.ok());
	}


	private ResponseCookie buildRefreshTokenCookie(String refreshToken) {
		return ResponseCookie.from("refreshToken", refreshToken)
			.httpOnly(true)
			.secure(cookieSecure)
			.sameSite("Strict")
			.path("/api/users")
			.maxAge(Duration.ofMillis(jwtProvider.getRefreshExpirationMs()))
			.build();
	}
}
