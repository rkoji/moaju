package rkoji.moaju.marketsummary.client;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import rkoji.moaju.global.config.AnthropicProperties;
import rkoji.moaju.marketsummary.dto.ClaudeMessageResponse;
import rkoji.moaju.marketsummary.dto.NewsItem;
import rkoji.moaju.marketsummary.dto.NewsSummaryResult;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class ClaudeApiClient {

	private static final String MODEL = "claude-haiku-4-5";

	private static final String SYSTEM_PROMPT = """
      너는 한국 증권 뉴스를 요약하는 어시스턴트다.
      주어진 뉴스 제목/요약 목록을 바탕으로 오늘 증권가 전반 요약(summary)과
      주요 뉴스 3~5개(topNews)를 뽑아라.
      topNews의 index는 뉴스 목록의 번호(1부터 시작)를 그대로 써라.
      """;

	private static final Map<String, Object> NEWS_SUMMARY_SCHEMA = Map.of(
		"type", "object",
		"properties", Map.of(
			"summary", Map.of("type", "string"),
			"topNews", Map.of(
				"type", "array",
				"items", Map.of(
					"type", "object",
					"properties", Map.of(
						"index", Map.of("type", "integer"),
						"summary", Map.of("type", "string")
					),
					"required", List.of("index", "summary"),
					"additionalProperties", false
				)
			)
		),
		"required", List.of("summary", "topNews"),
		"additionalProperties", false
	);

	private final WebClient anthropicWebClient;
	private final AnthropicProperties anthropicProperties;
	private final ObjectMapper objectMapper;

	public NewsSummaryResult summarizeNews(List<NewsItem> newsItems) {
		String newsListText = toPromptText(newsItems);

		Map<String, Object> requestBody = Map.of(
			"model", MODEL,
			"max_tokens", 2048,
			"system", SYSTEM_PROMPT,
			"messages", List.of(Map.of("role", "user", "content", newsListText)),
			"output_config", Map.of("format", Map.of(
				"type", "json_schema",
				"schema", NEWS_SUMMARY_SCHEMA
			))
		);

		ClaudeMessageResponse response = anthropicWebClient.post()
			.uri("/v1/messages")
			.header("x-api-key", anthropicProperties.apiKey())
			.header("anthropic-version", "2023-06-01")
			.bodyValue(requestBody)
			.retrieve()
			.bodyToMono(ClaudeMessageResponse.class)
			.block();

		String json = response.content().get(0).text();
		return objectMapper.readValue(json, NewsSummaryResult.class);
	}

	private String toPromptText(List<NewsItem> newsItems) {
		StringBuilder sb = new StringBuilder("다음은 오늘의 증권 뉴스 목록이다:\n\n");
		for (int i = 0; i < newsItems.size(); i++) {
			NewsItem item = newsItems.get(i);
			sb.append(i + 1).append(". [제목] ").append(item.title()).append("\n");
			sb.append("   [요약] ").append(item.description()).append("\n\n");
		}
		return sb.toString();
	}
}
