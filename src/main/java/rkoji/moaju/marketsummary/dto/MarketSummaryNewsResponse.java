package rkoji.moaju.marketsummary.dto;

import rkoji.moaju.marketsummary.entity.MarketSummaryNews;

public record MarketSummaryNewsResponse(
	String title,
	String summary,
	String link
) {

	public static MarketSummaryNewsResponse from(MarketSummaryNews news) {
		return new MarketSummaryNewsResponse(news.getTitle(), news.getSummary(), news.getLink());
	}
}
