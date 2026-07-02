package rkoji.moaju.alert.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import rkoji.moaju.account.service.AccountService;
import rkoji.moaju.alert.dto.UpdateAlertRequest;
import rkoji.moaju.global.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class AlertController {

	private final AccountService accountService;

	@PatchMapping("/{accountId}/alert")
	public ResponseEntity<ApiResponse<?>> updateAlertSettings(
		@PathVariable Long accountId,
		@RequestBody UpdateAlertRequest request,
		@AuthenticationPrincipal Long userId
	) {
		accountService.updateAlertSettings(userId, accountId, request);
		return ResponseEntity.ok(ApiResponse.ok());
	}
}
