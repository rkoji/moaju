package rkoji.moaju.portfolio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import rkoji.moaju.global.response.ApiResponse;
import rkoji.moaju.portfolio.dto.PortfolioResponse;
import rkoji.moaju.portfolio.service.PortfolioService;

@RestController
@RequestMapping("/api/accounts/{accountId}/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

	private final PortfolioService portfolioService;

	@GetMapping
	public ResponseEntity<ApiResponse<PortfolioResponse>> getPortfolio(
		@AuthenticationPrincipal Long userId,
		@PathVariable("accountId") Long accountId
	) {
		return ResponseEntity.ok(ApiResponse.ok(portfolioService.getPortfolio(userId, accountId)));
	}

}
