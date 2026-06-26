package rkoji.moaju.stock.controller;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import rkoji.moaju.global.response.ApiResponse;
import rkoji.moaju.stock.service.StockPriceService;
import rkoji.moaju.stock.service.StockService;
import rkoji.moaju.stock.service.StockSyncService;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

	private final StockSyncService stockSyncService;
	private final StockPriceService stockPriceService;

	@PostMapping("/sync")
	public ResponseEntity<ApiResponse<?>> sync() {
		stockSyncService.syncKospi();
		return ResponseEntity.ok(ApiResponse.ok());
	}

	@GetMapping("/{ticker}/price")
	public ResponseEntity<ApiResponse<BigDecimal>> getPrice(
		@PathVariable String ticker
	) {
		return ResponseEntity.ok(ApiResponse.ok(stockPriceService.getCurrentPrice(ticker)));
	}
}
