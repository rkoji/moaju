package rkoji.moaju.trade.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import rkoji.moaju.global.response.ApiResponse;
import rkoji.moaju.trade.dto.CreateTradeRequest;
import rkoji.moaju.trade.dto.TradeResponse;
import rkoji.moaju.trade.service.TradeService;

@RestController
@RequestMapping("/api/accounts/{accountId}/trades")
@RequiredArgsConstructor
public class TradeController {

	private final TradeService tradeService;

	@PostMapping
	public ResponseEntity<ApiResponse<TradeResponse>> create(
		@AuthenticationPrincipal Long userId,
		@PathVariable Long accountId,
		@RequestBody @Valid CreateTradeRequest request
	) {
		return ResponseEntity.ok(ApiResponse.ok(tradeService.create(userId, accountId, request)));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<TradeResponse>>> getTrades(
		@AuthenticationPrincipal Long userId,
		@PathVariable Long accountId
	) {
		return ResponseEntity.ok(ApiResponse.ok(tradeService.getTrades(userId, accountId)));
	}

	@GetMapping("/stock/{stockId}")
	public ResponseEntity<ApiResponse<List<TradeResponse>>> getTradesByStock(
		@AuthenticationPrincipal Long userId,
		@PathVariable Long accountId,
		@PathVariable Long stockId
	) {
		return ResponseEntity.ok(ApiResponse.ok(tradeService.getTradeByStock(userId, accountId, stockId)));
	}

	@DeleteMapping("/{tradeId}")
	public ResponseEntity<ApiResponse<Void>> deleteTrade(
		@AuthenticationPrincipal Long userId,
		@PathVariable Long accountId,
		@PathVariable Long tradeId
	) {
		tradeService.deleteTrade(userId, accountId, tradeId);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/stock/{stockId}")
	public ResponseEntity<Void> deleteAllByStock(
		@AuthenticationPrincipal Long userId,
		@PathVariable Long accountId,
		@PathVariable Long stockId
	) {
		tradeService.deleteAllByStock(userId, accountId, stockId);
		return ResponseEntity.noContent().build();
	}
}
