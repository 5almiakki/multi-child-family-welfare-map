package kr.dagagomap.service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import lombok.Getter;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import kr.dagagomap.entity.Company;
import kr.dagagomap.infrastructure.api.kakao.local.KakaoLocalClient;
import kr.dagagomap.infrastructure.api.kakao.local.dto.AddressToCoordinatesConversionResponse;
import kr.dagagomap.infrastructure.api.publicdata.busan.BusanPublicDataClient;
import kr.dagagomap.infrastructure.api.publicdata.busan.dto.BusanPublicDataResponse;
import kr.dagagomap.repository.CompanyRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Service
public class CompanySyncBatchService {

	private final Semaphore kakaoApiSemaphore;
	private final int pageSize;
	private final int maxPageCount;

	private final AsyncTaskExecutor asyncTaskExecutor;
	private final BusanPublicDataClient publicDataClient;
	private final KakaoLocalClient kakaoLocalClient;
	private final CompanyRepository companyRepository;

	public CompanySyncBatchService(
			@Value("${custom.kakao.local.semaphore-limit:50}") int semaphoreLimit,
			@Value("${custom.public-data.page-size:10}") int pageSize,
			@Value("${custom.public-data.max-page-count:-1}") int maxPageCount,
			@Qualifier("applicationTaskExecutor") AsyncTaskExecutor asyncTaskExecutor,
			BusanPublicDataClient publicDataClient,
			KakaoLocalClient kakaoLocalClient,
			CompanyRepository companyRepository) {
		this.kakaoApiSemaphore = new Semaphore(semaphoreLimit);
		this.pageSize = pageSize;
		this.maxPageCount = maxPageCount;
		this.asyncTaskExecutor = asyncTaskExecutor;
		this.publicDataClient = publicDataClient;
		this.kakaoLocalClient = kakaoLocalClient;
		this.companyRepository = companyRepository;
	}

	/**
	 * 부산시 공공데이터 API의 가족사랑카드 참여 업체 정보를 DB와 동기화한다.
	 * <br>
	 * 공공데이터에 존재하는 업체는 신규 등록하거나 변경된 정보를 반영하고,
	 * 더 이상 공공데이터에 없는 업체는 제거한다.
	 * 주소가 변경된 업체는 Kakao Local API로 좌표를 조회해 반영한다.
	 */
	@Scheduled(cron = "${custom.scheduler.company-update.cron:-}")
	public void syncCompanies() {
		log.info("== Company info update started. ==");
		long beginTime = System.currentTimeMillis();

		// 비동기 요청 시 사용할 전체 업체 수 확인을 위해 첫 페이지 조회
		BusanPublicDataResponse res = publicDataClient.getFamilyLoveCardInfo(1, pageSize);
		int companyCount = res.body().totalCount();
		List<Company> savedCompanies = new ArrayList<>();
		Set<Long> pubDataTaxIds = new HashSet<>();
		// 첫 페이지 내 업체 정보 저장
		addResults(savedCompanies, pubDataTaxIds, getUpdatedCompanies(res.body().items()));

		int pageCount = Math.ceilDiv(companyCount, pageSize);
		// 페이지 조회 상한 별도 지정이 없으면 위에서 얻은 전체 업체 모두 조회
		// (테스트 환경 등에서) 지정했으면 지정한 만큼만 페이지 조회
		if (maxPageCount != -1 && pageCount > maxPageCount) {
			pageCount = maxPageCount;
		}
		// 2페이지부턴 비동기로 조회
		List<CompletableFuture<CompanySyncResult>> futures = new ArrayList<>();
		for (int pageNum = 2; pageNum <= pageCount; pageNum++) {
			int targetPage = pageNum;
			CompletableFuture<CompanySyncResult> future = CompletableFuture.supplyAsync(() -> {
				try {
					BusanPublicDataResponse response = publicDataClient.getFamilyLoveCardInfo(targetPage, pageSize);
					return getUpdatedCompanies(response.body().items());
				} catch (Exception e) {
					log.error("Failed to process page [{}]. Skipping this page", targetPage, e);
					return CompanySyncResult.empty();
				}
			}, asyncTaskExecutor);
			futures.add(future);
		}
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		futures.forEach(future -> addResults(savedCompanies, pubDataTaxIds, future.join()));
		if (!savedCompanies.isEmpty()) {
			companyRepository.saveAll(savedCompanies);
		}
		List<Company> deletedCompanies = companyRepository.findAllByTaxIdNotIn(pubDataTaxIds);
		if (!deletedCompanies.isEmpty()) {
			companyRepository.deleteAll(deletedCompanies);
		}
		long endTime = System.currentTimeMillis();
		log.info("== Company info update completed. Duration: {}ms ==", endTime - beginTime);
	}

