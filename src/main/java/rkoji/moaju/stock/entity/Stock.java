package rkoji.moaju.stock.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String ticker;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Market market;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Currency currency;

	@Column(nullable = false)
	private boolean isActive = true;

	@Builder
	public Stock(String ticker, String name, Market market, Currency currency) {
		this.ticker = ticker;
		this.name = name;
		this.market = market;
		this.currency = currency;
		this.isActive = true;
	}

	public void activate() {
		this.isActive = true;
	}

	public void deactivate() {
		this.isActive = false;
	}

	public void updateName(String name) {
		this.name = name;
	}
}
