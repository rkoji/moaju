package rkoji.moaju.stock.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import rkoji.moaju.stock.service.StockSyncService;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockSyncScheduler {

	private final StockSyncService stockSyncService;

	@Scheduled(cron = "0 0 8 * * MON-FRI", zone = "Asia/Seoul")
	public void syncKospi() {
		log.info("KOSPI 종목 동기화 시작");
		stockSyncService.syncKospi();
	}
}
