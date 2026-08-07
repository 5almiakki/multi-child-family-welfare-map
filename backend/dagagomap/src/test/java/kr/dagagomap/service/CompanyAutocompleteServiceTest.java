package kr.dagagomap.service;

import kr.dagagomap.infrastructure.api.kakao.local.KakaoLocalClient;
import kr.dagagomap.infrastructure.api.publicdata.busan.BusanPublicDataClient;
import kr.dagagomap.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static kr.dagagomap.support.PublicDataTestFixtures.existingCompany;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class CompanyAutocompleteServiceTest {

	@Autowired
	private CompanyService companyService;

	@Autowired
	private CompanyRepository companyRepository;

	@MockitoBean
	private BusanPublicDataClient publicDataClient;

	@MockitoBean
	private KakaoLocalClient kakaoLocalClient;

	@BeforeEach
	void setUp() {
		companyRepository.deleteAll();
		companyRepository.saveAll(List.of(
				existingCompany(1L, "해운대 카페", "부산 해운대구 우동 1"),
				existingCompany(2L, "해운대 레스토랑", "부산 해운대구 좌동 2"),
				existingCompany(3L, "광안 카페", "부산 수영구 광안동 3"),
				existingCompany(4L, "서면 맛집", "부산 부산진구 부전동 4"),
				existingCompany(5L, "카페 드롭탑", "부산 해운대구 중동 5")
		));
	}

	@Test
	@DisplayName("키워드를 포함하는 업체명 목록을 반환한다")
	void returnsNamesContainingKeyword() {
		List<String> result = companyService.autocompleteNames("카페", 10);

		assertThat(result)
				.hasSize(3)
				.containsExactlyInAnyOrder("해운대 카페", "광안 카페", "카페 드롭탑");
	}

	@Test
	@DisplayName("limit 개수만큼만 반환한다")
	void respectsLimit() {
		List<String> result = companyService.autocompleteNames("해운대", 1);

		assertThat(result).hasSize(1);
	}

	@Test
	@DisplayName("일치하는 업체명이 없으면 빈 리스트를 반환한다")
	void returnsEmptyListWhenNoMatch() {
		List<String> result = companyService.autocompleteNames("없는키워드xyz", 10);

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("키워드가 null이면 빈 리스트를 반환한다")
	void returnsEmptyListWhenKeywordIsNull() {
		List<String> result = companyService.autocompleteNames(null, 10);

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("키워드가 공백이면 빈 리스트를 반환한다")
	void returnsEmptyListWhenKeywordIsBlank() {
		List<String> result = companyService.autocompleteNames("   ", 10);

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("동일한 업체명은 중복 없이 반환한다")
	void returnsDistinctNames() {
		companyRepository.save(existingCompany(6L, "해운대 카페", "부산 해운대구 우동 99"));

		List<String> result = companyService.autocompleteNames("해운대 카페", 10);

		assertThat(result)
				.hasSize(1)
				.containsExactly("해운대 카페");
	}

}
