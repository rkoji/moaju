package rkoji.moaju.marketsummary.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import rkoji.moaju.marketsummary.entity.MarketSummary;
import rkoji.moaju.marketsummary.entity.MarketSummaryNews;

public record MarketSummaryResponse(
	LocalDate date,
	LocalDateTime createdAt,
	String summary,
	List<MarketSummaryNewsResponse> news
) {

	public static MarketSummaryResponse of(MarketSummary marketSummary, List<MarketSummaryNews> newsList) {
		return new MarketSummaryResponse(
			marketSummary.getDate(),
			marketSummary.getCreatedAt(),
			marketSummary.getSummary(),
			newsList.stream().map(MarketSummaryNewsResponse::from).toList()
		);
	}
}
