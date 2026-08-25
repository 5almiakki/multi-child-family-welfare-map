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

	private final QuotaGuard addressToCoordinatesConversionQuotaGuard = new QuotaGuard();
	private final QuotaGuard placeSearchByKeywordQuotaGuard = new QuotaGuard();

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

	@Retryable(
			excludes = KakaoApiQuotaExceededException.class,
			maxRetries = 1L,
			delay = 2L,
			jitter = 1000L)
	@ConcurrencyLimit(limitString = "${custom.kakao.local.concurrency-limit:1}")
	public AddressToCoordinatesConversionResponse convertAddressToCoordinates(String address) {
		String uri = "/v2/local/search/address.JSON?query=" + address;
		return addressToCoordinatesConversionQuotaGuard.executeTracked(() -> {
			var response = restClient.get()
					.uri(uri)
					.retrieve()
					.onStatus(HttpStatusCode::is4xxClientError,
							(req, res) -> handle4xxClientError(res, addressToCoordinatesConversionQuotaGuard))
					.onStatus(HttpStatusCode::isError, (req, res) -> {
						throw new KakaoApiException("Kakao API Error: "
								+ "Status=" + res.getStatusCode()
								+ ", Headers=" + res.getHeaders()
								+ ", Body=" + new String(res.getBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
					})
					.body(AddressToCoordinatesConversionResponse.class);
			logDebugIfNotNull(response);
			return response;
		});
	}


	@Retryable(
			excludes = KakaoApiQuotaExceededException.class,
			maxRetries = 1L,
			delay = 2L,
			jitter = 1000L)
	@ConcurrencyLimit(limitString = "${custom.kakao.local.concurrency-limit:1}")
	public PlaceSearchByKeywordResponse searchPlaceByKeyword(String keyword) {
		String uri = "/v2/local/search/keyword.JSON?query=" + keyword;
		return placeSearchByKeywordQuotaGuard.executeTracked(() -> {
			var response = restClient.get()
					.uri(uri)
					.retrieve()
					.onStatus(HttpStatusCode::is4xxClientError,
							(req, res) -> handle4xxClientError(res, placeSearchByKeywordQuotaGuard))
					.onStatus(HttpStatusCode::isError, (req, res) -> {
						throw new KakaoApiException("Kakao API Error: "
								+ "Status=" + res.getStatusCode()
								+ ", Headers=" + res.getHeaders()
								+ ", Body=" + new String(res.getBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
					})
					.body(PlaceSearchByKeywordResponse.class);
			logDebugIfNotNull(response);
			return response;
		});
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

	private void handle4xxClientError(ClientHttpResponse res, QuotaGuard quotaGuard)
			throws IOException {
		byte[] bodyBytes = res.getBody().readAllBytes();
		switch (res.getStatusCode().value()) {
			case 400:
				handle400Error(bodyBytes, quotaGuard);
				break;
			default:
		}
		throw new KakaoApiException("Kakao API Error: "
				+ "Status=" + res.getStatusCode()
				+ ", Headers=" + res.getHeaders()
				+ ", Body=" + new String(res.getBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
	}

	private void handle400Error(byte[] bodyBytes, QuotaGuard quotaGuard) {
		Map<String, Object> body = objectMapper.readValue(bodyBytes, new TypeReference<>() {});
		if (body.get("code") instanceof Integer code) {
			switch (code) {
				case -10: // quota exceeded
					quotaGuard.trigger();
					throw new KakaoApiQuotaExceededException("Exceeded Kakao API quota.");
				default:
			}
		}
	}

	private static final class QuotaGuard {

		private final Set<Thread> inFlightThreads = ConcurrentHashMap.newKeySet();
		private volatile LocalDate exceededDate;

		void checkNotExceeded() {
			if (LocalDate.now().equals(exceededDate)) {
				throw new KakaoApiQuotaExceededException("Abort request due to Kakao API quota limit.");
			}
		}

		void trigger() {
			exceededDate = LocalDate.now();
			log.info("Kakao API quota exceeded. Block subsequent requests." +
					" Attempt to cancel requests already in progress.");
			cancelOthers();
		}

		void cancelOthers() {
			Thread currentThread = Thread.currentThread();
			for (Thread thread : inFlightThreads) {
				if (thread != currentThread) {
					thread.interrupt();
				}
			}
		}

		<T> T executeTracked(Supplier<T> call) {
			checkNotExceeded();
			Thread currentThread = Thread.currentThread();
			inFlightThreads.add(currentThread);
			try {
				return call.get();
			} catch (RestClientException e) {
				checkNotExceeded();
				throw e;
			} finally {
				inFlightThreads.remove(currentThread);
				Thread.interrupted();
			}
		}

	}

}
