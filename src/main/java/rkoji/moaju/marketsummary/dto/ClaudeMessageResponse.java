package rkoji.moaju.marketsummary.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClaudeMessageResponse(
	List<ContentBlock> content
) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ContentBlock(String type, String text) {

	}
}
