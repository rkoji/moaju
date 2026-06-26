package rkoji.moaju.stock.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import rkoji.moaju.global.response.ApiResponse;
import rkoji.moaju.stock.service.StockService;
import rkoji.moaju.stock.service.StockSyncService;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

	private final StockSyncService stockSyncService;

	@PostMapping("/sync")
	public ResponseEntity<ApiResponse<?>> sync() {
		stockSyncService.syncKospi();
		return ResponseEntity.ok(ApiResponse.ok());
	}
}
