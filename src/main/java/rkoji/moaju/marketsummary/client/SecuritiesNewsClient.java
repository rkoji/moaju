package rkoji.moaju.marketsummary.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.util.List;

import org.springframework.stereotype.Component;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import lombok.extern.slf4j.Slf4j;
import rkoji.moaju.marketsummary.dto.NewsItem;

@Slf4j
@Component
public class SecuritiesNewsClient {

	private static final List<String> RSS_URLS = List.of(
		"https://www.hankyung.com/feed/finance",
		"https://www.mk.co.kr/rss/50200011/",
		"https://www.yna.co.kr/rss/economy.xml"
	);

	private static final int MAX_ITEMS_PER_FEED = 5;

	public List<NewsItem> fetchLatestNews() {
		return RSS_URLS.stream()
			.flatMap(url -> fetchFeed(url).stream())
			.toList();
	}

	private List<NewsItem> fetchFeed(String url) {
		try {
			SyndFeedInput input = new SyndFeedInput();
			URLConnection connection = URI.create(url).toURL().openConnection();
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);

			try (InputStream inputStream = connection.getInputStream()) {
				SyndFeed feed = input.build(new XmlReader(inputStream));
				return feed.getEntries().stream()
					.limit(MAX_ITEMS_PER_FEED)
					.map(this::toNewsItem)
					.toList();
			}
		} catch (IOException | FeedException e) {
			log.warn("RSS 조회 실패 - url : {}", url, e);
			return List.of();
		}
	}

	private NewsItem toNewsItem(SyndEntry entry) {
		String description = entry.getDescription() != null
			? entry.getDescription().getValue()
			: "";
		return new NewsItem(entry.getTitle(), description, entry.getLink());
	}

}
