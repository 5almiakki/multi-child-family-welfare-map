package kr.dagagomap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.xml.JacksonXmlHttpMessageConverter;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

	@Bean
	public RestClient restClient(RestClient.Builder builder) {
		JacksonXmlHttpMessageConverter converter = new JacksonXmlHttpMessageConverter();
		return builder
				.configureMessageConverters(configurer -> configurer
						.withXmlConverter(converter))
				.baseUrl("http://apis.data.go.kr")
				.build();
	}

}
