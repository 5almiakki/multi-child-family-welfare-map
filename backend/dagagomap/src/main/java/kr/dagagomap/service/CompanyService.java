package kr.dagagomap.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import kr.dagagomap.entity.Company;
import kr.dagagomap.infrastructure.api.kakao.local.KakaoLocalClient;
import kr.dagagomap.infrastructure.api.kakao.local.dto.AddressToCoordinatesConversionResponse;
import kr.dagagomap.infrastructure.api.publicdata.busan.BusanPublicDataClient;
import kr.dagagomap.infrastructure.api.publicdata.busan.dto.BusanPublicDataResponse;
import kr.dagagomap.repository.CompanyJpaRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Service
public class CompanyService {

	private final Semaphore kakaoApiSemaphore;
	private final int pageSize;

	private final AsyncTaskExecutor asyncTaskExecutor;
	private final BusanPublicDataClient publicDataClient;
	private final KakaoLocalClient kakaoLocalClient;
	private final CompanyJpaRepository companyJpaRepository;

	public CompanyService(
			@Value("${custom.kakao.local.semaphore-limit:50}") int semaphoreLimit,
			@Value("${custom.public-data.page-size:10}") int pageSize,
			@Qualifier("applicationTaskExecutor") AsyncTaskExecutor asyncTaskExecutor,
			BusanPublicDataClient publicDataClient,
			KakaoLocalClient kakaoLocalClient,
			CompanyJpaRepository companyJpaRepository) {
		this.kakaoApiSemaphore = new Semaphore(semaphoreLimit);
		this.pageSize = pageSize;
		this.asyncTaskExecutor = asyncTaskExecutor;
		this.publicDataClient = publicDataClient;
		this.kakaoLocalClient = kakaoLocalClient;
		this.companyJpaRepository = companyJpaRepository;
	}

	@Scheduled(cron = "${custom.scheduler.company-update.cron:-}")
	public void updateCompanies() {
		log.info("== Company info update started. ==");
		long beginTime = System.currentTimeMillis();
		int totalSavedCount = 0;
		for (int pageNo = 1;; pageNo++) {
			BusanPublicDataResponse publicDataResponse =
					publicDataClient.getFamilyLoveCardInfo(pageNo, pageSize);
			int itemCount = publicDataResponse.body().numOfRows();
			BusanPublicDataResponse.Body.Item[] items = publicDataResponse.body().items();
			log.info("Public data page {} fetched, item count: {}", pageNo, itemCount);

			AddressToCoordinatesConversionResponse[] responses = fetchCoordinatesAsync(items);
			List<Company> companies = combine(items, responses);
			companyJpaRepository.saveAll(companies);

			totalSavedCount += itemCount;
			if (itemCount < pageSize) {
				break;
			}
		}
		long endTime = System.currentTimeMillis();
		log.info("== Company info update completed. Duration: {}ms, Total saved count: {} ==",
				endTime - beginTime, totalSavedCount);
	}

	private AddressToCoordinatesConversionResponse[] fetchCoordinatesAsync(BusanPublicDataResponse.Body.Item[] items) {
		AddressToCoordinatesConversionResponse[] responses = new AddressToCoordinatesConversionResponse[items.length];
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (int i = 0; i < items.length; i++) {
			int idx = i;
			futures.add(
					CompletableFuture.supplyAsync(() -> {
						try {
							kakaoApiSemaphore.acquire();
							return kakaoLocalClient.convertAddressToCoordinates(items[idx].cpAddr());
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							throw new RuntimeException("Rate limiter interrupted", e);
						} finally {
							kakaoApiSemaphore.release();
						}
					}, asyncTaskExecutor)
					.thenAccept(res -> {
						responses[idx] = res;
					})
					.exceptionally(e -> {
						log.error("Error while fetching coordinate for company. Exception: {}", e);
						return null;
					}));
		}
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		return responses;
	}

	private List<Company> combine(BusanPublicDataResponse.Body.Item[] items,
			AddressToCoordinatesConversionResponse[] responses) {
		List<Company> companies = new ArrayList<>();
		for (int i = 0; i < items.length; i++) {
			Company company = Company.builder()
					.taxId(Integer.valueOf(items[i].cpSanum()))
					.name(items[i].cpCompname())
					.homepageUrl(items[i].cpHome())
					.category(items[i].cpClass())
					.gu(items[i].cpHgu())
					.ceoName(items[i].cpCeoname())
					.beginDate(items[i].cpSidate())
					.address(items[i].cpAddr())
					.tel(items[i].cpTel())
					.email(items[i].cpEmail())
					.emailFlag(items[i].cpEmailflag())
					.description(items[i].cpInfo())
					.benefit(items[i].cpWoo())
					.usageStatus(items[i].cpState())
					.img(items[i].cpImg())
					.webFlag(items[i].cpWebflag())
					.build();
			if (responses[i] == null) {
				log.warn("Company [{}] has no coordinate data due to API failure.", items[i].cpCompname());
			} else {
				AddressToCoordinatesConversionResponse.Document[] documents = responses[i].documents();
				if (documents != null && documents.length > 0) {
					company.updateCoordinates(Double.valueOf(documents[0].y()),
							Double.valueOf(documents[0].x()));
				}
			}
			companies.add(company);
		}
		return companies;
	}

}
