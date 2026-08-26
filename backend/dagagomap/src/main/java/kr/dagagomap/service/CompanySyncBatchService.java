package kr.dagagomap.service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Getter;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.domain.Limit;
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

	private final int pageSize;
	private final int maxPageCount;

	private final AsyncTaskExecutor asyncTaskExecutor;
	private final BusanPublicDataClient publicDataClient;
	private final KakaoLocalClient kakaoLocalClient;
	private final CompanyRepository companyRepository;

	public CompanySyncBatchService(
			@Value("${custom.public-data.busan.page-size:10}") int pageSize,
			@Value("${custom.public-data.busan.max-page-count:-1}") int maxPageCount,
			@Qualifier("applicationTaskExecutor") AsyncTaskExecutor asyncTaskExecutor,
			BusanPublicDataClient publicDataClient,
			KakaoLocalClient kakaoLocalClient,
			CompanyRepository companyRepository) {
		this.pageSize = pageSize;
		this.maxPageCount = maxPageCount;
		this.asyncTaskExecutor = asyncTaskExecutor;
		this.publicDataClient = publicDataClient;
		this.kakaoLocalClient = kakaoLocalClient;
		this.companyRepository = companyRepository;
	}

	/**
	 * 부산시 공공데이터 API의 가족사랑카드 참여 업체 정보를 DB와 동기화한다.
	 * 공공데이터에 존재하는 업체는 신규 등록하거나 변경된 정보를 반영하고,
	 * 더 이상 공공데이터에 없는 업체는 제거한다.
	 * 주소가 변경된 업체는 Kakao Local API로 좌표를 조회해 반영한다.
	 */
	@Scheduled(cron = "${custom.scheduler.company-update.cron:-}")
	public void syncCompanies() {
		log.info("== Company info update started. ==");
		long beginTime = System.currentTimeMillis();

		syncCompaniesWithPublicData();
		updateCoordinatesWhereRequired();

		long endTime = System.currentTimeMillis();
		log.info("== Company info update completed. Duration: {}ms ==", endTime - beginTime);
	}

	public void syncCompaniesWithPublicData() {
		// 비동기 요청 시 사용할 전체 업체 수 확인을 위해 첫 페이지 조회
		BusanPublicDataResponse res = publicDataClient.getFamilyLoveCardInfo(1, pageSize);
		int companyCount = res.body().totalCount();
		Map<Company.NaturalKey, Company> newOrUpdatedCompanyMap = new HashMap<>();
		Set<Company.NaturalKey> pubDataNaturalKeys = new HashSet<>();
		// 첫 페이지 내 업체 정보 저장
		addResults(newOrUpdatedCompanyMap, pubDataNaturalKeys, makeResultComparingPubDataAndDb(res.body().items()));

		int pageCount = Math.ceilDiv(companyCount, pageSize);
		// 페이지 조회 상한 별도 지정이 없으면 위에서 얻은 전체 업체 모두 조회
		// (테스트 환경 등에서) 지정했으면 지정한 만큼만 페이지 조회
		if (maxPageCount != -1 && pageCount > maxPageCount) {
			pageCount = maxPageCount;
		}

		// 2페이지부턴 비동기로 조회
		List<CompletableFuture<CompanySyncResult>> futures = new ArrayList<>(pageCount - 1);
		for (int pageNum = 2; pageNum <= pageCount; pageNum++) {
			int targetPage = pageNum;
			CompletableFuture<CompanySyncResult> future = CompletableFuture.supplyAsync(() -> {
				try {
					BusanPublicDataResponse response = publicDataClient.getFamilyLoveCardInfo(targetPage, pageSize);
					return makeResultComparingPubDataAndDb(response.body().items());
				} catch (Exception e) {
					log.warn("Failed to process page [{}]. Skipping this page", targetPage);
					return CompanySyncResult.failed();
				}
			}, asyncTaskExecutor);
			futures.add(future);
		}
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		boolean failedPagePresent = false;
		for (CompletableFuture<CompanySyncResult> future : futures) {
			CompanySyncResult result = future.join();
			if (!result.isSuccessful()) {
				failedPagePresent = true;
				continue;
			}
			addResults(newOrUpdatedCompanyMap, pubDataNaturalKeys, result);
		}
		if (!newOrUpdatedCompanyMap.isEmpty()) {
			companyRepository.saveAll(newOrUpdatedCompanyMap.values());
		}
		if (failedPagePresent) {
			return;
		}
		List<Company> deletedCompanies = companyRepository.findAllNotMatchingNameAndAddress(pubDataNaturalKeys);
		if (!deletedCompanies.isEmpty()) {
			companyRepository.deleteAll(deletedCompanies);
		}
	}

	private void addResults(
			Map<Company.NaturalKey, Company> savedCompanyMap, Set<Company.NaturalKey> pubDataNaturalKeys,
			CompanySyncResult result) {
		savedCompanyMap.putAll(result.getNewOrUpdatedCompanyMap());
		pubDataNaturalKeys.addAll(result.getPubDataNaturalKeys());
	}

	private CompanySyncResult makeResultComparingPubDataAndDb(BusanPublicDataResponse.Body.Item[] pubData) {
		Map<Company.NaturalKey, BusanPublicDataResponse.Body.Item> pubDataMap = new HashMap<>();
		for (var item : pubData) {
			Company.NaturalKey naturalKey = item.toNaturalKey();
			var existing = pubDataMap.get(naturalKey);
			if (existing == null) {
				pubDataMap.put(naturalKey, item);
				continue;
			}
			pubDataMap.put(naturalKey, isNewer(item.cpSidate(), existing.cpSidate()) ? item : existing);
		}
		List<Company> dbCompanies = companyRepository.findAllMatchingNameAndAddress(pubDataMap.keySet());
		Map<Company.NaturalKey, Company> dbCompanyMap = dbCompanies.stream()
				.collect(Collectors.toMap(Company::naturalKey, Function.identity()));
		Map<Company.NaturalKey, Company> newOrUpdatedCompanyMap = new HashMap<>();
		for (var entry : pubDataMap.entrySet()) {
			Company.NaturalKey naturalKey = entry.getKey();
			var item = entry.getValue();
			Company dbCompany = dbCompanyMap.get(naturalKey);
			// 아예 새 업체인 경우
			if (dbCompany == null) {
				Company newCompany = item.toCompany();
				newCompany.updateCoordinatesUpdateRequired(true);
				newOrUpdatedCompanyMap.put(naturalKey, newCompany);
				continue;
			}
			// 기존 업체인 경우
			if (!requiresUpdate(item, dbCompany)) {
				continue;
			}
			if (!isNewer(item.cpSidate(), dbCompany.getBeginDate())) {
				continue;
			}
			item.updateCompany(dbCompany);
			newOrUpdatedCompanyMap.put(naturalKey, dbCompany);
		}
		return CompanySyncResult.successful(newOrUpdatedCompanyMap, pubDataMap.keySet());
	}

	private boolean isNewer(String newDate, String existingDate) {
		LocalDate newLocalDate;
		try {
			newLocalDate = LocalDate.parse(newDate);
		} catch (Exception ignored) {
			newLocalDate = LocalDate.MIN;
		}
		LocalDate existingLocalDate;
		try {
			existingLocalDate = LocalDate.parse(existingDate);
		} catch (Exception e) {
			existingLocalDate = LocalDate.MIN;
		}
		if (LocalDate.MIN.equals(newLocalDate) && LocalDate.MIN.equals(existingLocalDate)) {
			return true;
		}
		return newLocalDate.isAfter(existingLocalDate);
	}

	private boolean requiresUpdate(BusanPublicDataResponse.Body.Item item, Company oldCompany) {
		return !Objects.equals(item.cpCompname(), oldCompany.getName())
				|| !Objects.equals(item.cpHome(), oldCompany.getHomepageUrl())
				|| !Objects.equals(item.cpClass(), oldCompany.getCategory())
				|| !Objects.equals(item.cpHgu(), oldCompany.getGu())
				|| !Objects.equals(item.cpCeoname(), oldCompany.getCeoName())
				|| !Objects.equals(item.cpSidate(), oldCompany.getBeginDate())
				|| !Objects.equals(item.cpAddr(), oldCompany.getSourceAddress())
				|| !Objects.equals(item.cpTel(), oldCompany.getTel())
				|| !Objects.equals(item.cpEmail(), oldCompany.getEmail())
				|| !Objects.equals(item.cpEmailflag(), oldCompany.getEmailFlag())
				|| !Objects.equals(item.cpInfo(), oldCompany.getDescription())
				|| !Objects.equals(item.cpWoo(), oldCompany.getSourceBenefit())
				|| !Objects.equals(item.cpState(), oldCompany.getUsageStatus())
				|| !Objects.equals(item.cpImg(), oldCompany.getImg())
				|| !Objects.equals(item.cpWebflag(), oldCompany.getWebFlag());
	}

	public void updateCoordinatesWhereRequired() {
		List<Company> companies = companyRepository.findByCoordinatesUpdateRequired(true, Limit.unlimited());
		updateCoordinatesWhereRequiredAndSave(companies);
	}

	public void updateCoordinatesWhereRequired(int count) {
		List<Company> companies = companyRepository.findByCoordinatesUpdateRequired(true, Limit.of(count));
		updateCoordinatesWhereRequiredAndSave(companies);
	}

	private void updateCoordinatesWhereRequiredAndSave(Collection<Company> companies) {
		List<CompletableFuture<Void>> futures = new ArrayList<>(companies.size());
		companies.forEach(company -> {
			Executor jitterExecutor = CompletableFuture.delayedExecutor(
					ThreadLocalRandom.current().nextLong(2000L), TimeUnit.MILLISECONDS, asyncTaskExecutor);
			CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
				try {
					AddressToCoordinatesConversionResponse response = fetchCoordinates(company.getSourceAddress());
					updateCoordinatesIfExists(company, response);
				} catch (Exception e) {
					log.warn(
							"Failed to update coordinates for company [{}]. Skipping this company. {}",
							company.getName(), e.getMessage());
				}
			}, jitterExecutor);
			futures.add(future);
		});
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		if (!companies.isEmpty()) {
			companyRepository.saveAll(companies);
		}
	}

	private AddressToCoordinatesConversionResponse fetchCoordinates(String address) {
		var response = kakaoLocalClient.convertAddressToCoordinates(address);
		// TODO 좌표로 변환 못 한 경우 다른 방법으로 재시도
		return response;
	}

	private void updateCoordinatesIfExists(Company company, AddressToCoordinatesConversionResponse response) {
		if (response == null) {
			log.warn("Company [{}] has no coordinate data due to API failure.", company.getName());
			return;
		}
		AddressToCoordinatesConversionResponse.Document[] documents = response.documents();
		if (documents == null) {
			log.warn("Company [{}] has no coordinate data due to API failure.", company.getName());
			return;
		}
		if (documents.length == 0) {
			log.warn("Company [{}] has no coordinate data due to invalid address.", company.getName());
			company.updateCoordinatesUpdateRequired(false);
			return;
		}
		company.updateCoordinates(Double.valueOf(documents[0].y()), Double.valueOf(documents[0].x()));
		company.updateCoordinatesUpdateRequired(false);
	}

	@Getter
	private static class CompanySyncResult {

		private final boolean successful;
		private final Map<Company.NaturalKey, Company> newOrUpdatedCompanyMap;
		private final Set<Company.NaturalKey> pubDataNaturalKeys;

		private CompanySyncResult(
				boolean successful, Map<Company.NaturalKey, Company> newOrUpdatedCompanyMap,
				Set<Company.NaturalKey> pubDataNaturalKeys) {
			this.successful = successful;
			this.newOrUpdatedCompanyMap = newOrUpdatedCompanyMap;
			this.pubDataNaturalKeys = pubDataNaturalKeys;
		}

		private static CompanySyncResult successful(
				Map<Company.NaturalKey, Company> newOrUpdatedCompanyMap, Set<Company.NaturalKey> pubDataNaturalKeys) {
			return new CompanySyncResult(true, newOrUpdatedCompanyMap, pubDataNaturalKeys);
		}

		private static CompanySyncResult failed() {
			return new CompanySyncResult(false, Collections.emptyMap(), Collections.emptySet());
		}

	}

}
