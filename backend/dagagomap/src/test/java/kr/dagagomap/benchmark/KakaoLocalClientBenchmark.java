package kr.dagagomap.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;

import kr.dagagomap.infrastructure.api.kakao.local.KakaoLocalClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(properties = {
		"logging.level.kr.dagagomap.infrastructure.api.kakao.local=DEBUG"
})
@ActiveProfiles({ "secret" })
@Tag("manual-benchmark")
public class KakaoLocalClientBenchmark {

	private static final int QUOTA_REQUEST_COUNT = 33;
	private static final int PERFORMANCE_REQUEST_COUNT = 33;
	private static final List<String> ADDRESSES = List.of(
			"부산광역시 중구 중앙대로 1",
			"부산광역시 중구 중앙대로 2",
			"부산광역시 중구 중앙대로 3",
			"부산광역시 중구 중앙대로 4",
			"부산광역시 중구 중앙대로 5",
			"부산광역시 서구 구덕로 6",
			"부산광역시 서구 구덕로 7",
			"부산광역시 서구 구덕로 8",
			"부산광역시 서구 구덕로 9",
			"부산광역시 서구 구덕로 10",
			"부산광역시 동구 초량로 11",
			"부산광역시 동구 초량로 12",
			"부산광역시 동구 초량로 13",
			"부산광역시 동구 초량로 14",
			"부산광역시 동구 초량로 15",
			"부산광역시 영도구 태종로 16",
			"부산광역시 영도구 태종로 17",
			"부산광역시 영도구 태종로 18",
			"부산광역시 영도구 태종로 19",
			"부산광역시 영도구 태종로 20",
			"부산광역시 부산진구 중앙대로 21",
			"부산광역시 부산진구 중앙대로 22",
			"부산광역시 부산진구 중앙대로 23",
			"부산광역시 부산진구 중앙대로 24",
			"부산광역시 부산진구 중앙대로 25",
			"부산광역시 동래구 명륜로 26",
			"부산광역시 동래구 명륜로 27",
			"부산광역시 동래구 명륜로 28",
			"부산광역시 동래구 명륜로 29",
			"부산광역시 동래구 명륜로 30",
			"부산광역시 남구 수영로 31",
			"부산광역시 남구 수영로 32",
			"부산광역시 남구 수영로 33",
			"부산광역시 남구 수영로 34",
			"부산광역시 남구 수영로 35",
			"부산광역시 북구 금곡대로 36",
			"부산광역시 북구 금곡대로 37",
			"부산광역시 북구 금곡대로 38",
			"부산광역시 북구 금곡대로 39",
			"부산광역시 북구 금곡대로 40",
			"부산광역시 해운대구 해운대로 41",
			"부산광역시 해운대구 해운대로 42",
			"부산광역시 해운대구 해운대로 43",
			"부산광역시 해운대구 해운대로 44",
			"부산광역시 해운대구 해운대로 45",
			"부산광역시 사하구 낙동대로 46",
			"부산광역시 사하구 낙동대로 47",
			"부산광역시 사하구 낙동대로 48",
			"부산광역시 사하구 낙동대로 49",
			"부산광역시 사하구 낙동대로 50",
			"부산광역시 금정구 중앙대로 51",
			"부산광역시 금정구 중앙대로 52",
			"부산광역시 금정구 중앙대로 53",
			"부산광역시 금정구 중앙대로 54",
			"부산광역시 금정구 중앙대로 55",
			"부산광역시 강서구 공항로 56",
			"부산광역시 강서구 공항로 57",
			"부산광역시 강서구 공항로 58",
			"부산광역시 강서구 공항로 59",
			"부산광역시 강서구 공항로 60",
			"부산광역시 연제구 중앙대로 61",
			"부산광역시 연제구 중앙대로 62",
			"부산광역시 연제구 중앙대로 63",
			"부산광역시 연제구 중앙대로 64",
			"부산광역시 연제구 중앙대로 65",
			"부산광역시 수영구 광안해변로 66",
			"부산광역시 수영구 광안해변로 67",
			"부산광역시 수영구 광안해변로 68",
			"부산광역시 수영구 광안해변로 69",
			"부산광역시 수영구 광안해변로 70",
			"부산광역시 사상구 광장로 71",
			"부산광역시 사상구 광장로 72",
			"부산광역시 사상구 광장로 73",
			"부산광역시 사상구 광장로 74",
			"부산광역시 사상구 광장로 75",
			"부산광역시 기장군 기장대로 76",
			"부산광역시 기장군 기장대로 77",
			"부산광역시 기장군 기장대로 78",
			"부산광역시 기장군 기장대로 79",
			"부산광역시 기장군 기장대로 80",
			"부산광역시 중구 대청로 81",
			"부산광역시 중구 대청로 82",
			"부산광역시 동구 범일로 83",
			"부산광역시 동구 범일로 84",
			"부산광역시 부산진구 전포대로 85",
			"부산광역시 부산진구 전포대로 86",
			"부산광역시 남구 용소로 87",
			"부산광역시 남구 용소로 88",
			"부산광역시 해운대구 센텀중앙로 89",
			"부산광역시 해운대구 센텀중앙로 90",
			"부산광역시 수영구 수영로 91",
			"부산광역시 수영구 수영로 92",
			"부산광역시 연제구 월드컵대로 93",
			"부산광역시 연제구 월드컵대로 94",
			"부산광역시 금정구 금정로 95",
			"부산광역시 금정구 금정로 96",
			"부산광역시 사하구 다대로 97",
			"부산광역시 사하구 다대로 98",
			"부산광역시 강서구 명지국제로 99",
			"부산광역시 기장군 정관로 100");

