package kr.dagagomap.infrastructure.api.publicdata.busan;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.xml.JacksonXmlHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import kr.dagagomap.infrastructure.api.publicdata.busan.dto.BusanPublicDataResponse;

@Component
public class BusanPublicDataClient {

	private final String publicDataServicekey;
	private final RestClient restClient;

	public BusanPublicDataClient(
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

	public BusanPublicDataResponse getFamilyLoveCardInfo() {
		String uri = "/6260000/BusanFmlyLvcrInfoService/getFmlyLvcrInfo?ServiceKey="
				+ publicDataServicekey
				+ "&pageNo=1&numOfRows=9999";
		BusanPublicDataResponse response = restClient.get().uri(uri).retrieve().body(BusanPublicDataResponse.class);
		return response;
	}

}
