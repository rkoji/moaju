package rkoji.moaju.stock.dto;

import rkoji.moaju.stock.entity.Currency;
import rkoji.moaju.stock.entity.Market;
import rkoji.moaju.stock.entity.Stock;

public record StockResponse(
	Long id,
	String ticker,
	String name,
	Market market,
	Currency currency
) {
	public static StockResponse from(Stock stock) {
		return new StockResponse(
			stock.getId(),
			stock.getTicker(),
			stock.getName(),
			stock.getMarket(),
			stock.getCurrency()
		);
	}
}
