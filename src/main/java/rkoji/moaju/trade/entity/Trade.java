package rkoji.moaju.trade.entity;

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
@Table(name = "trades")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trade {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long accountId;

	@Column(nullable = false)
	private Long stockId;

	@Column(nullable = false)
	private TradeType type;

	@Column(nullable = false, precision = 18, scale = 6)
	private BigDecimal quantity;

	@Column(nullable = false, precision = 18, scale = 6)
	private BigDecimal price;

	private LocalDateTime tradedAt;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Builder
	public Trade(Long accountId, Long stockId, TradeType type,
		BigDecimal quantity, BigDecimal price, LocalDateTime tradedAt) {
		this.accountId = accountId;
		this.stockId = stockId;
		this.type = type;
		this.quantity = quantity;
		this.price = price;
		this.tradedAt = tradedAt;
		this.createdAt = LocalDateTime.now();
	}
}
