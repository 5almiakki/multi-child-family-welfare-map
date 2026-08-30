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

import java.util.Arrays;

import static kr.dagagomap.support.PublicDataTestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
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
class CompanySyncBatchServiceTest {

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

		Company company = companyRepository.findByNameAndSourceAddress("신규카페", "부산 해운대구 우동 1").orElseThrow();
		assertThat(company.getTaxId()).isEqualTo("1010101010");
		assertThat(company.getName()).isEqualTo("신규카페");
		assertThat(company.getLatitude()).isEqualTo(35.1796);
		assertThat(company.getLongitude()).isEqualTo(129.0756);
		verify(kakaoLocalClient).convertAddressToCoordinates("부산 해운대구 우동 1");
	}

	@Test
	@DisplayName("기존 업체의 주소를 제외하고 변경된 정보(전화번호 등)를 반영한다")
	void updatesChangedCompanyFields() {
		companyRepository.save(existingCompany("2020202020", "변경업체", "부산 해운대구 좌동 10"));
		BusanPublicDataResponse.Body.Item updatedItem = item("2020202020", "변경업체", "부산 해운대구 좌동 10");
		stubSinglePage(updatedItem);

		companySyncBatchService.syncCompanies();

		Company company = companyRepository.findByNameAndSourceAddress("변경업체", "부산 해운대구 좌동 10").orElseThrow();
		assertThat(company.getTel()).isEqualTo("051-000-0000");
		verify(kakaoLocalClient, never()).convertAddressToCoordinates(anyString());
	}

	@Test
	@DisplayName("주소가 변경된 기존 업체의 좌표를 다시 조회해 반영한다")
	void refetchesCoordinatesWhenAddressChanges() {
		Company company = existingCompany("3030303030", "좌표갱신상점", "부산 해운대구 중동 1");
		company.updateCoordinates(1.0, 1.0);
		companyRepository.save(company);

		BusanPublicDataResponse.Body.Item movedItem = item("3030303030", "좌표갱신상점", "부산 수영구 광안동 2");
		stubSinglePage(movedItem);
		when(kakaoLocalClient.convertAddressToCoordinates("부산 수영구 광안동 2"))
				.thenReturn(coordinates(35.1532, 129.1186));

		companySyncBatchService.syncCompanies();

		assertThat(companyRepository.findByNameAndSourceAddress("좌표갱신상점", "부산 해운대구 중동 1")).isEmpty();
		Company updated = companyRepository.findByNameAndSourceAddress("좌표갱신상점", "부산 수영구 광안동 2").orElseThrow();
		assertThat(updated.getLatitude()).isEqualTo(35.1532);
		assertThat(updated.getLongitude()).isEqualTo(129.1186);
		verify(kakaoLocalClient).convertAddressToCoordinates("부산 수영구 광안동 2");
	}

	@Test
	@DisplayName("주소 이외 정보가 바뀐 업체는 좌표 갱신 필요 여부가 그대로다")
	void hasSameCoordinatesUpdateRequiredWhenAddressUnchanged() {
		Company company1 = existingCompany("1", "name1", "address1", "051-000-0000");
		company1.updateCoordinatesUpdateRequired(true);
		companyRepository.save(company1);
		var item1 = item("1", "name1", "address1", "051-000-0000");
		stubSinglePage(item1);

		companySyncBatchService.syncCompanies();

		Company result1 = companyRepository.findByNameAndSourceAddress("name1", "address1").orElseThrow();
		assertThat(result1.getCoordinatesUpdateRequired())
				.isTrue();

		companyRepository.deleteAll();

		Company company2 = existingCompany("2", "name2", "address2", "051-000-0002");
		company2.updateCoordinatesUpdateRequired(false);
		companyRepository.save(company2);
		var item2 = item("2", "name2", "address2", "051-000-0003");
		stubSinglePage(item2);

		companySyncBatchService.syncCompanies();

		Company result2 = companyRepository.findByNameAndSourceAddress("name2", "address2").orElseThrow();
		assertThat(result2.getCoordinatesUpdateRequired())
				.isFalse();
	}

	@Test
	@DisplayName("변경되지 않은 기존 업체는 저장 대상에서 제외한다")
	void skipsUnchangedCompanies() {
		BusanPublicDataResponse.Body.Item sameItem = item("4040404040", "변경없음", "부산 해운대구 우동 3");
		companyRepository.save(existingCompanyMatching(sameItem));
		stubSinglePage(sameItem);

		companySyncBatchService.syncCompanies();

		Company company = companyRepository.findByNameAndSourceAddress("변경없음", "부산 해운대구 우동 3").orElseThrow();
		assertThat(company.getHomepageUrl()).isEqualTo(sameItem.cpHome());
		verify(kakaoLocalClient, never()).convertAddressToCoordinates(anyString());
	}

	@Test
	@DisplayName("공공데이터에 없는 기존 업체를 완전히 삭제한다")
	void deletesCompaniesMissingFromPublicData() {
		companyRepository.save(existingCompany("5050505050", "삭제대상", "부산 해운대구 우동 4"));
		BusanPublicDataResponse.Body.Item activeItem = item("6060606060", "유지대상", "부산 해운대구 우동 5");
		stubSinglePage(activeItem);

		companySyncBatchService.syncCompanies();

		assertThat(companyRepository.findByNameAndSourceAddress("유지대상", "부산 해운대구 우동 5")).isPresent();
		assertThat(companyRepository.findByNameAndSourceAddress("삭제대상", "부산 해운대구 우동 4")).isEmpty();
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
				.extracting(Company::getName)
				.containsExactlyInAnyOrder("1페이지업체", "2페이지업체");
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
				.extracting(Company::getName)
				.containsExactly("성공업체");
	}

	@Test
	@DisplayName("식별자가 중복되는 업체를 포함하는 공공데이터를 DB에 여러 번 저장해도 예외 발생이 없다")
	void savesPubDataWithDuplicateKeysWithoutException() {
		BusanPublicDataResponse.Body.Item item = item("9090909090", "성공업체", "부산 해운대구 우동 8");
		when(publicDataClient.getFamilyLoveCardInfo(1, 2))
				.thenReturn(page(4, 1, 2, item, item));

		assertThatCode(() -> {
			companySyncBatchService.syncCompanies();
			companySyncBatchService.syncCompanies();
		}).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("공공데이터에 식별자가 같은 업체가 여럿이고 시행 일자가 모두 유효하면 최신 업체만 저장한다")
	void savesSingleRecentCompanyWhenDuplicateKeysAndValidDatesExist() {
		stubCompaniesWithDates("name", "address", "2020-01-01", "2020-01-02");

		companySyncBatchService.syncCompanies();

		assertThat(companyRepository.findAll())
				.hasSize(1)
				.extracting(Company::getBeginDate)
				.containsExactly("2020-01-02");
	}

	@Test
	@DisplayName("공공데이터에 식별자가 같은 업체가 여럿이고 시행 일자가 모두 무효하면 한 업체만 저장한다")
	void savesSingleCompanyWhenDuplicateKeysAndInvalidDatesExist() {
		stubCompaniesWithDates("name", "address", null, "", "invalid");

		companySyncBatchService.syncCompanies();

		assertThat(companyRepository.findAll())
				.hasSize(1);
	}

	@Test
	@DisplayName("공공데이터에 식별자가 같은 업체가 여럿이고 시행 일자가 유효 무효 섞여 있으면 최신 유효한 업체만 저장한다")
	void savesRecentValidDataWhenDuplicateKeysAndMixedDatesExist() {
		stubCompaniesWithDates("name", "address", "2020-01-01", "2020-01-02", "invalid");

		companySyncBatchService.syncCompanies();

		assertThat(companyRepository.findAll())
				.hasSize(1)
				.extracting(Company::getBeginDate)
				.containsExactly("2020-01-02");
	}

	@Test
	@DisplayName("공공데이터, DB의 업체 시행일자가 유효하면 최신 것을 선택한다")
	void choosesRecentDataWhenDbAndPubDataValid() {
		// 공공데이터가 최신인 경우
		companyRepository.save(existingCompany("1", "name", "address", "2019-01-03"));
		stubCompaniesWithDates("name", "address", "2020-01-01", "2020-01-02");

		companySyncBatchService.syncCompanies();

		assertThat(companyRepository.findAll())
				.hasSize(1)
				.extracting(Company::getBeginDate)
				.containsExactly("2020-01-02");

		companyRepository.deleteAll();

		// DB가 최신인 경우
		companyRepository.save(existingCompany("1", "name", "address", "2020-01-03"));
		stubCompaniesWithDates("name", "address", "2020-01-01", "2020-01-02");

		companySyncBatchService.syncCompanies();

		assertThat(companyRepository.findAll())
				.hasSize(1)
				.extracting(Company::getBeginDate)
				.containsExactly("2020-01-03");
	}

	@Test
	@DisplayName("DB 시행일자가 유효하고 공공데이터 시행일자가 무효하면 기존 DB 데이터를 유지한다")
	void retainsDbCompanyWhenDbValidAndPubDataInvalid() {
		companyRepository.save(existingCompany("1", "name", "address", "2020-02-02"));
		stubCompaniesWithDates("name", "address", "invalid", "null");

		companySyncBatchService.syncCompanies();

		assertThat(companyRepository.findAll())
				.hasSize(1)
				.extracting(Company::getBeginDate)
				.containsExactly("2020-02-02");
	}

	@Test
	@DisplayName("공공데이터, DB의 업체 시행일자가 각각 혼합, 유효하면 유효한 최신 것을 선택한다")
	void choosesRecentDataWhenDbValidAndPubDataMixed() {
		companyRepository.save(existingCompany("1", "name", "address", "2020-02-02"));
		stubCompaniesWithDates("name", "address", "invalid", "2020-02-03");

		companySyncBatchService.syncCompanies();

		assertThat(companyRepository.findAll())
				.hasSize(1)
				.extracting(Company::getBeginDate)
				.containsExactly("2020-02-03");

		companyRepository.deleteAll();

		companyRepository.save(existingCompany("1", "name", "address", "2020-02-02"));
		stubCompaniesWithDates("name", "address", "invalid", "2020-02-01");

		companySyncBatchService.syncCompanies();

		assertThat(companyRepository.findAll())
				.hasSize(1)
				.extracting(Company::getBeginDate)
				.containsExactly("2020-02-02");
	}

	@Test
	@DisplayName("DB의 업체 시행일자가 무효하면 유효한 공공데이터 중 최신, 모두 무효면 공공데이터 중 하나를 저장한다")
	void savesPubDataWhenDbInvalid() {
		companyRepository.save(existingCompany("1", "name1", "address1", "null"));
		stubCompaniesWithDates("name1", "address1", "invalid", "2020-02-02");

		companySyncBatchService.syncCompanies();

		assertThat(companyRepository.findAll())
				.hasSize(1)
				.extracting(Company::getBeginDate)
				.containsExactly("2020-02-02");

		companyRepository.deleteAll();

		companyRepository.save(existingCompany("1", "name", "address", "null"));
		stubCompaniesWithDates("name", "address", "2020-02-02", "2020-02-01");

		companySyncBatchService.syncCompanies();

		assertThat(companyRepository.findAll())
				.hasSize(1)
				.extracting(Company::getBeginDate)
				.containsExactly("2020-02-02");

		companyRepository.deleteAll();

		companyRepository.save(existingCompany("1", "name", "address", "null"));
		stubCompaniesWithDates("name", "address", "invalid", "");

		companySyncBatchService.syncCompanies();

		assertThat(companyRepository.findAll())
				.hasSize(1)
				.extracting(Company::getBeginDate)
				.doesNotContain("null");
	}

	private void stubSinglePage(BusanPublicDataResponse.Body.Item item) {
		when(publicDataClient.getFamilyLoveCardInfo(1, 2))
				.thenReturn(page(1, 1, 2, item));
	}

	private void stubCompaniesWithDates(String name, String address, String... dates) {
		BusanPublicDataResponse.Body.Item[] items = Arrays.stream(dates)
				.map(date -> item("1", name, address, date))
				.toArray(BusanPublicDataResponse.Body.Item[]::new);
		when(publicDataClient.getFamilyLoveCardInfo(1, 2))
				.thenReturn(page(4, 1, 2, items));
	}

}
