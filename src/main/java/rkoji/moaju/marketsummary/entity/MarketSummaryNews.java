package rkoji.moaju.marketsummary.entity;

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
@Table(name = "market_summary_news")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketSummaryNews {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long marketSummaryId;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String summary;

	@Column(nullable = false)
	private String link;

	@Column(nullable = false)
	private int displayOrder;

	@Builder
	public MarketSummaryNews(Long marketSummaryId, String title, String summary, String link, int displayOrder) {
		this.marketSummaryId = marketSummaryId;
		this.title = title;
		this.summary = summary;
		this.link = link;
		this.displayOrder = displayOrder;
	}
}
