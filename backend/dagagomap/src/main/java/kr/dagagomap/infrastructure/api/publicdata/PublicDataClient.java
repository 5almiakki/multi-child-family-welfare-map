package kr.dagagomap.infrastructure.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import kr.dagagomap.infrastructure.api.dto.ApiResponse;

@Component
public class PublicDataClient {

	private final String publicDataServicekey;
	private final RestClient restClient;

	public PublicDataClient(
			@Value("${custom.public-data-service-key}")
			String publicDataServicekey,
			RestClient restClient) {
		this.publicDataServicekey = publicDataServicekey;
		this.restClient = restClient;
	}

	public ApiResponse getFamilyLoveCardInfo() {
		String uri = "/6260000/BusanFmlyLvcrInfoService/getFmlyLvcrInfo?ServiceKey="
				+ publicDataServicekey
				+ "&pageNo=1&numOfRows=9999";
		ApiResponse response = restClient.get().uri(uri).retrieve().body(ApiResponse.class);
		return response;
	}

}
