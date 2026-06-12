package kr.dagagomap.infrastructure.api.kakao.local;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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

	public AddressToCoordinatesConversionResponse convertAddressToCoordinates(String address) {
		String uri = "/v2/local/search/address.JSON?query=" + address;
		var response = restClient.get().uri(uri).retrieve().body(AddressToCoordinatesConversionResponse.class);

		if (log.isDebugEnabled()) {
			log.debug("Response: {}", response);
			log.debug("Documents.length: {}", response.documents().length);
			for (var document : response.documents()) {
				log.debug("Document: {}", document);
			}
		}
		return response;
	}

	public PlaceSearchByKeywordResponse searchPlaceByKeyword(String keyword) {
		String uri = "/v2/local/search/keyword.JSON?query=" + keyword;
		var response = restClient.get().uri(uri).retrieve().body(PlaceSearchByKeywordResponse.class);

		if (log.isDebugEnabled()) {
			log.debug("Response: {}", response);
			log.debug("Documents.length: {}", response.documents().length);
			for (var document : response.documents()) {
				log.debug("Document: {}", document);
			}
		}
		return response;
	}

}
