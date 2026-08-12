package kr.dagagomap.integration;

import kr.dagagomap.infrastructure.api.publicdata.busan.BusanPublicDataClient;
import kr.dagagomap.repository.CompanyRepository;
import kr.dagagomap.service.CompanySyncBatchService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest({
		"custom.public-data.busan.page-size=10000"
})
@ActiveProfiles({ "secret", "test" })
@Tag("manual-infra-integration")
class SavePubDataToDbTest {

	@Autowired
	private CompanySyncBatchService companySyncBatchService;

	@Autowired
	private CompanyRepository companyRepository;

	@Autowired
	private BusanPublicDataClient publicDataClient;

	@BeforeEach
	void cleanUp() {
		companyRepository.deleteAll();
	}

	@Test
	@DisplayName("공공데이터 → DB 저장 테스트")
	void savePubDataToDbTest() {
		companySyncBatchService.syncCompaniesWithPublicData();
	}

}
