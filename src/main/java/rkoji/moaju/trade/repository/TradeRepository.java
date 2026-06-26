package rkoji.moaju.trade.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import rkoji.moaju.trade.entity.Trade;

public interface TradeRepository extends JpaRepository<Trade, Long> {

	List<Trade> findALlByAccountId(Long accountId);

	List<Trade> findAllByAccountIdAndStockId(Long accountId, Long stockId);
}
