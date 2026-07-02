package rkoji.moaju.alert.service;

import static rkoji.moaju.global.exception.ErrorCode.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import rkoji.moaju.account.entity.BrokerageAccount;
import rkoji.moaju.account.repository.BrokerageAccountRepository;
import rkoji.moaju.global.exception.CustomException;
import rkoji.moaju.global.mail.EmailService;
import rkoji.moaju.portfolio.service.PortfolioService;
import rkoji.moaju.user.entity.User;
import rkoji.moaju.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

	private final BrokerageAccountRepository accountRepository;
	private final PortfolioService portfolioService;
	private final UserRepository userRepository;
	private final EmailService emailService;

	@Transactional
	public void checkAndSendAlerts() {
		List<BrokerageAccount> targets = accountRepository.findByAlertEnabledTrueAndTargetProfitRateIsNotNullAndAlertThresholdIsNotNull();

		for (BrokerageAccount account : targets) {
			try {
				BigDecimal currentRate = portfolioService.calculateProfitRate(account.getId());
				BigDecimal gap = account.getTargetProfitRate().subtract(currentRate);

				boolean inRange = gap.compareTo(account.getAlertThreshold()) <= 0;

				if (inRange && !account.isAlertTriggered()) {
					User user = userRepository.findById(account.getUserId()).orElseThrow(
						() -> new CustomException(USER_NOT_FOUND)
					);
					boolean achieved = currentRate.compareTo(account.getTargetProfitRate()) >= 0;
					emailService.sendAlertMail(
						user.getEmail(),
						account.getNickname(),
						currentRate,
						account.getTargetProfitRate(),
						achieved
					);
					account.onAlertTriggered();
				} else if (!inRange && account.isAlertTriggered()) {
					if (account.getLastAlertedAt().isBefore(LocalDateTime.now().minusHours(1))) {
						account.resetAlertTriggered();
					}
				}
			} catch (Exception e) {
				log.warn("알람 처리 실패 - accountId: {}", account.getId(), e);
			}
		}
	}

	private boolean isRecentlyAlerted(BrokerageAccount account) {
		if (account.getLastAlertedAt() == null) {
			return false;
		}
		return account.getLastAlertedAt().isAfter(LocalDateTime.now().minusHours(24));
	}
}
