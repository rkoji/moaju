package rkoji.moaju.stock.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import rkoji.moaju.stock.client.StockMstClient;
import rkoji.moaju.stock.client.dto.KospiStockItem;
import rkoji.moaju.stock.entity.Currency;
import rkoji.moaju.stock.entity.Market;
import rkoji.moaju.stock.entity.Stock;
import rkoji.moaju.stock.repository.StockRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockSyncService {

	private final StockMstClient stockMstClient;
	private final StockRepository stockRepository;

	@Transactional
	public void syncKospi() {
		List<KospiStockItem> items = stockMstClient.fetchKospiStocks();
		if (items.isEmpty()) {
			log.warn("KOSPI 종목 리스트가 비어있습니다. 동기화 중단.");
			return;
		}

		Map<String, Stock> existingMap = stockRepository.findAllByMarket(Market.KOSPI)
			.stream()
			.collect(Collectors.toMap(Stock::getTicker, s -> s));

		Set<String> fetchedTickers = new HashSet<>();
		List<Stock> toInsert = new ArrayList<>();

		for (KospiStockItem item : items) {
			fetchedTickers.add(item.ticker());
			Stock existing = existingMap.get(item.ticker());

			if (existing == null) {
				toInsert.add(Stock.builder()
					.ticker(item.ticker())
					.name(item.name())
					.market(Market.KOSPI)
					.currency(Currency.KRW)
					.build());
			}else{
				if (!existing.getName().equals(item.name())) {
					existing.updateName(item.name());
				}
				existing.activate();
			}
		}

		// 리스트에 없는 종목 비활성화 (상장폐지)
		existingMap.entrySet().stream()
			.filter(e-> !fetchedTickers.contains(e.getKey()))
			.forEach(e->e.getValue().deactivate());

		stockRepository.saveAll(toInsert);
		log.info("KOSPI 동기화 완료 - 신규: {}개, 전체: {}개", toInsert.size(), items.size());
	}
}
