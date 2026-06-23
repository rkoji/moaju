package rkoji.moaju.account.entity;

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
}
