package rkoji.moaju.marketsummary.dto;

import java.util.List;

public record NewsSummaryResult(
	String summary,
	List<TopNews> topNews
) {

	public record TopNews(int index, String summary) {}

}
