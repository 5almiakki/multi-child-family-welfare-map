package kr.dagagomap.infrastructure.api.kakao.local;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import kr.dagagomap.exception.KakaoApiException;
import kr.dagagomap.exception.KakaoApiQuotaExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import kr.dagagomap.infrastructure.api.kakao.local.dto.AddressToCoordinatesConversionResponse;
import kr.dagagomap.infrastructure.api.kakao.local.dto.PlaceSearchByKeywordResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClientException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class KakaoLocalClient {

	private final RestClient restClient;
	private final ObjectMapper objectMapper;
	private final Set<Thread> addressToCoordinatesConvertingThreads = ConcurrentHashMap.newKeySet();
	private volatile LocalDate addressToCoordinatesConversionQuotaExceededDate;

	public KakaoLocalClient(
			@Value("${custom.kakao.rest-api-key}")
			String restApiKey,
			RestClient.Builder restClientBuilder,
			ObjectMapper objectMapper) {
		this.restClient = restClientBuilder
				.baseUrl("https://dapi.kakao.com")
				.defaultHeader("Authorization", "KakaoAK " + restApiKey)
				.build();
		this.objectMapper = objectMapper;
	}

	@Retryable(excludes = KakaoApiQuotaExceededException.class)
	@ConcurrencyLimit(limitString = "${custom.kakao.local.concurrency-limit:1}")
	public AddressToCoordinatesConversionResponse convertAddressToCoordinates(String address) {
		String uri = "/v2/local/search/address.JSON?query=" + address;
		return executeTracked(() -> {
			var response = restClient.get()
					.uri(uri)
					.retrieve()
					.onStatus(HttpStatusCode::is4xxClientError, this::handle4xxClientError)
					.onStatus(HttpStatusCode::isError, this::logErrorAndThrow)
					.body(AddressToCoordinatesConversionResponse.class);
			logDebugIfNotNull(response);
			return response;
		});
	}

	@Retryable(excludes = KakaoApiQuotaExceededException.class)
	@ConcurrencyLimit(limitString = "${custom.kakao.local.concurrency-limit:1}")
	public PlaceSearchByKeywordResponse searchPlaceByKeyword(String keyword) {
		String uri = "/v2/local/search/keyword.JSON?query=" + keyword;
		// TODO executeTracked로 감싸기
		var response = restClient.get()
				.uri(uri)
				.retrieve()
				.onStatus(HttpStatusCode::isError, this::logErrorAndThrow)
				.body(PlaceSearchByKeywordResponse.class);
		logDebugIfNotNull(response);
		return response;
	}

	private <T> T executeTracked(Supplier<T> call) {
		checkQuotaNotExceeded();
		Thread currentThread = Thread.currentThread();
		addressToCoordinatesConvertingThreads.add(currentThread);
		try {
			return call.get();
		} catch (RestClientException e) {
			checkQuotaNotExceeded();
			throw e;
		} finally {
			addressToCoordinatesConvertingThreads.remove(currentThread);
			Thread.interrupted();
		}
	}

	private void checkQuotaNotExceeded() {
		if (LocalDate.now().equals(this.addressToCoordinatesConversionQuotaExceededDate)) {
			throw new KakaoApiQuotaExceededException("Abort request due to Kakao API quota limit.");
		}
	}

	private void logDebugIfNotNull(AddressToCoordinatesConversionResponse response) {
		if (!log.isDebugEnabled() || response == null) {
			return;
		}
		var documents = response.documents();
		if (documents == null) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("Response: ").append(response)
				.append(" / Documents.length: ").append(documents.length)
				.append(" / Document: ");
		for (var document : documents) {
			sb.append(document);
		}
		log.debug(sb.toString());
	}

	private void logDebugIfNotNull(PlaceSearchByKeywordResponse response) {
		if (!log.isDebugEnabled() || response == null) {
			return;
		}
		var documents = response.documents();
		if (documents == null) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("Response: ").append(response)
				.append(" / Documents.length: ").append(documents.length)
				.append(" / Document: ");
		for (var document : documents) {
			sb.append(document);
		}
		log.debug(sb.toString());
	}

	private void handle4xxClientError(HttpRequest req, ClientHttpResponse res) throws IOException {
		byte[] bodyBytes = res.getBody().readAllBytes();
		switch (res.getStatusCode().value()) {
			case 400:
				handle400Error(bodyBytes);
				break;
			default:
		}
		logErrorAndThrow(res, bodyBytes);
	}

	private void handle400Error(byte[] bodyBytes) {
		Map<String, Object> body = objectMapper.readValue(bodyBytes, new TypeReference<>() {});
		if (body.get("code") instanceof Integer code) {
			switch (code) {
				case -10: // quota exceeded
					triggerQuotaExceeded();
					throw new KakaoApiQuotaExceededException("Exceeded Kakao API quota.");
				default:
			}
		}
	}

	private void triggerQuotaExceeded() {
		addressToCoordinatesConversionQuotaExceededDate = LocalDate.now();
		log.error("Kakao API quota exceeded. Block subsequent requests." +
				" Attempt to cancel requests already in progress.");
		cancelOtherInFlightRequests();
	}

	private void cancelOtherInFlightRequests() {
		Thread currentThread = Thread.currentThread();
		for (Thread thread : addressToCoordinatesConvertingThreads) {
			if (thread != currentThread) {
				thread.interrupt();
			}
		}
	}

	private void logErrorAndThrow(HttpRequest req, ClientHttpResponse res) throws IOException {
		logErrorAndThrow(res, res.getBody().readAllBytes());
	}

	private void logErrorAndThrow(ClientHttpResponse res, byte[] bodyBytes) throws IOException {
		String responseBody = new String(bodyBytes, java.nio.charset.StandardCharsets.UTF_8);
		log.error("Kakao API Error: Status={}, Headers={}, Body={}",
				res.getStatusCode(), res.getHeaders(), responseBody);
		throw new KakaoApiException("Kakao API Error: " + responseBody);
	}

}
