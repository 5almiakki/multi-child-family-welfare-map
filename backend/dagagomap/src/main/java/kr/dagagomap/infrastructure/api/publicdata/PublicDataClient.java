package kr.dagagomap.infrastructure.api.publicdata;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.xml.JacksonXmlHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import kr.dagagomap.infrastructure.api.publicdata.dto.PublicDataResponse;

@Component
public class PublicDataClient {

	private final String publicDataServicekey;
	private final RestClient restClient;

	public PublicDataClient(
			@Value("${custom.public-data-service-key}")
			String publicDataServicekey,
			RestClient.Builder restClientBuilder) {
		this.publicDataServicekey = publicDataServicekey;
		this.restClient = restClientBuilder
				.configureMessageConverters(configurer -> configurer
						.withXmlConverter(new JacksonXmlHttpMessageConverter()))
				.baseUrl("http://apis.data.go.kr")
				.build();
	}

	public PublicDataResponse getFamilyLoveCardInfo() {
		String uri = "/6260000/BusanFmlyLvcrInfoService/getFmlyLvcrInfo?ServiceKey="
				+ publicDataServicekey
				+ "&pageNo=1&numOfRows=9999";
		PublicDataResponse response = restClient.get().uri(uri).retrieve().body(PublicDataResponse.class);
		return response;
	}

}
