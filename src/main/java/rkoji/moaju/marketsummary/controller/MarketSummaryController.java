package rkoji.moaju.marketsummary.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import rkoji.moaju.global.response.ApiResponse;
import rkoji.moaju.marketsummary.service.MarketSummaryService;

@RestController
@RequestMapping("/api/market-summary")
@RequiredArgsConstructor
public class MarketSummaryController {

	private final MarketSummaryService marketSummaryService;

	@PostMapping("/generate")
	public ResponseEntity<ApiResponse<?>> generate() {
		marketSummaryService.generateTodaySummary();
		return ResponseEntity.ok(ApiResponse.ok());
	}

	@GetMapping("/latest")
	public ResponseEntity<ApiResponse<?>> getLatest() {
		return ResponseEntity.ok(ApiResponse.ok(marketSummaryService.getLatestSummary()));
	}
}
