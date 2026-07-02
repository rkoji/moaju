package rkoji.moaju.marketsummary.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "market_summaries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketSummary {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private LocalDate date;

	@Lob
	@Column(nullable = false)
	private String summary;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Builder
	public MarketSummary(LocalDate date, String summary) {
		this.date = date;
		this.summary = summary;
		this.createdAt = LocalDateTime.now();
	}

}
