package rkoji.moaju.marketsummary.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import rkoji.moaju.marketsummary.entity.MarketSummary;

public interface MarketSummaryRepository extends JpaRepository<MarketSummary, Long> {

	Optional<MarketSummary> findByDate(LocalDate date);
}
