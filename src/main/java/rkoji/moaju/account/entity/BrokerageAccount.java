package rkoji.moaju.account.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "brokerage_accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BrokerageAccount {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false)
	private String brokerName;

	@Column(nullable = false)
	private String nickname;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column
	private BigDecimal targetProfitRate;  //  목표 수익률 (예 : 10%)

	@Column
	private BigDecimal alertThreshold;  // 알림 발동 여유폭 (예 : 2%)

	@Column(nullable = false, columnDefinition = "boolean default false")
	private boolean alertEnabled;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private boolean alertTriggered = false;

	@Column
	private LocalDateTime lastAlertedAt;

	@Builder
	public BrokerageAccount(Long userId, String brokerName, String nickname) {
		this.userId = userId;
		this.brokerName = brokerName;
		this.nickname = nickname;
		this.createdAt = LocalDateTime.now();
	}

	public void updateNickname(String nickname) {
		this.nickname = nickname;
	}

	public void updateAlertSettings(BigDecimal targetProfitRate, BigDecimal alertThreshold
	, boolean alertEnabled) {
		this.targetProfitRate = targetProfitRate;
		this.alertThreshold = alertThreshold;
		this.alertEnabled = alertEnabled;
		this.alertTriggered = false;
	}

	public void recordAlertSent(){
		this.lastAlertedAt = LocalDateTime.now();
	}

	public void onAlertTriggered(){
		this.alertTriggered = true;
		this.lastAlertedAt = LocalDateTime.now();
	}

	public void resetAlertTriggered(){
		this.alertTriggered = false;
	}
}