	private void addResults(List<Company> savedCompanies, Set<Long> pubDataTaxIds, CompanySyncResult result) {
		savedCompanies.addAll(result.getSavedCompanies());
		pubDataTaxIds.addAll(result.getPubDataTaxIds());
	}

	/**
	 * 기존 업체 엔티티 중 주어진 공공데이터 업체 정보 <code>pubData</code>의 내용과 다른 것을 고르고
	 * <code>pubData</code>의 내용을 반영해 리스트에 담는다.
	 * <br>
	 * DB에서 없앨 대상을 찾기 위해 <code>pubData</code> 내 모든 사업자번호를 셋에 담는다.
	 * <br>
	 * 이 리스트와 셋을 포함하는 객체를 반환한다.
	 *
	 * @param pubData 공공데이터 업체 정보
	 * @return 기존 업체 엔티티 중 <code>pubData</code>의 내용과 다른 것의 리스트
	 */
	private CompanySyncResult getUpdatedCompanies(BusanPublicDataResponse.Body.Item[] pubData) {
		List<Company> oldCompanies = companyRepository.findAllById(
				Arrays.stream(pubData)
						.map(item -> Long.valueOf(item.cpSanum()))
						.toList());
		Map<Long, Company> oldCompanyMap = oldCompanies.stream()
				.collect(Collectors.toMap(Company::getTaxId, c -> c));
		List<Company> savedCompanies = new ArrayList<>();
		Set<Long> pubDataTaxIds = new HashSet<>();
		for (var item : pubData) {
			Long taxId = Long.valueOf(item.cpSanum());
			pubDataTaxIds.add(taxId);
			Company oldCompany = oldCompanyMap.remove(taxId);
			// 아예 새 업체인 경우
			if (oldCompany == null) {
				AddressToCoordinatesConversionResponse coordinates = fetchCoordinatesAsync(item);
				Company company = combine(item, coordinates);
				savedCompanies.add(company);
				continue;
			}
			// 기존 업체인 경우
			// 주소 제외 다른 정보가 바뀐 경우
			boolean changed = false;
			if (isUpdated(item, oldCompany)) {
				changed = true;
				oldCompany.updateWithoutCoordinates(
						item.cpCompname(), item.cpHome(), item.cpClass(), item.cpHgu(), item.cpCeoname(),
						item.cpSidate(), item.cpTel(), item.cpEmail(), item.cpEmailflag(), item.cpInfo(),
						item.cpWoo(), item.cpState(), item.cpImg(), item.cpWebflag());
			}
			// 주소가 바뀐 경우
			if (!Objects.equals(item.cpAddr(), oldCompany.getSourceAddress())) {
				changed = true;
				AddressToCoordinatesConversionResponse coordinates = fetchCoordinatesAsync(item);
				updateCoordinatesIfExists(oldCompany, coordinates);
			}
			if (changed) {
				savedCompanies.add(oldCompany);
			}
		}
		return new CompanySyncResult(savedCompanies, pubDataTaxIds);
	}

