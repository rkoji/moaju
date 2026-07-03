package rkoji.moaju.marketsummary.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import rkoji.moaju.marketsummary.service.MarketSummaryService;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketSummaryScheduler {

	private final MarketSummaryService marketSummaryService;

	@Scheduled(cron = "0 50 7 * * *", zone = "Asia/Seoul")
	public void run() {
		log.info("시장 요약 스케줄러 실행");
		marketSummaryService.generateTodaySummary();
	}
}
