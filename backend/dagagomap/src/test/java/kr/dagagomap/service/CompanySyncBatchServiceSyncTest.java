package kr.dagagomap.service;

import kr.dagagomap.entity.Company;
import kr.dagagomap.infrastructure.api.kakao.local.KakaoLocalClient;
import kr.dagagomap.infrastructure.api.publicdata.busan.BusanPublicDataClient;
import kr.dagagomap.infrastructure.api.publicdata.busan.dto.BusanPublicDataResponse;
import kr.dagagomap.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static kr.dagagomap.support.PublicDataTestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
		"logging.level.kr.dagagomap.service.CompanySyncBatchService=DEBUG"
})
@ActiveProfiles("test")
class CompanySyncBatchServiceSyncTest {

	@Autowired
	private CompanySyncBatchService companySyncBatchService;

	@Autowired
	private CompanyRepository companyRepository;

	@MockitoBean
	private BusanPublicDataClient publicDataClient;

	@MockitoBean
	private KakaoLocalClient kakaoLocalClient;

	@BeforeEach
	void setUp() {
		companyRepository.deleteAll();
	}

	@Test
	@DisplayName("공공데이터에 있는 신규 업체를 좌표와 함께 저장한다")
	void savesNewCompaniesFromPublicData() {
		BusanPublicDataResponse.Body.Item newItem = item("1010101010", "신규카페", "부산 해운대구 우동 1");
		stubSinglePage(newItem);
		when(kakaoLocalClient.convertAddressToCoordinates("부산 해운대구 우동 1"))
				.thenReturn(coordinates(35.1796, 129.0756));

		companySyncBatchService.syncCompanies();

		List<Company> companies = companyRepository.findAll();
		assertThat(companies).hasSize(1);
		assertThat(companies.get(0).getTaxId()).isEqualTo(1010101010L);
		assertThat(companies.get(0).getName()).isEqualTo("신규카페");
		assertThat(companies.get(0).getLatitude()).isEqualTo(35.1796);
		assertThat(companies.get(0).getLongitude()).isEqualTo(129.0756);
		verify(kakaoLocalClient).convertAddressToCoordinates("부산 해운대구 우동 1");
	}

	@Test
	@DisplayName("기존 업체의 주소를 제외하고 변경된 정보를 반영한다")
	void updatesChangedCompanyFields() {
		companyRepository.save(existingCompany(2020202020L, "구이름", "부산 해운대구 좌동 10"));
		BusanPublicDataResponse.Body.Item updatedItem = item("2020202020", "신이름", "부산 해운대구 좌동 10");
		stubSinglePage(updatedItem);

		companySyncBatchService.syncCompanies();

		Company company = companyRepository.findById(2020202020L).orElseThrow();
		assertThat(company.getName()).isEqualTo("신이름");
		verify(kakaoLocalClient, never()).convertAddressToCoordinates(anyString());
	}

	@Test
	@DisplayName("주소가 변경된 기존 업체의 좌표를 다시 조회해 반영한다")
	void refetchesCoordinatesWhenAddressChanges() {
		Company company = existingCompany(3030303030L, "좌표갱신상점", "부산 해운대구 중동 1");
		company.updateCoordinates(1.0, 1.0);
		companyRepository.save(company);

		BusanPublicDataResponse.Body.Item movedItem = item("3030303030", "좌표갱신상점", "부산 수영구 광안동 2");
		stubSinglePage(movedItem);
		when(kakaoLocalClient.convertAddressToCoordinates("부산 수영구 광안동 2"))
				.thenReturn(coordinates(35.1532, 129.1186));

		companySyncBatchService.syncCompanies();

		Company updated = companyRepository.findById(3030303030L).orElseThrow();
		assertThat(updated.getLatitude()).isEqualTo(35.1532);
		assertThat(updated.getLongitude()).isEqualTo(129.1186);
		verify(kakaoLocalClient).convertAddressToCoordinates("부산 수영구 광안동 2");
	}

	@Test
	@DisplayName("변경되지 않은 기존 업체는 저장 대상에서 제외한다")
	void skipsUnchangedCompanies() {
		BusanPublicDataResponse.Body.Item sameItem = item("4040404040", "변경없음", "부산 해운대구 우동 3");
		companyRepository.save(existingCompanyMatching(sameItem));
		stubSinglePage(sameItem);

		companySyncBatchService.syncCompanies();

		Company company = companyRepository.findById(4040404040L).orElseThrow();
		assertThat(company.getHomepageUrl()).isEqualTo(sameItem.cpHome());
		verify(kakaoLocalClient, never()).convertAddressToCoordinates(anyString());
	}

	@Test
	@DisplayName("공공데이터에 없는 기존 업체를 완전히 삭제한다")
	void deletesCompaniesMissingFromPublicData() {
		companyRepository.save(existingCompany(5050505050L, "삭제대상", "부산 해운대구 우동 4"));
		BusanPublicDataResponse.Body.Item activeItem = item("6060606060", "유지대상", "부산 해운대구 우동 5");
		stubSinglePage(activeItem);

		companySyncBatchService.syncCompanies();

		assertThat(companyRepository.findById(6060606060L)).isPresent();
		assertThat(companyRepository.findById(5050505050L)).isEmpty();
	}

	@Test
	@DisplayName("여러 페이지를 조회해 모든 업체를 저장한다")
	void fetchesAndSavesCompaniesAcrossMultiplePages() {
		BusanPublicDataResponse.Body.Item page1Item = item("7070707070", "1페이지업체", "부산 해운대구 우동 6");
		BusanPublicDataResponse.Body.Item page2Item = item("8080808080", "2페이지업체", "부산 해운대구 우동 7");

		when(publicDataClient.getFamilyLoveCardInfo(1, 2))
				.thenReturn(page(3, 1, 2, page1Item));
		when(publicDataClient.getFamilyLoveCardInfo(2, 2))
				.thenReturn(page(3, 2, 2, page2Item));
		when(publicDataClient.getFamilyLoveCardInfo(3, 2))
				.thenThrow(new RuntimeException("should not request third page"));

		companySyncBatchService.syncCompanies();

		assertThat(companyRepository.findAll())
				.extracting(Company::getTaxId)
				.containsExactlyInAnyOrder(7070707070L, 8080808080L);
		verify(publicDataClient).getFamilyLoveCardInfo(1, 2);
		verify(publicDataClient).getFamilyLoveCardInfo(2, 2);
		verify(publicDataClient, never()).getFamilyLoveCardInfo(eq(3), anyInt());
	}

	@Test
	@DisplayName("페이지 조회 실패 시 해당 페이지는 건너뛰고 나머지를 저장한다")
	void skipsFailedPageAndContinuesSync() {
		BusanPublicDataResponse.Body.Item page1Item = item("9090909090", "성공업체", "부산 해운대구 우동 8");

		when(publicDataClient.getFamilyLoveCardInfo(1, 2))
				.thenReturn(page(4, 1, 2, page1Item));
		when(publicDataClient.getFamilyLoveCardInfo(2, 2))
				.thenThrow(new RuntimeException("public data timeout"));

		companySyncBatchService.syncCompanies();

		assertThat(companyRepository.findAll())
				.extracting(Company::getTaxId)
				.containsExactly(9090909090L);
	}

	private void stubSinglePage(BusanPublicDataResponse.Body.Item item) {
		when(publicDataClient.getFamilyLoveCardInfo(1, 2))
				.thenReturn(page(1, 1, 2, item));
	}

}