	@Autowired
	private KakaoLocalClient kakaoLocalClient;

	@Test
	@DisplayName("주소를 좌표로 변환하는 API 쿼터 확인용")
	void convertAddressToCoordinatesQuotaBenchmark() {
		SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("virtual-thread-");
		executor.setVirtualThreads(true);
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		long beginTime = System.currentTimeMillis();
		for (int i = 0; i < QUOTA_REQUEST_COUNT; i++) {
			int idx = i;
			futures.add(CompletableFuture.runAsync(() -> {
				kakaoLocalClient.convertAddressToCoordinates(ADDRESSES.get(idx));
			}, executor));
		}
		CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).join();
		long endTime = System.currentTimeMillis();
		log.info("== Convert address to coordinates benchmark completed. Duration: {}ms ==", endTime - beginTime);
	}

	@Test
	@DisplayName("주소를 좌표로 변환하는 API 순차 처리 성능 확인")
	void convertAddressToCoordinatesSequentialBenchmark() {
		long beginTime = System.currentTimeMillis();
		for (int i = 0; i < PERFORMANCE_REQUEST_COUNT; i++) {
			kakaoLocalClient.convertAddressToCoordinates(ADDRESSES.get(i));
		}
		long endTime = System.currentTimeMillis();
		log.info("== Convert address to coordinates sequential benchmark completed. Duration: {}ms ==", endTime - beginTime);
	}

	@Test
	@DisplayName("주소를 좌표로 변환하는 API 스레드 풀 처리 성능 확인")
	void convertAddressToCoordinatesThreadPoolBenchmark() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setThreadNamePrefix("thread-pool-");
		executor.setCorePoolSize(PERFORMANCE_REQUEST_COUNT);
		executor.setMaxPoolSize(PERFORMANCE_REQUEST_COUNT);
		executor.setQueueCapacity(0);
		executor.initialize();
		long beginTime = System.currentTimeMillis();
		convertAddressToCoordinatesTest(executor);
		long endTime = System.currentTimeMillis();
		log.info("== Convert address to coordinates thread pool benchmark completed. Duration: {}ms ==", endTime - beginTime);
	}

	@Test
	@DisplayName("주소를 좌표로 변환하는 API 가상 스레드 처리 성능 확인")
	void convertAddressToCoordinatesVirtualThreadBenchmark() {
		SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("virtual-thread-");
		executor.setVirtualThreads(true);
		long beginTime = System.currentTimeMillis();
		convertAddressToCoordinatesTest(executor);
		long endTime = System.currentTimeMillis();
		log.info("== Convert address to coordinates virtual thread benchmark completed. Duration: {}ms ==", endTime - beginTime);
	}

	private void convertAddressToCoordinatesTest(org.springframework.core.task.AsyncTaskExecutor executor) {
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (int i = 0; i < PERFORMANCE_REQUEST_COUNT; i++) {
			int idx = i;
			futures.add(CompletableFuture.runAsync(() -> {
				kakaoLocalClient.convertAddressToCoordinates(ADDRESSES.get(idx));
			}, executor));
		}
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
	}

}
