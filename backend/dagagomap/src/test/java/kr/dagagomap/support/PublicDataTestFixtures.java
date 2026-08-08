package kr.dagagomap.support;

import kr.dagagomap.entity.Company;
import kr.dagagomap.infrastructure.api.kakao.local.dto.AddressToCoordinatesConversionResponse;
import kr.dagagomap.infrastructure.api.publicdata.busan.dto.BusanPublicDataResponse;

public final class PublicDataTestFixtures {

	private PublicDataTestFixtures() {
	}

	public static BusanPublicDataResponse page(int totalCount, int pageNo, int numOfRows, BusanPublicDataResponse.Body.Item... items) {
		return new BusanPublicDataResponse(
				new BusanPublicDataResponse.Header(0, "OK"),
				new BusanPublicDataResponse.Body(totalCount, numOfRows, pageNo, items));
	}

	public static BusanPublicDataResponse.Body.Item item(String taxId, String name, String address) {
		return new BusanPublicDataResponse.Body.Item(
				name,
				"https://example.com",
				"업종",
				"해운대구",
				"대표자",
				taxId,
				"2024-01-01",
				address,
				"051-000-0000",
				"test@example.com",
				"Y",
				"회사소개",
				"우대내용",
				"Y",
				"image.png",
				"Y");
	}

	public static Company existingCompany(long taxId, String name, String address) {
		return Company.builder()
				.taxId(taxId)
				.name(name)
				.homepageUrl("https://old.example.com")
				.category("업종")
				.gu("해운대구")
				.ceoName("대표자")
				.beginDate("2023-01-01")
				.sourceAddress(address)
				.tel("051-111-1111")
				.email("old@example.com")
				.emailFlag("N")
				.description("기존 소개")
				.benefit("기존 우대")
				.usageStatus("Y")
				.img("old.png")
				.webFlag("Y")
				.build();
	}

	public static Company existingCompanyMatching(BusanPublicDataResponse.Body.Item item) {
		return item.toCompany();
	}

	public static AddressToCoordinatesConversionResponse coordinates(double latitude, double longitude) {
		return new AddressToCoordinatesConversionResponse(
				new AddressToCoordinatesConversionResponse.Meta(1, 1, true),
				new AddressToCoordinatesConversionResponse.Document[] {
						new AddressToCoordinatesConversionResponse.Document(
								"부산광역시",
								"ROAD",
								String.valueOf(longitude),
								String.valueOf(latitude),
								null,
								null)
				});
	}

}
