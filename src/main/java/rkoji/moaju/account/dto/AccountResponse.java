package rkoji.moaju.account.dto;

import java.time.LocalDateTime;

import rkoji.moaju.account.entity.BrokerageAccount;

public record AccountResponse(
	Long id,
	String brokerName,
	String nickname,
	LocalDateTime createdAt
) {

	public static AccountResponse from(BrokerageAccount account) {
		return new AccountResponse(
			account.getId(),
			account.getBrokerName(),
			account.getNickname(),
			account.getCreatedAt()
		);
	}
}
