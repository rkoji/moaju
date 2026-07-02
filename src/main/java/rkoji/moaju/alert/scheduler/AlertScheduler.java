package rkoji.moaju.alert.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import rkoji.moaju.alert.service.AlertService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertScheduler {

	private final AlertService alertService;

	@Scheduled(cron = "0 0/10 9-15 * * MON-FRI")
	public void run() {
		log.info("알람 스케줄러 실행");
		alertService.checkAndSendAlerts();
	}
}
