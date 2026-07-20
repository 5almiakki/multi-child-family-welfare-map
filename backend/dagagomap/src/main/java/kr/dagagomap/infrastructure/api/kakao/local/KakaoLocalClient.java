package kr.dagagomap.infrastructure.api.kakao.local;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import kr.dagagomap.exception.KakaoApiException;
import kr.dagagomap.infrastructure.api.kakao.local.dto.AddressToCoordinatesConversionResponse;
import kr.dagagomap.infrastructure.api.kakao.local.dto.PlaceSearchByKeywordResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KakaoLocalClient {

	private final RestClient restClient;

	public KakaoLocalClient(
			@Value("${custom.kakao.rest-api-key}")
			String restApiKey,
			RestClient.Builder restClientBuilder) {
		this.restClient = restClientBuilder
				.baseUrl("https://dapi.kakao.com")
				.defaultHeader("Authorization", "KakaoAK " + restApiKey)
				.build();
	}

	@Retryable
	@ConcurrencyLimit(limitString = "${custom.kakao.local.concurrency-limit:50}")
	public AddressToCoordinatesConversionResponse convertAddressToCoordinates(String address) {
		String uri = "/v2/local/search/address.JSON?query=" + address;
		var response = restClient.get()
				.uri(uri)
				.retrieve()
				.onStatus(HttpStatusCode::isError, this::logErrorAndThrow)
				.body(AddressToCoordinatesConversionResponse.class);
		logDebugIfNotNull(response);
		return response;
	}

	@Retryable
	@ConcurrencyLimit(limitString = "${custom.kakao.local.concurrency-limit:50}")
	public PlaceSearchByKeywordResponse searchPlaceByKeyword(String keyword) {
		String uri = "/v2/local/search/keyword.JSON?query=" + keyword;
		var response = restClient.get()
				.uri(uri)
				.retrieve()
				.onStatus(HttpStatusCode::isError, this::logErrorAndThrow)
				.body(PlaceSearchByKeywordResponse.class);
		logDebugIfNotNull(response);
		return response;
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

	private void logErrorAndThrow(HttpRequest req, ClientHttpResponse res) throws IOException {
		try (res; var bodyStream = res.getBody()) {
			byte[] bodyBytes = bodyStream.readAllBytes();
			String responseBody = new String(bodyBytes, java.nio.charset.StandardCharsets.UTF_8);
			log.error("Kakao API Error: Status={}, Headers={}, Body={}",
					res.getStatusCode(), res.getHeaders(), responseBody);
			throw new KakaoApiException("Kakao API Error: " + responseBody);
		}
	}

}
