package kr.dagagomap.infrastructure.api.publicdata.busan;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.xml.JacksonXmlHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import kr.dagagomap.exception.PublicDataApiException;
import kr.dagagomap.infrastructure.api.publicdata.busan.dto.BusanPublicDataResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BusanPublicDataClient {

	private final String publicDataServicekey;
	private final RestClient restClient;

	public BusanPublicDataClient(
			@Value("${custom.public-data.busan.service-key}")
			String publicDataServicekey,
			RestClient.Builder restClientBuilder) {
		this.publicDataServicekey = publicDataServicekey;
		this.restClient = restClientBuilder
				.configureMessageConverters(configurer -> configurer
						.withXmlConverter(new JacksonXmlHttpMessageConverter()))
				.baseUrl("http://apis.data.go.kr")
				.build();
	}

	public BusanPublicDataResponse getFamilyLoveCardInfo(int pageNo, int numOfRows) {
		String uri = "/6260000/BusanFmlyLvcrInfoService/getFmlyLvcrInfo"
				+ "?ServiceKey=" + publicDataServicekey
				+ "&pageNo=" + pageNo
				+ "&numOfRows=" + numOfRows;
		var response = restClient.get()
				.uri(uri)
				.retrieve()
				.onStatus(HttpStatusCode::isError, this::logErrorAndThrow)
				.body(BusanPublicDataResponse.class);
		log.debug("Response: {}", response);
		return response;
	}

	private void logErrorAndThrow(HttpRequest req, ClientHttpResponse res) throws IOException {
		try (res; var bodyStream = res.getBody()) {
			byte[] bodyBytes = bodyStream.readAllBytes();
			String responseBody = new String(bodyBytes, java.nio.charset.StandardCharsets.UTF_8);
			log.error("PublicData API Error: Status={}, Headers={}, Body={}",
					res.getStatusCode(), res.getHeaders(), responseBody);
			throw new PublicDataApiException("PublicData API Error: " + responseBody);
		}
	}

}
