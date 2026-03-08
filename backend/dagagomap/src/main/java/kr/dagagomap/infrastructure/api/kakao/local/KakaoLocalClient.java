package kr.dagagomap.infrastructure.api.kakao.local;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import kr.dagagomap.infrastructure.api.kakao.local.dto.KakaoLocalResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KakaoLocalClient {

	private final RestClient restClient;

	public KakaoLocalClient(
			@Value("${custom.kakao-rest-api-key}")
			String restApiKey,
			RestClient.Builder restClientBuilder) {
		this.restClient = restClientBuilder
				.baseUrl("https://dapi.kakao.com")
				.defaultHeader("Authorization", "KakaoAK " + restApiKey)
				.build();
	}

	public KakaoLocalResponse searchAddress(String address) {
		String uri = "/v2/local/search/address.json?query=" + address;
		var response = restClient.get().uri(uri).retrieve().body(KakaoLocalResponse.class);

		log.debug("Response: {}", response);
		log.debug("Documents.length: {}", response.documents().length);
		Arrays.stream(response.documents())
				.forEach(document -> log.debug("Document: {}", document));

		return response;
	}

}
