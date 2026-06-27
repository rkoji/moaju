package rkoji.moaju.stock.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import rkoji.moaju.stock.dto.StockResponse;
import rkoji.moaju.stock.repository.StockRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockService {

	private final StockRepository stockRepository;

	public List<StockResponse> search(String query) {
		return stockRepository.searchActive(query, PageRequest.of(0, 20))
			.stream()
			.map(StockResponse::from)
			.toList();
	}
}
