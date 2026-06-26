package rkoji.moaju.stock.client;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import rkoji.moaju.stock.client.dto.KospiStockItem;

@Slf4j
@Component
public class StockMstClient {

	private static final String MST_URL =
		"https://new.real.download.dws.co.kr/common/master/kospi_code.mst.zip";

	private final WebClient webClient;

	public StockMstClient() {
		this.webClient = WebClient.builder()
			.exchangeStrategies(ExchangeStrategies.builder()
				.codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
				.build())
			.build();
	}

	public List<KospiStockItem> fetchKospiStocks(){
		log.info("KOSPI MST 파일 다운로드 시작");
		byte[] zipBytes = webClient.get()
			.uri(MST_URL)
			.retrieve()
			.bodyToMono(byte[].class)
			.block();
		return parseMst(zipBytes);
	}

	private List<KospiStockItem> parseMst(byte[] zipBytes) {
		List<KospiStockItem> result = new ArrayList<>();

		try(ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
			zis.getNextEntry();
			byte[] mstBytes = zis.readAllBytes();

			BufferedReader reader = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(mstBytes), Charset.forName("CP949")));

			String line;
			while ((line = reader.readLine()) != null) {
				if (line.length() < 230) continue;

				String ticker = line.substring(0,9).strip();
				String name = line.substring(21, line.length() - 227).strip();

				if (ticker.isBlank() || name.isBlank()) continue;
				result.add(new KospiStockItem(ticker, name));
			}
		} catch (IOException e) {
			throw new RuntimeException("KOSPI MST 파일 파싱 실패",e);
		}
		log.info("KOSPI 종목 {}개 파싱 완료", result.size());
		return result;
	}
}
