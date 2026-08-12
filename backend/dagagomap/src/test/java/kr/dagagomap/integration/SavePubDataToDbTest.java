package kr.dagagomap.integration;

import kr.dagagomap.entity.Company;
import kr.dagagomap.infrastructure.api.publicdata.busan.BusanPublicDataClient;
import kr.dagagomap.infrastructure.api.publicdata.busan.dto.BusanPublicDataResponse;
import kr.dagagomap.repository.CompanyRepository;
import kr.dagagomap.service.CompanySyncBatchService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest({
		"custom.public-data.busan.page-size=100"
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
		BusanPublicDataResponse res = publicDataClient.getFamilyLoveCardInfo(1, 1);
		int companyCount = res.body().totalCount();
		try {
			companySyncBatchService.syncCompaniesWithPublicData();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			assertThat(e).isNull();
		}
		assertThat(companyRepository.findAll()).hasSize(companyCount);
	}

}
