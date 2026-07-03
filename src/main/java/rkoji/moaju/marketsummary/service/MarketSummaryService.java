package rkoji.moaju.marketsummary.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import rkoji.moaju.marketsummary.client.ClaudeApiClient;
import rkoji.moaju.marketsummary.client.SecuritiesNewsClient;
import rkoji.moaju.marketsummary.dto.NewsItem;
import rkoji.moaju.marketsummary.dto.NewsSummaryResult;
import rkoji.moaju.marketsummary.entity.MarketSummary;
import rkoji.moaju.marketsummary.entity.MarketSummaryNews;
import rkoji.moaju.marketsummary.repository.MarketSummaryNewsRepository;
import rkoji.moaju.marketsummary.repository.MarketSummaryRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketSummaryService {

	private final SecuritiesNewsClient securitiesNewsClient;
	private final ClaudeApiClient claudeApiClient;
	private final MarketSummaryRepository marketSummaryRepository;
	private final MarketSummaryNewsRepository marketSummaryNewsRepository;

	@Transactional
	public void generateTodaySummary(){
		LocalDate today = LocalDate.now();
		if (marketSummaryRepository.findByDate(today).isPresent()) {
			log.info("오늘({}) 시장 요약이 이미 존재해 스킵", today);
			return;
		}

		List<NewsItem> newsItems = securitiesNewsClient.fetchLatestNews();
		if (newsItems.isEmpty()){
			log.warn("수집된 뉴스가 없어 요약을 생성하지 않음");
			return;
		}

		NewsSummaryResult result = claudeApiClient.summarizeNews(newsItems);

		MarketSummary marketSummary = marketSummaryRepository.save(
			MarketSummary.builder()
				.date(today)
				.summary(result.summary())
				.build()
		);

		List<MarketSummaryNews> newsEntities = new ArrayList<>();
		int displayOrder = 0;
		for (NewsSummaryResult.TopNews topNews : result.topNews()) {
			int listIndex = topNews.index() - 1;
			if (listIndex < 0 || listIndex >= newsItems.size()) {
				log.warn("Claude가 유효 범위를 벗어난 index를 반환함 : {}", topNews.index());
				continue;
			}

			NewsItem original = newsItems.get(listIndex);
			newsEntities.add(MarketSummaryNews.builder()
				.marketSummaryId(marketSummary.getId())
				.title(original.title())
				.summary(topNews.summary())
				.link(original.link())
				.displayOrder(displayOrder++)
				.build());
		}
		marketSummaryNewsRepository.saveAll(newsEntities);
	}

}
