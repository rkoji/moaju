package rkoji.moaju.marketsummary.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import rkoji.moaju.marketsummary.entity.MarketSummaryNews;

public interface MarketSummaryNewsRepository extends JpaRepository<MarketSummaryNews, Long> {

	List<MarketSummaryNews> findAllByMarketSummaryIdOrderByDisplayOrder(Long marketSummaryId);
}
