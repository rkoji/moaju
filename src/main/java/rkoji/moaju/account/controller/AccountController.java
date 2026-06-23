package rkoji.moaju.account.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import rkoji.moaju.account.dto.AccountResponse;
import rkoji.moaju.account.dto.CreateAccountRequest;
import rkoji.moaju.account.dto.UpdateAccountRequest;
import rkoji.moaju.account.service.AccountService;
import rkoji.moaju.global.response.ApiResponse;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

	private final AccountService accountService;

	@PostMapping
	public ResponseEntity<ApiResponse<AccountResponse>> create(
		@AuthenticationPrincipal Long userId,
		@RequestBody @Valid CreateAccountRequest request
	) {
		return ResponseEntity.ok(ApiResponse.ok(accountService.create(userId, request)));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccounts(
		@AuthenticationPrincipal Long userId
	) {
		return ResponseEntity.ok(ApiResponse.ok(accountService.getAccounts(userId)));
	}

	@GetMapping("/{accountId}")
	public ResponseEntity<ApiResponse<AccountResponse>> getAccount(
		@AuthenticationPrincipal Long userId,
		@PathVariable Long accountId
	) {
		return ResponseEntity.ok(ApiResponse.ok(accountService.getAccount(userId, accountId)));
	}

	@PatchMapping("/{accountId}")
	public ResponseEntity<ApiResponse<AccountResponse>> update(
		@AuthenticationPrincipal Long userId,
		@PathVariable Long accountId,
		@RequestBody @Valid UpdateAccountRequest request
	) {
		return ResponseEntity.ok(ApiResponse.ok(accountService.update(userId, accountId, request)));
	}

	@DeleteMapping("/{accountId}")
	public ResponseEntity<ApiResponse<Void>> delete(
		@AuthenticationPrincipal Long userId,
		@PathVariable Long accountId
	) {
		accountService.delete(userId, accountId);
		return ResponseEntity.ok(ApiResponse.ok());
	}
}
