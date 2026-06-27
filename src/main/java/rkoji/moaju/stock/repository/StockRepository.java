package rkoji.moaju.stock.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import rkoji.moaju.stock.entity.Market;
import rkoji.moaju.stock.entity.Stock;

public interface StockRepository extends JpaRepository<Stock, Long> {

	Optional<Stock> findByTicker(String ticker);

	List<Stock> findAllByMarket(Market market);

	@Query("SELECT s FROM Stock s WHERE s.isActive = true AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :q, '%')) "
		+ "OR s.ticker LIKE CONCAT('%', :q, '%')) ORDER BY s.name")
	List<Stock> searchActive(@Param("q") String query, Pageable pageable);
}
