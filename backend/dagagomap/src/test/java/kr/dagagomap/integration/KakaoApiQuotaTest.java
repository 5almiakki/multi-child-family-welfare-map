package kr.dagagomap.integration;

import kr.dagagomap.exception.KakaoApiQuotaExceededException;
import kr.dagagomap.infrastructure.api.kakao.local.KakaoLocalClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;

@SpringBootTest
@ActiveProfiles({ "secret", "test" })
public class KakaoApiQuotaTest {

	private KakaoLocalClient kakaoLocalClient;
	private MockRestServiceServer mockServer;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder();
		mockServer = MockRestServiceServer.bindTo(builder).build();
		ObjectMapper objectMapper = new ObjectMapper();

		kakaoLocalClient = new KakaoLocalClient("dummy-api-key", builder, objectMapper);
	}

	@Test
	@DisplayName("400 응답의 code가 -10이면 KakaoApiQuotaExceededException이 발생하고 쿼터 초과 상태가 설정된다.")
	void handleQuotaExceededError() {
		String jsonResponseBody = "{\"code\": -10}";
		mockServer.expect(requestTo("https://dapi.kakao.com/v2/local/search/address.JSON?query=Seoul"))
				.andRespond(withBadRequest()
						.contentType(MediaType.APPLICATION_JSON)
						.body(jsonResponseBody));

		assertThatThrownBy(() -> kakaoLocalClient.convertAddressToCoordinates("Seoul"))
				.isInstanceOf(KakaoApiQuotaExceededException.class)
				.hasMessageContaining("Exceeded Kakao API quota.");
		mockServer.verify();
	}

	@Test
	@DisplayName("쿼터가 이미 초과된 당일에는 API를 호출하지 않고 사전 차단(KakaoApiQuotaExceededException)된다.")
	void blockSubsequentRequestsWhenQuotaAlreadyExceeded() {
		String quotaErrorJson = "{\"code\": -10}";
		mockServer.expect(requestTo("https://dapi.kakao.com/v2/local/search/address.JSON?query=FirstRequest"))
				.andRespond(withBadRequest()
						.contentType(MediaType.APPLICATION_JSON)
						.body(quotaErrorJson));

		assertThatThrownBy(() -> kakaoLocalClient.convertAddressToCoordinates("FirstRequest"))
				.isInstanceOf(KakaoApiQuotaExceededException.class);
		assertThatThrownBy(() -> kakaoLocalClient.convertAddressToCoordinates("SecondRequest"))
				.isInstanceOf(KakaoApiQuotaExceededException.class)
				.hasMessageContaining("Abort request due to Kakao API quota limit.");
	}

}
