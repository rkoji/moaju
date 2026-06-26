package rkoji.moaju.stock.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import rkoji.moaju.stock.repository.StockRepository;

@Service
@RequiredArgsConstructor
public class StockService {

	private final StockRepository stockRepository;

}
