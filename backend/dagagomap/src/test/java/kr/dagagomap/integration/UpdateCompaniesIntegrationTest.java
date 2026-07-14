package kr.dagagomap.integration;

import kr.dagagomap.entity.Company;
import kr.dagagomap.repository.CompanyJpaRepository;
import kr.dagagomap.service.CompanyService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(properties = {
		"custom.public-data.page-size=1",
		"custom.public-data.max-page-count=1"
})
@ActiveProfiles({ "secret", "infra-integration-test" })
@Tag("manual-infra-integration")
class UpdateCompaniesIntegrationTest {

	@Autowired
	private CompanyService companyService;

	@Autowired
	private CompanyJpaRepository companyJpaRepository;

	@BeforeEach
	void cleanUp() {
		companyJpaRepository.deleteAll();
	}

	@Test
	@DisplayName("공공데이터 → 카카오 API 좌표 변환 → DB 저장 전체 흐름 통합 테스트")
	void updateCompaniesTest() {
		// When: 실제 외부 API 호출 및 저장 실행
		companyService.updateCompanies();

		// Then: DB에 실제로 저장되었는지 검증
		List<Company> savedCompanies = companyJpaRepository.findAll();

		// 1. 하나라도 저장되어 있어야 함
		assertThat(savedCompanies).isNotEmpty();
		log.info("총 저장된 업체 수: {}", savedCompanies.size());

		// 2. 좌표가 채워진 업체와 아닌 업체 분리하여 현황 출력
		List<Company> withCoordinates = new ArrayList<>();
		List<Company> withoutCoordinates = new ArrayList<>();
		savedCompanies.forEach(company -> {
			if (company.getLatitude() != null && company.getLongitude() != null) {
				withCoordinates.add(company);
			} else {
				log.warn("좌표 없음 - 업체명: [{}], 주소: [{}]", company.getName(), company.getSourceAddress());
				withoutCoordinates.add(company);
			}
		});

		log.info("좌표 변환 성공: {}건", withCoordinates.size());
		log.info("좌표 변환 실패(주소 불량 등): {}건", withoutCoordinates.size());
	}

}