	private AddressToCoordinatesConversionResponse fetchCoordinatesAsync(BusanPublicDataResponse.Body.Item item) {
		CompletableFuture<AddressToCoordinatesConversionResponse> future = CompletableFuture.supplyAsync(() -> {
			try {
				kakaoApiSemaphore.acquire();
				var response = kakaoLocalClient.convertAddressToCoordinates(item.cpAddr());
				// TODO 좌표로 변환 못 한 경우 다른 방법으로 재시도
				return response;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException("Rate limiter interrupted", e);
			} finally {
				kakaoApiSemaphore.release();
			}
		}, asyncTaskExecutor);
		return future.join();
	}

	private Company combine(BusanPublicDataResponse.Body.Item item, AddressToCoordinatesConversionResponse response) {
		Company company = Company.builder()
				.taxId(Long.valueOf(item.cpSanum()))
				.name(item.cpCompname())
				.homepageUrl(item.cpHome())
				.category(item.cpClass())
				.gu(item.cpHgu())
				.ceoName(item.cpCeoname())
				.beginDate(item.cpSidate())
				.sourceAddress(item.cpAddr())
				// TODO 전처리된 주소
				// .normalizedAddress("")
				.tel(item.cpTel())
				.email(item.cpEmail())
				.emailFlag(item.cpEmailflag())
				.description(item.cpInfo())
				.benefit(item.cpWoo())
				.usageStatus(item.cpState())
				.img(item.cpImg())
				.webFlag(item.cpWebflag())
				.build();
		updateCoordinatesIfExists(company, response);
		return company;
	}

	/**
	 * 기존 업체 정보 중 주소를 제외한 나머지가 바뀌었는지를 반환한다.
	 *
	 * @param item 공공데이터에서 가져온 업체 정보
	 * @param oldCompany <code>item</code>과 사업자번호가 같은 업체 엔티티
	 * @return 주소를 제외한 업체 정보가 바뀌었는지
	 */
	private boolean isUpdated(BusanPublicDataResponse.Body.Item item, Company oldCompany) {
		return oldCompany == null
				|| !Objects.equals(item.cpCompname(), oldCompany.getName())
				|| !Objects.equals(item.cpHome(), oldCompany.getHomepageUrl())
				|| !Objects.equals(item.cpClass(), oldCompany.getCategory())
				|| !Objects.equals(item.cpHgu(), oldCompany.getGu())
				|| !Objects.equals(item.cpCeoname(), oldCompany.getCeoName())
				|| !Objects.equals(item.cpSidate(), oldCompany.getBeginDate())
				|| !Objects.equals(item.cpTel(), oldCompany.getTel())
				|| !Objects.equals(item.cpEmail(), oldCompany.getEmail())
				|| !Objects.equals(item.cpEmailflag(), oldCompany.getEmailFlag())
				|| !Objects.equals(item.cpInfo(), oldCompany.getDescription())
				|| !Objects.equals(item.cpWoo(), oldCompany.getBenefit())
				|| !Objects.equals(item.cpState(), oldCompany.getUsageStatus())
				|| !Objects.equals(item.cpImg(), oldCompany.getImg())
				|| !Objects.equals(item.cpWebflag(), oldCompany.getWebFlag());
	}

	private void updateCoordinatesIfExists(Company company, AddressToCoordinatesConversionResponse response) {
		if (response == null) {
			log.warn("Company [{}] has no coordinate data due to API failure.", company.getName());
		} else {
			AddressToCoordinatesConversionResponse.Document[] documents = response.documents();
			if (documents != null && documents.length > 0) {
				company.updateCoordinates(Double.valueOf(documents[0].y()), Double.valueOf(documents[0].x()));
			}
		}
	}

	@Getter
	private static class CompanySyncResult {

		private final List<Company> savedCompanies;
		private final Set<Long> pubDataTaxIds;

		public CompanySyncResult(List<Company> savedCompanies, Set<Long> pubDataTaxIds) {
			this.savedCompanies = savedCompanies;
			this.pubDataTaxIds = pubDataTaxIds;
		}

		public static CompanySyncResult empty() {
			return new CompanySyncResult(Collections.emptyList(), Collections.emptySet());
		}

	}

}
